package npc.npc_manifest;

/**
 * @author HM
 */
import minigame.DecisionMaker.DecisionMaker;
import minigame.DecisionMaker.DecisionMakerGem;
import minigame.DecisionMaker.DecisionMakerGold;
import minigame.DecisionMaker.DecisionMakerRuby;
import minigame.LuckyNumber.LuckyNumber;
import minigame.LuckyNumber.LuckyNumberService;
import minigame.RockPaperScissors.RockPaperScissors;
import npc.Npc;
import player.Player;
import services.TaskService;
import services.func.Input;

public class LyTieuNuong extends Npc {

    public class ConstMiniGame {

        public static final byte MENU_CHINH = 0;
        public static final byte MENU_KEO_BUA_BAO = 1;
        public static final byte MENU_CON_SO_MAY_MAN_VANG = 2;
        public static final byte MENU_CON_SO_MAY_MAN_NGOC = 3;
        public static final byte MENU_CHON_AI_DAY = 4;

        public static final byte MENU_PLAY_KEO_BUA_BAO = 5;

        public static final byte MENU_LUCKY_NUMBER = 6;
        public static final byte MENU_PLAY_LUCKY_NUMBER_GOLD = 7;
        public static final byte MENU_PLAY_LUCKY_NUMBER_GEM = 8;

        public static final byte MENU_PLAY_DECISION_MAKER_GOLD = 9;
        public static final byte MENU_PLAY_DECISION_MAKER_RUBY = 10;
        public static final byte MENU_PLAY_DECISION_MAKER_GEM = 11;
        public static final byte MENU_WAIT_NEW_GAME = 12;
    }

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
//        services.Service.gI().sendThongBaoOK(player, "Chức năng tạm đóng");
        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            if (player.nPoint.power < 2_000_000_000) {
                services.Service.gI().sendThongBaoOK(player, "Cần 2 tỷ sức mạnh");
                return;
            }
//            createOtherMenu(player, ConstMiniGame.MENU_CHINH, "Mini game.", "Kéo\nBúa\nBao", "Đóng");
            LuckyNumber.showMenu(this, player, true);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.iDMark.getIndexMenu()) {
                case ConstMiniGame.MENU_CHINH:
                    switch (select) {
                        case 0: // Kéo búa bao
                            createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO, "Hãy chọn mức cược.", "1 Tr vàng", "5 Tr vàng", "10 Tr vàng");
                            break;
                        default:
                            break;
                    }
                    break;
                case ConstMiniGame.MENU_KEO_BUA_BAO:
                    RockPaperScissors.confirmMenu(this, player, select);
                    break;
                case ConstMiniGame.MENU_PLAY_KEO_BUA_BAO:
                    if (player.iDMark.getTimePlayKeoBuaBao() - System.currentTimeMillis() > 0) {
                        RockPaperScissors.confirmPlay(this, player, select);
                    } else {
                        createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO, "Hãy chọn mức cược.", "1 Tr vàng", "5 Tr vàng", "10 Tr vàng");
                    }
                    break;
                case ConstMiniGame.MENU_LUCKY_NUMBER:
                    switch (select) {
                        case 0:
                            LuckyNumber.showMenu(this, player, true);
                            break;
                        default:
                            break;
                    }
                    break;
                case ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GEM:
                    player.iDMark.setGemCSMM(true);
                    switch (select) {
                        case 0:
                            LuckyNumber.showMenu(this, player, true);
                            break;
                        case 1:
                            Input.gI().createFormSelectOneNumberLuckyNumber(player, true);
                            break;
                        case 2:
                            LuckyNumberService.addNumber(player);
                            LuckyNumber.showMenu(this, player, true);
                            break;
                        case 3:
                            for (int i = 0; i < 10; i++) {
                                LuckyNumberService.addNumber(player);
                            }
                            LuckyNumber.showMenu(this, player, true);
                            break;
                        case 4:
                            LuckyNumber.showMenuTutorials(this, player);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
