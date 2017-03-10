package arena;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Program implements Comparable<Program> {

    private final String commandLine;

    public static int MAX_RANK = 1;
    private Map<Integer, Integer> rankCounts;

    Program(String commandLine) {
        this.commandLine = commandLine;
        rankCounts = new HashMap<Integer, Integer>();
        for (int i = 0; i <= MAX_RANK; i++) {
            rankCounts.put(i, 0);
        }
    }

    Program(File file) {
        this(file.getAbsolutePath());
    }

    void addResult(int rank) {
        MAX_RANK = Math.max(rank, MAX_RANK);
        Integer rankCount = rankCounts.get(rank);
        if (rankCount == null) {
            rankCounts.put(rank, 1);
        } else {
            rankCounts.put(rank, rankCount + 1);

        }
    }

    public Bot createBot() {
        return new Bot(commandLine);
    }

    public String toString() {
        StringBuilder str = new StringBuilder(getSmallCommandLine());
        str.append('\t');
        int nbMatchs = 0;
        for (int i = 0; i <= MAX_RANK; i++) {
            Integer rankCount = rankCounts.get(i);
            str.append(rankCount == null ? 0 : rankCount).append('\t');
            nbMatchs += rankCount;
        }
        if (nbMatchs == 0) {
            str.append('-');
        } else {
            int pct = rankCounts.get(0).intValue() * 100 / nbMatchs;
            str.append(pct).append('%');
        }
        return str.toString();

    }

    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public int compareTo(Program other) {
        for (int i = 0; i <= MAX_RANK; i++) {
            int count = rankCounts.get(i);
            int countOther = other.rankCounts.get(i);
            if (count > countOther) {
                return -1;
            }
            if (count < countOther) {
                return 1;
            }
        }
        return 0;
    }

    public String getSmallCommandLine() {
        String str = commandLine.substring(commandLine.lastIndexOf('\\') + 1);
        int idx = str.lastIndexOf('.');
        if (idx == -1) {
            return str;
        }
        return str.substring(0, idx);
    }

}
