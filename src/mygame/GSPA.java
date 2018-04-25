package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class GSPA extends SimpleApplication implements ActionListener {

    private final float movementSpeed = 10;
    private float sinusWaveMovePos = 0;
    private float bulletTimeout = 0;

    //______________________________
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
//    private boolean left = false, right = false, up = false, down = false;
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

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape
                = CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
//    player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));

//////        playerNode = new Node("Player node");
//////        rootNode.attachChild(playerNode);
//////        playerNode.addControl(player);
        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        createSimpleMap();
        addWeapon();

        initSky();
        flyCam.setMoveSpeed(0);

        //analogInitKeys();
        initKeys();
        initLights();
        initCrossHair();
        //initDebugText();
        generateEnemies();
    }

    @Override
    public void simpleUpdate(float tpf) {
//        Vector3f moveVectorSum = Vector3f.ZERO.clone();
//        if (jumping) {
//            currentJumpAcc = currentJumpAcc.subtract(0, 0.2f, 0);
//            moveVectorSum.addLocal(currentJumpAcc);
//            if (cam.getLocation().add(moveVectorSum).y <= 1) {
//                moveVectorSum = Vector3f.ZERO;
//                moveVectorSum.subtract(cam.getLocation().setX(0).setZ(0).add(new Vector3f(0, 2, 0)));
//                jumping = false;
//            }
//        }
//        cam.setLocation(cam.getLocation().add(moveVectorSum));

        playerMovement();

        --bulletTimeout;

        for (PauschalAngreifendesBullet b : bullets) {
            b.update();
        }
        for (Enemy e : enemies) {
            e.update();
        }
        
        //changeText(bulletTimeout <= 0 ? "READY" : "NOT READY");

    }

    boolean jumping = false;
    Vector3f jumpAcc = new Vector3f(0, 2, 0);
    Vector3f currentJumpAcc = new Vector3f(0, 2, 0);

    public void playerMovement() {
        walkDirection.set(0, 0, 0);
        Vector3f camDir = cam.getDirection();
        camDir.setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft();
        camLeft.setY(0).normalizeLocal();
        if (inputEvents.contains("Up")) {
            walkDirection.addLocal(camDir);
        }
        if (inputEvents.contains("Down")) {
            walkDirection.addLocal(camDir.negate());
        }
        if (inputEvents.contains("Left")) {
            walkDirection.addLocal(camLeft);
        }
        if (inputEvents.contains("Right")) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (inputEvents.contains("Jump")) {
            player.jump(new Vector3f(0, 5, 0));
        }
        if (inputEvents.contains("Shoot")) {
            shoot();
        }
        player.setWalkDirection(walkDirection.normalize().mult(0.6f));
        cam.setLocation(player.getPhysicsLocation());
    }

    public void shoot() {
        if (bulletTimeout <= 0) {
//            PauschalAngreifendesBullet bullet
//                    = new PauschalAngreifendesBullet(assetManager, bulletAppState, player.getPhysicsLocation(), cam.getDirection());
//            bullets.add(bullet);
//            rootNode.attachChild(bullet.getObject());

            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            CollisionResults results = new CollisionResults();
            for (Enemy e : enemies) {
                e.getObject().collideWith(ray, results);
                try {
                    System.out.println(results.getClosestCollision().getGeometry().getParent().getName() + "---" + e.getObject().getName());
                } catch (NullPointerException ex) {

                }

                if (results.getClosestCollision() != null
                        && results.getClosestCollision().getGeometry().getParent().getName().equals(e.getObject().getName())) {
                    e.getObject().removeFromParent();
                    break;
                }
            }
//            rootNode.collideWith(ray, results);
//            results.forEach(new Consumer<CollisionResult>() {
//                @Override
//                public void accept(CollisionResult target) {
//                    for (Enemy e : enemies) {
//                        if (e.getObject().getName().equals(target.getGeometry().getName())) {
//                            System.out.println("SE ENEMY WAS HIT!");
//                        }
//                        System.out.println(target.getGeometry().getName() + " <> " + e.getObject().getName());
//                    }
//                }
//            });

            bulletTimeout = 30;
            System.out.println(player.getPhysicsLocation());
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
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Shoot");
    }

    public void initSky() {
        Texture skyTexture = assetManager.loadTexture("Textures/sky.png");
        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, skyTexture, skyTexture, skyTexture, skyTexture, skyTexture);
        rootNode.attachChild(sky);
    }

    private void initLights() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
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
    
    private BitmapText helloText;
    private void initDebugText() {
                /** Write text on the screen (HUD) */
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

//        .__.__  .__    ___.                           
//__  _  _|__|  | |  |   \_ |__   ____                  
//\ \/ \/ /  |  | |  |    | __ \_/ __ \                 
// \     /|  |  |_|  |__  | \_\ \  ___/                 
//  \/\_/ |__|____/____/  |___  /\___  >                
//                            \/     \/                 
//                                              .___    
//_______   ____   _____   _______  __ ____   __| _/ /\ 
//\_  __ \_/ __ \ /     \ /  _ \  \/ // __ \ / __ |  \/ 
// |  | \/\  ___/|  Y Y  (  <_> )   /\  ___// /_/ |  /\ 
// |__|    \___  >__|_|  /\____/ \_/  \___  >____ |  \/ 
//             \/      \/                 \/     \/ 
    public void analogInitKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                keyPress(name, value, tpf);
            }
        }, "Up", "Left", "Down", "Right", "Jump");
        inputManager.addListener(new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                shoot();
            }
        }, "Shoot");
    }

    public void addWeapon() {
        //WAFFE
        Spatial waffe = assetManager.loadModel("Models/Beretta_93R/Beretta_93R.j3o");
        waffe.move(new Vector3f(2, 0, 5));
        rootNode.attachChild(waffe);
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(100f);
        lamp_light.setPosition(new Vector3f(1.5f, 0, 3));
        rootNode.addLight(lamp_light);

    }

    public void createSimpleMap() {
        TerrainQuad terrain = new TerrainQuad();
        Material mat_terrain;
        Box b = new Box(1, 2, 1);

        //ADD COLLISION
        Geometry geom = new Geometry("Box", b);
        CollisionShape geomShape
                = CollisionShapeFactory.createMeshShape(geom.center());
        RigidBodyControl geomRb = new RigidBodyControl(geomShape, 0);
        bulletAppState.getPhysicsSpace().add(geomRb);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        /*TERRAIN*/
        /**
         * 1. Create terrain material and load four textures into it.
         */
        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        /**
         * 1.1) Add ALPHA map (for red-blue-green coded splat textures)
         */
        mat_terrain.setTexture("Alpha", assetManager.loadTexture(
                "Textures/grass.jpg"));

        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture(
                "Textures/grass.png");
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        heightmap.load();

        /**
         * 3. We have prepared material and heightmap. Now we create the actual
         * terrain: 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A
         * good value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3)
         * We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
         * 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5) We supply the
         * prepared heightmap itself.
         */
        int patchSize = 17;
        terrain = new TerrainQuad("my terrain", patchSize, 17, new float[0]);

        /**
         * 4. We give the terrain its material, position & scale it, and attach
         * it.
         */
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, -2, 0);
        terrain.setLocalScale(2f, 1f, 2f);
        rootNode.attachChild(terrain);

        /**
         * 5. The LOD (level of detail) depends on were the camera is:
         */
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
    }

    public void keyPress(String name, float value, float tpf) {
        float posy = posy = (float) Math.sin(sinusWaveMovePos++ / 3) / 20;
        Vector3f moveY = new Vector3f(0, posy, 0);

        System.out.printf("%s, %f, %f\n", name, value, tpf);
        //Normalized vector of the view direction in XZ pane
        Vector3f walkDirXZ = new Vector3f(cam.getDirection().x, 0, cam.getDirection().z).normalize();
        //Vector3f 
        Vector3f walkDirXZLeft;

        Matrix3f m = new Matrix3f();
        m.fromAngleAxis((float) (Math.PI / 2.0), new Vector3f(0f, 1f, 0f));
        walkDirXZLeft = m.mult(walkDirXZ).normalize();

        Vector3f moveVectorSum = Vector3f.ZERO;
        switch (name) {
            case "Up":
                moveVectorSum = moveVectorSum.add(walkDirXZ);
                break;
            case "Down":
                moveVectorSum = moveVectorSum.subtract(walkDirXZ);
                break;
            case "Left":
                moveVectorSum = moveVectorSum.add(walkDirXZLeft);
                break;
            case "Right":
                moveVectorSum = moveVectorSum.subtract(walkDirXZLeft);
                break;
            case "Jump":
                //jumping = true;
                break;
        }
        moveVectorSum = moveVectorSum.normalize().mult(tpf).mult(movementSpeed).add(moveY);
        if (jumping) {
            currentJumpAcc.subtractLocal(0, 1, 0);
            moveVectorSum.addLocal(currentJumpAcc);
            if (currentJumpAcc.length() >= -10) {
                currentJumpAcc = jumpAcc.clone();
            }
        }
        cam.setLocation(cam.getLocation().add(moveVectorSum));
    }

}
