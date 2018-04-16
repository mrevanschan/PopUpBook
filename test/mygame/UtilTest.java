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
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Evans
 */
public class UtilTest {

    public UtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public void assertVector3f(Vector3f vectorA, Vector3f vectorB) {
        Assert.assertEquals(vectorA.distance(vectorB), 0.0f, 0.000001);
    }

    /**
     * Test of rotatePoint method, of class Util.
     */
    @Test
    public void testRotatePoint() {
        System.out.println("rotatePoint");
        Vector3f point = new Vector3f(0, 2, 0);
        Vector3f axis1 = new Vector3f(5, 0, 0);
        Vector3f axis2 = new Vector3f(-5, 0, 0);
        float pi = FastMath.PI;

        //Test All angles from -360 degrees to 360 degrees with 45 degree increments
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -0.25f), new Vector3f(0, 2 * FastMath.cos(pi * 0.25f), 2 * FastMath.sin(pi * -0.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -0.50f), new Vector3f(0, 2 * FastMath.cos(pi * 0.50f), 2 * FastMath.sin(pi * -0.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -0.75f), new Vector3f(0, 2 * FastMath.cos(pi * 0.75f), 2 * FastMath.sin(pi * -0.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -1.00f), new Vector3f(0, 2 * FastMath.cos(pi * 1.00f), 2 * FastMath.sin(pi * -1.00f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -1.25f), new Vector3f(0, 2 * FastMath.cos(pi * 1.25f), 2 * FastMath.sin(pi * -1.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -1.50f), new Vector3f(0, 2 * FastMath.cos(pi * 1.50f), 2 * FastMath.sin(pi * -1.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -1.75f), new Vector3f(0, 2 * FastMath.cos(pi * 1.75f), 2 * FastMath.sin(pi * -1.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * -2.00f), new Vector3f(0, 2 * FastMath.cos(pi * 2.00f), 2 * FastMath.sin(pi * -2.00f)));

        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 0f), point);

        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 0.25f), new Vector3f(0, 2 * FastMath.cos(pi * 0.25f), 2 * FastMath.sin(pi * 0.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 0.50f), new Vector3f(0, 2 * FastMath.cos(pi * 0.50f), 2 * FastMath.sin(pi * 0.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 0.75f), new Vector3f(0, 2 * FastMath.cos(pi * 0.75f), 2 * FastMath.sin(pi * 0.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 1.00f), new Vector3f(0, 2 * FastMath.cos(pi * 1.00f), 2 * FastMath.sin(pi * 1.00f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 1.25f), new Vector3f(0, 2 * FastMath.cos(pi * 1.25f), 2 * FastMath.sin(pi * 1.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 1.50f), new Vector3f(0, 2 * FastMath.cos(pi * 1.50f), 2 * FastMath.sin(pi * 1.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 1.75f), new Vector3f(0, 2 * FastMath.cos(pi * 1.75f), 2 * FastMath.sin(pi * 1.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi * 2.00f), new Vector3f(0, 2 * FastMath.cos(pi * 2.00f), 2 * FastMath.sin(pi * 2.00f)));

    }

    /**
     * Test of isBetween method, of class Util.
     */
    @Test
    public void testIsBetween() {
        System.out.println("isBetween");
        Vector3f left = new Vector3f(-5, 0, 0);
        Vector3f right = new Vector3f(5, 0, 0);

        //Case: middle equals left
        assertTrue(Util.isBetween(left, left, right));

        //Case: middle equals right
        assertTrue(Util.isBetween(left, right, right));

        //Case: middle between left and right
        assertTrue(Util.isBetween(left, new Vector3f(0, 0, 0), right));

        //Case: middle outside left and right
        assertFalse(Util.isBetween(left, new Vector3f(-6, 0, 0), right));

        //Case: middle outside left and right
        assertFalse(Util.isBetween(left, new Vector3f(6, 0, 0), right));

    }

    /**
     * Test of inLine method, of class Util.
     */
    @Test
    public void testInLine() {
        System.out.println("inLine");
        Vector3f left = new Vector3f(-5, 0, 0);
        Vector3f right = new Vector3f(5, 0, 0);
        //Case: middle Point same as left
        assertTrue(Util.inLine(left, left, right));

        //Case: middle Point same as right
        assertTrue(Util.inLine(left, right, right));

        //Case: middle Point in between left & right
        assertTrue(Util.inLine(left, new Vector3f(0, 0, 0), right));

        //Case: middle Point outside left & right
        assertTrue(Util.inLine(left, new Vector3f(6, 0, 0), right));

        //Case: middle Point outside left & right
        assertTrue(Util.inLine(left, new Vector3f(-6, 0, 0), right));

        //Case: middle Point not in line
        assertFalse(Util.inLine(left, new Vector3f(-6, 1, 0), right));
    }

    /**
     * Test of lineToPointTranslation method, of class Util.
     */
    @Test
    public void testLineToPointTranslation() {
        System.out.println("lineToPointTranslation");
        Vector3f linePoint = new Vector3f();
        Vector3f lineDirrection = new Vector3f(1, 0, 0);

        //Case: target is on line
        Vector3f target = linePoint;
        assertVector3f(Util.lineToPointTranslation(linePoint, lineDirrection, target), Vector3f.ZERO);

        //Case: target is not on line
        target = new Vector3f(0, 3, 0);
        assertVector3f(Util.lineToPointTranslation(linePoint, lineDirrection, target), target);

    }

    /**
     * Test of closestPointOnLine method, of class Util.
     */
    @Test
    public void testClosestPointOnLine() {
        System.out.println("closestPointOnLine");
        Vector3f linePoint = new Vector3f(0, 0, 0);
        Vector3f lineDirrection = new Vector3f(1, 0, 0);

        //Case: target is on line
        Vector3f target = linePoint;
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), target);

        //Case: target target not on line near the line dirrection
        target = new Vector3f(1, 1, 0);
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), new Vector3f(1, 0, 0));

        //Case: target target not on line and opposite the line dirrection
        target = new Vector3f(-1, 1, 0);
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), new Vector3f(-1, 0, 0));

        //Case: target target not on line and near the linePoint
        target = new Vector3f(0, 1, 0);
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), new Vector3f(0, 0, 0));
    }

    /**
     * Test of closestPointToDirrection method, of class Util.
     */
    @Test
    public void testClosestPointToDirrection() {
        System.out.println("closestPointToDirrection");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0),};

        //Check the closeset point of each directions
        assertVector3f(Util.closestPointToDirection(new Vector3f(0, 1, 0), boundary), new Vector3f(-1, 1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(0, -1, 0), boundary), new Vector3f(-1, -1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(1, 0, 0), boundary), new Vector3f(1, 1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(-1, 0, 0), boundary), new Vector3f(-1, -1, 0));

        assertVector3f(Util.closestPointToDirection(new Vector3f(-1, -1, 0), boundary), new Vector3f(-1, -1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(1, -1, 0), boundary), new Vector3f(1, -1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(1, 1, 0), boundary), new Vector3f(1, 1, 0));
        assertVector3f(Util.closestPointToDirection(new Vector3f(-1, 1, 0), boundary), new Vector3f(-1, 1, 0));
    }

    /**
     * Test of lineTouchesBoundary method, of class Util.
     */
    @Test
    public void testLineTouchesBoundary() {
        System.out.println("lineTouchesBoundary");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0),};

        //Test line shooting toward the boundary in the center from outside boundary
        assertTrue(Util.lineTouchesBoundary(new Vector3f(5, 0, 0), new Vector3f(-1, 0, 0), boundary));

        //Test line shooting parallel on and to an edge from outside boundary
        assertTrue(Util.lineTouchesBoundary(new Vector3f(5, 1, 0), new Vector3f(-1, 0, 0), boundary));

        //Test line shooting toward boundary edge from the center of boundary
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 0, 0), new Vector3f(-1, 0, 0), boundary));

        //Test line shooting from edge toward dirrection parallel to edge
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), boundary));

        //Test line shooting from inside to a vertex
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 0, 0), new Vector3f(-1, -1, 0), boundary));

        //Test line shooting from outside to a vertex
        assertTrue(Util.lineTouchesBoundary(new Vector3f(2, 0, 0), new Vector3f(-1, -1, 0).subtract(new Vector3f(2, 0, 0)).normalize(), boundary));

        //Test line shooting away from boundary from outside the boundary
        assertFalse(Util.lineTouchesBoundary(new Vector3f(2, 0, 0), new Vector3f(0, 1, 0), boundary));

    }

    /**
     * Test of inBoundary method, of class Util.
     */
    @Test
    public void testInBoundary() {
        System.out.println("inBoundary");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(1, 0, 0)};

        //Test Enge of boundary
        assertTrue(Util.inBoundary(new Vector3f(0, 0, 0), boundary));

        //Test Corner of boundary
        assertTrue(Util.inBoundary(new Vector3f(0, 1, 0), boundary));

        //Test OutSide of boundary
        assertFalse(Util.inBoundary(new Vector3f(2, 1, 0), boundary));


    }

    /**
     * Test of inBoundary method, of class Util.
     */
    @Test
    public void testOnBoundary() {
        System.out.println("onBoundary");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(1, 0, 0)};

        //Test Enge of boundary
        assertTrue(Util.onBoundary(new Vector3f(0, 0, 0), boundary));

        //Test Corner of boundary
        assertTrue(Util.onBoundary(new Vector3f(0, 1, 0), boundary));

        //Test OutSide of boundary
        assertFalse(Util.onBoundary(new Vector3f(2, 1, 0), boundary));

        //Test IutSide of boundary
        assertFalse(Util.onBoundary(new Vector3f(0, 0.5f, 0), boundary));

    }

    /**
     * Test of inBoundary method, of class Util.
     */
    @Test
    public void testGetBoundaryNormal() {
        System.out.println("getBoundaryNormal");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(0, 0, 0),
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(-1, 0, 0)};

        //Tested a boundary with multiple vertices in a line
        assertVector3f(Util.getBountdaryNormal(boundary), new Vector3f(0, 0, 1));
    }

    /**
     * Test of castRayOnBoundary method, of class Util.
     */
    @Test
    public void testCastLineOnBoundary() {
        System.out.println("castRayOnBoundary");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0),};

        //Cast Line From outside towards boundary
        assertVector3f(Util.castRayOnBoundary(new Vector3f(2, 0, 0), new Vector3f(-1, 0, 0), boundary), new Vector3f(1, 0, 0));

        //Cast Line From outside boundary toward and parallel to an edge
        assertVector3f(Util.castRayOnBoundary(new Vector3f(2, 1, 0), new Vector3f(-1, 0, 0), boundary), new Vector3f(1, 1, 0));

        //Cast Line From middle of an edge and out parallel to that edge
        assertVector3f(Util.castRayOnBoundary(new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), boundary), new Vector3f(-1, 1, 0));

        //Cast Line From inside the boundary to one vertex
        assertVector3f(Util.castRayOnBoundary(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0), boundary), new Vector3f(1, 1, 0));

        //Cast Line From outside boundary to one vertex
        assertVector3f(Util.castRayOnBoundary(new Vector3f(3, 0, 0), new Vector3f(1, 1, 0).subtract(new Vector3f(3, 0, 0)).normalize(), boundary), new Vector3f(1, 1, 0));

        //Cast Line From outside boundary away from boundary
        assertNull(Util.castRayOnBoundary(new Vector3f(2, 0, 0), new Vector3f(1, 0, 0), boundary));
    }

    /**
     * Test of lineBoundaryIntersectionPair method, of class Util.
     */
    @Test
    public void testLineBoundaryIntersectionPair() {
        System.out.println("lineBoundaryIntersectionPair");
        Vector3f[] boundary = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0),};
        //Test Ray That From point outside boundary to a boundary vertex
        //               
        //                          
        //               ___________
        //              |           |
        //     ---------+-----X-----+------
        //              |___________|
        //
        Vector3f[] result = Util.lineBoundaryIntersectionPair(Vector3f.ZERO, Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //               
        //                          
        //               ___________
        //              |           |
        //    ----X-----+-----------+------
        //              |___________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(3, 0, 0), Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //               
        //                          
        //               ___________
        //              |           |
        //        ------X-----------+----
        //              |___________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(1, 0, 0), Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 0, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 0, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //               
        //                          
        //      --------X--------------
        //              |             |
        //              |             |
        //              |_____________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(1, 1, 0), Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //
        //              |  
        //              X 
        //              |_____________             
        //              |             |
        //              |             |
        //              |             |
        //              |_____________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(3, 1, 0), Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //                /
        //               /
        //              /_____________
        //             /|             |
        //            / |             |
        //           X  |             |
        //          /   |_____________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(0, 1, 0), Vector3f.UNIT_X, boundary);
        assertTrue(result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(-1, 1, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //                 
        //             \ 
        //              \____
        //              |\   |
        //              | X  |
        //              |  \ |
        //              |___\|
        //                   \
        result = Util.lineBoundaryIntersectionPair(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0), boundary);
        assertTrue(result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(-1, -1, 0)) < FastMath.FLT_EPSILON
                || result[1].distance(new Vector3f(-1, -1, 0)) < FastMath.FLT_EPSILON && result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //                
        //               /
        //              /_____________
        //             /|             |
        //            / |             |
        //           X  |             |
        //          /   |_____________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(3, 0, 0), new Vector3f(1, 1, 0).subtract(new Vector3f(3, 0, 0)).normalize(), boundary);
        assertTrue(result[0].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON && result[1].distance(new Vector3f(1, 1, 0)) < FastMath.FLT_EPSILON);

        //Test Ray That From point outside boundary to a boundary vertex
        //                
        //          ------------X---------------    
        //               _____________
        //              |             |
        //              |             |
        //              |             |
        //              |_____________|
        //
        result = Util.lineBoundaryIntersectionPair(new Vector3f(0, 3, 0), new Vector3f(1, 0, 0), boundary);
        assertNull(result);

    }

    /**
     * Test of segmentIntesection method, of class Util.
     */
    @Test
    public void testSegmentIntesection() {
        System.out.println("segmentIntesection");
        //Test Intersection of Segment which starting point touches
        //
        //    1              2
        //    X--------------X 3 
        //                   |
        //                   |
        //                   |
        //                   |
        //                   |
        //                   X 4
        // 
        assertVector3f(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(4, 0, 0), new Vector3f(8, 0, 0)), new Vector3f(4, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(8, 0, 0), new Vector3f(4, 0, 0)), new Vector3f(4, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(1, 0, 0), new Vector3f(8, 4, 0)), new Vector3f(1, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(8, 4, 0), new Vector3f(1, 0, 0)), new Vector3f(1, 0, 0));

        //Test Intersection of Crossing segments
        //
        //            X 3
        //            |
        //    1       |
        //    X-------+------X 2 
        //            |
        //            |
        //            X 4
        //               
        assertVector3f(Util.segmentIntesection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));

        //Test Intersection of Segment of "T" Shape
        //
        //                 X 3
        //                 |
        //    1            |
        //    X------------X 2 
        //                 |
        //                 |
        //                 X 4
        //                 
        assertVector3f(Util.segmentIntesection(new Vector3f(0, 0, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(4, 4, 0), new Vector3f(0, 0, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(0, 0, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.segmentIntesection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(0, 0, 0)), new Vector3f(0, 0, 0));

        //Test Intersection of Parallel segment overlaping
        //    
        //    X------X-----X--------X 
        //    1      3     2        4
        //
        assertNull(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(3, 0, 0), new Vector3f(8, 0, 0)));
        assertNull(Util.segmentIntesection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(3, 0, 0), new Vector3f(4, 0, 0)));

        //Test Intersection of non-connected segments
        //           3            4
        //           X------------X 
        //  
        //  X------------X
        //  1            2
        assertNull(Util.segmentIntesection(new Vector3f(1, 1, 0), new Vector3f(4, 1, 0), new Vector3f(3, 0, 0), new Vector3f(8, 0, 0)));
    }

    /**
     * Test of lineIntersection method, of class Util.
     */
    @Test
    public void testLineIntersection() {
        System.out.println("lineIntersection");
        //Test Intersection of Segment which starting point touches
        //
        //                   ^ 3
        //                   | 
        //                   |
        //                   |
        //                   |
        //                   |
        //                   |
        //   <---------------y 4
        //   1               2
        assertVector3f(Util.lineIntersection(new Vector3f(1, 1, 0), new Vector3f(4, 0, 0), new Vector3f(4, 0, 0), new Vector3f(8, 0, 0)), new Vector3f(4, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(1, 1, 0), new Vector3f(4, 0, 0), new Vector3f(8, 0, 0), new Vector3f(4, 0, 0)), new Vector3f(4, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(1, 0, 0), new Vector3f(8, 4, 0)), new Vector3f(1, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(8, 4, 0), new Vector3f(1, 0, 0)), new Vector3f(1, 0, 0));

        //Test Intersection of Crossing segments
        //
        //            ^ 3
        //            |
        //    1       |
        //    <-------+------> 2 
        //            |
        //            |
        //            V 4
        //               
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));

        //Test Intersection of Segment of "T" Shape
        //
        //                 ^ 3
        //                 |
        //    1            |
        //    <------------> 2 
        //                 |
        //                 |
        //                 V 4
        //                 
        assertVector3f(Util.lineIntersection(new Vector3f(0, 0, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(0, 0, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(0, 0, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(0, 0, 0)), new Vector3f(0, 0, 0));

        //Test Intersection of Segment of "T" Shape
        //
        //                 ^ 3
        //                 |
        //    1            |
        //    <-------> 2  |
        //                 |
        //                 |
        //                 V 4
        //                 
        assertVector3f(Util.lineIntersection(new Vector3f(-1, -1, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-1, -1, 0), new Vector3f(4, -4, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(-1, 1, 0), new Vector3f(-4, 4, 0)), new Vector3f(0, 0, 0));
        assertVector3f(Util.lineIntersection(new Vector3f(4, 4, 0), new Vector3f(-4, -4, 0), new Vector3f(4, -4, 0), new Vector3f(1, -1, 0)), new Vector3f(0, 0, 0));

        //Test Intersection of Parallel segment overlaping
        //    
        //    <------<----->--------> 
        //    1      3     2        4
        //
        assertNull(Util.lineIntersection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(3, 0, 0), new Vector3f(8, 0, 0)));
        assertNull(Util.lineIntersection(new Vector3f(1, 0, 0), new Vector3f(4, 0, 0), new Vector3f(3, 0, 0), new Vector3f(4, 0, 0)));

        //Test Intersection of non-connected segments
        //           3            4
        //           <------------> 
        //  
        //  <------------>
        //  1            2
        assertNull(Util.lineIntersection(new Vector3f(1, 1, 0), new Vector3f(4, 1, 0), new Vector3f(3, 0, 0), new Vector3f(8, 0, 0)));
    }

    /**
     * Test of linePlaneIntersection method, of class Util.
     */
    @Test
    public void testLinePlaneIntersection() {
        System.out.println("linePlaneIntersection");

        Vector3f planePoint = new Vector3f(0, 0, 0);
        Vector3f planeNormal = new Vector3f(0, 1, 0);

        //Test if a line does not start on plane and is dirrection is toward the plane
        Vector3f origin = new Vector3f(0, 5, 0);
        Vector3f target = new Vector3f(0, 0, 0);
        assertVector3f(Util.linePlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal), target);

        //Test if a line does not start on plane and is dirrection is toward the plane
        origin = new Vector3f(0, 5, 0);
        target = new Vector3f(0, 0, 0);
        assertVector3f(Util.linePlaneIntersection(origin, origin.subtract(target).normalize(), planePoint, planeNormal), target);

        //Test if line starts from plane and line dirrection is not parallel to plane
        origin = new Vector3f(0, 0, 0);
        target = new Vector3f(0, 1, 0);
        assertVector3f(Util.linePlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal), origin);

        //Test if line dirrection does not touch plane from line starting point
        origin = new Vector3f(0, 1, 0);
        target = new Vector3f(0, 1, 0);
        assertNull(Util.linePlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal));

        //Test if line and plane is parallel
        origin = new Vector3f(0, 0, 0);
        target = new Vector3f(1, 0, 0);
        assertNull(Util.linePlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal));

    }

    /**
     * Test of linePlaneIntersection method, of class Util.
     */
    @Test
    public void testRayPlaneIntersection() {
        System.out.println("rayPlaneIntersection");

        Vector3f planePoint = new Vector3f(0, 0, 0);
        Vector3f planeNormal = new Vector3f(0, 1, 0);

        //Test if a line does not start on plane and is dirrection is toward the plane
        Vector3f origin = new Vector3f(0, 5, 0);
        Vector3f target = new Vector3f(0, 0, 0);
        assertVector3f(Util.rayPlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal), target);

        //Test if a line does not start on plane and is dirrection is toward the plane
        origin = new Vector3f(0, 5, 0);
        target = new Vector3f(0, 0, 0);
        assertNull(Util.rayPlaneIntersection(origin, origin.subtract(target).normalize(), planePoint, planeNormal));

        //Test if line starts from plane and line dirrection is not parallel to plane
        origin = new Vector3f(0, 0, 0);
        target = new Vector3f(0, 1, 0);
        assertVector3f(Util.rayPlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal), origin);

        //Test if line dirrection does not touch plane from line starting point
        origin = new Vector3f(0, 1, 0);
        target = new Vector3f(0, 1, 0);
        assertNull(Util.rayPlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal));

        //Test if line and plane is parallel
        origin = new Vector3f(0, 0, 0);
        target = new Vector3f(1, 0, 0);
        assertNull(Util.rayPlaneIntersection(origin, target.subtract(origin).normalize(), planePoint, planeNormal));

    }

    /**
     * Test of boundboundIntersect method, of class Util.
     */
    @Test
    public void testBoundboundIntersect() {
        System.out.println("boundboundIntersect");

        //Test Intersection of Inter Linked boundaries(Non coplanar)
        //
        //               ___________
        //     _________|           |
        //     \        |     B     |
        //      \     A |_____      |
        //       \            \     | 
        //        \____________\    | 
        //              |___________|   
        //
        //
        Vector3f[] boundaryA = new Vector3f[]{
            new Vector3f(-1, -2, 0),
            new Vector3f(-1, 2, 0),
            new Vector3f(2, 2, 0),
            new Vector3f(2, -2, 0)};
        Vector3f[] boundaryB = new Vector3f[]{
            new Vector3f(1, 0, 2),
            new Vector3f(1, 0, -2),
            new Vector3f(-2, 0, -2),
            new Vector3f(-2, 0, 2)};
        ArrayList<Vector3f> result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertTrue(result.size() == 2 && result.contains(new Vector3f(-1, 0, 0)) && result.contains(new Vector3f(1, 0, 0)));

        //Test boundaries with only one touching edge (Non coplanar)
        //
        //                     ___________
        //     _____________  |           |
        //     \            \ |     B     |
        //      \     A      \|           |
        //       \            \           | 
        //        \____________\          | 
        //                    |___________|   
        //
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-2, 0, -2),
            new Vector3f(-2, 0, 2),
            new Vector3f(0, 0, 2),
            new Vector3f(0, 0, -2)};
        boundaryB = new Vector3f[]{
            new Vector3f(2, -2, 0),
            new Vector3f(2, 2, 0),
            new Vector3f(0, 2, 0),
            new Vector3f(0, -2, 0)};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Test boundaries with only one touching vertices (Non coplanar)
        //     _____________       
        //     \            \
        //      \     A      \
        //       \            \
        //        \____________\___________
        //                     |           |
        //                     |     B     |
        //                     |           |
        //                     |___________|
        boundaryA = new Vector3f[]{
            new Vector3f(-2, 0, 0),
            new Vector3f(-2, 0, 2),
            new Vector3f(0, 0, 2),
            new Vector3f(0, 0, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(2, -2, 0),
            new Vector3f(2, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0, -2, 0)};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Testing boundaries that are separated (Non coplanar)
        //
        //                         ___________
        //     _____________      |           |
        //     \            \     |     B     |
        //      \     A      \    |           |
        //       \            \   |           | 
        //        \____________\  |           | 
        //                        |___________|   
        //
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-2, 0, -2),
            new Vector3f(-2, 0, 2),
            new Vector3f(-1, 0, 2),
            new Vector3f(-1, 0, -2)};
        boundaryB = new Vector3f[]{
            new Vector3f(2, -2, 0),
            new Vector3f(2, 2, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(1, -2, 0)};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Testing boundaries that are separated (Non coplanar)
        //
        //
        //
        //           _____
        //          |     |     
        //     _____|  B  |_ 
        //     \    |     | \   
        //      \   |_____|  \   
        //       \   A        \  
        //        \____________\ 
        //                       
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-2, 0, -2),
            new Vector3f(-2, 0, 2),
            new Vector3f(2, 0, 2),
            new Vector3f(2, 0, -2)};
        boundaryB = new Vector3f[]{
            new Vector3f(1, 0, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(-1, 2, 0),
            new Vector3f(-1, 0, 0)};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Test For one boundary inside another boundary (Non coplanar)
        //
        //         ______
        //        _\_____\_____
        //       |   ______    |
        //       |   \  B  \   |
        //       | A  \_____\  |
        //       |_____________|
        boundaryA = new Vector3f[]{
            new Vector3f(-2, -2, 0),
            new Vector3f(-2, 2, 0),
            new Vector3f(2, 2, 0),
            new Vector3f(2, -2, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(1, 0, -2),
            new Vector3f(1, 0, 2),
            new Vector3f(-1, 0, 2),
            new Vector3f(-1, 0, -2)};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertTrue(result.size() == 2 && result.contains(new Vector3f(1, 0, 0)) && result.contains(new Vector3f(-1, 0, 0)));

        //OverLapping Boundary (Coplanar)
        //     = = = = = = =
        //    ||           ||
        //    ||   A & B   ||
        //    ||           ||
        //    ||           ||
        //     = = = = = = =
        boundaryA = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, -1, 0),};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Boundary with only edge overlapping (Coplanar)
        //
        //               ___________
        //              |           |
        //  ____________|           |
        // |            |     A     |
        // |     B      |           |
        // |            |           |
        // |____________|___________|
        boundaryA = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, -1, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(0, -1, 0),
            new Vector3f(0, 2, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(1, -1, 0),};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Boundary with only edge overlapping (Coplanar)
        //
        //  ____________
        // |            |
        // |     A      |
        // |____________|___________
        //              |           |
        //              |     B     |
        //              |___________|
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0, -1, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(0, 0, 0),
            new Vector3f(0, 2, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(1, 0, 0),};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Boundary with only edge overlapping (Coplanar)
        //
        //  ____________
        // |            |
        // |     A      |
        // |____________|     ___________
        //                   |           |
        //                   |     B     |
        //                   |___________|
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-1, -1, 0),
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0, -1, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(2, 0, 0),
            new Vector3f(2, 2, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(1, 0, 0),};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);

        //Boundary with only edge overlapping (Coplanar)
        //
        //  ____________
        // |            |
        // |     A     _|_________
        // |__________|_|         |
        //            |     B     |
        //            |___________|
        //            
        //
        boundaryA = new Vector3f[]{
            new Vector3f(-2, -1, 0),
            new Vector3f(-2, 2, 0),
            new Vector3f(1, 2, 0),
            new Vector3f(1, -1, 0)};
        boundaryB = new Vector3f[]{
            new Vector3f(2, -2, 0),
            new Vector3f(2, 1, 0),
            new Vector3f(-1, 1, 0),
            new Vector3f(-1, -2, 0),};
        result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertNull(result);
    }

}
