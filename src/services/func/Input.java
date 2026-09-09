package services.func;

/*
 *
 *
 * @author HM
 */
import clan.Clan;
import clan.ClanMember;
import jdbc.DBConnecter;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import java.sql.PreparedStatement;
import map.Zone;
import minigame.cost.LuckyNumberCost;
import minigame.LuckyNumber.LuckyNumberService;
import npc.Npc;
import npc.NpcManager;
import player.Player;
import network.Message;
import network.SessionManager;
import network.inetwork.ISession;
import server.Client;
import services.Service;
import models.GiftCode.GiftCodeService;
import services.InventoryService;
import services.ItemService;
import services.NpcService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdbc.daos.SQLHandle;
import jdbc.daos.PlayerDAO;

import player.Inventory;
import server.Manager;
import server.ServerManager;
import services.ClanService;
import services.PlayerService;
import utils.Logger;
import utils.SystemMetrics;
import utils.Util;

public class Input {

    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 504;
    public static final int NAP_THE = 505;
    public static final int CHANGE_NAME_BY_ITEM = 506;
    public static final int GIVE_IT = 507;
    public static final int GET_IT = 508;
    public static final int DANGKY = 509;
    public static final int CHOOSE_LEVEL_KGHD = 510;
    public static final int CHOOSE_LEVEL_CDRD = 511;
    public static final int DISSOLUTION_CLAN = 513;
    public static final int SELECT_LUCKYNUMBER = 514;
    public static final int SEND_ITEM_OP_VIP = 515;
    public static final int NAP_COIN = 516;
    public static final int SEND_ITEM = 517;
    public static final int PASS_ADMIN = 518;
    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;
    public static final byte MBV = 23;
    public static final byte BANSLL = 24;
    public static final byte BANGHOI = 25;

    public static final byte NAP_NGOC_XANH = 26;
    public static final byte RUT_XU = 27;

    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.iDMark.getTypeInput()) {
                case PASS_ADMIN:
                    if (player.isAdmin()) {
                        if (text == null || text.length == 0 || text[0] == null) {
                            Service.gI().sendThongBao(player, "Vui lòng nhập mật khẩu");
                            break;
                        }
                        String matKhauNhap = text[0].trim();
                        String matKhauCuaToi = "1211";
                        if (matKhauNhap.equals(matKhauCuaToi)) {
                            Logger.warning("Player " + player.name + " vừa truy cập vào lệnh admin\n");
                            NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                                    "|7|Time start: " + ServerManager.timeStart + "\n|1|Clients: "
                                    + Client.gI().getPlayers().size() + " người chơi\n|2|Sessions: "
                                    + SessionManager.gI().getNumSession() + "\n|3|Threads: "
                                    + Thread.activeCount() + " luồng" + "\n" + SystemMetrics.ToString(),
                                    "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "VND + Tổng nạp",
                                    "Buff_Item", "Hộp Thư", "Đóng");
                        } else {
                            int maxWrongAttempts = 3;
                            int wrongAttempts = player.getWrongPasswordAttempts();

                            if (wrongAttempts >= maxWrongAttempts) {
                                PlayerService.gI().banPlayer(player);
                                Logger.warning("Player " + player.name + " bị ban vì nhập sai mật khẩu admin quá nhiều lần\n");
                            } else {
                                player.increaseWrongPasswordAttempts();
                                Service.gI().sendThongBao(player, "Mật khẩu sai. Bạn còn " + (maxWrongAttempts - wrongAttempts) + " lần thử.\n");
                                Logger.warning("Player " + player.name + " nhập sai mật khẩu admin (" + wrongAttempts + "/" + maxWrongAttempts + ")\n");
                            }
                        }
                    } else {
                        PlayerService.gI().banPlayer(player);
                        Logger.warning("Player " + player.name + " có dấu hiệu bug admin, tiến hành ban\n");
                    }
                    break;
                case SEND_ITEM: {
                    String itemIds = text[1];
                    String option = text[2];
                    int slItemBuff = Integer.parseInt(text[3]);
                    if (slItemBuff > 999) {
                        Service.gI().sendThongBaoOK(player, "Buff vượt số lượng giới hạn vui lòng để tối đa sl 999");
                        return;
                    }
                    String plName = text[0].trim();
                    if (plName.equals("all")) {
                        new Thread(() -> {
                            List<Player> allPlayer = SQLHandle.getAllPlayer();
                            for (Player pBuffItem : allPlayer) {
                                if (pBuffItem != null) {
                                    String[] itemIdsArray = itemIds.split(",");
                                    for (String itemId : itemIdsArray) {
                                        int idItemBuff = Integer.parseInt(itemId);
                                        Item itembuff = ItemService.gI().createNewItem((short) idItemBuff, slItemBuff);

                                        if (option != null) {
                                            String[] Option = option.split(",");
                                            if (Option.length > 0) {
                                                for (int i = 0; i < Option.length; i++) {
                                                    String[] optItem = Option[i].split("-");
                                                    int optID = Integer.parseInt(optItem[0]);
                                                    int param = Integer.parseInt(optItem[1]);
                                                    itembuff.itemOptions.add(new ItemOption(optID, param));
                                                }
                                            }
                                        }
                                        pBuffItem.inventory.itemsMailBox.add(itembuff);

                                        if (SQLHandle.updateMailBox(pBuffItem)) {
                                            Service.gI().sendThongBao(player, "Bạn vừa gửi " + itembuff.template.name
                                                    + " thành công cho " + pBuffItem.name);
                                        }
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Player không tồn tại");
                                }
                            }
                        }).start();
                    } else {
                        Player pBuffItem = SQLHandle.loadPlayerByName(text[0].trim());
                        if (pBuffItem != null) {
                            String[] itemIdsArray = itemIds.split(",");
                            for (String itemId : itemIdsArray) {
                                int idItemBuff = Integer.parseInt(itemId);
                                Item itembuff = ItemService.gI().createNewItem((short) idItemBuff, slItemBuff);
                                if (option != null) {
                                    String[] Option = option.split(",");
                                    if (Option.length > 0) {
                                        for (int i = 0; i < Option.length; i++) {
                                            String[] optItem = Option[i].split("-");
                                            int optID = Integer.parseInt(optItem[0]);
                                            int param = Integer.parseInt(optItem[1]);
                                            itembuff.itemOptions.add(new ItemOption(optID, param));
                                        }
                                    }
                                }
                                pBuffItem.inventory.itemsMailBox.add(itembuff);
                                if (SQLHandle.updateMailBox(pBuffItem)) {
                                    Service.gI().sendThongBao(player, "Bạn vừa gửi " + itembuff.template.name
                                            + " thành công cho " + pBuffItem.name);
                                }
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Player không tồn tại");
                        }
                    }
                    break;
                }
                case SEND_ITEM_OP_VIP:
                    if (player.isAdmin()) {
                        Player pBuffItem = Client.gI().getPlayer(text[0]);
                        int idItemBuff = Integer.parseInt(text[1]);
                        String idOptionBuff = text[2].trim();

                        int slItemBuff = Integer.parseInt(text[3]);

                        try {
                            if (pBuffItem != null) {
                                String txtBuff = "Buff to player: " + pBuffItem.name + "\b";

                                Item itemBuffTemplate = ItemService.gI().createNewItem((short) idItemBuff, slItemBuff);
                                if (!idOptionBuff.isEmpty()) {
                                    String arr[] = idOptionBuff.split("v");
                                    for (int i = 0; i < arr.length; i++) {
                                        String arr2[] = arr[i].split("-");
                                        int idoption = Integer.parseInt(arr2[0].trim());
                                        int param = Integer.parseInt(arr2[1].trim());
                                        itemBuffTemplate.itemOptions.add(new Item.ItemOption(idoption, param));
                                    }

                                }
                                txtBuff += "x" + slItemBuff + " " + itemBuffTemplate.template.name + "\b";
                                InventoryService.gI().addItemBag(pBuffItem, itemBuffTemplate);
                                InventoryService.gI().sendItemBag(pBuffItem);
                                NpcService.gI().createTutorial(player, 24, txtBuff);
                                if (player.id != pBuffItem.id) {
                                    NpcService.gI().createTutorial(pBuffItem, 24, txtBuff);
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Player không online");
                            }
                        } catch (Exception e) {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra vui lòng thử lại");
                        }

                    }
                    break;
                case NAP_COIN: {
                    String name = text[0];
                    long vnd = Long.valueOf(text[1]);
                    Player plne = Client.gI().getPlayer(name);
                    if (plne != null) {
                        plne.getSession().cash += vnd;

                        // Cộng điểm coupon
                        int couponPoint = (int) (vnd / 1000);
                        plne.inventory.coupon += couponPoint;
                        PreparedStatement ps;
                        try (java.sql.Connection con = DBConnecter.getConnectionServer();) {
                            ps = con.prepareStatement(
                                    "UPDATE account SET cash = (cash + ?), danap = (danap + ?) WHERE id = ?");
                            ps.setInt(1, (int) vnd);
                            ps.setInt(2, (int) vnd);
                            ps.setInt(3, plne.getSession().userId);
                            ps.executeUpdate();
                            ps.close();
                        } catch (Exception e) {
                            Logger.logException(PlayerDAO.class, e, "Lỗi update coin " + plne.name);
                        }

                        Service.gI().sendThongBao(player, "Đã nạp " + vnd + " VND cho " + plne.name
                                + (couponPoint > 0 ? ("\nNhận được " + couponPoint + " điểm coupon") : ""));
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không online");
                    }
                    break;
                }
                case NAP_NGOC_XANH: {
                    long cash = Long.valueOf(text[0]);
                    int ngocxanh = 0;
                    Item xuvang = InventoryService.gI().findItemBag(player, 1118);
                    if (cash < 1) {
                        Service.gI().sendThongBao(player, "Không thể quy đổi dưới 1 xu");
                        return;
                    }
                    if (xuvang.quantity < cash) {
                        Service.gI().sendThongBao(player, "Bạn không đủ xu vàng");
                        return;
                    }
                    ngocxanh += cash * 50;
                    player.inventory.gem += ngocxanh;
                    InventoryService.gI().subQuantityItemsBag(player, xuvang, (int) cash);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Đã quy đổi thành công " + ngocxanh + " ngọc xanh");
                    break;
                }
                case GIVE_IT:
                    String name = text[0];
                    int id = Integer.parseInt(text[1]);
                    int op = Integer.parseInt(text[2]);
                    int pr = Integer.parseInt(text[3]);
                    int q = Integer.parseInt(text[4]);

                    if (Client.gI().getPlayer(name) != null) {
                        Item item = ItemService.gI().createNewItem(((short) id));
                        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                        if (!ops.isEmpty()) {
                            item.itemOptions = ops;
                        }
                        item.quantity = q;
                        item.itemOptions.add(new Item.ItemOption(op, pr));
                        InventoryService.gI().addItemBag(Client.gI().getPlayer(name), item);
                        InventoryService.gI().sendItemBag(Client.gI().getPlayer(name));
                        Service.gI().sendThongBao(Client.gI().getPlayer(name),
                                "Nhận " + item.template.name + " từ " + player.name);

                    } else {
                        Service.gI().sendThongBao(player, "Không online");
                    }
                    break;
                case GET_IT:
                    id = Integer.parseInt(text[0]);
                    op = Integer.parseInt(text[1]);
                    pr = Integer.parseInt(text[2]);
                    q = Integer.parseInt(text[3]);

                    if (player.isAdmin()) {
                        Item item = ItemService.gI().createNewItem(((short) id));
                        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                        if (!ops.isEmpty()) {
                            item.itemOptions = ops;
                        }
                        item.quantity = q;
                        item.itemOptions.add(new Item.ItemOption(op, pr));
                        InventoryService.gI().addItemBag(player, item);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player, "Nhận " + item.template.name + " !");

                    } else {
                        Service.gI().sendThongBao(player, "Không đủ quyền hạn!");
                    }
                    break;
                case CHANGE_PASSWORD:
                    Service.gI().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    GiftCodeService.gI().giftCode(player, text[0]);
                    break;
                case FIND_PLAYER:
                    Player pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[]{"Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban",
                                    "Kick"},
                                pl);
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                case CHANGE_NAME: {
                    Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                    if (plChanged != null) {
                        if (DBConnecter.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        } else {
                            plChanged.name = text[0];
                            DBConnecter.executeUpdate("update player set name = ? where id = ?", plChanged.name,
                                    plChanged.id);
                            Service.gI().player(plChanged);
                            Service.gI().Send_Caitrang(plChanged);
                            Service.gI().sendFlagBag(plChanged);
                            Zone zone = plChanged.zone;
                            ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x,
                                    plChanged.location.y);
                            Service.gI().sendThongBao(plChanged,
                                    "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            Service.gI().sendThongBao(player, "Đổi tên người chơi thành công");
                        }
                    }
                }
                break;
                case CHANGE_NAME_BY_ITEM: {
                    if (player != null) {
                        if (DBConnecter.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                            createFormChangeNameByItem(player);
                        } else if (Util.haveSpecialCharacter(text[0])) {
                            Service.gI().sendThongBaoOK(player, "Tên nhân vật không được chứa ký tự đặc biệt");
                        } else if (text[0].length() < 5) {
                            Service.gI().sendThongBaoOK(player, "Tên nhân vật quá ngắn");
                        } else if (text[0].length() > 10) {
                            Service.gI().sendThongBaoOK(player,
                                    "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ 5 đến 10 ký tự");
                        } else {
                            Item theDoiTen = InventoryService.gI().findItem(player.inventory.itemsBag, 2006);
                            if (theDoiTen == null) {
                                Service.gI().sendThongBao(player, "Không tìm thấy thẻ đổi tên");
                            } else {
                                InventoryService.gI().subQuantityItemsBag(player, theDoiTen, 1);
                                player.name = text[0].toLowerCase();
                                DBConnecter.executeUpdate("update player set name = ? where id = ?", player.name,
                                        player.id);
                                Service.gI().player(player);
                                Service.gI().Send_Caitrang(player);
                                Service.gI().sendFlagBag(player);
                                Zone zone = player.zone;
                                ChangeMapService.gI().changeMap(player, zone, player.location.x, player.location.y);
                                Service.gI().sendThongBao(player,
                                        "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            }
                        }
                    }
                }
                break;
                case CHOOSE_LEVEL_BDKB:
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_BDKB,
                                    "Con có chắc muốn đến\nhang kho báu cấp độ " + level + " ?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }

                    break;
                case CHOOSE_LEVEL_KGHD:
                    level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.MR_POPO, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, 2,
                                    "Cậu có chắc muốn đến\nDestron Gas cấp độ " + level + " ?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    }
                    break;
                case CHOOSE_LEVEL_CDRD:
                    level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.THAN_VU_TRU, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, 3,
                                    "Con có chắc muốn đến\ncon đường rắn độc cấp độ " + level + " ?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    }
                    break;
                case MBV:
                    int mbv = Integer.parseInt(text[0]);
                    int nmbv = Integer.parseInt(text[1]);
                    int rembv = Integer.parseInt(text[2]);
                    if ((mbv + "").length() != 6 || (nmbv + "").length() != 6 || (rembv + "").length() != 6) {
                        Service.gI().sendThongBao(player, "Trêu bố mày à?");
                    } else if (player.mbv == 0) {
                        Service.gI().sendThongBao(player, "Bạn chưa cài mã bảo vệ!");
                    } else if (player.mbv != mbv) {
                        Service.gI().sendThongBao(player, "Mã bảo vệ không đúng");
                    } else if (nmbv != rembv) {
                        Service.gI().sendThongBao(player, "Mã bảo vệ không trùng khớp");
                    } else {
                        player.mbv = nmbv;
                        Service.gI().sendThongBao(player, "Đổi mã bảo vệ thành công!");
                    }
                    break;
                case BANSLL:
                    int sltv = Integer.parseInt(text[0]);
                    long cost = (long) sltv * 36900000;
                    if (sltv < 0) {
                        Service.gI().sendThongBao(player, "Có cái dái");
                        return;
                    }
                    Item ThoiVang = InventoryService.gI().findItemBag(player, 457);
                    if (ThoiVang != null) {
                        if (ThoiVang.quantity < sltv) {
                            Service.gI().sendThongBao(player, "Bạn chỉ có " + ThoiVang.quantity + " Thỏi vàng");
                        } else {
                            if (player.inventory.gold + cost > Inventory.LIMIT_GOLD) {
                                int slban = (int) ((Inventory.LIMIT_GOLD - player.inventory.gold) / 36900000);
                                if (slban < 1) {
                                    Service.gI().sendThongBao(player, "Vàng sau khi bán vượt quá giới hạn");
                                } else if (slban < 2) {
                                    Service.gI().sendThongBao(player, "Bạn chỉ có thể bán 1 Thỏi vàng");
                                } else {
                                    Service.gI().sendThongBao(player, "Số lượng trong khoảng 1 tới " + slban);
                                }
                            } else {
                                InventoryService.gI().subQuantityItemsBag(player, ThoiVang, sltv);
                                InventoryService.gI().sendItemBag(player);
                                player.inventory.gold += cost;
                                Service.gI().sendMoney(player);
                                Service.gI().sendThongBao(player,
                                        "Đã bán " + sltv + " Thỏi vàng thu được " + Util.numberToMoney(cost) + " vàng");
                                TransactionService.gI().cancelTrade(player);
                            }
                        }
                    }
                    break;
                case BANGHOI:
                    Clan clan = player.clan;
                    if (clan != null) {
                        ClanMember cm = clan.getClanMember((int) player.id);
                        if (clan.isLeader(player)) {
                            if (clan.canUpdateClan(player)) {
                                String tenvt = text[0];
                                if (!Util.haveSpecialCharacter(tenvt) && tenvt.length() > 1 && tenvt.length() < 5) {
                                    clan.name2 = tenvt;
                                    clan.update();
                                    Service.gI().sendThongBao(player, "[" + tenvt + "] OK");
                                } else {
                                    Service.gI().sendThongBaoOK(player,
                                            "Chỉ chấp nhận các ký tự a-z, 0-9 và chiều dài từ 2 đến 4 ký tự");
                                }
                            }
                        }
                    }
                    break;
                case DISSOLUTION_CLAN:
                    String xacNhan = text[0];
                    if (xacNhan.equalsIgnoreCase("OK")) {
                        clan = player.clan;
                        if (clan.isLeader(player)) {
                            clan.deleteDB(clan.id);
                            Manager.CLANS.remove(clan);
                            player.clan = null;
                            player.clanMember = null;
                            ClanService.gI().sendMyClan(player);
                            ClanService.gI().sendClanId(player);
                            Service.gI().sendThongBao(player, "Bang hội đã giải tán thành công.");
                        }
                    }
                    break;
                case SELECT_LUCKYNUMBER: {
                    int number = Integer.parseInt(text[0]);
                    LuckyNumberService.addNumber(player, number);
                }
                break;
            }
        } catch (Exception e) {
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.iDMark.setTypeInput(typeInput);
        Message msg = null;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void createForm(ISession session, int typeInput, String title, SubInput... subInputs) {
        Message msg = null;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void createFormSenditem2(Player pl) {
        createForm(pl, SEND_ITEM_OP_VIP, "BUFF VIP", new SubInput("Tên người chơi", ANY), new SubInput("Id Item", ANY),
                new SubInput("Chuỗi option vd : 50-20v30-1", ANY), new SubInput("Số lượng", ANY));
    }

    public void createFromMailBox(Player pl) {
        createForm(pl, SEND_ITEM, "Hộp thư gửi đến người chơi",
                new SubInput("Tên người chơi", ANY),
                new SubInput("ID Trang Bị", ANY),
                new SubInput("Chuỗi option", ANY),
                new SubInput("Số lượng", NUMERIC));
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi mật khẩu", new SubInput("Mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }

    public void createFormGiveItem(Player pl) {
        createForm(pl, GIVE_IT, "Tặng vật phẩm", new SubInput("Tên", ANY), new SubInput("Id Item", ANY),
                new SubInput("ID OPTION", ANY), new SubInput("PARAM", ANY), new SubInput("Số lượng", ANY));
    }

    public void createFormGetItem(Player pl) {
        createForm(pl, GET_IT, "Get vật phẩm", new SubInput("Id Item", ANY), new SubInput("ID OPTION", ANY),
                new SubInput("PARAM", ANY), new SubInput("Số lượng", ANY));
    }

    public void createFormGiftCode(Player pl) {
        createForm(pl, GIFT_CODE, "GiftCode", new SubInput("Giftcode", ANY));
    }

    public void createFormMBV(Player pl) {
        createForm(pl, MBV, "Đồ ngu! Đồ ăn hại! Cút mẹ mày đi!", new SubInput("Nhập Mã Bảo Vệ Đã Quên", NUMERIC),
                new SubInput("Nhập Mã Bảo Vệ Mới", NUMERIC), new SubInput("Nhập Lại Mã Bảo Vệ Mới", NUMERIC));
    }

    public void createFormBangHoi(Player pl) {
        createForm(pl, BANGHOI, "Nhập tên viết tắt bang hội", new SubInput("Tên viết tắt từ 2 đến 4 kí tự", ANY));
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }

    public void createFormNapThe(Player pl, byte loaiThe) {
        pl.iDMark.setLoaiThe(loaiThe);
        createForm(pl, NAP_THE, "Nạp thẻ", new SubInput("Mã thẻ", ANY), new SubInput("Seri", ANY));
    }

    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChangeNameByItem(Player pl) {
        createForm(pl, CHANGE_NAME_BY_ITEM, "Đổi tên " + pl.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Hãy chọn cấp độ hang kho báu từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelCDRD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_CDRD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelKGHD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_KGHD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormBanSLL(Player pl) {
        createForm(pl, BANSLL, "Bạn muốn bán bao nhiêu [Thỏi vàng] ?", new SubInput("Số lượng", NUMERIC));
    }

    public void createFormGiaiTanBangHoi(Player pl) {
        createForm(pl, DISSOLUTION_CLAN, "Nhập OK để xác nhận giải tán bang hội.", new SubInput("", ANY));
    }

    public void createFormPassAdmin(Player pl) {
        createForm(pl, PASS_ADMIN, "Nhập mật khẩu.", new SubInput("Mật Khẩu", ANY));
    }

    public void createFormNapCoin(Player pl) {
        createForm(pl, NAP_COIN, "Nạp coin", new SubInput("Tên nhân vật", ANY), new SubInput("Số Tiền", ANY));
    }

    public void createFormNapNgocXanh(Player pl) {
        Item xuvang = InventoryService.gI().findItemBag(pl, 1118);
        int soLuongXu = xuvang.quantity;
        createForm(pl, NAP_NGOC_XANH, "Số xu hiện có " + soLuongXu + " xu\n Quy đổi 1 xu = 50 ngọc", new SubInput("Nhập số xu cần đổi", ANY));
    }

    public void createFormSelectOneNumberLuckyNumber(Player pl, boolean isGem) {
        String text = "";
        if (isGem) {
            text = "Hãy chọn 1 số từ 0 đến 99 giá " + Util.numberFormatLouis(LuckyNumberCost.costPlayGem) + " ngọc";
        } else {
            text = "Hãy chọn 1 số từ 0 đến 99 giá " + Util.numberFormatLouis(LuckyNumberCost.costPlayGold) + " vàng";
        }
        createForm(pl, SELECT_LUCKYNUMBER, text, new SubInput("Số bạn chọn", NUMERIC));
    }

    public static class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }

}
