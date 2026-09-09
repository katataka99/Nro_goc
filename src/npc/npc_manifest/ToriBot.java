package npc.npc_manifest;

import consts.ConstNpc;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import server.Manager;
import server.ServerManager;
import services.InventoryService;
import services.PetService;
import services.Service;
import services.func.QuaToriBot;
import services.func.UseItem;
import shop.ShopService;
import utils.Util;

public class ToriBot extends Npc {

    public ToriBot(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Trong thời gian mùa 1 diễn ra\nNếu mua VIP sẽ được nhận\nnhiều ưu đãi hơn nữa.\nLưu ý: nâng cấp mỗi VIP chỉ được nâng 1 lần/1 VIP",
                    "VIP 1", "VIP 2", "VIP 3", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 0 || this.mapId == 7 || this.mapId == 14) {
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            createOtherMenu(player, 2,
                                    "Nâng cấp VIP 1 bạn sẽ được"
                                    + "\nx20 sao pha lê tnsm"
                                    + "\nCải trang Karin Kid Lân 15 ngày"
                                    + "\nx3 hộp mù bé ba"
                                    + "\nVán bay mây mưa 15 ngày.",
                                    "20K", "Đóng");
                        case 1 ->
                            createOtherMenu(player, 4,
                                    "Nâng cấp VIP 2 bạn sẽ được"
                                    + "\nx10 đá bảo vệ"
                                    + "\nVán bay mây mưa vv"
                                    + "\nCT Karin múa Lân vv"
                                    + "\nLính bảo vệ tròn vv"
                                    + "\nx10 mảnh đội trưởng vàng"
                                    + "\nx5 hộp mù bé ba",
                                    "100K", "Đóng");
                        case 2 ->
                            createOtherMenu(player, 6,
                                    "Nâng cấp VIP 3 bạn sẽ được"
                                    + "\nx30 đá bảo vệ"
                                    + "\nVán bay máy bay 41 vv"
                                    + "\nCT Bunma Rider vv"
                                    + "\nCánh thiên thần ác quỷ vv"
                                    + "\nPet cá mập vv"
                                    + "\nx20 mảnh đội trưởng vàng"
                                    + "\nx10 hộp mù bé ba",
                                    "300K", "Đóng");
                    }
                } else if (player.iDMark.getIndexMenu() == 2) {
                    switch (select) {
                        case 0 -> {
                            QuaToriBot.Qua_1(player, true);
                        }
                    }
                } else if (player.iDMark.getIndexMenu() == 4) {
                    switch (select) {
                        case 0 -> {
                            QuaToriBot.Qua_2(player, true);
                        }
                    }
                } else if (player.iDMark.getIndexMenu() == 6) {
                    switch (select) {
                        case 0 -> {
                            QuaToriBot.Qua_3(player, true);
                        }
                    }
                } else if (player.iDMark.getIndexMenu() == 8) {
                    switch (select) {
                        case 0 -> {
                            QuaToriBot.Qua_4(player, true);
                        }
                    }
                }
            }
        }
    }
}
