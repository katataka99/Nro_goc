package models.Combine;

import consts.ConstNpc;
import item.Item;

import java.io.IOException;

import models.Combine.manifest.CheTaoTrangBiThienSu;
import models.Combine.manifest.ChuyenHoaBangNgoc;
import models.Combine.manifest.ChuyenHoaBangVang;
import models.Combine.manifest.CuongHoaLoSaoPhaLe;
import models.Combine.manifest.DanhBongSaoPhaLe;
import models.Combine.manifest.EpSaoTrangBi;
import models.Combine.manifest.GiamDinhSach;
import models.Combine.manifest.HoiPhucSach;
import models.Combine.manifest.NangCapBongTai;
import models.Combine.manifest.NangCapKichHoat;
import models.Combine.manifest.NangCapKichHoatVip;
import models.Combine.manifest.NangCapSachTuyetKy;
import models.Combine.manifest.NangCapSaoPhaLe;
import models.Combine.manifest.NangCapVatPham;
import models.Combine.manifest.NangChiSoBongTai;
import models.Combine.manifest.NhapNgocRong;
import models.Combine.manifest.PhaLeHoaTrangBi;
import models.Combine.manifest.PhanRaSach;
import models.Combine.manifest.TaoDaHematite;
import models.Combine.manifest.TaySach;
import player.Player;
import network.Message;
import npc.Npc;
import npc.NpcManager;
import services.InventoryService;

public class CombineService {

    private static final int COST = 500000000;
    private static final int TIME_COMBINE = 1500;
    public static final byte MAX_STAR_ITEM = 7;
    public static final byte MAX_LEVEL_ITEM = 8;
    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte combineSUCCESS = 2;
    private static final byte combineFAIL = 3;
    private static final byte combineCHANGE_OPTION = 4;
    private static final byte combineDRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;
    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_BANG_VANG = 502;
    public static final int CHUYEN_HOA_BANG_NGOC = 503;
    public static final int NHAP_DA = 504;
    public static final int NANG_CAP_SAO_PHA_LE = 100;
    public static final int DANH_BONG_SAO_PHA_LE = 101;
    public static final int CUONG_HOA_LO_SAO_PHA_LE = 102;
    public static final int TAO_DA_HEMATITE = 103;
    public static final int GIAM_DINH_SACH = 104;
    public static final int TAY_SACH = 105;
    public static final int NANG_CAP_SACH_TUYET_KY = 106;
    public static final int HOI_PHUC_SACH = 107;
    public static final int PHAN_RA_SACH = 108;
    public static final int CHE_TAO_TRANG_BI_THIEN_SU = 109;
    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int NANG_CHI_SO_BONG_TAI = 517;
    public static final int NANG_CAP_KICH_HOAT = 518;
    public static final int NANG_CAP_KICH_HOAT_VIP = 519;

    private static CombineService instance;

    public final Npc baHatMit;
    public final Npc whis;

    private CombineService() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
    }

    public static CombineService gI() {
        if (instance == null) {
            instance = new CombineService();
        }
        return instance;
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     * @param index
     */
    private boolean isDoLuongLong(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 || item.template.id == 253 || item.template.id == 265 || item.template.id == 277 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoZelot(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 4 || item.template.id == 253 - 4 || item.template.id == 265 - 4 || item.template.id == 277 - 4 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoJean(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 8 || item.template.id == 253 - 8 || item.template.id == 265 - 8 || item.template.id == 277 - 8 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTrangBiGoc(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoLuongLong(item) || isDoJean(item) || isDoZelot(item)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanXD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 || item.template.id == 560 || item.template.id == 566 || item.template.id == 567 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).param;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 5;
            case 19:
                return 5;
            case 18:
                return 5;
            case 17:
                return 5;
            case 16:
                return 3;
            case 15:
                return 2;
            case 14:
                return 5;
            case 441:
                return 5;
            case 442:
                return 5;
            case 443:
                return 5;
            case 444:
                return 5;
            case 445:
                return 5;
            case 446:
                return 5;
            case 447:
                return 5;
            case 964:
                return 10;
            case 965:
                return 10;
            default:
                return -1;
        }
    }

    public int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).optionTemplate.id;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 77; // hp
            case 19:
                return 103; // ki
            case 18:
                return 80; // hp 30s
            case 17:
                return 81; // mp 30s
            case 16:
                return 50; // sức đánh
            case 15:
                return 94; // giáp %
            case 14:
                return 108; // né đòn
            case 441:
                return 95; // hút hp
            case 442:
                return 96; // hút ki
            case 443:
                return 97; // phả sát thương
            case 444:
                return 98; // xuyên giáp chưởng
            case 445:
                return 99; // xuyên giáp đấm
            case 446:
                return 100; // vàng rơi từ quái
            case 447:
                return 19; // tấn công % khi đánh quái
            case 964:
                return 14; // chí mạng
            case 965:
                return 50; // sức đánh
            default:
                return -1;
        }
    }

    public boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.type < 5 || item.template.type == 32 || item.template.type == 5) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanTD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 4 || item.template.id == 560 - 4 || item.template.id == 566 - 4 || item.template.id == 567 - 4 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanNM(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 2 || item.template.id == 560 - 2 || item.template.id == 566 - 2 || item.template.id == 567 - 2 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTrangBiChuyenHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoThanXD(item) || isDoThanTD(item) || isDoThanNM(item)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isCheckTrungTypevsGender(Item item, Item item2) {
        if (item != null && item.isNotNullItem() && item2 != null && item2.isNotNullItem()) {
            if (item.template.type == item2.template.type && item.template.gender == item2.template.gender) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void showInfoCombine(Player player, int[] index) {
        if (player.combine == null) {
            return;
        }
        player.combine.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combine.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combine.typeCombine) {
            case CHUYEN_HOA_BANG_NGOC ->
                ChuyenHoaBangNgoc.showInfoCombine(player);
            case CHUYEN_HOA_BANG_VANG ->
                ChuyenHoaBangVang.showInfoCombine(player);
            case EP_SAO_TRANG_BI ->
                EpSaoTrangBi.showInfoCombine(player);
            case PHA_LE_HOA_TRANG_BI ->
                PhaLeHoaTrangBi.showInfoCombine(player);
            case NHAP_NGOC_RONG ->
                NhapNgocRong.showInfoCombine(player);
            case NANG_CAP_VAT_PHAM ->
                NangCapVatPham.showInfoCombine(player);
            case NANG_CAP_BONG_TAI ->
                NangCapBongTai.showInfoCombine(player);
            case NANG_CHI_SO_BONG_TAI ->
                NangChiSoBongTai.showInfoCombine(player);
            case NANG_CAP_SAO_PHA_LE ->
                NangCapSaoPhaLe.showInfoCombine(player);
            case DANH_BONG_SAO_PHA_LE ->
                DanhBongSaoPhaLe.showInfoCombine(player);
            case CUONG_HOA_LO_SAO_PHA_LE ->
                CuongHoaLoSaoPhaLe.showInfoCombine(player);
            case TAO_DA_HEMATITE ->
                TaoDaHematite.showInfoCombine(player);
            case GIAM_DINH_SACH ->
                GiamDinhSach.showInfoCombine(player);
            case TAY_SACH ->
                TaySach.showInfoCombine(player);
            case NANG_CAP_SACH_TUYET_KY ->
                NangCapSachTuyetKy.showInfoCombine(player);
            case HOI_PHUC_SACH ->
                HoiPhucSach.showInfoCombine(player);
            case PHAN_RA_SACH ->
                PhanRaSach.showInfoCombine(player);
            case CHE_TAO_TRANG_BI_THIEN_SU ->
                CheTaoTrangBiThienSu.showInfoCombine(player);
            case NANG_CAP_KICH_HOAT ->
                NangCapKichHoat.showInfoCombine(player);
            case NANG_CAP_KICH_HOAT_VIP ->
                NangCapKichHoatVip.showInfoCombine(player);
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     * @param n
     */
    public void startCombine(Player player, int... n) {
        int num = 0;
        if (n.length > 0) {
            num = n[0];
        }
        switch (player.combine.typeCombine) {
            case CHUYEN_HOA_BANG_NGOC ->
                ChuyenHoaBangNgoc.ChuyenHoaBangngoc(player);
            case CHUYEN_HOA_BANG_VANG ->
                ChuyenHoaBangVang.ChuyenHoaBangvang(player);
            case EP_SAO_TRANG_BI ->
                EpSaoTrangBi.epSaoTrangBi(player);
            case PHA_LE_HOA_TRANG_BI ->
                PhaLeHoaTrangBi.phaLeHoa(player, num);
            case NHAP_NGOC_RONG ->
                NhapNgocRong.nhapNgocRong(player, num == 1);
            case NANG_CAP_VAT_PHAM ->
                NangCapVatPham.nangCapVatPham(player, num == 1);
            case NANG_CAP_BONG_TAI ->
                NangCapBongTai.nangCapBongTai(player);
            case NANG_CHI_SO_BONG_TAI ->
                NangChiSoBongTai.nangChiSoBongTai(player);
            case NANG_CAP_SAO_PHA_LE ->
                NangCapSaoPhaLe.nangCapSaoPhaLe(player);
            case DANH_BONG_SAO_PHA_LE ->
                DanhBongSaoPhaLe.danhBongSaoPhaLe(player);
            case CUONG_HOA_LO_SAO_PHA_LE ->
                CuongHoaLoSaoPhaLe.cuongHoaLoSaoPhaLe(player);
            case TAO_DA_HEMATITE ->
                TaoDaHematite.taoDaHematite(player);
            case GIAM_DINH_SACH ->
                GiamDinhSach.giamDinhSach(player);
            case TAY_SACH ->
                TaySach.taySach(player);
            case NANG_CAP_SACH_TUYET_KY ->
                NangCapSachTuyetKy.nangCapSachTuyetKy(player);
            case HOI_PHUC_SACH ->
                HoiPhucSach.hoiPhucSach(player);
            case PHAN_RA_SACH ->
                PhanRaSach.phanRaSach(player);
            case CHE_TAO_TRANG_BI_THIEN_SU ->
                CheTaoTrangBiThienSu.cheTaoTrangBiThienSu(player);
            case NANG_CAP_KICH_HOAT ->
                NangCapKichHoat.startCombine(player);
            case NANG_CAP_KICH_HOAT_VIP ->
                NangCapKichHoatVip.startCombine(player);
        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combine.clearParamCombine();
        player.combine.lastTimeCombine = System.currentTimeMillis();

    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        player.combine.setTypeCombine(type);
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.iDMark.getNpcChose() != null) {
                msg.writer().writeShort(player.iDMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendEffectCombineItem(Player player, byte type, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(type);
            switch (type) {
                case 0:
                    msg.writer().writeUTF("");
                    msg.writer().writeUTF("");
                    break;
                case 1:
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(-1);
                    break;
                case 2: // success 0 eff 0
                case 3: // success 1 eff 0
                    break;
                case 4: // success 0 eff 1
                    msg.writer().writeShort(icon1);
                    break;
                case 5: // success 0 eff 2
                    msg.writer().writeShort(icon1);
                    break;
                case 6: // success 0 eff 3
                    msg.writer().writeShort(icon1);
                    msg.writer().writeShort(icon2);
                    break;
                case 7: // success 0 eff 4
                    msg.writer().writeShort(icon1);
                    break;
                case 8: // success 1 eff 4
                    break;
            }
            msg.writer().writeShort(-1); // id npc
//            msg.writer().writeShort(-1); // x
//            msg.writer().writeShort(-1); // y
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    public void sendEffectSuccessCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineSUCCESS);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    public void sendEffectFailCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineFAIL);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    public void reOpenItemCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combine.itemsCombine.size());
            for (Item it : player.combine.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    public void sendEffectCombineDB(Player player, short icon) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineDRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendAddItemCombine(Player player, int npcId, Item... items) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("By NguyenDucVuEntertainment");
            msg.writer().writeUTF("DucVuPro - Đẳng Cấp Là Mãi Mãi");
            msg.writer().writeShort(npcId);
            player.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-81);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.length);
            for (Item item : items) {
                msg.writer().writeByte(InventoryService.gI().getIndexItemBag(player, item));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffSuccessVip(Player player, int iconID) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(7);
            msg.writer().writeShort(iconID);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffFailVip(Player player) {
        try {
            Message msg;
            msg = new Message(-81);
            msg.writer().writeByte(8);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private String getTextTopTabCombine(int type) {
        return switch (type) {
            case CHUYEN_HOA_BANG_NGOC, CHUYEN_HOA_BANG_VANG ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\n chuyển hóa thành trang bị khác";
            case EP_SAO_TRANG_BI ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở nên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case NHAP_NGOC_RONG ->
                "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NHAP_DA ->
                "Ta sẽ phù phép\ncho 10 mảnh đá vụn\ntrở thành 1 đá nâng cấp";
            case NANG_CAP_VAT_PHAM ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở nên mạnh mẽ";
            case NANG_CAP_BONG_TAI ->
                "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case NANG_CHI_SO_BONG_TAI ->
                "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case NANG_CAP_SAO_PHA_LE ->
                "Ta sẽ phù phép\nnâng cấp Sao Pha Lê\nthành cấp 2";
            case DANH_BONG_SAO_PHA_LE ->
                "Đánh bóng\nSao pha lê cấp 2";
            case CUONG_HOA_LO_SAO_PHA_LE ->
                "Cường hóa\nÔ Sao Pha Lê";
            case TAO_DA_HEMATITE ->
                "Ta sẽ phù phép\ntạo đá hematite";
            case GIAM_DINH_SACH ->
                "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case TAY_SACH ->
                "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY ->
                "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case HOI_PHUC_SACH ->
                "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH ->
                "Ta sẽ phù phép\nphân rã sách đó cho ngươi";
            case CHE_TAO_TRANG_BI_THIEN_SU ->
                "Chế tạo\ntrang bị thiên sứ";
            case LAM_PHEP_NHAP_DA ->
                "Ta sẽ phù phép\n"
                + "cho 10 mảnh đá vụn\n"
                + "trở thành 1 đá nâng cấp";
            case NANG_CAP_KICH_HOAT ->
                "Ta sẽ phù phép\nchế tạo trang bị Huỷ Diệt\nthành trang bị Kích Hoạt";
            case NANG_CAP_KICH_HOAT_VIP ->
                "Thiên sứ nhờ ta nâng cấp \n  trang bị của người thành\n SKH VIP!";
            default ->
                "";
        };
    }

    private String getTextInfoTabCombine(int type) {
        return switch (type) {
            case EP_SAO_TRANG_BI ->
                "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\nSau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI ->
                "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case NHAP_NGOC_RONG ->
                "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NHAP_DA ->
                "Vào hành trang\nChọn 10 mảnh đá vụn\nChọn 1 bình nước phép\n(mua tại Uron ở trạm tàu vũ trụ)\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM ->
                "Vào hành trang\nChọn trang bị\n(Áo,quần,găng,giày hoặc rađa)\nChọn loại đá để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_BONG_TAI ->
                "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, số lượng 9999 cái\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI ->
                "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn porata số lượng 99\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";
            case NANG_CAP_SAO_PHA_LE ->
                "Vào hành trang\nChọn đá Hematite\nChọn loại sao pha lê (cấp 1)\nSau đó chọn 'Nâng cấp'";
            case DANH_BONG_SAO_PHA_LE ->
                "Vào hành trang\nChọn loại sao pha lê cấp 2 có từ 2 viên trở lên\nChọn 1 đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO_PHA_LE ->
                "Vào hành trang\nChọn trang bị có Ô sao thứ 8 trở lên chưa cường hóa\nChọn đá Hematite\nChọn dùi đục\nSau đó chọn 'Cường hóa'";
            case TAO_DA_HEMATITE ->
                "Vào hành trang\nChọn 5 sao pha lê cấp 2 cùng màu\nChọn 'Tạo đá Hematite'";
            case GIAM_DINH_SACH ->
                "Vào hành trang chọn\n1 sách cần giám định";
            case TAY_SACH ->
                "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY ->
                "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case HOI_PHUC_SACH ->
                "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi";
            case PHAN_RA_SACH ->
                "Vào hành trang chọn\n1 sách cần phân rã";
            case CHE_TAO_TRANG_BI_THIEN_SU ->
                "Cần 1 công thức\nMảnh trang bị tương ứng\n1 đá nâng cấp (tùy chọn)\n1 đá may mắn (tùy chọn)";
            case LAM_PHEP_NHAP_DA ->
                "Vào hành trang\n"
                + "Chọn 10 mảnh đá vụn\n"
                + "Chọn 1 bình nước phép\n"
                + "(mua tại Uron ở trạm tàu vũ trụ)\n"
                + "Sau đó chọn 'Làm phép'";
            case NANG_CAP_KICH_HOAT ->
                "Vào hành trang\nChọn 1 trang bị Huỷ Diệt\nChọn 1 viên đá Kích Hoạt\nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_KICH_HOAT_VIP ->
                "vào hành trang\nChọn 1 trang bị Hủy Diệt\nChọn tiếp ngẫu nhiên 2 món Thần Linh \n "
                + " đồ SKH VIP sẽ cùng loại \n với đồ Hủy Diệt!"
                + "Chỉ cần chọn 'Nâng Cấp'";
            case CHUYEN_HOA_BANG_NGOC, CHUYEN_HOA_BANG_VANG ->
                "Vào hành trang\nChọn trang bị gốc ô 1\n(Áo,quần,găng,giày hoặc rada)\ntừ cấp[+4] trở lên\nChọn tiếp trang bị cần chuyển hóa ô 2\nvà chưa nâng cấp\nsau đó chọn 'Nâng cấp'";
            default ->
                "";
        };
    }

}
