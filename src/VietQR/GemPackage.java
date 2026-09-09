/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package VietQR;

/**
 *
 * @author HAIRMOD
 */
public class GemPackage {

    private int id;
    private String name;
    private long price;
    private long gems;
    private long bonusGems;
    private boolean isActive;

    public GemPackage(int id, String name, long price, long gems, long bonusGems) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.gems = gems;
        this.bonusGems = bonusGems;
        this.isActive = true;
    }

    public long getTotalGems() {
        return gems + bonusGems;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public long getGems() {
        return gems;
    }

    public long getBonusGems() {
        return bonusGems;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
