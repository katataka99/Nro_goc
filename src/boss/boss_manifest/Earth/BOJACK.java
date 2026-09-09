package boss.boss_manifest.Earth;

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
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class BOJACK extends Boss {

    private long st;

    public BOJACK() throws Exception {
        super(BossID.BOJACK, false, true, BossesData.BOJACK, BossesData.SUPER_BOJACK);
    }

    @Override
    public void reward(Player plKill) {
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
                138, 139, 142, 143, 147, 146, 150, 151,
                186, 187, 154, 155, 158, 159, 162, 163,
                166, 167, 170, 171, 174, 175, 178, 179,
                182, 183
            };
            int randomIndex = Util.nextInt(0, listIds.length - 1);
            short itemId = (short) listIds[randomIndex];

            ItemMap it = new ItemMap(zone, itemId, 1,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);

            List<Item.ItemOption> options = ItemService.gI().getListOptionItemShop(itemId);

            // Thêm option 107 với 10% tỉ lệ
            if (Util.isTrue(20, 100)) {
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
                options.add(new Item.ItemOption((byte) 107, (byte) value));
            }

            if (!options.isEmpty()) {
                it.options = options;
            }

            Service.gI().dropItemMap(this.zone, it);
        }

        // Rơi thêm 1 món khác (id 427) có option
        short itTemp = 427;
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

    @Override
    public void joinMap() {
        super.joinMap();
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

    @Override
    public void doneChatS() {
        if (this.currentLevel == 1) {
            return;
        }
        this.changeStatus(BossStatus.AFK);
    }
}
