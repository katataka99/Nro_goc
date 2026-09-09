package boss;

/*
 *
 *
 *
 */

public class ThoDauBacManager extends BossManager {

    private static ThoDauBacManager instance;

    public static ThoDauBacManager gI() {
        if (instance == null) {
            instance = new ThoDauBacManager();
        }
        return instance;
    }

}