package npc.npc_manifest;

/**
 * @author HM
 */
import consts.ConstMenu;
import npc.Npc;
import player.Player;
import server.Manager;
import services.NpcService;
import services.func.TopService;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstMenu.MENU_SHOW,
                "|2|Sự kiện đua Top\n"
                + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                + "Giải thưởng khủng chưa từng có, xem chi tiết tại diễn đàn, fanpage\n"
                + Manager.demTimeSuKien(),
                "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (select) {
                case 0:
                    NpcService.gI().createBigMessage(player, avartar, "Top 100 Sức mạnh", (byte) 1, "Mở", "https://ngocrongbkt.online/core/bang-xep-hang.php");
                    break;
                case 1:
                    NpcService.gI().createBigMessage(player, avartar, "Top 100 Nhiệm vụ", (byte) 1, "Mở", "https://ngocrongbkt.online/core/bang-xep-hang.php");
                    break;
                case 2:
                    NpcService.gI().createBigMessage(player, avartar, "Top 100 Đại gia", (byte) 1, "Mở", "https://ngocrongbkt.online/core/bang-xep-hang.php");
                    break;
            }
        }
    }
}
