package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstPlayer;
import java.util.ArrayList;
import npc.Npc;
import player.Player;
import services.*;
import services.func.Input;
import shop.ShopService;
import skill.Skill;
import utils.*;

public class TruongLaoGuru extends Npc {

    public TruongLaoGuru(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            if (player.gender != ConstPlayer.NAMEC) {
                NpcService.gI().createTutorial(player, tempId, avartar, "Con hãy về hành tinh của mình mà thể hiện");
                return;
            }

            ArrayList<String> menu = new ArrayList<>();
            if (player.canReward) {
                menu.add("Giao\nLân con");
            } else {
                menu.add("Nhiệm vụ");
                menu.add("Học\nKỹ năng");
                if (player.clan != null && player.clan.isLeader(player)) {
                    menu.add("Giải tán\nBang hội");
                }
            }

            String[] menus = menu.toArray(String[]::new);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.canReward) {
            RewardService.gI().rewardLancon(player);
            return;
        }

        switch (player.iDMark.getIndexMenu()) {
            case ConstNpc.BASE_MENU ->
                handleBaseMenu(player, select);
            case 3 ->
                handleDisbandClanConfirm(player, select);
            case 12 ->
                handleLearningSkillMenu(player, select);
                case 13 ->
                handleCancelLearningSkill(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        switch (select) {
            case 0 ->
                NpcService.gI().createTutorial(player, tempId, avartar,
                        player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);

            case 1 -> {
                if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                    finishLearningSkill(player);
                    Service.gI().sendThongBao(player, "Bạn đã học xong kỹ năng!");
                } else {

                    if (player.gender == 1) {
                        openSkillLearningMenu(player);
                    } else {
                        NpcService.gI().createTutorial(player, tempId, avartar, "Ta chỉ dạy kĩ năng cho người Namec");
                    }
                }
            }

            case 2 -> {
                if (player.clan != null && player.clan.isLeader(player)) {
                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                }
            }
        }
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

    private void handleDisbandClanConfirm(Player player, int select) {
        if (select == 0 && player.clan != null && player.clan.isLeader(player)) {
            Input.gI().createFormGiaiTanBangHoi(player);
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

    private void finishLearningSkill(Player player) {
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
                            player.LearnSkill.ItemTemplateSkillId)).name
                    + " cấp " + targetLevel + "!");
        } catch (Exception e) {
            Logger.log(e.toString());
        }
    }
}
