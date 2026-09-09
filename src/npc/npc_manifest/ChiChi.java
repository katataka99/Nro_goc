/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package npc.npc_manifest;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import server.Manager;
import services.NpcService;
import services.Service;
import services.func.TopService;
import shop.ShopService;

/**
 *
 * @author Administrator
 */
public class ChiChi extends Npc {

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Bạn muốn hỏi chi ?",
                    "Top\nđổi trứng\nVàng Rồng\nNhí", "Top\nHộp quà giỗ\ntổ cao cấp", "Top\nBánh dầy", "Cửa\nhàng",
                    "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5) {
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            createOtherMenu(player, 1,
                                    "Sự kiện đua Top đổi trứng Vàng Rồng Nhí nhận quà khủng\n"
                                    + "Kết thúc và trao giải sau: (... ngày nữa)\n"
                                    + "Hạn chót nhận giải: (... ngày nữa)\n"
                                    + "Đến gặp Chi Chi để nhận giải nhé\n"
                                    + "Chi tiết xem tại diễn đàn, fanpage.",
                                    "Top 100\nđổi Trứng\nVàng Rồng\nNhí", "Xem điểm", "Đóng");
                        case 1 ->
                            createOtherMenu(player, 2,
                                    "Sự kiện đua Top Hộp quà giỗ tổ cao cấp nhận quà khủng\n"
                                    + "Kết thúc và trao giải sau: (... ngày nữa)\n"
                                    + "Hạn chót nhận giải: (... ngày nữa)\n"
                                    + "Đến gặp Chi Chi để nhận giải nhé\n"
                                    + "Chi tiết xem tại diễn đàn, fanpage",
                                    "Top 100\nHộp quà giỗ\ntổ cao cấp", "Xem điểm", "Đóng");
                        case 2 ->
                            createOtherMenu(player, 3,
                                    "Sự kiện đua Top Bánh dầy nhận quà khủng\n"
                                    + "Kết thúc và trao giải sau: (... ngày nữa)\n"
                                    + "Hạn chót nhận giải: (... ngày nữa)\n"
                                    + "Đến gặp Chi Chi để nhận giải nhé\n"
                                    + "Chi tiết xem tại diễn đàn, fanpage",
                                    "Top 100\nBánh dầy", "Xem điểm", "Đóng");
                        case 3 ->
                            ShopService.gI().opendShop(player, "SU_KIEN", false);
                    }
                } else if (player.iDMark.getIndexMenu() == 1) {
                    switch (select) {
                        case 0:
                            TopService.gI().showListTopTrung(player);
                            break;
                        case 1:
                            if (player.point_trung >= 10) {
                                Service.gI().sendThongBaoOK(player, "Bạn đang có " + player.point_trung + " Điểm");
                            } else {
                                createOtherMenu(player, 2010,
                                        "Chưa đủ điểm vào TOP (ít nhất là 10 điểm)",
                                        "Đóng");
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == 2) {
                    switch (select) {
                        case 0:
                            TopService.gI().showListTopGioTio(player);
                            break;
                        case 1:
                            if (player.point_gioto >= 10) {
                                Service.gI().sendThongBaoOK(player, "Bạn đang có " + player.point_gioto + " Điểm");
                            } else {
                                createOtherMenu(player, 2010,
                                        "Chưa đủ điểm vào TOP (ít nhất là 10 điểm)",
                                        "Đóng");
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == 3) {
                    switch (select) {
                        case 0:
                            TopService.gI().showListTopBanhDay(player);
                            break;
                        case 1:
                            if (player.point_banhday >= 10) {
                                Service.gI().sendThongBaoOK(player, "Bạn đang có " + player.point_banhday + " Điểm");
                            } else {
                                createOtherMenu(player, 2010,
                                        "Chưa đủ điểm vào TOP (ít nhất là 10 điểm)",
                                        "Đóng");
                            }
                            break;
                    }
                }
            }
        }
    }
}
