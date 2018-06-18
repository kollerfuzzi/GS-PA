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
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 * Gui and Data class for management of the Scoreboard
 *
 * @author koller
 */
public class Scoreboard {

    private Object scoreData[][];
    private final String scoreTitles[] = new String[]{"User", "Kills", "Deaths", "K/D", "Ping"};
    private BitmapText[][] scoreUI;
    private Picture boardBG;
    private Node guiNode;
    private BitmapFont boardFont;
    private AppSettings appSetings;
    private AssetManager am;
    private boolean displaying = false;

    private static int HORIZONZAL_SPACING = 10;
    private static int VERTICAL_SPACING = 10;
    private static int FONT_SIZE = 20;

    public Scoreboard(Node guiNode, BitmapFont boardFont, AppSettings appSetings, AssetManager am) {
        this.guiNode = guiNode;
        this.boardFont = boardFont;
        this.appSetings = appSetings;
        this.am = am;
    }

    /**
     * Sets the received scoreboard data
     *
     * @param scoreData Scoreboard in the normed Zinker-ScoreBoard(tm) format
     */
    public void setScoreData(Object scoreData[][]) {
        this.scoreData = scoreData;
    }

    /**
     * Makes the scoreboard ready to display
     */
    public void generateScoreBoard() {
        if (scoreData == null) {
            System.out.println("missing data for scoreboard!");
            return;
        }
        boardBG = new Picture("HUD Picture");

        scoreUI = new BitmapText[scoreData.length + 1][scoreData[0].length];

        for (int i = 0; i < scoreTitles.length; i++) {
            scoreUI[0][i] = createTextNode(scoreTitles[i], ColorRGBA.Magenta);
        }
        for (int i = 0; i < scoreData.length; ++i) {
            for (int j = 0; j < scoreData[0].length; ++j) {
                scoreUI[i + 1][j] = createTextNode(scoreData[i][j].toString());
            }
        }
        float width = arrangeText();
        float height = (FONT_SIZE + VERTICAL_SPACING) * scoreData.length + 20;

        float space_multiplier = 6;
        boardBG.setImage(am, "Textures/scoreboard.png", true);
        boardBG.setWidth(width + 20 + HORIZONZAL_SPACING * space_multiplier);
        boardBG.setHeight(height + VERTICAL_SPACING * space_multiplier);
        boardBG.setPosition(appSetings.getWidth() - width - HORIZONZAL_SPACING * space_multiplier,
                appSetings.getHeight() - height - VERTICAL_SPACING * space_multiplier);
    }

    /**
     * Aranges the created text components for the scoreboards
     */
    private float arrangeText() {
        float colWidths[] = new float[scoreTitles.length];
        for (int i = 0; i < scoreTitles.length; i++) {
            colWidths[i] = colWidth(i);
        }
        float curXOffset = -xOffset(colWidths);

        // j is row, i is column
        for (int j = 0; j < scoreUI[0].length; j++) {
            for (int i = 0; i < scoreUI.length; i++) {
                scoreUI[i][j].setLocalTranslation(appSetings.getWidth() + curXOffset, appSetings.getHeight() - i * (scoreUI[i][j].getLineHeight() + VERTICAL_SPACING), 1);
            }
            curXOffset += colWidths[j];
        }
        return xOffset(colWidths);
    }

    /**
     * Returns the column width (max element width + HORIZONZAL_SPACING)
     *
     * @param col the column id
     * @return the column width
     */
    private float colWidth(int col) {
        float maxLW = 0;
        for (int i = 0; i < scoreUI.length; i++) {
            float curLW = ((BitmapText) scoreUI[i][col]).getLineWidth();
            if (maxLW < curLW) {
                maxLW = curLW;
            }
        }
        return maxLW + HORIZONZAL_SPACING;
    }

    /**
     * Calculates the x offset based on the column widths
     *
     * @param colWidths
     * @return
     */
    private float xOffset(float colWidths[]) {
        float sum = 0;
        for (int i = 0; i < colWidths.length; i++) {
            sum += colWidths[i];
        }
        return sum;
    }

    /**
     * Creates a bitmap text node with the set font and the given text
     *
     * @param text the text which should be displayed
     */
    private BitmapText createTextNode(String text, ColorRGBA color) {
        BitmapText textNode = new BitmapText(boardFont, false);
        textNode.setSize(boardFont.getCharSet().getRenderedSize());
        textNode.setColor(color);
        textNode.setText(text);
        textNode.setSize(FONT_SIZE);
        textNode.setLocalTranslation(0, 0, 0);
        return textNode;
    }

    private BitmapText createTextNode(String text) {
        return createTextNode(text, ColorRGBA.White);
    }

    /**
     * Shows or hides the scoreboard, depending on the given doShow value
     *
     * @param doShow indicates if the scoreboard should be displayed
     */
    public void showScoreBoard(boolean doShow) {
        if (doShow) {
            guiNode.attachChild(boardBG);
        } else {
            boardBG.removeFromParent();
        }
        displaying = doShow;
        for (Node[] row : scoreUI) {
            for (Node element : row) {
                if (doShow) {
                    guiNode.attachChild(element);
                } else {
                    element.removeFromParent();
                }
            }
        }
    }

    public boolean isDisplaying() {
        return displaying;
    }

    //"User", "Kills", "Deaths", "K/D", "Ping"
    public static Object DEMO_SCOREBOARD[][] = {
        {"Karl", "2", "1", "2", "3ms"},
        {"Heinz", "1", "2", "0.5", "0.001ms"},
        {"Sebastian Kurz", "8", "2", "4", "4ms"}};

    public static Object WAIT_SCOREBOARD[][] = {
        {"Please wait...", " ", " ", " ", " "}};

}
