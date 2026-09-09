package npc.npc_manifest;

/**
 * @author HM
 */
import consts.ConstDailyGift;
import consts.ConstMenu;
import consts.ConstNpc;
import item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.Combine.Combine;

import models.Combine.CombineService;
import models.Combine.manifest.CheTaoCuonSachCu;
import models.Combine.manifest.DoiSachTuyetKy;
import models.Combine.manifest.NangCapVatPham;
import models.DeathOrAliveArena.DeathOrAliveArena;
import models.DeathOrAliveArena.DeathOrAliveArenaManager;
import models.DeathOrAliveArena.DeathOrAliveArenaService;
import npc.Npc;
import player.Player;
import player.dailyGift.DailyGiftService;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.func.ChangeMapService;
import shop.ShopService;
import utils.Util;

public class BaHatMit extends Npc {

    public BaHatMit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", "Chức năng\nPha lê", "Chuyển hoá\ntrang bị");
                case 112 -> {
                    if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
                        player.haveRewardVDST = false;
                        player.thoiVangVoDaiSinhTu = 0;
                    }
                    if (player.haveRewardVDST) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đây là phần thưởng cho con.", "1 ngọc bí\nbất kì", "1 bí ngô");
                        return;
                    }
                    if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().equals(player)) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn hủy đăng ký thi đấu võ đài?", "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                            return;
                        }
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó", "Top 100", "Bình chọn", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                        return;
                    }
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó", "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                }
                case 174 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", "Quay về", "Từ chối");
                case 181 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", "Quay về", "Từ chối");
                default -> {
                    List<String> menu = new ArrayList<>(Arrays.asList("Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", "Làm phép\nNhập đá", "Nhập\nNgọc Rồng"));
                    String[] menus = menu.toArray(new String[0]);
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", menus);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 -> {
                    switch (player.iDMark.getIndexMenu()) {
                        case ConstNpc.BASE_MENU -> {
                            switch (select) {
                                case 0 ->
                                    createOtherMenu(player, ConstMenu.MENU_PHA_LE, "Ta có thể giúp gì cho ngươi ?", "Ép sao\ntrang bị", "Pha lê\nhoá\ntrang bị");
                                case 1 -> //Chuyển hoá trang bị
                                    createOtherMenu(player, ConstMenu.MENU_CHUYEN_HOA_TRANG_BI, "Ta có thể giúp gì cho ngươi ?", "Chuyển hoá\nbằng vàng", "Chuyển hoá\nbằng ngọc");

                            }
                        }
                        case ConstMenu.MENU_PHA_LE -> {
                            switch (select) {
                                case 0: //Ép sao trang bị
                                    CombineService.gI().openTabCombine(player, CombineService.EP_SAO_TRANG_BI);
                                    break;
                                case 1: //Pha lê hoá trang bị
                                    CombineService.gI().openTabCombine(player, CombineService.PHA_LE_HOA_TRANG_BI);
                                    break;
                            }
                        }
                        case ConstMenu.MENU_CHUYEN_HOA_TRANG_BI -> {
                            switch (select) {
                                case 0 -> {
                                    CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_BANG_VANG);
                                }
                                case 1 -> {
                                    CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_BANG_NGOC);
                                }
                            }
                        }
                        case ConstNpc.MENU_START_COMBINE -> {
                            switch (player.combine.typeCombine) {
                                case CombineService.PHA_LE_HOA_TRANG_BI, CombineService.CHUYEN_HOA_BANG_VANG, CombineService.CHUYEN_HOA_BANG_NGOC, CombineService.NANG_CAP_KICH_HOAT, CombineService.EP_SAO_TRANG_BI -> {
                                    switch (select) {
                                        case 0:
                                            CombineService.gI().startCombine(player);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
                case 112 -> {
                    if (player.iDMark.isBaseMenu()) {
                        if (player.haveRewardVDST) {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item item = ItemService.gI().createNewItem((short) (Util.nextInt(705, 708)));
                                        item.itemOptions.add(new Item.ItemOption(93, 30));
                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBag(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
                                        player.haveRewardVDST = false;
                                    } else {
                                        Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                    }
                                }
                                case 1 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item item = ItemService.gI().createNewItem((short) 585);
                                        item.itemOptions.add(new Item.ItemOption(93, 30));
                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBag(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
                                        player.haveRewardVDST = false;
                                    } else {
                                        Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                    }
                                }
                            }
                            return;
                        }
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                            if (DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().equals(player)) {
                                switch (select) {
                                    case 0 -> {
                                    }
                                    case 1 ->
                                        this.npcChat("Không thể thực hiện");
                                    case 2 -> {
                                    }
                                    case 3 ->
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                }
                                return;
                            }
                            switch (select) {
                                case 0 -> {
                                }
                                case 1 ->
                                    this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT, "Phí bình chọn là 1 triệu vàng\nkhi trận đấu kết thúc\n90% tổng tiền bình chọn sẽ chia đều cho phe bình chọn chính xác", "Bình chọn cho " + DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().name + " (" + DeathOrAliveArenaManager.gI().getVDST(player.zone).getCuocPlayer() + ")", "Bình chọn cho hạt mít (" + DeathOrAliveArenaManager.gI().getVDST(player.zone).getCuocBaHatMit() + ")");
                                case 2 ->
                                    DeathOrAliveArenaService.gI().startChallenge(player);
                                case 3 -> {
                                }
                                case 4 ->
                                    ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                            }
                            return;
                        }
                        switch (select) {
                            case 0 -> {
                            }
                            case 1 ->
                                DeathOrAliveArenaService.gI().startChallenge(player);
                            case 2 -> {
                            }
                            case 3 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    } else if (player.iDMark.getIndexMenu() == ConstNpc.DAT_CUOC_HAT_MIT) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                            switch (select) {
                                case 0 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocPlayer(vdst.getCuocPlayer() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonPlayer++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                                case 1 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocBaHatMit(vdst.getCuocBaHatMit() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonHatMit++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                            }
                        }
                    }
                }
                case 174, 181 -> {
                    if (player.iDMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 42, 43, 44, 84 -> {
                    if (player.iDMark.isBaseMenu()) {
                        switch (select) {
//                            case 0:
//                                createOtherMenu(player, ConstNpc.MENU_SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?",
//                                        "Đóng thành\nSách cũ",
//                                        "Đổi Sách\nTuyệt kỹ",
//                                        "Giám định\nSách",
//                                        "Tẩy\nSách",
//                                        "Nâng cấp\nSách\nTuyệt kỹ",
//                                        "Hồi phục\nSách",
//                                        "Phân rã\nSách");
//                                break;
                            case 0:
                                createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA, "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để " + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                        "Bùa\n1 giờ",
                                        "Bùa\n8 giờ",
                                        "Bùa\n1 tháng", "Đóng");
                                break;
                            case 1:
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_VAT_PHAM);
                                break;
                            case 2:
                                CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                                break;
                            case 3:
                                CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
                                break;
                        }
                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_SHOP_BUA) {
                        switch (select) {
                            case 0 ->
                                ShopService.gI().opendShop(player, "BUA_1H", true);
                            case 1 ->
                                ShopService.gI().opendShop(player, "BUA_8H", true);
                            case 2 ->
                                ShopService.gI().opendShop(player, "BUA_1M", true);
                        }
                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combine.typeCombine) {
                            case CombineService.NANG_CAP_BONG_TAI, CombineService.NANG_CHI_SO_BONG_TAI, CombineService.LAM_PHEP_NHAP_DA, CombineService.NHAP_NGOC_RONG, CombineService.GIAM_DINH_SACH, CombineService.TAY_SACH, CombineService.NANG_CAP_SACH_TUYET_KY, CombineService.HOI_PHUC_SACH, CombineService.PHAN_RA_SACH -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                }
                            }
                            case CombineService.NANG_CAP_VAT_PHAM -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                } else if (select == 1) {
                                    NangCapVatPham.nangCapVatPham(player, true);
                                }
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
    }
}
