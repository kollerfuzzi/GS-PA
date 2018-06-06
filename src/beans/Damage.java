/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;

/**
 * Gives damage to a player when received
 *
 * @author koller
 */
public class Damage implements Serializable {

    private String playerID;
    private int damageValue = 5;

    /**
     * Gives the default amount of damage to a player when received<br><br>
     * -player: the one who gets the damage
     */
    public Damage(String player) {
        this.playerID = player;
    }

    /**
     * Gives damage to a player when received<br><br>
     * -player: the one who gets the damage<br>
     * -damageValue: the amount of damage
     */
    public Damage(String player, int damageValue) {
        this.playerID = player;
        this.damageValue = damageValue;
    }

    public String getPlayer() {
        return playerID;
    }

    public void setPlayer(String player) {
        this.playerID = player;
    }

    public int getDamageValue() {
        return damageValue;
    }

    public void setDamageValue(int damageValue) {
        this.damageValue = damageValue;
    }

}
