package mob.bigboss_manifest;

/**
 *
 * @author HM
 */
import java.util.ArrayList;
import java.util.List;

import map.ItemMap;
import mob.BigBoss;
import mob.Mob;
import network.Message;
import player.Player;
import services.Service;
import utils.Util;

public class GaChinCua extends BigBoss {

    public GaChinCua(Mob mob) {
        super(mob);
    }

    @Override
    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        damage = this.point.hp / 100 > 0 ? this.point.hp / 100 : 1;
        super.injured(plAtt, damage, false);
    }

    private boolean isDrop = false; // thêm biến này vào class

    @Override
    public void update() {
        super.update();

        if (isDie() && !isDrop) {
            isDrop = true; // đảm bảo chỉ drop 1 lần khi chết
            ItemMap it = new ItemMap(this.zone, 1221, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), -1);
            Service.gI().dropItemMap(this.zone, it);
        }

        // Hồi sinh sau 10 phút
        if (isDie() && (System.currentTimeMillis() - lastTimeDie) > 300000) {
            lvMob = 0;
            action = 0;
            isDrop = false;
            this.location.x = 828;
            this.location.y = 432;
            this.point.hp = this.point.getHpFull();
            Service.gI().sendBigBoss2(this.zone, action, this);
            Message msg = null;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(this.point.gethp());
                msg.writer().writeInt(1);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    @Override
    public void attack() {
        if (!isDie() && !effectSkill.isHaveEffectSkill() && Util.canDoWithTime(lastBigBossAttackTime, 3000)) {
            // 10 : di chuyển, 11 - 20 : tấn công, 21 : bay, 22 : ..., 23 : die

            if (this.zone.getNotBosses().isEmpty()) {
                return;
            }
            List<Player> players = new ArrayList<>();
            action = Util.nextInt(11, 12);

            switch (action) {
                case 11 -> {
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 50) {
                            players.add(pl);
                            break;
                        }
                    }
                }
                case 12 -> {
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 100) {
                            players.add(pl);
                            break;
                        }
                    }
                }
            }

            if (players.isEmpty()) {
                int index = Util.nextInt(0, this.zone.getNotBosses().size() - 1);
                players.add(this.zone.getNotBosses().get(index));
                action = 10;
//                return;
            }

            Message msg = null;
            try {
                msg = new Message(102);
                msg.writer().writeByte(action);
                msg.writer().writeByte(this.id);
                switch (action) {
                    case 10 -> {
                        for (Player player : players) {
                            this.location.x = player.location.x + Util.nextInt(-10, 10);
                            this.location.y = player.location.y;
                        }
                        msg.writer().writeShort(this.location.x);
                        msg.writer().writeShort(this.location.y);
                    }
                    case 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> {
                        msg.writer().writeByte(players.size()); // sl player;
                        int dir = 0;
                        for (Player pl : players) {
                            int dame = pl.injured(null, this.point.getDameAttack(), false, true);
                            msg.writer().writeInt((int) pl.id); // id player
                            msg.writer().writeInt(dame); // dame
                            dir = pl.location.x < this.location.x ? -1 : 1;
                        }
                        msg.writer().writeByte(dir); // dir
                    }
                    case 22 -> {
                    }
                    case 23 -> {
                    }
                    default -> {
                    }
                }
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                lastBigBossAttackTime = System.currentTimeMillis();
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

}
