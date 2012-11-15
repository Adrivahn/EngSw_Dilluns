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
    
    public WorldCreator world;
    
    private int estat=1;
    private float angle;                /*utilitzat per a calcular direccions*/
    private boolean errorDreta= false;       //per a controlar el error de les rodes en el vector direccio
    private boolean errorEsquerra=false;
    private boolean enMoviment= false;  //per a controlar els resets
    private boolean pasPuntFinal= false;        //per a controlar que segueixi recitifcant quan toca
    private boolean rectificarRectaAEsquerra=false;
    private boolean rectificarRectaADreta=false;
    public Vector3f puntInici= new Vector3f(0f,-5.f,-10.f);
    private Vector3f puntAnterior;
    private Vector3f puntFinal;
    private Vector3f puntSeguent;
    
    
    //Constructor
    public Rival(AssetManager asset, PhysicsSpace phy,WorldCreator w){          /*la idea es passar el world on contingi a la llarga les coordenades del mon*/
        assetManager = asset;
        physicsSpace = phy;
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
    
    public void situar_graella (Vector3f pto) {
        this.reset_rival(puntInici);
        this.getVehicle().setPhysicsLocation(new Vector3f(0f,-5.f,-10.f)); /*posem el rival al terra (-5) i darrera del prota -10*/
        this.getVehicle().accelerate(1.f);  /*necessari pk te velocitat negativa involuntariament i intrepreta k ha de rectificar girant*/
    }
    
    //Getters i setters :
    //#################################################################
    
    //Getter del cotxe en si, s'utilitza per a obtenir-lo desde el main
    public Spatial getSpatial(){           /*conte el cotxe en si com a conjunt d'objectes geometrics*/
        return (Spatial)vehicleNode;
    }
    public VehicleControl getVehicle() {       /* conte tota la llibreria de les fisiques que apliquem*/
        return vehicle;
    }
    
    private float getVelocitat(){                               /*retornem la velocitat de la direccio de les coordenades en valor unic*/
        return this.getVehicle().getLinearVelocity().length();
    }
    
    private Vector3f getPosicio() {
        return this.vehicle.getPhysicsLocation();
    }
    //#################################################################
    //Metodes per a moure el cotxe de forma aleatoria (probablement no vagin aqui)
    //---------------------------------------------------
    
    //Mètodes de moviment bàsic, endavant, endarrere, esquerra i dreta :
    
    private void moureEndavant(){           /* si no te cap desviacio a la direccio utilitzarem aquesta funcio*/
        vehicle.brake(0f);
        if (this.getVelocitat()<15) {      /*si el cotxe va mes lent de 15 accelerem*/
            vehicle.accelerate(800.0f);
        } else {
            //System.out.println(vehicle.getLinearVelocity().length());
            vehicle.accelerate(0);
            enMoviment=true;                                /* per comprobar el primer moviment de tots*/
        }   
    }
    
    public void girarCurvaDreta(){      /* si va massa rapid per girar el frenem, massa lent accelerem i sino girem*/
        if (this.getVelocitat()<70 && this.getVelocitat()>10) {
            System.out.println("frenant");
            vehicle.accelerate(0);
            vehicle.brake(100.0f);
            System.out.println(vehicle.getLinearVelocity().length());
        } else if (this.getVelocitat()>=3 && this.getVelocitat()<=10){
            System.out.println("girant");
            vehicle.steer(-.5f);
            vehicle.brake(0f);
        }else{
            System.out.println("accelerant");
            vehicle.accelerate(100f);
            vehicle.brake(0f);
        }
    }
    public void girarCurvaEsquerra(){ /* = k el girarcurvadreta, seria interesant unificar els dos metodes a la llarga i pasarli per parametre dreta o eskerra*/
        if (this.getVelocitat()<70 && this.getVelocitat()>10) {
            System.out.println("frenant");
            vehicle.accelerate(0);
            vehicle.brake(100.0f);
            System.out.println(vehicle.getLinearVelocity().length());
        } else if (this.getVelocitat()>=3 && this.getVelocitat()<=10){
            System.out.println("girant");
            vehicle.steer(+.5f);
            vehicle.brake(0f);
        }else{
            System.out.println("accelerant");
            vehicle.accelerate(100f);
            vehicle.brake(0f);
        }  
    }
    
    public float getDistancia (Vector3f pto) { /*busquem la distancia del rival al pto del parametre*/
        Vector3f posRival= this.getPosicio();
        float distancia= pto.distance(posRival);
        return distancia;
    }
    public void rectificarDesviacioEsquerra (int estatAnterior,Vector3f pto,boolean seguent) { /* a la llarga es podra unificar els dos metodes de rectificar afegint per parametre dreta o eskerra*/
        float angleActual = calcular_angle_direccions(pto); /*nou angle de desviament mentre's girem*/
        //System.out.println("angleeeee="+angleActual);
        if ((errorEsquerra==false) && (angleActual<352.f && angleActual > 8.f)){ 
        /*el error es un error imposible d'evitar ja que quan calcules la direccio del cotxe medeix la direccio de les rodes i si estic girant s'incrementa inevitablement amb 10º aproximadament*/
                System.out.println("angleeeee ="+angleActual);
                girarCurvaDreta();   
        /*per lo tant girarem fins a arribar a 0º i despres continuarem girant fins a aconseguir el error calculat a ull*/
        } else {
            /*aqui es suposa que està rectificant el error de les rodes ja que està casi encarat del tot*/
            girarCurvaDreta();
            System.out.println("ANGLEEEEE error ="+angleActual);
            errorEsquerra=true;
            if (angleActual<350.f && angleActual > 10.f){           
                /*si hem aconseguit corregir l'error parem de girar i avisem que ja no estem modificant la direccio*/
                rectificarRectaAEsquerra=false; /*estiguem rectificant una recta o girant una curva avisem que ja estem*/
                rectificarRectaADreta=false;
                if (seguent==true) {    /*si es tracta d'una curva anem al estat seguent*/
                    pasPuntFinal= false;    /*aquesta variable controla si hem pogut arribar al punt de desti*/
                    System.out.println("error ULTIIIIIM");
                    estat=estatAnterior+1;  
                }
                vehicle.steer(0.f);
                errorEsquerra=false;
                errorDreta=false;
            }
            System.out.println("rectificant error de rodeeees");        
        }  
    }

    public void rectificarDesviacioDreta (int estatAnterior,Vector3f pto,boolean seguent) {
        float angleActual = calcular_angle_direccions(pto); /*nou angle despres de girar*/
        //System.out.println("angleeeee="+angleActual);
        if ((errorDreta==false) && (angleActual<352.f && angleActual > 8.f)){
                System.out.println("angleeeee ="+angleActual);
                girarCurvaEsquerra();    
        } else {
            System.out.println("Rectificant l'error que  provoca les rodes al estar girades");    
            System.out.println("Angle dins derror ="+angleActual);
            if (angleActual>=0) {
                errorDreta=true;
            }
            if (angleActual<350.f && angleActual > 10.f){  
                rectificarRectaAEsquerra=false; /*estiguem rectificant una recta o girant una curva avisem que ja estem*/
                rectificarRectaADreta=false;
                if (seguent==true) {    /*si es tracta d'una curva anem al estat seguent*/
                    pasPuntFinal= false;    /*aquesta variable controla si hem pogut arribar al punt de desti*/
                    System.out.println("Ultima lectura d'angle.");
                    estat=estatAnterior+1; 
                    
                }
                vehicle.steer(0.f); /*deixem de girar i reinciciem el error de rodes*/
                errorDreta=false;
                errorEsquerra=false;
            }        
        }    
    }
    
    public float calcular_angle_direccions (Vector3f pto) { /*calcula el angle entre el rival i la direccio ideal*/
        Vector2f dirActual = new Vector2f(0.f,0.f); /*direccio del rival utilitzat per a ser mes precis*/
        Vector2f dirIdeal = new Vector2f(0.f,0.f);  //direccio a la que a de encararse
        
        dirActual.setX(this.getVehicle().getLinearVelocity().getX());
        dirActual.setY(this.getVehicle().getLinearVelocity().getZ());
        dirActual = dirActual.normalize();
                
        dirIdeal.setX(pto.getX()-this.getPosicio().getX()); 
        dirIdeal.setY(pto.getZ()-this.getPosicio().getZ());
        dirIdeal=dirIdeal.normalize();        
        float angleRadians= dirActual.angleBetween(dirIdeal);
        angleRadians= (angleRadians*180.f)/(float)Math.PI;
        if (angleRadians<0.f) {
            return 360.f+angleRadians;
        } else {
            return angleRadians;
        }
    }
    
    public void reset_rival(Vector3f pto) { /*resetejem el rival al punt de partida en cas de que es quedi bloquejat*/
        vehicle.setPhysicsLocation(pto);
        vehicle.setPhysicsRotation(new Matrix3f());
        vehicle.setLinearVelocity(new Vector3f(0f,0f,0.1f));    /*al reiniciar el coche inevitablement tira enderrere molt molt poc i el vector direccio es 180 i no interesa i ho corregim*/
        vehicle.setAngularVelocity(Vector3f.ZERO);
        vehicle.resetSuspension();
        angle=0f;
        enMoviment=false;
        pasPuntFinal= false;
        errorDreta=false;
        errorEsquerra=false;
        estat=1;
        vehicle.steer(0.f);
        rectificarRectaAEsquerra=false;
        rectificarRectaADreta=false;        
    }
    
    public void rutina1() {  /* cada cas representa una recta*/
        switch (estat) {
            
            case 1:
                Vector3f puntAnterior = new Vector3f(5.f, -5.f,-55.f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                Vector3f puntFinal = new Vector3f(5.f, -5.f,42.f);   /*(5,42) es el pto de referencia del final de la recta o mig de la curva*/
                Vector3f puntSeguent = new Vector3f(-55.f, -5.f,42.f);  /*pto cap a on haura d'anar despres d'arribal al punt final*/
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<2 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 1 i començarem a girar");
                    if (angle<=180) {
                        System.out.println("Girem a la dreta al pto 1");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    }
                    if (angle>180) {
                        System.out.println("Girem a la esquerra al pto 1");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>20.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true && angle>20.f && angle<179.f)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto1 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<340.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 1 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
                
            case 2:
                puntAnterior = new Vector3f(5.f, -5.f,42.f);    /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-55.f, -5.f,42.f);
                puntSeguent = new Vector3f(-55.f, -5.f,-55.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 2 i començarem a girar");
                    if (angle<=180) {
                        System.out.println("Girem a la dreta al pto 2");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    }
                    if (angle>180) {
                        System.out.println("Girem a la esquerra al pto 2");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 2 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 2 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 3:
                puntAnterior = new Vector3f(-55.f, -5.f,42.f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-55.f, -5.f,-60.f);
                puntSeguent = new Vector3f(0.f, -5.f,-60.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 3 i començarem a girar");
                    if (angle<=180) {
                        System.out.println("Girem a la dreta al pto 3");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    }
                    if (angle>180) {
                        System.out.println("Girem a la esquerra al pto 3");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 3 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 3 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 4:
                puntAnterior = new Vector3f(-55.f, -5.f,-60.f); /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(5.f, -5.f,-55.f);
                puntSeguent = new Vector3f(0.f, -5.f,40.f); 
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 4 i començarem a girar");
                    if (angle<=180) {
                        System.out.println("Girem a la dreta al pto 4");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    }
                    if (angle>180) {
                        System.out.println("Girem a la esquerra al pto 4");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 4 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 4 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 5:
                estat=1;
                break;
                
            default:           
        }
    }
    
    public void rutina2() {  /* cada cas representa una recta*/
        switch (estat) {
            case 1:
                puntAnterior = new Vector3f(5.f, -5.f,-55.f); /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(13.f, -5.f,12.f);
                puntSeguent = new Vector3f(5.f, -5.f,42.f); 
                angle = calcular_angle_direccions(puntFinal);
                System.out.println(angle);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=10.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 8 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 8 cap al pto 1");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 8 cap al pto 1");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>20.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a la recta del pto 8 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<340.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 8 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 2:
                puntAnterior = new Vector3f(5.f, -5.f,-55.f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(5.f, -5.f,42.f);   /*(5,42) es el pto de referencia del final de la recta o mig de la curva*/
                puntSeguent = new Vector3f(-25.f, -5.f,47.f);  /*pto cap a on haura d'anar despres d'arribal al punt final*/
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<2 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=10.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 1 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 1");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 1");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>20.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 1 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<340.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 1 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
                
            case 3:
                puntAnterior = new Vector3f(5.f, -5.f,42.f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-25.f, -5.f,47.f);  /*(5,42) es el pto de referencia del final de la recta o mig de la curva*/
                puntSeguent = new Vector3f(-55.f, -5.f,42.f);  /*pto cap a on haura d'anar despres d'arribal al punt final*/
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<2 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 2 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 2");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 2");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>20.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 2 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<340.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 2 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;    
            case 4:
                puntAnterior = new Vector3f(5.f, -5.f,42.f);    /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-55.f, -5.f,42.f);
                puntSeguent = new Vector3f(-61.f, -5.f,-15f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 3 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 3");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 3");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 3 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 3 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 5:
                puntAnterior = new Vector3f(-55.f, -5.f,42.f);    /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-61.f, -5.f,-15f);
                puntSeguent = new Vector3f(-55.f, -5.f,-55.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 4 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 4");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 4");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 4 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 4 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;    
            case 6:
                puntAnterior = new Vector3f(-61.f, -5.f,-10f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-55.f, -5.f,-55.f);
                puntSeguent = new Vector3f(-22.f, -5.f,-60.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 5 i començarem a girar");
                    if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 5");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 5");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 5 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 5 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 7:
                puntAnterior = new Vector3f(-55.f, -5.f,-55.f);  /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(-22.f, -5.f,-60.f);
                puntSeguent = new Vector3f(5.f, -5.f,-55.f);
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 6 i començarem a girar");
                   if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 6");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 6");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a recta del pto 6 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 6 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 8:
                puntAnterior = new Vector3f(-55.f, -5.f,-60.f); /*punt interesant per a resetejarlo a la ultima curva o punt de control en cas de blokeig futur*/
                puntFinal = new Vector3f(5.f, -5.f,-55.f);
                puntSeguent = new Vector3f(13.f, -5.f,12.f); 
                angle = calcular_angle_direccions(puntFinal);
                if (vehicle.getLinearVelocity().length()<3 && enMoviment==true) {
                    reset_rival(puntInici);
                }
                if (this.getDistancia(puntFinal)<=12.f || pasPuntFinal==true) {     /*si hem arribat a la curva o pto de control girem al seguent punt*/
                    rectificarRectaADreta=false;    /*si estem rectificant pero arribaem ala curva ja no ho posem a false perke no rectiki a la cuirva actual sino a la seguent*/
                    rectificarRectaAEsquerra=false;
                    pasPuntFinal= true;         /*hem arribat  ala curva si ens passem de larea del pto de control seguirem girant*/
                    angle = calcular_angle_direccions(puntSeguent);
                    System.out.println("Hem arribat al punt de control del pto 7 i començarem a girar");
                   if ((angle<=180 && errorEsquerra==false && errorDreta==false)|| errorEsquerra==true) {
                        System.out.println("Girem a la dreta al pto 7");
                        rectificarDesviacioEsquerra(estat,puntSeguent,true);
                    } else if ((angle > 180 && errorDreta==false && errorEsquerra==false)|| errorDreta==true) {
                        System.out.println("Girem a la esquerra al pto 7");
                        rectificarDesviacioDreta(estat,puntSeguent,true);
                    }
                    
                } else if ((angle>30.f && pasPuntFinal == false && angle<179.f) || (rectificarRectaADreta==true)) { /*si encara no hem arribat al pto de control i ens desviem rectifiquem*/
                    rectificarRectaADreta=true;
                    System.out.println("Rectifiquem a la recta del pto 7 girant a la dreta");
                    rectificarDesviacioEsquerra(estat,puntFinal,false);
                } else if ((angle<330.f && pasPuntFinal == false && angle>=181.f) || (rectificarRectaAEsquerra==true)) {
                    rectificarRectaAEsquerra=true;
                    System.out.println("Rectifiquem a la recta del pto 7 girant a la esquerra");
                    rectificarDesviacioDreta(estat,puntFinal,false);
                } else {            /*sino hem arribat al pto de control i anem amb la direccio correcta ens movem endavant*/
                    moureEndavant();
                }
                break;
            case 9:
                estat=1;
                break;
                
            default:           
        }
    }
}
