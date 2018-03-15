package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.joints.HingeJoint;
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
    public BitmapText text;
    
    
    public static void main(String[] args) {
        PopUpBook app = new PopUpBook();
        app.start();
        
    }
    public PopUpBook(){
        super(new ExplorationState(true),new D1CreationState(false),new D1SCreationState(false),new D2CreationState(false));
    }
    @Override
    public void simpleInitApp() {
        inputManager.deleteMapping("SIMPLEAPP_Exit");
        setDisplayFps(false);
        setDisplayStatView(false);
        viewPort.setBackgroundColor(ColorRGBA.White);
        text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        text.setColor(ColorRGBA.Blue);                             // font color
        text.setLocalTranslation(5, text.getLineHeight()+5, 0); // position
        guiNode.attachChild(text);
        planes = new Node("plane");
        rootNode.attachChild(planes);
        selected = new ArrayList<>();
        selectedLocation = new ArrayList<>();
        initMaterial();
        initBook();   
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
    
    
    public void enableState(Class state,boolean onOff){
        stateManager.getState(state).setEnabled(onOff);
    }
    
    
    
}

