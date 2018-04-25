/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import interfaces.GameObject;

/**
 *
 * @author koller
 */
public class Enemy extends GameObject {
    
    private Vector3f direction;
    private static final float speed = 0.02f;

    public Enemy(AssetManager am, Vector3f position, Vector3f direction) {
        object = am.loadModel("Models/cent/cent.j3o");
        object.setLocalScale(0.3f);
        object.setLocalTranslation(position);
        this.direction = direction;
        this.direction.normalizeLocal().multLocal(speed);
    }

    @Override
    public void update() {
        object.move(direction);
    }
    
}
