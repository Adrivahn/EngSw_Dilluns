package controlador;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl;
import model.Rival;
import model.VehicleProtagonista;
import model.WorldCreator;
import vista.Display;


public class Main extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private VehicleProtagonista car;
    private Rival rival;
    private WorldCreator world;
    private final float accelerationForce = 1000.0f;
    private final float brakeForce = 100.0f;
    private CameraNode camNode;
    
    private MenuController menu;
    private Display display;
    private boolean gameStarted = false;
    
    //Factores para disminuir y aumentar la acceleracion y la frenadas
    private int accelerationFactor = 2; //Factor multiplicativo
    private int brakeForceFactor = 2;   //Factor de division
    
    
    /*Variables per a moure el rival per a fer el crcuit. Cal moure-ho en mesura del que es pugui 
    * a dins de la classe Rival*/
    int estado=1;
    public Vector3f direccioCar;
    public Vector3f direccioRival;
    public Vector2f r = new Vector2f(1.0f,0.1f);
    float angle;
    private boolean start= false;
    private Vector3f puntInici1= new Vector3f(0f,-5.f,-10.f);
    private Vector3f puntInici2= new Vector3f(4f,-5.f,-10.f);
    private Vector3f puntInici3= new Vector3f(0f, -5.f,-10.f);
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    private RigidBodyControl landscape;
    private Vector3f initialPos;
    private Quaternion initialRot;

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        /*if (settings.getRenderer().startsWith("LWJGL")) {
            BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 512);
            bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
            viewPort.addProcessor(bsr);
        }
        cam.setFrustumFar(150f);
         * 
         */             
        
        display = new Display(assetManager,settings,guiNode,this.timer);        
        menu = new MenuController(settings,stateManager,assetManager,rootNode,guiViewPort,inputManager,audioRenderer,this,false,1,0,5,2,1,10,1,0,1,0,0,0,0);   
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }
    
    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

     public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            if (value) {
                steeringValue += .5f;
            } else {
                steeringValue += -.5f;
            }
            car.getVehicle().steer(steeringValue);
        } else if (binding.equals("Rights")) {
            if (value) {
                steeringValue += -.5f;
            } else {
                steeringValue += .5f;
            }
            car.getVehicle().steer(steeringValue);
        } else if (binding.equals("Ups")) {
            if (value) {
                //Aqui hauriem de comprovar que s'activen tots els rivals. Per ara en creem un, amb una IA 1 o IA 2, quan implementem numRivals anirà aqui la activacio
                rival.setPartidaComensada(true);                 /*quan comencem amb el prota començarem la partida amb els rivals*/
                accelerationValue += (accelerationForce*accelerationFactor);
                
            } else {
                accelerationValue -= (accelerationForce*accelerationFactor);
            }
            //System.out.println("Accelerar "+accelerationValue);
            //System.out.println("AcceleraForce "+accelerationForce);
            car.getVehicle().accelerate(accelerationValue);
        } else if (binding.equals("Downs")) {
            if (value) {
                car.getVehicle().brake(brakeForce/brakeForceFactor);
            } else {
                car.getVehicle().brake(0f);
            }
        } else if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                car.getVehicle().setPhysicsLocation(initialPos);
                car.getVehicle().setPhysicsRotation(initialRot);
                car.getVehicle().setLinearVelocity(Vector3f.ZERO);
                car.getVehicle().setAngularVelocity(Vector3f.ZERO);
                car.getVehicle().resetSuspension();
            } else {
            }
        } else if (binding.equals("Space")) {               /*afegim un nou reset pel rival amb l'SAPCE*/
            if (value) {
                System.out.println("Reset Rival");
                rival.reset_rival();
            } else {
            }
        }
    }
    
    /*Metode per comprovar que el cotxe protagonista esta en moviment*/
    public boolean comprovaMoviment (){
        if (car.getVehicle().getLinearVelocity().length()>=5) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void simpleUpdate(float tpf) {
        
        flyCam.setEnabled(false);
        
        if(menu.isMenuFinished() && !gameStarted){
            setupKeys();
            setUpLight();
            addWorld();            
            addProtagonista();
            addRival();
            addDisplay();
            gameStarted = true;          
        }
        
        if(gameStarted){
            
            camNode.lookAt(car.getSpatial().getWorldTranslation(), Vector3f.UNIT_Y);
            
            camNode.setLocalTranslation(car.getSpatial().localToWorld( new Vector3f( 0, 4, -15), null));
            //System.out.println(car.getVehicle().getPhysicsLocation());
            /*Codi per a moure el rival, cal moure-ho d'aqui*/
            if(rival.comprovaPartidaComensada()==true) {      /*depen de la tecla up del prota*/
                rival.moureRival();
            }
            
            display.updateDisplay(car.getSpeed(),1);            
        }

    }
    
    // Añadir aqui los gets necesarios que cada uno necesite para su constructor
    // ej : menu.getCarColorRGBA()
    
    private void addWorld(){
        //Cargamos la escena
         world = new WorldCreator(rootNode, assetManager, bulletAppState, this.viewPort);
    }
    
    private void addDisplay(){        
        float minDimension = Math.min(settings.getWidth(),settings.getHeight());
        display.addDisplay((int)(settings.getWidth()-(minDimension/2.5f)/2),(int)((minDimension/2.5f)/2),2.5f,(int)(settings.getWidth()-(minDimension/40f)-(minDimension/11.42f)-10),(int)(settings.getHeight()*0.975f),40,(int)(settings.getWidth()-(minDimension/9.23f)-10),(int)(settings.getHeight()*0.95f),9.23f,(int)(settings.getWidth()-(minDimension/11.42f)-10),(int)(settings.getHeight()*0.85f),11.42f);        
    }
    
    private void addProtagonista(){
        car = new VehicleProtagonista(getAssetManager(), getPhysicsSpace(), cam);
        car.buildCar(menu.getCarColorRGBA(),menu.getCarColorRGBA());
        initialPos = world.getInitialPos();
        initialRot = world.getInitialRot();
         //Trasladamos el coche protagonista a su posición de salida
        car.getVehicle().setPhysicsLocation(initialPos);
        car.getVehicle().setPhysicsRotation(initialRot);
        
        //Añadimos el coche protagonista
        rootNode.attachChild(car.getSpatial());
        
        //Settejem la camera
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 4, -15));
        //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
        //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
        camNode.lookAt(car.getSpatial().getLocalTranslation(), Vector3f.UNIT_Y);
        
        rootNode.attachChild(camNode);
    }
    
    
    private void addRival(){
         //Aqui creem la classe rival i la afegim al rootNode
        Vector3f initialPosRival = world.getInitialPos();
        Quaternion initialRotRival = world.getInitialRot();
        rival = new Rival(getAssetManager(), getPhysicsSpace(), world,initialPosRival,initialRotRival,2); /*Creacio del rival, incolu el buildcar i el situar-lo correctament*/       
        //Trasladamos el coche protagonista a su posición de salida
        rootNode.attachChild(rival.getSpatial());
    }
}