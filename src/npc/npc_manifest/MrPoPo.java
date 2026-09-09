package npc.npc_manifest;

import Top.TopPowerManager_2;
import consts.ConstNpc;
import npc.Npc;
import player.Player;
import tinhnang.test.TopService;

/**
 *
 * @author Administrator
 */
public class MrPoPo extends Npc {

    public MrPoPo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 0, 7, 14 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Sự kiện ĐUA TOP TOÀN DÂN đã bắt đầu!\n"
                            + "Ngươi muốn xem bảng xếp hạng nào",
                            "Top Nhiệm Vụ", "Top Sức Mạnh", "Top Nạp"
                    //                            "Top Cà Rốt"
                    );
                }
                default ->
                    super.openBaseMenu(player);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 ->
                        TopService.showListTop(player, 0);
                    case 1 ->
                        TopPowerManager_2.showTopPower(player);
                    case 2 ->
                        TopService.showListTop(player, 3);
                    case 3 ->
                        TopService.showListTop(player, 4);
                }
            }
        }
    }
}
