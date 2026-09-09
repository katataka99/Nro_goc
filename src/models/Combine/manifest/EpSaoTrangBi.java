package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class EpSaoTrangBi {

    public static int getGem(int star) {
        return switch (star) {
            case 7 ->
                20;
            case 8 ->
                30;
            default ->
                10;
        };
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combine.itemsCombine) {
                if (item.canPhaLeHoa()) {
                    trangBi = item;
                } else if (item.isDaPhaLeEpSao()) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                for (Item.ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.combine.gemCombine = getGem(star);
                    String npcSay = trangBi.template.name + "\n|2|";
                    for (Item.ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id != 102) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }
                    if (daPhaLe.template.type == 30) {
                        for (Item.ItemOption io : daPhaLe.itemOptions) {
                            npcSay += "|7|" + io.getOptionString() + "\n";
                        }
                    } else {
                        npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(CombineService.gI().getOptionDaPhaLe(daPhaLe)).name.replaceAll("#", CombineService.gI().getParamDaPhaLe(daPhaLe) + "") + "\n";
                    }
                    npcSay += "|1|Cần " + Util.numberToMoney(player.combine.gemCombine) + " ngọc";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\ncần " + player.combine.gemCombine + " ngọc");

                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
        }
    }

    public static void epSaoTrangBi(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            int gem = player.combine.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combine.itemsCombine) {
                if (item.canPhaLeHoa()) {
                    trangBi = item;
                } else if (item.isDaPhaLeEpSao()) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                        optionStar = io;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.inventory.gem -= gem;
                    int optionId = CombineService.gI().getOptionDaPhaLe(daPhaLe);
                    int param = CombineService.gI().getParamDaPhaLe(daPhaLe);
                    Item.ItemOption option = null;
                    for (Item.ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (option != null) {
                        option.param += param;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(optionId, param));
                    }
                    if (optionStar != null) {
                        optionStar.param++;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(102, 1));
                    }

                    InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    CombineService.gI().sendEffectSuccessCombine(player);
                }
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}
