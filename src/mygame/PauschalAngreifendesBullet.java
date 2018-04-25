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
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import interfaces.GameObject;

/**
 *
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

    @Override
    public void update() {
        this.moveDir.subtractLocal(new Vector3f(0, 0.001f, 0));
        object.move(moveDir.normalize().mult(0.3f));
        object.rotate(0.1f, 0.1f, 0.1f);
        ++ticc;
        if (ticc > 10) {
            object.getParent().getChildren().remove(this);
        }
    }

}
