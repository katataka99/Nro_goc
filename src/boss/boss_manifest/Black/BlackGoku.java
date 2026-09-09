package boss.boss_manifest.Black;

/*
 *
 *
 * @author HM
 */
import boss.*;
import consts.ConstPlayer;
import consts.ConstTask;
import item.Item;
import map.ItemMap;
import player.Player;
import server.Manager;
import services.*;
import utils.Util;

import java.util.Random;

import utils.SkillUtil;

public class BlackGoku extends Boss {

    private long st;
    private int timeLeaveMap;

    public BlackGoku() throws Exception {
        super(BossID.BLACK_GOKU, false, true, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();
        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_31_0 && Util.isTrue(20, 100)) {
            ItemMap nhan = new ItemMap(this.zone, 992, 1, this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            nhan.options.add(new Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, nhan);
        }
        if (Util.isTrue(5, 100)) {
            ItemMap it = ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);

            // Thêm option 107 với 10% tỉ lệ
            if (Util.isTrue(10, 100)) {
                int rand = Util.nextInt(1, 101);
                int value;
                if (rand <= 64) {
                    value = 1;
                } else if (rand <= 89) {
                    value = 2;
                } else if (rand <= 99) {
                    value = 3;
                } else {
                    value = 4;
                }
                it.options.add(new Item.ItemOption(107, value));
            }

            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(10, 100)) {
            ItemMap it = new ItemMap(this.zone, 15, 1, this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(80, 100)) {
            ItemMap it = new ItemMap(this.zone, 16, 1, this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
//            if (this.currentLevel != 0) {
//                damage /= 2;
//            }
            damage = this.nPoint.subDameInjureWithDeff(damage - Util.nextInt(100000));
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
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
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(1500000, 18000000);
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
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
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
