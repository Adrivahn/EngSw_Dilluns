/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterBoxShape;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 *
 * @author normenhansen
 */
public class WorldCreator {
    private static ParticleEmitter rain;
    private static ParticleEmitter snow;
    
    private static Box brick;
    static float bLength = 0.48f;
    static float bWidth = 0.24f;
    static float bHeight = 0.12f;
    
    private Node rootNode;
    private AssetManager assetManager;
    private BulletAppState space;
    private ViewPort viewPort;
    
    private Material mat;
    private Material mat2;
    private Material mat_obs;
    private Material mat_snow;
    private Material mat_rain;

    /**
     * creates a simple physics test world with a floor, an obstacle and some test boxes
     * @param rootNode
     * @param assetManager
     * @param space
     */
    
    public WorldCreator(Node rootNode, AssetManager assetManager, BulletAppState space, ViewPort viewPort) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.space = space;
        this.viewPort = viewPort;
        initMaterial();
        
    }
    
    
    public void createWorld() {        
        //Afegim la llum
        DirectionalLight sun = new DirectionalLight();
        Vector3f lightDir=new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f);
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        //AmbientLight sun = new AmbientLight();
        //sun.setColor(ColorRGBA.LightGray);
        rootNode.addLight(sun);
        
        brick = new Box(Vector3f.ZERO, bLength, bHeight, bWidth);
        brick.scaleTextureCoordinates(new Vector2f(1f, .5f));
        
        //Afegim boira
        initBoira();
        
        //Afegim ombres
        BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 256);
        bsr.setDirection(new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f)); // light direction
        viewPort.addProcessor(bsr); 
        
        //Afegim el cel
        Node sky = new Node();
        sky.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
        rootNode.attachChild(sky);
        
        //Road creation
        // We load the scene
        Spatial sceneModel = assetManager.loadModel("Models/AngularRoad.j3o");
        sceneModel.setLocalTranslation(0, -5, 0);
        sceneModel.scale(20,0.25f,20);
        
        
        sceneModel.setMaterial(mat);
        
        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape((Node) sceneModel);
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        sceneModel.setShadowMode(ShadowMode.Receive);

        // We attach the scene to the rootNode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(sceneModel);
        
        
        space.getPhysicsSpace().add(sceneModel);
        
        //wall creation
        initWall();

        //Obstacle creation
        Box obstacleBox = new Box(2,2,2);
        Geometry obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(2, -2, -10);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 2
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(2, -2, -100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 3
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-50, -2, -100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 4
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-100, -2, -100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 5
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-100, -2, -50);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 6
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-100, -2, 0);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 7
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-100, -2, 100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 8
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(-50, -2, 100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);
        
        //Obstacle 9
        obstacleBox = new Box(2,2,2);
        obstacleModel = new Geometry("Obstacle", obstacleBox);
        obstacleModel.setLocalTranslation(0, -2, 100);
        obstacleModel.setMaterial(mat_obs);
        obstacleModel.addControl(new RigidBodyControl(2));
        obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(obstacleModel);
        space.getPhysicsSpace().add(obstacleModel);

        //Creem el efecte de neu
        initNeu();
        
        
        //Creem el efecte de pluja
        initPluja();
    }
    

    public static void updateWorld(Vector3f localToWorld) {
//        snow.setLocalTranslation(localToWorld);
    }
    
    public void initNeu() {
        snow = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 1000);
        snow.setMaterial(mat_snow);
        snow.setImagesX(2); snow.setImagesY(2); // 2x2 texture animation
        snow.setShape(new EmitterBoxShape(new Vector3f(-100f,10f,-100f),new Vector3f(100f,10f,100f)));
        snow.setStartColor(ColorRGBA.White);
        snow.getParticleInfluencer().setInitialVelocity(new Vector3f(0,-2,0));
        snow.setStartSize(10.11f);
        snow.setEndSize(10.11f);
        snow.setGravity(0,2,0);
        snow.setLowLife(6.5f);
        snow.setHighLife(6.5f);
        snow.getParticleInfluencer().setVelocityVariation(0.3f);
        snow.setLocalTranslation(0f, 0f, 0f);
        snow.setParticlesPerSec(200);
        rootNode.attachChild(snow);
    }
    
    public void initPluja() {
        rain = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 100000);
        rain.setMaterial(mat_rain);
        //rain.setParticlesPerSec(50);
        rain.setImagesX(2); rain.setImagesY(2); // 2x2 texture animation
        rain.setShape(new EmitterBoxShape(new Vector3f(-100f,10f,-100f),new Vector3f(100f,10f,100f)));
        rain.setStartColor(new ColorRGBA(192f,192f,192f,0.2f));
        //rain.setStartColor(new ColorRGBA(0f,0f,255f,1f));
        rain.getParticleInfluencer().setInitialVelocity(new Vector3f(0,-25,0));
        rain.getParticleInfluencer().setVelocityVariation(0f);
        rain.setStartSize(0.50f);
        rain.setEndSize(0.50f);
        rain.setGravity(0,1,0);
        rain.setLowLife(6.5f);
        rain.setHighLife(6.5f);
        rain.setParticlesPerSec(2000);
        rootNode.attachChild(rain);
    }
    
    public void initBoira() {
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        FogFilter fog=new FogFilter();
        fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fog.setFogDistance(100);
        fog.setFogDensity(2.0f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);
        
        /*FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        BloomFilter bloom=new BloomFilter();
        //bloom.setBlurScale(0.5f);
        bloom.setBloomIntensity(1.25f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);*/
    }
    
    public void initWall() {
        float startpt = bLength / 4;
        float height = 0;
        for (int j = 0; j < 15; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt = new Vector3f(i * bLength * 2 + startpt, bHeight + height-5, 10);
                addBrick(vt);
            }
            startpt = -startpt;
            height += 2 * bHeight;
        }
    }
    
    public void initMaterial() {
        
        mat = new Material( 
            assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", 
            assetManager.loadTexture("Textures/RoadTexture.jpg"));

        mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        mat2.setTexture("ColorMap", tex2);
        
        mat_obs = new Material( 
            assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat_obs.setTexture("DiffuseMap", 
            assetManager.loadTexture("Textures/BoxTexture.jpg"));
        
        mat_snow = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_snow.setTexture("Texture", assetManager.loadTexture("Textures/snow.png"));
        
        mat_rain = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_rain.setTexture("Texture", assetManager.loadTexture("Textures/teardrop.png"));

    }

    public void addBrick(Vector3f ori) {

        Geometry reBoxg = new Geometry("brick", brick);
        reBoxg.setMaterial(mat2);
        reBoxg.setLocalTranslation(ori);
        //for geometry with sphere mesh the physics system automatically uses a sphere collision shape
        reBoxg.addControl(new RigidBodyControl(1.5f));
        reBoxg.setShadowMode(ShadowMode.CastAndReceive);
        reBoxg.getControl(RigidBodyControl.class).setFriction(0.6f);
        rootNode.attachChild(reBoxg);
        space.getPhysicsSpace().add(reBoxg);
    }

}