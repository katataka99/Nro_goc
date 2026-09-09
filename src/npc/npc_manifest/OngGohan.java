package npc.npc_manifest;

/**
 * @author HM
 */
import consts.ConstDailyGift;
import consts.ConstNpc;
import consts.ConstTask;
import consts.ConstTaskBadges;
import item.Item;

import java.util.ArrayList;
import java.util.List;

import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import player.dailyGift.DailyGiftService;
import server.Client;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;
import task.Badges.BadgesTaskService;
import utils.Util;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    int costNapVang = 1;

    int[][] napVang = {{20000, 40}, {50000, 105}, {100000, 250}, {500000, 1500}, {1000000, 3100},
    {2000000, 6500}, {5000000, 17000}};

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con cần ta giúp gì nào",
                        "Rút xu",
                        "Ngọc xanh",
                        "Mã\nQuà tặng",
                        "Từ chối");

            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        this.createOtherMenu(player, ConstNpc.RUT_XU,
                                "Ta đang giữ giúp con " + player.getSession().cash + " xu\nCon muốn rút xu vào túi không?",
                                "Rút", "Đóng");
                        break;
                    case 1:
                        Input.gI().createFormNapNgocXanh(player);
                        break;
                    case 2:
                        Input.gI().createFormGiftCode(player);
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_TIEN) {
                switch (select) {
                    case 0:
                        List<String> menu = new ArrayList<>();
                        for (int i = 0; i < napVang.length; i++) {
                            menu.add(i, Util.mumberToLouis(napVang[i][0]) + "\n"
                                    + Util.mumberToLouis(napVang[i][1] * costNapVang) + " Thỏi\nvàng");
                        }
                        String[] menus = menu.toArray(new String[0]);
                        createOtherMenu(player, ConstNpc.NAP_VANG, "Ta sẽ giữ giúp con\n"
                                + "Nếu con cần dùng tới hãy quay lại đây gặp ta!", menus);
                        break;
                    case 1:
                        if (player.getSession().goldBar > 0) {
                            List<Item> listItem = new ArrayList<>();
                            Item thoiVang = ItemService.gI().createNewItem((short) 457, player.getSession().goldBar);
                            listItem.add(thoiVang);
                            if (InventoryService.gI().getCountEmptyBag(player) < listItem.size()) {
                                Service.gI().sendThongBao(player,
                                        "Cần ít nhất " + listItem.size() + " ô trống trong hành trang");
                            }
                            for (Item it : listItem) {
                                InventoryService.gI().addItemBag(player, it);
                                InventoryService.gI().sendItemBag(player);
                            }
                            Service.gI().sendThongBao(player,
                                    "Bạn đã nhận được " + player.getSession().goldBar + " thỏi vàng");
                            PlayerDAO.subGoldBar(player, -(napVang[select][1] * costNapVang));
                            PlayerDAO.subGoldBar(player, player.getSession().goldBar);
                        }
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_VANG) {
                if (player.getSession().cash >= napVang[select][0]) {
                    List<Item> listItem = new ArrayList<>();
                    if (InventoryService.gI().getCountEmptyBag(player) < listItem.size()) {
                        Service.gI().sendThongBao(player,
                                "Cần ít nhất " + listItem.size() + " ô trống trong hành trang");
                    }
                    for (Item it : listItem) {
                        InventoryService.gI().addItemBag(player, it);
                        InventoryService.gI().sendItemBag(player);
                    }
                    PlayerDAO.subcash(player, napVang[select][0]);
                    BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.DAI_GIA_MOI_NHU, napVang[select][0]);
                    PlayerDAO.subGoldBar(player, -(napVang[select][1] * costNapVang));
                    Service.gI().sendThongBao(player,
                            "Bạn có thêm " + Util.mumberToLouis(napVang[select][1] * costNapVang) + " thỏi vàng.");
                } else {
                    Service.gI().sendThongBao(player, "Không đủ số dư");
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.RUT_XU) {
                if (player.getSession().cash > 0) {
                    if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                        Service.gI().sendThongBao(player, "Cần ít nhất 2 ô trống trong hành trang.");
                        return;
                    }
                    long cash = player.getSession().cash; 
                    Item xu_vang = ItemService.gI().createNewItem((short) 1118);
                    xu_vang.quantity += cash - 1; 
                    InventoryService.gI().addItemBag(player, xu_vang);
                    InventoryService.gI().sendItemBag(player);
                    PlayerDAO.subcash(player, (int) cash);
                    Service.gI().sendThongBao(player, "Rút thành công " + cash + " xu vào hành trang.");
                } else {
                    Service.gI().sendThongBao(player, "Ta không giữ xu nào của con.");
                }
            }
        }
    }
}
