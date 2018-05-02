/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import com.jme3.scene.Spatial;

/**
 *
 * @author koller
 */
public abstract class GameObject {
    protected Spatial object;
    
    public Spatial getObject() {
        return object;
    }
    
    public abstract void update(float tpf);
}
