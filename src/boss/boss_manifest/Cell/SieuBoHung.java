package boss.boss_manifest.Cell;

/*
 *
 *
 * @author HM
 */
import consts.ConstPlayer;
import boss.Boss;
import boss.BossID;
import boss.BossManager;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import server.Manager;
import services.*;
import utils.Util;

import java.util.Random;

public class SieuBoHung extends Boss {

    private long st;
    public boolean callCellCon;
    private long lastTimeHapThu;
    private int timeHapThu;

    private final String text[] = {"Thưa quý vị và các bạn, đây đúng là trận đấu trời long đất lở",
        "Vượt xa mọi dự đoán của chúng tôi",
        "Eo ơi toàn thân lão Xên bốc cháy kìa"};
    private long lastTimeChat;
    private long lastTimeMove;
    private int indexChat = 0;

    public SieuBoHung() throws Exception {
        super(BossID.SIEU_BO_HUNG, BossesData.SIEU_BO_HUNG_1, BossesData.SIEU_BO_HUNG_2);
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callCellCon = false;
    }

    public void callCellCon() {
        new Thread(() -> {
            try {
                this.changeStatus(BossStatus.AFK);
                this.changeToTypeNonPK();
                this.recoverHP();
                this.callCellCon = true;
                this.chat("Hãy đấu với 7 đứa con của ta, chúng đều là siêu cao thủ");
                Thread.sleep(2000);
                this.chat("Cứ chưởng tiếp đi haha");
                Thread.sleep(2000);
                this.chat("Liệu mà giữ mạng đấy");
                Thread.sleep(2000);
                for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                    switch ((int) boss.id) {
                        case BossID.XEN_CON_1 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_2 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_3 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_4 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_5 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_6 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_7 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                    }
                }
            } catch (Exception e) {
            }
        }).start();
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }

    @Override
    public void reward(Player plKill) {
//        plKill.effect.addPointTrumSanBoss();
//        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
//        if (Util.isTrue(5, 50)) {
//            for (int i = 0; i < Util.nextInt(25, 50); i++) {
//                ItemMap it = new ItemMap(this.zone, 1229, 1, this.location.x + Util.nextInt(-15, 15), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
//                Service.gI().dropItemMap(this.zone, it);
//            }
//        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        plKill.effect.addPointTrumSanBoss();

        // Rơi 1 món ngẫu nhiên trong danh sách với tỉ lệ 50%
        if (Util.isTrue(50, 100)) {
            int[] listIds = {
                245, 257, 269, 241, 253, 265, 277, 249, 237, 261, 273, 233 
            };
            int randomIndex = Util.nextInt(0, listIds.length - 1);
            short itemId = (short) listIds[randomIndex];

            ItemMap it = new ItemMap(zone, itemId, 1,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);

            List<Item.ItemOption> options = ItemService.gI().getListOptionItemShop(itemId);
            for (Item.ItemOption opt : options) {
                int originalValue = opt.param;
                
                int variation = (int) (originalValue * Util.nextInt(1, 7) / 100.0);
                opt.param = Math.max(1, originalValue + variation); // Không cho nhỏ hơn 1
            }

            // Thêm option 107 với 20% tỉ lệ
            if (Util.isTrue(20, 100)) {
                int rand = Util.nextInt(1, 101);
                int value;
                if(rand <= 30){
                    value = 0;
                }else if  (rand <= 70) {
                    value = 1;
                } else if (rand <= 90) {
                    value = 2;
                } else if (rand <= 99) {
                    value = 3;
                } else {
                    value = 4;
                }
                options.add(new Item.ItemOption((byte) 107, (byte) value));
            }

            if (!options.isEmpty()) {
                it.options = options;
            }

            Service.gI().dropItemMap(this.zone, it);
        }

        if (Util.isTrue(5, 100)) {
            // 20% rơi đồ ID = 15
            short itTemp = 15;
            ItemMap it = new ItemMap(zone, itTemp, 1,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
            if (!ops.isEmpty()) {
                it.options = ops;
            }
            Service.gI().dropItemMap(this.zone, it);

        } else {
            // Nếu KHÔNG rơi đồ ID = 15 thì rơi đồ ID = 16
            short itTemp = 16;
            ItemMap it = new ItemMap(zone, itTemp, 1,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
            if (!ops.isEmpty()) {
                it.options = ops;
            }
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (prepareBom) {
            return 0;
        }
        if (!this.callCellCon && damage >= this.nPoint.hp) {
//            if (Util.isTrue(1, 3)) {
            this.callCellCon();
            return 0;
//            } else {
//                this.callCellCon = true;
//            }
        }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                setBom(plAtt);
                return 0;
            }
            return (int) damage;
        } else {
            return 0;
        }

    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        this.mc();
        if (this.currentLevel > 0) {
            if (this.bossStatus == BossStatus.AFK) {
                this.changeStatus(BossStatus.ACTIVE);
            }
        }
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    public void mc() {
        Player mc = zone.getNpc();
        if (mc != null) {
            if (Util.canDoWithTime(lastTimeChat, 3000)) {
                String textchat = text[indexChat];
                Service.gI().chat(mc, textchat);
                indexChat++;
                if (indexChat == text.length) {
                    indexChat = 0;
                    lastTimeChat = System.currentTimeMillis() + 7000;
                } else {
                    lastTimeChat = System.currentTimeMillis();
                }
            }

            if (Util.canDoWithTime(lastTimeMove, 15000)) {
                if (Util.isTrue(2, 3)) {
                    int x = this.location.x + Util.nextInt(-100, 100);
                    int y = x > 156 && x < 611 ? 288 : 312;
                    PlayerService.gI().playerMove(mc, x, y);
                }
                lastTimeMove = System.currentTimeMillis();
            }
        }
    }

}
