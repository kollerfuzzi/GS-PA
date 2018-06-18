package mygame;

import beans.Damage;
import beans.PlayerData;
import beans.PlayerStatus;
import beans.RoundInfo;
import beans.RoundTime;
import gameobjects.Scoreboard;
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
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
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
import gameobjects.LogWindowsn;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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

    //GameObjects
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private Player player;
    private AudioManager audio;
    private Spatial weapon;
    private Set<String> inputEvents = new HashSet<String>();
    private int showBlood = 0;

    //Menu
    private BitmapFont ubuntu;
    private Container menuWindow;
    private List<String> teams = new ArrayList<>();

    //server connection
    private Client client;

    //this for inner classes
    private final GSPA gegenschlaegst = this;

    //game running?
    private boolean gameRunning = false;

    public final Map<String, PlayerData> players = new HashMap<>();
    public final Map<String, Spatial> playerSpatials = new HashMap<>();

    //HUD
    private Picture hudBlood;
    private BitmapText hudHealth;
    private BitmapText hudArmor;
    private Picture hudKilled;
    private BitmapText hudKilledTxt;

    private Scoreboard scboard;
    private LogWindowsn logWin;

    /**
     * Main method of the Game Sets the title dialog and its settings, to start
     * the game
     *
     * @param args
     */
    public static void main(String[] args) {
        GSPA app = new GSPA();
        AppSettings settings = new AppSettings(false);
        settings.setSettingsDialogImage("Textures/title-banner.png");
        app.settings = settings;
        app.settings.setTitle("GegenSchlägst: pauschal angreifend");
        app.start();
    }

    /**
     * Method which is called when the user starts the game. It is called when
     * the user starts the game via the dialog.
     */
    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

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

        //player.getPlayer().setPhysicsLocation(new Vector3f(-100, -100, -100));
        //guiNode.attachChild(hudKilled);
        scboard = new Scoreboard(guiNode, ubuntu, settings, assetManager);
        scboard.setScoreData(Scoreboard.WAIT_SCOREBOARD);
        scboard.generateScoreBoard();

        logWin = new LogWindowsn(guiNode, guiFont, settings, assetManager);
        logWin.initLogWindow();
        logWin.showLogWindowsn(true);
    }

    /**
     * Update method of the game, frequency depends on the system performance.
     *
     * @param tpf time passed since the last simpleUpdate call in seconds
     */
    @Override
    public void simpleUpdate(float tpf) {
        //if the game is not running, the map rotates
        if (!gameRunning) {
            sceneModel.rotate(0.0004f, 0.0005f, 0.0f);
            return;
        }

        //method called for player movement
        player.playerControl(inputEvents, cam, tpf);

        //sets the weapons rotation to display it in front of the player
        CharacterControl cc = player.getPlayer();
        Vector3f pLoc = cc.getPhysicsLocation();
        pLoc.addLocal(cam.getDirection().normalize().mult(2.5f));
        weapon.setLocalTranslation(pLoc.subtract(cam.getLeft().normalize().mult(1.3f)).subtract(0, 0.5f, 0));
        weapon.setLocalRotation(cam.getRotation());
        weapon.rotate(0, FastMath.DEG_TO_RAD * 90, 0);

        //decreases the bullet timeout
        bulletTimeout -= 60 * tpf;
        //shoots if "Shoot" event occurs and the bullettimeout is less than 0
        if (inputEvents.contains("Shoot") && bulletTimeout < 0) {
            String hit = player.shoot(tpf, playerSpatials);
            if (hit != null) {
                client.send(new Damage(hit, 2));
            } else {
                client.send(new Damage("noone", 0));
            }
            bulletTimeout = 30;
        }

        if (client != null) {
            client.send(player.getPlayerData());
        }

        if (inputEvents.contains("Scoreboard") && !scboard.isDisplaying()) {
            scboard.generateScoreBoard();
            client.send("gimmiscoreboardnowwwww");
            scboard.showScoreBoard(true);
        } else if (!inputEvents.contains("Scoreboard") && scboard.isDisplaying()) {
            scboard.showScoreBoard(false);
        }

        synchronized (players) {
            updatePlayerObjects();
        }

        //shows the blood overlay if the player gets shoot
        if (showBlood == 0) {
            showblood(false);
        } else if (showBlood == 6) {
            showblood(true);
        }
        --showBlood;

        //if the player's health is less or equal than 0, he dies
        if (player.getHealth() <= 0) {
            client.send(new PlayerStatus(player.getPlayerId(), null, PlayerStatus.Type.DEAD));
            killmyself(KillType.KILL);
        }

        //if the player falls to far (y -150), he dies
        if (player.getPlayerData().getPosition().y < -150) {
            client.send(new PlayerStatus(player.getPlayerId(), null, PlayerStatus.Type.KILLEDHIMSELF));
            killmyself(KillType.ROUNDOVER);
        }

        //displays the current player health
        hudHealth.setText("Health: " + player.getHealth() + "/10");
    }

    int countdown;

    /**
     * Called when the player kills him self, or gets killed by damage of
     * another player. It tells the server that the player died, and respawns
     * the player in the next 5 seconds.
     */
    public enum KillType {
        KILL, ROUNDOVER
    }

    public void killmyself(KillType type) {
        //stop game
        player.setHealth(0);
        guiNode.attachChild(hudKilled);
        gameRunning = false;
        audio.playHorseTechno();
        switch (type) {
            case KILL:
                hudKilled.setImage(assetManager, "Textures/transparent_black.png", true);
                break;
            case ROUNDOVER:
                hudKilled.setImage(assetManager, "Textures/transparent_over.png", true);
                break;
        }
        weapon.removeFromParent();
        client.send("gimmiscoreboardnowwwww");
        scboard.showScoreBoard(true);
        hudKilledTxt.setText("Round will be restarted by server");
        //thread to restart game in 5 seconds, and display a countdown to the player
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                countdown = 5;
                //Show countdown
                gegenschlaegst.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        guiNode.attachChild(hudKilledTxt);
                    }
                });
                do {
                    //update countdown
                    gegenschlaegst.enqueue(new Runnable() {
                        @Override
                        public void run() {
                            hudKilledTxt.setText("Respawn in " + countdown + " seconds");
                        }
                    });

                    //sleep 1 second
                    try {
                        Thread.sleep(1000l);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GSPA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while (--countdown >= 0);
                //restarts the game and removes the hudKilled overlay
                gegenschlaegst.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        rootNode.attachChild(weapon);
                        player.setHealth(10);
                        sceneModel.setLocalRotation(Matrix3f.IDENTITY);
                        hudKilledTxt.removeFromParent();
                        hudKilled.removeFromParent();
                        player.getPlayer().setPhysicsLocation(new Vector3f(0, 10, 0));
                        audio.stopHorseTechno();
                        gameRunning = true;
                    }
                });
            }
        });
        if (type.equals(KillType.KILL)) {
            t.start();
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    /**
     * called if an input event occurs, and adds or removes the event from
     * inputEvents
     *
     * @param name name of the event (Left, Right, Shoot, Jump...)
     * @param isPressed indicates if the button of the event is pressed
     * @param tpf ticks per frame
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            inputEvents.add(name);
        } else {
            inputEvents.remove(name);
        }
    }

    /**
     * Maps the event names to the keys and to the eventlistener
     */
    public void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Scoreboard", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump", "Sprint", "Shoot", "Scoreboard");
    }

    /**
     * Initializes the skybox
     */
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

    /**
     * Initializes the sunlight on the map
     */
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

    /**
     * Adds the crosshair to the screen
     */
    private void initCrossHair() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    /**
     * Loads, scales, and adds the map to the scene
     */
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

    /**
     * Adds the player's weapon to the scene
     */
    public void initWeapon() {
        weapon = assetManager.loadModel("Models/Beretta/Beretta.j3o");
        weapon.setLocalScale(1.5f);
        weapon.setLocalTranslation(new Vector3f(2, 2, 2));
        rootNode.attachChild(weapon);
    }

    /**
     * Initializes the Main Menu, with the logo and username/ip fields
     */
    public void initMenu() {
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        //creates a new gui container
        menuWindow = new Container();
        guiNode.attachChild(menuWindow);
        menuWindow.setLocalTranslation(200, settings.getHeight() - 200, 0);

        //adds the GSPA logo
        Label title = new Label("");
        title.setBackground(new IconComponent("Textures/menu-banner.png"));
        menuWindow.addChild(title);

        //Adds connection text fields and labels (and connect button)
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

        //defines what happens on connect button press
        startGame.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                //gets the values from the textfields (and if not shows 'connection refused')
                String usr = usernameTf.getText();
                String ip = ipAddrTf.getText();

                //sets the player's name
                player.setPlayerId(usr);

                //tries to establish a connection to 
                client = new Client(gegenschlaegst);
                try {
                    client.connect(ip);
                    client.send(new PlayerStatus(player.getPlayerId(), null, PlayerStatus.Type.LOGGED_IN));
                    client.send("TEAMS");
                    appendLogMessage("Connceted to " + ip);

                    menuWindow.removeChild(lbConnect);
                    menuWindow.removeChild(username);
                    menuWindow.removeChild(usernameTf);
                    menuWindow.removeChild(ipAddr);
                    menuWindow.removeChild(ipAddrTf);
                    menuWindow.removeChild(startGame);
                } catch (IOException ex) {
                    Logger.getLogger(GSPA.class.getName()).log(Level.SEVERE, null, ex);
                    appendLogMessage("Connection Refused");
                    return;
                }
            }
        });
    }

    List<Button> teamButtons = new ArrayList<>();

    /**
     * Displays the teamMenu if the server sends the teamList. When the player
     * chooses a team, the menu is closed and the game starts.
     *
     * @param teams the list of teams
     */
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

    /**
     * Gets called when the server sends a message to the client, and reacts to
     * the message.
     *
     * @param obj
     */
    @Override
    public void receive(Object obj) {
        //When any type of list is received
        if (obj instanceof List) {
            List objects = (List) obj;
            if (objects.size() == 0) {
                System.out.println("Empty list received");
                return;
            }
            if (objects.get(0) instanceof PlayerData) {

            }

            //calls the teamMenu when the teamList is received
            if (objects.get(0) instanceof String) { //Teams List
                teams = (List<String>) obj;
                System.out.println("Teams Received");
                gegenschlaegst.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        teamMenu(teams);
                    }
                });
            }
        }

        //Updates a player if his playerData is received
        if (obj instanceof PlayerData) {
            PlayerData pd = (PlayerData) obj;
            synchronized (players) {
                if (!player.getPlayerId().equals(pd.getPlayerID())) {
                    players.put(pd.getPlayerID(), pd);
                }
            }
        }

        //If damage is received and the destination is the own player, the 
        //  player is damaged and shown a red screen
        //A gun sound is played on receive
        if (obj instanceof Damage) {
            Damage dmg = (Damage) obj;
            if (dmg.getPlayer().equals(player.getPlayerId())) {
                player.setHealth(player.getHealth() - dmg.getDamageValue());
                showBlood = 6;
            }
            this.enqueue(new Callable<Integer>() {
                public Integer call() throws Exception {
                    gegenschlaegst.enqueue(new Runnable() {
                        @Override
                        public void run() {
                            audio.playGunSound();
                        }
                    });
                    return 0;
                }

            });
        }

        //If a playerstatus of type death or killedhimself is received
        // a tombstone is placed on the player's position.
        if (obj instanceof PlayerStatus) {
            final PlayerStatus.Type status = ((PlayerStatus) obj).getType();
            final String player = ((PlayerStatus) obj).getPlayerID();

            if (status.equals(PlayerStatus.Type.DEAD) || status.equals(PlayerStatus.Type.KILLEDHIMSELF)) {
                this.enqueue(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        Spatial killedOne = playerSpatials.get(player);
                        if (killedOne == null) {
                            return 0;
                        }
                        playerSpatials.put("", weapon);
                        Spatial rip = assetManager.loadModel(
                                "Models/Tombstone_RIP_/Tombstone_RIP_obj.j3o");
                        rip.setLocalScale(1f);
                        rip.setLocalTranslation(killedOne.getLocalTranslation());
                        rootNode.attachChild(rip);
                        playerSpatials.get(player).removeFromParent();
                        playerSpatials.remove(player);
                        killedOne.removeFromParent();
                        return 0;
                    }

                });
            }
        }

        if (obj instanceof String) {
            final String receivedMsg = (String) obj;
            gegenschlaegst.enqueue(new Runnable() {
                @Override
                public void run() {
                    appendLogMessage(receivedMsg);
                }
            });
            System.out.println("Status received: " + receivedMsg);
        }

        if (obj instanceof Object[][]) {
            final Object scoreData[][] = (Object[][]) obj;
            gegenschlaegst.enqueue(new Runnable() {
                @Override
                public void run() {
                    scboard.setScoreData(scoreData);
                    scboard.generateScoreBoard();
                }
            });

        }

        if (obj instanceof RoundTime) {
            final RoundTime info = (RoundTime) obj;
            gegenschlaegst.enqueue(new Runnable() {
                @Override
                public void run() {
                    appendLogMessage("Time left: " + info.getTimeLeft());
                }
            });
            switch (info.getType()) {
                case STARTED:
                    if (!gameRunning) {
                        gegenschlaegst.enqueue(new Runnable() {
                            @Override
                            public void run() {
                                rootNode.attachChild(weapon);
                                player.setHealth(10);
                                sceneModel.setLocalRotation(Matrix3f.IDENTITY);
                                hudKilledTxt.removeFromParent();
                                hudKilled.removeFromParent();
                                player.getPlayer().setPhysicsLocation(new Vector3f(0, 10, 0));
                                audio.stopHorseTechno();
                                gameRunning = true;
                            }
                        });
                    }
                    break;

                case STOPPED:
                    killmyself(KillType.ROUNDOVER);
                    break;
            }
        }
    }

    /**
     * Creates or Updates Playerobjects, based on the players map
     */
    public void updatePlayerObjects() {
        synchronized (players) {
            for (String pl : players.keySet()) {
                PlayerData data = players.get(pl);

                if (playerSpatials.containsKey(pl)) {
                    Spatial gameObj = playerSpatials.get(pl);
                    Vector3f realObjectPos = data.getPosition().subtract(0, 4.5f, 0);
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

    /**
     * Shows or hides the blood overlay
     *
     * @param show
     */
    public void showblood(boolean show) {
        if (show) {
            hudBlood.setImage(assetManager, "Textures/blood.png", true);
        } else {
            hudBlood.setImage(assetManager, "Textures/noblood.png", true);
        }
    }

    /**
     * Initializes the HUD of the game (Displayed Text and Images).
     */
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
        name.setSize(guiFont.getCharSet().getRenderedSize());
        name.setColor(ColorRGBA.White);
        name.setText(player.getPlayerId());
        name.setSize(24);
        name.setLocalTranslation(20, name.getLineHeight() + 80, 0);
        guiNode.attachChild(name);

        BitmapText team = new BitmapText(ubuntu, false);
        team.setSize(guiFont.getCharSet().getRenderedSize());
        team.setColor(ColorRGBA.Yellow);
        team.setText(player.getTeam());
        team.setSize(24);
        team.setLocalTranslation(40 + name.getLineWidth(), name.getLineHeight() + 80, 0);
        guiNode.attachChild(team);

        hudHealth = new BitmapText(ubuntu, false);
        hudHealth.setSize(guiFont.getCharSet().getRenderedSize());
        hudHealth.setColor(new ColorRGBA(1, 0.8f, 0.8f, 1));
        hudHealth.setText("Health: 10/10");
        hudHealth.setSize(30);
        hudHealth.setLocalTranslation(20, hudHealth.getLineHeight() + 50, 0);
        guiNode.attachChild(hudHealth);

        hudArmor = new BitmapText(ubuntu, false);
        hudArmor.setSize(guiFont.getCharSet().getRenderedSize());
        hudArmor.setColor(new ColorRGBA(0.8f, 0.8f, 1, 1));
        hudArmor.setText("Shield: 0/10");
        hudArmor.setSize(30);
        hudArmor.setLocalTranslation(20, hudArmor.getLineHeight() + 20, 0);
        guiNode.attachChild(hudArmor);

        hudKilled = new Picture("HUD Picture");
        hudKilled.setImage(assetManager, "Textures/transparent_black.png", true);
        hudKilled.setWidth(settings.getWidth());
        hudKilled.setHeight(settings.getHeight());
        hudKilled.setPosition(0, 0);

        hudKilledTxt = new BitmapText(ubuntu, false);
        hudKilledTxt.setColor(new ColorRGBA(1, 1, 1, 1));
        hudKilledTxt.setSize(80);
        hudKilledTxt.setText("");
        hudKilledTxt.setLocalTranslation(100, settings.getHeight() - 100, 10);
        guiNode.attachChild(hudKilledTxt);
    }

    /**
     * Appends a Message to the Log window
     *
     * @param statusTxt Text to display
     */
    public void appendLogMessage(String statusTxt) {
        logWin.appendLogMessage(statusTxt);
    }

    /**
     * Gets called when the game closes, disconnects the client.
     */
    @Override
    public void destroy() {
        super.destroy();
        if (client != null) {
            client.disconnect();
        }
        System.exit(0);
    }

}
