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
import consts.ConstPlayer;
import item.Item;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.PlayerService;
import services.Service;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class Baby extends Boss {

    public boolean callBaby;

    public Baby() throws Exception {
        super(BABY, BossID.BABY, false, true, BossesData.BABY, BossesData.BABY_VEGETA);
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callBaby = false;
    }

    public void callBaby() {
        try {
            this.changeStatus(BossStatus.AFK);
            this.changeToTypeNonPK();
            this.recoverHP();
            this.callBaby = true;
            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                if (boss.id == BossID.BABY_MONKEY) {
                    boss.changeStatus(BossStatus.RESPAWN);
                }
            }
            this.setDie(this);
            this.die(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
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
    ItemMap item16 = new ItemMap(this.zone, 17, 1, this.location.x,
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
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null && !plAtt.isBoss && !plAtt.isPet) {
            this.playerReward = plAtt; // <<-- gán playerReward
        }

        if (!this.callBaby && this.currentLevel == 1 && damage >= this.nPoint.hp && Util.isTrue(50, 100)) {
            this.callBaby();
            return 0;
        }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 7);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 7;
            }

            this.nPoint.subHP(damage);

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
