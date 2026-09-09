package TuanBinh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import jdbc.DBConnecter;

/**
 *
 * @author admin
 */
public class AddPointEvent {
    //Point skien hè

    public static boolean addpointhe(int accountId, int pointHe) {
        String sql = "UPDATE account SET pointhe = pointhe + ? WHERE id = ?";
        try ( Connection con = DBConnecter.getConnectionServer();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pointHe);
            ps.setInt(2, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("pointhe đã được tăng thêm " + pointHe + " cho account_id: " + accountId);
                return true;
            } else {
                System.out.println("Không tìm thấy tài khoản có ID: " + accountId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
