/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameobjects;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 *
 * @author koller
 */
public class LogWindowsn {

    private String[] ringPufferLog = new String[4];
    private BitmapText[] ringPuffer = new BitmapText[4];

    private Picture logBG;
    private Node guiNode;
    private BitmapFont boardFont;
    private AppSettings appSetings;
    private AssetManager am;
    private boolean displaying = false;

    private static int VERTICAL_SPACING = 10;
    private static int WIDTH = 300;
    private static int FONT_SIZE = 20;

    public LogWindowsn(Node guiNode, BitmapFont boardFont, AppSettings appSetings, AssetManager am) {
        this.guiNode = guiNode;
        this.boardFont = boardFont;
        this.appSetings = appSetings;
        this.am = am;
        for (int i = 0; i < ringPufferLog.length; i++) {
            ringPufferLog[i] = "";
        }
    }

    public void initLogWindow() {
        logBG = new Picture("HUD Picture");

        logBG.setImage(am, "Textures/logwindousn.png", true);
        logBG.setWidth(WIDTH);
        logBG.setHeight((VERTICAL_SPACING + FONT_SIZE) * ringPufferLog.length);
        logBG.setPosition(appSetings.getWidth() - WIDTH, 0);
        
        for (int i = 0; i < ringPufferLog.length; i++) {
            ringPufferLog[i] = "";
        }
        updateLogMessages();
    }

    public void updateLogMessages() {
        for (int i = 0; i < ringPufferLog.length; i++) {
            if(ringPuffer[i] != null) {
                ringPuffer[i].removeFromParent();
            }
            ringPuffer[i] = createTextNode(ringPufferLog[i], ColorRGBA.White);
            guiNode.attachChild(ringPuffer[i]);
            ringPuffer[i].setLocalTranslation(appSetings.getWidth() - 260, 
                    (i + 1) * (VERTICAL_SPACING + FONT_SIZE), 0);
        }
    }

    private BitmapText createTextNode(String text, ColorRGBA color) {
        BitmapText textNode = new BitmapText(boardFont, false);
        textNode.setSize(boardFont.getCharSet().getRenderedSize());
        textNode.setColor(color);
        textNode.setText(text);
        textNode.setSize(FONT_SIZE);
        textNode.setLocalTranslation(0, 0, 0);
        return textNode;
    }

    public void appendLogMessage(String msg) {
        for (int i = ringPuffer.length - 1; i > 0; --i) {
            ringPufferLog[i] = ringPufferLog[i - 1];
        }
        ringPufferLog[0] = msg;
        updateLogMessages();
    }
    
    public void showLogWindowsn(boolean showlog) {
        if(showlog) {
            guiNode.attachChild(logBG);
            for(BitmapText txt: ringPuffer) {
                guiNode.attachChild(txt);
            }
        } else {
            logBG.removeFromParent();
            for(BitmapText txt: ringPuffer) {
                txt.removeFromParent();
            }
        }
    }

}
