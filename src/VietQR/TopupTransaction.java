/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package VietQR;

import java.time.LocalDateTime;

/**
 *
 * @author HAIRMOD
 */
public class TopupTransaction {

    private String transactionId;
    private int playerId;
    private String playerName;
    private long amount;
    private long gemsAmount;
    private String qrContent;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private String bankTransactionId; // Mã giao dịch ngân hàng
    private String bankNote; // Nội dung chuyển khoản

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, EXPIRED
    }

    public TopupTransaction(int playerId, String playerName, long amount, long gemsAmount, String qrContent) {
        this.transactionId = generateTransactionId();
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
        this.gemsAmount = gemsAmount;
        this.qrContent = qrContent;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusMinutes(10);
    }

    private String generateTransactionId() {
        return System.currentTimeMillis() + "_" + playerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getAmount() {
        return amount;
    }

    public long getGemsAmount() {
        return gemsAmount;
    }

    public String getQrContent() {
        return qrContent;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public String getBankNote() {
        return bankNote;
    }

    public void setBankNote(String bankNote) {
        this.bankNote = bankNote;
    }
}
