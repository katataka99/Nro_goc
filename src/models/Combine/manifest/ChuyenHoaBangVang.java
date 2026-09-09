/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;

/**
 *
 * @author Administrator
 */
public class ChuyenHoaBangVang {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            Item trangBiGoc = player.combine.itemsCombine.get(0); // trang bị gốc
            Item trangBiCanChuyenHoa = player.combine.itemsCombine.get(1); // trang bị thần

            int levelTrangBi = 0;
            int soLanRotCap = 0;
            int khongthechuyenhoa = 0;

            for (ItemOption io : trangBiGoc.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    levelTrangBi = io.param; 
                } else if (io.optionTemplate.id == 230) {
                    soLanRotCap += io.param;
                } else if (io.optionTemplate.id == 30) {
                    khongthechuyenhoa = 1;
                }
            }

            boolean trangBi_daNangCap_daPhaLeHoa = false;
            for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                if (io.optionTemplate.id == 72 || io.optionTemplate.id == 102) {
                    trangBi_daNangCap_daPhaLeHoa = true;
                    break;
                }
            }

            if (khongthechuyenhoa == 1) {
                Service.gI().sendThongBaoOK(player, "Không thể chuyển hóa đồ không giao dịch");
                return;
            } else if (!CombineService.gI().isTrangBiGoc(trangBiGoc)) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ bậc lưỡng long, Jean hoặc Zelot");
                return;
            } else if (levelTrangBi < 4) { 
                Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ [+4] trở lên");
                return;
            } else if (!CombineService.gI().isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải là đồ thần linh");
                return;
            } else if (trangBi_daNangCap_daPhaLeHoa) {
                Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải chưa nâng cấp và pha lê hóa trang bị");
                return;
            } else if (!CombineService.gI().isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc và Trang bị chuyển hóa phải cùng loại và hành tinh");
                return;
            } else {
                String NpcSay = "|2|Hiện tại " + trangBiCanChuyenHoa.getName() + "\n";
                for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                    if (io.optionTemplate.id != 72) {
                        NpcSay += "|0|" + io.getOptionString() + "\n";
                    }
                }
                Item trangBiKetQuaTam = ItemService.gI().createNewItem(trangBiCanChuyenHoa.template.id);
                for (ItemOption opt : trangBiCanChuyenHoa.itemOptions) {
                    trangBiKetQuaTam.itemOptions.add(new ItemOption(opt.optionTemplate.id, opt.param));
                }
                
                for (ItemOption optGoc : trangBiGoc.itemOptions) {
                    boolean found = false;
                    for (ItemOption optKQ : trangBiKetQuaTam.itemOptions) {
                        if (optKQ.optionTemplate.id == optGoc.optionTemplate.id) {
                            optKQ.param += optGoc.param;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        trangBiKetQuaTam.itemOptions.add(new ItemOption(optGoc.optionTemplate.id, optGoc.param));
                    }
                }
                int levelThan = 0;
                for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        levelThan = io.param;
                        break;
                    }
                }
                int capSauKhiChuyen = levelThan + levelTrangBi;

                NpcSay += "|2|Sau khi chuyển hóa (+" + capSauKhiChuyen + ")\n";
                for (ItemOption io : trangBiKetQuaTam.itemOptions) {
                    if (io.optionTemplate.id != 72) {
                        NpcSay += "|1|" + io.getOptionString() + "\n";
                    }
                }

                NpcSay += "Chuyển qua tất cả sao pha lê\n";
                NpcSay += "|2|Cần 2 tỷ vàng";

                CombineService.gI().baHatMit.createOtherMenu(
                        player,
                        ConstNpc.MENU_START_COMBINE,
                        NpcSay,
                        "Nâng cấp\n2 tỷ\nvàng",
                        "Từ chối"
                );
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị có cấp từ [+4] và 1 trang bị không có cấp nhưng cao hơn 1 bậc");
        }
    }

    public static void ChuyenHoaBangvang(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                Item trangBiGoc = player.combine.itemsCombine.get(0);
                Item trangBiCanChuyenHoa = player.combine.itemsCombine.get(1);

                Item trangBiKetQua = ItemService.gI().createNewItem(trangBiCanChuyenHoa.template.id);

                long vangChuyenHoa = 2_000_000_000L;
                int levelTrangBi = 0;
                int khongchuyenhoa = 0;

                for (ItemOption io : trangBiGoc.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        levelTrangBi = io.param;
                    } else if (io.optionTemplate.id == 30) {
                        khongchuyenhoa = 1;
                    }
                }

                if (player.inventory.gold >= vangChuyenHoa) {
                    if (khongchuyenhoa == 1) {
                        Service.gI().sendThongBaoOK(player, "Không thể chuyển hóa trang bị không thể giao dịch");
                        return;
                    }
                    if (!CombineService.gI().isTrangBiGoc(trangBiGoc)) {
                        Service.gI().sendThongBaoOK(player, "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                        return;
                    }
                    if (levelTrangBi < 3) {
                        Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ [+4] trở lên");
                        return;
                    }
                    if (!CombineService.gI().isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                        Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải là đồ thần linh trở lên");
                        return;
                    }
                    if (!CombineService.gI().isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                        Service.gI().sendThongBaoOK(player, "Trang bị gốc và Trang bị chuyển hóa phải cùng loại và hành tinh");
                        return;
                    }
                    for (ItemOption opt : trangBiCanChuyenHoa.itemOptions) {
                        trangBiKetQua.itemOptions.add(new ItemOption(opt.optionTemplate.id, opt.param));
                    }
                    for (ItemOption optGoc : trangBiGoc.itemOptions) {
                        boolean found = false;
                        for (ItemOption optKQ : trangBiKetQua.itemOptions) {
                            if (optKQ.optionTemplate.id == optGoc.optionTemplate.id) {
                                optKQ.param += optGoc.param;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            trangBiKetQua.itemOptions.add(new ItemOption(optGoc.optionTemplate.id, optGoc.param));
                        }
                    }

                    // Trừ vàng + cập nhật
                    player.inventory.gold -= vangChuyenHoa;
                    Service.gI().sendMoney(player);
                    InventoryService.gI().addItemBag(player, trangBiKetQua);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiCanChuyenHoa, 1);
                    InventoryService.gI().sendItemBag(player);
                    CombineService.gI().reOpenItemCombine(player);
                    CombineService.gI().sendEffectSuccessCombine(player);
                } else {
                    Service.gI().sendThongBao(player, "Không đủ vàng !!");
                }
            } else {
                Service.gI().sendThongBao(player, "Cần 1 ô trống hành trang");
            }
        }
    }
}
