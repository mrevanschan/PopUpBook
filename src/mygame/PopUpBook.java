package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class PopUpBook extends SimpleApplication{
    public float height = 5.5f;
    public float width = 4f;
    public ChaseCamera chaseCam;
    public HingeJoint joint;
    public Node planes;
    public Material paper;
    public Material markPaper;
    public ArrayList<Geometry> selected;
    public ArrayList<Vector3f> selectedLocation;
    public HashMap<Geometry,Vector3f[]> boundaries = new HashMap();
    public Geometry front;
    public Geometry back;
    public static final float PI = 3.1f;
    public final float resolution = 1f/1000000f;
    public PopUpBookTree popUpBook;
    public BitmapText modeText;
    public BitmapText hintText;
    public BitmapText debugText;
    
    protected float secondCounter = 0.0f;
    protected int frameCounter = 0;
    
    
    
    
    public static void main(String[] args) {
        PopUpBook app = new PopUpBook();
        AppSettings newSettings = new AppSettings(true);
        newSettings.setFrameRate(60);
        newSettings.setVSync(true);
        newSettings.setSamples(16);
        app.setSettings(newSettings);
        app.start();
        
    }
    public PopUpBook(){
        super(new ExplorationState(true),new D1CreationState(false),new D1SCreationState(false),new D2CreationState(false));
    }
    @Override
    public void simpleInitApp() {
        inputManager.deleteMapping("SIMPLEAPP_Exit");
        
        //viewPort.setBackgroundColor(ColorRGBA.White);
        hintText = new BitmapText(guiFont, false);
        modeText = new BitmapText(guiFont, false);
        modeText.setLocalTranslation(5, PI, PI);
        modeText.setSize(guiFont.getCharSet().getRenderedSize()*1.2f);      // font size
        modeText.setLocalTranslation(5, settings.getHeight()-2, 0);
        debugText = new BitmapText(guiFont, false);
        guiNode.attachChild(hintText);
        guiNode.attachChild(modeText);
        guiNode.attachChild(debugText);
        planes = new Node("plane");
        rootNode.attachChild(planes);
        selected = new ArrayList<>();
        selectedLocation = new ArrayList<>();
        initMaterial();
        initBook();
        for(Spatial node :rootNode.getChildren()){
            System.out.println(node.getName());
            System.out.println(node.getTriangleCount());
        }
        System.out.println(popUpBook.front.geometry.getTriangleCount());
        }


    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code        
    }
    private void initMaterial(){
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.DarkGray);
        sun.setDirection(new Vector3f(1f, -5f, -1f).normalizeLocal());
        rootNode.addLight(sun);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.LightGray.mult(1.1f));
        rootNode.addLight(al);
        
        paper = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        paper.setBoolean("UseMaterialColors",true);
        paper.setColor("Diffuse", ColorRGBA.White ); // with Lighting.j3md
        paper.setColor("Ambient", ColorRGBA.White);
        paper.setTexture("DiffuseMap", assetManager.loadTexture("Textures/paper.jpg"));
        paper.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        Texture paperTexture = assetManager.loadTexture("Textures/selected.jpg");
        paperTexture.setWrap(Texture.WrapMode.Repeat);
        markPaper = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        markPaper.setTexture("ColorMap",paperTexture);
        markPaper.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
    }

    private void initBook(){
        popUpBook = new PopUpBookTree(width,height,this);
        
        chaseCam = new ChaseCamera(cam,popUpBook.getFront() , inputManager);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setMaxVerticalRotation(FastMath.HALF_PI);
        chaseCam.setDefaultHorizontalRotation(FastMath.HALF_PI);
        
        
    }
    
    public void setText(String type, String string){
        switch (type){
            case "Mode":{
                modeText.setText(string);
                break;
            }
            case "Debug":{
                debugText.setText(string);
                debugText.setLocalTranslation(settings.getWidth()-debugText.getLineWidth()-5, (debugText.getLineHeight()*string.split("[\n|\r]").length)+5, 0);
                break;
            }
            case "Hint":{
                hintText.setText(string);
                hintText.setColor(ColorRGBA.Blue);
                hintText.setLocalTranslation(5, hintText.getLineHeight()+5, 0);
                break;
                
            }
            case "Error":{
                hintText.setText(string);
                hintText.setLocalTranslation(5, hintText.getLineHeight()+5, 0);
                hintText.setColor(ColorRGBA.Red);
                break;
            }
                
            default: 
                break;
        }
         // position
    }
    
    public void enableState(Class state,boolean onOff){
        stateManager.getState(state).setEnabled(onOff);
    }

    public PopUpBook(ChaseCamera chaseCam, HingeJoint joint, Node planes, Material paper, Material markPaper, ArrayList<Geometry> selected, ArrayList<Vector3f> selectedLocation, Geometry front, Geometry back, PopUpBookTree popUpBook, BitmapText modeText, BitmapText hintText, BitmapText debugText) {
        this.chaseCam = chaseCam;
        this.joint = joint;
        this.planes = planes;
        this.paper = paper;
        this.markPaper = markPaper;
        this.selected = selected;
        this.selectedLocation = selectedLocation;
        this.front = front;
        this.back = back;
        this.popUpBook = popUpBook;
        this.modeText = modeText;
        this.hintText = hintText;
        this.debugText = debugText;
    }

    @Override
    public void simpleUpdate(float tpf) {
            secondCounter += getTimer().getTimePerFrame();
            frameCounter ++;
            if (secondCounter >= 1.0f) {
                int fps = (int) (frameCounter / secondCounter);
                secondCounter = 0.0f;
                frameCounter = 0;
                setText("Debug", " FPS : " + fps + "\nTriCount : " + rootNode.getTriangleCount() + " \n VerCount : " + rootNode.getVertexCount());
            }
        super.simpleUpdate(tpf); //To change body of generated methods, choose Tools | Templates.
        
        
    }
    
    
    
}

