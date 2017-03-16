import java.util.concurrent.ExecutionException;

import org.junit.Test;

import arena.Tournament;


public class TournamentTest {

    @Test
    public void execute() throws InterruptedException, ExecutionException {
        Tournament tournament = new Tournament();
        tournament.execute("D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\sync\\compiled", "D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\archives", 3*60);
        
    }
    
    
    
}
