package arena;

import games.GameOverException;
import games.InvalidFormatException;
import games.InvalidInputException;
import games.LostException;
import games.MultiReferee;
import games.WinException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;

import arena.Bot;

public class Arena implements Callable<List<Bot>> {

    private static final String GAME_NAME = "GhostInTheCell";

    private final int index;
    
    private List<Bot> bots;

    private int nbBots;

    private MultiReferee referee;
    
    private static final Random rnd = new Random();
    
    public Arena(int index) {
        super();
        this.index = index;
    }
    
    
    private void init() throws IOException, InvalidFormatException {
        referee = MultiReferee.create(GAME_NAME, null, null, null);
        Properties prop = new Properties();
        prop.setProperty("seed", String.valueOf(rnd.nextLong()));
        referee.initReferee(bots.size(), new Properties());
        
        for (int i = 0 ; i < nbBots ; i++) {
            try {
                bots.get(i).init(referee.getInitInputForPlayer(i));
            } catch (Exception excp) {
                System.err.println(excp);
                bots.get(i).die(0);
            }
        }
    }
    
    
    /**
     * 
     * @param bots
     * @throws IOException
     * @throws InvalidFormatException 
     */
    public void setBots(List<Bot> bots) {
        this.bots = bots;
        nbBots = bots.size();
    }
    
    private void updateRanks() {
        
        for (int i = 0 ; i < bots.size() ; i++) {
            bots.get(i).setScore(referee.getScore(i));
        }
        List<Bot> sortedBots = new ArrayList<Bot>();
        sortedBots.addAll(bots);
        
        Comparator<Bot> comparator = new Comparator<Bot>() {

            @Override
            public int compare(Bot b1, Bot b2) {
                if (b1.isDead()) {
                    if (!b2.isDead()) {
                        return 1;
                    } else return b2.getDeadAtTurn() - b1.getDeadAtTurn();
                } else if (b2.isDead()) {
                    return -1;
                }
                return  b2.getScore() - b1.getScore(); 
            }
        };
        
        Collections.sort(sortedBots, comparator);
        
        for (int i = 0 ; i < sortedBots.size() ; i++) {
            Bot bot = sortedBots.get(i);
            if (i > 0 && comparator.compare(bot, sortedBots.get(i-1)) == 0) {
                bot.setFinalRank(sortedBots.get(i-1).getFinalRank());
            } else {
                sortedBots.get(i).setFinalRank(i);
            }
        }
        Bot winner = sortedBots.get(0);
        Bot loser = sortedBots.get(1);

        StringBuilder result = new StringBuilder();
        result.append("Game ").append(index).append("\t");
        result.append(winner.getSmallCommandLine());
        if (winner.getFinalRank() < loser.getFinalRank()) {
            result.append("\twins vs\t");
        } else {
            result.append("\tdraw vs\t");
        }
        result.append(loser.getSmallCommandLine());
        result.append("\t(");
        if (winner.isDead()) {
            if (loser.isDead()) {
                result.append("(death at ").append(winner.getDeadAtTurn()).append(" vs ").append(loser.getDeadAtTurn());
            } else {
                throw new IllegalStateException("Cannot be 1st if dead and not the other");
            }
        } else if (loser.isDead()) {
            result.append("kill at turn ").append(loser.getDeadAtTurn());
        } else {
            result.append(winner.getScore()).append("-").append(loser.getScore());
        }
        result.append(')');
        
        if (winner == bots.get(0)) {
            result.append("\tpos 1");
        } else {
            result.append("\tpos 2");
        }
        System.out.println(result);
    }
    @Override
    public List<Bot> call() throws Exception {
        if (bots == null) {
            throw new IllegalArgumentException("Missing bots");
        }

        play();
        
        updateRanks();
        
        for (Bot bot : bots) {
            bot.close();
        }
        return bots;
    }
    
    private void play() throws IOException, InvalidFormatException {
        init();
        
        if (noMoreAliveBot()) {
            System.err.println("start failed");
            return;
        }


        int nbMaxTurns = referee.getMaxRoundCount(bots.size());
        for (int turn = 0 ; turn < nbMaxTurns ; turn++) {
            sendInputs(turn);
            
            if (noMoreAliveBot()) {
                System.err.println("all dead after send input");
                return;
            }

            receiveOutputs(turn);
            if (noMoreAliveBot()) {
                System.err.println("all dead after receive input");
                return;
            }
            
            try {
                referee.updateGame(turn);
            } catch (GameOverException e) {
                return;
            }
        }
    }

    private void die(int playerIdx, int turn) {
        bots.get(playerIdx).die(turn);
    }
    
    private void receiveOutputs(int turn) {
        for (int i = 0 ; i < nbBots ; i++) {
            Bot bot = bots.get(i);
            if (bot.isDead()) {
                continue;
            }
            String [] outputs = new String[referee.getExpectedOutputLineCountForPlayer(i)];
            try {
                String output = "";
//                long timeout = referee.getMillisTimeForRound(turn);
//                long start = System.currentTimeMillis();
//                Thread.sleep(timeout*2);
                output = bot.receiveOutput();
                /** cannot manage timeout when multi threading, too much perturbation */
//                if (System.currentTimeMillis() - start > timeout) {
//                    System.err.println("timeout "+(System.currentTimeMillis() - start)+" > "+referee.getMillisTimeForRound(turn));
//                    die(i, turn);
//                    referee.setPlayerTimeout(0, turn, i);
//                    continue;
//                }
                
                if (outputs.length != referee.getExpectedOutputLineCountForPlayer(i)) {
                    System.err.println("wrong ouput count "+outputs.length);
                    die(i, turn);
                    continue;
                }
                outputs[0] = output;
            } catch (Exception excp) {
                System.err.println(excp);
                die(i, turn);
                continue;
            }

            try {
                referee.handlePlayerOutput(0, turn, i, outputs);
            } catch (WinException excp) {
                throw new IllegalArgumentException("win exception not implemented");
            } catch (LostException | InvalidInputException excp) {
                System.err.println(excp);
                die(i, turn);
            }
        }
    }

    private void sendInputs(int turn) {
        for (int i = 0 ; i < nbBots ; i++) {
            String[] input = referee.getInputForPlayer(turn, i);
            try {
                bots.get(i).sendInput(input);
            } catch (Exception excp) {
                System.err.println(excp);
                die(i, turn);
            }
        }
    }

    private boolean noMoreAliveBot() {
        int nbAlive = 0;
        for (Bot bot : bots) {
            if (!bot.isDead()) {
                nbAlive++;
            }
        }
        return nbAlive < 2;
    }
    
}
