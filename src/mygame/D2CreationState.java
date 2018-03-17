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
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author FatE
 */
public class D2CreationState extends BaseAppState {

    private PopUpBook app;
    private InputManager inputManager;
    private Geometry geometryA;
    private Geometry geometryB;
    private Vector3f axisPointA;
    private Vector3f axisPointB;
    private Vector3f axisTranslationA;
    private Vector3f axisTranslationB;
    private Vector3f normalA;
    private Vector3f normalB;
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
    private Plane planeA;
    private Plane planeB;
    private HashMap<Geometry, Vector3f> dotVecticesMap;
    private HashMap<Geometry, Vector3f[]> lineVecticesMap;
    private Node collisionNode;
    private Vector3f refferencePoint;
    public static final String D2_ESCAPE = "D2_Escape";
    public static final String D2_CONFIRM = "D2_Confirm";
    public static final String D2_SELECT = "D2_Select";
    public static final String D2_MOUSE_MOVE = "D2_Mouse";
    private final ActionListener d2BasicInput = new D2BasicListener();
    private final D2MoustListener d2MouseListener = new D2MoustListener();
    private final float lineRadius = 0.05f;
    private final float sphereRadius = 0.15f;
    private final int sample = 20;
    public static final float PI = 3.1f;

    private class D2MoustListener implements AnalogListener {

        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (dragMode != null) {
                app.text.setText(dragMode);
                CollisionResults results = new CollisionResults();
                Vector2f click2d = inputManager.getCursorPosition().clone();
                Vector3f click3d = app.getCamera().getWorldCoordinates(click2d, 0f).clone();
                Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                collisionNode.collideWith(ray, results);
                if (results.size() > 0) {
                    Vector3f newPoint = results.getClosestCollision().getContactPoint();
                    switch (dragMode) {
                        case "shift": {
                            float angle = verticesA.get(3).subtract(refferencePoint).normalize().angleBetween(newPoint.subtract(refferencePoint).normalize());
                            Vector3f translation = verticesA.get(3).subtract(refferencePoint).normalize().mult(newPoint.distance(refferencePoint) * FastMath.cos(angle));
                            verticesA.get(2).addLocal(translation);
                            verticesA.get(3).addLocal(translation);
                            if (Util.isBetween(boundaryA.get(2), verticesA.get(2), boundaryA.get(3)) && Util.isBetween(boundaryA.get(2), verticesA.get(3), boundaryA.get(3))) {
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
                                refferencePoint.addLocal(translation);
                                fitInBoundaries();
                                updateGraphics();
                            } else {
                                translation.negateLocal();
                                verticesA.get(2).addLocal(translation);
                                verticesA.get(3).addLocal(translation);
                            }

                            break;
                        }
                        case "shiftA": {
                            float angle = axisTranslationA.normalize().angleBetween(newPoint.subtract(refferencePoint).normalize());
                            Vector3f translation = axisTranslationA.normalize().mult(newPoint.distance(refferencePoint) * FastMath.cos(angle));
                            if(translation.normalize().distance(axisTranslationA.normalize())  > FastMath.FLT_EPSILON){
                                System.out.println("Wrong!!!");
                            }
                            for (Vector3f point : verticesA) {
                                point.addLocal(translation);
                            }
                            refferencePoint.addLocal(translation);
                            updateBoundaries();
                            if (boundaryA == null || boundaryA.get(3).distance(boundaryA.get(2)) < 0.5f) {
                                translation.negateLocal();
                                for (Vector3f point : verticesA) {
                                    point.addLocal(translation);
                                }
                                refferencePoint.addLocal(translation);
                                updateBoundaries();
                            }else{
                                fitInBoundaries();
                            }
                            updateGraphics();

                            break;
                        }
                        case "shiftB": {
                            float angle = axisTranslationB.normalize().angleBetween(newPoint.subtract(refferencePoint).normalize());
                            Vector3f translation = axisTranslationB.normalize().mult(newPoint.distance(refferencePoint) * FastMath.cos(angle));
                            for (Vector3f point : verticesB) {
                                point.addLocal(translation);
                            }
                            refferencePoint.addLocal(translation);
                            updateBoundaries();
                            if (boundaryB == null || boundaryB.get(3).distance(boundaryB.get(2)) < 0.5f) {
                                translation.negateLocal();
                                for (Vector3f point : verticesB) {
                                    point.addLocal(translation);
                                }
                                refferencePoint.addLocal(translation);
                                updateBoundaries();
                            }else{
                                fitInBoundaries();
                            }
                            updateGraphics();
                            break;
                        }
                        case "shiftDot": {
                            Vector3f point = dotVecticesMap.get(selected);
                            Vector3f pairPoint;
                            Vector3f boundaryPoint;
                            if (verticesA.contains(point)) {
                                pairPoint = verticesA.get(pairNum(verticesA.indexOf(point)));
                                boundaryPoint = boundaryA.get(verticesA.indexOf(point));
                            } else {
                                pairPoint = verticesB.get(pairNum(verticesB.indexOf(point)));
                                boundaryPoint = boundaryB.get(verticesB.indexOf(point));
                            }
                            float angle = pairPoint.subtract(point).normalize().angleBetween(newPoint.subtract(point).normalize());
                            Vector3f translation = pairPoint.subtract(point).normalize().mult(newPoint.distance(point) * FastMath.cos(angle));
                            if (pairPoint.distance(point.add(translation)) > 0.5f && pairPoint.distance(point.add(translation)) <= pairPoint.distance(boundaryPoint)) {
                                point.addLocal(translation);
                                updateGraphics();
                            }

                        }
                        default:
                            break;
                    }
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

                        PopUpBookTree.PageNode pageA = app.popUpBook.addPage(geometryA, boundaryA, new Vector3f[]{verticesA.get(0).clone(), verticesA.get(1).clone()});
                        PopUpBookTree.PageNode pageB = app.popUpBook.addPage(geometryB, boundaryB, new Vector3f[]{jointPointB, verticesB.get(1).clone()});

                        app.popUpBook.addJoint(pageA, pageB, new Vector3f[]{jointPointMid, verticesA.get(2)}, "D2Joint");

                        app.getStateManager().getState(ExplorationState.class).setEnabled(true);
                        setEnabled(false);
                    }
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
                                    refferencePoint = closest.getContactPoint();
                                    Vector3f[] points = lineVecticesMap.get(selected);
                                    if ((verticesA.get(3).equals(points[0]) && verticesA.get(2).equals(points[1])) || (verticesA.get(2).equals(points[0]) && verticesA.get(3).equals(points[1]))) {
                                        //enlarge
                                        dragMode = "shift";
                                        lineMaterialA.setColor("Color", ColorRGBA.Yellow);
                                        lineMaterialB.setColor("Color", ColorRGBA.Yellow);
                                        lineMaterialMid.setColor("Color", ColorRGBA.Yellow);
                                        Vector3f h = closest.getContactPoint().subtract(points[0]);
                                        Vector3f o = points[1].subtract(points[0]).normalize();
                                        refferencePoint = points[0].add(o.mult(FastMath.cos(h.normalize().angleBetween(o)) * h.length()));

                                        Vector3f dirrection1 = verticesA.get(0).subtract(verticesB.get(0)).mult(100f);
                                        Vector3f dirrection2 = verticesA.get(1).subtract(verticesA.get(0)).mult(100f);
                                        Vector3f point1 = refferencePoint.add(dirrection1).add(dirrection2);
                                        Vector3f point2 = refferencePoint.add(dirrection1).add(dirrection2.negate());
                                        Vector3f point3 = refferencePoint.add(dirrection1.negate()).add(dirrection2.negate());
                                        Vector3f point4 = refferencePoint.add(dirrection1.negate()).add(dirrection2);

                                        Geometry collision = new Geometry("Collision", Util.makeMesh(new Vector3f[]{point1, point2, point3, point4}));
                                        collisionNode.attachChild(collision);

                                        app.text.setText("shift");
                                    } else {
                                        if (verticesA.contains(points[0]) && verticesA.contains(points[1])) {
                                            dragMode = "shiftA";
                                            lineMaterialA.setColor("Color", ColorRGBA.Yellow);
                                            lineMaterialMid.setColor("Color", ColorRGBA.Yellow);
                                            Vector3f h = closest.getContactPoint().subtract(points[0]);
                                            Vector3f o = points[1].subtract(points[0]).normalize();
                                            refferencePoint = points[0].add(o.mult(FastMath.cos(h.normalize().angleBetween(o)) * h.length()));
                                            Vector3f dirrection1 = verticesA.get(0).subtract(verticesA.get(1)).mult(100f);
                                            Vector3f dirrection2 = axisTranslationA.normalize().mult(verticesB.get(0).distance(verticesB.get(3)) - 0.2f);
                                            Vector3f point1 = refferencePoint.add(dirrection1).add(dirrection2.normalize().mult(100f));
                                            Vector3f point2 = refferencePoint.add(dirrection1).add(dirrection2.negate());
                                            Vector3f point3 = refferencePoint.add(dirrection1.negate()).add(dirrection2.negate());
                                            Vector3f point4 = refferencePoint.add(dirrection1.negate()).add(dirrection2.normalize().mult(100f));
                                            Geometry collision = new Geometry("Collision", Util.makeMesh(new Vector3f[]{point1, point2, point3, point4}));
                                            collisionNode.attachChild(collision);

                                            app.text.setText("shiftA");
                                        } else {
                                            dragMode = "shiftB";
                                            lineMaterialB.setColor("Color", ColorRGBA.Yellow);
                                            lineMaterialMid.setColor("Color", ColorRGBA.Yellow);

                                            Vector3f h = closest.getContactPoint().subtract(points[0]);
                                            Vector3f o = points[1].subtract(points[0]).normalize();
                                            refferencePoint = points[0].add(o.mult(FastMath.cos(h.normalize().angleBetween(o)) * h.length()));
                                            Vector3f dirrection1 = verticesB.get(0).subtract(verticesB.get(1)).normalize().mult(10f);
                                            Vector3f dirrection2 = axisTranslationB.normalize().mult(verticesA.get(0).distance(verticesA.get(3)) - 0.2f);
                                            Vector3f point1 = refferencePoint.add(dirrection1).add(dirrection2.normalize().mult(10f));
                                            Vector3f point2 = refferencePoint.add(dirrection1).add(dirrection2.negate());
                                            Vector3f point3 = refferencePoint.add(dirrection1.negate()).add(dirrection2.negate());
                                            Vector3f point4 = refferencePoint.add(dirrection1.negate()).add(dirrection2.normalize().mult(10f));
                                            Geometry collision = new Geometry("Collision", Util.makeMesh(new Vector3f[]{point1, point2, point3, point4}));
                                            collisionNode.attachChild(collision);
                                            app.text.setText("shiftB");
                                        }
                                    }
                                    break;
                                }
                                case "Dot": {
                                    dragMode = "shiftDot";
                                    selected.getMaterial().setColor("Color", ColorRGBA.Yellow);
                                    Vector3f point = dotVecticesMap.get(selected);
                                    Vector3f pairPoint;
                                    if (verticesA.contains(point)) {
                                        pairPoint = verticesA.get(pairNum(verticesA.indexOf(point)));
                                    } else {
                                        pairPoint = verticesB.get(pairNum(verticesB.indexOf(point)));
                                    }
                                    Vector3f dirrection1 = point.subtract(pairPoint).mult(2f);
                                    Vector3f dirrection2 = verticesA.get(3).subtract(verticesA.get(0)).mult(2f);
                                    Vector3f point1 = pairPoint.add(dirrection2);
                                    Vector3f point2 = pairPoint.add(dirrection2.negate());
                                    Vector3f point3 = point2.add(dirrection1);
                                    Vector3f point4 = point1.add(dirrection1);
                                    Geometry collision = new Geometry("Collision", Util.makeMesh(new Vector3f[]{point1, point2, point3, point4}));
                                    collisionNode.attachChild(collision);
                                    app.text.setText("shiftDot");

                                }
                                default:
                                    break;
                            }

                        }

                    } else {
                        if (selected != null) {
                            switch (selected.getName()) {
                                case "Line": {
                                    lineMaterialA.setColor("Color", ColorRGBA.Black);
                                    lineMaterialB.setColor("Color", ColorRGBA.Black);
                                    lineMaterialMid.setColor("Color", ColorRGBA.Black);
                                    break;
                                }
                                case "Dot": {
                                    selected.getMaterial().setColor("Color", ColorRGBA.Red);
                                }
                                default:
                                    break;
                            }
                            selected = null;
                        }

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
        inputManager.addMapping(D2_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_X, false));

        dotMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dotMaterial.setColor("Color", ColorRGBA.Red);
        lineMaterialA = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialA.setColor("Color", ColorRGBA.Black);
        lineMaterialB = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialB.setColor("Color", ColorRGBA.Black);
        lineMaterialMid = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterialMid.setColor("Color", ColorRGBA.Black);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
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
        inputManager.addListener(d2MouseListener, D2_MOUSE_MOVE);
        initialize();

    }

    private void initialize() {
        
        PopUpBookTree.PageNode pageA = app.popUpBook.geomPageMap.get(app.selected.get(0));
        PopUpBookTree.PageNode pageB = app.popUpBook.geomPageMap.get(app.selected.get(1));
        normalA = pageB.getNormal();
        normalB = pageA.getNormal();
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
        geometryA = pageA.geometry;
        geometryB = pageB.geometry;
        Vector3f[] listA = pageA.boundary;
        Vector3f[] listB = pageB.boundary;
        Vector3f[] jointPoint = app.popUpBook.axisBetween(geometryA, geometryB);

        axisPointA = jointPoint[0];
        axisPointB = jointPoint[1];

        Vector3f midPoint = axisPointA.add(axisPointB).divide(2);
        Vector3f pointA = axisPointA.subtract(axisPointA.subtract(midPoint).mult(0.2f));
        Vector3f pointB = axisPointB.subtract(axisPointB.subtract(midPoint).mult(0.2f));
        Vector3f deltaAxis = pointB.subtract(pointA);
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
        if(axisTranslationA.length() < axisTranslationB.length()){
            float length = axisTranslationA.length()*0.5f;
            axisTranslationA.normalizeLocal().multLocal(length);
            axisTranslationB.normalizeLocal().multLocal(length);
        }else{
            float length = axisTranslationB.length()*0.5f;
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
            app.text.setText("Dun have enought space to build this joint");
            app.getStateManager().getState(ExplorationState.class).setEnabled(true);
            setEnabled(false);
            
            
        } else {
            if (!Util.isBetween(boundaryA.get(2), verticesA.get(2), boundaryA.get(3)) || !Util.isBetween(boundaryA.get(2), verticesA.get(3), boundaryA.get(3))) {
                verticesA.get(2).set(boundaryA.get(2).add(boundaryA.get(3).subtract(boundaryA.get(2)).mult(0.1f)));
                verticesA.get(3).set(boundaryA.get(3).add(boundaryA.get(2).subtract(boundaryA.get(3)).mult(0.1f)));
            }
                
            

//            float length = verticesA.get(0).distance(verticesA.get(3)) * FastMath.cos(verticesA.get(1).subtract(verticesA.get(0)).normalize().angleBetween(verticesA.get(3).subtract(verticesA.get(0)).normalize()));
//            verticesA.get(0).addLocal(verticesA.get(1).subtract(verticesA.get(0)).normalize().mult(length));
//
//            length = verticesA.get(1).distance(verticesA.get(2)) * FastMath.cos(verticesA.get(0).subtract(verticesA.get(1)).normalize().angleBetween(verticesA.get(2).subtract(verticesA.get(1)).normalize()));
//            verticesA.get(1).addLocal(verticesA.get(0).subtract(verticesA.get(1)).normalize().mult(length));
//
//            length = verticesB.get(0).distance(verticesB.get(3)) * FastMath.cos(verticesB.get(1).subtract(verticesB.get(0)).normalize().angleBetween(verticesB.get(3).subtract(verticesB.get(0)).normalize()));
//            verticesB.get(0).addLocal(verticesB.get(1).subtract(verticesB.get(0)).normalize().mult(length));
//
//            length = verticesB.get(1).distance(verticesB.get(2)) * FastMath.cos(verticesB.get(0).subtract(verticesB.get(1)).normalize().angleBetween(verticesB.get(2).subtract(verticesB.get(1)).normalize()));
//            verticesB.get(1).addLocal(verticesB.get(0).subtract(verticesB.get(1)).normalize().mult(length));

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
        if (!Util.isBetween(boundaryA.get(0), verticesA.get(0), boundaryA.get(1))) {
            verticesA.get(0).set(boundaryA.get(0));
        }
        if (!Util.isBetween(boundaryA.get(0), verticesA.get(1), boundaryA.get(1))) {
            verticesA.get(1).set(boundaryA.get(1));
        }

        if (!Util.isBetween(boundaryB.get(0), verticesB.get(0), boundaryB.get(1))) {
            verticesB.get(0).set(boundaryB.get(0));
        }
        if (!Util.isBetween(boundaryB.get(0), verticesB.get(1), boundaryB.get(1))) {
            verticesB.get(1).set(boundaryB.get(1));
        }

        if (!Util.isBetween(boundaryA.get(2), verticesA.get(2), boundaryA.get(3))) {
            verticesA.get(2).set(boundaryA.get(2));
        }
        if (!Util.isBetween(boundaryA.get(2), verticesA.get(3), boundaryA.get(3))) {
            verticesA.get(3).set(boundaryA.get(3));
        }
    }

    private void updateBoundaries() {
        ArrayList<ArrayList<Vector3f>> results = app.popUpBook.getBoundarys(geometryA, geometryB, verticesA, verticesB, "D2Joint");
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
            ((Cylinder) line.getMesh()).updateGeometry(sample, sample, lineRadius, lineRadius, vertexA.distance(vertexB), false, false);
            line.lookAt(vertexA, new Vector3f(0, 1, 0));
        }
    }

    private void addDot(Vector3f dotLocation) {
        if (!dotVecticesMap.values().contains(dotLocation)) {
            Sphere sphere = new Sphere(sample, sample, sphereRadius);
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

//    @Override
//    public void update(float fps) {
//    }
}
