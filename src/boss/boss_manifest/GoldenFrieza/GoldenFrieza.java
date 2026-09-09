package boss.boss_manifest.GoldenFrieza;

/*
 *
 *
 * @author HM
 */
import boss.*;
import consts.ConstPlayer;
import item.Item;
import item.Item.ItemOption;
import java.util.List;
import map.ItemMap;
import player.Player;
import server.Manager;
import services.EffectSkillService;
import services.Service;
import utils.Util;

import java.util.Random;
import mob.Mob;
import network.Message;
import services.ItemService;
import services.MapService;
import services.PlayerService;
import services.SkillService;
import services.func.ChangeMapService;
import utils.SkillUtil;
import utils.TimeUtil;

public class GoldenFrieza extends Boss {

    private int status;
    private long lastStatusChange;
    private int timeChanges;
    private boolean callDeathBeam;

    public GoldenFrieza() throws Exception {
        super(BossID.GOLDEN_FRIEZA, BossesData.GOLDEN_FRIEZA);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();

        // 20% rơi item id 629 với option
        if (Util.isTrue(20, 100)) {
            ItemMap itemMap = new ItemMap(
                    plKill.zone,
                    629,
                    1,
                    plKill.location.x,
                    plKill.zone.map.yPhysicInTop(plKill.location.x, plKill.location.y - 24),
                    plKill.id
            );
            itemMap.options.add(new ItemOption(50, Util.nextInt(15, 22)));
            itemMap.options.add(new ItemOption(77, Util.nextInt(15, 22)));
            itemMap.options.add(new ItemOption(103, Util.nextInt(15, 22)));
            itemMap.options.add(new ItemOption(99, Util.nextInt(10, 15)));
            if (Util.isTrue(30, 100)) {
                int[] dates = {7, 15, 30};
                int hanSuDung = dates[Util.nextInt(0, dates.length - 1)];
                itemMap.options.add(new ItemOption(93, hanSuDung));
            }
            Service.gI().dropItemMap(plKill.zone, itemMap);
        }

        for (int i = 0; i < Util.nextInt(3, 5); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 76, Util.nextInt(500_000, 1_500_000),
                    this.location.x + i * 10,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id));
        }

        // 50% rơi item ID 987
        if (Util.isTrue(50, 100)) {
            Item item = ItemService.gI().createNewItem((short) 987);
            ItemMap itemMap = new ItemMap(
                    plKill.zone,
                    item.template.id,
                    1,
                    plKill.location.x + Util.nextInt(-10, 10),
                    plKill.zone.map.yPhysicInTop(plKill.location.x, plKill.location.y - 24),
                    plKill.id
            );
            Service.gI().dropItemMap(plKill.zone, itemMap);
        }

        // 100% rơi item ID 1118
        Item item = ItemService.gI().createNewItem((short) 1118);
        ItemMap itemMap = new ItemMap(
                plKill.zone,
                item.template.id,
                1,
                plKill.location.x + Util.nextInt(-10, 10),
                plKill.zone.map.yPhysicInTop(plKill.location.x, plKill.location.y - 24),
                plKill.id
        );
        Service.gI().dropItemMap(plKill.zone, itemMap);
    }

    @Override
    public void active() {
        super.active();
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
        if (!TimeUtil.is21H()) {
            this.leaveMap();
        }
    }

    @Override
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        if (this.zone != null) {
            for (Mob mob : this.zone.mobs) {
                mob.injured(this, 99999999, true);
            }
            this.zone.isGoldenFriezaAlive = true;
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            if (Util.canDoWithTime(lastStatusChange, timeChanges)) {
                callDeathBeam = false;
                timeChanges = Util.nextInt(5000, 10000);
                lastStatusChange = System.currentTimeMillis();
                status = Util.nextInt(3);
            }
            try {
                switch (status) {
                    case 0:
                        setBom();
                        timeChanges = 5000;
                        break;
                    case 1:
                        if (callDeathBeam) {
                            boolean checkDeathBeamDie = true;
                            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                                if (boss.bossStatus != BossStatus.REST) {
                                    checkDeathBeamDie = false;
                                }
                            }
                            if (checkDeathBeamDie) {
                                status = 2;
                                lastStatusChange = System.currentTimeMillis();
                                timeChanges = 30000;
                            }
                            return;
                        }
                        callDeathBeam = true;
                        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                            if (boss.bossStatus == BossStatus.REST) {
                                boss.changeStatus(BossStatus.RESPAWN);
                            }
                        }
                        timeChanges = 15000;
                        break;
                    default:
                        timeChanges = 30000;
                        Player pl = getPlayerAttack();
                        if (pl == null || pl.isDie()) {
                            return;
                        }
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                        if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                            if (Util.isTrue(5, 20)) {
                                if (SkillUtil.isUseSkillChuong(this)) {
                                    this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                                } else {
                                    this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                                }
                            }
                            SkillService.gI().useSkill(this, pl, null, -1, null);
                            checkPlayerDie(pl);
                        } else {
                            if (Util.isTrue(1, 2)) {
                                this.moveToPlayer(pl);
                            }
                        }
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setBom() {
        if (this.playerSkill.prepareTuSat) {
            return;
        }
        new Thread(() -> {
            if (!this.playerSkill.prepareTuSat) {
                this.playerSkill.prepareTuSat = true;
                this.playerSkill.lastTimePrepareTuSat = System.currentTimeMillis();
                Message msg;
                try {
                    msg = new Message(-45);
                    msg.writer().writeByte(7);
                    msg.writer().writeInt((int) this.id);
                    msg.writer().writeShort(104);
                    msg.writer().writeShort(2000);
                    Service.gI().sendMessAllPlayerInMap(this, msg);
                    msg.cleanup();
                } catch (Exception e) {
                }
            }

            while (this.playerSkill.prepareTuSat && this.zone != null) {
                if (Util.canDoWithTime(this.playerSkill.lastTimePrepareTuSat, 2500)) {
                    this.playerSkill.prepareTuSat = false;
                    List<Player> playersMap = this.zone.getNotBosses();
                    if (!MapService.gI().isMapOffline(this.zone.map.mapId)) {
                        for (Player pl : playersMap) {
                            if (!this.equals(pl)) {
                                pl.injured(this, 2_100_000_000, true, false);
//                            pl.setDie();
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                Service.gI().Send_Info_NV(pl);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void leaveMap() {
        this.zone.isGoldenFriezaAlive = false;
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
