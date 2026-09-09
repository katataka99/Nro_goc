/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bot;

import java.util.List;
import map.Map;
import map.Zone;
import mob.Mob;
import mob.MobMe;
import player.Player;
import server.Manager;
import services.SkillService;
import skill.Skill;
import utils.SkillUtil;
import utils.Util;

/**
 *
 * @author HairMod
 */
public class VirtualPlayer extends Player {

    public int mapNext = 0;
    public long timeSpawn = 0;
    public boolean isTrain = false;
    private long lastupdate = 0;
    public Mob mobFocus = null;
    public long lastAtt;
    public Skill skill;

    public VirtualPlayer(String name) {
        super();
        initSkill();
        this.name = name;
        this.id = Util.nextInt(1000000, 9999999);
        this.isPlayer = true;
    }

    public VirtualPlayer(int hp, int mp, int dame, short head, short body, short leg, String name, Zone zone) {
        super();
        this.setInfo(hp, mp, dame, 100, 5);
        initSkill();
        this.name = name;
        if (zone.map.mapId != 5) {
            this.setPos((short) Util.nextInt(0, zone.map.mapWidth - 50), location.y);
        } else {
            if (Util.nextInt(10) % 2 == 0) {
                this.setPos((short) Util.nextInt(0, zone.map.mapWidth - 50), location.y);
            } else {
                this.setPos((short) Util.nextInt(888, 1280), location.y);
            }
        }
        timeSpawn = System.currentTimeMillis();
        this.id = Util.nextInt(1000000, 9999999);

    }

    public void attack() {
        if (zone != null) {
            if (mobFocus == null) {
                List<Mob> mobs = zone.mobs;
                double minDistance = Double.MAX_VALUE;

                int playerX = this.getX();
                int playerY = this.getY();

                for (Mob mob : mobs) {
                    if (mob != null && mob.status != 0 && mob.status != 1 && !(mob instanceof MobMe) && mob.point.gethp() > 0) {
                    
                        double distance = Math.sqrt(
                                Math.pow(mob.location.x - playerX, 2)
                                + Math.pow(mob.location.y - playerY, 2)
                        );
 
                        if (distance < minDistance) {
                            minDistance = distance;
                            mobFocus = mob;
                        }
                    }
                }

            }

            if (mobFocus != null) { 
                if (playerSkill == null || playerSkill.skills == null || playerSkill.skills.isEmpty()) {
                    return;
                }

                playerSkill.skillSelect = playerSkill.skills.get(0);
                playerSkill.skillSelect.manaUse = 0;
                Mob mob = mobFocus; 
                if (mob.status == 0 || mob.status == 1 || mob instanceof MobMe || mob.point.gethp() <= 0) {
                    mobFocus = null;
                    return;
                }
 
                double distanceX = Math.abs(this.getX() - mob.location.x);
                double distanceY = Math.abs(this.getY() - mob.location.y);
                if (distanceX > playerSkill.skillSelect.dx * 1.2 || distanceY > playerSkill.skillSelect.dy * 1.2) {
                    if (!this.isDie()) {
                        int newX = mob.location.x;
                        int newY = mob.location.y;
                        this.move(newX, newY);
                    }
                    return;
                }

                if (!this.isDie() && mob != null && System.currentTimeMillis() - lastAtt >= 550) {
                    lastAtt = System.currentTimeMillis();
                    SkillService.gI().useSkill(this, null, mobFocus, -1, null);
                }
            }
        } else {
        }
    }

    @Override
    public void update() {
        this.nPoint.mp = this.nPoint.mpMax = Integer.MAX_VALUE;

        if (this.zone != null) {
            if (!isTrain) {
                if (System.currentTimeMillis() - lastupdate >= Util.nextInt(5000, 60000)) {
                    try {
                        super.update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lastupdate = System.currentTimeMillis();
                }
            }
        }

    }

    public void setLocation(int mapID, int zoneID) {
        Map map = Manager.MAPS.get(mapID);
        if (zoneID == -1) {
            zoneID = map.randomZoneID();
        }
        Zone zone = map.getZoneByID(zoneID);
        setLocation(zone);
    }

    public void setLocation(Zone zone) {
        Map map = zone.map;
        int w = map.mapWidth;
        int h = map.mapHeight;
        this.setPos(w / 2, map.collisionLand((short) this.getX(), (short) 24));
        zone.addPlayer(this);
        zone.load_Me_To_Another(this);
    }

    protected void initSkill() {

        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;

        int[] skillTemps = new int[]{
            0, // Skill ID 0
            9, // Skill ID 9
            17, // Skill ID 17
        };

        for (int skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp, 7);
            if (skill != null) {
                skill.coolDown = 500;
                this.playerSkill.skills.add(skill);

            }
        }
    }

    @Override
    public boolean isPl() {
        return true;
    }
}
