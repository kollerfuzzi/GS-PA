/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import gameobjects.GameObject;

/**
 * Class for a bullet in form of a flowerpot (not used in the game anymore)
 * @author koller
 */
public class PauschalAngreifendesBullet extends GameObject {

    Vector3f moveDir;
    int ticc = 0;

    public PauschalAngreifendesBullet(AssetManager am, BulletAppState bas, Vector3f position, Vector3f moveDir) {
        object = am.loadModel("Models/Flower/Flower.j3o");
        object.setLocalScale(0.6f);
        object.setLocalTranslation(position);
        
        CollisionShape geomShape
                = CollisionShapeFactory.createMeshShape(object);
        RigidBodyControl geomRb = new RigidBodyControl(geomShape, 0);
        bas.getPhysicsSpace().add(geomRb);
        
        this.moveDir = moveDir;
    }

    /**
     * Updates the bullet (movement, rotation...)
     * @param tpf ticks per frame
     */
    @Override
    public void update(float tpf) {
        this.moveDir.subtractLocal(new Vector3f(0, 0.001f, 0));
        object.move(moveDir.normalize().mult(0.3f));
        object.rotate(0.1f, 0.1f, 0.1f);
        ++ticc;
        if (ticc > 10) {
            object.getParent().getChildren().remove(this);
        }
    }

}
