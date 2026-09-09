package player;

/**
 *
 * @author HM
 */
import java.util.ArrayList;
import java.util.List;
import models.SuperRank.SuperRankService;
import services.NpcService;
import services.Service;
import utils.TimeUtil;

public class SuperRank {

    private Player player;
    public int rank;
    public int win;
    public int lose;
    public List<String> history;
    public List<Long> lastTime;
    public long lastTimePK;
    public long lastTimeReward;
    public int ticket = 3;

    public SuperRank(Player player) {
        this.player = player;
        this.history = new ArrayList<>();
        this.lastTime = new ArrayList<>();
    }

    public void history(String text, long lastTime) {
        if (this.history.size() > 4) {
            this.history.remove(0);
            this.lastTime.remove(0);
        }
        this.history.add(text);
        this.lastTime.add(lastTime);
    }

    public void reward() {
        int rw = SuperRankService.gI().reward(rank);
        if (rw != -1) {
            NpcService.gI().createTutorial(player, -1, "Bạn đang ở TOP " + rank + " võ đài Siêu Hạng, được thưởng " + rw + " ngọc");
            player.inventory.gem += rw; 
        }
        lastTimeReward = TimeUtil.currentTimeMillisPlus11();
    }

    public void dispose() {
        history.clear();
        lastTime.clear();
        win = -1;
        lose = -1;
        lastTimePK = -1;
        player = null;
    }
}
