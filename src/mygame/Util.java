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

    /**
     * A threshold value used to compare two float value if the difference
     * between two value is smaller that FLT_EPSILON we consider them as the
     * same value
     */
    public static final float FLT_EPSILON = 0.0001f;

    /**
     * Clone the ArrayList of an Vector3f and return as Array
     *
     * @param arrayList ArrayList of Vector3f
     * @return Array clone of Vector3f
     */
    public static Vector3f[] toArray(ArrayList<Vector3f> arrayList) {
        Vector3f[] array = new Vector3f[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            array[i] = arrayList.get(i).clone();
        }
        return array;
    }

    /**
     * Creates a polygon Mesh from the given vertices Vectices must be ordered
     *
     * @param vertices Ordered Vertices
     * @return Polygon mesh from the vertices
     */
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
                for (int i : notDone) {
                    Vector3f u = vertices[notDone.get((notDone.indexOf(i) - 1 + size) % size)].subtract(vertices[i]);
                    Vector3f v = vertices[notDone.get((notDone.indexOf(i) + 1) % size)].subtract(vertices[i]);
                    if (plane.whichSide(vertices[0].add(u.cross(v))).equals(correct)) {
                        Vector3f prevPointNormal = vertices[notDone.get((notDone.indexOf(i) - 2 + size) % size)].subtract(vertices[(i-1+size)%size]).cross(
                        vertices[notDone.get(notDone.indexOf(i))].subtract(vertices[(i-1+size)%size]));
                        Vector3f nextPointNormal = vertices[notDone.get(notDone.indexOf(i))].subtract(vertices[(i+1)%size]).cross(
                        vertices[notDone.get((notDone.indexOf(i) + 2)%size)].subtract(vertices[(i+1)%size]));
                        if(!plane.whichSide(vertices[0].add(prevPointNormal)).equals(correct) || !plane.whichSide(vertices[0].add(nextPointNormal)).equals(correct)){
                            clean = false;
                            triangle.add(notDone.get((notDone.indexOf(i) - 1 + size) % size));
                            triangle.add(i);
                            triangle.add(notDone.get((notDone.indexOf(i) + 1) % size));
                            notDone.remove(notDone.indexOf(i));
                            break;
                        }
                        
                    }
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

    /**
     * Return resulting point after a rotation translation of a point around a
     * given axis and radian; The rotate follows a left hand rotation rule with
     * the axis's dirrection being the order of the axis point given.
     *
     * @param point the point to rotate
     * @param axis1 pointA that makes the axis
     * @param axis2 pointB that makes the axis
     * @param radian radian to rotate
     * @return
     */
    public static Vector3f rotatePoint(Vector3f point, Vector3f axis1, Vector3f axis2, float radian) {
        Vector3f center = closestPointOnLine(axis1, axis2.subtract(axis1), point);
        Vector3f up = point.subtract(center);
        if(up.length() < FastMath.FLT_EPSILON){
            return point;
        }
        Vector3f side = setVector3fLength(axis1.subtract(axis2).cross(point.subtract(axis2)), up.length());
        return center.add(up.mult(FastMath.cos(radian))).add(side.mult(FastMath.sin(radian)));
        
    }

    /**
     * Checks if point Mid is between point left and right. Note that Mid being
     * between left and right doesn't mean that they are inLine
     *
     * @param left Left point
     * @param mid Mid Point
     * @param right Right
     * @return If mid is between left and right
     */
    public static boolean isBetween(Vector3f left, Vector3f mid, Vector3f right) {
        
        if (mid.distance(left) < FLT_EPSILON || mid.distance(right) < FLT_EPSILON) {
            return true;
        }
        return   mid.subtract(left).dot(right.subtract(mid)) > 0;
    }

    /**
     * Checks if point Mid is in line with point left and right Note the in line
     * doesn't gaurentees that mid is in between
     *
     * @param left Left Point
     * @param mid Mid Point
     * @param right Right Point
     * @return if mid, left and right are aligned
     */
    public static boolean inLine(Vector3f left, Vector3f mid, Vector3f right) {
        Vector3f vector1 = left.subtract(mid);
        Vector3f vector2 = mid.subtract(right);
        if (vector1.length() < FLT_EPSILON || vector2.length() < FLT_EPSILON) {
            return true;
        }
        //System.out.println("OOLD: "+ vector1.normalize().cross(vector2.normalize()).distance(Vector3f.ZERO));
        return vector1.normalize().cross(vector2.normalize()).distance(Vector3f.ZERO) < FLT_EPSILON;
    }

    /**
     * Set the length of a vector without the use of normalizing the vector in
     * the process
     *
     * @param vector Vector
     * @param length the desire length
     * @return vector scales to the desired lenth
     */
    public static Vector3f setVector3fLength(Vector3f vector, float length) {
        if (length == 0f) {
            return new Vector3f(0, 0, 0);
        }
        return vector.mult(length / vector.length());
    }

    /**
     * Get the shortest vector, perpendicular from am infinitely long line to a
     * point
     *
     * @param linePoint A point that define the line
     * @param lineDirrection The Direction of the line
     * @param targetPoint The target point
     * @return the vector from the line to the point
     */
    public static Vector3f lineToPointTranslation(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint) {
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return targetPoint.subtract(linePoint.add(setVector3fLength(lineDirrection, targetPoint.distance(linePoint) * FastMath.cos(angle))));
    }

    /**
     * Find the point on a infinitely long line that is closest to a targeted
     * point
     *
     * @param linePoint A point that defines the line
     * @param lineDirrection The direction of the line
     * @param targetPoint Target Point
     * @return The point on the line that is closest to the target Point
     */
    public static Vector3f closestPointOnLine(Vector3f linePoint, Vector3f lineDirrection, Vector3f targetPoint) {
        if (linePoint.subtract(targetPoint).normalize().cross(linePoint.subtract(linePoint.add(lineDirrection)).normalize()).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON) {
            return targetPoint;
        }
        float angle = lineDirrection.normalize().angleBetween(targetPoint.subtract(linePoint).normalize());
        return linePoint.add(setVector3fLength(lineDirrection, targetPoint.distance(linePoint) * FastMath.cos(angle)));
    }

    /**
     * Given a set points defining a boundary, Gets the point that is furthest
     * in a directions
     *
     * @param direction the direction
     * @param boundary the vertices definng a boundary
     * @return furthest vertex in a direction
     */
    public static Vector3f closestPointToDirection(Vector3f direction, Vector3f[] boundary) {

        Vector3f center = new Vector3f();

        for (Vector3f point : boundary) {
            center.addLocal(point);
        }
        center.divideLocal(boundary.length);
        Vector3f target = direction.mult(1000f).add(center);
        Vector3f closest = new Vector3f(center);
        for (Vector3f point : boundary) {
            if (point.distance(target) < closest.distance(target)) {
                closest.set(point);
            }
        }
        return closest;
    }

    /**
     * Checks if a infinely long line touches a boundary
     *
     * @param linePoint point that defines the line
     * @param lineDir the direction of the line
     * @param boundary the set of vertices making the boundary
     * @return
     */
    public static boolean lineTouchesBoundary(Vector3f linePoint, Vector3f lineDir, Vector3f[] boundary) {
        return !(castRayOnBoundary(linePoint, lineDir, boundary) == null && castRayOnBoundary(linePoint, lineDir.negate(), boundary) == null);
    }

    /**
     * Checks if a given point is coplanar and within a boundary
     *
     * @param target The target point
     * @param boundary The vertices making the boundary
     * @return if the target is in coplanar and within the boundary
     */
    public static boolean inBoundary(Vector3f target, Vector3f[] boundary) {
        if (FastMath.abs(target.subtract(boundary[0]).normalize().dot(getBountdaryNormal(boundary).normalize())) < FastMath.FLT_EPSILON) {
            //System.out.println("On Plane" + FastMath.abs(target.subtract(boundary[0].normalize()).dot(getBountdaryNormal(boundary).normalize())));
            Vector3f dir = new Vector3f();
            float length = 0f;
            ArrayList<Vector3f> collision = new ArrayList<>();
            for (int i = 0; i < boundary.length; i++) {
                if (target.distance(boundary[i]) > length) {
                    length = target.distance(boundary[i]);
                }
                dir.addLocal(boundary[i]);
            }
            Vector3f center = dir.divide(boundary.length);
            dir = setVector3fLength(center.subtract(target), length*2);
            for (int i = 0; i < boundary.length; i++) {
                if (target.equals(boundary[i]) || (inLine(boundary[i], target, boundary[(i + 1) % boundary.length])) && isBetween(boundary[i], target, boundary[(i + 1) % boundary.length])) {
                    return true;
                }
                Vector3f rayHit = segmentIntesection(target, target.add(dir), boundary[i], boundary[(i + 1) % boundary.length]);
                if (rayHit != null && !Util.containsVector3f(collision, rayHit)) {
                    collision.add(rayHit);
                }
            }

            if (collision.size() % 2 == 1) {
               System.out.println("boundary = new Vector3f[]{");
               for(Vector3f point:boundary){
                   System.out.println("new Vector3f("+point.x+"f,"+point.y+"f,"+point.z+"f),");
               }
                System.out.println("};");
                System.out.println("assertFalse(Util.inBoundary(new Vector3f("+target.x+"f, "+target.y+"f, "+target.z+"f), boundary));");
                
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Cast a Ray to the boundary and checks for collision with the frame of the
     * boundary only
     *
     * @param rayStart
     * @param rayDir
     * @param boundary
     * @return the point of collision
     */
    public static Vector3f castRayOnBoundary(Vector3f rayStart, Vector3f rayDir, Vector3f[] boundary) {
        Vector3f closestCollision = null;
        Vector3f rayEnd = rayStart.add(setVector3fLength(rayDir, 100f));
        for (int i = 0; i < boundary.length; i++) {
            if (!inLine(boundary[i], rayStart, boundary[(i + 1) % boundary.length])) {
                Vector3f intersection = segmentIntesection(rayStart, rayEnd, boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    if (closestCollision == null || intersection.distance(rayStart) < closestCollision.distance(rayStart)) {
                        closestCollision = intersection;
                    }
                }
            }
        }
        return closestCollision;
    }

    /**
     * Gets the pair of points that are furthest aways from each other from: the
     * set of intersection points between a boundary and an infinetly long line
     *
     * @param linePoint the point that defines the line
     * @param lineDir the direction of the line
     * @param boundary the boundary
     * @return the pair point intersection points
     */
    public static Vector3f[] lineBoundaryIntersectionPair(Vector3f linePoint, Vector3f lineDir, Vector3f[] boundary) {
        ArrayList<Vector3f> intersections = new ArrayList<>();

        for (int i = 0; i < boundary.length; i++) {
            if (inLine(boundary[i], lineDir, boundary[(i + 1) % boundary.length]) && isBetween(boundary[i], lineDir, boundary[(i + 1) % boundary.length])) {
                if (boundary[(i + 1) % boundary.length].subtract(boundary[i]).cross(lineDir).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON) {
                    intersections.add(boundary[i].clone());
                    intersections.add(boundary[(i + 1) % boundary.length].clone());
                } else if (linePoint.distance(boundary[i]) < FastMath.FLT_EPSILON) {
                    intersections.add(boundary[i].clone());
                    intersections.add(boundary[i].clone());
                }
            }
            if (linePoint.subtract(boundary[i]).cross(lineDir).distance(Vector3f.ZERO) < FastMath.FLT_EPSILON && inLine(boundary[i], lineDir, boundary[(i + 1) % boundary.length])) {
                intersections.add(boundary[i].clone());
            } else {
                Vector3f intersection = segmentIntesection(linePoint, linePoint.add(setVector3fLength(lineDir, 100f)), boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    intersections.add(intersection);
                }
                intersection = segmentIntesection(linePoint, linePoint.add(setVector3fLength(lineDir, -100f)), boundary[i], boundary[(i + 1) % boundary.length]);
                if (intersection != null) {
                    intersections.add(intersection);
                }
            }
        }
        if (!intersections.isEmpty()) {
            Vector3f[] returnPair = new Vector3f[2];
            returnPair[0] = new Vector3f();
            returnPair[1] = new Vector3f();
            for (int i = 0; i < intersections.size() - 1; i++) {
                for (int n = i; n < intersections.size(); n++) {
                    if (intersections.get(i).distance(intersections.get(n)) >= returnPair[0].distance(returnPair[1])) {
                        returnPair[0].set(intersections.get(i));
                        returnPair[1].set(intersections.get(n));
                    }
                }
            }
            return returnPair;
        } else {
            return null;
        }

    }

    /**
     * Intersection points of two line segments
     *
     * @param lineAStart
     * @param lineAEnd
     * @param lineBStart
     * @param lineBEnd
     * @return the intersection point of the two sigments
     */
    public static Vector3f segmentIntesection(Vector3f lineAStart, Vector3f lineAEnd, Vector3f lineBStart, Vector3f lineBEnd) {
        if ((lineAStart.distance(lineBStart) < FastMath.FLT_EPSILON || lineAEnd.distance(lineBStart) < FastMath.FLT_EPSILON) && !(inLine(lineAStart, lineBEnd, lineAEnd) && isBetween(lineAStart, lineBEnd, lineAEnd))) {
            return lineBStart;
        }
        if ((lineAStart.distance(lineBEnd) < FastMath.FLT_EPSILON || lineAEnd.distance(lineBEnd) < FastMath.FLT_EPSILON) && !(inLine(lineAStart, lineBStart, lineAEnd) && isBetween(lineAStart, lineBStart, lineAEnd))) {
            return lineBEnd;
        }
        if (lineAStart.subtract(lineAEnd).normalize().cross(lineBStart.subtract(lineBEnd).normalize()).distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
            if (inLine(lineAStart, lineBEnd, lineAEnd) && isBetween(lineAStart, lineBEnd, lineAEnd)) {
                return lineBEnd;
            }
            if (inLine(lineAStart, lineBStart, lineAEnd) && isBetween(lineAStart, lineBStart, lineAEnd)) {
                return lineBStart;
            }
            if (inLine(lineBStart, lineAEnd, lineBEnd) && isBetween(lineBStart, lineAEnd, lineBEnd)) {
                return lineAEnd;
            }
            if (inLine(lineBStart, lineAStart, lineBEnd) && isBetween(lineBStart, lineAStart, lineBEnd)) {
                return lineAStart;
            }
        }
        return lineSegmentIntersectHelper(lineAStart, lineAEnd, lineBStart, lineBEnd, false);
    }

    /**
     * Intersection of two infinitly long line
     *
     * @param lineAPoint1
     * @param lineAPoint2
     * @param lineBPoint1
     * @param lineBPoint2
     * @return
     */
    public static Vector3f lineIntersection(Vector3f lineAPoint1, Vector3f lineAPoint2, Vector3f lineBPoint1, Vector3f lineBPoint2) {
        return lineSegmentIntersectHelper(lineAPoint1, lineAPoint2, lineBPoint1, lineBPoint2, true);
    }

    /**
     * Helper method for lineIntersection and segmentIntersection Where line 1 &
     * 2 define one line and 3 & 4 define another
     *
     * @param point1 linePoint1
     * @param point2 linePoint2
     * @param point3 linePoint3
     * @param point4 linePoint4
     * @param infiniteLine if the lines are infinely long or not
     * @return collision point
     */
    private static Vector3f lineSegmentIntersectHelper(Vector3f point1, Vector3f point2, Vector3f point3, Vector3f point4, boolean infiniteLine) {
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
        if (pointA.distance(pointB) < 0.01) {
            if (infiniteLine || (isBetween(point1, pointA, point2) && isBetween(point3, pointB, point4))) {
                return pointA;
            } else {
                //System.out.println("NotBetween " + infiniteLine);
                return null;
            }

        } else {
            //System.out.println("Distance = " + pointA.distance(pointB));
            return null;
        }
    }

    /**
     * Get the intersection between a infinitely long line and a infinitely
     * large plane
     *
     * @param linePoint point that difines the line
     * @param lineDir direction of the line
     * @param planePoint point that defines the plane
     * @param planeNormal the normal verctor of the plane
     * @return
     */
    public static Vector3f linePlaneIntersection(Vector3f linePoint, Vector3f lineDir, Vector3f planePoint, Vector3f planeNormal) {

        if (FastMath.abs(lineDir.dot(planeNormal)) > FastMath.FLT_EPSILON) {
            float d = (planePoint.subtract(linePoint).dot(planeNormal)) / (lineDir.dot(planeNormal));
            return linePoint.add(lineDir.mult(d));
        } else {
            //System.out.println("lineDir.dot(planeNormal) = " + lineDir.dot(planeNormal));
        }

        return null;
    }

    /**
     * Get the intersection between an infinitely large plane and a ray cast
     *
     * @param rayStart
     * @param rayDir
     * @param planePoint
     * @param planeNormal
     * @return the intersection Point
     */
    public static Vector3f rayPlaneIntersection(Vector3f rayStart, Vector3f rayDir, Vector3f planePoint, Vector3f planeNormal) {

        if (FastMath.abs(rayDir.normalize().dot(planeNormal.normalize())) > FastMath.FLT_EPSILON) {
            float d = (planePoint.subtract(rayStart).dot(planeNormal)) / (rayDir.dot(planeNormal));
            if (d >= 0) {
                return rayStart.add(rayDir.mult(d));
            }
        } else {
            //System.out.println("rayDir.dot(planeNormal) = " + rayDir.dot(planeNormal));
        }

        return null;
    }

    /**
     * get the Normal of a boundary
     *
     * @param boundary vertices of the boudary
     * @return normal of the boundary
     */
    public static Vector3f getBountdaryNormal(Vector3f[] boundary) {
        for (int i = 0; i < boundary.length; i++) {
            Vector3f normal = boundary[(boundary.length + i - 1) % boundary.length].subtract(boundary[i]).normalize().cross(boundary[(i + 1) % boundary.length].subtract(boundary[i]).normalize()).normalize();
            if (normal.distance(Vector3f.ZERO) > FastMath.FLT_EPSILON) {
                return normal;
            }
        }
        return null;
    }
    public static boolean onBoundary(Vector3f target,Vector3f[] boundary){
        for(int i = 0; i < boundary.length;i++){
            if(inLine(boundary[i], target, boundary[(i + 1) % boundary.length]) && isBetween(boundary[i], target, boundary[(i + 1) % boundary.length])){
                return true;
            }
        }
        return false;
    }
    /**
     * Intersection of two boundaries
     *
     * @param boundaryA boundaryA
     * @param boundaryB boundaryB
     * @return intersection of the two boundaries
     */
    public static ArrayList<Vector3f> boundboundIntersect(Vector3f[] boundaryA, Vector3f[] boundaryB) {
        ArrayList<Vector3f> collisionList = new ArrayList<>();
        Vector3f normalA = getBountdaryNormal(boundaryA);
        Vector3f normalB = getBountdaryNormal(boundaryB);

        for (int i = 0; i < boundaryA.length; i++) {
            Vector3f nextPoint = boundaryA[(i + 1) % boundaryA.length];
            Vector3f planeCollision = Util.rayPlaneIntersection(boundaryA[i], nextPoint.subtract(boundaryA[i]).normalize(), boundaryB[0], normalB);
            if (planeCollision != null
                    && planeCollision.distanceSquared(boundaryA[i]) < boundaryA[i].distanceSquared(nextPoint)
                    && planeCollision.distanceSquared(nextPoint) > FLT_EPSILON
                    && planeCollision.distanceSquared(boundaryA[i]) > FLT_EPSILON
                    && Util.inBoundary(planeCollision, boundaryB) &&
                    !onBoundary(planeCollision, boundaryB)) {
                collisionList.add(planeCollision);
            }
        }
        for (int i = 0; i < boundaryB.length; i++) {
            Vector3f nextPoint = boundaryB[(i + 1) % boundaryB.length];
            Vector3f planeCollision = Util.rayPlaneIntersection(boundaryB[i], nextPoint.subtract(boundaryB[i]).normalize(), boundaryA[0], normalA);
            if (planeCollision != null
                    && planeCollision.distanceSquared(boundaryB[i]) < boundaryB[i].distanceSquared(nextPoint)
                    && planeCollision.distanceSquared(nextPoint) > FLT_EPSILON
                    && planeCollision.distanceSquared(boundaryB[i]) > FLT_EPSILON
                    && Util.inBoundary(planeCollision, boundaryA) &&
                    !onBoundary(planeCollision, boundaryA)) {
                collisionList.add(planeCollision);
            }
        }

        if (collisionList.size() < 1) {
            return null;
        } else {
            return collisionList;
        }
    }
    /**
     * checks if an ArrayList of Vector3f contains a target
     * @param list ArrayList
     * @param target Target
     * @return true if contains, false otherwise.
     */
    private static boolean containsVector3f(ArrayList<Vector3f> list, Vector3f target) {
        for (Vector3f point : list) {
            if (target.distance(point) < Util.FLT_EPSILON) {
                return true;
            }
        }
        return false;
    }
}
