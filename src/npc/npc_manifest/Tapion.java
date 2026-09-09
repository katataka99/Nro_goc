package npc.npc_manifest;

/**
 * @author HM
 */
import npc.Npc;
import player.Player;
import services.Service;

public class Tapion extends Npc {

    public Tapion(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, 0, "Hãy quay lại vào lúc sau", "Ok");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            Service.gI().sendThongBao(player, "Chức năng hiện tại chưa khả dụng");
        }
    }
}