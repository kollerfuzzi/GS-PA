/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.jme3.math.Vector3f;
import java.io.Serializable;

/**
 * Class to transfer player data, such as location, facing direction, movement direction or team
 * @author koller
 */
public class PlayerData implements Serializable {

    private String playerID;
    private String team;
    private Vector3f position;
    private Vector3f facingDir;
    private Vector3f movementDir;

    public PlayerData(String playerID, String team, Vector3f position, Vector3f facingDir, Vector3f movementDir) {
        this.playerID = playerID;
        this.team = team;
        this.position = position;
        this.facingDir = facingDir;
        this.movementDir = movementDir;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getFacingDir() {
        return facingDir;
    }

    public void setFacingDir(Vector3f facingDir) {
        this.facingDir = facingDir;
    }

    public Vector3f getMovementDir() {
        return movementDir;
    }

    public void setMovementDir(Vector3f movementDir) {
        this.movementDir = movementDir;
    }

}
