/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boss.boss_manifest.Baby;

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import static boss.BossType.BABY;
import boss.BossesData;
import item.Item;
import java.util.Random;
import map.ItemMap;
import player.Player;
import server.Manager;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class BabyMonKeyYellow extends Boss {

    private long st;

    public BabyMonKeyYellow() throws Exception {
        super(BABY, BossID.BABY_MONKEY, BossesData.BABY_MONKEY);
    }

   @Override
public void reward(Player plKill) {
    plKill.effect.addPointTrumSanBoss();
    plKill.playerTask.kolTask.addCount();

    // 15% rơi đồ TL (có thêm option 107)
    if (Util.isTrue(15, 100)) {
        ItemMap it = ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        // Thêm option 107 với 10% tỉ lệ
        if (Util.isTrue(10, 100)) {
            int rand = Util.nextInt(1, 101);
            int value;
            if (rand <= 64) {
                value = 1;
            } else if (rand <= 89) {
                value = 2;
            } else if (rand <= 99) {
                value = 3;
            } else {
                value = 4;
            }
            it.options.add(new Item.ItemOption(107, value));
        }

        Service.gI().dropItemMap(this.zone, it);
    }

    // 15% rơi item id 1763 với option cố định + hạn sử dụng random
    if (Util.isTrue(15, 100)) {
        ItemMap it = new ItemMap(this.zone, 1763, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
        it.options.add(new Item.ItemOption(50, 24));
        it.options.add(new Item.ItemOption(77, 24));
        it.options.add(new Item.ItemOption(103, 24));
        it.options.add(new Item.ItemOption(94, 12));
        it.options.add(new Item.ItemOption(5, 12));

        // option hạn sử dụng ngẫu nhiên với tỉ lệ
        int rd = Util.nextInt(1, 100); // random 1 - 100
        if (rd <= 30) {
            it.options.add(new Item.ItemOption(93, 7));   // 30%
        } else if (rd <= 60) {
            it.options.add(new Item.ItemOption(93, 15));  // 30%
        } else if (rd <= 90) {
            it.options.add(new Item.ItemOption(93, 30));  // 30%
        }
        // 10% còn lại sẽ không có option 93

        Service.gI().dropItemMap(this.zone, it);
    }

    // 100% rơi item id 16
    ItemMap item16 = new ItemMap(this.zone, 16, 1, this.location.x,
            this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
    Service.gI().dropItemMap(this.zone, item16);

    // Rơi vàng id 189: 5 - 10 cục, mỗi cục 1.000.000
    int soLuong = Util.nextInt(5, 10);
    for (int i = 0; i < soLuong; i++) {
        ItemMap vang = new ItemMap(this.zone, 189, 1_000_000,
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
        Service.gI().dropItemMap(this.zone, vang);
    }
}

    @Override
    public void joinMap() {
        st = System.currentTimeMillis();
        this.zone = this.parentBoss.zone;
        ChangeMapService.gI().changeMap(this, this.zone,
                this.parentBoss.location.x + Util.nextInt(-100, 100), this.parentBoss.location.y);
        Service.gI().sendFlagBag(this);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
//        BossManager.gI().removeBoss(this);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {

    // GÁN NGƯỜI ĐÁNH ĐỂ DROP ĐỒ
    if (plAtt != null && !plAtt.isBoss && !plAtt.isPet) {
        this.playerReward = plAtt;
    }

    if (!this.isDie()) {
        // né đòn
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }

        // trừ theo def
        damage = this.nPoint.subDameInjureWithDeff(damage / 10);

        // khi có khiên
        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = damage / 10;
        }

        // trừ máu boss
        this.nPoint.subHP(damage);

        // nếu chết
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }

        return (int) damage;
    } else {
        return 0;
    }
}

}
