/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;

/**
 *
 * @author koller
 */
public class PlayerStatus implements Serializable {

    private String playerID;
    private String player2ID;
    private Type type;

    public enum Type {
        LOGGED_IN, PLAYING, DEAD, DISCONNECTED
    }

    public PlayerStatus(String playerID, String player2ID, Type type) {
        this.playerID = playerID;
        this.player2ID = player2ID;
        this.type = type;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getPlayer2ID() {
        return player2ID;
    }

    public void setPlayer2ID(String player2ID) {
        this.player2ID = player2ID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PlayerStatus{" + "playerID=" + playerID + ", player2ID=" + player2ID + ", type=" + type + '}';
    }

}
