package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import static com.jme3.scene.plugins.fbx.mesh.FbxLayerElement.Type.Texture;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.sun.javafx.collections.MappingChange.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class GSPA extends SimpleApplication {

    private final float movementSpeed = 10;
    private float sinusWaveMovePos = 0;
    private float bulletTimeout = 0;

    private TerrainQuad terrain = new TerrainQuad();
    private Material mat_terrain;

    private List<PauschalAngreifendesBullet> bullets = new ArrayList<>();

    public static void main(String[] args) {
        GSPA app = new GSPA();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 2, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        flyCam.setMoveSpeed(0);

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
                shoot(name, value, tpf);
            }
        }, "Shoot");

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

        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f moveVectorSum = Vector3f.ZERO.clone();
        if (jumping) {
            currentJumpAcc = currentJumpAcc.subtract(0, 0.2f, 0);
            moveVectorSum.addLocal(currentJumpAcc);
            if (cam.getLocation().add(moveVectorSum).y <= 1) {
                moveVectorSum = Vector3f.ZERO;
                moveVectorSum.subtract(cam.getLocation().setX(0).setZ(0).add(new Vector3f(0, 2, 0)));
                jumping = false;
            }
        }
        
        --bulletTimeout;

        for (PauschalAngreifendesBullet b : bullets) {
            b.positionUpdate();
        }

        cam.setLocation(cam.getLocation().add(moveVectorSum));
    }

    boolean jumping = false;
    Vector3f jumpAcc = new Vector3f(0, 2, 0);
    Vector3f currentJumpAcc = new Vector3f(0, 2, 0);

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

    public void shoot(String name, float value, float tpf) {
        
        if (bulletTimeout <= 0) {
            PauschalAngreifendesBullet bullet
                    = new PauschalAngreifendesBullet(assetManager, cam.getLocation(), cam.getDirection());
            bullets.add(bullet);
            rootNode.attachChild(bullet);
            bulletTimeout = 30;
        }
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
