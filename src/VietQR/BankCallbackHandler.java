/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package VietQR;

import jdbc.daos.SQLHandle;
import player.Player;
import services.PlayerService;
import services.Service;

/**
 *
 * @author Administrator
 */
public class BankCallbackHandler {

    /**
     * Xử lý callback từ ngân hàng (có thể từ webhook hoặc API polling)
     *
     * @param amount Số tiền
     * @param content Nội dung chuyển khoản
     * @param transactionId Mã giao dịch ngân hàng
     * @param status Trạng thái (SUCCESS/FAILED)
     */
    public static void handleBankCallback(long amount, String content, String transactionId, String status) {
        TopupTransaction transaction = TopupManager.getInstance().getTransactionByQrContent(content);

        if (transaction == null) {
            System.out.println("Transaction not found for content: " + content);
            return;
        }

        if (transaction.getAmount() != amount) {
            System.out.println("Amount mismatch: expected " + transaction.getAmount() + ", got " + amount);
            return;
        }

        if ("SUCCESS".equals(status)) {
            Player player = SQLHandle.loadById(transaction.getPlayerId());
            if (player != null) {
                player.inventory.gem += transaction.getGemsAmount();
                Service.gI().sendMoney(player);
                Service.gI().sendThongBao(player, "Nạp tiền thành công! Đã cộng " + transaction.getGemsAmount() + " ngọc.");

                // Cập nhật trạng thái
                TopupManager.getInstance().updateTransactionStatus(
                        transaction.getTransactionId(),
                        TopupTransaction.TransactionStatus.SUCCESS,
                        transactionId,
                        content
                );

                // Log giao dịch
                System.out.println("Successfully added " + transaction.getGemsAmount() + " gems to player " + player.name);
            }
        } else {
            // Cập nhật trạng thái thất bại
            TopupManager.getInstance().updateTransactionStatus(
                    transaction.getTransactionId(),
                    TopupTransaction.TransactionStatus.FAILED,
                    transactionId,
                    content
            );
        }
    }
}
