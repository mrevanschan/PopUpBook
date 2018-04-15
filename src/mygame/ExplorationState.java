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

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;

/**
 *
 * @author Yin Fung Evans Chan
 */
public class ExplorationState extends BaseAppState {

    private InputManager inputManager;
    private PopUpBook app;
    private int fold;
    private Material dotMaterial;
    private Node collisionIndicatorNode;
    
    //input mapping names
    private final String E_CLICK = "Click";
    private final String E_FOLD = "Fold";
    private final String E_DELETE = "DELETE";
    private final String E_D1 = "D1";
    private final String E_D2 = "D2";
    private final String E_FOLD_INCREMENT = "E_FOLD_INCREMENT";
    private final String E_UNFOLD_INCREMENT = "E_UNFOLD_INCREMENT";
    private final String E_CLEAR = "E_CLEAR";
    
    //input listeners for this app state
    private final ActionListener buildListener = new BuildListener();
    private final ActionListener exploreListener = new ExplorationListener();
    
    //setting for the speed the design folds. bigger number is faster
    private final float frame = 0.75f;
    
    //variable to keep track of fold percentage
    private float percentage = 0;

    
    /*
    * ActionListener responsible for inputs regarding joint building.
    * This includes builing Step Joint, V-style joint, and special v-style joint
    */
    private class BuildListener implements ActionListener {
        @Override
        public void onAction(String action, boolean isPressed, float tpf) {
            switch (action) {
                case E_D1: {
                    //Case V-Style || Special V-Style Joint
                    
                    if (isPressed) {
                        if (app.selected.size() == 2) {
                            //two patches is selected
                            Geometry geomA = app.selected.get(0);
                            Geometry geomB = app.selected.get(1);
                            geomA.setMaterial(app.paper);
                            geomB.setMaterial(app.paper);
                            
                            
                            if (app.popUpBook.isNeighbor(geomA, geomB)) {
                                //Case V-Style Joint
                                app.popUpBook.fold(0f);
                                setEnabled(false);
                                app.getStateManager().getState(D1CreationState.class).setEnabled(true);
                            } else {
                                //Case Special V-Style Joint
                                setEnabled(false);
                                app.popUpBook.fold(0f);
                                //More Computation to check if Special V-Style conditions are met when D1SCreationState is enabled
                                //If not app state returns to Exploration state.
                                app.getStateManager().getState(D1SCreationState.class).setEnabled(true);
                            }
                        } else {
                            //number of pathes selected is not two
                            app.setText("Error", "Please Select two planes");
                        }
                    }
                    break;
                }
                case E_D2: {
                    if (app.selected.size() == 2) {
                        if (app.popUpBook.isNeighbor(app.selected.get(0), app.selected.get(1))) {
                            Vector3f normal1 = app.popUpBook.geomPatchMap.get(app.selected.get(0)).getNormal();
                            Vector3f normal2 = app.popUpBook.geomPatchMap.get(app.selected.get(1)).getNormal();
                            if (normal1.cross(normal2).distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                                app.popUpBook.fold(0f);
                                setEnabled(false);
                                app.getStateManager().getState(D2CreationState.class).setEnabled(true);

                            } else {
                                app.setText("Error", "The two planes Must not be parallel");
                            }

                        } else {
                            app.setText("Error", "Not neigbour");
                        }
                    } else {
                        app.setText("Error", "Please Select two planes");
                    }
                    break;
                }
                
                default:
                    break;
            }

        }
    }

    private class ExplorationListener implements ActionListener {

        @Override
        public void onAction(String action, boolean keyPressed, float tpf) {
            switch (action) {
                case E_FOLD: {
                    if (keyPressed) {
                        fold = 1;
                    } else {
                        fold = -1;
                    }
                    break;
                }
                case E_FOLD_INCREMENT: {
                    if (keyPressed) {
                        fold = 0;
                        percentage += 0.1;
                        if (percentage > 0.98f) {
                            percentage = 1f - 0.001f;
                            app.setText("Hint", "100%");
                        } else {
                            app.setText("Hint", (int) (percentage * 100) + "%");
                        }

                        app.popUpBook.fold(percentage);
                        ArrayList<Vector3f> collision = app.popUpBook.getCollisions();
                        if (collision != null) {
                            for (Vector3f point : collision) {
                                addDot(point);
                            }
                            fold = 0;
                        }
                    }
                    break;
                }
                case E_UNFOLD_INCREMENT: {
                    if (keyPressed) {
                        fold = 0;
                        percentage -= 0.1;
                        if (percentage < 0.02f) {
                            percentage = 0f;
                            app.setText("Hint", "0%");

                        } else {
                            app.setText("Hint", (int) (percentage * 100) + "%");

                        }
                        app.popUpBook.fold(percentage);

                    }
                    break;
                }
                case E_CLICK: {
                    if (keyPressed) {
                        CollisionResults results = new CollisionResults();
                        Vector2f click2d = inputManager.getCursorPosition().clone();
                        Vector3f click3d = app.getCamera().getWorldCoordinates(
                                click2d, 0f).clone();
                        Vector3f dir = app.getCamera().getWorldCoordinates(
                                click2d, 1f).subtractLocal(click3d).normalizeLocal();
                        Ray ray = new Ray(click3d, dir);
                        app.patches.collideWith(ray, results);
                        if (results.size() > 0) {

                            // The closest collision point is what was truly hit:
                            CollisionResult closest = results.getClosestCollision();
                            if (!(app.selected.contains(closest.getGeometry()))) {
                                app.selected.add(closest.getGeometry());
                            }
                            closest.getGeometry().setMaterial(app.markPaper);

                        } else {
                            removeSelect();
                        }
                    }
                    break;
                }
                case E_DELETE: {
                    if (keyPressed) {
                        if (app.selected.size() == 0) {
                            app.setText("Hint", "To Delete, You Must Select A Plane");
                        } else if (app.selected.size() == 2) {
                            app.setText("Hint", "To Delete, You Must Select Only One Plane");
                        } else {
                            app.popUpBook.delete(app.popUpBook.geomPatchMap.get(app.selected.get(0)));
                            app.selected.clear();
                            app.popUpBook.update();
                        }

                    }
                    break;
                }
                case E_CLEAR: {
                    removeSelect();
                    break;
                }
                default:
                    break;
            }
        }
    }

    ExplorationState(boolean b) {
        setEnabled(b);
    }

    @Override
    protected void initialize(Application app) {
        this.app = (PopUpBook) app;
        if (inputManager == null) {
            inputManager = app.getInputManager();
        }
        System.out.println("created");
        inputManager.addMapping(E_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(E_FOLD, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(E_D1, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(E_D2, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping(E_CLEAR, new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping(E_DELETE, new KeyTrigger(KeyInput.KEY_DELETE), new KeyTrigger(KeyInput.KEY_BACK));
        inputManager.addMapping(E_FOLD_INCREMENT, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(E_UNFOLD_INCREMENT, new KeyTrigger(KeyInput.KEY_LEFT));

        collisionIndicatorNode = new Node("Collision indicator");
        this.app.getRootNode().attachChild(collisionIndicatorNode);
        dotMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dotMaterial.setColor("Color", ColorRGBA.Red);

    }

    @Override
    public void update(float tpf) {
        if (fold == 1) {
            if (percentage < 0.98f) {
                percentage += tpf * frame;
                app.setText("Hint", (int) (percentage * 100) + "%");
                app.popUpBook.fold(percentage);
                ArrayList<Vector3f> collision = app.popUpBook.getCollisions();
                if (collision != null) {
                    for (Vector3f point : collision) {
                        addDot(point);
                    }
                    fold = 0;
                }

            } else {
                fold = 0;
                percentage = 1 - 0.001f;
                app.setText("Hint", "100%");
                app.popUpBook.fold(percentage);
            }

        } else if (fold == -1) {
            collisionIndicatorNode.detachAllChildren();
            for (Geometry patch : app.popUpBook.geomPatchMap.keySet()) {
                patch.setMaterial(app.paper);
            }
            if (percentage < 0.02) {
                fold = 0;
                percentage = 0f;
                app.setText("Hint", "0%");

            } else {
                percentage -= tpf * frame;
                app.setText("Hint", (int) (percentage * 100) + "%");

            }
            app.popUpBook.fold(percentage);
        }

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        app.setText("Mode", "Exploration Mode");
        app.popUpBook.fold(0f);

        inputManager.addListener(buildListener, E_D1);
        inputManager.addListener(buildListener, E_D2);
        inputManager.addListener(exploreListener, E_FOLD);
        inputManager.addListener(exploreListener, E_CLICK);
        inputManager.addListener(exploreListener, E_DELETE);
        inputManager.addListener(exploreListener, E_CLEAR);
        inputManager.addListener(exploreListener, E_FOLD_INCREMENT);
        inputManager.addListener(exploreListener, E_UNFOLD_INCREMENT);
        
        app.setText("Instruction", "-[1]   special/ v-style joint\n"
                                 + "-[2]                     Step joint\n"
                                 + "-[Left Click]            Select\n"
                                 + "-[Esc]             Deselect all\n"
                                 + "-[Space], [<-], [->]       fold\n");
        removeSelect();

    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(buildListener);
        inputManager.removeListener(exploreListener);
        System.out.println("Explore disabled");
        //System.out.println(app.getInputManager().de);
    }

    private void addDot(Vector3f dotLocation) {
        Geometry dot = new Geometry("Dot", new Sphere(8, 8, 0.05f));
        dot.setMaterial(dotMaterial);
        dot.setLocalTranslation(dotLocation);
        collisionIndicatorNode.attachChild(dot);

    }

    private void removeSelect() {
        app.selected.clear();
        for (Geometry patch : app.popUpBook.geomPatchMap.keySet()) {
            patch.setMaterial(app.paper);
        }
    }

}
