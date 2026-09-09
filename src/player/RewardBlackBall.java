package player;

/*
 *
 *
 * @author HM
 */
import item.Item;
import java.util.List;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.TimeUtil;
import utils.Util;

public class RewardBlackBall {

    private static final int TIME_REWARD = 79200000;

    public static final int R1S_1 = 15;
    public static final int R1S_2 = 21;
    public static final int R2S_1 = 20;
    public static final int R2S_2 = 20;
    public static final int R3S_1 = 35;
    public static final int R3S_2 = 10;
    public static final int R4S_1 = 10;
    public static final int R4S_2 = 35;
    public static final int R5S_1 = 35;
    public static final int R5S_2 = 20;
    public static final int R5S_3 = 20;
    public static final int R6S_1 = 40;
    public static final int R6S_2 = 20;
    public static final int R7S_1 = 10;
    public static final int R7S_2 = 15;

    public static final int TIME_WAIT = 3600000;
    public static long time8h;
    private Player player;

    public long[] timeOutOfDateReward;
    public int[] quantilyBlackBall;
    public long[] lastTimeGetReward;

    public RewardBlackBall(Player player) {
        this.player = player;
        this.timeOutOfDateReward = new long[7];
        this.lastTimeGetReward = new long[7];
        this.quantilyBlackBall = new int[7];
        time8h = TimeUtil.getStartTimeBlackBallWar();
    }

    public void reward(byte star) {
        if (this.timeOutOfDateReward[star - 1] > time8h) {
            quantilyBlackBall[star - 1]++;
        }
        this.timeOutOfDateReward[star - 1] = System.currentTimeMillis() + TIME_REWARD;
        Service.gI().point(player);
    }

    public void getRewardSelect(byte select) {
        int index = 0;
        for (int i = 0; i < timeOutOfDateReward.length; i++) {
            if (timeOutOfDateReward[i] > System.currentTimeMillis()) {
                index++;
                if (index == select + 1) {
                    getReward(i + 1);
                    break;
                }
            }
        }
    }

    private void getReward(int star) {
        if (timeOutOfDateReward[star - 1] > System.currentTimeMillis()
                && Util.canDoWithTime(lastTimeGetReward[star - 1], TIME_WAIT)) {
            switch (star) {
                case 1:
                case 2:
                    Service.gI().sendThongBao(player, "Phần thưởng chỉ số tự động nhận");
                    break;
                case 3:
                    Item tv = ItemService.gI().createNewItem((short) 595, 10);
                    tv.itemOptions.add(new Item.ItemOption(2, 256000)); 
                    InventoryService.gI().addItemBag(player, tv);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 10 hạt đậu 10");
                    lastTimeGetReward[star - 1] = System.currentTimeMillis();
                    break;
                case 4:
                    Item nro = ItemService.gI().createNewItem((short) 18, 1);
                    InventoryService.gI().addItemBag(player, nro);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 1 viên 5 sao");
                    lastTimeGetReward[star - 1] = System.currentTimeMillis();
                    break;
                case 5:
                    int[] listda = {220,
                        221,
                        222,
                        223,
                        224};
                    Item da = ItemService.gI().createNewItem((short) listda[Util.nextInt(4)], 3);
                    InventoryService.gI().addItemBag(player, da);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 1 đá nâng cấp");
                    lastTimeGetReward[star - 1] = System.currentTimeMillis();
                    break;

                case 6:
                    player.inventory.addGold(1_000_000);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được 1tr vàng");
                    lastTimeGetReward[star - 1] = System.currentTimeMillis();
                    break;
                case 7:
                    player.inventory.gem += R7S_1;
                    Service.gI().sendMoney(player);
                    lastTimeGetReward[star - 1] = System.currentTimeMillis();
                    Service.gI().sendThongBao(player, "Bạn nhận được 10 ngọc");
                    break;
            }
        } else {
            Service.gI().sendThongBao(player, "Chờ đợi là hạnh phúc :)");
        }
    }

    public void dispose() {
        this.player = null;
    }
}
