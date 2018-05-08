/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameobjects;

import beans.PlayerData;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.List;
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

    public Player(BulletAppState bulletAppState, Camera cam) {
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(player);
        this.cam = cam;
    }

    public void update(float tpf) {

    }

    public void playerControl(Set<String> inputEvents, Camera cam, float tpf) {
        System.out.println(tpf);
        float speedMult = tpf * 30f;
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

    public Enemy shoot(float tpf, List<Enemy> collidingObjects) {
        Enemy closestCollision = null;
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        CollisionResults results = new CollisionResults();
        for (Enemy e : collidingObjects) {
            e.getObject().collideWith(ray, results);
            try {
                System.out.println(results.getClosestCollision().getGeometry().getParent().getName() + "---" + e.getObject().getName());
            } catch (NullPointerException ex) {

            }

            if (results.getClosestCollision() != null
                    && results.getClosestCollision().getGeometry().getParent().getName().equals(e.getObject().getName())) {
                closestCollision = e;
                break;
            }
        }
        System.out.println(player.getPhysicsLocation());
        return closestCollision;
    }

    public CharacterControl getPlayer() {
        return player;
    }
    
    public PlayerData getPlayerData() {
        PlayerData data = new PlayerData(player.getPhysicsLocation(), cam.getDirection(), walkDirection, playerId);
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

}
