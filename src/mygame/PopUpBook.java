package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
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
    private float height = 5.5f;
    private float width = 4f;
    public ChaseCamera chaseCam;
    public Node patches;
    public Material paper;
    public Material markPaper;
    public ArrayList<Geometry> selected;
    public PopUpBookTree popUpBook;
    private BitmapText modeText;
    private BitmapText hintText;
    private BitmapText debugText;
    private BitmapText instructionText;
    
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
        
        //initialize all the text area on the corners for hint, name of appstate, instruction, and debugging.
        hintText = new BitmapText(guiFont, false);
        modeText = new BitmapText(guiFont, false);
        instructionText = new BitmapText(guiFont, false);
        //debugText = new BitmapText(guiFont, false);

        modeText.setSize(guiFont.getCharSet().getRenderedSize()*1.2f);      // font size
        modeText.setLocalTranslation(5, settings.getHeight()-2, 0);
        
        //attach the text area to the guiNode
        guiNode.attachChild(hintText);
        guiNode.attachChild(modeText);
        //guiNode.attachChild(debugText);
        
        //attatch the node for the patches
        patches = new Node("pathes");
        rootNode.attachChild(patches);
        selected = new ArrayList<>();
        initMaterial();
        initBook();
        }

    private void initMaterial(){
        //set up the light source for the 3d space
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.DarkGray);
        sun.setDirection(new Vector3f(1f, -5f, -1f).normalizeLocal());
        rootNode.addLight(sun);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.LightGray.mult(1.1f));
        rootNode.addLight(al);
        
        //set up the materials used for the patches
        paper = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        paper.setBoolean("UseMaterialColors",true);
        paper.setColor("Diffuse", ColorRGBA.White ); // with Lighting.j3md
        paper.setColor("Ambient", ColorRGBA.White);
        paper.setTexture("DiffuseMap", assetManager.loadTexture("Textures/paper.jpg"));
        paper.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        paper.getAdditionalRenderState().setWireframe(true);
        
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
            case "Instruction":{
                instructionText.setText(string);
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


    @Override
    public void simpleUpdate(float tpf) {
//            //Gives debugging data inclucing fps, Total number of triangles, and the total number of vertices       
//            secondCounter += getTimer().getTimePerFrame();
//            frameCounter ++;
//            if (secondCounter >= 1.0f) {
//                int fps = (int) (frameCounter / secondCounter);
//                secondCounter = 0.0f;
//                frameCounter = 0;
//                setText("Debug", " FPS : " + fps + "\nTriCount : " + rootNode.getTriangleCount() + " \n VerCount : " + rootNode.getVertexCount());
//            }
        super.simpleUpdate(tpf);
        
        
    }
    
    
    
}

