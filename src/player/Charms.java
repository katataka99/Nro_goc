package player;

public class Charms {
    // Charm expiration times
    public long tdTriTue;
    public long tdManhMe;
    public long tdDaTrau;
    public long tdOaiHung;
    public long tdBatTu;
    public long tdDeoDai;
    public long tdThuHut;
    public long tdDeTu;
    public long tdTriTue3;
    public long tdTriTue4;

    // Charm durations in minutes
    public int minutesTriTue;
    public int minutesManhMe;
    public int minutesDaTrau;
    public int minutesOaiHung;
    public int minutesBatTu;
    public int minutesDeoDai;
    public int minutesThuHut;
    public int minutesDeTu;
    public int minutesTriTue3;
    public int minutesTriTue4;

    public long lastTimeSubMinTriTueX4;

    public void addTimeCharms(int itemId, int min) {
        switch (itemId) {
            case 213:
                if (tdTriTue < System.currentTimeMillis()) {
                    tdTriTue = System.currentTimeMillis();
                    minutesTriTue = 0;
                }
                tdTriTue += min * 60 * 1000L;
                minutesTriTue += min;
                break;
            case 214:
                if (tdManhMe < System.currentTimeMillis()) {
                    tdManhMe = System.currentTimeMillis();
                    minutesManhMe = 0;
                }
                tdManhMe += min * 60 * 1000L;
                minutesManhMe += min;
                break;
            case 215:
                if (tdDaTrau < System.currentTimeMillis()) {
                    tdDaTrau = System.currentTimeMillis();
                    minutesDaTrau = 0;
                }
                tdDaTrau += min * 60 * 1000L;
                minutesDaTrau += min;
                break;
            case 216:
                if (tdOaiHung < System.currentTimeMillis()) {
                    tdOaiHung = System.currentTimeMillis();
                    minutesOaiHung = 0;
                }
                tdOaiHung += min * 60 * 1000L;
                minutesOaiHung += min;
                break;
            case 217:
                if (tdBatTu < System.currentTimeMillis()) {
                    tdBatTu = System.currentTimeMillis();
                    minutesBatTu = 0;
                }
                tdBatTu += min * 60 * 1000L;
                minutesBatTu += min;
                break;
            case 218:
                if (tdDeoDai < System.currentTimeMillis()) {
                    tdDeoDai = System.currentTimeMillis();
                    minutesDeoDai = 0;
                }
                tdDeoDai += min * 60 * 1000L;
                minutesDeoDai += min;
                break;
            case 219:
                if (tdThuHut < System.currentTimeMillis()) {
                    tdThuHut = System.currentTimeMillis();
                    minutesThuHut = 0;
                }
                tdThuHut += min * 60 * 1000L;
                minutesThuHut += min;
                break;
            case 522:
                if (tdDeTu < System.currentTimeMillis()) {
                    tdDeTu = System.currentTimeMillis();
                    minutesDeTu = 0;
                }
                tdDeTu += min * 60 * 1000L;
                minutesDeTu += min;
                break;
            case 671:
                if (tdTriTue3 < System.currentTimeMillis()) {
                    tdTriTue3 = System.currentTimeMillis();
                    minutesTriTue3 = 0;
                }
                tdTriTue3 += min * 60 * 1000L;
                minutesTriTue3 += min;
                break;
            case 672:
                if (tdTriTue4 < System.currentTimeMillis()) {
                    tdTriTue4 = System.currentTimeMillis();
                    minutesTriTue4 = 0;
                }
                tdTriTue4 += min * 60 * 1000L;
                minutesTriTue4 += min;
                break;
        }
    }

    // Adjust charm times based on offline duration
    public void adjustCharmTimes(long offlineDuration) {
        if (offlineDuration > 0) {
            if (tdTriTue > System.currentTimeMillis()) {
                tdTriTue += offlineDuration;
            }
            if (tdManhMe > System.currentTimeMillis()) {
                tdManhMe += offlineDuration;
            }
            if (tdDaTrau > System.currentTimeMillis()) {
                tdDaTrau += offlineDuration;
            }
            if (tdOaiHung > System.currentTimeMillis()) {
                tdOaiHung += offlineDuration;
            }
            if (tdBatTu > System.currentTimeMillis()) {
                tdBatTu += offlineDuration;
            }
            if (tdDeoDai > System.currentTimeMillis()) {
                tdDeoDai += offlineDuration;
            }
            if (tdThuHut > System.currentTimeMillis()) {
                tdThuHut += offlineDuration;
            }
            if (tdDeTu > System.currentTimeMillis()) {
                tdDeTu += offlineDuration;
            }
            if (tdTriTue3 > System.currentTimeMillis()) {
                tdTriTue3 += offlineDuration;
            }
            if (tdTriTue4 > System.currentTimeMillis()) {
                tdTriTue4 += offlineDuration;
            }
        }
    }

    public void dispose() {
    }
}