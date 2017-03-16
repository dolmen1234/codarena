package arena;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Tournament {

    private static final int nbParalleArena = 4;

    private static final int botPerArena = 2;

    private final boolean noDraw = true;

    public void execute(String reference, String programFolder, long dureeMax) throws InterruptedException,
            ExecutionException {
        execute(new File(reference), new File(programFolder), dureeMax);
    }

    public void execute(File reference, File programFolder, long dureeMax) throws InterruptedException,
            ExecutionException {

        List<Program> programs = new ArrayList<Program>();
        programs.add(new Program(reference));
        for (File programFile : programFolder.listFiles()) {
            if ((programFile.isDirectory() && !programFile.getName().equals("ignore"))
                    || (programFile.isFile() && programFile.getName().indexOf(".jar") != -1)) {
                programs.add(new Program(programFile));
            }
        }

        long start = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(nbParalleArena);
        int nbMatches = 0;
        int pctPalier = 10;
        List<Arena> arenas = new ArrayList<Arena>();
        do {
            arenas.clear();
            // fill the queue
            while (arenas.size() < nbParalleArena) {
                nbMatches++;
                arenas.add(createArena(programs, nbMatches + 1));
            }

            List<Future<List<Bot>>> futures = executorService.invokeAll(arenas);
            for (Future<List<Bot>> future : futures) {
                future.get();
                addResult(future.get(), programs);
            }
            double pctAvancement = 100D * (System.currentTimeMillis() - start) / (1000D * dureeMax);
            if (pctAvancement > pctPalier && pctPalier < 95D) {
                System.out.println();
                System.out.println("Completed " + (int) pctAvancement + "%");
                printResults(programs);
                pctPalier += 10D;
            }
        } while (System.currentTimeMillis() - start < 1000 * dureeMax);

        System.out.println();
        System.out.println("Final results for " + nbMatches + " matches : ");
        printResults(programs);
    }

    private void printResults(List<Program> programs) {
        System.out.println("Program\tWin\tLose\tWinrate");
        Collections.sort(programs);
        for (Program program : programs) {
            System.out.println(program);
        }

    }

    private void addResult(List<Bot> bots, List<Program> programs) {
        if (noDraw && bots.size() == 2 && bots.get(0).getFinalRank() == bots.get(1).getFinalRank()) {
            // draw not used in stats
            return;
        }
        for (Bot bot : bots) {
            for (Program program : programs) {
                if (program.getCommandLine().equals(bot.getCommandLine())) {
                    program.addResult(bot.getFinalRank());
                }
            }

        }

    }

    private Arena createArena(List<Program> programs, int index) {
        List<Bot> bots = new ArrayList<Bot>();
        do {
            Bot bot = programs.get(new Random().nextInt(programs.size())).createBot();
            if (!bots.contains(bot)) {
                bots.add(bot);
            }
        } while (bots.size() < botPerArena);

        Arena arena = new Arena(index);
        arena.setBots(bots);
        return arena;
    }

}
