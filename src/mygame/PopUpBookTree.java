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
 * The class the holds the data structure for the program.
 * @author Evans
 */
public class PopUpBookTree {
    //Hashmap mapping the geometry with its patchNode;
    public HashMap<Geometry, PatchNode> geomPatchMap = new HashMap<>();
    
    //The PatchNode and JointNode for the front and back cover
    private PatchNode front;
    private PatchNode back;
    private JointNode bookJoint;
    
    //List of all JointNode in the structure
    private ArrayList<JointNode> joints;
    
    private float height;
    private float width;
    private PopUpBook app;

    /**
     * Constructor for the PopUpBookTree. Creates the front and back cover given width and height
     * @param width
     * @param height
     * @param app reference to the PopUpBook object
     */
    PopUpBookTree(float width, float height, PopUpBook app) {

        this.app = app;
        this.width = width;
        this.height = height;
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
        joints = new ArrayList<>();

        back = addPatch(null, backBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        front = addPatch(null, frontBoundary, new Vector3f[]{Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Z});
        bookJoint = new JointNode(back, front, new Vector3f[]{new Vector3f(0f, 0f, -height / 2f), new Vector3f(0f, 0f, height / 2f)}, "D1Joint");
        back.joint = bookJoint;
        front.joint = bookJoint;
        joints.add(bookJoint);
    }

    /**
     * Checks if a collision exist, if yess, gets all point of collision of patches and marks the patches involved.
     * @return the list of collision points
     */
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
    
    /**
     * returns the geometry of the front cover
     * @return front cover geometry
     */
    public Geometry getFront() {
        return front.geometry;
    }

    /**
     * Delete a patch, and all its children and the patch at is joint to it.
     * @param patch 
     */
    public void delete(PatchNode patch) {
        if (!patch.equals(front) && !patch.equals(back)) {
            if(patch.geometry!= null){
                patch.geometry.removeFromParent();
            }
            geomPatchMap.remove(patch.geometry);
            System.out.println(patch);
            //patch.geometry = null;
            if(patch.joint != null){
                patch.joint.theOther(patch).joint = null;
                joints.remove(patch.joint);
                delete(patch.joint.theOther(patch));
                patch.joint = null;
            }
            
            if(patch.parent!= null && patch.parent.next !=null){
               patch.parent.next.remove(patch); 
            }
            
            while (patch.next!= null && !patch.next.isEmpty()) {
                delete(patch.next.get(0));
            }
            patch = null;
        }

    }

    /**
     * Builds a patch given the parent of the patch, its boundary, and its axis
     * @param prev parent of the patch to add
     * @param boundary boundary of the patch
     * @param axis axis betwen the patch and its parent
     * @return 
     */
    public PatchNode addPatch(Geometry prev, Vector3f[] boundary, Vector3f[] axis) {
        Geometry geometry = new Geometry("Patch", Util.makeMesh(boundary));
        geometry.setMaterial(app.paper);
        app.patches.attachChild(geometry);
        PatchNode patch = new PatchNode(prev, geometry, axis, boundary);
        geomPatchMap.put(geometry, patch);

        return patch;
    }

    /**
     * Adds a joint relation between two patches given the two patch, the jointing axis, and the type of joint
     * @param patchA patchA
     * @param patchB patchB
     * @param axis joint axis
     * @param type Type of joint
     */
    public void addJoint(PatchNode patchA, PatchNode patchB, Vector3f[] axis, String type) {
        JointNode joint = new JointNode(patchA, patchB, axis, type);
        patchA.joint = joint;
        patchB.joint = joint;
        joints.add(joint);
    }

    /**
     * Folds the design given a percentage. 1 is folded all the way and 0 is not folded.
     * Updates the graphics after
     * @param percent percentage of fold
     */
    public void fold(float percent) {
        reset();
        fold(percent, true);
    }

    /**
     * Folds the design given a percentage. 1 is folded all the way and 0 is not folded.
     * Option is given if graphics update is desired
     * @param percent percentage of fold
     * @param update update graphics of not
     */
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
    
    /**
     * Get the axis Between two Geometry
     * @param geomA Geometry A
     * @param geomB Geometry B
     * @return the axis between the two geometries
     */
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

    /**
     * Checks if two geometry is patches that are neighbors
     * @param geomA geometry A
     * @param geomB geometry B
     * @return true if they are neighbors, false otherwise.
     */
    public boolean isNeighbor(Geometry geomA, Geometry geomB) {
        if (geomA == null || geomB == null) {
            return false;
        }
        return geomPatchMap.get(geomB).isNeighbor(geomPatchMap.get(geomA));
    }

    /**
     * Attatch a point on a geometry. And get the position of the point when the design is folded 
     * @param parent geometry to attach point to
     * @param point geometry position to attatch
     * @param percent percentage of fold
     * @return the position of the point when design is folded
     */
    public Vector3f predictWhenFold(Geometry parent, Vector3f point, float percent) {
        reset();
        point = point.clone();
        ArrayList<Vector3f> pointWrapper = new ArrayList();
        pointWrapper.add(point);
        geomPatchMap.get(parent).attatched = pointWrapper;
        fold(percent, false);
        return point;
    }

    /**
     * Get the maximum safty area patches in a joint without patches sticking out when folded
     * @param parentA parent A
     * @param parentB parent B
     * @param axisA1 joint point 1 between patch A and parent A
     * @param axisA2 joint point 2 between patch A and parent A
     * @param axisB1 joint point 1 between patch B and parent B
     * @param axisB2 joint point 2 between patch B and parent B
     * @param jointA1 joint point 1 between patch A and patch B on patch A
     * @param jointA2 joint point 2 between patch A and patch B on patch A
     * @param jointB1 joint point 1 between patch A and patch B on patch B
     * @param jointB2 joint point 2 between patch A and patch B on patch B
     * @param type type of joint
     * @return pair of vertices list representing the maximum area of patch A and patch B
     */
    public ArrayList<ArrayList<Vector3f>> getBoundarys(Geometry parentA, Geometry parentB,
            Vector3f axisA1, Vector3f axisA2, Vector3f axisB1, Vector3f axisB2,
            Vector3f jointA1, Vector3f jointA2, Vector3f jointB1, Vector3f jointB2, String type) {
        reset();
        ArrayList<ArrayList<Vector3f>> returnArray = new ArrayList();
        
        switch (type) {
            case "D1Joint": {
                
                ArrayList<Vector3f> pointsA = new ArrayList<>();
                ArrayList<Vector3f> pointsB = new ArrayList<>();
                pointsA.add(axisA1 .clone());
                pointsA.add(axisA2 .clone());
                pointsA.add(jointA1.clone());
                pointsB.add(axisB1 .clone());
                pointsB.add(axisB2 .clone());
                pointsB.add(jointB1.clone());
                
                PatchNode patchA = new PatchNode(parentA, new Vector3f[]{axisA1.clone(), axisA2.clone()}, Util.toArray(pointsA));
                PatchNode patchB = new PatchNode(parentB, new Vector3f[]{axisB1.clone(), axisB2.clone()}, Util.toArray(pointsB));
                JointNode joint  = new JointNode(patchA, patchB, new Vector3f[]{jointA1, jointA2}, "D1Joint");
                patchA.joint = joint;
                patchB.joint = joint;
                joints.add(joint);
                fold(1f - 0.001f, false);

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
                    temp.collideWith(new Ray(patchA.translatedBoundary[1], patchA.translatedBoundary[0].subtract(patchA.translatedBoundary[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[1], patchB.translatedBoundary[0].subtract(patchB.translatedBoundary[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();
                    boundaryA.add(patchA.translatedBoundary[1]);
                    boundaryB.add(patchB.translatedBoundary[1]);

                    temp.collideWith(new Ray(patchA.translatedBoundary[1], patchA.translatedBoundary[2].subtract(patchA.translatedBoundary[1]).normalize()), results);
                    boundaryA.add(results.getFarthestCollision().getContactPoint());
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[1], patchB.translatedBoundary[2].subtract(patchB.translatedBoundary[1]).normalize()), results);
                    boundaryB.add(results.getFarthestCollision().getContactPoint());
                    results.clear();
                } catch (Exception e) {
                    joints.remove(joints.size() - 1);
                    return null;
                }

                ArrayList<Vector3f> original = pointsA;

                for (ArrayList<Vector3f> boundary : returnArray) {
                    Plane thresholdPlane1 = new Plane();
                    Plane thresholdPlane2 = new Plane();
                    thresholdPlane1.setPlanePoints(boundary.get(0), boundary.get(1), boundary.get(1).add(new Vector3f(0, 1, 0)));
                    thresholdPlane2.setPlanePoints(boundary.get(2), boundary.get(1), boundary.get(2).add(new Vector3f(0, 1, 0)));

                    Plane.Side rightSide1 = thresholdPlane1.whichSide(boundary.get(2));
                    Plane.Side rightSide2 = thresholdPlane2.whichSide(boundary.get(0));

                    ArrayList<Vector3f> legalPoint = new ArrayList<>();
                    for (Vector3f point : back.boundary) {
                        if (!thresholdPlane1.isOnPlane(point) && !thresholdPlane2.isOnPlane(point)
                                && thresholdPlane1.whichSide(point).equals(rightSide1) && thresholdPlane2.whichSide(point).equals(rightSide2)) {
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
                    for (Vector3f point : boundary) {
                        point.set(Util.rotatePoint(point, original.get(1), original.get(1).add(u.cross(v)), angle));
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
                    temp.collideWith(new Ray(patchA.translatedBoundary[1], patchA.translatedBoundary[0].subtract(patchA.translatedBoundary[1])), results);
                    patchA.translatedBoundary[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(1).add(pointsA.get(0).subtract(pointsA.get(1)).normalize().mult(patchA.translatedBoundary[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBoundary[0], patchA.translatedBoundary[1].subtract(patchA.translatedBoundary[0])), results);
                    patchA.translatedBoundary[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(0).add(pointsA.get(1).subtract(boundaryA.get(0)).normalize().mult(patchA.translatedBoundary[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBoundary[3], patchA.translatedBoundary[2].subtract(patchA.translatedBoundary[3])), results);
                    patchA.translatedBoundary[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(pointsA.get(3).add(pointsA.get(2).subtract(pointsA.get(3)).normalize().mult(patchA.translatedBoundary[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchA.translatedBoundary[2], patchA.translatedBoundary[3].subtract(patchA.translatedBoundary[2])), results);
                    patchA.translatedBoundary[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryA.add(boundaryA.get(2).add(pointsA.get(3).subtract(boundaryA.get(2)).normalize().mult(patchA.translatedBoundary[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[1], patchB.translatedBoundary[0].subtract(patchB.translatedBoundary[1])), results);
                    patchB.translatedBoundary[0].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(1).add(pointsB.get(0).subtract(pointsB.get(1)).normalize().mult(patchB.translatedBoundary[1].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[0], patchB.translatedBoundary[1].subtract(patchB.translatedBoundary[0])), results);
                    patchB.translatedBoundary[1].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(0).add(pointsB.get(1).subtract(boundaryB.get(0)).normalize().mult(patchB.translatedBoundary[0].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[3], patchB.translatedBoundary[2].subtract(patchB.translatedBoundary[3])), results);
                    patchB.translatedBoundary[2].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(pointsB.get(3).add(pointsB.get(2).subtract(pointsB.get(3)).normalize().mult(patchB.translatedBoundary[3].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                    temp.collideWith(new Ray(patchB.translatedBoundary[2], patchB.translatedBoundary[3].subtract(patchB.translatedBoundary[2])), results);
                    patchB.translatedBoundary[3].set(results.getFarthestCollision().getContactPoint());
                    boundaryB.add(boundaryB.get(2).add(pointsB.get(3).subtract(boundaryB.get(2)).normalize().mult(patchB.translatedBoundary[2].distance(results.getFarthestCollision().getContactPoint()))));
                    results.clear();

                } catch (Exception e) {
                    joints.remove(joint);
                    return null;
                }

                geomPatchMap.get(parentA).next.remove(patchA);
                geomPatchMap.get(parentB).next.remove(patchB);
                joints.remove(joint);
                break;
            }
            default:
                break;
        }

        return returnArray;
    }

    /**
     * reset the patches and joints to default position, meaning unfold position
     */
    public void reset() {
        for (PatchNode patch : geomPatchMap.values()) {
            patch.reset();
        }
        for (JointNode joint : joints) {
            joint.reset();
        }
    }
    /**
     * updates the graphics of the program
     */
    void update() {
        app.lines.detachAllChildren();
        Material lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.Black);

        for (PatchNode patch : geomPatchMap.values()) {
            patch.geometry.getMesh().getBuffer(VertexBuffer.Type.Position).updateData(BufferUtils.createFloatBuffer(patch.translatedBuffer));
            patch.geometry.getMesh().createCollisionData();
            patch.geometry.updateModelBound();
            for (int i = 0; i < patch.translatedBoundary.length; i++) {
                Geometry line = new Geometry("Line", new Cylinder());
                line.setMaterial(lineMaterial);
                line.setLocalTranslation(patch.translatedBoundary[i].add(patch.translatedBoundary[(i + 1) % patch.translatedBoundary.length]).divide(2));
                ((Cylinder) line.getMesh()).updateGeometry(3, 3, 0.01f, 0.01f, patch.translatedBoundary[i].distance(patch.translatedBoundary[(i + 1) % patch.translatedBoundary.length]), false, false);
                line.lookAt(patch.translatedBoundary[i], new Vector3f(0, 1, 0));
                app.lines.attachChild(line);
            }
        }
    }
    
    /**
     * PatchNode class the the PopupbookTree class uses to represent patches
     */
    public class PatchNode {
        public Geometry geometry;
        public ArrayList<PatchNode> next;
        public PatchNode parent;
        public JointNode joint;
        public Vector3f[] axis;
        public Vector3f[] boundary;
        private Vector3f[] translatedBoundary;
        private VertexBuffer originalBuffer;
        private Vector3f[] translatedBuffer;
        private Vector3f[] translatedAxis;
        private boolean ready;
        
        private ArrayList<Vector3f> attatched;

        /**
         * Constructor of the PatchNode Class
         * @param prev parent to the patch
         * @param geometry the geometry representing the patch
         * @param axis the axis between the patch and its parent
         * @param boundary the boundary around the geometry 
         */
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
        /**
         * constructor for the patchNodes for phantom patches. These patches does not render and are only used for computations.
         * @param prev parent of the patch
         * @param axis axis between the patch and its parent
         * @param boundary boundary that describe the patch
         */
        private PatchNode(Geometry prev, Vector3f[] axis, Vector3f[] boundary) {
            this.axis = axis;
            translatedAxis = new Vector3f[2];
            translatedAxis[0] = axis[0].clone();
            translatedAxis[1] = axis[1].clone();
            geomPatchMap.get(prev).next.add(this);
            joint = null;
            this.boundary = boundary;
            translatedBoundary = new Vector3f[boundary.length];
            for (int i = 0; i < boundary.length; i++) {
                translatedBoundary[i] = boundary[i].clone();
            }
        }
        
        /**
         * Checks if a patch is neighbor to this patch
         * @param patch the patch to check
         * @return true if is neighbor
         */
        private boolean isNeighbor(PatchNode patch) {
            if (this == patch) {
                return false;
            }
            if (next.contains(patch) || patch.next.contains(this)) {
                return true;
            }
            return joint.patchA.equals(patch) || joint.patchB.equals(patch);
        }
        
        /**
         * Gets the distance to a point from this patch
         * @param point the point
         * @return distance to point
         */
        public float distanceFromPoint(Vector3f point) {
            Plane plane = new Plane();
            plane.setOriginNormal(boundary[0], getNormal());
            return FastMath.abs(plane.pseudoDistance(point));
        }

        /**
         * Gets the normal of this patch
         * @return patch normal
         */
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
        
        /**
         * Given point "from" is a point on this patch, rotate this patch from the point "from" to the point "to"
         * @param from point "from"
         * @param to point "to"
         */
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
        
        /**
         * Rotate the patch, and all children with left hand rule and a specific radian. 
         * If ready is true, mark the children of this patch as ready
         * @param radian
         * @param nextReady 
         */
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
                for (Vector3f point : translatedBuffer) {
                point.set(Util.rotatePoint(point, axis[0], axis[1], radian));
            }
                
            }
            for (Vector3f point : translatedBoundary) {
                    point.set(Util.rotatePoint(point, axis[0], axis[1], radian));
                }
            
            
            joint.rotate(this, axis, radian);
            
            translatedAxis[0].set(Util.rotatePoint(translatedAxis[0], axis[0], axis[1], radian));
            translatedAxis[1].set(Util.rotatePoint(translatedAxis[1], axis[0], axis[1], radian));
        }

        /**
         * rotate patch, and all children, with the left hand rule, a specific radian and a specific axis
         * @param axis axis to rotate
         * @param radian radian to rotate
         * @param nextReady set children as ready or not
         */
        private void rotate(Vector3f[] axis, float radian, boolean nextReady) {
            this.ready = true;
            if (next != null) {
                for (PatchNode patch : next) {
                    patch.ready = true;
                }
            }

            rotate(axis, radian);
        }
        
        /**
         * reset patch to starting position
         */
        private void reset() {
            translatedAxis[0].set(axis[0].clone());
            translatedAxis[1].set(axis[1].clone());
            attatched = null;
            ready = false;
            if(geometry!= null){
                translatedBuffer = BufferUtils.getVector3Array((FloatBuffer) originalBuffer.clone().getData());
                translatedBuffer = BufferUtils.getVector3Array((FloatBuffer) originalBuffer.clone().getData());
            }
            
            for (int i = 0; i < boundary.length; i++) {
                translatedBoundary[i].set(boundary[i]);
            }
            
        }
    }
    /**
     * Class to represent joints in PopUpBookTree.
     */
    public class JointNode {
        public String type;
        public Vector3f[] jointAxis;
        private PatchNode patchA;
        private PatchNode patchB;
        private Vector3f[] axisA;
        private Vector3f[] axisB;
        private HashMap<PatchNode, Vector3f[]> translatedJointAxis;
        private Plane.Side upSide;
        private Vector3f previousIntersection;

        /**
         * Constructor for the jointNode
         * @param patchA patch A
         * @param patchB patch B
         * @param jointAxis joint Axis between patch A & B
         * @param type the type of joint
         */
        private JointNode(PatchNode patchA, PatchNode patchB, Vector3f[] jointAxis, String type) {
            this.type = type;
            this.jointAxis = jointAxis;
            translatedJointAxis = new HashMap<>();
            
            
            //store the reference to the patches
            this.patchA = patchA;
            this.patchB = patchB;
            
            //the joint axis local to patch A and B
            axisA = new Vector3f[2];
            axisB = new Vector3f[2];
            axisA[0] = jointAxis[0].clone();
            axisA[1] = jointAxis[1].clone();
            axisB[0] = jointAxis[0].clone();
            axisB[1] = jointAxis[1].clone();
            translatedJointAxis.put(patchA, axisA);
            translatedJointAxis.put(patchB, axisB);
            
            
            
            //store a upSide and previousIntersection to use for reference for picking one from the two intersection point in fix Joint
            Plane plane = new Plane();
            plane.setPlanePoints(patchA.translatedBoundary[0], patchA.translatedBoundary[1], patchB.translatedBoundary[0]);
            upSide = plane.whichSide(axisA[0]);
            previousIntersection = new Vector3f(axisA[0]);
            
            
        }
        
        /**
         * Provide a reference to the other patch in the joint, given a patch.
         * @param thisPatch patch 1
         * @return patch 2
         */
        public PatchNode theOther(PatchNode thisPatch) {
            if (patchA.equals(thisPatch)) {
                return patchB;
            } else {
                return patchA;
            }
        }
        
        /**
         * marks the interesection point for reference use
         */
        private void markLastIntersection(){
            previousIntersection.set(axisA[0]);
        }

        /**
         * rotate the intersection points of a patch in this joint, given the patch, rotation axis, and radian
         * @param patch patch
         * @param axis rotation axis
         * @param radian radian
         */
        private void rotate(PatchNode patch, Vector3f[] axis, float radian) {
            if (patch.equals(patchA)) {
                axisA[0].set(Util.rotatePoint(axisA[0], axis[0], axis[1], radian));
                axisA[1].set(Util.rotatePoint(axisA[1], axis[0], axis[1], radian));
            } else {
                axisB[0].set(Util.rotatePoint(axisB[0], axis[0], axis[1], radian));
                axisB[1].set(Util.rotatePoint(axisB[1], axis[0], axis[1], radian));
            }
        }
        /**
         * resets the intersection points to position when fold = 0
         */
        private void reset() {
            axisA[0].set(jointAxis[0].clone());
            axisA[1].set(jointAxis[1].clone());
            axisB[0].set(jointAxis[0].clone());
            axisB[1].set(jointAxis[1].clone());
        }

        /**
         * Fix a seperated joint by rotation both patches to aligning the intersection points.
         */
        private void fixJoint() {
            switch (type) {
                case "D1Joint": {
                        Plane planeMid = new Plane();
                        Plane planeA = new Plane();
                        float jointLength = jointAxis[0].distance(jointAxis[1]);

                        planeA.setOriginNormal(axisA[1], patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]));
                        Vector3f midPoint = new Vector3f();

                        midPoint = patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]).normalize().cross(
                                patchB.translatedAxis[0].subtract(patchB.translatedAxis[1]).normalize())
                                .normalize().negate();
                        
                        if (patchA.translatedAxis[0].subtract(patchA.translatedAxis[1]).cross(patchB.translatedAxis[0].subtract(patchB.translatedAxis[1])).distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                            planeMid.setPlanePoints(patchA.translatedBoundary[0], patchA.translatedBoundary[1], patchB.translatedBoundary[0]);
                            if (!planeMid.whichSide(midPoint.add(patchA.translatedAxis[1])).equals(upSide)) {
                                midPoint.negateLocal();
                            }
                        }else{
                            if(midPoint.add(patchA.translatedAxis[1]).distance(previousIntersection) > midPoint.negate().add(patchA.translatedAxis[1]).distance(previousIntersection)){
                                midPoint.negateLocal();
                            }
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
                        patchA.rotateFromTo(axisA[0], midPoint);
                        patchB.rotateFromTo(axisB[0], midPoint);
                    
                    markLastIntersection();
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
                    }
                    float a = (r1 * r1 - r2 * r2 + d * d) / (2 * d);
                    float h = FastMath.sqrt(r1 * r1 - a * a);
                    Vector3f v = c2.subtract(c1).normalize();
                    Vector3f w = axisB[0].subtract(axisB[1]).cross(v).normalize();
                    Plane plane = new Plane();
                    plane.setPlanePoints(patchA.translatedBoundary[0], patchA.translatedBoundary[1], patchB.translatedBoundary[0]);
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
        }
        /**
         * checks if a joint is ready, meaning both its patch is finalized
         * @return true if ready, false otherwise.
         */
        private boolean ready() {
            return patchA.ready && patchB.ready;
        }

    }
}
