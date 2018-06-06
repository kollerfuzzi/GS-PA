/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

/**
 *
 * @author koller
 */
public class AudioManager {
    
    private AssetManager am;
    private AudioNode gun;
    
    public AudioManager (AssetManager am) {
        this.am = am;
        init();
    }
    
    private void init() {
        gun = new AudioNode(am, "Sounds/Gun2.wav", AudioData.DataType.Buffer);
        gun.setPositional(false);
        gun.setLooping(false);
        gun.setVolume(2);
    }
    
    public void attachToRoot(Node root) {
        root.attachChild(gun);
    }
    
    public void playGunSound() {
        gun.playInstance();
    }
}