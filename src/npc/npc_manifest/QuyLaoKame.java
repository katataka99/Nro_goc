package npc.npc_manifest;

import TuanBinh.AddPointEvent;
import clan.Clan;
import consts.ConstNpc;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import models.TreasureUnderSea.TreasureUnderSea;
import models.TreasureUnderSea.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PlayerService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import skill.Skill;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;

public class QuyLaoKame extends Npc {

    public List<Item.ItemOption> options;

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            menu.add("Nói\nchuyện");
           
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
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
            case 0 ->
                handleMainMenu(player, select);
            case 2 ->
                handleConfirmDisbandClan(player, select);
            case 3 -> {
                Clan clan = player.clan;
                if (clan != null) {
                    if (clan.isLeader(player)) {
                        if (select == 0) {
                            Input.gI().createFormGiaiTanBangHoi(player);
                        }
                    }
                }
            }
            case ConstNpc.MENU_OPENED_DBKB ->
                handleOpenedTreasureMapMenu(player, select);
            case ConstNpc.MENU_OPEN_DBKB ->
                handleOpenTreasureMapMenu(player, select);
            case ConstNpc.MENU_ACCEPT_GO_TO_BDKB ->
                handleAcceptGoToTreasureMap(player, select);
            case 12 ->
                handleLearningSkillMenu(player, select);
            case 13 ->
                handleCancelLearningSkill(player, select);
            case 15 -> {
                handleDoiSuKien(player, select);
            }
        }
    }

    private void handleBaseMenu(Player player, int select) {
    if (select == 0) {
        if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
            finishLearningSkill(player);
            Service.gI().sendThongBao(player, "Bạn đã học xong kỹ năng!");
        } else {
            showMainMenu(player);
        }
    }
}

    private void handleMainMenu(Player player, int select) {
        switch (select) {
            case 0 ->
                openTutorial(player);
            case 1 -> {
                if (player.gender == 0) {
                    openSkillLearningMenu(player);
                } else {
                    NpcService.gI().createTutorial(player, tempId, avartar, "Ta chỉ dạy kĩ năng cho người trái đất");
                }
            }
            case 2 ->
                openTreasureMapRelatedMenu(player);
            case 3 -> {
                if (player.clan != null && player.clan.isLeader(player)) {
                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                            "Từ chối");
                }
            }
        }
    }

    private void openTutorial(Player player) {
        NpcService.gI().createTutorial(player, tempId, avartar,
                player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    private void openSkillLearningMenu(Player player) {
        if (player.LearnSkill.Time != -1) {
            long time = player.LearnSkill.Time - System.currentTimeMillis();
            int ngoc = 5 + (int) (time / 3_600_000);

            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
            byte level = Byte.parseByte(subName[subName.length - 1]);

            createOtherMenu(player, 12,
                    "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(
                            SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name
                    + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                    "Học\nCấp tốc\n" + ngoc + " ngọc", "Huỷ", "Bỏ qua");
        } else {
            ShopService.gI().opendShop(player, "QUY_LAO", false);
        }
    }

    private void openTreasureMapRelatedMenu(Player player) {
        Clan clan = player.clan;
        if (clan != null) {
            if (clan.BanDoKhoBau != null) {
                showJoinedTreasureMapMenu(player);
            } else {
                showNewTreasureMapMenu(player);
            }
        } else {
            showNewTreasureMapMenu(player);
        }
    }

    private void showMainMenu(Player player) {
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Nhiệm vụ");
        menu.add("Học\nKỹ năng");
        menu.add("Kho báu\ndưới biển");
        if (player.clan != null && player.clan.isLeader(player)) {
            menu.add("Giải tán\nBang hội");
        }
        String[] menus = menu.toArray(String[]::new);
        createOtherMenu(player, 0, "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
    }

    private void showJoinedTreasureMapMenu(Player player) {
        createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                "Bang hội con đang ở hang kho báu cấp " + player.clan.BanDoKhoBau.level
                + "\ncon có muốn đi cùng họ không?",
                "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
    }

    private void showNewTreasureMapMenu(Player player) {
        createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
    }

    private void handleConfirmDisbandClan(Player player, int select) {
        if (select == 0 && player.clan != null && player.clan.isLeader(player)) {
            Input.gI().createFormGiaiTanBangHoi(player);
        }
    }

    private void handleOpenedTreasureMapMenu(Player player, int select) {
        if (select == 2) {
            if (player.clan == null) {
                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
            } else if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                ChangeMapService.gI().goToDBKB(player);
            } else {
                npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
            }
        }
    }

    private void handleOpenTreasureMapMenu(Player player, int select) {
        if (select == 2) {
            if (player.clan == null) {
                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
            } else if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                Input.gI().createFormChooseLevelBDKB(player);
            } else {
                npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
            }
        }
    }

    private void handleAcceptGoToTreasureMap(Player player, int select) {
        if (select == 0) {
            TreasureUnderSeaService.gI().openBanDoKhoBau(player,
                    Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
        }
    }

    private void handleLearningSkillMenu(Player player, int select) {
        switch (select) {
            case 1 ->
                createOtherMenu(player, 13,
                        "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
            case 0 ->
                completeSkillInstantly(player);
        }
    }

    private void completeSkillInstantly(Player player) {
        long time = player.LearnSkill.Time - System.currentTimeMillis();
        int ngoc = 5 + (int) (time / 3_600_000);

        if (player.inventory.gem < ngoc) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return;
        }

        player.inventory.subGem(ngoc);
        player.LearnSkill.Time = -1;
        finishLearningSkill(player);
    }

    public static void finishLearningSkill(Player player) {
        player.LearnSkill.Time = -1;
        try {
            String[] subName = ItemService.gI()
                    .getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
            byte targetLevel = Byte.parseByte(subName[subName.length - 1]);

            Skill curSkill = SkillUtil.createSkill(
                    SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId),
                    targetLevel);

            player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
            SkillUtil.setSkill(player, curSkill);

            var msg = Service.gI().messageSubCommand((byte) 62);
            msg.writer().writeShort(curSkill.skillId);
            player.sendMessage(msg);
            msg.cleanup();
            PlayerService.gI().sendInfoHpMpMoney(player);
            Service.gI().sendThongBao(player, "Bạn đã học xong "
                    + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(
                            player.LearnSkill.ItemTemplateSkillId)).name + " cấp " + targetLevel + "!");
        } catch (Exception e) {
            Logger.log(e.toString());
        }
    }

    private void handleCancelLearningSkill(Player player, int select) {
        if (select == 0) {
            if (player.LearnSkill.Time != -1) {
                long remainingTime = player.LearnSkill.Time - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    Service.gI().sendThongBao(player, "Bạn đã học xong kỹ năng này!");
                    return;
                }
                long hours = remainingTime / 3_600_000;
                if (remainingTime % 3_600_000 != 0) {
                    hours += 1;
                }
                long refundTiemNang = (hours * 1000);
                player.LearnSkill.Time = -1;
                player.LearnSkill.ItemTemplateSkillId = -1;
                player.nPoint.tiemNang += refundTiemNang;
                PlayerService.gI().sendInfoHpMpMoney(player);
                Service.gI().sendThongBao(player, "Bạn đã huỷ học kỹ năng và nhận lại "
                        + Util.numberToMoney(refundTiemNang) + " tiềm năng.");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đang học kỹ năng nào!");
            }
        }
    }

   private void handleDoiSuKien(Player player, int select) {
    if (select == 0) {
        doiVatPham(player, 1002, 99, 1003, 99, 1005, 1); // cá nóc + cá bảy màu -> xô cá xanh
    } else if (select == 1) {
        doiVatPham(player, 1004, 99, -1, 0, 1006, 2); // cá diêu hồng -> xô cá vàng
    } else if (select == 2) {
        doiVatPham(player, 1166, 99, 1118, 1, 1866, 1); // cá tuyết + xu vàng -> xô cá đỏ
    }
}

    private void doiVatPham(Player player, int idNguyenLieu1, int soLuong1,
                         int idNguyenLieu2, int soLuong2, int idNhanDuoc, int pointHe) {
    Item nl1 = InventoryService.gI().findItemBag(player, idNguyenLieu1);
    Item nl2 = (idNguyenLieu2 != -1) ? InventoryService.gI().findItemBag(player, idNguyenLieu2) : null;

    boolean duNguyenLieu1 = nl1 != null && nl1.quantity >= soLuong1;
    boolean duNguyenLieu2 = (idNguyenLieu2 == -1) || (nl2 != null && nl2.quantity >= soLuong2);

    if (duNguyenLieu1 && duNguyenLieu2) {
        if (idNguyenLieu2 != -1) {
            InventoryService.gI().subQuantityItemsBag(player, nl2, soLuong2);
        }
        InventoryService.gI().subQuantityItemsBag(player, nl1, soLuong1);

        Item itemMoi = ItemService.gI().createNewItem((short) idNhanDuoc);

        // Đảm bảo luôn có option 30 (không thể giao dịch)
        boolean hasOption30 = itemMoi.itemOptions.stream()
                .anyMatch(opt -> opt.optionTemplate.id == 30);
        if (!hasOption30) {
            itemMoi.itemOptions.add(new Item.ItemOption(30, 0));
        }

        InventoryService.gI().addItemBag(player, itemMoi);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + itemMoi.template.name);
        AddPointEvent.addpointhe(player.getSession().userId, pointHe);
    } else {
        Service.gI().sendThongBao(player, "Bạn cần kiếm đủ nguyên liệu");
    }
}

}
