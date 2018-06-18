package mygame;

import beans.Damage;
import beans.PlayerData;
import beans.PlayerStatus;
import gameobjects.Enemy;
import gameobjects.Player;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
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
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.style.BaseStyles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class GSPA extends SimpleApplication implements ActionListener, Receiver {

    private float bulletTimeout = 0;

    //______________________________
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private Player player;
    private AudioManager audio;
    private Spatial weapon;
    private Set<String> inputEvents = new HashSet<String>();
    private int showBlood = 0;
    //______________________________
    //MENU
    private BitmapFont ubuntu;
    private Container menuWindow;
    private List<String> teams = new ArrayList<>();

    private List<Enemy> enemies = new ArrayList<>();

    private Client client;
    private final GSPA gegenschlaegst = this;

    private boolean gameRunning = false;

    public final Map<String, PlayerData> players = new HashMap<>();
    public final Map<String, Spatial> playerSpatials = new HashMap<>();

    //HUD
    private Picture hudBlood;
    private BitmapText hudHealth;
    private BitmapText hudArmor;
    private BitmapText hudStatus;
    private Picture hudKilled;
    private BitmapText hudKilledTxt;

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
        setDisplayStatView(false);
        setDisplayFps(false);

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);

        audio = new AudioManager(assetManager);
        audio.attachToRoot(rootNode);

        player = new Player(bulletAppState, cam, audio);

        ubuntu = assetManager.loadFont("Interface/Fonts/Ubuntu.fnt");

        initMenu();
        initMap();
        initLights();
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(0);
        audio.playHorseTechno();

        hudKilled = new Picture("HUD Picture");
        hudKilled.setImage(assetManager, "Textures/transparent_black.png", true);
        hudKilled.setWidth(settings.getWidth());
        hudKilled.setHeight(settings.getHeight());
        hudKilled.setPosition(0, 0);

        hudStatus = new BitmapText(ubuntu, false);
        hudStatus.setColor(new ColorRGBA(0.6f, 0.1f, 0.1f, 1));                             // font color
        hudStatus.setSize(30);
        guiNode.attachChild(hudStatus);

        hudKilledTxt = new BitmapText(ubuntu, false);
        hudKilledTxt.setColor(new ColorRGBA(1, 1, 1, 1));                             // font color
        hudKilledTxt.setSize(80);
        hudKilledTxt.setText("");
        hudKilledTxt.setLocalTranslation(100, settings.getHeight() - 100, 10);
        guiNode.attachChild(hudKilledTxt);

        //player.getPlayer().setPhysicsLocation(new Vector3f(-100, -100, -100));
        //guiNode.attachChild(hudKilled);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (teams.size() != 0 && !gameRunning) {
            teamMenu(teams);
            teams.clear();
        }

        if (!gameRunning) {
            sceneModel.rotate(0.0004f, 0.0005f, 0.0f);
            return;
        }

        player.playerControl(inputEvents, cam, tpf);

        CharacterControl cc = player.getPlayer();
        Vector3f pLoc = cc.getPhysicsLocation();
        pLoc.addLocal(cam.getDirection().normalize().mult(2.5f));
        weapon.setLocalTranslation(pLoc.subtract(cam.getLeft().normalize().mult(1.3f)).subtract(0, 0.5f, 0));
        weapon.setLocalRotation(cam.getRotation());
        weapon.rotate(0, FastMath.DEG_TO_RAD * 90, 0);

        if (inputEvents.contains("Shoot") && bulletTimeout < 0) {
            String hit = player.shoot(tpf, playerSpatials);
            if (hit != null) {
                client.send(new Damage(hit, 2));
            } else {
                client.send(new Damage("noone", 0));
            }
            bulletTimeout = 30;
        }

        bulletTimeout -= 60 * tpf;

        for (Enemy e : enemies) {
            e.update(tpf);
        }
        if (client != null) {
            client.send(player.getPlayerData());
        }
        synchronized (players) {
            updatePlayerObjects();
        }

        if (showBlood == 0) {
            showblood(false);
        } else if (showBlood == 6) {
            showblood(true);
        }
        --showBlood;

        if (player.getHealth() <= 0) {
            killmyself();
        }

        if (player.getPlayerData().getPosition().y < -150) {
            killmyself();
        }

        hudHealth.setText("Health: " + player.getHealth() + "/10");
    }

    int countdown;
    public void killmyself() {
        player.setHealth(0);
        client.send(new PlayerStatus(player.getPlayerId(), null, PlayerStatus.Type.DEAD));
        guiNode.attachChild(hudKilled);
        gameRunning = false;
        audio.playHorseTechno();
        weapon.removeFromParent();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                countdown = 5;
                do {
                    guiNode.attachChild(hudKilledTxt);
                    gegenschlaegst.enqueue(new Runnable() {
                        @Override
                        public void run() {
                            hudKilledTxt.setText("Respawn in " + countdown + " seconds");
                        }
                    });
                    try {
                        Thread.sleep(1000l);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GSPA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println(countdown);
                } while (--countdown >= 0);
                gegenschlaegst.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        hudKilledTxt.removeFromParent();
                    }
                });
                gegenschlaegst.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        rootNode.attachChild(weapon);
                        player.setHealth(10);
                        sceneModel.setLocalRotation(Matrix3f.IDENTITY);
                        hudKilled.removeFromParent();
                        player.getPlayer().setPhysicsLocation(new Vector3f(0, 10, 0));
                        audio.stopHorseTechno();
                        gameRunning = true;
                    }
                });
            }
        });
        t.start();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
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
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    public void initMap() {
        // We load the scene from the zip file and adjust its size.
        //assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = assetManager.loadModel("Models/scifitown/scifi dowtown scenery.j3o");
        sceneModel.setLocalScale(0.2f);

        sceneModel.setLocalTranslation(0, -100, 0);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape
                = CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
        rootNode.attachChild(sceneModel);
    }

    public void initWeapon() {
        weapon = assetManager.loadModel("Models/Beretta/Beretta.j3o");
        weapon.setLocalScale(1.5f);
        weapon.setLocalTranslation(new Vector3f(2, 2, 2));
        rootNode.attachChild(weapon);
    }

    public void initMenu() {
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        menuWindow = new Container();
        guiNode.attachChild(menuWindow);
        menuWindow.setLocalTranslation(200, settings.getHeight() - 200, 0);

        Label title = new Label("");
        title.setBackground(new IconComponent("Textures/menu-banner.png"));
        menuWindow.addChild(title);

        //FIRST WINDOW
        final Label lbConnect = menuWindow.addChild(new Label("Server Connection"));
        lbConnect.setFont(ubuntu);
        lbConnect.setFontSize(30f);
        lbConnect.setColor(ColorRGBA.White);

        final Label username = new Label("Username:");
        username.setFont(ubuntu);
        username.setFontSize(20f);
        menuWindow.addChild(username);

        final TextField usernameTf = new TextField("");
        usernameTf.setFont(ubuntu);
        usernameTf.setFontSize(28f);
        menuWindow.addChild(usernameTf);

        final Label ipAddr = new Label("Server IP:");
        ipAddr.setFont(ubuntu);
        ipAddr.setFontSize(20f);
        menuWindow.addChild(ipAddr);

        final TextField ipAddrTf = new TextField("localhost");
        ipAddrTf.setFont(ubuntu);
        ipAddrTf.setFontSize(28f);
        menuWindow.addChild(ipAddrTf);

        final Button startGame = menuWindow.addChild(new Button("Start Game"));
        startGame.setFont(ubuntu);
        startGame.setFontSize(25f);

        startGame.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                String usr = usernameTf.getText();
                String ip = ipAddrTf.getText();
                player.setPlayerId(usr);
                client = new Client(gegenschlaegst);
                try {
                    client.connect(ip);
                    client.send(new PlayerStatus(player.getPlayerId(), null, PlayerStatus.Type.LOGGED_IN));
                    client.send("TEAMS");
                    setStatusText("Connceted to " + ip);

                    menuWindow.removeChild(lbConnect);
                    menuWindow.removeChild(username);
                    menuWindow.removeChild(usernameTf);
                    menuWindow.removeChild(ipAddr);
                    menuWindow.removeChild(ipAddrTf);
                    menuWindow.removeChild(startGame);
                } catch (IOException ex) {
                    Logger.getLogger(GSPA.class.getName()).log(Level.SEVERE, null, ex);
                    setStatusText("Connection Refused");
                    return;
                }
            }
        });
    }

    List<Button> teamButtons = new ArrayList<>();

    public void teamMenu(List<String> teams) {
        System.out.println("ou yesse!!!");
        final Label lbTeam = menuWindow.addChild(new Label("Team Selection"));
        lbTeam.setFont(ubuntu);
        lbTeam.setFontSize(30f);
        lbTeam.setColor(ColorRGBA.White);
        for (final String team : teams) {
            Button selectTeam = menuWindow.addChild(new Button(" Join \"" + team + "\""));
            selectTeam.setName(team);
            selectTeam.setFont(ubuntu);
            selectTeam.setFontSize(25f);
            selectTeam.addClickCommands(new Command<Button>() {
                @Override
                public void execute(Button s) {
                    lbTeam.removeFromParent();
                    inputManager.setCursorVisible(false);
                    //set team
                    player.setTeam(team);
                    //remove buttons
                    for (Button btn : teamButtons) {
                        menuWindow.removeChild(btn);
                    }
                    menuWindow.addChild(new Label("Loading..."));
                    //Start game
                    audio.stopHorseTechno();
                    initKeys();
                    initCrossHair();
                    initWeapon();
                    initHUD();
                    player.getPlayer().setPhysicsLocation(new Vector3f(0, 10, 0));
                    flyCam.setRotationSpeed(1);
                    gameRunning = true;
                    sceneModel.setLocalRotation(Matrix3f.IDENTITY);
                    menuWindow.removeFromParent();
                }
            });
            teamButtons.add(selectTeam);
            menuWindow.addChild(selectTeam);
        }
    }

    @Override
    public void receive(Object obj) {
        if (obj instanceof List) {
            List objects = (List) obj;
            if (objects.size() == 0) {
                System.out.println("Empty list received");
                return;
            }
            if (objects.get(0) instanceof PlayerData) {

            }
            if (objects.get(0) instanceof String) { //Teams List
                teams = (List<String>) obj;
                System.out.println("Reams receivesd");
            }
        }
        if (obj instanceof PlayerData) {
            PlayerData pd = (PlayerData) obj;
            synchronized (players) {
                if (!player.getPlayerId().equals(pd.getPlayerID())) {
                    players.put(pd.getPlayerID(), pd);
                }
            }
        }
        if (obj instanceof Damage) {
            Damage dmg = (Damage) obj;
            if (dmg.getPlayer().equals(player.getPlayerId())) {
                player.setHealth(player.getHealth() - dmg.getDamageValue());
                showBlood = 6;
            }
            this.enqueue(new Callable<Integer>() {
                public Integer call() throws Exception {
                    audio.playGunSound();
                    return 0;
                }

            });
        }

        if (obj instanceof PlayerStatus) {
            final String player = ((PlayerStatus) obj).getPlayerID();
            this.enqueue(new Callable<Integer>() {
                public Integer call() throws Exception {
                    Spatial killedOne = playerSpatials.get(player);
                    playerSpatials.put("", weapon);
                    Spatial rip = assetManager.loadModel("Models/Tombstone_RIP_/Tombstone_RIP_obj.j3o");
                    rip.setLocalScale(1f);
                    rip.setLocalTranslation(killedOne.getLocalTranslation());
                    rootNode.attachChild(rip);
                    playerSpatials.remove(player);
                    killedOne.removeFromParent();
                    return 0;
                }

            });
        }

    }

    public void updatePlayerObjects() {
        synchronized (players) {
            for (String pl : players.keySet()) {
                PlayerData data = players.get(pl);

                if (playerSpatials.containsKey(pl)) {
                    Spatial gameObj = playerSpatials.get(pl);
                    System.out.println(data.getFacingDir().negate());
                    Vector3f realObjectPos = data.getPosition().subtract(0, 5, 0);
                    gameObj.setLocalTranslation(realObjectPos);
                    gameObj.lookAt(realObjectPos.add(data.getFacingDir()), Vector3f.UNIT_Y);

                } else {
                    Spatial gameObj = assetManager.loadModel("Models/cent/cent.j3o");
                    gameObj.setLocalScale(0.3f);
                    gameObj.setLocalTranslation(data.getPosition());
                    rootNode.attachChild(gameObj);
                    playerSpatials.put(pl, gameObj);
                }
            }
        }
    }

    public void showblood(boolean show) {
        if (show) {
            hudBlood.setImage(assetManager, "Textures/blood.png", true);
        } else {
            hudBlood.setImage(assetManager, "Textures/noblood.png", true);
        }
    }

    public void initHUD() {
        hudBlood = new Picture("HUD Picture");
        hudBlood.setImage(assetManager, "Textures/noblood.png", true);
        hudBlood.setWidth(settings.getWidth());
        hudBlood.setHeight(settings.getHeight());
        hudBlood.setPosition(0, 0);
        guiNode.attachChild(hudBlood);

        Picture pic = new Picture("HUD Picture");
        pic.setImage(assetManager, "Textures/hud.png", true);
        pic.setWidth(320);
        pic.setHeight(140);
        pic.setPosition(0, 0);
        guiNode.attachChild(pic);

        BitmapText name = new BitmapText(ubuntu, false);
        name.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        name.setColor(ColorRGBA.White);                             // font color
        name.setText(player.getPlayerId());             // the text
        name.setSize(24);
        name.setLocalTranslation(20, name.getLineHeight() + 80, 0); // position
        guiNode.attachChild(name);

        BitmapText team = new BitmapText(ubuntu, false);
        team.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        team.setColor(ColorRGBA.Yellow);                             // font color
        team.setText(player.getTeam());             // the text
        team.setSize(24);
        team.setLocalTranslation(40 + name.getLineWidth(), name.getLineHeight() + 80, 0); // position
        guiNode.attachChild(team);

        hudHealth = new BitmapText(ubuntu, false);
        hudHealth.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudHealth.setColor(new ColorRGBA(1, 0.8f, 0.8f, 1));                             // font color
        hudHealth.setText("Health: 10/10");             // the text
        hudHealth.setSize(30);
        hudHealth.setLocalTranslation(20, hudHealth.getLineHeight() + 50, 0); // position
        guiNode.attachChild(hudHealth);

        hudArmor = new BitmapText(ubuntu, false);
        hudArmor.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudArmor.setColor(new ColorRGBA(0.8f, 0.8f, 1, 1));                             // font color
        hudArmor.setText("Teflon: 0/10");             // the text
        hudArmor.setSize(30);
        hudArmor.setLocalTranslation(20, hudArmor.getLineHeight() + 20, 0); // position
        guiNode.attachChild(hudArmor);

    }

    public void setStatusText(String statusTxt) {
        hudStatus.setText(statusTxt);             // the text
        hudStatus.setLocalTranslation(settings.getWidth() - hudStatus.getLineWidth() - 50, settings.getHeight() - 50, 0); // position
    }

    @Override
    public void destroy() {
        super.destroy();
        if (client != null) {
            client.disconnect();
        }
        System.exit(0);
    }

}
