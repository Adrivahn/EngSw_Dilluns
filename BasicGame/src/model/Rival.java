/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Rival {
    //Atributs de la classe, s'ha d'anar ampliant
    
    private float mass;
    private VehicleControl vehicle;
    private Geometry chasis1;
    private Geometry wheel1;
    private Geometry wheel3;
    private Geometry wheel2;
    private Geometry wheel4;
    private float wheelRadius;
    private AssetManager assetManager;
    private CameraNode camNode;
    public Node vehicleNode;
    private PhysicsSpace physicsSpace;
    public float acceleracio;   //Variable de control de la velocitat màxima
    public float velocitat;   //Variable de control de la velocitat mínima
    public float gir;
    public WorldCreator world;
    
    private int estat=1;
    private Vector2f dirActual = new Vector2f(0.f,0.f); //direccio del rival
    private Vector2f dirIdeal = new Vector2f(0.f,0.f);  //direccio a la que a de encararse
    private boolean error= false;       //per a controlar el error de les rodes en el vector direccio
    private boolean enMoviment= false;  //per a controlar els resets
    boolean pasPuntFinal= false;        //per a controlar que segueixi recitifcant quan toca
    boolean rectificarRectaAEsquerra=false;
    boolean rectificarRectaADreta=false;
    
    //Constructor
    public Rival(AssetManager asset, PhysicsSpace phy,WorldCreator w){
        assetManager = asset;
        physicsSpace = phy;
        velocitat = 0;
        gir = 0;
        world = w;
    }
    
    private Geometry findGeom(Spatial spatial, String name) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                Geometry result = findGeom(child, name);
                if (result != null) {
                    return result;
                }
            }
        } else if (spatial instanceof Geometry) {
            if (spatial.getName().startsWith(name)) {
                return (Geometry) spatial;
            }
        }
        return null;
    }
    
    public void buildCar() {
        mass = 400;
         Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat.getAdditionalRenderState().setWireframe(true);
         mat.setColor("Color", ColorRGBA.Blue);
         /*
         Material matW = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         matW.getAdditionalRenderState().setWireframe(true);
         matW.setColor("Color", ColorRGBA.Black);
*/
        Node meshNode = (Node) assetManager.loadModel("Models/tempCar/Car.scene");
        
        chasis1 = findGeom(meshNode,"Car");
        chasis1.rotate(0, 3.135f, 0);
        chasis1.setMaterial(mat);
        
        CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis1);
        BoundingBox box = (BoundingBox) chasis1.getModelBound();


        //create vehicle node
        vehicleNode = new Node("vehicleNode");
        vehicle = new VehicleControl(carHull, mass);
        vehicleNode.addControl(vehicle);
        //vehicleNode.setMaterial(mat);
        
        vehicleNode.attachChild(chasis1);
        
        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 10.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);
        
        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.5f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;

        Node node1 = new Node("wheel 1 node");
        wheel1 = findGeom(meshNode, "WheelFrontRight");
        wheel1.setMaterial(mat);
        node1.attachChild(wheel1);
        wheel1.center();
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        wheel2 = findGeom(meshNode, "WheelFrontLeft");
        wheel2.setMaterial(mat);
        node2.attachChild(wheel2);
        wheel2.center();
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        wheel3 = findGeom(meshNode, "WheelBackRight");
        wheel3.setMaterial(mat);
        node3.attachChild(wheel3);
        wheel3.center();
        vehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        wheel4 = findGeom(meshNode, "WheelBackLeft");
        wheel4.setMaterial(mat);
        node4.attachChild(wheel4);
        wheel4.center();
        vehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);
        
        vehicle.getWheel(0).setFrictionSlip(9.8f);
        vehicle.getWheel(1).setFrictionSlip(9.8f);
        
        //rootNode.attachChild(vehicleNode);
        
        physicsSpace.add(vehicle);
    }
    
    //Getters i setters :
    //#################################################################
    
    //Getter del cotxe en si, s'utilitza per a obtenir-lo desde el main
    public Spatial getSpatial(){
        return (Spatial)vehicleNode;
    }
    public VehicleControl getVehicle() {
        return vehicle;
    }
    
    public float getAcceleracio(){
        return acceleracio;
    }
    
    public float getVelocitat(){
        return velocitat;
    }
    //#################################################################
    //Metodes per a moure el cotxe de forma aleatoria (probablement no vagin aqui)
    //---------------------------------------------------
    
    //Mètodes de moviment bàsic, endavant, endarrere, esquerra i dreta :
    
    public void moureEndavant(){
        vehicle.brake(0f);
        if (vehicle.getLinearVelocity().length()<15) {
            vehicle.accelerate(800.0f);
        } else {
            //System.out.println(vehicle.getLinearVelocity().length());
            vehicle.accelerate(0);
            enMoviment=true;
        }   
    }
    public void moureEndarrere(){
        velocitat = -10;
    }
    
    public void moureEsquerra(){
        gir = .5f;
    }
    
    public void girarCurvaDreta(){
        if (vehicle.getLinearVelocity().length()<70 && vehicle.getLinearVelocity().length()>10) {
            System.out.println("frenant");
            vehicle.accelerate(0);
            vehicle.brake(100.0f);
            System.out.println(vehicle.getLinearVelocity().length());
        } else if (vehicle.getLinearVelocity().length()>=3 && vehicle.getLinearVelocity().length()<=10){
            System.out.println("girant");
            vehicle.steer(-.5f);
            vehicle.brake(0f);
        }else{
            System.out.println("accelerar");
            vehicle.accelerate(100f);
            vehicle.brake(0f);
        }
    
    }
    public void girarCurvaEsquerra(){
        if (vehicle.getLinearVelocity().length()<70 && vehicle.getLinearVelocity().length()>10) {
            System.out.println("frenant");
            vehicle.accelerate(0);
            vehicle.brake(100.0f);
            System.out.println(vehicle.getLinearVelocity().length());
        } else if (vehicle.getLinearVelocity().length()>=3 && vehicle.getLinearVelocity().length()<=10){
            System.out.println("girant");
            vehicle.steer(+.5f);
            vehicle.brake(0f);
        }else{
            
            System.out.println("accelerar");
            vehicle.accelerate(100f);
            vehicle.brake(0f);
        }
    
    }
    
    //Mètode que s'alimenta dels mètodes de moviment bàsic per a moure el cotxe aleatoriament
    public void frenar(){
        velocitat = velocitat - acceleracio/2;
    }
    public float getDistancia (Vector3f pto) {
        Vector3f posRival= this.getVehicle().getPhysicsLocation();
        float distancia= pto.distance(posRival);
        return distancia;
    }
    public void rectificarLimitEsquerra (int estatAnterior,Vector3f pto,boolean seguent) {
        float angleActual = calcular_angle_direccions(pto); /*nou angle despres de girar*/
        //System.out.println("angleeeee="+angleActual);
        if ((error==false) && (angleActual<352.f && angleActual > 8.f)){
                System.out.println("angleeeee ="+angleActual);
                girarCurvaDreta();    
        } else {
            girarCurvaDreta();
            System.out.println("ANGLEEEEE error ="+angleActual);
            error=true;
            if (angleActual<350.f && angleActual > 10.f){           
                //if (seguent==true) {
                    pasPuntFinal= false;
                    rectificarRectaAEsquerra=false;
                    rectificarRectaADreta=false;
                    if(estat==4) {
                        estat=1;
                    } else {
                        estat=estatAnterior+1;
                        System.out.println("error ULTIIIIIM");
                    }
                //}
                vehicle.steer(0.f);
                error=false;
            }
            System.out.println("rectificant error de rodeeees");        
        }  
    }

    public void rectificarLimitDreta (int estatAnterior,Vector3f pto,boolean seguent) {
        float angleActual = calcular_angle_direccions(pto); /*nou angle despres de girar*/
        //System.out.println("angleeeee="+angleActual);
        if ((error==false) && (angleActual<352.f && angleActual > 8.f)){
                System.out.println("angleeeee ="+angleActual);
                girarCurvaEsquerra();    
        } else {
            System.out.println("ANGLEEEEE error ="+angleActual);
            error=true;
            if (angleActual<350.f && angleActual > 10.f){           
                //if (seguent==true) {
                    pasPuntFinal= false;
                    rectificarRectaAEsquerra=false;
                    rectificarRectaADreta=false;
                    if(estat==4) {
                        estat=1;
                    } else {
                        estat=estatAnterior+1;
                        System.out.println("error ULTIIIIIM");
                    }
                //}
                vehicle.steer(0.f);
                error=false;
            }
            System.out.println("rectificant error de rodeeees");        
        }
        
    }
    
    public float calcular_angle_direccions (Vector3f pto) {
        dirActual.setX(this.getVehicle().getLinearVelocity().getX());
        dirActual.setY(this.getVehicle().getLinearVelocity().getZ());
        dirActual = dirActual.normalize();
                
        dirIdeal.setX(pto.getX()-this.getVehicle().getPhysicsLocation().getX()); 
        dirIdeal.setY(pto.getZ()-this.getVehicle().getPhysicsLocation().getZ());
        dirIdeal=dirIdeal.normalize();
                
        float angleRadians= dirActual.angleBetween(dirIdeal);
        
        angleRadians= (angleRadians*180.f)/(float)Math.PI;
        if (angleRadians<0.f) {
            return 360.f+angleRadians;
        } else {
            return angleRadians;
        }
    }
    
    public void reset_rival(Vector3f pto) {
        vehicle.setPhysicsLocation(new Vector3f (0,-5,0));
        vehicle.setPhysicsRotation(new Matrix3f());
        vehicle.setLinearVelocity(Vector3f.ZERO);
        vehicle.setAngularVelocity(Vector3f.ZERO);
        vehicle.resetSuspension();
        enMoviment=false;
        pasPuntFinal= false;
        error=false;
        estat=1;
        vehicle.steer(0.f);
        rectificarRectaAEsquerra=false;
        rectificarRectaADreta=false;
    }
    
    public void rutina() {
        switch (estat) {
            case 1:
                Vector3f puntAnterior = new Vector3f(5.f, -5.f,-55.f);
                Vector3f puntFinal = new Vector3f(5.f, -5.f,42.f);   /*(0,42) es el pto de referencia del final de la recta*/
                Vector3f puntSeguent = new Vector3f(-55.f, -5.f,42.f);
                float angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntAnterior);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {
                    rectificarRectaADreta=false;
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;
                    System.out.println("curva 111111111111");
                    rectificarLimitEsquerra(estat,puntSeguent,true);
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) {
                    rectificarRectaADreta=true;
                    System.out.println("rectifiquem recta 1 gir a la dreta");
                    rectificarLimitEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("rectifiquem recta 1 gir a la esquerra");
                    rectificarLimitDreta(estat,puntFinal,false);
                } else {
                    moureEndavant();
                }
                break;
                
            case 2:
                puntAnterior = new Vector3f(5.f, -5.f,42.f);
                puntFinal = new Vector3f(-55.f, -5.f,42.f);
                puntSeguent = new Vector3f(-55.f, -5.f,-55.f);
                angle = calcular_angle_direccions(puntFinal);
                
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntAnterior);
                }
                if (this.getDistancia(puntFinal)<=10.f || pasPuntFinal==true) {
                    pasPuntFinal= true;
                    System.out.println("curva 22222222222");
                    rectificarLimitEsquerra(estat,puntSeguent,true);
                } else {
                    /*if (angle>15.f && pasPuntFinal == false) {
                        System.out.println("rectifiquem recta 1");
                        rectificarLimitEsquerra(estat,puntFinal,false);
                    }*/
                    moureEndavant();
                }            
                break;
            case 3:
                puntAnterior = new Vector3f(-55.f, -5.f,42.f);
                puntFinal = new Vector3f(-55.f, -5.f,-60.f);
                puntSeguent = new Vector3f(0.f, -5.f,-60.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntAnterior);
                }
                if (this.getDistancia(puntFinal)<=10.f || pasPuntFinal==true) {
                    pasPuntFinal= true;
                    System.out.println("curva 3333333333333");
                    rectificarLimitEsquerra(estat,puntSeguent,true);
                } else {
                    /*if (angle>15.f && pasPuntFinal == false) {
                        System.out.println("rectifiquem recta 1");
                        rectificarLimitEsquerra(estat,puntFinal,false);
                    }*/
                    moureEndavant();
                }
                break;
            case 4:
                puntAnterior = new Vector3f(-55.f, -5.f,-60.f);
                puntFinal = new Vector3f(5.f, -5.f,-55.f);
                puntSeguent = new Vector3f(0.f, -5.f,40.f); 
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntAnterior);
                }
                if (this.getDistancia(puntFinal)<=10.f || pasPuntFinal==true) {
                    pasPuntFinal= true;
                    System.out.println("curva 44444444444444");
                    rectificarLimitEsquerra(estat,puntSeguent,true);
                } else {
                    /*if (angle>15.f && pasPuntFinal == false) {
                        System.out.println("rectifiquem recta 1");
                        rectificarLimitEsquerra(estat,puntFinal,false);
                    }*/
                    moureEndavant();
                }
                break;  
            default:
                
        }
    }
}
