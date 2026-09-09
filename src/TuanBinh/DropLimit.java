package TuanBinh;

/**
 *
 * @author admin
 */
public class DropLimit {

    public static boolean isDropLimit(int id) {
        return id >= 1002 && id <= 1004;
    }

    public static int getDropLimit(int id) {
        switch (id) {
            case 1002:
            case 1003:
            case 1004:
                return 99;
        }
        return -1;
    }
}
