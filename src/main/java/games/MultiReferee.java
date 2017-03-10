package games;


import games.gitc.Referee;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

public abstract class MultiReferee {

    public MultiReferee(InputStream is, PrintStream out, PrintStream err) {
        // TODO Auto-generated constructor stub
    }

    public abstract void initReferee(int playerCount, Properties prop) throws InvalidFormatException;

    protected Properties getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract String[] getInitInputForPlayer(int playerIdx);

    protected void prepare(int round) {
        // TODO Auto-generated method stub
        
    }

    public abstract String[] getInputForPlayer(int round, int playerIdx);

    public abstract int getExpectedOutputLineCountForPlayer(int playerIdx);

    public abstract void handlePlayerOutput(int frame, int round, int playerIdx, String[] outputs) throws WinException, LostException, InvalidInputException, InvalidInputException;

    public abstract void updateGame(int round) throws GameOverException;

    public abstract void populateMessages(Properties p);

    protected String[] getInitDataForView() {
        // TODO Auto-generated method stub
        return null;
    }

    protected String[] getFrameDataForView(int round, int frame, boolean keyFrame) {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract String getGameName();

    protected String getHeadlineAtGameStartForConsole() {
        // TODO Auto-generated method stub
        return null;
    }

    protected int getMinimumPlayerCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    protected boolean showTooltips() {
        // TODO Auto-generated method stub
        return false;
    }

    protected String[] getPlayerActions(int playerIdx, int round) {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean isPlayerDead(int playerIdx) {
        // TODO Auto-generated method stub
        return false;
    }

    protected String getDeathReason(int playerIdx) {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract int getScore(int playerIdx);

    protected String[] getGameSummary(int round) {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract void setPlayerTimeout(int frame, int round, int playerIdx);

    public abstract int getMaxRoundCount(int playerCount);

    public abstract int getMillisTimeForRound(int round);

    public static MultiReferee create(String gameName, InputStream is, PrintStream out, PrintStream err) throws IOException {
        if (Referee.NAME.equals(gameName)) {
            return new Referee(is, out, err);
        }
        throw new IllegalArgumentException("Unknown gane : "+gameName);
    }

}
