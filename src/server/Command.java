package server;

import boss.BabyManager;
import boss.BossID;
/**
 * @author HM
 */
import boss.BossManager;
import boss.BrolyManager;
import boss.GasDestroyManager;
import boss.OtherBossManager;
import boss.RedRibbonHQManager;
import boss.SnakeWayManager;
import boss.ThoDauBacManager;
import boss.TreasureUnderSeaManager;
import boss.TrungThuEventManager;
import consts.ConstNpc;
import item.Item;

import java.util.List;

import minigame.LuckyNumber.LuckyNumber;
import models.GiftCode.GiftCodeManager;
import models.ShenronEvent.ShenronEvent;
import models.ShenronEvent.ShenronEventManager;
import network.SessionManager;
import player.Pet;
import player.Player;
import player.badges.BadgesData;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import skill.Skill;
import utils.SystemMetrics;

public class Command {

    private static Command instance;

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        try {
            if (player.isAdmin()) {
                if (text.equals("gt")) {
                    GiftCodeManager.gI().checkInfomationGiftCode(player);
                    return true;
                } else if (text.equals("a")) {
                    BossManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("b")) {
                    BrolyManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("tdk")) {
                    ThoDauBacManager.gI().showListBoss(player);
                    return true;
                }  else if (text.equals("callsuper")) {
                    BossManager.gI().createBoss(BossID.SUPER_BROLY);
                    return true;
                }else if (text.equals("baby")) {
                    BabyManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("mapboss2")) {
                    OtherBossManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("mapdt")) {
                    RedRibbonHQManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("mapbdkb")) {
                    TreasureUnderSeaManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("mapcdrd")) {
                    SnakeWayManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("mapkghd")) {
                    GasDestroyManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("maptrungthu")) {
                    TrungThuEventManager.gI().showListBoss(player);
                    return true;
                } else if (text.equals("r")) {
                    Service.gI().releaseCooldownSkill(player);
                    return true;
                } else if (text.startsWith("sp")) {
                    try {
                        long power = Long.parseLong(text.replaceAll("up", ""));
                        Service.gI().addSMTN(player, (byte) 2, power, false);
                        return true;
                    } catch (Exception e) {
                    }
                } else if (text.equals("battu")) {
                    if (player.isBattu) {
                        player.isBattu = false;
                    } else {
                        player.isBattu = true;
                    }
                    Service.gI().sendThongBao(player, "Bất tử" + (player.isBattu ? ": ON" : ": OFF"));
                    return true;
                } else if (text.startsWith("dt")) {
                    try {
                        long power = Long.parseLong(text.replaceAll("upp", ""));
                        Service.gI().addSMTN(player.pet, (byte) 2, power, false);
                        return true;
                    } catch (Exception e) {
                    }
                } else if (text.equals("test")) {
                    switch (player.gender) {
                        case 0 ->
                            SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME, 1);
                        case 2 ->
                            SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG, 1);
                        default ->
                            SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA, 1);
                    }
                    return true;
                } else if (text.equals("dragon")) {
                    ShenronEvent shenron = new ShenronEvent();
                    shenron.setPlayer(player);
                    ShenronEventManager.gI().add(shenron);
                    player.shenronEvent = shenron;
                    shenron.setZone(player.zone);
                    shenron.activeShenron(true, ShenronEvent.DRAGON_EVENT);
                    shenron.sendWhishesShenron();
                    return true;
                } else if (text.equals("ad")) {
                    NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                            "|7|Time start: " + ServerManager.timeStart + "\n|1|Clients: "
                            + Client.gI().getPlayers().size() + " người chơi\n|2|Sessions: "
                            + SessionManager.gI().getNumSession() + "\n|3|Threads: "
                            + Thread.activeCount() + " luồng" + "\n" + SystemMetrics.ToString(),
                            "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "VND + Tổng nạp",
                            "Buff_Item", "Hộp Thư", "Đóng");
                    return true;
                } else if (text.equals("daucatmoi")) {
                    for (int i = 0; i < 10; i++) {
                        ServerNotify.gI().notify("BOSS Nro vừa xuất hiện tại nhà anh ấy");
                    }
                    return true;
                } else if (text.startsWith("m")) {
                    int mapId = Integer.parseInt(text.replace("m", ""));
                    ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
                    return true;
                }
                if (text.startsWith("dmg")) {
                    try {
                        int dameg = Integer.parseInt(text.replaceAll("dmg", ""));
                        player.nPoint.dameg = dameg;
                        Service.gI().point(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("hpg")) {
                    try {
                        int hpg = Integer.parseInt(text.replaceAll("hpg", ""));
                        player.nPoint.hpg = hpg;
                        Service.gI().point(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("kig")) {
                    try {
                        int mpg = Integer.parseInt(text.replaceAll("kig", ""));
                        player.nPoint.mpg = mpg;
                        Service.gI().point(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("defg")) {
                    try {
                        int defg = Integer.parseInt(text.replaceAll("defg", ""));
                        player.nPoint.defg = defg;
                        Service.gI().point(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("crg")) {
                    try {
                        int critg = Integer.parseInt(text.replaceAll("crg", ""));
                        player.nPoint.critg = critg;
                        Service.gI().point(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("ntask")) {
                    try {
                        int idTask = Integer.parseInt(text.replaceAll("ntask", ""));
                        player.playerTask.taskMain.id = idTask - 1;
                        player.playerTask.taskMain.index = 0;
                        TaskService.gI().sendNextTaskMain(player);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (text.startsWith("badges_")) {
                    int idBadges = Integer.parseInt(text.replaceAll("badges_", ""));
                    player.badges.idBadges = idBadges;
                }
                if (text.startsWith("kq")) {
                    Service.gI().sendThongBao(player, "Kết quả Lucky Round tiếp theo là: " + LuckyNumber.RESULT);
                    return true;
                }
                if (text.startsWith("danhhieu_")) {
                    int idGender = Integer.parseInt(text.replaceAll("danhhieu_", ""));
                    BadgesData data = new BadgesData(player, idGender, 5);
                    return true;
                }
                if (text.startsWith("gender_")) {
                    byte idGender = Byte.parseByte(text.replaceAll("gender_", ""));
                    player.gender = idGender;
                    return true;
                }
                if (text.startsWith("i")) {
                    try {
                        String[] txt = text.replace("i", "").split(" ");
                        if (Short.parseShort(txt[0]) <= 2046) {
                            Item it = ItemService.gI().createNewItem((short) Short.parseShort(txt[0]));
                            if (it != null && txt.length == 1) {
                                InventoryService.gI().addItemBag(player, it);
                                InventoryService.gI().sendItemBag(player);
                                Service.gI().sendThongBao(player, "Đã nhận được " + it.template.name);
                            } else if (it != null && txt.length == 2 && Client.gI().getPlayer(String.valueOf(txt[1])) == null) {
                                it.quantity = Integer.parseInt(txt[1]);
                                InventoryService.gI().addItemBag(player, it);
                                InventoryService.gI().sendItemBag(player);
                                Service.gI().sendThongBao(player, "Đã nhận được x" + Integer.valueOf(txt[1]) + " " + it.template.name);
                            } else if (it != null && txt.length == 2 && Client.gI().getPlayer(String.valueOf(txt[1])) != null) {
                                String name = String.valueOf(txt[1]);
                                InventoryService.gI().addItemBag(Client.gI().getPlayer(name), it);
                                InventoryService.gI().sendItemBag(Client.gI().getPlayer(name));
                                Service.gI().sendThongBao(player, "Đã buff " + it.template.name + " đến player " + name);
                                Service.gI().sendThongBao(Client.gI().getPlayer(name), "Đã nhận được " + it.template.name);
                            } else if (it != null && txt.length == 3 && Client.gI().getPlayer(String.valueOf(txt[2])) != null) {
                                String name = String.valueOf(txt[2]);
                                it.quantity = Integer.parseInt(txt[1]);
                                InventoryService.gI().addItemBag(Client.gI().getPlayer(name), it);
                                InventoryService.gI().sendItemBag(Client.gI().getPlayer(name));
                                Service.gI().sendThongBao(player, "Đã buff x" + Integer.valueOf(txt[1]) + " " + it.template.name + " đến player " + name);
                                Service.gI().sendThongBao(Client.gI().getPlayer(name), "Đã nhận được x" + Integer.valueOf(txt[1]) + " " + it.template.name);
                            } else {
                                Service.gI().sendThongBao(player, "Không tìm thấy player");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không tìm thấy item");
                        }
                    } catch (Exception e) {
                        Service.gI().sendThongBao(player, "Nhập sai cú pháp !");
                    }
                } else if (text.equals("item")) {
                    Input.gI().createFormGiveItem(player);
                    return true;
                } else if (text.equals("getitem")) {
                    Input.gI().createFormGetItem(player);
                    return true;
                } else if (text.equals("d")) {
                    Service.gI().setPos(player, player.location.x, player.location.y + 10);
                    return true;
                }
            }
            if (text.startsWith("ten con la ")) {
                PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
            }

            if (player.pet != null) {
                switch (text) {
                    case "di theo", "follow" ->
                        player.pet.changeStatus(Pet.FOLLOW);
                    case "bao ve", "protect" ->
                        player.pet.changeStatus(Pet.PROTECT);
                    case "tan cong", "attack" ->
                        player.pet.changeStatus(Pet.ATTACK);
                    case "ve nha", "go home" ->
                        player.pet.changeStatus(Pet.GOHOME);
                    case "bien hinh" ->
                        player.pet.transform();
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
