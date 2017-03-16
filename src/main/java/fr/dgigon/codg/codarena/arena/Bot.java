package arena;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class Bot {

    private int finalRank;
    private boolean isDead;
    private int deadAtTurn = 400;
    private int score;
    
    private final String commandLine;
    private Process process;
    private PrintStream out;
    private Scanner in;
    
    
    public Bot(String commandLine) {
        this.commandLine = commandLine;
    }

    public void init(String[] initInputForPlayer) throws IOException {
        ProcessBuilder builder;
        
        int idxClass = commandLine.indexOf(".jar");
        if (idxClass != -1) {
            builder = new ProcessBuilder("java", "-jar", commandLine);
        } else {
            builder = new ProcessBuilder("java", "Player");
            builder.directory(new File(commandLine));
        }
        builder.redirectError(Redirect.INHERIT);

        process = builder.start();
        out = new PrintStream(process.getOutputStream());
        in = new Scanner(process.getInputStream());
        sendInput(initInputForPlayer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bot other = (Bot) obj;
        if (commandLine == null) {
            if (other.commandLine != null)
                return false;
        } else if (!commandLine.equals(other.commandLine))
            return false;
        return true;
    }

    public void sendInput(String[] inputForPlayer) throws IOException {
        for (String line : inputForPlayer) {
            out.println(line);
        }
        out.flush();
        
    }

    public void die(int turn) {
        isDead = true;  
        deadAtTurn = turn;
    }

    public String receiveOutput() throws IOException {
        if (in.hasNextLine()) {
            return in.nextLine();
        }
        return "";
    }

    public String[] receiveOutput(int len) throws IOException {
        String[] output = new String[len];

        for (int i = 0 ; i < len ; i++) {
            output[i] = in.nextLine();
        }
        return output;
    }
    
    public void close() throws IOException {
        in.close();
        out.close();
        if (process != null) {
            process.destroy();
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public int getDeadAtTurn() {
        return deadAtTurn;
    }

    public int getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(int finalRank) {
        this.finalRank = finalRank;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(getSmallCommandLine());
        str.append('[').append(finalRank == 0 ? "1st" : ""+(1+finalRank)+"d").append(",s:").append(score);
        if (isDead) {
            str.append("(D").append(deadAtTurn);
        }
        str.append(']');
        return str.toString();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSmallCommandLine() {
        String str = commandLine.substring(commandLine.lastIndexOf('\\')+1);
        int idx = str.lastIndexOf('.');
        if (idx == -1) {
            return str; 
        }
        return str.substring(0, idx);
    }

    
    
    
}
