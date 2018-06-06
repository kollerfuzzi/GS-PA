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
    private Type type;

    public enum Type {
        LOGGED_IN, PLAYING, DEAD, DISCONNECTED
    }

    public PlayerStatus(String playerID, Type type) {
        this.playerID = playerID;
        this.type = type;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
