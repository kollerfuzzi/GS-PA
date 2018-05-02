/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.jme3.math.Vector3f;

/**
 *
 * @author koller
 */
public class PlayerData {
    private String playerID;
    private Vector3f position;
    private Vector3f facingDir;
    private Vector3f movementDir;

    public PlayerData(Vector3f position, Vector3f facingDir, Vector3f movementDir, String playerID) {
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
