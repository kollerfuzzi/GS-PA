/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameobjects;

import mygame.AudioManager;
import beans.PlayerData;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author koller
 */
public class Player {

    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private Camera cam;
    private String playerId;
    private String team;
    private AudioManager sound;
    private int health = 10;

    public Player(BulletAppState bulletAppState, Camera cam, AudioManager sound) {
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 3f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(player);
        this.cam = cam;
        this.sound = sound;
    }

    public void update(float tpf) {

    }

    public void playerControl(Set<String> inputEvents, Camera cam, float tpf) {
        float speedMult = 0.3f; // 1, 0.5
        walkDirection.set(0, 0, 0);
        Vector3f camDir = cam.getDirection();
        camDir.setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft();
        camLeft.setY(0).normalizeLocal();
        if (inputEvents.contains("Up")) {
            walkDirection.addLocal(camDir);
        }
        if (inputEvents.contains("Down")) {
            walkDirection.addLocal(camDir.negate());
        }
        if (inputEvents.contains("Left")) {
            walkDirection.addLocal(camLeft);
        }
        if (inputEvents.contains("Right")) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (inputEvents.contains("Jump")) {
            player.jump(new Vector3f(0, 5, 0));
        }
        if (inputEvents.contains("Sprint")) {
            speedMult *= 2;
        }
        player.setWalkDirection(walkDirection.normalize().mult(speedMult));
        cam.setLocation(player.getPhysicsLocation());
    }

    public String shoot(float tpf, Map<String, Spatial> otherPlayers) {
        sound.playGunSound();
        Spatial closestCollision = null;
        String closestCollisionPlayer = null;
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        
        CollisionResults results = new CollisionResults();
        for (String player  : otherPlayers.keySet()) {
            Spatial s = otherPlayers.get(player);
            s.collideWith(ray, results);
            if (results.getClosestCollision() != null
                    && results.getClosestCollision().getGeometry().getParent().getName().equals(s.getName())) {
                closestCollision = s;
                closestCollisionPlayer = player;
                break;
            }
        }
        return closestCollisionPlayer;
    }

    public CharacterControl getPlayer() {
        return player;
    }
    
    public PlayerData getPlayerData() {
        System.out.println(cam.getDirection());
        PlayerData data = new PlayerData(playerId, team, player.getPhysicsLocation(), cam.getDirection(), walkDirection);
        return data;
    }

    public void setPlayer(CharacterControl player) {
        this.player = player;
    }

    public Vector3f getWalkDirection() {
        return walkDirection;
    }

    public void setWalkDirection(Vector3f walkDirection) {
        this.walkDirection = walkDirection;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
    
    

}
