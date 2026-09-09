package boss.boss_manifest.ThoDauBacDaiKa;

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import static boss.BossType.THO_DAU_BAC_DAI_KA;
import boss.BossesData;
import consts.ConstPlayer;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import utils.Util;

public class ThoDauBacDaiKa extends Boss {

    private long st;
    private int timeLeaveMap;

    public ThoDauBacDaiKa() throws Exception {
        super(THO_DAU_BAC_DAI_KA, BossID.THO_DAU_BAC_DAI_KA, true, true, BossesData.THO_DAU_BAC_DAI_KA);
    }

    @Override
    public void joinMap() {
        this.name = "Thỏ Đại Ka";
        this.zone = getMapJoin();
        ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
        this.changeStatus(BossStatus.CHAT_S);
//        this.name = this.data[this.currentLevel].getName();
//        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(1800000, 3600000);
    }

    @Override
    public void reward(Player plKill) {
        int quantity = Util.nextInt(1, 11); // random từ 1 đến 5
        for (int i = 0; i < quantity; i++) {
            int x = this.location.x + Util.nextInt(-15, 15);
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
            ItemMap item = new ItemMap(this.zone, 462, 1, x, y, plKill.id);
            Service.gI().dropItemMap(this.zone, item);
        }
    }

    @Override
public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
    if (!this.isDie()) {
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }
        if (this.currentLevel != 0) {
            damage /= 2;
        }
        damage = this.nPoint.subDameInjureWithDeff(damage - Util.nextInt(100000));

        // Nếu có khiên chắn sát thương
        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        // Giới hạn tối đa 5,000 sát thương
        if (damage > 5000) {
            damage = 5000;
        }

        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    } else {
        return 0;
    }
}

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeaveMap)) {
            if (Util.isTrue(1, 2)) {
                this.leaveMap();
            } else {
                this.leaveMapNew();
            }
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            timeLeaveMap = Util.nextInt(300000, 900000);
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}