/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Evans
 */
public class PopUpBookTree {

    private Node rootNode;
    public  PageNode front;
    public PageNode back;
    private JointNode bookJoint;
    private ArrayList<PageNode> pages;
    public HashMap<Geometry, PageNode> geomPageMap = new HashMap<>();
    private HashMap<Geometry, Vector3f[]> geomBoundary = new HashMap<>();
    private ArrayList<JointNode> joints;
    public float height;
    public float width;
    public Node planes;
    public Node lines;
    private Geometry mark1;
    private Geometry mark2;
    private Geometry mark3;
    private Geometry mark4;
    private PopUpBook app;

    PopUpBookTree(float width, float height, PopUpBook app) {

        this.app = app;
        this.width = width;
        this.height = height;
        planes = app.planes;
        lines = new Node("Line");
        app.getRootNode().attachChild(lines);

//        Sphere sphere = new Sphere(30, 30, 0.2f);
//        mark1 = new Geometry("BOOM!", sphere);
//        Material mark_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//        mark_mat.setColor("Color", ColorRGBA.Red);
//        mark1.setMaterial(mark_mat.clone());
//        planes.attachChild(mark1);
//        mark2 = new Geometry("BOOM!", sphere);
//        mark_mat.setColor("Color", ColorRGBA.Yellow);
//        mark2.setMaterial(mark_mat.clone());
//        planes.attachChild(mark2);
//        mark3 = new Geometry("BOOM!", sphere);
//        mark_mat.setColor("Color", ColorRGBA.Blue);
//        mark3.setMaterial(mark_mat.clone());
//        planes.attachChild(mark3);
//        mark4 = new Geometry("BOOM!", sphere);
//        mark_mat.setColor("Color", ColorRGBA.Black);
//        mark4.setMaterial(mark_mat.clone());
//        planes.attachChild(mark4);

        Vector3f[] backBoundary = new Vector3f[4];
        backBoundary[0] = new Vector3f(0f, 0f, -height / 2f);
        backBoundary[1] = new Vector3f(width, 0f, -height / 2f);
        backBoundary[2] = new Vector3f(width, 0f, height / 2f);
        backBoundary[3] = new Vector3f(0f, 0f, height / 2f);

        Vector3f[] frontBoundary = new Vector3f[4];
        frontBoundary[3] = new Vector3f(0f, 0f, -height / 2);
        frontBoundary[2] = new Vector3f(-width, 0f, -height / 2);
        frontBoundary[1] = new Vector3f(-width, 0f, height / 2);
        frontBoundary[0] = new Vector3f(0f, 0f, height / 2);
        pages = new ArrayList<>();
        joints = new ArrayList<>();

        back = addPage(null, backBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        front = addPage(null, frontBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        bookJoint = new JointNode(back, front, new Vector3f[] {new Vector3f(0f, 0f, -height / 2f), new Vector3f(0f, 0f, height / 2f)},  "D1Joint");
        back.relatedJoint.add(bookJoint);
        front.relatedJoint.add(bookJoint);
        joints.add(bookJoint);

    }

    public Geometry getFront() {
        return front.geometry;
    }


    public PageNode addPage(Geometry prev, Vector3f[] boundary, Vector3f[] axis) {

        System.out.println("actually");
        Geometry geometry = new Geometry("Page", Util.makeMesh(boundary));
        geometry.setMaterial(app.paper);
        planes.attachChild(geometry);
        PageNode page = new PageNode(prev, geometry, axis, boundary);
        pages.add(page);
        geomPageMap.put(geometry, page);

        return page;
    }

    public void addJoint(PageNode pageA, PageNode pageB, Vector3f[] axis, String type) {
        JointNode joint = new JointNode(pageA, pageB, axis, type);
        pageA.relatedJoint.add(joint);
        pageB.relatedJoint.add(joint);
        joints.add(joint);
    }

    public void fold(float percent) {
        reset();
        fold(percent, true);
    }

    private void fold(float percent, boolean update) {
        if (update) {
            reset();
        }
        if (percent != 0f) {
            front.rotate(new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z}, percent * FastMath.PI, true);
            back.rotate(new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z}, 0, true);
            Queue<JointNode> toUpdateList = new LinkedList<>();
            for (JointNode joint : joints) {
                toUpdateList.add(joint);
            }
            toUpdateList.remove(bookJoint);
            while (!toUpdateList.isEmpty()) {
                JointNode currentJoint = toUpdateList.remove();
                if (currentJoint.ready()) {
                    currentJoint.fixJoint();
                } else {
                    toUpdateList.add(currentJoint);
                }
            }
        }

        if (update) {
            update();
        }

    }

    public Vector3f[] axisBetween(Geometry geomA, Geometry geomB) {
        PageNode pageA = geomPageMap.get(geomA);
        PageNode pageB = geomPageMap.get(geomB);

        if ((pageA.equals(front) && pageB.equals(back)) || (pageA.equals(back) && pageB.equals(front))) {
            return new Vector3f[]{new Vector3f(0f, 0f, -height / 2f), new Vector3f(0f, 0f, height / 2f)};
        }
        if (pageA.next.contains(pageB)) {
            return pageB.axis;
        }
        if (pageB.next.contains(pageA)) {
            return pageA.axis;
        }
        for (JointNode joint : joints) {
            if (joint.pageA.equals(pageA) && joint.pageB.equals(pageB) || joint.pageB.equals(pageA) && joint.pageA.equals(pageB)) {
                return joint.jointAxis;
            }
        }
        return null;
    }

    public boolean isNeighbor(Geometry geomA, Geometry geomB) {
        if(geomA == null || geomB == null){
            return false;
        }
        return geomPageMap.get(geomB).isNeighbor(geomPageMap.get(geomA));
    }

    public Vector3f predictWhenFold(Geometry parent, Vector3f point, float percent) {
        reset();
        point = point.clone();
        ArrayList<Vector3f> pointWrapper = new ArrayList();
        pointWrapper.add(point);
        geomPageMap.get(parent).attatched = pointWrapper;
        fold(percent, false);
        return point;
    }

    public ArrayList<ArrayList<Vector3f>> getBoundarys(Geometry parentA, Geometry parentB, ArrayList<Vector3f> pointsA, ArrayList<Vector3f> pointsB, String type) {
        reset();
        ArrayList<ArrayList<Vector3f>> returnArray = new ArrayList();
        
        switch (type) {
            case "D1Joint": {
                PageNode pageA = new PageNode(parentA, new Vector3f[]{pointsA.get(0), pointsA.get(1)}, Util.toArray(pointsA));
                PageNode pageB = new PageNode(parentB, new Vector3f[]{pointsB.get(0), pointsB.get(1)}, Util.toArray(pointsB));
                JointNode joint = new JointNode(pageA, pageB, new Vector3f[]{pointsA.get(2), pointsA.get(1)}, "D1Joint");
                pageA.relatedJoint.add(joint);
                pageB.relatedJoint.add(joint);
                joints.add(joint);
                fold(0.999f, false);

                ArrayList<Vector3f> boundaryA = new ArrayList();
                ArrayList<Vector3f> boundaryB = new ArrayList();
                returnArray.add(boundaryA);
                returnArray.add(boundaryB);

                Vector3f center = new Vector3f();
                center.set(width / 2, 0, 0);
                Geometry pageBox = new Geometry("Box", new Box(center, width / 2, 0.5f, height / 2));
                Node temp = new Node("temp");
                temp.attachChild(pageBox);
                CollisionResults results = new CollisionResults();
                try {
                    temp.collideWith(new Ray(pageA.translatedBuffer[1], pageA.translatedBuffer[0].subtract(pageA.translatedBuffer[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(pageB.translatedBuffer[1], pageB.translatedBuffer[0].subtract(pageB.translatedBuffer[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    boundaryA.add(pageA.translatedBuffer[1]);
                    boundaryB.add(pageB.translatedBuffer[1]);

                    temp.collideWith(new Ray(pageA.translatedBuffer[1], pageA.translatedBuffer[2].subtract(pageA.translatedBuffer[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(pageB.translatedBuffer[1], pageB.translatedBuffer[2].subtract(pageB.translatedBuffer[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();
                } catch (Exception e) {
                    pages.remove(pageA);
                    pages.remove(pageB);
                    joints.remove(joints.size()-1);
                    return null;
                }

                ArrayList<Vector3f> original = pointsA;

                for (ArrayList<Vector3f> boundary : returnArray) {
                    Plane thresholdPlane1 = new Plane();
                    Plane thresholdPlane2 = new Plane();
                    thresholdPlane1.setPlanePoints(boundary.get(0), boundary.get(1), boundary.get(1).add(new Vector3f(0, 1, 0)));
                    thresholdPlane2.setPlanePoints(boundary.get(2), boundary.get(1), boundary.get(2).add(new Vector3f(0, 1, 0)));

                    Plane.Side wrongSide1 = thresholdPlane1.whichSide(boundary.get(2));
                    Plane.Side wrongSide2 = thresholdPlane2.whichSide(boundary.get(0));

                    ArrayList<Vector3f> legalPoint = new ArrayList<>();
                    for (Vector3f point : back.boundary) {
                        if (!thresholdPlane1.isOnPlane(point) && !thresholdPlane2.isOnPlane(point)
                                && thresholdPlane1.whichSide(point).equals(wrongSide1) && thresholdPlane2.whichSide(point).equals(wrongSide2)) {
                            legalPoint.add(point.clone());
                        }
                    }

                    while (!legalPoint.isEmpty()) {
                        int closest = -1;
                        Float distance = Float.MAX_VALUE;
                        for (int i = 0; i < legalPoint.size(); i++) {
                            if (legalPoint.get(i).distance(boundary.get(boundary.size() - 1)) < distance) {
                                distance = legalPoint.get(i).distance(boundary.get(boundary.size() - 1));
                                closest = i;
                            }
                        }
                        boundary.add(legalPoint.remove(closest));
                    }

                    Vector3f transformation = original.get(1).subtract(boundary.get(1));
                    if (transformation.distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                        for (Vector3f point : boundary) {
                            point.addLocal(transformation);
                        }
                    }

                    Vector3f v = boundary.get(0).subtract(boundary.get(1)).normalize();
                    Vector3f u = original.get(0).subtract(original.get(1)).normalize();
                    float angle = u.angleBetween(v);
                    if (angle > FastMath.FLT_EPSILON) {
                        for (Vector3f point : boundary) {
                            point.set(rotatePoint(point, original.get(1), original.get(1).add(u.cross(v)), angle));
                        }
                    }
                    angle = (u.cross(boundary.get(2).subtract(original.get(1))).normalize()).angleBetween(
                            u.cross(original.get(2).subtract(original.get(1))).normalize());
                    if (original.get(2).distance(rotatePoint(boundary.get(2), original.get(0), original.get(1), angle))
                            > original.get(2).distance(rotatePoint(boundary.get(2), original.get(1), original.get(0), angle))) {
                        angle = angle * -1;
                    }
                    if (FastMath.abs(angle) > FastMath.FLT_EPSILON) {
                        for (Vector3f point : boundary) {
                            point.set(rotatePoint(point, boundary.get(0), boundary.get(1), angle));
                        }
                    }
                    original = pointsB;
                }
                pages.remove(pageA);
                pages.remove(pageB);
                joints.remove(joint);
                break;
            }
            case "D2Joint": {
                float length = pointsA.get(0).distance(pointsA.get(3))* FastMath.cos(pointsA.get(1).subtract(pointsA.get(0)).normalize().angleBetween(pointsA.get(3).subtract(pointsA.get(0)).normalize()));
                Vector3f jointPoint = pointsA.get(3).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(length));
                length = pointsA.get(0).distance(pointsB.get(0))* FastMath.cos(pointsA.get(1).subtract(pointsA.get(0)).normalize().angleBetween(pointsB.get(0).subtract(pointsA.get(0)).normalize()));
                Vector3f jointPointB = pointsB.get(0).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(length));
                
                PageNode pageA = new PageNode(parentA, new Vector3f[]{pointsA.get(0), pointsA.get(1)}, Util.toArray(pointsA));
                PageNode pageB = new PageNode(parentB, new Vector3f[]{jointPointB, pointsB.get(1)}, Util.toArray(pointsB));
                JointNode joint = new JointNode(pageA, pageB, new Vector3f[]{jointPoint, pointsA.get(2).clone()}, "D2Joint");
                pageA.relatedJoint.add(joint);
                pageB.relatedJoint.add(joint);
                joints.add(joint);
                fold(0.999f, false);

                ArrayList<Vector3f> boundaryA = new ArrayList();
                ArrayList<Vector3f> boundaryB = new ArrayList();
                returnArray.add(boundaryA);
                returnArray.add(boundaryB);
                Vector3f center = new Vector3f();
                center.set(width / 2, 0, 0);
                Geometry pageBox = new Geometry("Box", new Box(center, width / 2, 0.5f, height / 2));
                Node temp = new Node("temp");
                temp.attachChild(pageBox);
                CollisionResults results = new CollisionResults();
                try {
                    temp.collideWith(new Ray(pageA.translatedBuffer[1], pageA.translatedBuffer[0].subtract(pageA.translatedBuffer[1])), results);
                    pageA.translatedBuffer[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(1).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(pageA.translatedBuffer[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(pageA.translatedBuffer[0], pageA.translatedBuffer[1].subtract(pageA.translatedBuffer[0])), results);
                    pageA.translatedBuffer[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(0).add(pointsA.get(1).subtract(boundaryA.get(0)).normalize().mult(pageA.translatedBuffer[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    
                    temp.collideWith(new Ray(pageA.translatedBuffer[3], pageA.translatedBuffer[2].subtract(pageA.translatedBuffer[3])), results);
                    pageA.translatedBuffer[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(3).add(pointsA.get(2).subtract(pointsA.get(3)).normalize().mult(pageA.translatedBuffer[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    
                    temp.collideWith(new Ray(pageA.translatedBuffer[2], pageA.translatedBuffer[3].subtract(pageA.translatedBuffer[2])), results);
                    pageA.translatedBuffer[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(2).add(pointsA.get(3).subtract(boundaryA.get(2)).normalize().mult(pageA.translatedBuffer[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    
                    
                    temp.collideWith(new Ray(pageB.translatedBuffer[1], pageB.translatedBuffer[0].subtract(pageB.translatedBuffer[1])), results);
                    pageB.translatedBuffer[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(1).add(pointsB.get(0).subtract(pointsB.get(1)).normalize().mult(pageB.translatedBuffer[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(pageB.translatedBuffer[0], pageB.translatedBuffer[1].subtract(pageB.translatedBuffer[0])), results);
                    pageB.translatedBuffer[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(0).add(pointsB.get(1).subtract(boundaryB.get(0)).normalize().mult(pageB.translatedBuffer[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    
                    temp.collideWith(new Ray(pageB.translatedBuffer[3], pageB.translatedBuffer[2].subtract(pageB.translatedBuffer[3])), results);
                    pageB.translatedBuffer[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(3).add(pointsB.get(2).subtract(pointsB.get(3)).normalize().mult(pageB.translatedBuffer[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    
                    temp.collideWith(new Ray(pageB.translatedBuffer[2], pageB.translatedBuffer[3].subtract(pageB.translatedBuffer[2])), results);
                    pageB.translatedBuffer[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(2).add(pointsB.get(3).subtract(boundaryB.get(2)).normalize().mult(pageB.translatedBuffer[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();
                    

                } catch (Exception e) {
                    pages.remove(pageA);
                    pages.remove(pageB);
                    joints.remove(joint);
                    return null;
                }
                pages.remove(pageA);
                pages.remove(pageB);
                joints.remove(joint);
                break;
            }
            default:
                break;
        }

        
        return returnArray;
    }

    public void reset() {
        for (PageNode page : pages) {
            page.reset();
        }
        for (JointNode joint : joints) {
            joint.reset();
        }
    }

    void update() {
        for (Spatial line : lines.getChildren()) {
            line.removeFromParent();
            line = null;
        }
        Material lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.Black);

        for (PageNode page : pages) {
            page.geometry.getMesh().getBuffer(VertexBuffer.Type.Position).updateData(BufferUtils.createFloatBuffer(page.translatedBuffer));
            page.geometry.getMesh().createCollisionData();
            for (int i = 0; i < page.translatedBoundary.length; i++) {
                Geometry line = new Geometry("Line", new Cylinder());
                line.setMaterial(lineMaterial);
                line.setLocalTranslation(page.translatedBoundary[i].add(page.translatedBoundary[(i + 1) % page.translatedBoundary.length]).divide(2));
                ((Cylinder) line.getMesh()).updateGeometry(3, 3, 0.01f, 0.01f, page.translatedBoundary[i].distance(page.translatedBoundary[(i + 1) % page.translatedBoundary.length]), false, false);
                line.lookAt(page.translatedBoundary[i], new Vector3f(0, 1, 0));
                lines.attachChild(line);
            }
        }
    }

    public class PageNode {

        public Geometry geometry;
        private VertexBuffer originalBuffer;
        private Vector3f[] translatedBuffer;
        public Vector3f[] axis;
        private Vector3f[] translatedAxis;
        public Vector3f[] boundary;
        private Vector3f[] translatedBoundary;
        private boolean ready;
        public ArrayList<PageNode> next;
        public PageNode parent;
        public ArrayList<JointNode> relatedJoint;
        private ArrayList<Vector3f> attatched;

        private PageNode(Geometry prev, Geometry geometry, Vector3f[] axis, Vector3f[] boundary) {
            this.next = new ArrayList<>();
            
            this.relatedJoint = new ArrayList<>();
            this.geometry = geometry;
            this.axis = axis;
            translatedAxis = new Vector3f[2];
            translatedAxis[0] = axis[0].clone();
            translatedAxis[1] = axis[1].clone();

            originalBuffer = this.geometry.getMesh().getBuffer(VertexBuffer.Type.Position).clone();
            translatedBuffer = BufferUtils.getVector3Array((FloatBuffer) originalBuffer.clone().getData());
            this.boundary = boundary;
            translatedBoundary = new Vector3f[boundary.length];
            for (int i = 0; i < boundary.length; i++) {
                translatedBoundary[i] = boundary[i].clone();
            }
            if (prev != null) {
                geomPageMap.get(prev).next.add(this);
                this.parent = geomPageMap.get(prev);
            }
        }

        private PageNode(Geometry prev, Vector3f[] axis, Vector3f[] buffer) {
            this.axis = axis;
            translatedAxis = new Vector3f[2];
            translatedAxis[0] = axis[0].clone();
            translatedAxis[1] = axis[1].clone();
            geomPageMap.get(prev).next.add(this);
            this.relatedJoint = new ArrayList<>();
            translatedBuffer = buffer;
        }

        private boolean isNeighbor(PageNode page) {
            if(this == page){
                return false;
            }
            if (next.contains(page) || page.next.contains(this)) {
                return true;
            }
            for (JointNode joint : relatedJoint) {
                if (joint.pageA.equals(page) || joint.pageB.equals(page)) {
                    return true;
                }
            }
            return false;
        }

        public Vector3f getNormal() {
            Vector3f vector1 = boundary[0].subtract(boundary[1]);
            Vector3f vector2;
            for (int i = 2; i < boundary.length; i++) {
                vector2 = boundary[i].subtract(boundary[1]);
                if (vector2.distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                    return vector1.cross(vector2);
                }
            }
            return null;
        }

        private void rotateFromTo(Vector3f from, Vector3f to) {
            Plane fromPlane = new Plane();
            Plane toPlane = new Plane();
            fromPlane.setPlanePoints(from, translatedAxis[0], translatedAxis[1]);
            toPlane.setPlanePoints(to, translatedAxis[0], translatedAxis[1]);
            float angle = fromPlane.getNormal().angleBetween(toPlane.getNormal());
            if (rotatePoint(from, translatedAxis[0], translatedAxis[1], angle).distance(to) > rotatePoint(from, translatedAxis[0], translatedAxis[1], -angle).distance(to)) {
                angle = -1 * angle;
            }
            rotate(angle, true);
        }

        private void rotate(float radian, boolean nextReady) {
            rotate(translatedAxis, radian, nextReady);
        }

        private void rotate(Vector3f[] axis, float radian) {
            if (geometry != null) {
                if (attatched != null) {
                    for (Vector3f point : attatched) {
                        point.set(rotatePoint(point, axis[0], axis[1], radian));
                    }
                }
                for (PageNode page : next) {
                    page.rotate(axis, radian);
                }
                for (Vector3f point : translatedBoundary) {
                    point.set(rotatePoint(point, axis[0], axis[1], radian));
                }
            }
            for (Vector3f point : translatedBuffer) {
                point.set(rotatePoint(point, axis[0], axis[1], radian));
            }
            for (JointNode joint : relatedJoint) {
                joint.rotate(this, axis, radian);
            }
            translatedAxis[0].set(rotatePoint(translatedAxis[0], axis[0], axis[1], radian));
            translatedAxis[1].set(rotatePoint(translatedAxis[1], axis[0], axis[1], radian));
        }

        private void rotate(Vector3f[] axis, float radian, boolean nextReady) {
            this.ready = true;
            if (next != null) {
                for (PageNode page : next) {
                    page.ready = true;
                }
            }

            rotate(axis, radian);
        }

        private void reset() {
            translatedAxis[0].set(axis[0].clone());
            translatedAxis[1].set(axis[1].clone());
            attatched = null;
            ready = false;
            if (translatedBoundary != null) {
                translatedBuffer = BufferUtils.getVector3Array((FloatBuffer) originalBuffer.clone().getData());
                for (int i = 0; i < boundary.length; i++) {
                    translatedBoundary[i].set(boundary[i]);
                }
            }
        }
    }

    public class JointNode {

        public String type;
        private PageNode pageA;
        private PageNode pageB;
        public Vector3f[] jointAxis;
        private Vector3f[] axisA;
        private Vector3f[] axisB;
        private HashMap<PageNode, Vector3f[]> translatedJointAxis;
        private Plane.Side upSide;

        private JointNode(PageNode pageA, PageNode pageB, Vector3f[] jointAxis, String type) {
            this.type = type;
            this.jointAxis = jointAxis;
            translatedJointAxis = new HashMap<>();
            axisA = new Vector3f[2];
            axisB = new Vector3f[2];
            axisA[0] = jointAxis[0].clone();
            axisA[1] = jointAxis[1].clone();
            axisB[0] = jointAxis[0].clone();
            axisB[1] = jointAxis[1].clone();
            this.pageA = pageA;
            this.pageB = pageB;
            Plane plane = new Plane();
            plane.setPlanePoints(pageA.translatedBuffer[0], pageA.translatedBuffer[1], pageB.translatedBuffer[0]);
            upSide = plane.whichSide(axisA[0]);
            translatedJointAxis.put(pageA, axisA);
            translatedJointAxis.put(pageB, axisB);
        }
        
        public PageNode theOther(PageNode thisPage){
            if(pageA.equals(thisPage)){
                return pageB;
            }else{
                return pageA;
            }
        }
        
        private void rotate(PageNode page, Vector3f[] axis, float radian) {
            if (page.equals(pageA)) {
                axisA[0].set(rotatePoint(axisA[0], axis[0], axis[1], radian));
                axisA[1].set(rotatePoint(axisA[1], axis[0], axis[1], radian));
            } else {
                axisB[0].set(rotatePoint(axisB[0], axis[0], axis[1], radian));
                axisB[1].set(rotatePoint(axisB[1], axis[0], axis[1], radian));
            }
        }

        private void reset() {
            axisA[0].set(jointAxis[0].clone());
            axisA[1].set(jointAxis[1].clone());
            axisB[0].set(jointAxis[0].clone());
            axisB[1].set(jointAxis[1].clone());
        }

        private boolean fixJoint() {
            switch (type) {
                case "D1Joint": {
                    if (axisA[0].subtract(axisA[1]).cross(axisB[0].subtract(axisB[1])).distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                        Plane planeMid = new Plane();
                        Plane planeA = new Plane();
                        float jointLength = jointAxis[0].distance(jointAxis[1]);

                        planeA.setOriginNormal(axisA[1], pageA.translatedAxis[0].subtract(pageA.translatedAxis[1]));
                        Vector3f midPoint = new Vector3f();

                        midPoint = pageA.translatedAxis[0].subtract(pageA.translatedAxis[1]).normalize().cross(
                                pageB.translatedAxis[0].subtract(pageB.translatedAxis[1]).normalize())
                                .normalize().negate();
                        planeMid.setPlanePoints(pageA.translatedBuffer[0], pageA.translatedBuffer[1], pageB.translatedBuffer[0]);
                        if (!planeMid.whichSide(midPoint.add(pageA.translatedAxis[1])).equals(upSide)) {
                            midPoint.negateLocal();
                        }


                        Vector3f baseA = Util.closestPointOnLine(pageA.translatedAxis[0], pageA.translatedAxis[0].subtract(pageA.translatedAxis[1]), axisA[0]);
                        Vector3f midVector = pageA.translatedAxis[0].subtract(pageA.translatedAxis[1]).normalize().add(pageB.translatedAxis[0].subtract(pageB.translatedAxis[1]).normalize()).normalize();                        
                        Vector3f aVector;
                        if(baseA.distance(pageA.translatedAxis[0]) < 0.00001){
                            aVector = pageA.translatedAxis[0].subtract(axisA[1]).normalize();
                        }else{
                            aVector = baseA.subtract(axisA[1]).normalize();
                        }
                        Vector3f c1 = midVector.normalize().mult(baseA.distance(axisA[1]) / FastMath.cos(midVector.angleBetween(aVector))).add(axisA[1]);
                        
                        float angle = axisA[1].subtract(c1).angleBetween(midPoint);
                        float baseLength = axisA[1].distance(c1);
                        float b = -2 * FastMath.cos(angle) * baseLength;
                        float c = FastMath.sqr(baseLength) - FastMath.sqr(jointLength);
                        float midLength = (-b + FastMath.sqrt((b * b) - (4 * c))) / 2;
                        midPoint = c1.add(midPoint.normalize().mult(midLength));
                        pageA.rotateFromTo(axisA[0], midPoint);
                        pageB.rotateFromTo(axisB[0], midPoint);
                    }
                    break;
                }
                case "D2Joint": {
                    Vector3f c1 = pageA.translatedAxis[0];
                    Vector3f c2 = pageB.translatedAxis[0];
                    float r1 = axisA[0].distance(c1);
                    float r2 = axisB[0].distance(c2);
                    float d = c1.distance(c2);
                    if (d > (r1 + r2) + 0.00001 || d < FastMath.abs(r1 - r2) - 0.00001 || d < FastMath.FLT_EPSILON) {
                        if(d > (r1 + r2)){
                            System.out.println("case1");
                        }
                        if(d < FastMath.abs(r1 - r2)-0.00001){
                            System.out.println("case2");
                            System.out.println("d = " + d);
                            System.out.println("r1 = " + r1);
                            System.out.println("r2 = " + r2);
                            System.out.println("r1-r2 = " + (r1-r2));
                        }
                        if(d < FastMath.FLT_EPSILON){
                            System.out.println("case3");
                        }
                        System.out.println(c1);
                        System.out.println(c2);
                        System.out.println("fail");
                        return false;
                    }
                    float a = (r1 * r1 - r2 * r2 + d * d) / (2 * d);
                    float h = FastMath.sqrt(r1 * r1 - a * a);
                    Vector3f v = c2.subtract(c1).normalize();
                    Vector3f w = axisB[0].subtract(axisB[1]).cross(v).normalize();
                    Plane plane = new Plane();
                    plane.setPlanePoints(pageA.translatedBuffer[0], pageA.translatedBuffer[1], pageB.translatedBuffer[0]);
                    Vector3f midPoint = c1.add(v.mult(a)).add(w.mult(h));
                    if (!plane.whichSide(midPoint).equals(upSide)) {
                        midPoint = c1.add(v.mult(a)).subtract(w.mult(h));
                    }
                    pageA.rotateFromTo(axisA[0], midPoint);
                    pageB.rotateFromTo(axisB[0], midPoint);
                    break;
                }
                default:
                    break;
            }
            return true;
        }

        private boolean ready() {
            return pageA.ready && pageB.ready;
        }

    }

    public static Vector3f rotatePoint(Vector3f point, Vector3f axis1, Vector3f axis2, float radian) {
        Plane plane = new Plane();
        Vector3f newPoint = null;
        Vector3f crossVector = axis1.subtract(axis2).cross(point.subtract(axis2)).normalize();
        if (crossVector.distance(Vector3f.ZERO) < FastMath.FLT_EPSILON) {
            return point;
        }
        plane.setPlanePoints(axis2, axis1, axis2.add(crossVector));
        if (FastMath.abs(FastMath.abs(radian) - FastMath.PI) < FastMath.FLT_EPSILON) {
            newPoint = plane.reflect(point, null);
        } else if (FastMath.abs(FastMath.abs(radian) - FastMath.HALF_PI) < FastMath.FLT_EPSILON) {
            Vector3f base = plane.getClosestPoint(point);
            if (radian > 0) {
                newPoint = base.add(crossVector.normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            } else {
                newPoint = base.add(crossVector.normalize().mult(-1 * FastMath.abs(plane.pseudoDistance(point))));
            }
        } else {
            if (FastMath.abs(radian) < FastMath.HALF_PI) {
                Vector3f base = plane.getClosestPoint(point);
                newPoint = base.add(point.subtract(base).normalize()).add(crossVector.normalize().mult(FastMath.tan(radian)));
                newPoint = base.add(newPoint.subtract(base).normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            } else {
                Vector3f base = plane.getClosestPoint(point);
                newPoint = base.add(base.subtract(point).normalize()).add(crossVector.normalize().mult(-FastMath.tan(radian - FastMath.PI)));
                newPoint = base.add(newPoint.subtract(base).normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            }

        }
        return newPoint;
    }

}
