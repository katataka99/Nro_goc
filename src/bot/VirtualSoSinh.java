package bot;

import map.Map;
import map.Zone;
import server.Manager;
import utils.Util;

/**
 *
 * @author HairMod
 */
public class VirtualSoSinh extends VirtualPlayer {

    private Thread botThread;
    private volatile boolean running = true; 
    private boolean lockMap = true;

    public VirtualSoSinh(String name) {
        super(name); 
        this.gender = (byte) Util.nextInt(3); 
        this.nPoint.power = Util.nextInt(1_500_000, 1_500_005); 
        int hp = Util.nextInt(3000, 5000);
        int mp = Integer.MAX_VALUE;
        this.setInfo(hp, mp, Util.nextInt(50, 70), 10000, 1);

        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.mp = this.nPoint.mpMax;
        this.charms.tdThuHut = 1924966799000L; 
        short[][] hair = {
            {64, 30, 31},
            {9, 29, 32},
            {6, 27, 28}
        };

        short head = hair[this.gender][Util.nextInt(hair[this.gender].length)];
        short body = (short) (this.gender == 0 ? 14 : this.gender == 1 ? 10 : 16);
        short leg  = (short) (this.gender == 0 ? 15 : this.gender == 1 ? 11 : 17);

        boolean ctrang = Util.isTrue(5, 10);

        this.setHead(ctrang ? (short) 906 : head);
        this.setBody(ctrang ? (short) 880 : body);
        this.setLeg (ctrang ? (short) 881 : leg);

        timeSpawn = System.currentTimeMillis();

        startBotThread();
    }
 
    private void startBotThread() {
        botThread = new Thread(() -> {
            try {
                Thread.sleep(2000); 

                while (running && !beforeDispose) {
                    try {
                        update();
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println("[BOT " + name + "] update error");
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }, "Bot-" + name);

        botThread.start();
    }
 
    @Override
    public void update() { 
        this.nPoint.mp = this.nPoint.mpMax = Integer.MAX_VALUE; 
        if (!lockMap) {
            updateNextMap();
            changeMapIfNeeded();
        } 
        if (zone != null) {
            attack();
        }
    } 

    private void changeMapIfNeeded() {
        if (zone == null) return;

        if (zone.map.mapId != mapNext) {
            Map map = Manager.MAPS.get(mapNext);
            if (map != null) {
                Zone newZone = map.getMinPlayerZone();
                if (newZone != null) {
                    zone.removePlayer(this);
                    newZone.addPlayer(this);
                }
            }
        }
    }
 
    public void setLockMap(boolean lock) {
        this.lockMap = lock;
    }
 
    public void setCurrentMap(int mapId) {
        this.mapNext = mapId;
    }
 
    private void updateNextMap() {
        long time = System.currentTimeMillis() - timeSpawn;
        int oldMap = mapNext;

        if (time < 50_000) {
            mapNext = gender == 0 ? 0 : gender == 1 ? 7 : 14;
        } else if (time < 100_000) {
            mapNext = gender == 0 ? 1 : gender == 1 ? 8 : 15;
        } else if (time < 150_000) {
            mapNext = gender == 0 ? 2 : gender == 1 ? 9 : 16;
        } else {
            mapNext = -1;
            running = false;
            if (zone != null) zone.removePlayer(this);
            dispose();
        }

        if (oldMap != mapNext && mapNext >= 0) {
            this.nPoint.hp = this.nPoint.hpMax;
            this.nPoint.mp = this.nPoint.mpMax;
        }
    }
 
    @Override
    public boolean isPl() {
        return true;
    }

    @Override
    public void dispose() {
        running = false;
        if (botThread != null) {
            botThread.interrupt();
        }
        super.dispose();
    }
}
