
/*
 * Copyright by HAIRMOD
 */
package minigame.RockPaperScissors;

import consts.ConstFont;
import npc.Npc;
import npc.npc_manifest.LyTieuNuong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import player.Player;
import services.ItemTimeService;
import services.Service;
import utils.Util;

public class RockPaperScissors {

    public static final byte KEO = 0;
    public static final byte BUA = 1;
    public static final byte BAO = 2;
    private static final Logger log = LoggerFactory.getLogger(RockPaperScissors.class);

    public static long timePlay = 15; // có 15 giây để chơi

    public static int COST_0 = 1_000_000;
    public static int COST_1 = 5_000_000;
    public static int COST_2 = 10_000_000;

    public static void confirmMenu(Npc npc, Player player, int select) {
        int tiendatcuoc = (select == 0 ? COST_0 : select == 1 ? COST_1 : COST_2);
        String money = Util.numberFormatLouis(tiendatcuoc);
        player.iDMark.setMoneyKeoBuaBao(tiendatcuoc);
        player.iDMark.setTimePlayKeoBuaBao(System.currentTimeMillis() + (timePlay * 1000));
        ItemTimeService.gI().sendTextTimeKeoBuaBao(player, (int) timePlay);
        npc.createOtherMenu(player, LyTieuNuong.ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_GREEN + "Mức vàng cược: " + money + "\n"
                + "Hãy chọn Kéo, Búa hoặc Bao\n"
                + ConstFont.BOLD_RED + "Thời gian " + timePlay + " giây bắt đầu",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
    }

    public static void confirmPlay(Npc npc, Player player, int select) {
        switch (select) {
            case 0, 1, 2:
                if (player.inventory.gold < player.iDMark.getMoneyKeoBuaBao()) {
                    long soVangConThieu = player.iDMark.getMoneyKeoBuaBao() - player.inventory.gold;
                    Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(soVangConThieu) + " vàng nữa");
                    return;
                }

                byte playerChoice = (byte) select;
                byte serverChoice;

                int rand = Util.nextInt(100); // 0–99

                if (rand < 40) {
                    // 40% server ra cái để thắng người chơi
                    switch (playerChoice) {
                        case KEO:
                            serverChoice = BUA;
                            break;
                        case BUA:
                            serverChoice = BAO;
                            break;
                        case BAO:
                            serverChoice = KEO;
                            break;
                        default:
                            serverChoice = (byte) Util.nextInt(3);
                    }
                } else if (rand < 70) {
                    // 30% server ra cái để hòa với player
                    serverChoice = playerChoice;
                } else {
                    // 30% server ra cái để thua
                    switch (playerChoice) {
                        case KEO:
                            serverChoice = BAO;
                            break;
                        case BUA:
                            serverChoice = KEO;
                            break;
                        case BAO:
                            serverChoice = BUA;
                            break;
                        default:
                            serverChoice = (byte) Util.nextInt(3);
                    }
                }

                player.iDMark.setKeoBuaBaoPlayer(playerChoice);
                player.iDMark.setKeoBuaBaoServer(serverChoice);

                int result = RockPaperScissorsService.checkWinLose(player);
                if (result == 1) {
                    RockPaperScissorsService.winKeoBuaBao(npc, player);
                } else if (result == 2) {
                    RockPaperScissorsService.loseKeoBuaBao(npc, player);
                } else {
                    RockPaperScissorsService.hoaKeoBuaBao(npc, player);
                }
                break;

            case 3:
                npc.createOtherMenu(player, LyTieuNuong.ConstMiniGame.MENU_KEO_BUA_BAO,
                        "Hãy chọn mức cược.",
                        "1 Tr vàng",
                        "5 Tr vàng",
                        "10 Tr vàng");
                break;

            default:
                break;
        }
    }
}
