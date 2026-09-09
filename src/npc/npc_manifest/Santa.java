package npc.npc_manifest;

/**
 * @author HM
 */
import clan.Clan;
import consts.ConstNpc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import npc.Npc;
import player.Player;
import services.ChatGlobalService;
import services.InventoryService;
import services.Service;
import shop.ShopService;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Cửa hàng",
                    "Mở rộng\nHành trang\nRương đồ",
                    "Mở rộng\n Bang hội",
                    "Cửa hàng\nHạn sử dụng",
                    "Tiệm\nHớt tóc",
                    "Shop\nXu vàng"
            ));
            String[] menus = menu.toArray(new String[0]);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                if (player.iDMark.getIndexMenu() == ConstNpc.MENU_GIAM_GIA) {
                    switch (select) {
                        case 0 ->
                            ShopService.gI().opendShop(player, "SANTA", false);
                        case 1 ->
                            ShopService.gI().opendShop(player, "GIAM_GIA", false);
                    }
                }
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {// Cửa hàng
                            try {
                                if (InventoryService.gI().isExistItemBag(player, 459)) {

                                    List<String> menu = new ArrayList<>(Arrays.asList(
                                            "Cửa hàng",
                                            "Cửa hàng\ngiảm giá"));
                                    String[] menus = menu.toArray(new String[0]);
                                    createOtherMenu(player, ConstNpc.MENU_GIAM_GIA,
                                            "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);

                                } else {
                                    ShopService.gI().opendShop(player, "SANTA", false);
                                }

                            } catch (Exception e) {

                            }
                        }
                        case 1 -> // Mở rộng hành trang
                            ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
                        case 2 -> // Mở rộng bang hội
                            moGioiHanBang(player);
                        case 3 -> // Cửa hàng hạn sử dụng
                            ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
                        case 4 -> // Mua lại vật phẩm đã bán
                            ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                        case 5 ->
                            ShopService.gI().opendShop(player, "SANTA_XU", true);
                    }
                }
            }
        }
    }

    public void moGioiHanBang(Player player) {
    Clan clan = player.clan;
    if (clan == null) {
        Service.gI().sendThongBao(player, "Bạn cần phải vào bang hội.");
        return;
    }
    if (!clan.isLeader(player)) {
        Service.gI().sendThongBao(player, "Bạn cần phải là bang chủ để có thể mở giới hạn bang.");
        return;
    }
    if (player.inventory.gem < 100) {
        Service.gI().sendThongBao(player, "Bạn cần 100 ngọc xanh để có thể mở giới hạn bang.");
        return;
    }
    if (clan.maxMember >= 20) {
        Service.gI().sendThongBao(player, "Bang hội đã đạt giới hạn tối đa 20 thành viên, không thể mở thêm.");
        return;
    }

    clan.maxMember++;
    player.inventory.gem -= 100;
    Service.gI().sendThongBao(player, "Bang hội của bạn đã được mở rộng thêm 1 thành viên (tối đa 20).");
    ChatGlobalService.gI().chat(player, "Hahaha bang hội " + clan.name + " đã mở rộng lãnh địa.");
}
}
