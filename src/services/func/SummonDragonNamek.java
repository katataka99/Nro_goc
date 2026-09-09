package services.func;

/*
 *
 *
 * @author HM
 */
import network.Message;
import consts.ConstNpc;
import jdbc.daos.SQLHandle;
import jdbc.daos.PlayerDAO;
import java.util.List;
import java.util.function.Consumer;
import map.Zone;
import player.Player;
import server.Client;
import services.NpcService;
import services.Service;
import utils.Util;

public class SummonDragonNamek {

    public static final byte DRAGON_PORUNGA = 1;
    private static SummonDragonNamek instance;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    private boolean isShenronAppear;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private int menuShenron;
    private byte select;
    private final Thread update;
    private boolean active;
    public boolean isPlayerDisconnect;
    private long lastTimeShenronWait;
    private final int timeShenronWait = 300000;

    public static SummonDragonNamek gI() {
        if (instance == null) {
            instance = new SummonDragonNamek();
        }
        return instance;
    }

    private SummonDragonNamek() {
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {
                            List<Player> players = mapShenronAppear.getPlayers();
                            for (Player plMap : players) {
                                if (plMap.isPl() && plMap.id == playerSummonShenronId) {
                                    playerSummonShenron = plMap;
                                    reSummonShenron();
                                    isPlayerDisconnect = false;
                                    break;
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public void summonNamec(Player pl) {
        if (pl.zone.map.mapId == 7) {
            playerSummonShenron = pl;
            playerSummonShenronId = (int) pl.id;
            mapShenronAppear = pl.zone;
            lastTimeShenronWait = System.currentTimeMillis();
            sendNotifyShenronNamekAppear();
            activeShenron(pl, true, DRAGON_PORUNGA);
            sendWhishesNamec(pl);
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, DRAGON_PORUNGA);
        sendWhishesNamec(playerSummonShenron);
    }

    private void activeShenron(Player pl, boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("ducvupro");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(type);
                isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private void sendNotifyShenronNamekAppear() {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần namek tại "
                    + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
            Service.gI().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void giveCharm(Player player, String charmName, int charmType) {
        long currentTime = System.currentTimeMillis();
        long timeToAdd = 7L * 24 * 60 * 60 * 1000; // 7 ngày

        Consumer<Player> applyCharm = p -> {
            switch (charmType) {
                case 0:
                    p.charms.tdTriTue = updateCharmTime(p.charms.tdTriTue, currentTime, timeToAdd);
                    break;
                case 1:
                    p.charms.tdDaTrau = updateCharmTime(p.charms.tdDaTrau, currentTime, timeToAdd);
                    break;
                case 2:
                    p.charms.tdManhMe = updateCharmTime(p.charms.tdManhMe, currentTime, timeToAdd);
                    break;
                case 3:
                    p.charms.tdThuHut = updateCharmTime(p.charms.tdThuHut, currentTime, timeToAdd);
                    break;
                case 4:
                    p.charms.tdDeoDai = updateCharmTime(p.charms.tdDeoDai, currentTime, timeToAdd);
                    break;
                case 5:
                    p.charms.tdOaiHung = updateCharmTime(p.charms.tdOaiHung, currentTime, timeToAdd);
                    break;
                case 6:
                    p.charms.tdBatTu = updateCharmTime(p.charms.tdBatTu, currentTime, timeToAdd);
                    break;
            }
            Service.gI().sendThongBao(p, "Bạn đã nhận được " + charmName + " 7 ngày");
        };

        if (player.clan != null) {
            player.clan.members.forEach(m -> {
                Player p = Client.gI().getPlayer(m.id);
                if (p == null) {
                    p = SQLHandle.loadById(m.id);
                }
                if (p != null) {
                    applyCharm.accept(p);
                    if (p.session == null) {
                        PlayerDAO.updatePlayer(p);
                    }
                }
            });
        } else {
            applyCharm.accept(player);
        }
    }

    private long updateCharmTime(long current, long now, long add) {
        return current < now ? now + add : current + add;
    }

    public void confirmWish() {
        switch (this.menuShenron) {
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                try {
                    switch (select) {
                        case 0: // Bùa Trí Tuệ
                            giveCharm(playerSummonShenron, "Bùa Trí Tuệ", 0);
                            break;
                        case 1: // Bùa Da Trâu
                            giveCharm(playerSummonShenron, "Bùa Da Trâu", 1);
                            break;
                        case 2: // Bùa Mạnh Mẽ
                            giveCharm(playerSummonShenron, "Bùa Mạnh Mẽ", 2);
                            break;
                        case 3: // Bùa Thu Hút
                            giveCharm(playerSummonShenron, "Bùa Thu Hút", 3);
                            break;
                        case 4: // Bùa Dẻo Dai
                            giveCharm(playerSummonShenron, "Bùa Dẻo Dai", 4);
                            break;
                        case 5: // Bùa Oai Hùng
                            giveCharm(playerSummonShenron, "Bùa Oai Hùng", 5);
                            break;
                        case 6: // Bùa bất tử
                            giveCharm(playerSummonShenron, "Bùa Bất Tử", 6);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM -> {
                switch (select) {
                    case 0 ->
                        wish = "7 ngày bùa trí tuệ";
                    case 1 ->
                        wish = "7 ngày bùa da trâu";
                    case 2 ->
                        wish = "7 ngày bùa mạnh mẽ";
                    case 3 ->
                        wish = "7 ngày bùa thu hút";
                    case 4 ->
                        wish = "7 ngày bùa dẻo dai";
                    case 5 ->
                        wish = "7 ngày bùa oai hùng";
                    case 6 ->
                        wish = "7 ngày bùa bất tử";
                }
            }
        }
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_NAMEK_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void sendWhishesNamec(Player pl) {
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM, "Ta sẽ ban cho cả bang hội ngươi 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định", "Bùa trí tuệ", "Bùa da trâu", "Bùa mạnh mẽ", "Bùa thu hút", "Bùa dẻo dai", "Bùa oai hùng", "Bùa bất tử");
    }

    public void shenronLeave(Player pl, byte type) {
        if (type == WISHED) {
            //Điều ước Bùa mạnh mẽ cho tất cả trong 7 ngày của các con đã được thực hiện...tạm biệt
            NpcService.gI().createTutorial(pl, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
        } else {
            NpcService.gI().createMenuRongThieng(pl, ConstNpc.IGNORE_MENU, "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
        }
        activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
        this.isShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.mapShenronAppear = null;
    }
}
