/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Evans
 */
public final class Util {
    public static final float FLT_EPSILON = 0.000001f;
    public static Vector3f[] toArray(ArrayList<Vector3f> arrayList){
        Vector3f[] array = new Vector3f[arrayList.size()];
        for(int i = 0; i < arrayList.size();i++){
            array[i] = arrayList.get(i).clone();
        }
        return array;
    }
    public static Mesh makeMesh(Vector3f[] vertices) {
        Mesh mesh = new Mesh();
        mesh.setDynamic();
        Plane plane = new Plane();
        plane.setPlanePoints(vertices[0], vertices[1], vertices[2]);
        int count = 0;
        ArrayList<Integer> notDone = new ArrayList<>();
        ArrayList<Integer> triangle = new ArrayList<>();
        int size = vertices.length;

        for (int i = 0; i < size; i++) {
            Vector3f u = vertices[(i - 1 + size) % size].subtract(vertices[i]);
            Vector3f v = vertices[(i + 1) % size].subtract(vertices[i]);
            if (plane.whichSide(vertices[0].add(u.cross(v))).equals(Plane.Side.Positive)) {
                count++;
            }
            notDone.add(i);
        }
        Plane.Side correct;
        if (count < size - count) {
            correct = Plane.Side.Negative;
        } else {
            correct = Plane.Side.Positive;
        }
        
        if (count != 0 || count != size) {
            boolean clean = false;
            while (!clean) {
                clean = true;
                size = notDone.size();
                boolean lastConcave = false;
                int found = -1;
                for (int i : notDone) {
                    Vector3f u = vertices[notDone.get((notDone.indexOf(i) - 1 + size) % size)].subtract(vertices[i]);
                    Vector3f v = vertices[notDone.get((notDone.indexOf(i) + 1) % size)].subtract(vertices[i]);
                    if (!plane.whichSide(vertices[0].add(u.cross(v))).equals(correct)) {
                        lastConcave = true;
                    } else {
                        if (lastConcave) {
                            found = i;
                            break;
                        }
                        lastConcave = false;
                    }
                }
                if (found != -1) {
                    clean = false;
                    triangle.add(notDone.get((notDone.indexOf(found) - 1 + size) % size));
                    triangle.add(found);
                    triangle.add(notDone.get((notDone.indexOf(found) + 1) % size));
                    notDone.remove(notDone.indexOf(found));
                }
            }
        }

        while (notDone.size() >= 3) {
            int found = notDone.get(0);
            size = notDone.size();
            triangle.add(notDone.get((notDone.indexOf(found) - 1 + size) % size));
            triangle.add(found);
            triangle.add(notDone.get((notDone.indexOf(found) + 1) % size));
            notDone.remove(notDone.indexOf(found));

        }

        int[] triangles = new int[triangle.size()];

        for (int i = 0; i < triangle.size(); i++) {
            triangles[i] = triangle.get(i);
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(triangles));
        mesh.updateBound();
        return mesh;

    }
    public static boolean isBetween(Vector3f left, Vector3f mid, Vector3f right){
        return mid.subtract(left).dot(right.subtract(mid)) > 0;
    }
    
    public static boolean inLine(Vector3f point1, Vector3f point2, Vector3f point3){
        
        return point1.subtract(point2).normalize().cross(point2.subtract(point3).normalize()).distance(Vector3f.ZERO) < FLT_EPSILON;
    }
    
    public static Vector3f lineToPointTranslation(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint){
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return targetPoint.subtract(linePoint.add(lineDirrection.normalize().mult(targetPoint.distance(linePoint)*FastMath.cos(angle)))).normalize();
    }
    
    public static Vector3f closestPointOnLine(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint){
        if(linePoint.subtract(targetPoint).normalize().cross(linePoint.subtract(linePoint.add(lineDirrection)).normalize()).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON){
            return targetPoint;
        }
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return linePoint.add(lineDirrection.normalize().mult(targetPoint.distance(linePoint)*FastMath.cos(angle)));
    }
    
    public static boolean inBoundary(Vector3f target, Vector3f[] boundary){
        Vector3f center = new Vector3f();
        for(Vector3f point:boundary){
            center.addLocal(point);
        }
        center.divideLocal(boundary.length);
        Vector3f normal = boundary[1].subtract(boundary[0]).cross(boundary[2].subtract(boundary[0]));
        Plane plane = new Plane();
        for(int i = 0; i < boundary.length;i++){
            plane.setPlanePoints(boundary[i], boundary[(i+1)%boundary.length], boundary[i].add(normal));
            if(!plane.whichSide(center).equals(plane.whichSide(target))){
                System.out.println("Out!!!");
                return false;
            }
        }
        return true;
    }
}
