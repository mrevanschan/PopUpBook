/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author FatE
 */
public class ExplorationState extends BaseAppState {

    private InputManager inputManager;
    private PopUpBook app;
    private Geometry mark;
    private int fold;
    public static final String E_CLICK = "Click";
    public static final String E_FOLD = "Fold";
    public static final String E_D1 = "D1";
    public static final String E_D2 = "D2";
    public static final String E_FOLD_INCREMENT = "E_FOLD_INCREMENT";
    public static final String E_UNFOLD_INCREMENT = "E_UNFOLD_INCREMENT";

    private final ActionListener buildListener = new BuildListener();
    private final ActionListener exploreListener = new ExplorationListener();
    private final float frame = 0.75f;
    private float percentage = 0;

    private class BuildListener implements ActionListener {

        @Override
        public void onAction(String action, boolean isPressed, float tpf) {
            switch (action) {
                case E_D1: {
                    if (isPressed) {
                        if (app.selected.size() == 2) {
                            Geometry geomA = app.selected.get(0);
                            Geometry geomB = app.selected.get(1);
                            geomA.setMaterial(app.paper);
                            geomB.setMaterial(app.paper);

                            if (app.popUpBook.isNeighbor(geomA, geomB)) {
                                app.popUpBook.fold(0f);
                                setEnabled(false);
                                app.getStateManager().getState(D1CreationState.class).setEnabled(true);
                            } else {
                                setEnabled(false);
                                app.popUpBook.fold(0f);
                                app.getStateManager().getState(D1SCreationState.class).setEnabled(true);
                            }
                        } else {
                            app.setText("Error", "Please Select two planes");
                        }
                    }
                    break;
                }
                case E_D2: {
                    if (app.selected.size() == 2) {
                        if (app.popUpBook.isNeighbor(app.selected.get(0), app.selected.get(1))) {
                            Vector3f normal1 = app.popUpBook.geomPageMap.get(app.selected.get(0)).getNormal();
                            Vector3f normal2 = app.popUpBook.geomPageMap.get(app.selected.get(1)).getNormal();
                            if (normal1.cross(normal2).distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                                app.popUpBook.fold(0f);
                                setEnabled(false);
                                app.getStateManager().getState(D2CreationState.class).setEnabled(true);

                            } else {
                                app.setText("Error","The two planes Must not be parallel");
                            }

                        } else {
                            app.setText("Error","Not neigbour");
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
                            percentage = 0.99f;
                            app.setText("Hint", "100%");
                        } else {
                            app.setText("Hint", (int) (percentage * 100) + "%");
                        }

                        app.popUpBook.fold(percentage);
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
                        app.planes.collideWith(ray, results);
                        if (results.size() > 0) {

                            // The closest collision point is what was truly hit:
                            CollisionResult closest = results.getClosestCollision();
                            if (!(app.selected.contains(closest.getGeometry()))) {
                                closest.getGeometry().setMaterial(app.markPaper);
                                app.selected.add(closest.getGeometry());
                                app.selectedLocation.add(closest.getContactPoint());
                            }

                        } else {

                            for (Geometry i : app.selected) {
                                i.setMaterial(app.paper);
                            }

                            app.selected.clear();
                            app.selectedLocation.clear();
                        }
                    }
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
        inputManager.addMapping(E_FOLD_INCREMENT, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(E_UNFOLD_INCREMENT, new KeyTrigger(KeyInput.KEY_LEFT));

    }

    @Override
    public void update(float tpf) {
        if (fold == 1) {
            if (percentage < 0.98f) {
                percentage += tpf * frame;
                app.setText("Hint", (int) (percentage * 100) + "%");  

            } else {
                fold = 0;
                percentage = 0.99f;
                app.setText("Hint", "100%");
            }
            app.popUpBook.fold(percentage);
        } else if (fold == -1) {
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
        inputManager.addListener(exploreListener, E_FOLD_INCREMENT);
        inputManager.addListener(exploreListener, E_UNFOLD_INCREMENT);
        for (Geometry i : app.selected) {
            i.setMaterial(app.paper);
        }

        app.selected.clear();
        app.selectedLocation.clear();
        //app.getRootNode().detachChild(mark);

    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(buildListener);
        inputManager.removeListener(exploreListener);
        System.out.println("Explore disabled");
        //System.out.println(app.getInputManager().de);
    }


}
