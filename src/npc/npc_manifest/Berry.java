/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package npc.npc_manifest;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.TaskService;
import shop.ShopService;

/**
 *
 * @author Administrator
 */
public class Berry extends Npc {

    public Berry(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                switch (mapId) {
                    case 160 -> {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "\b|1|Đưa em đi đu đưa đi, đưa em đi đu đưa đi",
                                "Cửa hàng",
                                "Từ chối");
                    }
                    default ->
                        super.openBaseMenu(player);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 160 -> {
                    ShopService.gI().opendShop(player, "SHOP_BERRY", false);
                }
            }
        }
    }
}
