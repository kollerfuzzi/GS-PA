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
 * Class for Managing sound effects and background music
 * @author koller
 */
public class AudioManager {

    private AssetManager am;
    private AudioNode gun;
    private AudioNode horsetechno;

    public AudioManager(AssetManager am) {
        this.am = am;
        init();
    }

    private void init() {
        gun = new AudioNode(am, "Sounds/Gun2.wav", AudioData.DataType.Buffer);
        gun.setPositional(false);
        gun.setLooping(false);
        gun.setVolume(2);

        horsetechno = new AudioNode(am, "Sounds/HorseTechno.wav", AudioData.DataType.Buffer);
        horsetechno.setPositional(false);
        horsetechno.setLooping(false);
        horsetechno.setVolume(2);
    }

    public void attachToRoot(Node root) {
        root.attachChild(gun);
        root.attachChild(horsetechno);
    }

    public void playGunSound() {
        gun.playInstance();
    }

    public void playHorseTechno() {
        horsetechno.play();
    }

    public void stopHorseTechno() {
        horsetechno.stop();
    }
}
