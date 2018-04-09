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
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import static mygame.Util.getBountdaryNormal;
import static mygame.Util.inBoundary;
import static mygame.Util.linePlaneIntersection;

/**
 *
 * @author Evans
 */
public class PopUpBookTree {

    private Node rootNode;
    public PatchNode front;
    public PatchNode back;
    private JointNode bookJoint;
    private ArrayList<PatchNode> patchs;
    public HashMap<Geometry, PatchNode> geomPatchMap = new HashMap<>();
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
        patchs = new ArrayList<>();
        joints = new ArrayList<>();

        back = addPatch(null, backBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        front = addPatch(null, frontBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        bookJoint = new JointNode(back, front, new Vector3f[]{new Vector3f(0f, 0f, -height / 2f), new Vector3f(0f, 0f, height / 2f)}, "D1Joint");
        back.joint = bookJoint;
        front.joint = bookJoint;
        joints.add(bookJoint);
        mark1 = new Geometry("name", new Sphere(5, 5, 0.05f));
        Material dotMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dotMaterial.setColor("Color", ColorRGBA.Red);

    }

    public ArrayList<Vector3f> getCollisions() {
        ArrayList<Vector3f> collisionList = new ArrayList<>();
        PatchNode[] patchList = geomPatchMap.values().toArray(new PatchNode[geomPatchMap.size()]);
        for (int i = 0; i < patchList.length; i++) {
            for (int x = i + 1; x < patchList.length; x++) {
                PatchNode patchA = patchList[i];
                PatchNode patchB = patchList[x];
                
                if (!patchA.isNeighbor(patchB) &&
                    !patchA.joint.theOther(patchA).isNeighbor(patchB) && 
                    !patchB.joint.theOther(patchB).isNeighbor(patchA)) {
                    //System.out.println("Checking "+i +" "+x);
                    ArrayList<Vector3f> collision = Util.boundboundIntersect(patchA.translatedBoundary, patchB.translatedBoundary);
                    if (collision != null) {
                        patchA.geometry.setMaterial(app.markPaper);
                        patchB.geometry.setMaterial(app.markPaper);
                        for(Vector3f point:collision){
                            collisionList.add(point);
                        }
                    }
                }
            }
        }
        if (!collisionList.isEmpty()) {
            return collisionList;
        } else {
            return null;
        }
    }
    

    public Geometry getFront() {
        return front.geometry;
    }

    public void delete(PatchNode patch) {
        if (!patch.equals(front) && !patch.equals(back)) {
            System.out.println(patchs.indexOf(patch));
            patchs.remove(patch);
            patch.geometry.removeFromParent();
            geomPatchMap.remove(patch.geometry);
            System.out.println(patch);
            //patch.geometry = null;
            if(patch.joint != null){
                patch.joint.theOther(patch).joint = null;
                joints.remove(patch.joint);
                delete(patch.joint.theOther(patch));
                patch.joint = null;
            }
            
            
            patch.parent.next.remove(patch);
            while (!patch.next.isEmpty()) {
                System.out.println("Next" + patchs.indexOf(patch.next.get(0)));
                delete(patch.next.get(0));
            }
            //patch = null;
        }

    }

    public PatchNode addPatch(Geometry prev, Vector3f[] boundary, Vector3f[] axis) {

        System.out.println("actually");
        Geometry geometry = new Geometry("Patch", Util.makeMesh(boundary));
        geometry.setMaterial(app.paper);
        planes.attachChild(geometry);
        PatchNode patch = new PatchNode(prev, geometry, axis, boundary);
        patchs.add(patch);
        geomPatchMap.put(geometry, patch);

        return patch;
    }

    public void addJoint(PatchNode patchA, PatchNode patchB, Vector3f[] axis, String type) {
        JointNode joint = new JointNode(patchA, patchB, axis, type);
        patchA.joint = joint;
        patchB.joint = joint;
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
        PatchNode patchA = geomPatchMap.get(geomA);
        PatchNode patchB = geomPatchMap.get(geomB);

        if ((patchA.equals(front) && patchB.equals(back)) || (patchA.equals(back) && patchB.equals(front))) {
            return new Vector3f[]{new Vector3f(0f, 0f, -height / 2f), new Vector3f(0f, 0f, height / 2f)};
        }
        if (patchA.next.contains(patchB)) {
            return patchB.axis;
        }
        if (patchB.next.contains(patchA)) {
            return patchA.axis;
        }
        for (JointNode joint : joints) {
            if (joint.patchA.equals(patchA) && joint.patchB.equals(patchB) || joint.patchB.equals(patchA) && joint.patchA.equals(patchB)) {
                return joint.jointAxis;
            }
        }
        return null;
    }

    public boolean isNeighbor(Geometry geomA, Geometry geomB) {
        if (geomA == null || geomB == null) {
            return false;
        }
        return geomPatchMap.get(geomB).isNeighbor(geomPatchMap.get(geomA));
    }

    public Vector3f predictWhenFold(Geometry parent, Vector3f point, float percent) {
        reset();
        point = point.clone();
        ArrayList<Vector3f> pointWrapper = new ArrayList();
        pointWrapper.add(point);
        geomPatchMap.get(parent).attatched = pointWrapper;
        fold(percent, false);
        return point;
    }

    public ArrayList<ArrayList<Vector3f>> getBoundarys(Geometry parentA, Geometry parentB,
            Vector3f axisA1, Vector3f axisA2, Vector3f axisB1, Vector3f axisB2,
            Vector3f jointA1, Vector3f jointA2, Vector3f jointB1, Vector3f jointB2, String type) {
        reset();
        ArrayList<ArrayList<Vector3f>> returnArray = new ArrayList();

        switch (type) {
            case "D1Joint": {
                ArrayList<Vector3f> pointsA = new ArrayList<>();
                ArrayList<Vector3f> pointsB = new ArrayList<>();
                pointsA.add(axisA1.clone());
                pointsA.add(axisA2.clone());
                pointsA.add(jointA1.clone());
                pointsB.add(axisB1.clone());
                pointsB.add(axisB2.clone());
                pointsB.add(jointB1.clone());

                PatchNode patchA = new PatchNode(parentA, new Vector3f[]{axisA1.clone(), axisA2.clone()}, Util.toArray(pointsA));
                PatchNode patchB = new PatchNode(parentB, new Vector3f[]{axisB1.clone(), axisB2.clone()}, Util.toArray(pointsB));
                JointNode joint = new JointNode(patchA, patchB, new Vector3f[]{jointA1, jointA2}, "D1Joint");
                patchA.joint = joint;
                patchB.joint = joint;
                joints.add(joint);
                fold(1f - 0.0008f, false);

                ArrayList<Vector3f> boundaryA = new ArrayList();
                ArrayList<Vector3f> boundaryB = new ArrayList();
                returnArray.add(boundaryA);
                returnArray.add(boundaryB);

                Vector3f center = new Vector3f();
                center.set(width / 2, 0, 0);
                Geometry patchBox = new Geometry("Box", new Box(center, width / 2, 0.5f, height / 2));
                Node temp = new Node("temp");
                temp.attachChild(patchBox);
                CollisionResults results = new CollisionResults();
                try {
                    temp.collideWith(new Ray(patchA.translatedBuffer[1], patchA.translatedBuffer[0].subtract(patchA.translatedBuffer[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[1], patchB.translatedBuffer[0].subtract(patchB.translatedBuffer[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    boundaryA.add(patchA.translatedBuffer[1]);
                    boundaryB.add(patchB.translatedBuffer[1]);

                    temp.collideWith(new Ray(patchA.translatedBuffer[1], patchA.translatedBuffer[2].subtract(patchA.translatedBuffer[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[1], patchB.translatedBuffer[2].subtract(patchB.translatedBuffer[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();
                } catch (Exception e) {
                    patchs.remove(patchA);
                    patchs.remove(patchB);
                    joints.remove(joints.size() - 1);
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
                            point.set(Util.rotatePoint(point, original.get(1), original.get(1).add(u.cross(v)), angle));
                        }
                    }
                    angle = (u.cross(boundary.get(2).subtract(original.get(1))).normalize()).angleBetween(
                            u.cross(original.get(2).subtract(original.get(1))).normalize());
                    if (original.get(2).distance(Util.rotatePoint(boundary.get(2), original.get(0), original.get(1), angle))
                            > original.get(2).distance(Util.rotatePoint(boundary.get(2), original.get(1), original.get(0), angle))) {
                        angle = angle * -1;
                    }
                    if (FastMath.abs(angle) > FastMath.FLT_EPSILON) {
                        for (Vector3f point : boundary) {
                            point.set(Util.rotatePoint(point, boundary.get(0), boundary.get(1), angle));
                        }
                    }
                    original = pointsB;
                }
                patchs.remove(patchA);
                patchs.remove(patchB);
                geomPatchMap.get(parentA).next.remove(patchA);
                geomPatchMap.get(parentB).next.remove(patchB);

                joints.remove(joint);
                break;
            }
            case "D2Joint": {
                ArrayList<Vector3f> pointsA = new ArrayList<>();
                pointsA.add(axisA1);
                pointsA.add(axisA2);
                pointsA.add(jointA1);
                pointsA.add(jointA2);
                ArrayList<Vector3f> pointsB = new ArrayList<>();
                pointsB.add(axisB1);
                pointsB.add(axisB2);
                pointsB.add(jointB1);
                pointsB.add(jointB2);
                float length = pointsA.get(0).distance(pointsA.get(3)) * FastMath.cos(pointsA.get(1).subtract(pointsA.get(0)).normalize().angleBetween(pointsA.get(3).subtract(pointsA.get(0)).normalize()));
                Vector3f jointPoint = pointsA.get(3).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(length));
                length = pointsA.get(0).distance(pointsB.get(0)) * FastMath.cos(pointsA.get(1).subtract(pointsA.get(0)).normalize().angleBetween(pointsB.get(0).subtract(pointsA.get(0)).normalize()));
                Vector3f jointPointB = pointsB.get(0).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(length));

                PatchNode patchA = new PatchNode(parentA, new Vector3f[]{pointsA.get(0), pointsA.get(1)}, Util.toArray(pointsA));
                PatchNode patchB = new PatchNode(parentB, new Vector3f[]{jointPointB, pointsB.get(1)}, Util.toArray(pointsB));
                JointNode joint = new JointNode(patchA, patchB, new Vector3f[]{jointPoint, pointsA.get(2).clone()}, "D2Joint");
                patchA.joint = joint;
                patchB.joint = joint;
                joints.add(joint);
                fold(0.999f, false);

                ArrayList<Vector3f> boundaryA = new ArrayList();
                ArrayList<Vector3f> boundaryB = new ArrayList();
                returnArray.add(boundaryA);
                returnArray.add(boundaryB);
                Vector3f center = new Vector3f();
                center.set(width / 2, 0, 0);
                Geometry patchBox = new Geometry("Box", new Box(center, width / 2, 0.5f, height / 2));
                Node temp = new Node("temp");
                temp.attachChild(patchBox);
                CollisionResults results = new CollisionResults();
                try {
                    temp.collideWith(new Ray(patchA.translatedBuffer[1], patchA.translatedBuffer[0].subtract(patchA.translatedBuffer[1])), results);
                    patchA.translatedBuffer[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(1).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(patchA.translatedBuffer[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBuffer[0], patchA.translatedBuffer[1].subtract(patchA.translatedBuffer[0])), results);
                    patchA.translatedBuffer[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(0).add(pointsA.get(1).subtract(boundaryA.get(0)).normalize().mult(patchA.translatedBuffer[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBuffer[3], patchA.translatedBuffer[2].subtract(patchA.translatedBuffer[3])), results);
                    patchA.translatedBuffer[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(3).add(pointsA.get(2).subtract(pointsA.get(3)).normalize().mult(patchA.translatedBuffer[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBuffer[2], patchA.translatedBuffer[3].subtract(patchA.translatedBuffer[2])), results);
                    patchA.translatedBuffer[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(2).add(pointsA.get(3).subtract(boundaryA.get(2)).normalize().mult(patchA.translatedBuffer[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[1], patchB.translatedBuffer[0].subtract(patchB.translatedBuffer[1])), results);
                    patchB.translatedBuffer[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(1).add(pointsB.get(0).subtract(pointsB.get(1)).normalize().mult(patchB.translatedBuffer[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[0], patchB.translatedBuffer[1].subtract(patchB.translatedBuffer[0])), results);
                    patchB.translatedBuffer[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(0).add(pointsB.get(1).subtract(boundaryB.get(0)).normalize().mult(patchB.translatedBuffer[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[3], patchB.translatedBuffer[2].subtract(patchB.translatedBuffer[3])), results);
                    patchB.translatedBuffer[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(3).add(pointsB.get(2).subtract(pointsB.get(3)).normalize().mult(patchB.translatedBuffer[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBuffer[2], patchB.translatedBuffer[3].subtract(patchB.translatedBuffer[2])), results);
                    patchB.translatedBuffer[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(2).add(pointsB.get(3).subtract(boundaryB.get(2)).normalize().mult(patchB.translatedBuffer[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                } catch (Exception e) {
                    patchs.remove(patchA);
                    patchs.remove(patchB);
                    joints.remove(joint);
                    return null;
                }

                geomPatchMap.get(parentA).next.remove(patchA);
                geomPatchMap.get(parentB).next.remove(patchB);
                patchs.remove(patchA);
                patchs.remove(patchB);
                joints.remove(joint);
                break;
            }
            default:
                break;
        }

        return returnArray;
    }

    public void reset() {
        for (PatchNode patch : patchs) {
            patch.reset();
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

        for (PatchNode patch : patchs) {
            patch.geometry.getMesh().getBuffer(VertexBuffer.Type.Position).updateData(BufferUtils.createFloatBuffer(patch.translatedBuffer));
            patch.geometry.getMesh().createCollisionData();
            for (int i = 0; i < patch.translatedBoundary.length; i++) {
                Geometry line = new Geometry("Line", new Cylinder());
                line.setMaterial(lineMaterial);
                line.setLocalTranslation(patch.translatedBoundary[i].add(patch.translatedBoundary[(i + 1) % patch.translatedBoundary.length]).divide(2));
                ((Cylinder) line.getMesh()).updateGeometry(3, 3, 0.01f, 0.01f, patch.translatedBoundary[i].distance(patch.translatedBoundary[(i + 1) % patch.translatedBoundary.length]), false, false);
                line.lookAt(patch.translatedBoundary[i], new Vector3f(0, 1, 0));
                lines.attachChild(line);
            }
        }
    }

    public class PatchNode {

        public Geometry geometry;
        private VertexBuffer originalBuffer;
        private Vector3f[] translatedBuffer;
        public Vector3f[] axis;
        private Vector3f[] translatedAxis;
        public Vector3f[] boundary;
        public Vector3f[] translatedBoundary;
        private boolean ready;
        public ArrayList<PatchNode> next;
        public PatchNode parent;
        public JointNode joint;
        private ArrayList<Vector3f> attatched;

        private PatchNode(Geometry prev, Geometry geometry, Vector3f[] axis, Vector3f[] boundary) {
            this.next = new ArrayList<>();
            this.joint = null;
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
                geomPatchMap.get(prev).next.add(this);
                this.parent = geomPatchMap.get(prev);
            }
        }

        private PatchNode(Geometry prev, Vector3f[] axis, Vector3f[] buffer) {
            this.axis = axis;
            translatedAxis = new Vector3f[2];
            translatedAxis[0] = axis[0].clone();
            translatedAxis[1] = axis[1].clone();
            geomPatchMap.get(prev).next.add(this);
            joint = null;
            translatedBuffer = buffer;
        }

        private boolean isNeighbor(PatchNode patch) {
            if (this == patch) {
                return false;
            }
            if (next.contains(patch) || patch.next.contains(this)) {
                return true;
            }
            return joint.patchA.equals(patch) || joint.patchB.equals(patch);
        }

        public float distanceFromPoint(Vector3f point) {
            Plane plane = new Plane();
            plane.setOriginNormal(boundary[0], getNormal());
            return FastMath.abs(plane.pseudoDistance(point));
        }

        public Vector3f getNormal() {
            Vector3f vector1 = boundary[0].subtract(boundary[1]);
            Vector3f vector2;
            for (int i = 2; i < boundary.length; i++) {
                vector2 = boundary[i].subtract(boundary[1]);
                if (vector2.distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                    return vector1.cross(vector2).normalize();
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
            if (Util.rotatePoint(from, translatedAxis[0], translatedAxis[1], angle).distance(to) > Util.rotatePoint(from, translatedAxis[0], translatedAxis[1], -angle).distance(to)) {
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
                        point.set(Util.rotatePoint(point, axis[0], axis[1], radian));
                    }
                }
                for (PatchNode patch : next) {
                    patch.rotate(axis, radian);
                }
                for (Vector3f point : translatedBoundary) {
                    point.set(Util.rotatePoint(point, axis[0], axis[1], radian));
                }
            }
            for (Vector3f point : translatedBuffer) {
                point.set(Util.rotatePoint(point, axis[0], axis[1], radian));
            }
            
            joint.rotate(this, axis, radian);
            
            translatedAxis[0].set(Util.rotatePoint(translatedAxis[0], axis[0], axis[1], radian));
            translatedAxis[1].set(Util.rotatePoint(translatedAxis[1], axis[0], axis[1], radian));
        }

        private void rotate(Vector3f[] axis, float radian, boolean nextReady) {
            this.ready = true;
            if (next != null) {
                for (PatchNode patch : next) {
                    patch.ready = true;
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
        private PatchNode patchA;
        private PatchNode patchB;
        public Vector3f[] jointAxis;
        private Vector3f[] axisA;
        private Vector3f[] axisB;
        private HashMap<PatchNode, Vector3f[]> translatedJointAxis;
        private Plane.Side upSide;

        private JointNode(PatchNode patchA, PatchNode patchB, Vector3f[] jointAxis, String type) {
            this.type = type;
            this.jointAxis = jointAxis;
            translatedJointAxis = new HashMap<>();
            axisA = new Vector3f[2];
            axisB = new Vector3f[2];
            axisA[0] = jointAxis[0].clone();
            axisA[1] = jointAxis[1].clone();
            axisB[0] = jointAxis[0].clone();
            axisB[1] = jointAxis[1].clone();
            this.patchA = patchA;
            this.patchB = patchB;
            Plane plane = new Plane();
            plane.setPlanePoints(patchA.translatedBuffer[0], patchA.translatedBuffer[1], patchB.translatedBuffer[0]);
            upSide = plane.whichSide(axisA[0]);
            translatedJointAxis.put(patchA, axisA);
            translatedJointAxis.put(patchB, axisB);
        }

        public PatchNode theOther(PatchNode thisPatch) {
            if (patchA.equals(thisPatch)) {
                return patchB;
            } else {
                return patchA;
            }
        }

        private void rotate(PatchNode patch, Vector3f[] axis, float radian) {
            if (patch.equals(patchA)) {
                axisA[0].set(Util.rotatePoint(axisA[0], axis[0], axis[1], radian));
                axisA[1].set(Util.rotatePoint(axisA[1], axis[0], axis[1], radian));
            } else {
                axisB[0].set(Util.rotatePoint(axisB[0], axis[0], axis[1], radian));
                axisB[1].set(Util.rotatePoint(axisB[1], axis[0], axis[1], radian));
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

                        planeA.setOriginNormal(axisA[1], patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]));
                        Vector3f midPoint = new Vector3f();

                        midPoint = patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]).normalize().cross(
                                patchB.translatedAxis[0].subtract(patchB.translatedAxis[1]).normalize())
                                .normalize().negate();
                        planeMid.setPlanePoints(patchA.translatedBuffer[0], patchA.translatedBuffer[1], patchB.translatedBuffer[0]);
                        if (!planeMid.whichSide(midPoint.add(patchA.translatedAxis[1])).equals(upSide)) {
                            midPoint.negateLocal();
                        }

                        Vector3f baseA = Util.closestPointOnLine(patchA.translatedAxis[0], patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]), axisA[0]);
                        Vector3f midVector = patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]).normalize().add(patchB.translatedAxis[0].subtract(patchB.translatedAxis[1]).normalize()).normalize();
                        Vector3f aVector;
                        if (baseA.distance(patchA.translatedAxis[0]) < 0.00001) {
                            aVector = patchA.translatedAxis[0].subtract(axisA[1]).normalize();
                        } else {
                            aVector = baseA.subtract(axisA[1]).normalize();
                        }
                        Vector3f c1 = midVector.normalize().mult(baseA.distance(axisA[1]) / FastMath.cos(midVector.angleBetween(aVector))).add(axisA[1]);

                        float angle = axisA[1].subtract(c1).angleBetween(midPoint);
                        float baseLength = axisA[1].distance(c1);
                        float b = -2 * FastMath.cos(angle) * baseLength;
                        float c = FastMath.sqr(baseLength) - FastMath.sqr(jointLength);
                        float midLength = (-b + FastMath.sqrt((b * b) - (4 * c))) / 2;
                        midPoint = c1.add(midPoint.normalize().mult(midLength));
                        mark1.setLocalTranslation(midPoint);
                        patchA.rotateFromTo(axisA[0], midPoint);
                        patchB.rotateFromTo(axisB[0], midPoint);
                    }
                    break;
                }
                case "D2Joint": {
                    Vector3f c1 = patchA.translatedAxis[0];
                    Vector3f c2 = patchB.translatedAxis[0];
                    float r1 = axisA[0].distance(c1);
                    float r2 = axisB[0].distance(c2);
                    float d = c1.distance(c2);
                    if (d > (r1 + r2) + 0.00001 || d < FastMath.abs(r1 - r2) - 0.00001 || d < FastMath.FLT_EPSILON) {
                        if (d > (r1 + r2)) {
                            System.out.println("case1");
                        }
                        if (d < FastMath.abs(r1 - r2) - 0.00001) {
                            System.out.println("case2");
                            System.out.println("d = " + d);
                            System.out.println("r1 = " + r1);
                            System.out.println("r2 = " + r2);
                            System.out.println("r1-r2 = " + (r1 - r2));
                        }
                        if (d < FastMath.FLT_EPSILON) {
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
                    plane.setPlanePoints(patchA.translatedBuffer[0], patchA.translatedBuffer[1], patchB.translatedBuffer[0]);
                    Vector3f midPoint = c1.add(v.mult(a)).add(w.mult(h));
                    if (!plane.whichSide(midPoint).equals(upSide)) {
                        midPoint = c1.add(v.mult(a)).subtract(w.mult(h));
                    }
                    patchA.rotateFromTo(axisA[0], midPoint);
                    patchB.rotateFromTo(axisB[0], midPoint);
                    break;
                }
                default:
                    break;
            }
            return true;
        }

        private boolean ready() {
            return patchA.ready && patchB.ready;
        }

    }
}
