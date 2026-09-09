package boss.boss_manifest.Frieza;

/*
 *
 *
 * @author HM
 */
import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Fide extends Boss {

    private long st;

    public Fide() throws Exception {
        super(BossID.FIDE, BossesData.FIDE_DAI_CA_1, BossesData.FIDE_DAI_CA_2, BossesData.FIDE_DAI_CA_3);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();

        if (this.currentLevel == 0) {
            // Fide 1
            if (Util.isTrue(100, 100)) {
                ItemMap it = new ItemMap(this.zone, 19, 1,
                        this.location.x,
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                        plKill.id);
                Service.gI().dropItemMap(this.zone, it);
            }

        } else if (this.currentLevel == 1) {
            // Fide 2
            if (Util.isTrue(15, 100)) {
                ItemMap it2 = new ItemMap(this.zone, 18, 1,
                        this.location.x + 20,
                        this.zone.map.yPhysicInTop(this.location.x + 20, this.location.y - 24),
                        plKill.id);
                Service.gI().dropItemMap(this.zone, it2);
            }

        } else if (this.currentLevel == 2) {
            // Fide 3
            if (Util.isTrue(20, 100)) {
                ItemMap it3 = new ItemMap(this.zone, 17, 1,
                        this.location.x - 20,
                        this.zone.map.yPhysicInTop(this.location.x - 20, this.location.y - 24),
                        plKill.id);
                Service.gI().dropItemMap(this.zone, it3);
            }
        }

        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

}
