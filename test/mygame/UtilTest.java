/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    public void assertVector3f(Vector3f vectorA, Vector3f vectorB){
        Assert.assertEquals(vectorA.distance(vectorB), 0.0f, 0.000001);
    }


    /**
     * Test of rotatePoint method, of class Util.
     */
    @Test
    public void testRotatePoint() {
        System.out.println("rotatePoint");
        Vector3f point = new Vector3f(0,1,0);
        Vector3f axis1 = new Vector3f(5,0,0);
        Vector3f axis2 = new Vector3f(-5,0,0);
        float pi = FastMath.PI;
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -0.25f),new Vector3f(0,FastMath.cos(pi*0.25f),FastMath.sin(pi * -0.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -0.50f),new Vector3f(0,FastMath.cos(pi*0.50f),FastMath.sin(pi * -0.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -0.75f),new Vector3f(0,FastMath.cos(pi*0.75f),FastMath.sin(pi * -0.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -1.00f),new Vector3f(0,FastMath.cos(pi*1.00f),FastMath.sin(pi * -1.00f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -1.25f),new Vector3f(0,FastMath.cos(pi*1.25f),FastMath.sin(pi * -1.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -1.50f),new Vector3f(0,FastMath.cos(pi*1.50f),FastMath.sin(pi * -1.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -1.75f),new Vector3f(0,FastMath.cos(pi*1.75f),FastMath.sin(pi * -1.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* -2.00f),new Vector3f(0,FastMath.cos(pi*2.00f),FastMath.sin(pi * -2.00f)));
        
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi*0f),point);
        
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 0.25f),new Vector3f(0,FastMath.cos(pi*0.25f),FastMath.sin(pi * 0.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 0.50f),new Vector3f(0,FastMath.cos(pi*0.50f),FastMath.sin(pi * 0.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 0.75f),new Vector3f(0,FastMath.cos(pi*0.75f),FastMath.sin(pi * 0.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 1.00f),new Vector3f(0,FastMath.cos(pi*1.00f),FastMath.sin(pi * 1.00f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 1.25f),new Vector3f(0,FastMath.cos(pi*1.25f),FastMath.sin(pi * 1.25f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 1.50f),new Vector3f(0,FastMath.cos(pi*1.50f),FastMath.sin(pi * 1.50f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 1.75f),new Vector3f(0,FastMath.cos(pi*1.75f),FastMath.sin(pi * 1.75f)));
        assertVector3f(Util.rotatePoint(point, axis1, axis2, pi* 2.00f),new Vector3f(0,FastMath.cos(pi*2.00f),FastMath.sin(pi * 2.00f)));
        

    }

    /**
     * Test of isBetween method, of class Util.
     */
    @Test
    public void testIsBetween() {
        System.out.println("isBetween");
        Vector3f left = new Vector3f(-5, 0, 0);
        Vector3f right = new Vector3f(5, 0, 0);
        assertTrue(Util.isBetween(left, left, right));
        assertTrue(Util.isBetween(left, right, right));
        assertTrue(Util.isBetween(left, new Vector3f(0, 0, 0), right));
        
        assertFalse(Util.isBetween(left, new Vector3f(-6, 0, 0), right));
        assertFalse(Util.isBetween(left, new Vector3f(6, 0, 0) , right));
        assertFalse(Util.isBetween(left, new Vector3f(0, 1, 0) , right));
        
        
    }

    /**
     * Test of inLine method, of class Util.
     */
    @Test
    public void testInLine() {
        System.out.println("inLine");
        Vector3f left = new Vector3f(-5, 0, 0);
        Vector3f right = new Vector3f(5, 0, 0);
        assertTrue(Util.inLine(left, left, right));
        assertTrue(Util.inLine(left, right, right));
        assertTrue(Util.inLine(left, new Vector3f(0, 0, 0), right));
        assertTrue(Util.inLine(left, new Vector3f(6, 0, 0), right));
        assertTrue(Util.inLine(left, new Vector3f(-6, 0, 0), right));
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of lineToPointTranslation method, of class Util.
     */
    @Test
    public void testLineToPointTranslation() {
        System.out.println("lineToPointTranslation");
        Vector3f linePoint = new Vector3f();
        Vector3f lineDirrection = new Vector3f(1,0,0);
        Vector3f target = linePoint;
        assertVector3f(Util.lineToPointTranslation(linePoint, lineDirrection, target), Vector3f.ZERO);
        target = new Vector3f(0, 3, 0);
        assertVector3f(Util.lineToPointTranslation(linePoint, lineDirrection, target), target);
        
    }

    /**
     * Test of closestPointOnLine method, of class Util.
     */
    @Test
    public void testClosestPointOnLine() {
        System.out.println("closestPointOnLine");
        Vector3f linePoint = new Vector3f(0,0,0);
        Vector3f lineDirrection = new Vector3f(1,0,0);
        Vector3f target = linePoint;
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), target);
        target = new Vector3f(0, 1, 0);
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), new Vector3f(0,0,0));
        target = new Vector3f(-1, 1, 0);
        assertVector3f(Util.closestPointOnLine(linePoint, lineDirrection, target), new Vector3f(-1,0,0));
    }

    /**
     * Test of closestPointToDirrection method, of class Util.
     */
    @Test
    public void testClosestPointToDirrection() {
        System.out.println("closestPointToDirrection");
        Vector3f[] boundary = new Vector3f[]{
                                                new Vector3f(-1, -1, 0),
                                                new Vector3f(-1,  1, 0),
                                                new Vector3f( 1,  1, 0),
                                                new Vector3f( 1, -1, 0),
                                            };
        
        assertVector3f(Util.closestPointToDirrection(new Vector3f(  0,  1, 0), boundary), new Vector3f( -1,  1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f(  0, -1, 0), boundary), new Vector3f( -1, -1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f(  1,  0, 0), boundary), new Vector3f(  1,  1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f( -1,  0, 0), boundary), new Vector3f( -1, -1, 0));
        
        assertVector3f(Util.closestPointToDirrection(new Vector3f( -1, -1, 0), boundary), new Vector3f(-1, -1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f(  1, -1, 0), boundary), new Vector3f( 1, -1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f(  1,  1, 0), boundary), new Vector3f( 1,  1, 0));
        assertVector3f(Util.closestPointToDirrection(new Vector3f( -1,  1, 0), boundary), new Vector3f(-1,  1, 0));
        // TODO review the generated test code and remove the default call to fail
    }

    /**
     * Test of lineTouchesBoundary method, of class Util.
     */
    @Test
    public void testLineTouchesBoundary() {
        System.out.println("lineTouchesBoundary");
        Vector3f[] boundary = new Vector3f[]{
                                                new Vector3f(-1, -1, 0),
                                                new Vector3f(-1,  1, 0),
                                                new Vector3f( 1,  1, 0),
                                                new Vector3f( 1, -1, 0),
                                            };
        
        assertTrue(Util.lineTouchesBoundary(new Vector3f(5, 0, 0), new Vector3f(-1, 0, 0), boundary));
        assertTrue(Util.lineTouchesBoundary(new Vector3f(5, 1, 0), new Vector3f(-1, 0, 0), boundary));
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 0, 0), new Vector3f(-1, 0, 0), boundary));
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), boundary));
        assertTrue(Util.lineTouchesBoundary(new Vector3f(0, 0, 0), new Vector3f(-1, -1, 0), boundary));
        
    }

    /**
     * Test of inBoundary method, of class Util.
     */
    @Test
    public void testInBoundary() {
        System.out.println("inBoundary");
        Vector3f[] boundary = new Vector3f[]{
                                                new Vector3f(-1, -1, 0),
                                                new Vector3f(-1,  1, 0),
                                                new Vector3f( 1,  1, 0),
                                                new Vector3f( 1, -1, 0),
                                            };
        
        
        assertTrue(Util.inBoundary(new Vector3f(0,0,0), boundary));
        assertTrue(Util.inBoundary(new Vector3f(0,1,0), boundary));
        assertTrue(Util.inBoundary(new Vector3f(1,1,0), boundary));
        assertFalse(Util.inBoundary(new Vector3f(2,0,0), boundary));
    }

    /**
     * Test of castLineOnBoundary method, of class Util.
     */
    @Test
    public void testCastLineOnBoundary() {
        System.out.println("castLineOnBoundary");
        Vector3f[] boundary = new Vector3f[]{
                                                new Vector3f(-1, -1, 0),
                                                new Vector3f(-1,  1, 0),
                                                new Vector3f( 1,  1, 0),
                                                new Vector3f( 1, -1, 0),
                                            };
        assertVector3f(Util.castLineOnBoundary(new Vector3f(2, 0, 0), new Vector3f(), boundary), Vector3f.ZERO);
    }

    /**
     * Test of lineBoundaryIntersections method, of class Util.
     */
    @Test
    public void testLineBoundaryIntersections() {
        System.out.println("lineBoundaryIntersections");
        Vector3f point = null;
        Vector3f dir = null;
        Vector3f[] boundary = null;
        Vector3f[] expResult = null;
        Vector3f[] result = Util.lineBoundaryIntersections(point, dir, boundary);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of segmentIntesection method, of class Util.
     */
    @Test
    public void testSegmentIntesection() {
        System.out.println("segmentIntesection");
        Vector3f point1 = null;
        Vector3f point2 = null;
        Vector3f point3 = null;
        Vector3f point4 = null;
        Vector3f expResult = null;
        Vector3f result = Util.segmentIntesection(point1, point2, point3, point4);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of lineIntersection method, of class Util.
     */
    @Test
    public void testLineIntersection() {
        System.out.println("lineIntersection");
        Vector3f point1 = null;
        Vector3f point2 = null;
        Vector3f point3 = null;
        Vector3f point4 = null;
        Vector3f expResult = null;
        Vector3f result = Util.lineIntersection(point1, point2, point3, point4);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of linePlaneIntersection method, of class Util.
     */
    @Test
    public void testLinePlaneIntersection() {
        System.out.println("linePlaneIntersection");
        Vector3f linePoint = null;
        Vector3f lineDir = null;
        Vector3f planePoint = null;
        Vector3f planeNormal = null;
        Vector3f expResult = null;
        Vector3f result = Util.linePlaneIntersection(linePoint, lineDir, planePoint, planeNormal);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of boundboundIntersect method, of class Util.
     */
    @Test
    public void testBoundboundIntersect() {
        System.out.println("boundboundIntersect");
        Vector3f[] boundaryA = null;
        Vector3f[] boundaryB = null;
        ArrayList<Vector3f> expResult = null;
        ArrayList<Vector3f> result = Util.boundboundIntersect(boundaryA, boundaryB);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toArray method, of class Util.
     */
    @Test
    public void testToArray() {
        System.out.println("toArray");
        ArrayList<Vector3f> arrayList = null;
        Vector3f[] expResult = null;
        Vector3f[] result = Util.toArray(arrayList);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeMesh method, of class Util.
     */
    @Test
    public void testMakeMesh() {
        System.out.println("makeMesh");
        Vector3f[] vertices = null;
        Mesh expResult = null;
        Mesh result = Util.makeMesh(vertices);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
