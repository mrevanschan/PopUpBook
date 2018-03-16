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
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
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
public class D1CreationState extends BaseAppState {

    private PopUpBook app;
    private InputManager inputManager;
    private Geometry geometryA;
    private Geometry geometryB;
    private Vector3f deltaAxis;
    private Vector3f axisPointA;
    private Vector3f axisPointB;
    private Vector3f axisTranslationA;
    private Vector3f axisTranslationB;
    private Node tempNode;
    private Node frameNode;
    private Material dotMaterial;
    private Material lineMaterial;
    private String mode;
    private String dragMode;
    private Geometry selected;
    private ArrayList<Vector3f> verticesA;
    private ArrayList<Vector3f> verticesB;
    private ArrayList<Vector3f> boundaryA;
    private ArrayList<Vector3f> boundaryB;
    private Geometry boundaryAGeom;
    private Geometry boundaryBGeom;
    private HashMap<Geometry, Vector3f> dotVecticesMap;
    private HashMap<Geometry, Vector3f[]> lineVecticesMap;
    private Node collisionNode;
    private Vector3f referencePoint;
    public static final String D1_ESCAPE = "Escape";
    public static final String D1_SELECT = "Select";
    public static final String D1_ADD = "ADD";
    public static final String D1_MOUSE_MOVE = "MouseMove";
    public static final String D1_CONFIRM = "Confirm";
    private final ActionListener d1BasicInput = new D1BasicListener();
    private final D1MoustListener d1MouseListener = new D1MoustListener();
    private final float lineRadius = 0.05f;
    private final float sphereRadius = 0.2f;
    private final int sample = 20;
    private final float angleConstant = FastMath.PI / 8;
    private Sphere sphere;

    private class D1MoustListener implements AnalogListener {

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
                        case "freeMove":
                            selected.setLocalTranslation(newPoint);
                            Vector3f vertex = dotVecticesMap.get(selected);
                            vertex.set(newPoint);
                            fitInBoundaries();
                            selected.setLocalTranslation(vertex);
                            for (HashMap.Entry pair : lineVecticesMap.entrySet()) {
                                Vector3f[] points = (Vector3f[]) pair.getValue();
                                if (points[0] == vertex || points[1] == vertex) {
                                    Geometry line = (Geometry) pair.getKey();
                                    updateLine(line, points[0], points[1]);
                                }
                            }
                            break;
                        case "SideAngle": {
                            if (verticesA.get(1).add(deltaAxis).distance(verticesA.get(0)) >= verticesA.get(1).add(deltaAxis.negate()).distance(verticesA.get(0))) {
                                deltaAxis.negateLocal();
                            }
                            Float angle = angleConstant / 2 + angleConstant * Math.round((newPoint.subtract(verticesA.get(1)).normalize().angleBetween(deltaAxis.normalize()) - angleConstant / 2) / angleConstant);
                            Float current = angleConstant / 2 + angleConstant * Math.round((verticesA.get(0).subtract(verticesA.get(1)).normalize().angleBetween(deltaAxis.normalize()) - angleConstant / 2) / angleConstant);
                            if (current - angle != 0.0f) {
                                app.text.setText("Yes");
                                Vector3f rotationVectorA = verticesA.get(0).subtract(verticesA.get(1)).cross(deltaAxis);
                                Vector3f rotationVectorB = verticesB.get(0).subtract(verticesB.get(1)).cross(deltaAxis);
                                verticesA.get(0).set(PopUpBookTree.rotatePoint(verticesA.get(0), verticesA.get(1).add(rotationVectorA), verticesA.get(1), current - angle));
                                verticesB.get(0).set(PopUpBookTree.rotatePoint(verticesB.get(0), verticesB.get(1).add(rotationVectorB), verticesB.get(1), current - angle));
                                Plane plane = new Plane();
                                plane.setPlanePoints(verticesA.get(0), verticesA.get(1), verticesA.get(2));
                                String change = "nope";
                                float distance = 0;
                                for (Vector3f point : verticesA) {
                                    if (FastMath.abs(plane.pseudoDistance(point)) > FastMath.FLT_EPSILON && !point.equals(verticesA.get(0)) && !point.equals(verticesA.get(1)) && !point.equals(verticesA.get(2))) {
                                        point.set(plane.getClosestPoint(point));

                                    }
                                }
                                plane.setPlanePoints(verticesB.get(0), verticesB.get(1), verticesB.get(2));
                                for (Vector3f point : verticesB) {
                                    if (FastMath.abs(plane.pseudoDistance(point)) > FastMath.FLT_EPSILON && !point.equals(verticesB.get(0)) && !point.equals(verticesB.get(1)) && !point.equals(verticesB.get(2))) {
                                        point.set(plane.getClosestPoint(point));
                                    }
                                }
                                updateBoundaries();
                            }

                            if (verticesA.get(0).equals(dotVecticesMap.get(selected))) {
                                Vector3f original = newPoint.subtract(verticesA.get(1));
                                app.text.setText("Changing A");
                                float length = original.length() * FastMath.cos(original.angleBetween(verticesA.get(0).subtract(verticesA.get(1))));
                                if (length < 0.5f) {
                                    length = 0.5f;
                                }
                                verticesA.get(0).set(verticesA.get(0).subtract(verticesA.get(1)).normalize().mult(length).add(verticesA.get(1)));

                            } else {

                                app.text.setText("Changing B");
                                Vector3f original = newPoint.subtract(verticesB.get(1));
                                float length = original.length() * FastMath.cos(original.angleBetween(verticesB.get(0).subtract(verticesB.get(1))));
                                if (length < 0.5f) {
                                    length = 0.5f;
                                }
                                verticesB.get(0).set(verticesB.get(0).subtract(verticesB.get(1)).normalize().mult(length).add(verticesB.get(1)));
                            }
                            
                            fitInBoundaries();
                            updateGraphics();
                            break;
                        }
                        case "TopAngle": {
                            //TopAngle
                            Float targetAngle = angleConstant + angleConstant * Math.round((newPoint.subtract(verticesA.get(1)).normalize().angleBetween(deltaAxis.normalize()) - angleConstant) / angleConstant);
                            if (targetAngle == 0f) {
                                targetAngle = angleConstant;
                            } else if (targetAngle == FastMath.PI) {
                                targetAngle -= angleConstant;
                            }
                            app.text.setText("Angle: " + targetAngle * 180 / 3.14);
                            Vector3f rotationNormal = deltaAxis.cross(verticesA.get(2).subtract(verticesA.get(1)).normalize());
                            Float currentAngle = angleConstant + angleConstant * Math.round((verticesA.get(2).subtract(verticesA.get(1)).normalize().angleBetween(deltaAxis.normalize()) - angleConstant) / angleConstant);
                            if (FastMath.abs(currentAngle - targetAngle) > FastMath.FLT_EPSILON) {
                                verticesA.get(2).set(PopUpBookTree.rotatePoint(verticesA.get(2), verticesA.get(1), verticesA.get(1).add(rotationNormal), currentAngle - targetAngle));
                                updateBoundaries();
                            }
                            
                            verticesA.get(2).set(Util.closestPointOnLine(verticesA.get(1), verticesA.get(2).subtract(verticesA.get(1)).normalize(), newPoint));
                            Plane plane = new Plane();
                            plane.setPlanePoints(verticesA.get(0), verticesA.get(1), verticesA.get(2));
                            for (Vector3f point : verticesA) {
                                if (!plane.isOnPlane(point)) {
                                    point.set(plane.getClosestPoint(point));
                                }
                            }
                            plane.setPlanePoints(verticesB.get(0), verticesB.get(1), verticesB.get(2));
                            for (Vector3f point : verticesB) {
                                if (!plane.isOnPlane(point)) {
                                    point.set(plane.getClosestPoint(point));
                                }
                            }
                            fitInBoundaries();
                            updateGraphics();
                            break;
                        }
                        case "shift": {
                            newPoint = Util.closestPointOnLine(verticesA.get(1), deltaAxis.normalize(), newPoint);
                            if (newPoint.distance(referencePoint) > FastMath.FLT_EPSILON) {
                                app.text.setText("shift");
                                Vector3f translation = newPoint.subtract(referencePoint);
                                Vector3f postTranslation = verticesA.get(1).add(translation);
                                if (FastMath.abs(axisPointA.distance(axisPointB) - axisPointA.distance(postTranslation) - axisPointB.distance(postTranslation)) < FastMath.FLT_EPSILON) {
                                    referencePoint.set(newPoint);
                                    for (Vector3f point : verticesA) {
                                        if (!point.equals(verticesA.get(1)) && !point.equals(verticesA.get(2))) {
                                            point.addLocal(translation);
                                        }
                                    }
                                    for (Vector3f point : verticesB) {
                                        if (!point.equals(verticesA.get(1)) && !point.equals(verticesA.get(2))) {
                                            point.addLocal(translation);
                                        }
                                    }
                                    verticesA.get(1).addLocal(translation);
                                    verticesA.get(2).addLocal(translation);

                                    updateBoundaries();
                                    fitInBoundaries();
                                    fitCenterPoint();
                                    updateGraphics();
                                }

                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }

        }

    }

    private class D1BasicListener implements ActionListener {

        @Override
        public void onAction(String action, boolean isPressed, float tpf) {
            switch (action) {
                case D1_CONFIRM: {
                    if (isPressed) {
                        Vector3f[] boundaryA = verticesA.toArray(new Vector3f[verticesA.size()]);
                        Vector3f[] boundaryB = verticesB.toArray(new Vector3f[verticesB.size()]);

                        PopUpBookTree.PageNode pageA = app.popUpBook.addPage(geometryA, boundaryA, new Vector3f[]{verticesA.get(0).clone(), verticesA.get(1).clone()});
                        PopUpBookTree.PageNode pageB = app.popUpBook.addPage(geometryB, boundaryB, new Vector3f[]{verticesB.get(0).clone(), verticesB.get(1).clone()});
                        app.popUpBook.addJoint(pageA, pageB, new Vector3f[]{verticesA.get(2), verticesA.get(1)}, "D1Joint");

                        app.getStateManager().getState(ExplorationState.class).setEnabled(true);
                        setEnabled(false);
                    }
                    break;
                }

                case D1_ADD: {
                    if (isPressed) {
                        mode = D1_ADD;
                    } else {
                        mode = null;
                    }
                    break;
                }

                case D1_ESCAPE: {
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

                case D1_SELECT: {
                    if (isPressed) {
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
                            if (mode == D1_ADD) {
                                if (closest.getGeometry().getName().equals("Line")) {
                                    Geometry line = closest.getGeometry();
                                    Vector3f[] points = lineVecticesMap.get(line);
                                    if (verticesA.get(1) != points[0] && verticesA.get(1) != points[1]) {
                                        Vector3f newPoint = Util.closestPointOnLine(points[0], points[1].subtract(points[0]), closest.getContactPoint());
                                        if (verticesA.contains(points[0]) && verticesA.contains(points[1])) {
                                            int index = Math.max(verticesA.indexOf(points[0]), verticesA.indexOf(points[1]));
                                            if (verticesA.indexOf(points[0]) == 0 || verticesA.indexOf(points[1]) == 0) {
                                                verticesA.add(newPoint);
                                            } else {
                                                verticesA.add(index, newPoint);
                                            }
                                        } else {
                                            int index = Math.max(verticesB.indexOf(points[0]), verticesB.indexOf(points[1]));
                                            if (verticesB.indexOf(points[0]) == 0 || verticesB.indexOf(points[1]) == 0) {
                                                verticesB.add(newPoint);
                                            } else {
                                                verticesB.add(index, newPoint);
                                            }
                                        }

                                        addDot(newPoint);
                                        addLine(points[1], newPoint);
                                        updateLine(line, points[0], newPoint);
                                        lineVecticesMap.get(line)[0] = points[0];
                                        lineVecticesMap.get(line)[1] = newPoint;

                                    }

                                }
                            } else {
                                //drag movement
                                closest.getGeometry().getMaterial().setColor("Color", ColorRGBA.Yellow);
                                selected = closest.getGeometry();
                                if (selected.getName().equals("Line")) {
                                    //move whole structure
                                    Vector3f centerAxis = verticesA.get(0).subtract(verticesA.get(1)).normalize().add(verticesB.get(0).subtract(verticesB.get(1)).normalize());
                                    Vector3f topA = verticesA.get(1).add(centerAxis.normalize().mult(100)).add(verticesA.get(0).subtract(verticesB.get(0)).normalize().mult(50f));
                                    Vector3f topB = verticesA.get(1).subtract(centerAxis.normalize().mult(100)).add(verticesA.get(0).subtract(verticesB.get(0)).normalize().mult(50f));
                                    Vector3f botB = verticesA.get(1).subtract(centerAxis.normalize().mult(100)).add(verticesB.get(0).subtract(verticesA.get(0)).normalize().mult(50f));
                                    Vector3f botA = verticesA.get(1).add(centerAxis.normalize().mult(100)).add(verticesB.get(0).subtract(verticesA.get(0)).normalize().mult(50f));
                                    Vector3f[] temp = {botA, botB, topB, topA};
                                    Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                    collision.setMaterial(dotMaterial);
                                    collisionNode.attachChild(collision);
                                    //app.getRootNode().attachChild(collision);
                                    results.clear();
                                    collisionNode.collideWith(ray, results);
                                    if (results.size() > 0) {
                                        referencePoint = results.getClosestCollision().getContactPoint();
                                        Vector3f midA = verticesA.get(1).add(deltaAxis.normalize().mult(100));
                                        Vector3f midB = verticesA.get(1).subtract(deltaAxis.normalize().mult(100));
                                        float angle = referencePoint.subtract(midA).angleBetween(midB.subtract(midA));
                                        referencePoint = midA.add(midB.subtract(midA).normalize().mult(FastMath.cos(angle) * referencePoint.distance(midA)));

                                        dragMode = "shift";
                                    }

                                } else {
                                    Vector3f selectedVertex = dotVecticesMap.get(selected);
                                    if (selectedVertex.equals(verticesA.get(1))) {
                                        //center point

                                        app.text.setText("center");
                                    } else if (selectedVertex.equals(verticesA.get(0)) || selectedVertex.equals(verticesB.get(0))) {
                                        //side point
                                        dragMode = "SideAngle";
                                        Vector3f centerTop = verticesA.get(1).add(deltaAxis.normalize().mult(100));
                                        Vector3f centerBot = verticesA.get(1).subtract(deltaAxis.normalize().mult(100));
                                        Vector3f sideTop;
                                        Vector3f sideBot;
                                        if (verticesA.contains(selectedVertex)) {
                                            sideTop = centerTop.add(axisTranslationA.normalize().mult(100));
                                            sideBot = centerBot.add(axisTranslationA.normalize().mult(100));
                                        } else {
                                            sideTop = centerTop.add(axisTranslationB.normalize().mult(100));
                                            sideBot = centerBot.add(axisTranslationB.normalize().mult(100));
                                        }
                                        Vector3f[] temp = {centerTop, centerBot, sideBot, sideTop};

                                        Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                        collision.setMaterial(dotMaterial);
                                        collisionNode.attachChild(collision);

                                    } else if (selectedVertex.equals(verticesA.get(2)) || selectedVertex.equals(verticesB.get(2))) {
                                        //top point
                                        app.text.setText("top");
                                        dragMode = "TopAngle";
                                        Vector3f botA = verticesA.get(1).add(deltaAxis.normalize().mult(100f));
                                        Vector3f botB = verticesA.get(1).subtract(deltaAxis.normalize().mult(100f));
                                        Vector3f up = verticesA.get(0).subtract(verticesA.get(1)).cross(verticesB.get(0).subtract(verticesB.get(1))).normalize();
                                        if (up.add(verticesA.get(1)).distance(verticesA.get(2)) > up.negate().add(verticesA.get(1)).distance(verticesA.get(2))) {
                                            up.negateLocal();
                                        }
                                        Vector3f topA = botA.add(up.mult(100f));
                                        Vector3f topB = botB.add(up.mult(100f));

                                        Vector3f[] temp = {botA, botB, topB, topA};
                                        for (Vector3f point : temp) {
                                            addDot(point);
                                        }
                                        Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                        collision.setMaterial(dotMaterial);
                                        collisionNode.attachChild(collision);
                                    } else {
                                        //free movement
                                        dragMode = "freeMove";

                                        Vector3f up = verticesA.get(2).subtract(verticesA.get(1));
                                        up = verticesA.get(1).add(up.normalize().mult(100));
                                        Vector3f side;
                                        if (verticesA.contains(selectedVertex)) {
                                            side = verticesA.get(0).subtract(verticesA.get(1));
                                        } else {
                                            side = verticesB.get(0).subtract(verticesB.get(1));

                                        }
                                        side = verticesA.get(1).add(side.normalize().mult(100));
                                        Vector3f[] temp = {up, verticesA.get(1), side};

                                        Geometry collision = new Geometry("Collision", Util.makeMesh(temp));
                                        collision.setMaterial(dotMaterial);
                                        collisionNode.attachChild(collision);
                                    }

                                }
                            }
                        }

                    } else {
                        if (selected != null) {
                            if (selected.getName().equals("Dot")) {
                                selected.getMaterial().setColor("Color", ColorRGBA.Red);
                            } else {
                                selected.getMaterial().setColor("Color", ColorRGBA.Black);
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

    D1CreationState(boolean b) {
        setEnabled(b);
    }

    @Override
    protected void initialize(Application app) {
        this.app = (PopUpBook) app;
        inputManager = app.getInputManager();
        inputManager.addMapping(D1_ESCAPE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping(D1_SELECT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(D1_ADD, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(D1_CONFIRM, new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_NUMPADENTER));
        inputManager.addMapping(D1_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_X, false));

        dotMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dotMaterial.setColor("Color", ColorRGBA.Red);
        lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.Black);
        sphere = new Sphere(sample, sample, sphereRadius);

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
        inputManager.addListener(d1BasicInput, D1_ESCAPE);
        inputManager.addListener(d1BasicInput, D1_SELECT);
        inputManager.addListener(d1BasicInput, D1_ADD);
        inputManager.addListener(d1BasicInput, D1_CONFIRM);
        inputManager.addListener(d1MouseListener, D1_MOUSE_MOVE);
        initialize();
    }

    private void initialize() {
        geometryA = app.selected.get(0);
        geometryB = app.selected.get(1);
        PopUpBookTree.PageNode pageA = app.popUpBook.geomPageMap.get(geometryA);
        PopUpBookTree.PageNode pageB = app.popUpBook.geomPageMap.get(geometryB);
        boolean tShape = false;
        if (pageA.next.contains(pageB)) {
            geometryA = app.selected.get(1);
            geometryB = app.selected.get(0);
            pageA = app.popUpBook.geomPageMap.get(geometryA);
            pageB = app.popUpBook.geomPageMap.get(geometryB);
            tShape = true;
            System.out.println("TShape");
        } else if (pageB.next.contains(pageA)) {
            tShape = true;
            System.out.println("TShape");
        }
        Vector3f[] listA = pageA.boundary;
        Vector3f[] listB = pageB.boundary;
        Vector3f[] jointPoint = app.popUpBook.axisBetween(geometryA, geometryB);

        axisPointA = jointPoint[0];
        axisPointB = jointPoint[1];

        Vector3f center = axisPointA.add(axisPointB).divide(2);
        Vector3f endPoint = axisPointA.subtract(axisPointB).mult(0.4f).add(center);
        deltaAxis = center.subtract(endPoint);
        Float angle = FastMath.tan(angleConstant * 3 / 2) * center.distance(endPoint);

        for (Vector3f point : listA) {
            if (!Util.inLine(center, point, endPoint)) {
                axisTranslationA = Util.lineToPointTranslation(endPoint, deltaAxis, point).mult(angle);
                //addDot(point);
                break;
            }
        }
        for (Vector3f point : listB) {
            if (!point.equals(jointPoint[0]) && !point.equals(jointPoint[1])) {
                axisTranslationB = Util.lineToPointTranslation(endPoint, deltaAxis, point).mult(angle);
                break;
            }
        }

        verticesA = new ArrayList<>();
        verticesB = new ArrayList<>();
        verticesA.add(endPoint.add(axisTranslationA));
        if (tShape) {
            if (endPoint.add(axisTranslationB).distance(verticesA.get(0)) < app.popUpBook.predictWhenFold(geometryA, verticesA.get(0), 0.5f).distance(app.popUpBook.predictWhenFold(geometryB, endPoint.add(axisTranslationB), 0.5f))) {
                axisTranslationB.negateLocal();
            }
        }

        verticesB.add(endPoint.add(axisTranslationB));

        verticesA.add(center.clone());
        verticesB.add(verticesA.get(1));

        Vector3f up = verticesA.get(0).subtract(verticesA.get(1)).cross(verticesB.get(0).subtract(verticesB.get(1))).normalize().mult(deltaAxis.length() / 1.61f);
        if (tShape) {
            Vector3f testPoint = app.popUpBook.predictWhenFold(geometryA, verticesA.get(0), 0.01f).add(app.popUpBook.predictWhenFold(geometryB, verticesB.get(0), 0.01f)).divide(2);
            if (endPoint.add(up).distance(testPoint) > endPoint.add(up.negate()).distance(testPoint)) {
                up.negateLocal();
            }
        }else{
            if(verticesA.get(1).add(up).distance(app.getCamera().getLocation()) >verticesA.get(1).add(up.negate()).distance(app.getCamera().getLocation())){
                up.negateLocal();
            }
        }

        verticesA.add(up.add(center).clone());
        verticesB.add(verticesA.get(2));
        updateBoundaries();
        fitInBoundaries();
        addDot(verticesA.get(1));
        addDot(verticesA.get(0));
        addDot(verticesA.get(2));
        addDot(verticesB.get(0));

        addLine(verticesA.get(0), verticesA.get(1));
        addLine(verticesA.get(0), verticesA.get(2));
        addLine(verticesA.get(2), verticesA.get(1));
        addLine(verticesB.get(1), verticesB.get(0));
        addLine(verticesB.get(2), verticesB.get(0));

    }

    private void fitInBoundaries() {
        if (!boundaryA.isEmpty()) {
            float length = verticesA.get(0).distance(verticesA.get(1));
            if (length > boundaryA.get(1).distance(boundaryA.get(0))) {
                length = boundaryA.get(1).distance(boundaryA.get(0));
            }
            if (length < 0.5f) {
                length = 0.5f;
            }
            verticesA.get(0).set(verticesA.get(0).subtract(verticesA.get(1)).normalize().mult(length).add(verticesA.get(1)));

            length = verticesB.get(0).distance(verticesB.get(1));
            if (length > boundaryB.get(1).distance(boundaryB.get(0))) {
                length = boundaryB.get(1).distance(boundaryB.get(0));
            }
            if (length < 0.5f) {
                length = 0.5f;
            }
            verticesB.get(0).set(verticesB.get(0).subtract(verticesB.get(1)).normalize().mult(length).add(verticesB.get(1)));

            length = verticesA.get(1).distance(verticesA.get(2));
            if (length > boundaryA.get(1).distance(boundaryA.get(2))) {
                length = boundaryA.get(1).distance(boundaryA.get(2));
            }
            if (length < 0.5f) {
                length = 0.5f;
            }
            verticesA.get(2).set(verticesA.get(2).subtract(verticesA.get(1)).normalize().mult(length).add(verticesA.get(1)));
            Plane planeBot = new Plane();
            Plane planeTop = new Plane();

            Vector3f planeNormal = verticesA.get(0).subtract(verticesA.get(1)).cross(verticesA.get(2).subtract(verticesA.get(1)));
            planeBot.setPlanePoints(verticesA.get(0), verticesA.get(1), verticesA.get(1).add(planeNormal));
            planeTop.setPlanePoints(verticesA.get(2), verticesA.get(1), verticesA.get(1).add(planeNormal));
            //ArrayList<Vector3f> remove = new ArrayList<>();
            for (int i = 3; i < verticesA.size(); i++) {
                if (FastMath.abs(planeBot.pseudoDistance(verticesA.get(i))) < FastMath.FLT_EPSILON || FastMath.abs(planeTop.pseudoDistance(verticesA.get(i))) < FastMath.FLT_EPSILON) {
                    //remove.add(verticesA.get(i));
                    app.text.setText("on Base or Top");
                } else {
                    if (planeBot.whichSide(verticesA.get(i)).equals(planeBot.whichSide(verticesA.get(2)))
                            && planeTop.whichSide(verticesA.get(i)).equals(planeTop.whichSide(verticesA.get(0)))) {
                        app.text.setText("rightPlace");
                        for (int x = 2; x < boundaryA.size(); x++) {
                            Vector3f point1 = boundaryA.get(x);
                            Vector3f point2;
                            if (x == boundaryA.size() - 1) {
                                point2 = boundaryA.get(0);
                            } else {
                                point2 = boundaryA.get(x + 1);
                            }
                            Float angle1 = point1.subtract(verticesA.get(1)).normalize().angleBetween(verticesA.get(0).subtract(verticesA.get(1)).normalize());
                            Float angle2 = point2.subtract(verticesA.get(1)).normalize().angleBetween(verticesA.get(0).subtract(verticesA.get(1)).normalize());
                            Float targetAngle = verticesA.get(i).subtract(verticesA.get(1)).normalize().angleBetween(verticesA.get(0).subtract(verticesA.get(1)).normalize());
                            if ((angle1 > targetAngle && angle2 < targetAngle)) {
                                Plane plane = new Plane();
                                plane.setPlanePoints(point1, point2, point1.add(planeNormal));
                                if (plane.whichSide(verticesA.get(i)).equals(plane.whichSide(verticesA.get(1)))) {
                                    break;
                                } else {
                                    verticesA.get(i).set(plane.getClosestPoint(verticesA.get(i)));

                                    plane.setPlanePoints(point1, boundaryA.get(x - 1), point1.add(planeNormal));
                                    if (!plane.whichSide(verticesA.get(i)).equals(plane.whichSide(point1.add(point2).divide(2)))) {
                                        verticesA.get(i).set(point1);
                                        break;

                                    }
                                    plane.setPlanePoints(point2, boundaryA.get((x + 2) % boundaryA.size()), point2.add(planeNormal));

                                    if (!plane.whichSide(verticesA.get(i)).equals(plane.whichSide(point1.add(point2).divide(2)))) {
                                        verticesA.get(i).set(point2);
                                        break;
                                    }
//                                    

                                    break;
                                }
                            }
                        }
                    } else {
                        //remove.add(verticesA.get(i));
                        app.text.setText("not inBetween");
                    }
                }
            }
            planeNormal = verticesB.get(0).subtract(verticesB.get(1)).cross(verticesB.get(2).subtract(verticesB.get(1)));
            planeBot.setPlanePoints(verticesB.get(0), verticesB.get(1), verticesB.get(1).add(planeNormal));
            planeTop.setPlanePoints(verticesB.get(2), verticesB.get(1), verticesB.get(1).add(planeNormal));
            //ArrayList<Vector3f> remove = new ArrayList<>();
            for (int i = 3; i < verticesB.size(); i++) {
                if (FastMath.abs(planeBot.pseudoDistance(verticesB.get(i))) < FastMath.FLT_EPSILON || FastMath.abs(planeTop.pseudoDistance(verticesB.get(i))) < FastMath.FLT_EPSILON) {
                    //remove.add(verticesA.get(i));
                    app.text.setText("on Base or Top");
                } else {
                    if (planeBot.whichSide(verticesB.get(i)).equals(planeBot.whichSide(verticesB.get(2)))
                            && planeTop.whichSide(verticesB.get(i)).equals(planeTop.whichSide(verticesB.get(0)))) {
                        app.text.setText("rightPlace");
                        for (int x = 2; x < boundaryB.size(); x++) {
                            Vector3f point1 = boundaryB.get(x);
                            Vector3f point2;
                            if (x == boundaryB.size() - 1) {
                                point2 = boundaryB.get(0);
                            } else {
                                point2 = boundaryB.get(x + 1);
                            }
                            Float angle1 = point1.subtract(verticesB.get(1)).normalize().angleBetween(verticesB.get(0).subtract(verticesB.get(1)).normalize());
                            Float angle2 = point2.subtract(verticesB.get(1)).normalize().angleBetween(verticesB.get(0).subtract(verticesB.get(1)).normalize());
                            Float targetAngle = verticesB.get(i).subtract(verticesB.get(1)).normalize().angleBetween(verticesB.get(0).subtract(verticesB.get(1)).normalize());
                            if ((angle1 > targetAngle && angle2 < targetAngle)) {
                                Plane plane = new Plane();
                                plane.setPlanePoints(point1, point2, point1.add(planeNormal));
                                if (plane.whichSide(verticesB.get(i)).equals(plane.whichSide(verticesB.get(1)))) {
                                    break;
                                } else {
                                    verticesB.get(i).set(plane.getClosestPoint(verticesB.get(i)));

                                    plane.setPlanePoints(point1, boundaryB.get(x - 1), point1.add(planeNormal));
                                    if (!plane.whichSide(verticesB.get(i)).equals(plane.whichSide(point1.add(point2).divide(2)))) {
                                        verticesB.get(i).set(point1);
                                        break;

                                    }
                                    plane.setPlanePoints(point2, boundaryB.get((x + 2) % boundaryB.size()), point2.add(planeNormal));

                                    if (!plane.whichSide(verticesB.get(i)).equals(plane.whichSide(point1.add(point2).divide(2)))) {
                                        verticesB.get(i).set(point2);
                                        break;
                                    }
//                                    

                                    break;
                                }
                            }
                        }
                    } else {
                        //remove.add(verticesA.get(i));
                        app.text.setText("not inBetween");
                    }
                }
            }

        }

    }

    private void fitCenterPoint() {
        if (verticesA.get(1).distance(axisPointA) < 0.5f || verticesA.get(1).distance(axisPointB) < 0.5f) {
            Vector3f closerPoint;
            Vector3f translation;
            if (axisPointA.distance(verticesA.get(1)) < axisPointB.distance(verticesB.get(1))) {
                closerPoint = axisPointA;
                translation = axisPointB.subtract(axisPointA).normalize().mult(0.5f).add(axisPointA).subtract(verticesA.get(1));
            } else {
                closerPoint = axisPointB;
                translation = axisPointA.subtract(axisPointB).normalize().mult(0.5f).add(axisPointB).subtract(verticesA.get(1));
            }

            for (Vector3f point : verticesA) {
                if (!(point.equals(verticesA.get(1)) || point.equals(verticesA.get(2)))) {
                    point.set(point.add(translation));
                }
            }
            for (Vector3f point : verticesB) {
                if (!(point.equals(verticesA.get(1)) || point.equals(verticesA.get(2)))) {
                    point.set(point.add(translation));
                }
            }
            verticesA.get(1).set(verticesA.get(1).add(translation));
            verticesA.get(2).set(verticesA.get(2).add(translation));
            updateGraphics();
        }
    }

    private void updateBoundaries() {
        System.out.println("called");
        ArrayList<ArrayList<Vector3f>> results = app.popUpBook.getBoundarys(geometryA, geometryB, verticesA, verticesB, "D1Joint");

        if (results != null) {
            boundaryA = results.get(0);
            boundaryB = results.get(1);
            if (boundaryAGeom == null) {
                boundaryAGeom = new Geometry(mode, Util.makeMesh(boundaryA.toArray(new Vector3f[boundaryA.size()])));
                boundaryBGeom = new Geometry(mode, Util.makeMesh(boundaryB.toArray(new Vector3f[boundaryB.size()])));
                Material allowed = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                allowed.setColor("Color", new ColorRGBA(0, 1, 0, 0.5f));
                allowed.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                allowed.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

                boundaryAGeom.setQueueBucket(Bucket.Transparent);
                boundaryBGeom.setQueueBucket(Bucket.Transparent);
                boundaryAGeom.setMaterial(allowed);
                boundaryBGeom.setMaterial(allowed.clone());
                tempNode.attachChild(boundaryAGeom);
                tempNode.attachChild(boundaryBGeom);

            } else {
                boundaryAGeom.setMesh(Util.makeMesh(boundaryA.toArray(new Vector3f[boundaryA.size()])));
                boundaryBGeom.setMesh(Util.makeMesh(boundaryB.toArray(new Vector3f[boundaryB.size()])));
            }
        }

    }

    private void addLine(Vector3f from, Vector3f to) {
        Geometry line = new Geometry("Line", new Cylinder());
        line.setMaterial(lineMaterial);
        updateLine(line, to, from);
        lineVecticesMap.put(line, new Vector3f[]{from, to});
        frameNode.attachChild(line);
    }

    private void updateLine(Geometry line, Vector3f vertexA, Vector3f vertexB) {
        if (line.getName().equals("Line")) {
            line.setLocalTranslation(vertexA.add(vertexB).divide(2f));
            ((Cylinder) line.getMesh()).updateGeometry(sample, sample, lineRadius, lineRadius, vertexA.distance(vertexB), false, false);
            line.lookAt(vertexA, new Vector3f(0, 1, 0));
            line.getMesh().createCollisionData();
        }
    }

    private void addDot(Vector3f dotLocation) {
        if (!dotVecticesMap.values().contains(dotLocation)) {
            Geometry dot = new Geometry("Dot", sphere);
            dot.setMaterial(dotMaterial.clone());
            dot.setLocalTranslation(dotLocation);
            frameNode.attachChild(dot);
            dotVecticesMap.put(dot, dotLocation);
        }
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(d1BasicInput);
        app.getRootNode().detachChild(tempNode);
    }

    private void updateGraphics() {
        for (HashMap.Entry pair : dotVecticesMap.entrySet()) {
            ((Geometry) pair.getKey()).setLocalTranslation((Vector3f) pair.getValue());
        }
        for (HashMap.Entry<Geometry,Vector3f[]> pair : lineVecticesMap.entrySet()) {
            Vector3f[] points = pair.getValue();
            updateLine(pair.getKey(), points[0], points[1]);
        }
    }


}
