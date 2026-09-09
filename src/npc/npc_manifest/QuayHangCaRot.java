package npc.npc_manifest;

import consts.ConstNpc;
import item.Item;
import java.util.ArrayList;
import static jdbc.daos.PlayerDAO.addCarot;
import npc.Npc;
import static npc.npc_manifest.QuyLaoKame.finishLearningSkill;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import tinhnang.test.TopService;

/**
 *
 * @author Administrator
 */
public class QuayHangCaRot extends Npc {

    public QuayHangCaRot(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            menu.add("Đổi\nCà Rốt");
            menu.add("Top\nCà Rốt");
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                createOtherMenu(player, ConstNpc.BASE_MENU, "Sự Kiện Săn Thỏ Đại Ka - Quy Đổi Quà Lớn\n"
                        + "Cách tính điểm sự kiện:\n"
                        + "Mỗi lần đổi x99 Cà rốt sẽ nhận được 1 điểm "
                        + "Hoặc với mỗi lần nạp tích lũy 100k sẽ nhận được 1 điểm "
                        + "Bạn hãy cố gắng kiếm thật nhiều cà rốt mang về cho tôi nhé", menus);
                        
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        switch (player.iDMark.getIndexMenu()) {
            case ConstNpc.BASE_MENU ->
                handleBaseMenu(player, select);
            case ConstNpc.MENU_DOI_CA_ROT ->
                handleDoiCaRot(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        if (select == 0) {
            try {
                ArrayList<String> menu = new ArrayList<>();
                menu.add("Đổi Quà");
                menu.add("Từ Chối");
                String[] menus = menu.toArray(String[]::new);
                String caption = "|7|Quy đổi x99 Củ Cà Rốt để nhận 1 hộp quà\nHiện tại con đang có";
                Item item = InventoryService.gI().findItemBag(player, 462);
                if (item != null) {
                    caption += " x" + item.quantity + " Củ Cà Rốt";
                    if (item.quantity < 99) {
                        caption += "\n|2|Con đang thiếu x" + (99 - (item.quantity)) + " Củ Cà Rốt";
                    }
                } else {
                    caption += " 0 Củ Cà Rốt\n|2|Con đang thiếu x99 Củ Cà Rốt";
                }
                createOtherMenu(player, ConstNpc.MENU_DOI_CA_ROT, caption, menus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(select == 1){
             TopService.showListTop(player, 4);
        }
    }

    private void handleDoiCaRot(Player player, int select) {
        if (select == 0) {
            if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                Service.gI().sendThongBao(player, "Hành Trang Đã Đầy!");
                return;
            }
            Item item = InventoryService.gI().findItemBag(player, 462);
            if (item != null) {
                int quantity = item.quantity;
                short idHopQua = 1862;
                if (quantity >= 99) {
                    InventoryService.gI().subQuantityItemsBag(player, item, 99);
                    InventoryService.gI().addItemBag(player, ItemService.gI().createNewItem(idHopQua));
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Con nhận được Hộp Quà Cải Trang!");
                    addCarot(player, 1);
                } else {
                    Service.gI().sendThongBao(player, "Con không đủ x99 Củ Cà Rốt!");
                }
            } else {
                Service.gI().sendThongBao(player, "Con không đủ x99 Củ Cà Rốt!");
            }
        }

    }

}
