package boss.boss_manifest.Android;

/*
 *
 *
 * @author HM
 */
import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Poc extends Boss {

    public Poc() throws Exception {
        super(BossID.POC, BossesData.POC);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        plKill.effect.addPointTrumSanBoss();

        // Rơi ngọc xanh (id 77) từ 20-30 viên 100% rơi
        int quantity = Util.nextInt(20, 31);
        Service.gI().dropItemMap(this.zone,
                new ItemMap(zone, 77, quantity,
                        this.location.x + Util.nextInt(-50, 50),
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                        plKill.id));

        // Rơi tiền (id 76) ngẫu nhiên 3-15 lần, mỗi lần 10,000 - 500,000
        for (int i = 0; i < Util.nextInt(3, 15); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 76, Util.nextInt(10_000, 500_000),
                    this.location.x + i * 10,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id));
        }

        // Rơi 1 món ngẫu nhiên trong danh sách với tỉ lệ 50%
        if (Util.isTrue(50, 100)) {
            int[] listIds = {
                242, 243, 246, 247, 250, 251
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
                // Tăng hoặc giảm từ -5% đến +5%
                int variation = (int) (originalValue * Util.nextInt(0, 6) / 100.0);

                opt.param = Math.max(1, originalValue + variation); // Không cho nhỏ hơn 1
            }

            // Thêm option 107 với 10% tỉ lệ
            if (Util.isTrue(20, 100)) {
                int rand = Util.nextInt(1, 101);
                int value;
                if (rand <= 64) {
                    value = 1;
                } else if (rand <= 89) {
                    value = 2;
                } else if (rand <= 98) {
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

//        if (Util.isTrue(10, 100)) {
        short itTemp = 17;
        ItemMap it = new ItemMap(zone, itTemp, 1,
                this.location.x + Util.nextInt(-50, 50),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);
        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }
        Service.gI().dropItemMap(this.zone, it);
//        }
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

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    private long st;

//    @Override
//    public void wakeupAnotherBossWhenDisappear() {
//        if (this.parentBoss != null && !this.parentBoss.isDie()) {
//            this.parentBoss.changeToTypePK();
//        }
//    }
    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null) {
            return;
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }
//    public void doneChatE() {
//        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
//                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
//            return;
//        }
//        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
//            if (boss.id == BossID.PIC && !boss.isDie()) {
//                boss.changeStatus(BossStatus.ACTIVE);
//                break;
//            }
//        }
//    }

}
