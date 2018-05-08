package mygame;

import gameobjects.Enemy;
import gameobjects.Player;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.style.BaseStyles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class GSPA extends SimpleApplication implements ActionListener, Receiver {

    private final float movementSpeed = 10;
    private float sinusWaveMovePos = 0;
    private float bulletTimeout = 0;

    //______________________________
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private Player player;
    private Set<String> inputEvents = new HashSet<String>();
    //______________________________

    private List<PauschalAngreifendesBullet> bullets = new ArrayList<>();

    private List<Enemy> enemies = new ArrayList<>();

    public static void main(String[] args) {
        GSPA app = new GSPA();
        AppSettings settings = new AppSettings(false);
        settings.setSettingsDialogImage("Textures/title-banner.png");
        app.settings = settings;
        app.settings.setTitle("GegenSchlägst: pauschal angreifend");
        app.start();
    }

    @Override
    public void simpleInitApp() {
        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);

        // We load the scene from the zip file and adjust its size.
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);
        sceneModel.setLocalTranslation(0, -3, 0);

        player = new Player(bulletAppState, cam);

        initSky();
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(0);

        initMenu();
    }

    @Override
    public void simpleUpdate(float tpf) {
        player.playerControl(inputEvents, cam, tpf);
        if (inputEvents.contains("Shoot") && bulletTimeout < 0) {
            Enemy hit = player.shoot(tpf, enemies);
            if (hit != null) {
                hit.die();
            }
            bulletTimeout = 30;
        }

        bulletTimeout -= 60 * tpf;

        for (PauschalAngreifendesBullet b : bullets) {
            b.update(tpf);
        }
        for (Enemy e : enemies) {
            e.update(tpf);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        System.out.println(name + ";" + isPressed + ";" + tpf);
        if (isPressed) {
            inputEvents.add(name);
        } else {
            inputEvents.remove(name);
        }
    }

    public void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump", "Sprint", "Shoot");
    }

    public void initSky() {
        Texture skBK = assetManager.loadTexture("Textures/mp_deception/deception_pass_bk.tga");
        Texture skDN = assetManager.loadTexture("Textures/mp_deception/deception_pass_dn.tga");
        Texture skFT = assetManager.loadTexture("Textures/mp_deception/deception_pass_ft.tga");
        Texture skLF = assetManager.loadTexture("Textures/mp_deception/deception_pass_lf.tga");
        Texture skRT = assetManager.loadTexture("Textures/mp_deception/deception_pass_rt.tga");
        Texture skUP = assetManager.loadTexture("Textures/mp_deception/deception_pass_up.tga");
        Spatial sky = SkyFactory.createSky(assetManager, skRT, skLF, skFT, skBK, skUP, skDN);
        rootNode.attachChild(sky);
    }

    private void initLights() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, -0.5f, 1).normalizeLocal());
        sun.setColor(new ColorRGBA(1, 1, 1, 0.4f));
        rootNode.addLight(sun);

        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(-1, -0.5f, -1).normalizeLocal());
        sun2.setColor(ColorRGBA.White);
        rootNode.addLight(sun2);
    }

    private void initCrossHair() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    private void generateEnemies() {
        Random r = new Random();
        for (int i = 0; i < 10; ++i) {
            float dist = r.nextFloat() * 100 - 50;
            float dirX = r.nextFloat();
            float dirZ = r.nextFloat();
            Enemy enemy = new Enemy(assetManager, new Vector3f(10, -3, dist), new Vector3f(dirX, 0, dirZ));
            rootNode.attachChild(enemy.getObject());
            enemy.getObject().setName("ENEMY_" + i);
            enemies.add(enemy);
        }
    }

    public void initMap() {
        // We load the scene from the zip file and adjust its size.
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);
        sceneModel.setLocalTranslation(0, -3, 0);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape
                = CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
        rootNode.attachChild(sceneModel);
    }

    private BitmapText helloText;

    private void initDebugText() {
        /**
         * Write text on the screen (HUD)
         */
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        helloText = new BitmapText(guiFont, false);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Hello World");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);

    }

    private void changeText(String text) {
        helloText.setText("BulletTimeout: " + text);
    }

    public void initMenu() {
        BitmapFont comicSans = assetManager.loadFont("Interface/Fonts/Ubuntu.fnt");

        // Initialize the globals access so that the defualt
        // components can find what they need.
        GuiGlobals.initialize(this);

        // Load the 'glass' style
        BaseStyles.loadGlassStyle();

        // Set 'glass' as the default style when not specified
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Create a simple container for our elements
        final Container myWindow = new Container();
        guiNode.attachChild(myWindow);

        // Put it somewhere that we will see it
        // Note: Lemur GUI elements grow down from the upper left corner.
        myWindow.setLocalTranslation(200, settings.getHeight() - 200, 0);

        // Add some elements
        Label title = new Label("GegenSchlägst: pauschal angreifend");
        title.setFont(comicSans);
        title.setFontSize(30f);
        myWindow.addChild(title);
        Label subtitle = new Label("Hauptmenü");
        subtitle.setFont(comicSans);
        subtitle.setFontSize(55f);
        myWindow.addChild(subtitle);

        Label username = new Label("Username:");
        username.setFont(comicSans);
        username.setFontSize(20f);
        myWindow.addChild(username);

        TextField usernameTf = new TextField("");
        usernameTf.setFont(comicSans);
        usernameTf.setFontSize(28f);
        myWindow.addChild(usernameTf);

        Label ipAddr = new Label("Server IP:");
        ipAddr.setFont(comicSans);
        ipAddr.setFontSize(20f);
        myWindow.addChild(ipAddr);

        TextField ipAddrTf = new TextField("");
        ipAddrTf.setFont(comicSans);
        ipAddrTf.setFontSize(28f);
        myWindow.addChild(ipAddrTf);

        Button startGame = myWindow.addChild(new Button("Start Game"));
        startGame.setFont(comicSans);
        startGame.setFontSize(25f);
        startGame.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                myWindow.removeFromParent();
                inputManager.setCursorVisible(false);
                initKeys();
                initLights();
                initCrossHair();
                initMap();
                generateEnemies();
                player.getPlayer().setPhysicsLocation(new Vector3f(0, 10, 0));
                flyCam.setRotationSpeed(1);
            }
        });
    }

    @Override
    public void receive(Object obj) {

    }
}
