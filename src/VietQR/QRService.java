/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package VietQR;

/**
 *
 * @author Administrator
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jdbc.DBConnecter;
import network.Message;
import player.Player;
import services.Service;

public class QRService {

    public static final int ID_NganHang = 970422;
    public static final String tenNganHang = "MB";
    public static final String soTaiKhoan = "00131052009100";
    public static final String chuTaiKhoan = "NGUYEN HUU HAI";

    public static Message messageQR(byte type) {
        try {
            Message msg = Service.gI().messageSubCommand((byte) 0x50);
            msg.writer().writeByte(type);
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendMessageQR(Player pl) {
        Message msg = messageQR((byte) 0x00);
        pl.sendMessage(msg);
    }

    public static void sendGemPackages(Player pl) throws IOException {
        Message msg = messageQR((byte) 0x02);
        List<GemPackage> packages = TopupManager.getInstance().getActivePackages();

        msg.writer().writeByte(packages.size());
        for (GemPackage pkg : packages) {
            msg.writer().writeInt(pkg.getId());
            msg.writer().writeUTF(pkg.getName());
            msg.writer().writeLong(pkg.getPrice());
            msg.writer().writeLong(pkg.getGems());
            msg.writer().writeLong(pkg.getBonusGems());
        }
        pl.sendMessage(msg);
    }

    public static void sendMessageURL(Player pl, String URL, String transactionId) throws IOException {
        Message msg = messageQR((byte) 0x01);
        msg.writer().writeUTF(URL);
        msg.writer().writeUTF(transactionId);
        msg.writer().writeInt(ID_NganHang);
        msg.writer().writeUTF(tenNganHang);
        msg.writer().writeUTF(soTaiKhoan);
        msg.writer().writeUTF(chuTaiKhoan);
        pl.sendMessage(msg);
    }

    public static void sendTransactionStatus(Player pl, String transactionId, String status, String message) throws IOException {
        Message msg = messageQR((byte) 0x03);
        msg.writer().writeUTF(transactionId);
        msg.writer().writeUTF(status);
        msg.writer().writeUTF(message);
        pl.sendMessage(msg);
    }

    private static void readMoney(Message msg, Player pl) throws IOException {
        String money = msg.reader().readUTF();
        long tMoney = Long.parseLong(money);

        if (tMoney > 100_000_000_000L) {
            Service.gI().sendThongBao(pl, "Max 100B!");
            return;
        }

        if (tMoney < 10000) {
            Service.gI().sendThongBao(pl, "Số tiền nạp tối thiểu 10,000 VND!");
            return;
        }

        // Tính số ngọc (1000 VND = 1 ngọc)
        long gemsAmount = tMoney / 1000;

        String noidung = pl.id + " nap " + tMoney;
        String url = "https://img.vietqr.io/image/" + tenNganHang + "-" + soTaiKhoan + "-compact.png"
                + "?amount=" + tMoney
                + "&addInfo=" + URLEncoder.encode(noidung, StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(chuTaiKhoan, StandardCharsets.UTF_8);

        TopupTransaction transaction = new TopupTransaction(
                (int) pl.id, pl.name, tMoney, gemsAmount, noidung
        );

        TopupManager.getInstance().addPendingTransaction(transaction);
        sendMessageURL(pl, url, transaction.getTransactionId());

        Service.gI().sendThongBao(pl, "Đã tạo QR thanh toán! Vui lòng chuyển khoản trong 15 phút.");
    }

    private static void readPackageTopup(Message msg, Player pl) throws IOException {
        int packageId = msg.reader().readInt();

        GemPackage gemPackage = TopupManager.getInstance().getPackageById(packageId);
        if (gemPackage == null) {
            Service.gI().sendThongBao(pl, "Gói nạp không tồn tại!");
            return;
        }

        String noidung = pl.id + " nap " + gemPackage.getPrice();
        String url = "https://img.vietqr.io/image/" + tenNganHang + "-" + soTaiKhoan + "-compact.png"
                + "?amount=" + gemPackage.getPrice()
                + "&addInfo=" + URLEncoder.encode(noidung, StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(chuTaiKhoan, StandardCharsets.UTF_8);

        // Tạo transaction
        TopupTransaction transaction = new TopupTransaction(
                (int) pl.id, pl.name, gemPackage.getPrice(), gemPackage.getTotalGems(), noidung
        );

        TopupManager.getInstance().addPendingTransaction(transaction);
        sendMessageURL(pl, url, transaction.getTransactionId());

        Service.gI().sendThongBao(pl, "Đã tạo QR cho gói " + gemPackage.getName() + "! Vui lòng chuyển khoản trong 15 phút.");
    }

    private static void checkTransactionStatus(Message msg, Player pl) throws IOException {
        String transactionId = msg.reader().readUTF();

        // Tìm transaction trong database
        TopupTransaction transaction = findTransactionById(transactionId);
        if (transaction == null) {
            sendTransactionStatus(pl, transactionId, "NOT_FOUND", "Không tìm thấy giao dịch!");
            return;
        }

        if (transaction.getPlayerId() != pl.id) {
            sendTransactionStatus(pl, transactionId, "UNAUTHORIZED", "Không có quyền truy cập!");
            return;
        }

        String statusMsg = "";
        switch (transaction.getStatus()) {
            case PENDING:
                statusMsg = "Đang chờ thanh toán...";
                break;
            case SUCCESS:
                statusMsg = "Thanh toán thành công! Đã cộng " + transaction.getGemsAmount() + " ngọc.";
                break;
            case FAILED:
                statusMsg = "Thanh toán thất bại!";
                break;
            case EXPIRED:
                statusMsg = "Giao dịch đã hết hạn!";
                break;
        }

        sendTransactionStatus(pl, transactionId, transaction.getStatus().name(), statusMsg);
    }

    private static TopupTransaction findTransactionById(String transactionId) {
        try (Connection conn = DBConnecter.getConnectionServer()) {
            String sql = "SELECT * FROM topup_transactions WHERE transaction_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, transactionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                TopupTransaction transaction = new TopupTransaction(
                        rs.getInt("player_id"),
                        rs.getString("player_name"),
                        rs.getLong("amount"),
                        rs.getLong("gems_amount"),
                        rs.getString("qr_content")
                );
                // Set other fields...
                return transaction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void readMessage(Message msg, Player pl) throws IOException {
        byte type = msg.reader().readByte();
        switch (type) {
            case 0:
                sendMessageQR(pl);
                break;
            case 1:
                readMoney(msg, pl);
                break;
            case 2:
                sendGemPackages(pl);
                break;
            case 3:
                readPackageTopup(msg, pl);
                break;
            case 4:
                checkTransactionStatus(msg, pl);
                break;
        }
    }
}
