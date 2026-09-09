package boss;

/*
 *
 *
 * @author HM
 */
import server.Maintenance;

public class GasDestroyManager extends BossManager {

    private static GasDestroyManager instance;

    public static GasDestroyManager gI() {
        if (instance == null) {
            instance = new GasDestroyManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    if (i < this.bosses.size()) {
                        Boss boss = this.bosses.get(i);
                        try {
                            boss.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                removeBoss(boss);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
                Thread.sleep(Math.max(1, 150 - (System.currentTimeMillis() - st)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
