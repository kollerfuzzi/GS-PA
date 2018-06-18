/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameobjects;

import com.jme3.scene.Spatial;

/**
 * Class for a standard GameObject interface
 * @author koller
 */
public abstract class GameObject {
    protected Spatial object;
    
    public Spatial getObject() {
        return object;
    }
    
    public abstract void update(float tpf);
}
