package mob;

/*
 *
 *
 * @author HM
 */
import TuanBinh.DropLimit;
import consts.ConstDailyGift;
import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;
import event.EventManager;
import item.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import map.Zone;
import models.Achievement.AchievementService;
import models.Training.TrainingService;
import network.Message;
import player.Location;
import player.Pet;
import player.Player;
import player.dailyGift.DailyGiftService;
import server.Maintenance;
import server.ServerNotify;
import services.InventoryService;
import services.ItemMapService;
import services.ItemService;
import services.MapService;
import services.Service;
import services.TaskService;
import skill.Skill;
import utils.TimeUtil;
import utils.Util;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp(this.point.getHpFull());
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {

            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI)
                        && damage > this.point.maxHp / 10) {
                    damage = this.point.maxHp / 10;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0
                    && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = Math.min((int) ((this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000) * (10.0 / 100)), damage);
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2)
                    && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            if (damage > 2_000_000_000) {
                damage = 2_000_000_000;
            }
            if (damage > this.point.hp) {
                damage = this.point.hp;
            }
            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                    if (plAtt.itemTime != null && plAtt.itemTime.isUseTDLT) {
                        plAtt.playerTask.kolTask.addCount();
                    }
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked((int) damage,
                        plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        int levelPlayer = Service.gI().getCurrLevel(pl);
        int n = levelPlayer - this.level;
        if (pl.zone != null && MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            n = 0;
        }
        if (pl.nPoint != null && pl.nPoint.power < 40_000_000_000L) {
            n = 0;
        }
        long pDameHit = dame * 100 / point.getHpFull();
        long tiemNang = pDameHit * maxTiemNang / 100;
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (n >= 0) {
            for (int i = 0; i < n; i++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int i = 0; i < -n; i++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (pl.zone != null && pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
//        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
//            tiemNang *= 2;
//        }
        if (MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            tiemNang *= 2;
        }

        return tiemNang / 4;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive
                            && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        int hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }

        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN
                || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE
                || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA
                || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO
                || this.tempId == ConstMob.PIANO);
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN
                && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss()
                && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend)
                        && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                        && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 230;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh)
                            || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        int dameMob = this.point.getDameAttack();
        if (player.effectSkill.isShielding
                && System.currentTimeMillis() - player.effectSkill.lastTimeShieldUp <= player.effectSkill.timeShield) {
            dameMob = 0;
        }
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.isPet && ((Pet) player).master.charms != null
                && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (int) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (int) Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        int dame = player.injured(null, dameMob, false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, int dame) {
        if (!player.isPet && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(dame); // dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(player.nPoint.hp);
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 18 && !isBigBoss() ? Util.isTrue(3, 100) ? 1 : 0 : 0;
        this.point.hp = this.lvMob > 0 ? this.point.maxHp <= 20000000 ? this.point.maxHp * 2 : 2000000000
                : this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(int hp) {
        Message msg = null;
        try {
            this.point.sethp(this.point.gethp() + hp);
            int HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(HP);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, int dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private boolean isValidCaiTrang(Item caiTrang, int... ids) {
        if (caiTrang == null || caiTrang.template == null || !caiTrang.isNotNullItem()) {
            return false;
        }
        for (int id : ids) {
            if (caiTrang.template.id == id) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidQuan(Item quan, int... ids) {
        if (quan == null || quan.template == null || !quan.isNotNullItem()) {
            return false;
        }
        for (int id : ids) {
            if (quan.template.id == id) {
                return true;
            }
        }
        return false;
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); // sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    private boolean isHeoNe() {
        return this.tempId == ConstMob.HEO_DA_XANH || this.tempId == ConstMob.HEO_RUNG
                || this.tempId == ConstMob.HEO_RUNG_ME
                || this.tempId == ConstMob.HEO_XANH_ME || this.tempId == ConstMob.HEO_XAYDA
                || this.tempId == ConstMob.HEO_XAYDA_ME;
    }

    private boolean isMocNhan() {
        return this.tempId == ConstMob.MOC_NHAN;
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player.isBoss) {
            return list;
        }

        // if (player.isPl() && Util.isTrue(1, 10000) && this.tempId == 0) {
        // short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
        // ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
        // List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        // if (!ops.isEmpty()) {
        // it.options = ops;
        // }
        // it.options.add(new Item.ItemOption(210, 0));
        // it.options.add(new Item.ItemOption(216, 0));
        // it.options.add(new Item.ItemOption(30, 0));
        // list.add(it);
        // }
        if (this.tempId == 0) {
            return list;
        }
        int mapid = player.zone.map.mapId;
        int dropRateModifier = 1;
        if (player != null) {
            if (player.itemTime.isCoBonLa) {
                dropRateModifier *= 2; // Tăng 50% (x1.5)
            }
        }

        if (EventManager.CHRISTMAS) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (Util.isTrue(1, 50)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropTatVoGiangSinh(100)) {
                    list.add(new ItemMap(zone, 649, 1, x, yEnd, player.id));
                }
            }
        }
        if (mapid == 5 || mapid == 13) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
//            if (Util.isTrue(1, 500)) {
//                if (pl.itemEvent != null && pl.itemEvent.canDropBinhNuoc(100)) {
//                    list.add(new ItemMap(zone, 456, 1, x, yEnd, pl.id));
//                }
//            }
        }
        if (EventManager.INTERNATIONAL_WOMANS_DAY) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (Util.isTrue(1, 50)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropHoaHong(100)) {
                    list.add(new ItemMap(zone, 610, 1, x, yEnd, player.id));
                }
            }
        }

        if (MapService.gI().isMapTuongLai(mapid) && Util.isTrue(5, 5000)) {
            ItemMap it = ItemService.gI().randDoTuongLai(this.zone, 1, x, yEnd, player.id);
            list.add(it);
        }
        if (mapid > 1 && Util.isTrue(5, 5000)) {
            ItemMap it = ItemService.gI().randDoThuong(this.zone, 1, x, yEnd, player.id);
            list.add(it);
        }
        if (mapid >= 63 && mapid <= 83 && Util.isTrue(5, 5000)) {
            ItemMap it = ItemService.gI().randDoNappa(this.zone, 1, x, yEnd, player.id);
            list.add(it);
        }

        if (Util.isTrue(1, 3000)) {
            list.add(new ItemMap(zone, 18, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(1, 3000)) {
            list.add(new ItemMap(zone, 19, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(1, 3000)) {
            list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
        }
        if (EventManager.HALLOWEEN) {
            if (MapService.gI().isMapEventHalloween(mapid)) {
                if (Util.isTrue(1, 50)) {
                    list.add(new ItemMap(zone, 707, 1, x, yEnd, player.id));
                } else if (Util.isTrue(1, 50)) {
                    list.add(new ItemMap(zone, 708, 1, x, yEnd, player.id));
                }
            }
        }

        if (player.itemTime.isUseMayDo && (Util.isTrue(3, 100) || (player.actived() && Util.isTrue(3, 100)))
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
        }

        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
            if (player.gender == 0 && this.tempId == 11 || player.gender == 1 && this.tempId == 12
                    || player.gender == 2 && this.tempId == 10) {
                list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
            }
        }

        //doanh trại
        if (MapService.gI().isMapDoanhTrai(mapid) && Util.isTrue(100, 100)) {
            for (int i = 0; i < 3; i++) {
                if (i == 0 || Util.isTrue(80, 100)) {
                    list.add(new ItemMap(zone, 190, Util.nextInt(10000, 20000), x + i * 20, yEnd, player.id));
                }
            }
        }

        if (MapService.gI().isMapPhoBan(mapid) && this.tempId != 22) {
        }
        if (DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_NGOC_MIEN_PHI)) {
            if (Util.isTrue(50, 100)) {
                list.add(new ItemMap(zone, 77, Util.nextInt(1, 3), x, yEnd, player.id));
                DailyGiftService.updateDailyGift(player, ConstDailyGift.NHAN_NGOC_MIEN_PHI);
            }
        }
        if (MapService.gI().isMapUpPorata(mapid)) {
            if (Util.isTrue(70, 100)) {
                ItemMap it = new ItemMap(zone, 934, Util.nextInt(1, 10), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(150, 200)) {
                ItemMap it = new ItemMap(zone, 935, Util.nextInt(1, 10), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(8, 10)) {
                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, Util.nextInt(1, 100)));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }

//        Item quan = player.inventory.itemsBody.get(1);
//if (isValidQuan(quan, 691, 692, 693)) {
//    int itemId = -1;
//    int rand = Util.nextInt(1, 100); // 1 - 100
//
//    if (rand <= 5) { // 5%
//        itemId = 1002;
//    } else if (rand <= 10) { // 5% tiếp theo
//        itemId = 1003;
//    } else if (rand == 11) { // 1%
//        itemId = 1004;
//    }
//
//    // Thêm cá tuyết (1166) rơi không giới hạn
//    if (Util.nextInt(1, 100) <= 5) { // 5% rơi cá tuyết
//        ItemMap caTuyet = new ItemMap(zone, 1166, 1, x, yEnd, player.id);
//        caTuyet.options.add(new Item.ItemOption(93, 15));
//        list.add(caTuyet);
//    }
//
//    if (itemId != -1) {
//        if (DropLimit.isDropLimit(itemId)) {
//            if (player.drop_limit[itemId - 1002] < DropLimit.getDropLimit(itemId)) {
//                player.drop_limit[itemId - 1002]++;
//                ItemMap itemSKienHe = new ItemMap(zone, itemId, 1, x, yEnd, player.id);
//                itemSKienHe.options.add(new Item.ItemOption(93, 15));
//                itemSKienHe.options.add(new Item.ItemOption(30, 0));
//                list.add(itemSKienHe);
//            }
//        }
//    }
//}
        // Cơm Nếp
         // Vàng rơi
int ratioGold = 0;
if (player.nPoint != null && player.nPoint.tlGold > 0) {
    ratioGold = player.nPoint.tlGold;
}
if (Util.isTrue(7, 100)) {
    int vang;
    int mapId = zone.map.mapId;

    if (MapService.gI().isMapNappa(mapId)) {
        vang = Util.nextInt(2000, 8888);
    } else if (MapService.gI().isMapTuongLai(mapId)) {
        vang = Util.nextInt(8888, 18888);
    } else if (MapService.gI().isMapCold(mapId)) {
        vang = Util.nextInt(10000, 50000);
    } else {
        vang = Util.nextInt(500, 2222);
    }

    if (player.playerIntrinsic != null && player.playerIntrinsic.intrinsic != null && player.playerIntrinsic.intrinsic.id == 23) {
        vang *= (player.playerIntrinsic.intrinsic.param1 / 100) / 16;
    }

    if (ratioGold > 0) {
        vang += (vang * ratioGold) / 100;
    }

    if (vang < 2222) {
        list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
    } else if (vang < 8888) {
        list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
    } else {
        list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
    }
}

//        // Set kich hoat
//       if (true) {
//    long accountAgeInMs = System.currentTimeMillis() - player.createdTime;
//    long thirtyDaysInMs = 30L * 24 * 60 * 60 * 1000;
//
//    if (accountAgeInMs <= thirtyDaysInMs) {
//        // Mầm 30 ngay
//        if (Util.isTrue(1, 100_000) && MapService.gI().isMapUpSKH(mapid)) {
//            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//
//            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//            if (!ops.isEmpty()) {
//                it.options = ops;
//            }
//
//            int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
//            it.options.add(new Item.ItemOption(opsrand[0], 0));
//            it.options.add(new Item.ItemOption(opsrand[1], 0));
//            it.options.add(new Item.ItemOption(30, 0));
//
//            list.add(it);
//
//            // Thông báo toàn server 
//         //   Service.gI().sendThongBaoAllPlayer("Người chơi " + player.name + " vừa tìm được vật phẩm SIÊU HIẾM!");
//        }
//    }
//}
//        } else {
//            if (Util.isTrue(10, 500) && MapService.gI().isMapUpSKH(mapid)) {
//                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//                if (!ops.isEmpty()) {
//                    it.options = ops;
//                }
//
//                int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
//                it.options.add(new Item.ItemOption(opsrand[0], 0));
//                it.options.add(new Item.ItemOption(opsrand[1], 0));
//                list.add(it);
//                ChatGlobalService.gI().autoChatGlobal(player, "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
//            }
//        }
//        if (((Util.isTrue(1, 50000)) || (Manager.TEST && Util.isTrue(5, 1000000)) || Util.isTrue(1, 10000000)) && MapService.gI().isMapUpSKH(mapid)) {
//            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//            if (!ops.isEmpty()) {
//                it.options = ops;
//            }
//
//            int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(player.gender);
//            it.options.add(new Item.ItemOption(opsrand[0], 0));
//            it.options.add(new Item.ItemOption(opsrand[1], 0));
//            it.options.add(new Item.ItemOption(opsrand[2], 0));
//            it.options.add(new Item.ItemOption(opsrand[3], 0));
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//            ChatGlobalService.gI().autoChatGlobal(player, "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
//        } 
            // Sao pha le
            if (player.itemTime.isCoBonLa) {
                if (Util.isTrue(1, 1000)) {
                    int rand = Util.nextInt(0, 6);
                    ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
                    it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
                    list.add(it);
                }
            } else {
                if (Util.isTrue(1, 1000)) {
                    int rand = Util.nextInt(0, 6);
                    ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
                    it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
                    list.add(it);
                }
            }

            // Da nang cap
            if (player.itemTime.isCoBonLa) {
                if (Util.isTrue(3, 1500)) {
                    int rand = Util.nextInt(0, 4);
                    ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
                    it.options.add(new Item.ItemOption(71 - rand, 0));
                    list.add(it);
                }
            } else {
                if (Util.isTrue(3, 1500)) {
                    int rand = Util.nextInt(0, 4);
                    ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
                    it.options.add(new Item.ItemOption(71 - rand, 0));
                    list.add(it);
                }
            }

            if (MapService.gI().isMapCold(mapid)) {
                if (player.isPet) {
                    player = ((Pet) player).master;
                }
                if (player.isPet) {
                    player = ((Pet) player).master;
                }
                if (player.itemTime.isCoBonLa) {
                    if (Util.isTrue(1, 5000)) {
                        ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, player.id);
                        list.add(it);
                        ServerNotify.gI().notify(player.name + " vừa nhặt được " + it.itemTemplate.name + " tại "
                                + this.zone.map.mapName + " khu " + this.zone.zoneId);
                    }
                    if (Util.isTrue(1, 1200) && InventoryService.gI().fullSetThan(player)) {
                        ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                        it.options.add(new Item.ItemOption(30, 0));
                        list.add(it);
                    }
                } else {
                    if (Util.isTrue(1, 8000)) {
                        ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, player.id);
                        list.add(it);
                        ServerNotify.gI().notify(player.name + " vừa nhặt được " + it.itemTemplate.name + " tại "
                                + this.zone.map.mapName + " khu " + this.zone.zoneId);
                    }
                    if (Util.isTrue(1, 3000) && InventoryService.gI().fullSetThan(player)) {
                        ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                        it.options.add(new Item.ItemOption(30, 0));
                        list.add(it);
                    }
                }
            }
            if (MapService.gI().isMapBiaRung(mapid)) {
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_31_7) {
                    if (Util.isTrue(1, 150)) {
                        ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                        it.options.add(new Item.ItemOption(30, 0));
                        list.add(it);
                    }
                }
            }
//        if (Util.isTrue(1, 1000)) {
//            if (this.isMocNhan()) {
//                ItemMap it = new ItemMap(zone, 751, 1, x, yEnd, player.id);
//                it.options.add(new Item.ItemOption(86, 0));
//                it.options.add(new Item.ItemOption(174, 2025));
//                list.add(it);
//            }
//        }
            // bản đồ x doanh trại
            if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId) && player.clan != null) {
                int bdkbLevel = player.clan.BanDoKhoBau.level;
                int goldPerBag = 15000 + (bdkbLevel / 5) * 1000;
                int trai = 1;
                int phai = 1;
                int next = 0;
                for (int i = 0; i < 15; i++) {
                    int X = next == 0 ? -20 * trai : 20 * phai;
                    if (next == 0) {
                        trai++;
                    } else {
                        phai++;
                    }
                    next = next == 0 ? 1 : 0;
                    if (trai > 20) {
                        trai = 0;
                    }
                    if (phai > 20) {
                        phai = 0;
                    }

                    ItemMap itemMap = new ItemMap(this.zone, 190, goldPerBag, x + X, yEnd, player.id);
                    list.add(itemMap);
                }
            }

//        if (MapService.gI().isMapTuongLai(mapid)
//                && ((Util.isTrue(1, 1000) || (player.actived() && Util.isTrue(1, 150))))
//                && InventoryService.gI().fullSetThan(player)) {
//            ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//        }
//        if (Util.isTrue(1, 100000) || (player.actived() && Util.isTrue(1, 10000))) {
//            list.add(new ItemMap(zone, 457, 1, x, yEnd, player.id));
//        }
            // Manh thien su
            if ((Util.isTrue(1, 100000) || (player.actived() && Util.isTrue(20, 100)))
                    && MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {
                list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, yEnd, player.id));
            }
            if (Util.isTrue(1, 300) && MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {
                list.add(new ItemMap(zone, 1281, 1, x, yEnd, player.id));
            }
//        if (Util.isTrue(1, 5000) || (player.actived() && Util.isTrue(1, 1000))) {
//            list.add(new ItemMap(zone, 861, 1, x, yEnd, player.id));
//        }
            // if (player.nPoint.power >= 80000000000L) {
            // if (player.zone.map.mapId == 155) {
            // list.add(new ItemMap(zone, 2055, 1, x, yEnd, player.id));
            // } else {
            // if (Util.isTrue(1, 500) || (player.actived() && Util.isTrue(1, 100))) {
            // list.add(new ItemMap(zone, 2051, 1, x, yEnd, player.id));
            // }
            // if (Util.isTrue(1, 1000) || (player.actived() && Util.isTrue(1, 200))) {
            // list.add(new ItemMap(zone, 2052, 1, x, yEnd, player.id));
            // }
            // }
            // }
            return list;
        }

    

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(int dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(int maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(maxHp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            int damePST = (int) (long) (dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                int hpMob = this.point.hp;
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writer().writeInt(this.point.hp);
                msg.writer().writeInt(damePST);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

}
