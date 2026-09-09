/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package VietQR;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.sql.*;
import java.util.*;
import jdbc.DBConnecter;

/**
 *
 * @author Administrator
 */
public class TopupManager {

    private static TopupManager instance;
    private final Map<String, TopupTransaction> pendingTransactions = new ConcurrentHashMap<>();
    private final List<GemPackage> gemPackages = new ArrayList<>();

    public static TopupManager getInstance() {
        if (instance == null) {
            instance = new TopupManager();
        }
        return instance;
    }

    private TopupManager() {
        initGemPackages();
        loadPendingTransactions();
        startCleanupTask();
    }

    private void initGemPackages() {
        gemPackages.add(new GemPackage(1, "20,000đ", 20000, 30, 10));
        gemPackages.add(new GemPackage(2, "50,000đ", 50000, 70, 21));
        gemPackages.add(new GemPackage(3, "100,000đ", 100000, 150, 45));
        gemPackages.add(new GemPackage(4, "200,000đ", 200000, 350, 105));
        gemPackages.add(new GemPackage(5, "500,000đ", 500000, 1100, 330));
        gemPackages.add(new GemPackage(6, "1,000,000đ", 1000000, 2500, 750));
    }

    public List<GemPackage> getActivePackages() {
        return gemPackages.stream()
                .filter(GemPackage::isActive)
                .collect(Collectors.toList());
    }

    public GemPackage getPackageById(int id) {
        return gemPackages.stream()
                .filter(p -> p.getId() == id && p.isActive())
                .findFirst()
                .orElse(null);
    }

    public void addPendingTransaction(TopupTransaction transaction) {
        pendingTransactions.put(transaction.getQrContent(), transaction);

        // Lưu vào database
        try (Connection conn = DBConnecter.getConnectionServer()) {
            String sql = "INSERT INTO topup_transactions (transaction_id, player_id, player_name, amount, gems_amount, qr_content, status, created_at, expired_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, transaction.getTransactionId());
            ps.setInt(2, transaction.getPlayerId());
            ps.setString(3, transaction.getPlayerName());
            ps.setLong(4, transaction.getAmount());
            ps.setLong(5, transaction.getGemsAmount());
            ps.setString(6, transaction.getQrContent());
            ps.setString(7, transaction.getStatus().name());
            ps.setTimestamp(8, Timestamp.valueOf(transaction.getCreatedAt()));
            ps.setTimestamp(9, Timestamp.valueOf(transaction.getExpiredAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TopupTransaction getTransactionByQrContent(String qrContent) {
        return pendingTransactions.get(qrContent);
    }

    public void updateTransactionStatus(String transactionId, TopupTransaction.TransactionStatus status, String bankTransactionId, String bankNote) {
        // Cập nhật trong memory
        TopupTransaction transaction = pendingTransactions.values().stream()
                .filter(t -> t.getTransactionId().equals(transactionId))
                .findFirst()
                .orElse(null);

        if (transaction != null) {
            transaction.setStatus(status);
            transaction.setBankTransactionId(bankTransactionId);
            transaction.setBankNote(bankNote);

            if (status == TopupTransaction.TransactionStatus.SUCCESS
                    || status == TopupTransaction.TransactionStatus.FAILED) {
                pendingTransactions.remove(transaction.getQrContent());
            }
        }

        // Cập nhật database
        try (Connection conn = DBConnecter.getConnectionServer()) {
            String sql = "UPDATE topup_transactions SET status = ?, bank_transaction_id = ?, bank_note = ?, updated_at = ? WHERE transaction_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status.name());
            ps.setString(2, bankTransactionId);
            ps.setString(3, bankNote);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, transactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingTransactions() {
        try (Connection conn = DBConnecter.getConnectionServer()) {
            String sql = "SELECT * FROM topup_transactions WHERE status = 'PENDING' AND expired_at > ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TopupTransaction transaction = new TopupTransaction(
                        rs.getInt("player_id"),
                        rs.getString("player_name"),
                        rs.getLong("amount"),
                        rs.getLong("gems_amount"),
                        rs.getString("qr_content")
                );
                pendingTransactions.put(transaction.getQrContent(), transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startCleanupTask() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredTransactions();
            }
        }, 60000, 60000); // Mỗi phút chạy 1 lần
    }

    private void cleanupExpiredTransactions() {
        LocalDateTime now = LocalDateTime.now();
        Iterator<Map.Entry<String, TopupTransaction>> iterator = pendingTransactions.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TopupTransaction> entry = iterator.next();
            TopupTransaction transaction = entry.getValue();

            if (now.isAfter(transaction.getExpiredAt())) {
                updateTransactionStatus(transaction.getTransactionId(),
                        TopupTransaction.TransactionStatus.EXPIRED, null, null);
                iterator.remove();
            }
        }
    }
}
