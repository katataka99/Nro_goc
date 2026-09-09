/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services.func;

import item.Item;
import item.Item.ItemOption;
import jdbc.daos.PlayerDAO;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class QuaToriBot {

    public static void Qua_1(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 5) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            Item itemqua4;
            Item itemqua5;
            Item itemqua6;
            try {
                int time = 5;
                if (player.getSession().hasReceivedVIP) {
                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
                    return;
                }
                if (player.getSession().cash >= 20000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 1 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }
                    PlayerDAO.subVIP(player);
                    PlayerDAO.subcash(player, 20000);
                    player.getSession().vip = 1;
                    Service.gI().sendMoney(player);
                    itemqua3 = ItemService.gI().createNewItem((short) 447, 20);
                    itemqua3.itemOptions.add(new ItemOption(101, 5));
                    itemqua4 = ItemService.gI().createNewItem((short) 1785, 1);
                    itemqua4.itemOptions.add(new ItemOption(50, 25));
                    itemqua4.itemOptions.add(new ItemOption(101, 70));
                    itemqua4.itemOptions.add(new ItemOption(95, 15));
                    itemqua4.itemOptions.add(new ItemOption(30, 0));
                    itemqua4.itemOptions.add(new ItemOption(93, 15));
                    itemqua5 = ItemService.gI().createNewItem((short) 1796, 3);
                    itemqua5.itemOptions.add(new ItemOption(30, 0));
                    itemqua6 = ItemService.gI().createNewItem((short) 1578, 1);
                    itemqua6.itemOptions.add(new ItemOption(77, 8));
                    itemqua6.itemOptions.add(new ItemOption(103, 8));
                    itemqua6.itemOptions.add(new ItemOption(84, 20));
                    itemqua6.itemOptions.add(new ItemOption(93, 15));

                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua5.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua6.template.name);

                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemqua4);
                    InventoryService.gI().addItemBag(player, itemqua5);
                    InventoryService.gI().addItemBag(player, itemqua6);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Tiền Để Nhận Vip Nạp Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 6 ô trống hành trang");
        }

    }

    public static void Qua_2(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 5) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            Item itemqua4;
            Item itemqua5;
            Item itemqua6;
            Item itemqua7;
            Item itemqua8;
            try {
                int time = 5;
                if (player.getSession().hasReceivedVIP) {
                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
                    return;
                }
                if (player.getSession().cash >= 100000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 2 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }
                    PlayerDAO.subVIP(player);
                    PlayerDAO.subcash(player, 100000);
                    player.getSession().vip = 2;
                    Service.gI().sendMoney(player);

                    itemqua1 = ItemService.gI().createNewItem((short) 987, 10);

                    itemqua2 = ItemService.gI().createNewItem((short) 1578, 1);
                    itemqua2.itemOptions.add(new ItemOption(77, 8));
                    itemqua2.itemOptions.add(new ItemOption(103, 8));
                    itemqua2.itemOptions.add(new ItemOption(84, 20));

                    itemqua3 = ItemService.gI().createNewItem((short) 1785, 1);
                    itemqua3.itemOptions.add(new ItemOption(50, 25));
                    itemqua3.itemOptions.add(new ItemOption(101, 70));
                    itemqua3.itemOptions.add(new ItemOption(95, 15));
                    itemqua3.itemOptions.add(new ItemOption(30, 0));

                    itemqua4 = ItemService.gI().createNewItem((short) 918, 1);
                    itemqua4.itemOptions.add(new ItemOption(50, 11));
                    itemqua4.itemOptions.add(new ItemOption(77, 12));
                    itemqua4.itemOptions.add(new ItemOption(103, 12));
                    itemqua4.itemOptions.add(new ItemOption(5, 15));
                    itemqua4.itemOptions.add(new ItemOption(30, 1));

                    itemqua5 = ItemService.gI().createNewItem((short) 956, 10);

                    itemqua6 = ItemService.gI().createNewItem((short) 1796, 5);

                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua5.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua6.template.name);

                    InventoryService.gI().addItemBag(player, itemqua1);
                    InventoryService.gI().addItemBag(player, itemqua2);
                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemqua4);
                    InventoryService.gI().addItemBag(player, itemqua5);
                    InventoryService.gI().addItemBag(player, itemqua6);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Tiền Để Nhận Vip Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 6 ô trống hành trang");
        }
    }

    public static void Qua_3(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 10) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            Item itemqua4;
            Item itemqua5;
            Item itemqua6;
            Item itemqua7;
            Item itemqua8;
            Item itemqua9;
            Item itemqua10;

            try {
                int time = 5;
                if (player.getSession().hasReceivedVIP) {
                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
                    return;
                }
                if (player.getSession().cash >= 300000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 3 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }
                    PlayerDAO.subVIP(player);
                    PlayerDAO.subcash(player, 300000);
                    player.getSession().vip = 3;
                    Service.gI().sendMoney(player);

                    itemqua1 = ItemService.gI().createNewItem((short) 987, 30);

                    itemqua2 = ItemService.gI().createNewItem((short) 1798, 1);
                    itemqua2.itemOptions.add(new ItemOption(50, 15));
                    itemqua2.itemOptions.add(new ItemOption(77, 10));
                    itemqua2.itemOptions.add(new ItemOption(103, 10));

                    itemqua3 = ItemService.gI().createNewItem((short) 1797, 1);
                    itemqua3.itemOptions.add(new ItemOption(50, 30));
                    itemqua3.itemOptions.add(new ItemOption(77, 28));
                    itemqua3.itemOptions.add(new ItemOption(103, 28));
                    itemqua3.itemOptions.add(new ItemOption(101, 50));
                    itemqua3.itemOptions.add(new ItemOption(106, 0));

                    itemqua4 = ItemService.gI().createNewItem((short) 1722, 1);
                    itemqua4.itemOptions.add(new ItemOption(50, 15));
                    itemqua4.itemOptions.add(new ItemOption(77, 13));
                    itemqua4.itemOptions.add(new ItemOption(103, 13));
                    itemqua4.itemOptions.add(new ItemOption(14, 5));
                    itemqua4.itemOptions.add(new ItemOption(5, 10));
                    itemqua4.itemOptions.add(new ItemOption(30, 1));

                    itemqua5 = ItemService.gI().createNewItem((short) 1786, 1);
                    itemqua5.itemOptions.add(new ItemOption(50, 12));
                    itemqua5.itemOptions.add(new ItemOption(77, 16));
                    itemqua5.itemOptions.add(new ItemOption(103, 16));

                    itemqua6 = ItemService.gI().createNewItem((short) 956, 20);

                    itemqua7 = ItemService.gI().createNewItem((short) 1796, 10);

                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua5.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua6.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua7.template.name);

                    InventoryService.gI().addItemBag(player, itemqua1);
                    InventoryService.gI().addItemBag(player, itemqua2);
                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemqua4);
                    InventoryService.gI().addItemBag(player, itemqua5);
                    InventoryService.gI().addItemBag(player, itemqua6);
                    InventoryService.gI().addItemBag(player, itemqua7);

                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Tiền Để Nhận Vip Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 6 ô trống hành trang");
        }
    }

    public static void Qua_4(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 5) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            Item itemqua4;
            Item itemqua5;
            Item itemqua6 = null;
            Item itemqua7;
            Item itemqua8;
            Item itemqua9;
            Item itemqua10;
            Item itemqua11;
            try {
                int time = 5;
                if (player.getSession().hasReceivedVIP) {
                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
                    return;
                }
                if (player.getSession().cash >= 400000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 4 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }
                    PlayerDAO.subVIP(player);
                    PlayerDAO.subcash(player, 400000);
                    player.getSession().vip = 4;
                    Service.gI().sendMoney(player);

                    itemqua1 = ItemService.gI().createNewItem((short) 457, 30);
                    itemqua1.itemOptions.add(new ItemOption(100, 1));
                    itemqua1.itemOptions.add(new ItemOption(86, 0));

                    itemqua2 = ItemService.gI().createNewItem((short) 956, 15);

                    int randomda = new int[]{1079, 1080, 1081, 1082, 1083, 220, 221, 222, 223, 224}[Util
                            .nextInt(10)];
                    itemqua3 = ItemService.gI().createNewItem((short) randomda, 15);
                    itemqua4 = ItemService.gI().createNewItem((short) 987, 50);
                    itemqua5 = ItemService.gI().createNewItem((short) 1187, 50);
                    switch (player.gender) {
                        case 0:
                            itemqua6 = ItemService.gI().createNewItem((short) 1227, 2);
                            break;
                        case 1:
                            itemqua6 = ItemService.gI().createNewItem((short) 1228, 2);
                            break;
                        case 2:
                            itemqua6 = ItemService.gI().createNewItem((short) 1229, 2);
                            break;
                        default:
                            break;
                    }
                    if (itemqua6 != null) {
                        InventoryService.gI().addItemBag(player, itemqua6);
                        Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua6.template.name);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể tạo vật phẩm theo hành tinh!");
                    }
                    itemqua7 = ItemService.gI().createNewItem((short) 1283, 1);
                    itemqua7.itemOptions.add(new ItemOption(50, 27));
                    itemqua7.itemOptions.add(new ItemOption(77, 25));
                    itemqua7.itemOptions.add(new ItemOption(103, 25));
                    itemqua7.itemOptions.add(new ItemOption(5, 15));
                    itemqua7.itemOptions.add(new ItemOption(101, 100));
                    itemqua7.itemOptions.add(new ItemOption(95, 10));
                    itemqua7.itemOptions.add(new ItemOption(30, 1));

                    itemqua8 = ItemService.gI().createNewItem((short) 1284, 1);
                    itemqua8.itemOptions.add(new ItemOption(77, 15));
                    itemqua8.itemOptions.add(new ItemOption(103, 15));
                    itemqua8.itemOptions.add(new ItemOption(14, 9));
                    itemqua8.itemOptions.add(new ItemOption(84, 0));
                    itemqua8.itemOptions.add(new ItemOption(30, 1));

                    itemqua9 = ItemService.gI().createNewItem((short) 1199, 20);

                    itemqua10 = ItemService.gI().createNewItem((short) 1115, 1);
                    itemqua10.itemOptions.add(new ItemOption(50, 28));
                    itemqua10.itemOptions.add(new ItemOption(77, 27));
                    itemqua10.itemOptions.add(new ItemOption(103, 27));
                    itemqua10.itemOptions.add(new ItemOption(14, 15));
                    itemqua10.itemOptions.add(new ItemOption(95, 10));
                    itemqua10.itemOptions.add(new ItemOption(93, 365));
                    itemqua10.itemOptions.add(new ItemOption(30, 1));
                    itemqua11 = ItemService.gI().createNewItem((short) 1127, 1);
                    itemqua11.itemOptions.add(new ItemOption(50, 15));
                    itemqua11.itemOptions.add(new ItemOption(77, 13));
                    itemqua11.itemOptions.add(new ItemOption(103, 13));
                    itemqua11.itemOptions.add(new ItemOption(14, 5));
                    itemqua11.itemOptions.add(new ItemOption(5, 10));
                    itemqua11.itemOptions.add(new ItemOption(30, 1));
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua5.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua7.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua8.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua9.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua10.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua11.template.name);
                    InventoryService.gI().addItemBag(player, itemqua1);
                    InventoryService.gI().addItemBag(player, itemqua2);
                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemqua4);
                    InventoryService.gI().addItemBag(player, itemqua5);
                    InventoryService.gI().addItemBag(player, itemqua7);
                    InventoryService.gI().addItemBag(player, itemqua8);
                    InventoryService.gI().addItemBag(player, itemqua9);
                    InventoryService.gI().addItemBag(player, itemqua10);
                    InventoryService.gI().addItemBag(player, itemqua11);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Tiền Để Nhận Vip Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 6 ô trống hành trang");
        }
    }
}
