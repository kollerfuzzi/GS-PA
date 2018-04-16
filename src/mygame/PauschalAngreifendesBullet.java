/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;

/**
 *
 * @author koller
 */
public class PauschalAngreifendesBullet extends Geometry {

    Vector3f moveDir;
    int ticc = 0;

    public PauschalAngreifendesBullet(AssetManager am, Vector3f position, Vector3f moveDir) {
        //super("Box", new Box(0.1f, 0.1f, 0.1f));
        super("Box", new Box(0.2f, 0.2f, 0.2f));
        this.move(position);
        this.moveDir = moveDir;

        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        this.setMaterial(mat);
    }

    public void positionUpdate() {
        this.moveDir.subtractLocal(new Vector3f(0, 0.001f, 0));
        this.move(moveDir.normalize().mult(30));
        ++ticc;
        if(ticc > 10) {
            this.getParent().getChildren().remove(this);
        }
    }

}
