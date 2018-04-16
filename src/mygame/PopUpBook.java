/*
 * Copyright (C) 2018 Yin Fung Evans Chan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.ArrayList;

/**
 * This is the Main Class of program
 * @author normenhansen
 */
public class PopUpBook extends SimpleApplication{
    //dimension of the design
    private float height = 5.5f;
    private float width = 4f;
    
    public ChaseCamera chaseCam;
    
    //node holding all patches
    public Node patches;
    //node holding all lines
    public Node lines;
    
    //materials for the patches
    public Material paper;
    public Material markPaper;
    
    //selected patches
    public ArrayList<Geometry> selected;
    
    //design data
    public PopUpBookTree popUpBook;
    
    //textbox at the corners of window
    private BitmapText modeText;
    private BitmapText hintText;
    private BitmapText instructionText;
    
    //used for debugging fps
    private float secondCounter = 0.0f;
    private int frameCounter = 0;
    private BitmapText debugText;
    
    
    
    /**
     * The main method. Sets up the application settings.
     * @param args 
     */
    public static void main(String[] args) {
        PopUpBook app = new PopUpBook();
        app.setShowSettings(false);
        AppSettings newSettings = new AppSettings(true);
        newSettings.setFrameRate(60);
        newSettings.setVSync(true);
        newSettings.setSamples(16);
        newSettings.setResolution(1024, 768);
        newSettings.setTitle("Pop-up Design Software");
        app.setSettings(newSettings);
        app.start();
        
    }
    /**
     * constructor of the PopUpBook
     * initialize all the app states
     */
    public PopUpBook(){
        super(new ExplorationState(true),new D1CreationState(false),new D1SCreationState(false),new D2CreationState(false));
    }
    /**
     * initializes the text area around the corner and the nodes used in the program.
     */
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
        guiNode.attachChild(instructionText);
        //guiNode.attachChild(debugText);
        
        //attatch the node for the patches
        patches = new Node("pathes");
        lines = new Node("lines");
        rootNode.attachChild(patches);
        rootNode.attachChild(lines);
        selected = new ArrayList<>();
        initMaterial();
        initBook();
        }
    /**
     * initializes light sourcese and material
     */
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
        //paper.getAdditionalRenderState().setWireframe(true);
        
        Texture paperTexture = assetManager.loadTexture("Textures/selected.jpg");
        paperTexture.setWrap(Texture.WrapMode.Repeat);
        markPaper = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        markPaper.setTexture("ColorMap",paperTexture);
        markPaper.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
    }
    
    /**
     * initializes the data and the camera
     */
    private void initBook(){
        popUpBook = new PopUpBookTree(width,height,this);
        
        chaseCam = new ChaseCamera(cam,popUpBook.getFront() , inputManager);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setMaxVerticalRotation(FastMath.HALF_PI);
        chaseCam.setDefaultHorizontalRotation(FastMath.HALF_PI);
        
        
    }
    /**
     * Sets the text on corner of the window
     * @param type
     * @param string 
     */
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
                instructionText.setLocalTranslation(settings.getWidth()-instructionText.getLineWidth()-5, (instructionText.getLineHeight()*string.split("[\n|\r]").length)+5, 0);
                break;
            }
            case "Hint":{
                hintText.setText(string);
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
    
    /**
     * Function to switch enable or disable app states
     * @param state
     * @param onOff 
     */
    public void enableState(Class state,boolean onOff){
        stateManager.getState(state).setEnabled(onOff);
    }


    /**
     * When debugging, simpleUpdate can be overiden to show fps, number of triangles and number of vertices
     * @param tpf 
     */
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

