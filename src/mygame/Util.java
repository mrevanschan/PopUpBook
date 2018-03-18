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

/**
 *
 * @author Evans
 */
public final class Util {

    public static final float FLT_EPSILON = 0.00001f;

    public static Vector3f[] toArray(ArrayList<Vector3f> arrayList) {
        Vector3f[] array = new Vector3f[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
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

    public static boolean isBetween(Vector3f left, Vector3f mid, Vector3f right) {
        return mid.subtract(left).dot(right.subtract(mid)) > 0;
    }

    public static boolean inLine(Vector3f point1, Vector3f point2, Vector3f point3) {
        return point1.subtract(point2).normalize().cross(point2.subtract(point3).normalize()).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON;
    }

    public static Vector3f lineToPointTranslation(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint) {
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return targetPoint.subtract(linePoint.add(lineDirrection.normalize().mult(targetPoint.distance(linePoint) * FastMath.cos(angle))));
    }
    

    public static Vector3f closestPointOnLine(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint) {
        if (linePoint.subtract(targetPoint).normalize().cross(linePoint.subtract(linePoint.add(lineDirrection)).normalize()).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON) {
            return targetPoint;
        }
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return linePoint.add(lineDirrection.normalize().mult(targetPoint.distance(linePoint) * FastMath.cos(angle)));
    }
    
    public static Vector3f closestPointToDirrection(Vector3f dirrection, Vector3f[] boundary){
        Vector3f target = dirrection.mult(1000f);
        Vector3f center = new Vector3f();
        Vector3f closest = new Vector3f();
        for(Vector3f point: boundary){
            center.addLocal(point);
        }
        center.divide(center);
        for(Vector3f point: boundary){
            if(point.distance(target) < closest.distance(target)){
                closest.set(point);
            }
        }
        return closest;
    }
    public static boolean lineTouchesBoundary(Vector3f rayPoint, Vector3f rayDir,Vector3f[] boundary){
        return !(castLineOnBoundary(rayPoint,rayDir, boundary) == null && castLineOnBoundary(rayPoint,rayDir.negate(), boundary) == null); 
    }
    public static boolean inBoundary(Vector3f target, Vector3f[] boundary) {
        Vector3f dir = boundary[0].add(boundary[1]).divide(2).subtract(target).normalize().mult(100f);
        int intersectCount = 0;
        for (int i = 0; i < boundary.length; i++) {
            if (inLine(boundary[i], target, boundary[(i + 1) % boundary.length]) && isBetween(boundary[i], target, boundary[(i + 1) % boundary.length])) {
                return true;
            }
            if (lineIntersection(target, target.add(dir), boundary[i], boundary[(i + 1) % boundary.length]) != null) {
                intersectCount++;
            }
        }

        return (intersectCount & 1) != 0;
    }

    public static Vector3f castLineOnBoundary(Vector3f rayStart, Vector3f rayDir, Vector3f[] boundary) {
        Vector3f closestCollision = null;
        Vector3f rayEnd = rayStart.add(rayDir.normalize().mult(100f));
        for (int i = 0; i < boundary.length; i++) {
            if (inLine(boundary[i], rayStart, boundary[(i + 1) % boundary.length])) {
                if(boundary[i].distance(rayStart) < boundary[(i + 1) % boundary.length].distance(rayStart)){
                    if (closestCollision == null || boundary[i].distance(rayStart) < closestCollision.distance(rayStart)) {
                        closestCollision = boundary[i].clone();
                    }
                }else{
                    if (closestCollision == null || boundary[(i + 1) % boundary.length].distance(rayStart) < closestCollision.distance(rayStart)) {
                        closestCollision = boundary[(i + 1) % boundary.length].clone();
                    }
                }
            } else {
                Vector3f intersection = lineIntersection(rayStart, rayEnd, boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    if (closestCollision == null || intersection.distance(rayStart) < closestCollision.distance(rayStart)) {
                        closestCollision = intersection;
                    }
                }
            }

        }
        return closestCollision;
    }
    public static Vector3f[] lineBoundaryIntersections(Vector3f point, Vector3f dir, Vector3f[] boundary){
        ArrayList<Vector3f> intersections = new ArrayList<>();
        
        for (int i = 0; i < boundary.length; i++) {
            // ADD SOMETHING HERE TO MAKE IT WORK FOR CORNERS
            System.out.println("Checking Point[" + i + "]");
            System.out.println("Cross = " + point.subtract(boundary[i]).cross(dir).distance(Vector3f.ZERO));
            if (point.subtract(boundary[i]).cross(dir).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON) {
                intersections.add(boundary[i].clone());
            } else {
                Vector3f intersection = lineIntersection(point, point.add(dir.normalize().mult(100f)), boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    intersections.add(intersection);
                }
                intersection = lineIntersection(point, point.add(dir.negate().normalize().mult(100f)), boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    intersections.add(intersection);
                }
            }
        }
        System.out.println("Intersection Count = " +intersections.size());
        Vector3f[] returnPair = new Vector3f[2];
        returnPair[0] = new Vector3f();
        returnPair[1] = new Vector3f();
        for(int i = 0; i < intersections.size()-1;i++){
            for(int n = i+1; n < intersections.size();n++){
                if(intersections.get(i).distance(intersections.get(n)) >= returnPair[0].distance(returnPair[1])){
                    returnPair[0].set(intersections.get(i));
                    returnPair[1].set(intersections.get(n));
                }
            }
        }
        return returnPair;
    }

    public static Vector3f lineIntersection(Vector3f point1, Vector3f point2, Vector3f point3, Vector3f point4) {
        Vector3f v13 = point1.subtract(point3);
        Vector3f v43 = point4.subtract(point3);
        Vector3f v21 = point2.subtract(point1);

        if (v43.lengthSquared() < FastMath.FLT_EPSILON || v21.lengthSquared() < FastMath.FLT_EPSILON) {
            return null;
        }
        float d1343 = v13.x * v43.x + v13.y * v43.y + v13.z * v43.z;
        float d4321 = v43.x * v21.x + v43.y * v21.y + v43.z * v21.z;
        float d1321 = v13.x * v21.x + v13.y * v21.y + v13.z * v21.z;
        float d4343 = v43.x * v43.x + v43.y * v43.y + v43.z * v43.z;
        float d2121 = v21.x * v21.x + v21.y * v21.y + v21.z * v21.z;

        float denom = d2121 * d4343 - d4321 * d4321;
        if (FastMath.abs(denom) < FastMath.FLT_EPSILON) {
            //System.out.println("Denom = " + FastMath.abs(denom));
            return null;
        }
        float numer = d1343 * d4321 - d1321 * d4343;

        float mua = numer / denom;
        float mub = (d1343 + d4321 * mua) / d4343;
        Vector3f pointA = point1.add(v21.mult(mua));
        Vector3f pointB = point3.add(v43.mult(mub));
        if (pointA.distance(pointB) < FLT_EPSILON) {
            if (isBetween(point1, pointA, point2) && isBetween(point3, pointB, point4)) {
                return pointA;
            } else {
                return null;
            }

        } else {
            //System.out.println("Distance = " + pointA.distance(pointB));
            return null;
        }
    }
}
