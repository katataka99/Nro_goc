package services.func;

/*
 *
 *
 * @author HM
 */
import boss.Boss;
import boss.BossID;
import boss.boss_manifest.BossNew.SoiHecQuyn;
import consts.ConstItem;
import models.Combine.CombineService;
import models.ShenronEvent.ShenronEventService;
import models.Card.Card;
import models.Card.RadarService;
import models.Card.RadarCard;
import consts.ConstMap;
import item.Item;
import consts.ConstNpc;
import consts.ConstPlayer;
import item.Item.ItemOption;
import map.Zone;
import player.Inventory;
import services.*;
import player.Player;
import skill.Skill;
import network.Message;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;
import server.io.MySession;
import utils.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import map.ItemMap;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private int randClothes(int level) {
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null) {
            return;
        }
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            if (player.setClothes != null) {
                player.setClothes.setup();
            }
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().sendFlagBag(player);
            Service.gI().point(player);
            Service.gI().sendSpeedPlayer(player, -1);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public Item finditem(Player player, int iditem) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học "
                                            + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {
                                    // if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                    // Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                    // return;
                                    // }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    if (player.zone.items.stream()
                                            .filter(it -> it != null && it.itemTemplate.type == 22).count() > 2) {
                                        Service.gI().sendThongBaoOK(player, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            UseItem.gI().useItem(player, item, index);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }

                        if (item.isNotNullItem() && item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }
                        if (!item.isNotNullItem()) {
                            return;
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryService.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryService.gI().sendItemBag(player);
                    break;
                case ACCEPT_USE_ITEM:
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
                if (time == 0) {
                    Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
                } else {
                    openRuongGo(pl, item);
                }
                return;
            }
            if (item.template.strRequire <= pl.nPoint.power) {
                switch (item.template.type) {
                    case 33: // card
                        UseCard(pl, item);
                        break;
                    case 7: // sách học, nâng skill
                        learnSkill(pl, item);
                        break;
                    case 6: // đậu thần
                        this.eatPea(pl);
                        break;
                    case 12: // ngọc rồng các loại
                        controllerCallRongThan(pl, item);
                        break;
                    case 23: // thú cưỡi mới
                    case 24: // thú cưỡi cũ
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    case 11: // item bag
                        if (indexBag >= 0 && indexBag < pl.inventory.itemsBag.size()) {
                            InventoryService.gI().itemBagToBody(pl, indexBag);
                            Service.gI().sendFlagBag(pl);
                        } else {
                            Service.gI().sendThongBao(pl, "Không thể sử dụng vật phẩm. ID vật phẩm không hợp lệ.");
                        }
                        break;
                    case 75:
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendchienlinh(pl, (short) (item.template.iconID - 1));
                        break;
                    case 21:
                        if (pl.newPet != null) {
                            ChangeMapService.gI().exitMap(pl.newPet);
                            pl.newPet.dispose();
                            pl.newPet = null;
                        }
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        PetService.Pet2(pl, item.template.head, item.template.body, item.template.leg);
                        Service.gI().point(pl);
                        break;
                    case 72: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendPetFollow(pl, (short) (item.template.iconID - 1));
                        break;
                    }
                    case 35: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    }
                    default:
                        switch (item.template.id) {
                            case 992: // Nhan thoi khong
                                pl.type = 2;
                                pl.maxTime = 5;
                                Service.gI().Transport(pl);
                                break;
                            case 361:
                                pl.idGo = (short) Util.nextInt(0, 6);
                                NgocRongNamecService.gI().menuCheckTeleNamekBall(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                break;
                            case 211: // nho tím
                            case 212: // nho xanh
                                eatGrapes(pl, item);
                                break;
                            case 342:
                            case 343:
                            case 344:
                            case 345:
                                if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22)
                                        .count() < 3) {
                                    Service.gI().dropSatellite(pl, item, pl.zone, pl.location.x, pl.location.y);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                } else {
                                    Service.gI().sendThongBaoOK(pl, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                }
                                break;
                            case 380: // cskb
                                openCSKB(pl, item);
                                break;
                            case 381: // cuồng nộ
                            case 382: // bổ huyết
                            case 383: // bổ khí
                            case 384: // giáp xên
                            case 385: // ẩn danh
                            case 379: // máy dò capsule
                            case 638: // commeson
                            case 2075: // rocket
                            case 2160: // Nồi cơm điện
                            case 579:
                            case 1045: // đuôi khỉ
                            case 663: // bánh pudding
                            case 664: // xúc xíc
                            case 665: // kem dâu
                            case 666: // mì ly
                            case 667: // sushi
                            case 1150:
                            case 1151:
                            case 1152:
                            case 1153:
                            case 1154:
                            case 764:
                            case 1628:
                            case 1532:
                            case 1635:
                            case 1016:
                            case 1017:
                                useItemTime(pl, item);
                                break;
                            case 1852:
                                hopQuaCaiTrang(pl, item);
                                break;
                            case 1858:
                                hopQuaCaiTrangGogeta(pl, item);
                                break;
                            case 1863:
                                hopQuaCaiTrangOnePiece(pl, item);
                                break;
                            case 1864:
                                hopQuaCaiTrangThanhGuomDietQuy(pl, item);
                                break;
                            case 1005:
                                XoCaXanh(pl, item);
                                break;
                            case 1006:
                                XoCaVang(pl, item);
                                break;
                            case 1866:
                                XoCaDo(pl, item);
                                break;
                            case 1862:
                                hopQuaSuKienCarot(pl, item);
                                break;
                            case 1865:
                                hopQuaThuCuoi(pl, item);
                                break;
                            case 1867:
                                hopQuaCaiTrangVegeta(pl, item);
                                break;
                            case 1868:
                                hopQuaCaiTrangGranola(pl, item);
                                break;
                            case 1869:
                                hopQuaCaiTrangCadic(pl, item);
                                break;
                            case 1870:
                                hopQuaCaiTrangGranolaTop(pl, item);
                                break;
                            case 1871:
                                hopQuaCaiTrangVegetaTop(pl, item);
                                break;
                            case 460:
                                XuongCho(pl, item);
                                break;
                            case 1576:
                                ItemService.gI().OpenItem1576(pl, item);
                                break;
                            case 1779:
                                ItemService.gI().OpenItem1779(pl, item);
                                break;
                            case 1780:
                                ItemService.gI().OpenItem1780(pl, item);
                                break;
                            case 1781:
                                ItemService.gI().OpenItem1781(pl, item);
                                break;
                            case 758:
                                ItemService.gI().OpenItem758(pl, item);
                                break;
                            case 1840:
                                ItemService.gI().OpenItem1840(pl, item);
                                break;
                            case 1757:
                                ItemService.gI().OpenItem1757(pl, item);
                                break;
                            case 1796:
                                ItemService.gI().OpenItem1796(pl, item);
                                break;
                            case 962, 963:
                                ItemService.gI().OpenCapsuleCaiTrang(pl, item);
                                break;
                            case 1808:
                                open1808(pl, item);
                                break;
                            case 1804:
                                open1804(pl, item);
                                break;
                            case 1821:
                                open1821(pl, item);
                                break;
                            case 1824:
                                open1824(pl, item);
                                break;
                            case 1565:
                                ChangeMapService.gI().changeMapBySpaceShip(pl, 177, -1, 124);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                break;
                            case 1822:
                                Item trungRong = InventoryService.gI().findItemBag(pl, 1822);
                                if (trungRong != null && trungRong.quantity >= 99) {
                                    open1822(pl, item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Bạn cần x99 Mảnh Trứng");
                                }
                                break;
                            case 627:
                                open627(pl, item);
                                break;
                            case 880:
                            case 881:
                            case 882:
                                if (pl.itemTime.isEatMeal2) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 521: // tdlt
                                useTDLT(pl, item);
                                break;
                            case 454: // bông tai
                                UseItem.gI().usePorata(pl);
                                break;
                            case 921: // bông tai
                                UseItem.gI().usePorata2(pl);
                                break;
                            case 193: // gói 10 viên capsule
                                openCapsuleUI(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            case 194: // capsule đặc biệt
                                openCapsuleUI(pl);
                                break;
                            case 401: // đổi đệ tử
                                changePet(pl, item);
                                break;
                            case 402: // sách nâng chiêu 1 đệ tử
                            case 403: // sách nâng chiêu 2 đệ tử
                            case 404: // sách nâng chiêu 3 đệ tử
                            case 759: // sách nâng chiêu 4 đệ tử
                                upSkillPet(pl, item);
                                break;
                            case 726:
                                UseItem.gI().ItemManhGiay(pl, item);
                                break;
                            case 727:
                            case 728:
                                UseItem.gI().ItemSieuThanThuy(pl, item);
                                break;
                            case 648:
                                ItemService.gI().OpenItem648(pl, item);
                                break;
                            case 736:
                                ItemService.gI().OpenItem736(pl, item);
                                break;
                            case 987:
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); // đá bảo vệ
                                break;
                            case 2006:
                                Input.gI().createFormChangeNameByItem(pl);
                                break;
                            case 1623:
                                TaskService.gI().sendNextTaskMain(pl);
                                break;
                            case 1228:
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.HOP_QUA_THAN_LINH, -1,
                                        "Chọn hành tinh của đồ thần linh muốn nhận.",
                                        "Trái đất", "Namek", "Xayda");
                                break;
                            case 1626: {
                                int[] listItem = {856, 943, 942};
                                if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                                    Service.gI().sendThongBaoOK(pl, "Cần 1 ô hành trang để mở");
                                    return;
                                }
                                Item phuKien = ItemService.gI().createNewItem((short) listItem[Util.nextInt(2)]);
                                if (phuKien.template.id == 856) {
                                    phuKien.itemOptions.add(new Item.ItemOption(50, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(77, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(103, 10));
                                } else if (phuKien.template.id == 943) {
                                    phuKien.itemOptions.add(new Item.ItemOption(50, 10));
                                } else if (phuKien.template.id == 942) {
                                    phuKien.itemOptions.add(new Item.ItemOption(77, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(103, 10));
                                }
                                if (Util.isTrue(95, 100)) {
                                    phuKien.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 5)));
                                }
                                InventoryService.gI().addItemBag(pl, phuKien);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + phuKien.template.name);
                            }
                            break;
                        }
                        break;
                }
                TaskService.gI().checkDoneTaskUseItem(pl, item);
                InventoryService.gI().sendItemBag(pl);
            } else {
                Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
            }
        }
    }

    public void openRuongGo(Player pl, Item item) {
        List<String> textRuongGo = new ArrayList<>();
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time != 0) {
            Item itemReward = null;
            int param = item.itemOptions.get(0).param;
            int gold = 0;
            int[] listItem = {441, 442, 443, 444, 445, 446, 447, 220, 221, 222, 223, 224, 225};
            int[] listClothesReward;
            int[] listItemReward;
            String text = "Bạn nhận được\n";
            if (param < 8) {
                gold = 100000 * param;
                listClothesReward = new int[]{randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 3);
            } else if (param < 10) {
                gold = 250000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 4);
            } else {
                gold = 500000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 5);
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");
            }
            for (var i : listClothesReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionClothes(itemReward.template.id, itemReward.template.type,
                        itemReward.itemOptions);
                RewardService.gI().initStarOption(itemReward, new RewardService.RatioStar[]{
                    new RewardService.RatioStar((byte) 1, 1, 2), new RewardService.RatioStar((byte) 2, 1, 3),
                    new RewardService.RatioStar((byte) 3, 1, 4), new RewardService.RatioStar((byte) 4, 1, 5),});
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            for (var i : listItemReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionSaoPhaLe(itemReward);
                itemReward.quantity = Util.nextInt(1, 5);
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            if (param == 11) {
                itemReward = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
                itemReward.quantity = Util.nextInt(1, 3);
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1,
                    "Bạn nhận được\n|1|+" + Util.numberToMoney(gold) + " vàng", "OK [" + textRuongGo.size() + "]");
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            pl.inventory.addGold(gold);
            Logger.success("Người chs: " + pl.name + "Vàng nhận từ Mở rương: " + gold);
            InventoryService.gI().sendItemBag(pl);
            PlayerService.gI().sendInfoHpMpMoney(pl);
        }
    }

    // public void openRuongGo(Player player) {
    // Item ruongGo = InventoryService.gI().findItemBag(player, 570);
    // if (ruongGo != null) {
    // int level = InventoryService.gI().getParam(player, 72, 570);
    // if (InventoryService.gI().getCountEmptyBag(player) < level) {
    // Service.gI().sendThongBao(player, "Cần ít nhất " + level + " ô trống trong
    // hành trang");
    // } else {
    // player.itemsWoodChest.clear();
    // if (level == 0) {
    // InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
    // InventoryService.gI().sendItemBag(player);
    // Item item = ItemService.gI().createNewItem((short) 673);
    // InventoryService.gI().addItemBag(player, item);
    // InventoryService.gI().sendItemBag(player);
    // Service.gI().sendThongBao(player, "Bạn vừa nhận được set Ngọc Rồng Sơ Cấp!");
    // return;
    // }
    // int rand = Util.nextInt(0, 6);
    // Item level1 = ItemService.gI().createNewItem(((short) (441 + rand)));
    // level1.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand ==
    // 4) ? 3 : 5));
    // level1.quantity = Util.nextInt(1, level * 2);
    // player.itemsWoodChest.add(level1);
    // if (level > 1) {
    // rand = Util.nextInt(0, 4);
    // Item level2 = ItemService.gI().createNewItem(((short) (220 + rand)));
    // level2.itemOptions.add(new Item.ItemOption(71 - rand, 0));
    // level2.quantity = Util.nextInt(1, level * 3);
    // player.itemsWoodChest.add(level2);
    // }
    // if (level > 2) {
    // Item level3 = ItemService.gI().createNewItem((short) 20); // 7 sao
    // level3.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level3);
    // }
    // if (level > 3) {
    // Item level4 = ItemService.gI().createNewItem((short) 19); // 6 sao
    // level4.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level4);
    // }
    // if (level > 4) {
    // Item level5 = ItemService.gI().createNewItem((short) 18); // 5 sao
    // level5.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level5);
    // }
    // if (level > 5) {
    // Item level6 = ItemService.gI().createNewItem((short) 17); // 4 sao
    // level6.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level6);
    // }
    // if (level > 6) {
    // Item level7 = ItemService.gI().createNewItem((short) 16); // 3 sao
    // level7.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level7);
    // }
    // if (level > 7) {
    // Item level8 = ItemService.gI().createNewItem((short) 457); // thoi vang
    // level8.quantity = Util.nextInt(100, level * 100);
    // player.itemsWoodChest.add(level8);
    // }
    // if (level > 8) {
    // Item level9 = ItemService.gI().createNewItem((short) 1229); // bi kiep tuyet
    // ky
    // level9.quantity = Util.nextInt(15, level + 15);
    // player.itemsWoodChest.add(level9);
    // }
    // if (level > 9) {
    // int[] itemId = {1229, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021,
    // 2022, 2023, 2024};
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level10 = ItemService.gI().createNewItem((short) itemId[Util.nextInt(0,
    // 12)]);
    // level10.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level10.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level10.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level10.itemOptions.add(new Item.ItemOption(30, 0));
    // level10.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // level10.quantity = 1;
    // player.itemsWoodChest.add(level10);
    // }
    // if (level > 10) {
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level11 = ItemService.gI().createNewItem((short) Util.nextInt(2112,
    // 2118));
    // level11.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level11.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level11.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level11.itemOptions.add(new Item.ItemOption(30, 0));
    // if (Util.isTrue(90, 100)) {
    // level11.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // }
    // level11.quantity = 1;
    // player.itemsWoodChest.add(level11);
    // }
    // if (level > 11) {
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level12 = ItemService.gI().createNewItem((short) Util.nextInt(2108,
    // 2122));
    // level12.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level12.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level12.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level12.itemOptions.add(new Item.ItemOption(30, 0));
    // if (Util.isTrue(50, 100)) {
    // level12.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // }
    // level12.quantity = 1;
    // player.itemsWoodChest.add(level12);
    // }
    // InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
    // InventoryService.gI().sendItemBag(player);
    // for (Item it : player.itemsWoodChest) {
    // InventoryService.gI().addItemBag(player, it);
    // }
    // InventoryService.gI().sendItemBag(player);
    // player.indexWoodChest = player.itemsWoodChest.size() - 1;
    // int i = player.indexWoodChest;
    // if (i < 0) {
    // return;
    // }
    // Item itemWoodChest = player.itemsWoodChest.get(i);
    // player.indexWoodChest--;
    // String info = "|1|" + itemWoodChest.template.name;
    // String info2 = "\n|2|";
    // if (!itemWoodChest.itemOptions.isEmpty()) {
    // for (Item.ItemOption io : itemWoodChest.itemOptions) {
    // if (io.optionTemplate.id != 102 && io.optionTemplate.id != 73) {
    // info2 += io.getOptionString() + "\n";
    // }
    // }
    // }
    // info = (info2.length() > "\n|2|".length() ? (info + info2).trim() :
    // info.trim()) + "\n|0|" + itemWoodChest.template.description;
    // NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1, "Bạn nhận
    // được\n"
    // + info.trim(), "OK" + (i > 0 ? " [" + i + "]" : ""));
    // }
    // }
    // }
    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void phaoHoa(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {1783, 1784};
            short[] bkt = {17, 18, 19, 20};
            int[][] gold = {{10000, 100000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(40, 100)) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                Service.gI().sendThongBao(pl, "Bạn Nhận Được Vàng và 1 Điểm Bắn Pháo");
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else if (Util.isTrue(40, 100)) {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name + " và 1 Điểm Bắn Pháo");
                icon[1] = it.template.iconID;
            } else {
                Item it = ItemService.gI().createNewItem(bkt[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name + " và 1 Điểm Bắn Pháo");
                icon[1] = it.template.iconID;
            }
            // pl.point_phaohoa++;
            Service.gI().LogicEffect(pl, 29, 1, -1, 1, 1, 5000);
            Service.gI().LogicEffect(pl, 64, 1, -1, 1, 1, 5000);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1822(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem((short) 1820);
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được Quả Trứng Rồng Nhí");
            icon[1] = 15127;
            InventoryService.gI().subQuantityItemsBag(pl, item, 99);
            InventoryService.gI().sendItemBag(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1824(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            int rand = Util.nextInt(1000); // sử dụng hệ số 1000 để tăng độ chính xác

            if (rand < 5) { // 0.5% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1811);
                item2.itemOptions.add(new ItemOption(50, 26));
                item2.itemOptions.add(new ItemOption(77, 23));
                item2.itemOptions.add(new ItemOption(103, 23));
                item2.itemOptions.add(new ItemOption(95, 10));
                item2.itemOptions.add(new ItemOption(97, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 15) { // 1% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1557);
                item2.itemOptions.add(new Item.ItemOption(50, 23));
                item2.itemOptions.add(new Item.ItemOption(77, 22));
                item2.itemOptions.add(new Item.ItemOption(27, 22));
                item2.itemOptions.add(new Item.ItemOption(108, 5));
                item2.itemOptions.add(new Item.ItemOption(30, 0));
            } else if (rand < 65) { // 5% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1806);
                item2.itemOptions.add(new ItemOption(77, 17));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 135) { // 7% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1807);
                item2.itemOptions.add(new ItemOption(50, 17));
                item2.itemOptions.add(new ItemOption(14, 11));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 385) { // 25% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1224);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 685) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1225);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 985) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1226);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else {
                Service.gI().sendThongBao(pl, "Bạn không nhận được vật phẩm nào.");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                return;
            }
            // Thêm option ngẫu nhiên
            if (Util.nextInt(100) < 99) {
                item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            }

            InventoryService.gI().addItemBag(pl, item2);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item2.template.name);
            InventoryService.gI().sendItemBag(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1821(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            int rand = Util.nextInt(1000); // sử dụng hệ số 1000 để tăng độ chính xác

            if (rand < 15) { // 0.5% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1813);
                item2.itemOptions.add(new ItemOption(77, 22));
                item2.itemOptions.add(new ItemOption(50, 22));
                item2.itemOptions.add(new ItemOption(94, 8));
                item2.itemOptions.add(new ItemOption(5, 11));
                item2.itemOptions.add(new ItemOption(14, 8));
                item2.itemOptions.add(new ItemOption(106, 0));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 35) { // 1% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1814);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(103, 18));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(94, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 65) { // 5% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1815);
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(108, 7));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 135) { // 7% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1816);
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(5, 7));
                item2.itemOptions.add(new ItemOption(14, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 385) { // 25% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1817);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(94, 15));
                item2.itemOptions.add(new ItemOption(108, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 685) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1818);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(5, 7));
                item2.itemOptions.add(new ItemOption(14, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 985) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1819);
                item2.itemOptions.add(new ItemOption(77, 16));
                item2.itemOptions.add(new ItemOption(50, 16));
                item2.itemOptions.add(new ItemOption(103, 16));
                item2.itemOptions.add(new ItemOption(236, 20));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else {
                Service.gI().sendThongBao(pl, "Bạn không nhận được vật phẩm nào.");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                return;
            }
            // Thêm option ngẫu nhiên
            if (Util.nextInt(100) < 99) {
                item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            }

            InventoryService.gI().addItemBag(pl, item2);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item2.template.name);
            InventoryService.gI().sendItemBag(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1804(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item1;
            if (Util.nextInt(100) < 50) {
                item1 = ItemService.gI().createNewItem((short) 1801);
                item1.itemOptions.add(new ItemOption(50, 18));
                item1.itemOptions.add(new ItemOption(77, 7));
                item1.itemOptions.add(new ItemOption(103, 7));
                item1.itemOptions.add(new ItemOption(5, 11));
                item1.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item1.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item1);
            } else if (Util.nextInt(100) < 50) {
                item1 = ItemService.gI().createNewItem((short) 1802);
                item1.itemOptions.add(new ItemOption(50, 18));
                item1.itemOptions.add(new ItemOption(5, 8));
                item1.itemOptions.add(new ItemOption(14, 5));
                item1.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item1.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item1);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1808(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1579);
                item2.itemOptions.add(new ItemOption(50, 20));
                item2.itemOptions.add(new ItemOption(77, 20));
                item2.itemOptions.add(new ItemOption(103, 20));
                item2.itemOptions.add(new ItemOption(108, 12));
                item2.itemOptions.add(new ItemOption(94, 12));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1580);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(103, 18));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(94, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1581);
                item2.itemOptions.add(new ItemOption(50, 17));
                item2.itemOptions.add(new ItemOption(77, 17));
                item2.itemOptions.add(new ItemOption(103, 17));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1582);
                item2.itemOptions.add(new ItemOption(50, 16));
                item2.itemOptions.add(new ItemOption(77, 16));
                item2.itemOptions.add(new ItemOption(103, 16));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1583);
                item2.itemOptions.add(new ItemOption(50, 15));
                item2.itemOptions.add(new ItemOption(77, 15));
                item2.itemOptions.add(new ItemOption(103, 15));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1584);
                item2.itemOptions.add(new ItemOption(50, 13));
                item2.itemOptions.add(new ItemOption(77, 13));
                item2.itemOptions.add(new ItemOption(103, 13));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1585);
                item2.itemOptions.add(new ItemOption(50, 12));
                item2.itemOptions.add(new ItemOption(77, 12));
                item2.itemOptions.add(new ItemOption(103, 12));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            } else if (Util.nextInt(100) < 20) {
                item2 = ItemService.gI().createNewItem((short) 1586);
                item2.itemOptions.add(new ItemOption(50, 22));
                item2.itemOptions.add(new ItemOption(77, 22));
                item2.itemOptions.add(new ItemOption(103, 22));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(94, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
                if (Util.nextInt(100) < 99) {
                    item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().addItemBag(pl, item2);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void open627(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 7) {
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            short baseAo = 1;
            short baseQuan = 7;
            short baseGang = 22;
            short baseGiay = 28;
            short baseCaiTrang = 1470;
            short genderAo = (pl.gender == 0) ? -1 : ((pl.gender == 2) ? 1 : (short) 0);
            short genderQuan = (pl.gender == 0) ? -1 : ((pl.gender == 2) ? 1 : (short) 0);
            short genderGang = (pl.gender == 0) ? -1 : ((pl.gender == 2) ? 1 : (short) 0);
            short genderGiay = (pl.gender == 0) ? -1 : ((pl.gender == 2) ? 1 : (short) 0);
            short genderCaiTrang = (pl.gender == 0) ? -1 : ((pl.gender == 2) ? 1 : (short) 0);
            Item ao = ItemService.gI().createNewItem((short) (baseAo + genderAo));
            Item quan = ItemService.gI().createNewItem((short) (baseQuan + genderQuan));
            Item gang = ItemService.gI().createNewItem((short) (baseGang + genderGang));
            Item giay = ItemService.gI().createNewItem((short) (baseGiay + genderGiay));
            Item nhan = ItemService.gI().createNewItem((short) 12);
            Item caiTrang = ItemService.gI().createNewItem((short) (baseCaiTrang + genderCaiTrang));
            ao.itemOptions.add(new ItemOption(47, 3));
            quan.itemOptions.add(new ItemOption(6, 30));
            gang.itemOptions.add(new ItemOption(0, 4));
            giay.itemOptions.add(new ItemOption(7, 10));
            nhan.itemOptions.add(new ItemOption(14, 1));
            ao.itemOptions.add(new ItemOption(107, 3));
            quan.itemOptions.add(new ItemOption(107, 3));
            gang.itemOptions.add(new ItemOption(107, 3));
            giay.itemOptions.add(new ItemOption(107, 3));
            nhan.itemOptions.add(new ItemOption(107, 3));
            caiTrang.itemOptions.add(new ItemOption(50, 25));
            caiTrang.itemOptions.add(new ItemOption(77, 25));
            caiTrang.itemOptions.add(new ItemOption(103, 25));
            caiTrang.itemOptions.add(new ItemOption(101, 100));
            caiTrang.itemOptions.add(new ItemOption(93, 2));
            InventoryService.gI().addItemBag(pl, ao);
            InventoryService.gI().addItemBag(pl, quan);
            InventoryService.gI().addItemBag(pl, gang);
            InventoryService.gI().addItemBag(pl, giay);
            InventoryService.gI().addItemBag(pl, nhan);
            InventoryService.gI().addItemBag(pl, caiTrang);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {550, 551, 552};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem(temp[index]);
            switch (it.template.id) {
                case 550 ->
                    it.itemOptions.add(new Item.ItemOption(19, 24));
                case 551 ->
                    it.itemOptions.add(new Item.ItemOption(19, 22));
                case 552 ->
                    it.itemOptions.add(new Item.ItemOption(19, 20));
                default -> {
                }
            }
            it.itemOptions.add(new Item.ItemOption(93, 7));
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);
//            Item it2 = ItemService.gI().createNewItem((short) 595, 300);
//            it2.itemOptions.add(new Item.ItemOption(30, 0));
//             InventoryService.gI().addItemBag(pl, it2);
//              Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it2.template.name);
            Item it3 = ItemService.gI().createNewItem((short) 396, 1);
            it3.itemOptions.add(new Item.ItemOption(89, 0));
            it3.itemOptions.add(new Item.ItemOption(30, 0));
            it3.itemOptions.add(new Item.ItemOption(93, 7));
            InventoryService.gI().addItemBag(pl, it3);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it3.template.name);
            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void XuongCho(Player pl, Item item) {
        List<Player> bosses = pl.zone.getBosses();
        boolean checkSoi = false;

        synchronized (bosses) {
            for (Player bossPlayer : bosses) {
                if (bossPlayer.id == BossID.SOI_HEC_QUYN && !pl.isDie()) {
                    checkSoi = true;
                }
            }
        }

        if (!checkSoi) {
            Service.gI().sendThongBao(pl, "Không tìm thấy Sói hẹc quyn");
            return;
        }

        synchronized (bosses) {
            for (Player bossPlayer : bosses) {
                if (bossPlayer.id == BossID.SOI) {
                    Boss soihecQuyn = (Boss) bossPlayer;
                    if (soihecQuyn != null) {
                        if (((SoiHecQuyn) soihecQuyn).NguyenTanTai_KiemTraNhatXuong()) {
                            Service.gI().sendThongBao(pl, "Sói đã no rồi");
                            continue;
                        } else {
                            ((SoiHecQuyn) soihecQuyn).NhatXuong();
                            Service.gI().chat(soihecQuyn, "Ê, Cục xương ngon quá");
                        }

                        ItemMap itemMap = null;
                        int x = pl.location.x;
                        if (x < 0 || x >= pl.zone.map.mapWidth) {
                            return;
                        }
                        int y = pl.zone.map.yPhysicInTop(x, pl.location.y - 24);
                        itemMap = new ItemMap(pl.zone, 460, 1, x, y, pl.id);
                        itemMap.isPickedUp = true;
                        itemMap.createTime -= 23000;
                        if (itemMap != null) {
                            Service.gI().dropItemMap(pl.zone, itemMap);
                        }

                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        InventoryService.gI().sendItemBag(pl);

                        if (Util.nextInt(4) < 3) {
                            int rand = Util.nextInt(0, 6);
                            short idItem = (short) (rand + 441);
                            Item it = ItemService.gI().createNewItem(idItem);
                            it.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));

                            if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                                InventoryService.gI().addItemBag(pl, it);
                                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
                            } else {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                            }
                        } else {
                            short idItem = 459; // Item 459
                            Item it = ItemService.gI().createNewItem(idItem);
                            it.itemOptions.add(new Item.ItemOption(112, 80));
                            it.itemOptions.add(new Item.ItemOption(93, 90));
                            it.itemOptions.add(new Item.ItemOption(20, Util.nextInt(10000)));
                            if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                                InventoryService.gI().addItemBag(pl, it);
                                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
                            } else {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                            }
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                        }
                        ItemMapService.gI().removeItemMapAndSendClient(itemMap);
                        ((SoiHecQuyn) soihecQuyn).leaveMapNew();
                    }

                }
            }
            InventoryService.gI().sendItemBag(pl);
        }

    }

    private void XoCaDo(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] rewards = {189, 16, 17, 379}; // 189: vàng, 16: NR 3 sao, 17: NR 4 sao, 379: máy dò
            byte index = (byte) Util.nextInt(0, rewards.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it;
            if (rewards[index] == 189) {
                // Vàng vật phẩm số lượng 1m-15m
                int soVang = Util.nextInt(1_000_000, 15_000_000);
                it = ItemService.gI().createNewItem((short) 189, soVang);
            } else {
                it = ItemService.gI().createNewItem(rewards[index], 1);
            }

            // Không thể giao dịch
            //       it.itemOptions.add(new Item.ItemOption(30, 0));
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + it.template.name);

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void XoCaVang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp;
            if (pl.gender == 0) {              // Trái đất
                temp = new short[]{1010, 1000, 994, 996, 1667, 1657};
            } else if (pl.gender == 1) {       // Namek
                temp = new short[]{1012, 1000, 994, 996, 1667, 1657};
            } else {                           // Xayda
                temp = new short[]{1011, 1000, 994, 996, 1667, 1657};
            }
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short itemId = temp[index];
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(itemId);

            // ======== GÁN OPTION RIÊNG CHO TỪNG ID TẠI ĐÂY ========
            if (itemId == 1010) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(104, Util.nextInt(1, 5)));
            } else if (itemId == 1011) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1012) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 23)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1000) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(8, 12)));

            } else if (itemId == 994) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(8, 12)));

            } else if (itemId == 996) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(8, 12)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(8, 12)));
            } else if (itemId == 1657) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 8)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1667) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(19, 24)));
                it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 8)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));

            }
            // ======================================================
            it.itemOptions.add(new Item.ItemOption(30, 0));
            // 80% có thời hạn sử dụng (option id = 93)
            if (Util.nextInt(1, 100) <= 50) {
                int[] hanSuDung = {7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");

        }
    }

    private void XoCaXanh(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp;
            if (pl.gender == 0) {              // Trái đất
                temp = new short[]{1010, 1000, 994, 996};
            } else if (pl.gender == 1) {       // Namek
                temp = new short[]{1012, 1000, 994, 996};
            } else {                           // Xayda
                temp = new short[]{1011, 1000, 994, 996};
            }
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short itemId = temp[index];
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(itemId);

            // ======== GÁN OPTION RIÊNG CHO TỪNG ID TẠI ĐÂY ========
            if (itemId == 1010) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1011) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1012) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
                it.itemOptions.add(new Item.ItemOption(95, Util.nextInt(1, 5)));
            } else if (itemId == 1000) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 10)));

            } else if (itemId == 994) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 10)));

            } else if (itemId == 996) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 10)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 10)));

            }
            // ======================================================
            it.itemOptions.add(new Item.ItemOption(30, 0));
            // 80% có thời hạn sử dụng (option id = 93)
            if (Util.nextInt(100, 100) <= 100) {
                int[] hanSuDung = {3, 7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    private void hopQuaSuKienCarot(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {464, 1041, 1041, 1042, 1042, 1043, 1043};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short itemId = temp[index];
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(itemId);

            // ======== GÁN OPTION RIÊNG CHO TỪNG ID TẠI ĐÂY ========
            if (itemId == 464) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(117, Util.nextInt(10, 16)));
                it.itemOptions.add(new Item.ItemOption(114, 20));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 25)));
            } else if (itemId == 1042) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(114, 20));
                it.itemOptions.add(new Item.ItemOption(196, Util.nextInt(2, 8)));

            } else if (itemId == 1041) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(114, 20));
                it.itemOptions.add(new Item.ItemOption(196, Util.nextInt(2, 8)));
            } else if (itemId == 1043) {
                it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(18, 25)));
                it.itemOptions.add(new Item.ItemOption(114, 20));
                it.itemOptions.add(new Item.ItemOption(196, Util.nextInt(2, 8)));
            }
            // ======================================================

            // 80% có thời hạn sử dụng (option id = 93)
            if (Util.nextInt(1, 100) <= 80) {
                int[] hanSuDung = {3, 7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    private void hopQuaCaiTrangGogeta(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1855, 1853, 1857};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 3)));
            if (Util.nextInt(1, 100) <= 70) {
                int[] hanSuDung = {3, 7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangVegeta(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1861};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(10, 20)));
            it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));
            it.itemOptions.add(new Item.ItemOption(216, Util.nextInt(5, 8))); 

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangGranola(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1859};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(30, 30)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(10, 20)));
             it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));
           it.itemOptions.add(new Item.ItemOption(216, Util.nextInt(5, 8))); 

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangVegetaTop(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1861};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(5, 15)));
            it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));
            it.itemOptions.add(new Item.ItemOption(216, Util.nextInt(2, 5))); 

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangGranolaTop(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1859};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(25, 27)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(5, 15)));
            it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));
            it.itemOptions.add(new Item.ItemOption(216, Util.nextInt(2, 5))); 

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void hopQuaCaiTrangCadic(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1741, 1742, 1743, 1744, 1745, 1746};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(22, 26)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(22, 26)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(22, 26)));
            it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 5)));
            if (Util.nextInt(1, 100) <= 70) {
                int[] hanSuDung = {7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaThuCuoi(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {743, 744, 733, 1711};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(2, 4)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(2, 4)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(2, 4)));
            it.itemOptions.add(new Item.ItemOption(89, 0));
            if (Util.nextInt(1, 100) <= 70) {
                int[] hanSuDung = {7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }

            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangOnePiece(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {618, 619, 620, 621, 622, 623, 624, 625, 626};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(104, Util.nextInt(1, 8)));
            if (Util.nextInt(1, 100) <= 80) {
                int[] hanSuDung = {3, 7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopQuaCaiTrangThanhGuomDietQuy(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 1) {
            short[] temp = {1087, 1088, 1089, 1090, 1091};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            // Thêm các option ngẫu nhiên theo yêu cầu
            it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 22)));
            it.itemOptions.add(new Item.ItemOption(104, Util.nextInt(1, 8)));
            if (Util.nextInt(1, 100) <= 80) {
                int[] hanSuDung = {3, 7, 15, 30};
                int han = hanSuDung[Util.nextInt(0, hanSuDung.length - 1)];
                it.itemOptions.add(new Item.ItemOption(93, han));
            }
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);

            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 1016:
                pl.itemTime.lastTimethuocMo = System.currentTimeMillis();
                pl.itemTime.thuocMoIpana = true;
                break;
            case 1017:
                pl.itemTime.lastTimethuocMo1 = System.currentTimeMillis();
                pl.itemTime.thuocMoIpana1 = true;
                break;
            case 1635:
                pl.itemTime.lastTimeCoBonLa = System.currentTimeMillis();
                pl.itemTime.isCoBonLa = true;
                break;
            case 1532: // máy dò đồ
                pl.itemTime.lastTimeUseKhoBauX2 = System.currentTimeMillis();
                pl.itemTime.isUseKhoBauX2 = true;
                break;
            case 1628: // máy dò đồ
                long currentTime = System.currentTimeMillis();
                // +++
                if (pl.itemTime.isUseBuaSanta) {
                    pl.itemTime.lastTimeBuaSanta += 1_800_000; // 30p
                } else {
                    pl.itemTime.lastTimeBuaSanta = currentTime + 1; // 0
                    pl.itemTime.isUseBuaSanta = true;
                }
                break;
            case 764:
                pl.itemTime.lastTimeKhauTrang = System.currentTimeMillis();
                pl.itemTime.isKhauTrang = true;
                break;
            case 381: // cuồng nộ
                if (pl.itemTime.isUseCuongNo2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            case 382: // bổ huyết
                if (pl.itemTime.isUseBoHuyet2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                Service.gI().point(pl);
                break;
            case 383: // bổ khí
                if (pl.itemTime.isUseBoKhi2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                Service.gI().point(pl);
                break;
            case 384: // giáp xên
                if (pl.itemTime.isUseGiapXen2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                Service.gI().point(pl);
                break;
            case 385: // ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case 379: // máy dò capsule
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
            case 1150: // cuồng nộ 2
                if (pl.itemTime.isUseCuongNo) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);
                break;

            case 1151: // bổ khí 2
                if (pl.itemTime.isUseBoKhi) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                Service.gI().point(pl);
                break;

            case 1152: // bổ huyết 2
                if (pl.itemTime.isUseBoHuyet) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                Service.gI().point(pl);
                break;

            case 1153: // giáp xên 2
                if (pl.itemTime.isUseGiapXen) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                Service.gI().point(pl);
                break;
            case 1154:// an danh
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;
            case 638: // Commeson
                pl.itemTime.lastTimeUseCMS = System.currentTimeMillis();
                pl.itemTime.isUseCMS = true;
                break;
            case 2160: // Nồi cơm điện
                pl.itemTime.lastTimeUseNCD = System.currentTimeMillis();
                pl.itemTime.isUseNCD = true;
                break;
            case 579:
            case 1045: // Đuôi khỉ
                pl.itemTime.lastTimeUseDK = System.currentTimeMillis();
                pl.itemTime.isUseDK = true;
                break;
            case 663: // bánh pudding
            case 664: // xúc xíc
            case 665: // kem dâu
            case 666: // mì ly
            case 667: // sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            case 880:
            case 881:
            case 882:
                pl.itemTime.lastTimeEatMeal2 = System.currentTimeMillis();
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
                break;
            case 1109: // máy dò đồ
                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
                pl.itemTime.isUseMayDo2 = true;
                break;
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId >= ShenronEventService.NGOC_RONG_1_SAO && tempId <= ShenronEventService.NGOC_RONG_7_SAO) {
            ShenronEventService.gI().openMenuSummonShenron(pl, 0);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {// Hoc skill moi
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else { // neu chua hoc ma hoc lv cao
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl,
                                    "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            pl.BoughtSkill.add((int) item.template.id);
                            // System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                    + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBag(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void learnSkillNew2(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                byte level = SkillUtil.getLevelSkillByItemID(item.template.id);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill == null) {
                    SkillService.gI().learSkillSpecial(pl,
                            (byte) SkillUtil.getSkillByItemID(pl, item.template.id).skillId);
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    return;
                } else {
                    if (curSkill.point == 7) {
                        Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                    } else {
                        if (curSkill.point == 0) {
                            if (level == 1) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 23);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                                if (curSkill.template.id == Skill.SUPER_NAMEC
                                        || curSkill.template.id == Skill.SUPER_SAIYAN
                                        || curSkill.template.id == Skill.SUPER_TRAI_DAT) {
                                    curSkill = SkillUtil.createSkill(Skill.GONG, level);
                                    SkillUtil.setSkill(pl, curSkill);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    msg = Service.gI().messageSubCommand((byte) 23);
                                    msg.writer().writeShort(curSkill.skillId);
                                    pl.sendMessage(msg);
                                    msg.cleanup();
                                }
                            } else {
                                Skill skillNeed = SkillUtil
                                        .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp "
                                        + skillNeed.point + " trước!");
                            }
                        } else {
                            if (curSkill.point + 1 == level) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                // System.out.println(curSkill.template.name + " - " + curSkill.point);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                        + (curSkill.point + 1) + " trước!");
                            }
                        }
                        InventoryService.gI().sendItemBag(pl);
                    }
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {

        }
    }

    private void learnSkillSuperNew(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                byte level = SkillUtil.getLevelSkillByItemID(item.template.id);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 6) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            SkillService.gI().learSkillSpecial(pl, (byte) 30);
                        } else {
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            if (level > 1) {
                                Item itemNew = ItemService.gI().createNewItem((short) (item.template.id - 1));
                                String name = itemNew.template.name;
                                String desiredName = name.substring(5);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + desiredName + " trước!");
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " trước!");
                            }
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            if (level > 1) {
                                Item itemNew = ItemService.gI().createNewItem((short) (item.template.id - 1));
                                String name = itemNew.template.name;
                                String desiredName = name.substring(5);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + desiredName + " trước!");
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " trước!");
                            }
                        }
                    }
                    InventoryService.gI().sendItemBag(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {

        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        // Kiểm tra số lượng người trong khu

        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 10000)) {
            return;
        }
        player.lastTimeEatPea = System.currentTimeMillis();
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc > player.nPoint.hpMax ? player.nPoint.hpMax : player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc > player.nPoint.mpMax ? player.nPoint.mpMax : player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc
                        > player.pet.nPoint.hpMax ? player.pet.nPoint.hpMax : player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc
                        > player.pet.nPoint.mpMax ? player.pet.nPoint.mpMax : player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cám ơn sư phụ");
            }

            InventoryService.gI().subQuantityItemsBag(player, pea, 1);
            InventoryService.gI().sendItemBag(player);
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: // skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: // skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: // skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: // skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void ItemManhGiay(Player pl, Item item) {
        if (pl.winSTT && !Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            Service.gI().sendThongBao(pl, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (pl.winSTT && Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            pl.winSTT = false;
            pl.callBossPocolo = false;
            pl.zoneSieuThanhThuy = null;
        }
        NpcService.gI().createMenuConMeo(pl, item.template.id, 564,
                "Đây chính là dấu hiệu riêng của...\nĐại Ma Vương Pôcôlô\nĐó là một tên quỷ dữ đội lốt người, một kẻ đại gian ác\ncó sức mạnh vô địch và lòng tham không đáy...\nĐối phó với hắn không phải dễ\nCon có chắc chắn muốn tìm hắn không?",
                "Đồng ý", "Từ chối");
    }

    private void ItemSieuThanThuy(Player pl, Item item) {
        long tnsm = 5_000_000;
        int n = 0;
        switch (item.template.id) {
            case 727:
                n = 2;
                break;
            case 728:
                n = 10;
                break;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
        if (Util.isTrue(50, 100)) {
            Service.gI().sendThongBao(pl, "Bạn đã bị chết vì độc của thuốc tăng lực siêu thần thủy.");
            pl.setDie();
        } else {
            for (int i = 0; i < n; i++) {
                Service.gI().addSMTN(pl, (byte) 2, tnsm, true);
            }
        }
    }

    public void UseCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id)
                .findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }
        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                    .filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }
            Card cardRequire = pl.Cards.stream().filter(r -> r.Id == radarRequireTemplate.Id).findFirst().orElse(null);
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + radarRequireTemplate.Name + " ở cấp độ "
                        + radarTemplate.RequireLevel + " mới có thể sử dụng thẻ này");
                return;
            }
        }
        Card card = pl.Cards.stream().filter(r -> r.Id == item.template.id).findFirst().orElse(null);
        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            if (pl.Cards.add(newCard)) {
                RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
                RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBag(pl);
            }
        } else {
            if (card.Level >= 2) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
        }
    }

}
