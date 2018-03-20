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
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author FatE
 */
public class D2CreationState extends BaseAppState {

    private PopUpBook app;
    private InputManager inputManager;
    private PopUpBookTree.PageNode pageA;
    private PopUpBookTree.PageNode pageB;
    private Vector3f deltaAxis;
    private Vector3f axisTranslationA;
    private Vector3f axisTranslationB;
    private Plane midPlane;
    private Node tempNode;
    private Node frameNode;
    private Material dotMaterial;
    private Material lineMaterialA;
    private Material lineMaterialB;
    private Material lineMaterialMid;
    private String mode;
    private String dragMode;
    private Geometry selected;
    private ArrayList<Vector3f> verticesA;
    private ArrayList<Vector3f> verticesB;
    private ArrayList<Vector3f> boundaryA;
    private ArrayList<Vector3f> boundaryB;
    private Geometry boundaryAGeom;
    private Geometry boundaryBGeom;
    private Geometry mark;
    private Plane planeA;
    private Plane planeB;
    private HashMap<Geometry, Vector3f> dotVecticesMap;
    private HashMap<Geometry, Vector3f[]> lineVecticesMap;
    private Node collisionNode;
    private Vector3f referencePoint;
    private boolean autolock;
    public static final String D2_ESCAPE = "D2_Escape";
    public static final String D2_CONFIRM = "D2_Confirm";
    public static final String D2_SELECT = "D2_Select";
    public static final String D2_MOUSE_MOVE = "D2_Mouse";
    public static final String D2_LOCK = "D2_LOCK";
    private final ActionListener d2BasicInput = new D2BasicListener();
    private final D2MoustListener d2MouseListener = new D2MoustListener();
    private final float lineRadius = 0.05f;
    private final float sphereRadius = 0.125f;
    public static final float PI = 3.1f;

    private class D2MoustListener implements AnalogListener {

        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (dragMode != null) {
                CollisionResults results = new CollisionResults();
                Vector2f click2d = inputManager.getCursorPosition().clone();
                Vector3f click3d = app.getCamera().getWorldCoordinates(click2d, 0f).clone();
                Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);

                switch (dragMode) {
                    case "shift": {
                        collisionNode.collideWith(ray, results);
                        if (results.size() > 0) {

                            Vector3f newPoint = results.getClosestCollision().getContactPoint();
                            app.setText("Hint", newPoint.toString());
                            float angle = verticesA.get(3).subtract(referencePoint).normalize().angleBetween(newPoint.subtract(referencePoint).normalize());
                            Vector3f translation = verticesA.get(3).subtract(referencePoint).normalize().mult(newPoint.distance(referencePoint) * FastMath.cos(angle));
                            ArrayList<ArrayList<Vector3f>> preTransState = copyCurrentState();
                            verticesA.get(2).addLocal(translation);
                            verticesA.get(3).addLocal(translation);
                            for (Vector3f point : verticesA) {
                                if (!point.equals(verticesA.get(3)) && !point.equals(verticesA.get(2))) {
                                    point.addLocal(translation);
                                }
                            }
                            for (Vector3f point : verticesB) {
                                if (!point.equals(verticesA.get(3)) && !point.equals(verticesA.get(2))) {
                                    point.addLocal(translation);
                                }
                            }
                            if (Util.isBetween(boundaryA.get(2), verticesA.get(2), boundaryA.get(3)) && Util.isBetween(boundaryA.get(2), verticesA.get(3), boundaryA.get(3))
                                    && (Util.inBoundary(verticesA.get(0), pageA.boundary) || Util.inBoundary(verticesA.get(1), pageA.boundary))
                                    && (Util.inBoundary(verticesB.get(0), pageB.boundary) || Util.inBoundary(verticesB.get(1), pageB.boundary))) {

                                fitInBoundaries();
                                referencePoint.addLocal(translation);
                                updateGraphics();

                            } else {
                                for (int i = 0; i < verticesA.size(); i++) {
                                    verticesA.get(i).set(preTransState.get(0).get(i));
                                    if (i != 3 && i != 2) {
                                        verticesB.get(i).set(preTransState.get(1).get(i));
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "shiftA": {
                        collisionNode.collideWith(ray, results);
                        if (results.size() > 0) {
                            ArrayList<ArrayList<Vector3f>> preTransState = copyCurrentState();
                            for(Geometry geom:app.popUpBook.geomPageMap.keySet()){
                                geom.setMaterial(app.paper);
                            }
                            Vector3f newPoint = results.getClosestCollision().getContactPoint();
                            if(autolock){
                                tempNode.attachChild(mark);
                                results.clear();
                                app.planes.collideWith(ray, results);
                                if(results.size() > 0){
                                    Vector3f contactPoint = results.getClosestCollision().getContactPoint();
                                    PopUpBookTree.PageNode collidePage = app.popUpBook.geomPageMap.get(results.getClosestCollision().getGeometry());
                                    if(collidePage.getNormal().cross(pageB.getNormal()).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON){
                                        collidePage.geometry.setMaterial(app.markPaper);
                                        Vector3f closest = collidePage.boundary[0];
                                        for(int i = 1; i < collidePage.boundary.length;i++){
                                            if(collidePage.boundary[i].distance(contactPoint) < closest.distance(contactPoint)){
                                                closest = collidePage.boundary[i];
                                            }
                                        }
                                        mark.setLocalTranslation(closest);
                                        newPoint = closest;
                                    }
                                    
                                }
                            }else{
                                mark.removeFromParent();
                            }
                            Vector3f translation = Util.closestPointOnLine(referencePoint, axisTranslationA, newPoint).subtract(referencePoint);

                            for (Vector3f point : verticesA) {
                                point.addLocal(translation);
                            }
                            Plane plane = new Plane();
                            plane.setOriginNormal(pageB.boundary[0], axisTranslationA.normalize());
                            if (plane.pseudoDistance(verticesA.get(0)) < 0.25f) {
                                for (int i = 0; i < verticesA.size(); i++) {
                                    verticesA.get(i).set(preTransState.get(0).get(i));
                                }
                            } else {
                                updateBoundaries();
                                if (boundaryA == null || boundaryA.get(3).distance(boundaryA.get(2)) < 0.5f) {
                                    System.out.println("bad boundary");
                                    for (int i = 0; i < verticesA.size(); i++) {
                                        verticesA.get(i).set(preTransState.get(0).get(i));
                                    }

                                } else {
                                    if (Util.lineTouchesBoundary(verticesA.get(0), deltaAxis, pageA.boundary)) {
                                        //Still OK tall
                                        fitInBoundaries();
                                        referencePoint.addLocal(translation);
                                    } else {
                                        //Too tall
                                        Vector3f tallestPoint = Util.closestPointToDirrection(axisTranslationA, pageA.boundary);
                                        Vector3f adjustTranslation = Util.lineIntersection(tallestPoint, tallestPoint.add(axisTranslationA.mult(100f)), verticesA.get(0).add(deltaAxis.mult(100f)), verticesA.get(1).add(deltaAxis.negate().mult(100f)));
                                        if (adjustTranslation != null) {
                                            adjustTranslation.set(tallestPoint.subtract(adjustTranslation));
                                            for (Vector3f point : verticesA) {
                                                point.addLocal(adjustTranslation);
                                            }
                                            if (!Util.lineTouchesBoundary(verticesA.get(0), deltaAxis, pageA.boundary)) {
                                                for (int i = 0; i < verticesA.size(); i++) {
                                                    verticesA.get(i).set(preTransState.get(0).get(i));
                                                }
                                            } else {
                                                referencePoint.addLocal(adjustTranslation);
                                                referencePoint.addLocal(translation);
                                            }
                                        } else {
                                            for (int i = 0; i < verticesA.size(); i++) {
                                                verticesA.get(i).set(preTransState.get(0).get(i));
                                            }
                                        }

                                    }

                                }
                                updateBoundaries();
                                updateGraphics();
                            }

                        }
                        break;
                    }
                    case "shiftB": {
                        collisionNode.collideWith(ray, results);
                        if (results.size() > 0) {
                            ArrayList<ArrayList<Vector3f>> preTransState = copyCurrentState();
                            for(Geometry geom:app.popUpBook.geomPageMap.keySet()){
                                geom.setMaterial(app.paper);
                            }
                            Vector3f newPoint = results.getClosestCollision().getContactPoint();
                            if(autolock){
                                results.clear();
                                app.planes.collideWith(ray, results);
                                if(results.size() > 0){
                                    Vector3f contactPoint = results.getClosestCollision().getContactPoint();
                                    PopUpBookTree.PageNode collidePage = app.popUpBook.geomPageMap.get(results.getClosestCollision().getGeometry());
                                    if(!collidePage.equals(pageA) || !collidePage.equals(pageA)){
                                        collidePage.geometry.setMaterial(app.markPaper);
                                        Vector3f closest = collidePage.boundary[0];
                                        for(int i = 1; i < collidePage.boundary.length;i++){
                                            if(collidePage.boundary[i].distance(contactPoint) < closest.distance(contactPoint)){
                                                closest = collidePage.boundary[i];
                                            }
                                        }
                                        newPoint = closest;
                                    }
                                    
                                }
                                
                            }
                            Vector3f translation = Util.closestPointOnLine(referencePoint, axisTranslationB, newPoint).subtract(referencePoint);

                            for (Vector3f point : verticesB) {
                                point.addLocal(translation);
                            }
                            Plane plane = new Plane();
                            plane.setOriginNormal(pageA.boundary[0], axisTranslationB.normalize());
                            if (plane.pseudoDistance(verticesB.get(0)) < 0.25f) {
                                for (int i = 0; i < verticesA.size(); i++) {
                                    verticesB.get(i).set(preTransState.get(1).get(i));
                                }
                            } else {
                                updateBoundaries();
                                if (boundaryA == null || boundaryB.get(3).distance(boundaryB.get(2)) < 0.5f) {
                                    System.out.println("bad boundary");
                                    for (int i = 0; i < verticesB.size(); i++) {
                                        verticesB.get(i).set(preTransState.get(1).get(i));
                                    }

                                } else {
                                    if (Util.lineTouchesBoundary(verticesB.get(0), deltaAxis, pageB.boundary)) {
                                        //Still OK tall
                                        fitInBoundaries();
                                        referencePoint.addLocal(translation);
                                    } else {
                                        //Too tall
                                        Vector3f tallestPoint = Util.closestPointToDirrection(axisTranslationB, pageB.boundary);
                                        Vector3f adjustTranslation = Util.lineIntersection(tallestPoint, tallestPoint.add(axisTranslationB.mult(100f)), verticesB.get(0).add(deltaAxis.mult(100f)), verticesB.get(1).add(deltaAxis.negate().mult(100f)));
                                        if (adjustTranslation != null) {
                                            adjustTranslation.set(tallestPoint.subtract(adjustTranslation));
                                            for (Vector3f point : verticesB) {
                                                point.addLocal(adjustTranslation);
                                            }

                                            if (!Util.lineTouchesBoundary(verticesB.get(0), deltaAxis, pageB.boundary)) {
                                                for (int i = 0; i < verticesA.size(); i++) {
                                                    verticesB.get(i).set(preTransState.get(0).get(i));
                                                }
                                            } else {
                                                referencePoint.addLocal(adjustTranslation);
                                                referencePoint.addLocal(translation);
                                            }
                                        } else {
                                            for (int i = 0; i < verticesB.size(); i++) {
                                                verticesB.get(i).set(preTransState.get(0).get(i));
                                            }
                                        }

                                    }

                                }
                                updateBoundaries();
                                updateGraphics();
                            }

                        }
                        break;
                    }
                    case "shiftDot": {
                        collisionNode.collideWith(ray, results);
                        if (results.size() > 0) {
                            Vector3f newPoint = results.getClosestCollision().getContactPoint();
                            Vector3f pairPoint;
                            Vector3f boundaryPoint;
                            if (verticesA.contains(referencePoint)) {
                                pairPoint = verticesA.get(pairNum(verticesA.indexOf(referencePoint)));
                                boundaryPoint = boundaryA.get(verticesA.indexOf(referencePoint));
                            } else {
                                pairPoint = verticesB.get(pairNum(verticesB.indexOf(referencePoint)));
                                boundaryPoint = boundaryB.get(verticesB.indexOf(referencePoint));
                            }
                            results.clear();
                            frameNode.collideWith(ray, results);
                            if (results.size() > 1) {
                                Geometry geom = results.getClosestCollision().getGeometry();
                                if (geom.getName().equals("Dot") && !dotVecticesMap.get(geom).equals(pairPoint) && !geom.equals(selected)) {
                                    geom.getMaterial().setColor("Color", ColorRGBA.Yellow);
                                    newPoint.set(dotVecticesMap.get(geom));
                                } else {
                                    for (Geometry geometry : dotVecticesMap.keySet()) {
                                        if (!geometry.equals(selected)) {
                                            geometry.getMaterial().setColor("Color", ColorRGBA.Red);
                                        }
                                    }
                                }
                            } else {
                                for (Geometry geometry : dotVecticesMap.keySet()) {
                                    if (!geometry.equals(selected)) {
                                        geometry.getMaterial().setColor("Color", ColorRGBA.Red);
                                    }
                                }
                            }
                            Vector3f translation = Util.closestPointOnLine(referencePoint, deltaAxis, newPoint).subtract(referencePoint);
                            if (pairPoint.distance(referencePoint.add(translation)) > 0.5f) {
                                referencePoint.addLocal(translation);
                                fitInBoundaries();
                                updateGraphics();
                            }

                            app.setText("Error", "In: " + Util.inBoundary(referencePoint.add(translation), pageA.boundary));
                        }
                        break;

                    }
                    default:
                        break;
                }

            }

        }

    }

    private class D2BasicListener implements ActionListener {

        @Override
        public void onAction(String action, boolean isPressed, float tpf) {
            switch (action) {
                case D2_ESCAPE: {
                    if (isPressed) {
                        for (Geometry plane : app.selected) {
                            plane.setMaterial(app.paper);
                        }
                        app.selected.clear();
                        setEnabled(false);
                        app.enableState(ExplorationState.class, true);
                    }
                    break;
                }
                case D2_CONFIRM: {
                    if (isPressed) {
                        Vector3f[] boundaryA = verticesA.toArray(new Vector3f[verticesA.size()]);
                        Vector3f[] boundaryB = verticesB.toArray(new Vector3f[verticesB.size()]);
                        float length = verticesA.get(0).distance(verticesA.get(3)) * FastMath.cos(verticesA.get(1).subtract(verticesA.get(0)).normalize().angleBetween(verticesA.get(3).subtract(verticesA.get(0)).normalize()));
                        Vector3f jointPointMid = verticesA.get(3).add(verticesA.get(0).subtract(verticesA.get(1)).normalize().mult(length));
                        length = verticesA.get(0).distance(verticesB.get(0)) * FastMath.cos(verticesA.get(1).subtract(verticesA.get(0)).normalize().angleBetween(verticesB.get(0).subtract(verticesA.get(0)).normalize()));
                        Vector3f jointPointB = verticesB.get(0).add(verticesA.get(0).subtract(verticesA.get(1)).normalize().mult(length));

                        PopUpBookTree.PageNode newPageA = app.popUpBook.addPage(pageA.geometry, boundaryA, new Vector3f[]{verticesA.get(0).clone(), verticesA.get(1).clone()});
                        PopUpBookTree.PageNode newPageB = app.popUpBook.addPage(pageB.geometry, boundaryB, new Vector3f[]{jointPointB, verticesB.get(1).clone()});

                        app.popUpBook.addJoint(newPageA, newPageB, new Vector3f[]{jointPointMid, verticesA.get(2)}, "D2Joint");

                        app.getStateManager().getState(ExplorationState.class).setEnabled(true);
                        setEnabled(false);
                    }
                    break;
                }
                case D2_LOCK: {
                    autolock = isPressed;
                    System.out.println("Autolock =  " + autolock);
                    break;
                }
                case D2_SELECT: {
                    if (isPressed) {
                        collisionNode.detachAllChildren();
                        CollisionResults results = new CollisionResults();
                        Vector2f click2d = inputManager.getCursorPosition().clone();
                        Vector3f click3d = app.getCamera().getWorldCoordinates(click2d, 0f).clone();
                        Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
                        Ray ray = new Ray(click3d, dir);
                        frameNode.collideWith(ray, results);
                        if (results.size() > 0) {
                            app.chaseCam.setEnabled(false);
                            app.getInputManager().setCursorVisible(true);
                            collisionNode.detachAllChildren();
                            CollisionResult closest = results.getClosestCollision();

                            //drag movement
                            selected = closest.getGeometry();
                            switch (selected.getName()) {
                                case "Line": {
                                    referencePoint = closest.getContactPoint();
                                    Vector3f[] points = lineVecticesMap.get(selected);
                                    if ((verticesA.get(3).equals(points[0]) && verticesA.get(2).equals(points[1])) || (verticesA.get(2).equals(points[0]) && verticesA.get(3).equals(points[1]))) {
                                        //enlarge
                                        dragMode = "shift";
                                        lineMaterialA.setColor("Color", ColorRGBA.Yellow);
                                        lineMaterialB.setColor("Color", ColorRGBA.Yellow);
                                        lineMaterialMid.setColor("Color", ColorRGBA.Yellow);
                                        referencePoint = Util.closestPointOnLine(points[0], points[1].subtract(points[0]), referencePoint);
                                        Vector3f deltaSide = Util.lineToPointTranslation(referencePoint, deltaAxis, app.getCamera().getLocation()).cross(deltaAxis).normalize().mult(50f);
                                        Vector3f topA = verticesA.get(1).add(deltaAxis.normalize().mult(100)).add(deltaSide);
                                        Vector3f topB = verticesA.get(1).subtract(deltaAxis.normalize().mult(100)).add(deltaSide);
                                        Vector3f botB = verticesA.get(1).subtract(deltaAxis.normalize().mult(100)).add(deltaSide.negate());
                                        Vector3f botA = verticesA.get(1).add(deltaAxis.normalize().mult(100)).add(deltaSide.negate());
                                        Vector3f[] temp = {botA, botB, topB, topA};
                                        Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                        collisionNode.attachChild(collision);

                                        //app.text.setText("shift");
                                    } else {
                                        if (verticesA.contains(points[0]) && verticesA.contains(points[1])) {
                                            dragMode = "shiftA";
                                            lineMaterialA.setColor("Color", ColorRGBA.Yellow);
                                            lineMaterialMid.setColor("Color", ColorRGBA.Yellow);

                                            referencePoint = Util.closestPointOnLine(points[0], points[1].subtract(points[0]), referencePoint);
                                            Vector3f deltaSide = Util.lineToPointTranslation(referencePoint, axisTranslationA, app.getCamera().getLocation()).cross(deltaAxis).normalize().mult(100f);
                                            Vector3f point1 = referencePoint.add(deltaAxis.normalize().mult(100f)).add(deltaSide);
                                            Vector3f point2 = referencePoint.subtract(deltaAxis.normalize().mult(100f)).add(deltaSide);
                                            Vector3f point3 = referencePoint.subtract(deltaAxis.normalize().mult(100f)).add(deltaSide.negate());
                                            Vector3f point4 = referencePoint.add(deltaAxis.normalize().mult(100f)).add(deltaSide.negate());
                                            Vector3f[] temp = {point1, point2, point3, point4};
                                            Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                            collisionNode.attachChild(collision);

                                            //app.text.setText("shiftA");
                                        } else {
                                            dragMode = "shiftB";
                                            lineMaterialB.setColor("Color", ColorRGBA.Yellow);
                                            lineMaterialMid.setColor("Color", ColorRGBA.Yellow);

                                            referencePoint = Util.closestPointOnLine(points[0], points[1].subtract(points[0]), referencePoint);
                                            Vector3f deltaSide = Util.lineToPointTranslation(referencePoint, axisTranslationB, app.getCamera().getLocation()).cross(deltaAxis).normalize().mult(100f);
                                            Vector3f point1 = referencePoint.add(deltaAxis.normalize().mult(100f)).add(deltaSide);
                                            Vector3f point2 = referencePoint.subtract(deltaAxis.normalize().mult(100f)).add(deltaSide);
                                            Vector3f point3 = referencePoint.subtract(deltaAxis.normalize().mult(100f)).add(deltaSide.negate());
                                            Vector3f point4 = referencePoint.add(deltaAxis.normalize().mult(100f)).add(deltaSide.negate());
                                            Vector3f[] temp = {point1, point2, point3, point4};
                                            Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                            collisionNode.attachChild(collision);
                                            //app.text.setText("shiftB");
                                        }
                                    }
                                    break;
                                }
                                case "Dot": {
                                    dragMode = "shiftDot";
                                    selected.getMaterial().setColor("Color", ColorRGBA.Yellow);
                                    referencePoint = dotVecticesMap.get(selected);
                                    Vector3f pairPoint;
                                    if (verticesA.contains(referencePoint)) {
                                        pairPoint = verticesA.get(pairNum(verticesA.indexOf(referencePoint)));
                                    } else {
                                        pairPoint = verticesB.get(pairNum(verticesB.indexOf(referencePoint)));
                                    }

                                    Vector3f dirrection1 = referencePoint.subtract(pairPoint).mult(100f);
                                    Vector3f dirrection2 = Util.lineToPointTranslation(referencePoint, deltaAxis, app.getCamera().getLocation()).cross(deltaAxis).normalize().mult(50f);
                                    Vector3f point1 = pairPoint.add(dirrection2);
                                    Vector3f point2 = pairPoint.add(dirrection2.negate());
                                    Vector3f point3 = point2.add(dirrection1);
                                    Vector3f point4 = point1.add(dirrection1);
                                    Geometry collision = new Geometry("Collision", Util.makeMesh(new Vector3f[]{point1, point2, point3, point4}));
                                    collisionNode.attachChild(collision);
                                    //app.text.setText("shiftDot");

                                }
                                default:
                                    break;
                            }

                        }

                    } else {
                        for (Geometry geometry : dotVecticesMap.keySet()) {
                            if (!geometry.equals(selected)) {
                                geometry.getMaterial().setColor("Color", ColorRGBA.Red);
                            }
                        }
                        lineMaterialA.setColor("Color", ColorRGBA.Black);
                        lineMaterialB.setColor("Color", ColorRGBA.Black);
                        lineMaterialMid.setColor("Color", ColorRGBA.Black);
                        collisionNode.detachAllChildren();
                        app.chaseCam.setEnabled(true);
                        dragMode = null;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    D2CreationState(boolean b) {
        setEnabled(b);
    }

    @Override
    protected void initialize(Application app) {
        this.app = (PopUpBook) app;
        inputManager = app.getInputManager();
        inputManager.addMapping(D2_ESCAPE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping(D2_CONFIRM, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(D2_SELECT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(D2_LOCK, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(D2_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_X, false));

        dotMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dotMaterial.setColor("Color", ColorRGBA.Red);
        lineMaterialA = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialA.setColor("Color", ColorRGBA.Black);
        lineMaterialB = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialB.setColor("Color", ColorRGBA.Black);
        lineMaterialMid = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialMid.setColor("Color", ColorRGBA.Black);
        Sphere sphere = new Sphere(8, 8, sphereRadius);
        mark = new Geometry("Dot", sphere);
        mark.setMaterial(dotMaterial.clone());
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        app.setText("Mode", "D2 Creation Mode");
        tempNode = new Node("temp");
        frameNode = new Node("frame");
        collisionNode = new Node("collision");
        boundaryAGeom = null;
        boundaryBGeom = null;
        tempNode.attachChild(frameNode);
        lineVecticesMap = new HashMap();
        dotVecticesMap = new HashMap();
        this.app.getRootNode().attachChild(tempNode);
        inputManager.addListener(d2BasicInput, D2_ESCAPE);
        inputManager.addListener(d2BasicInput, D2_CONFIRM);
        inputManager.addListener(d2BasicInput, D2_SELECT);
        inputManager.addListener(d2BasicInput, D2_LOCK);
        inputManager.addListener(d2MouseListener, D2_MOUSE_MOVE);
        initialize();

    }

    private void initialize() {

        pageA = app.popUpBook.geomPageMap.get(app.selected.get(0));
        pageB = app.popUpBook.geomPageMap.get(app.selected.get(1));
        midPlane = null;
        if (pageA.next.contains(pageB)) {
            pageA = app.popUpBook.geomPageMap.get(app.selected.get(1));
            pageB = app.popUpBook.geomPageMap.get(app.selected.get(0));
            midPlane = new Plane();
            Vector3f[] boundary = pageA.boundary;
            midPlane.setPlanePoints(boundary[0], boundary[1], boundary[2]);
        } else if (pageB.next.contains(pageA)) {
            midPlane = new Plane();
            Vector3f[] boundary = pageA.boundary;
            midPlane.setPlanePoints(boundary[0], boundary[1], boundary[2]);
        }
        Vector3f[] listA = pageA.boundary;
        Vector3f[] listB = pageB.boundary;
        Vector3f[] jointPoint = app.popUpBook.axisBetween(pageA.geometry, pageB.geometry);

        Vector3f midPoint = jointPoint[0].add(jointPoint[1]).divide(2);
        Vector3f pointA = jointPoint[0].subtract(jointPoint[0].subtract(midPoint).mult(0.3f));
        Vector3f pointB = jointPoint[1].subtract(jointPoint[1].subtract(midPoint).mult(0.3f));
        deltaAxis = pointB.subtract(pointA);
        Vector3f deltaA = null;
        Vector3f deltaB = null;
        Plane plane = new Plane();
        plane.setPlanePoints(jointPoint[0], jointPoint[1], jointPoint[1].add(listB[0].subtract(listB[1]).cross(listB[2].subtract(listB[1]))));
        float distance = 0f;

        for (Vector3f point : listB) {
            if (!point.equals(jointPoint[0]) && !point.equals(jointPoint[1]) && distance < FastMath.abs(plane.pseudoDistance(point))) {
                if (midPlane != null) {
                    if (midPlane.whichSide(point).equals(midPlane.whichSide(jointPoint[0].add(app.getCamera().getDirection().negate())))) {
                        deltaB = point;
                        distance = FastMath.abs(plane.pseudoDistance(point));
                    }
                } else {
                    deltaB = point;
                    distance = FastMath.abs(plane.pseudoDistance(point));
                }

            }
        }

        plane.setPlanePoints(jointPoint[0], jointPoint[1], jointPoint[1].add(listA[0].subtract(listA[1]).cross(listA[2].subtract(listA[1]))));
        distance = 0f;
        for (Vector3f point : listA) {
            if (!point.equals(jointPoint[0]) && !point.equals(jointPoint[1]) && distance < FastMath.abs(plane.pseudoDistance(point))) {
                deltaA = point;
                distance = FastMath.abs(plane.pseudoDistance(point));
            }
        }

        axisTranslationB = Util.lineToPointTranslation(jointPoint[0], deltaAxis, deltaB);
        axisTranslationA = Util.lineToPointTranslation(jointPoint[0], deltaAxis, deltaA);
        if (axisTranslationA.length() < axisTranslationB.length()) {
            float length = axisTranslationA.length() * 0.5f;
            axisTranslationA.normalizeLocal().multLocal(length);
            axisTranslationB.normalizeLocal().multLocal(length);
        } else {
            float length = axisTranslationB.length() * 0.5f;
            axisTranslationA.normalizeLocal().multLocal(length);
            axisTranslationB.normalizeLocal().multLocal(length);
        }
        verticesA = new ArrayList<>();
        verticesB = new ArrayList<>();

        verticesA.add(pointA.add(axisTranslationA));
        verticesA.add(pointB.add(axisTranslationA));

        verticesB.add(pointA.add(axisTranslationB));
        verticesB.add(pointB.add(axisTranslationB));

        verticesA.add(verticesA.get(1).add(axisTranslationB));
        verticesA.add(verticesA.get(0).add(axisTranslationB));

        verticesB.add(verticesA.get(2));
        verticesB.add(verticesA.get(3));

        updateBoundaries();
        if (boundaryA == null) {
            //dun have enought space
            app.setText("Error", "Dun have enought space to build this joint");
            app.getStateManager().getState(ExplorationState.class).setEnabled(true);
            setEnabled(false);

        } else {
            fitInBoundaries();

            addDot(verticesA.get(1));
            addDot(verticesA.get(0));
            addDot(verticesB.get(1));
            addDot(verticesB.get(0));
            addDot(verticesA.get(2));
            addDot(verticesA.get(3));

            addLine(verticesA.get(0), verticesA.get(1));
            addLine(verticesA.get(0), verticesA.get(3));
            addLine(verticesA.get(1), verticesA.get(2));
            addLine(verticesA.get(2), verticesA.get(3));
            addLine(verticesB.get(0), verticesB.get(1));
            addLine(verticesB.get(1), verticesB.get(2));
            addLine(verticesB.get(3), verticesB.get(0));
        }

    }

    private void fitInBoundaries() {
        Vector3f[] boundary = Util.lineBoundaryIntersections(verticesA.get(0), deltaAxis, pageA.boundary);
        
        if (boundary[0].distance(Vector3f.ZERO) > FastMath.FLT_EPSILON && boundary[1].distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
            if (!Util.isBetween(boundary[0], verticesA.get(0), boundary[1]) && !Util.isBetween(boundary[0], verticesA.get(1), boundary[1])) {
                verticesA.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                verticesA.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
                app.setText("Hint", "Both Out");
            }
            if (Util.isBetween(boundary[0], verticesA.get(0), boundary[1]) && !Util.isBetween(boundary[0], verticesA.get(1), boundary[1])) {
                verticesA.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
                if (verticesA.get(1).distance(verticesA.get(0)) < 0.25f) {
                    Vector3f temp = verticesA.get(1).add(deltaAxis.normalize().negate().mult(0.25f));
                    if (Util.isBetween(boundary[0], temp, boundary[1])) {
                        verticesA.get(0).set(temp);
                    } else {
                        verticesA.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                    }
                }
            }
            if (!Util.isBetween(boundary[0], verticesA.get(0), boundary[1]) && Util.isBetween(boundary[0], verticesA.get(1), boundary[1])) {
                verticesA.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                if (verticesA.get(0).distance(verticesA.get(1)) < 0.25f) {
                    Vector3f temp = verticesA.get(0).add(deltaAxis.normalize().mult(0.25f));
                    if (Util.isBetween(boundary[0], temp, boundary[1])) {
                        verticesA.get(1).set(temp);
                    } else {
                        verticesA.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
                    }
                }
            }
        }

        boundary = Util.lineBoundaryIntersections(verticesB.get(0), deltaAxis, pageB.boundary);
        if (boundary[0].distance(Vector3f.ZERO) > FastMath.FLT_EPSILON && boundary[1].distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
            if (!Util.isBetween(boundary[0], verticesB.get(0), boundary[1]) && !Util.isBetween(boundary[0], verticesB.get(1), boundary[1])) {
                verticesB.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                verticesB.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
            }
            if (Util.isBetween(boundary[0], verticesB.get(0), boundary[1]) && !Util.isBetween(boundary[0], verticesB.get(1), boundary[1])) {
                verticesB.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
                if (verticesB.get(1).distance(verticesB.get(0)) < 0.25f) {
                    Vector3f temp = verticesB.get(1).add(deltaAxis.normalize().negate().mult(0.25f));
                    if (Util.isBetween(boundary[0], temp, boundary[1])) {
                        verticesB.get(0).set(temp);
                    } else {
                        verticesB.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                    }
                }
            }
            if (!Util.isBetween(boundary[0], verticesB.get(0), boundary[1]) && Util.isBetween(boundary[0], verticesB.get(1), boundary[1])) {
                verticesB.get(0).set(Util.closestPointToDirrection(deltaAxis.negate(), boundary));
                if (verticesB.get(0).distance(verticesB.get(1)) < 0.25f) {
                    Vector3f temp = verticesB.get(0).add(deltaAxis.normalize().mult(0.25f));
                    if (Util.isBetween(boundary[0], temp, boundary[1])) {
                        verticesB.get(1).set(temp);
                    } else {
                        verticesB.get(1).set(Util.closestPointToDirrection(deltaAxis, boundary));
                    }
                }
            }
        }

        if (!Util.isBetween(boundaryA.get(2), verticesA.get(2), boundaryA.get(3))) {
            verticesA.get(2).set(boundaryA.get(2));
        }
        if (!Util.isBetween(boundaryA.get(2), verticesA.get(3), boundaryA.get(3))) {
            verticesA.get(3).set(boundaryA.get(3));
        }

    }

    private void updateBoundaries() {
        ArrayList<ArrayList<Vector3f>> results = app.popUpBook.getBoundarys(pageA.geometry, pageB.geometry, verticesA, verticesB, "D2Joint");
        if (results != null) {

            boundaryA = results.get(0);
            boundaryB = results.get(1);
            if (boundaryAGeom == null) {

                boundaryAGeom = new Geometry(mode, Util.makeMesh(boundaryA.toArray(new Vector3f[boundaryA.size()])));
                boundaryBGeom = new Geometry(mode, Util.makeMesh(boundaryB.toArray(new Vector3f[boundaryB.size()])));
                Material allowed = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                allowed.setColor("Color", new ColorRGBA(0, 1, 0, 0.5f));
                allowed.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                allowed.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

                boundaryAGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
                boundaryBGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
                boundaryAGeom.setMaterial(allowed);
                boundaryBGeom.setMaterial(allowed.clone());
                tempNode.attachChild(boundaryAGeom);
                tempNode.attachChild(boundaryBGeom);

            } else {
                boundaryAGeom.setMesh(Util.makeMesh(boundaryA.toArray(new Vector3f[boundaryA.size()])));
                boundaryBGeom.setMesh(Util.makeMesh(boundaryB.toArray(new Vector3f[boundaryB.size()])));
            }
        } else {
            boundaryA = null;
            boundaryB = null;
        }

    }

    private int pairNum(int i) {
        switch (i) {
            case 0: {
                return 1;
            }
            case 1: {
                return 0;
            }
            case 2: {
                return 3;
            }
            case 3: {
                return 2;
            }
        }
        return -1;
    }

    private void addLine(Vector3f from, Vector3f to) {
        Geometry line = new Geometry("Line", new Cylinder());
        if (verticesA.contains(to) && verticesA.contains(from)) {
            if (verticesB.contains(to) && verticesB.contains(from)) {
                line.setMaterial(lineMaterialMid);
            } else {
                line.setMaterial(lineMaterialA);
            }
        } else {
            line.setMaterial(lineMaterialB);
        }

        updateLine(line, to, from);
        lineVecticesMap.put(line, new Vector3f[]{from, to});
        frameNode.attachChild(line);
    }

    private void updateLine(Geometry line, Vector3f vertexA, Vector3f vertexB) {
        if (line.getName().equals("Line")) {
            line.setLocalTranslation(vertexA.add(vertexB).divide(2f));
            if (vertexA.distance(vertexB) > FastMath.FLT_EPSILON) {
                ((Cylinder) line.getMesh()).updateGeometry(5, 3, lineRadius, lineRadius, vertexA.distance(vertexB), false, false);
                line.lookAt(vertexA, new Vector3f(0, 1, 0));
            } else {
                ((Cylinder) line.getMesh()).updateGeometry(5, 3, lineRadius, lineRadius, 0.001f, false, false);
            }
            line.lookAt(vertexA, new Vector3f(0, 1, 0));
        }
    }

    private void addDot(Vector3f dotLocation) {
        if (!dotVecticesMap.values().contains(dotLocation)) {
            Sphere sphere = new Sphere(8, 8, sphereRadius);
            Geometry dot = new Geometry("Dot", sphere);
            dot.setMaterial(dotMaterial.clone());
            dot.setLocalTranslation(dotLocation);
            frameNode.attachChild(dot);
            dotVecticesMap.put(dot, dotLocation);
        }
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(d2BasicInput);
        app.getRootNode().detachChild(tempNode);
    }

    private void updateGraphics() {
        for (HashMap.Entry pair : dotVecticesMap.entrySet()) {
            ((Geometry) pair.getKey()).setLocalTranslation((Vector3f) pair.getValue());
        }
        for (HashMap.Entry pair : lineVecticesMap.entrySet()) {
            Vector3f[] points = (Vector3f[]) pair.getValue();
            updateLine((Geometry) pair.getKey(), points[0], points[1]);
        }
    }

    private ArrayList<ArrayList<Vector3f>> copyCurrentState() {
        ArrayList<ArrayList<Vector3f>> returnList = new ArrayList<>();
        ArrayList<Vector3f> listACopy = new ArrayList<>();
        ArrayList<Vector3f> listBCopy = new ArrayList<>();
        listACopy.add(verticesA.get(0).clone());
        listACopy.add(verticesA.get(1).clone());
        listACopy.add(verticesA.get(2).clone());
        listACopy.add(verticesA.get(3).clone());

        listBCopy.add(verticesB.get(0).clone());
        listBCopy.add(verticesB.get(1).clone());
        listBCopy.add(listACopy.get(2));
        listBCopy.add(listACopy.get(3));
        returnList.add(listACopy);
        returnList.add(listBCopy);
        return returnList;
    }

//    @Override
//    public void update(float fps) {
//    }
}
