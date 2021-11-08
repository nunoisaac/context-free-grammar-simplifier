import java.io.FileNotFoundException;
import java.util.HashMap;

public class main {
    public static void main(String [] args) throws FileNotFoundException {

        CFG cfg = new CFG();
        cfg.readFromFile();
        System.out.println(cfg);

        cfg.removeERules();
        System.out.println(cfg);
        cfg.removeUnitRule();  //Comment out to run program without unit rule
        System.out.println(cfg);
        cfg.removeUselessRule();

        System.out.println(cfg);
    }
}
