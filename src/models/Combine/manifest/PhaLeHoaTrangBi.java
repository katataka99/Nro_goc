package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import server.ServerNotify;
import services.InventoryService;
import services.Service;
import utils.Util;

public class PhaLeHoaTrangBi {

    private static float getRatio(int star) {
        return switch (star) {
            case 0 ->
                80;
            case 1 ->
                60;
            case 2 ->
                30;
            case 3 ->
                10;
            case 4 ->
                5;
            case 5 ->
                3;
            case 6 ->
                1;
            default ->
                0;
        };
    }

    private static String getRatioStr(int star) {
        int ratio = (int) getRatio(star);
        if (ratio < 1) {
            ratio = 1;
        }
        return String.valueOf(ratio);
    }

    private static int getGold(int star) {
        return switch (star) {
            case 0 ->
                5_000_000;
            case 1 ->
                10_000_000;
            case 2 ->
                20_000_000;
            case 3 ->
                40_000_000;
            case 4 ->
                60_000_000;
            case 5 ->
                90_000_000;
            case 6 ->
                120_000_000;
            case 7 ->
                150_000_000;
            case 8 ->
                180_000_000;
            default ->
                0;
        };
    }

    private static int getGem(int star) {
        return switch (star) {
            case 0 ->
                10;
            case 1 ->
                20;
            case 2 ->
                30;
            case 3 ->
                40;
            case 4 ->
                50;
            case 5 ->
                60;
            case 6 ->
                70;
            default ->
                0;
        };
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 1) {
            Item item = player.combine.itemsCombine.get(0);
            if (item.canPhaLeHoa()) {
                int star = 0;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        break;
                    }
                }
                if (star < CombineService.gI().MAX_STAR_ITEM) {
                    player.combine.goldCombine = getGold(star);
                    player.combine.gemCombine = getGem(star);
                    player.combine.ratioCombine = getRatio(star);

                    String npcSay = item.template.name + "\n|2|";
                    for (Item.ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id != 102) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }
                    npcSay += "|7|Tỉ lệ thành công: " + player.combine.ratioCombine + "%" + "\n";
                    if (player.combine.goldCombine <= player.inventory.gold) {
                        npcSay += "|1|Cần " + Util.numberToMoney(player.combine.goldCombine) + " vàng";
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp\ncần " + player.combine.gemCombine + " ngọc");
//                        Item thoivang = null;
//                        {
//                            thoivang = InventoryService.gI().findItemBag(player, 457);
//                        }
//                        if (thoivang == null || thoivang.quantity < 5) {
//                            Service.gI().sendThongBao(player, "Sắp hết thỏi vàng rồi !");
//                        } else if (player.inventory.gold < 500_000_000) {
//                            InventoryService.gI().subQuantityItemsBag(player, thoivang, 1);
//                            player.inventory.gold += 500_000_000;
//                            Service.gI().sendThongBao(player, "Bán thành công thỏi vàng!");
//                            Service.gI().sendThongBao(player, "Bạn vừa nhận được 500tr vàng");
//                        }
                    } else {
                        npcSay += "Còn thiếu " + Util.numberToMoney(player.combine.goldCombine - player.inventory.gold) + " vàng";
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                    }

                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
        }
    }

    public static void phaLeHoa(Player player, int... numm) {
        if (!player.combine.itemsCombine.isEmpty()) {
            int gold = player.combine.goldCombine;
            int gem = player.combine.gemCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            } else if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item item = player.combine.itemsCombine.get(0);
            if (CombineService.gI().isTrangBiPhaLeHoa(item)) {
                int star = 0;
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < CombineService.gI().MAX_STAR_ITEM) {
                    player.inventory.gold -= gold;
                    player.inventory.gem -= gem;
                    byte ratio = (optionStar != null && optionStar.param > 4) ? (byte) 2 : 1;
                    if (Util.isTrue(player.combine.ratioCombine, 100 * ratio)) {
                        if (optionStar == null) {
                            item.itemOptions.add(new Item.ItemOption(107, 1));
                        } else {
                            optionStar.param++;
                        }
                        CombineService.gI().sendEffectSuccessCombine(player);
                        if (optionStar != null && optionStar.param >= 7) {
                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                                    + "thành công " + item.template.name + " lên " + optionStar.param + " sao pha lê");
                        }
                    } else {
                        CombineService.gI().sendEffectFailCombine(player);
                    }
                }
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}
