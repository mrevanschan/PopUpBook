/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author FatE
 */
public class PageTree {
    private PageTree prev;
    private Geometry geom;
    private VertexBuffer mesh;
    private Vector3f[] translatedVertices;
    private HashMap<PageTree,Vector3f[]> joint = new HashMap<>();
    private ArrayList<PageTree> next;
    private final Vector3f axis1;
    private final Vector3f axis2;
    private final Vector3f translatedAxis1;
    private final Vector3f translatedAxis2;
    
    private final float angle;
    
    public PageTree(PageTree prev,Geometry geom, VertexBuffer original,Vector3f axis1, Vector3f axis2, float angle){
        this.prev = prev;
        
        
        this.geom = geom;
        this.mesh = original.clone();
        this.translatedVertices = BufferUtils.getVector3Array((FloatBuffer)mesh.clone().getData());
        next = new ArrayList();
        this.axis1 = axis1;
        this.axis2 = axis2;
        
        if(prev != null){
            prev.addNext(this);
            translatedAxis1 = axis1.clone();
            translatedAxis2 = axis2.clone(); 
        }else{
            translatedAxis1 = null;
            translatedAxis2 = null;
        }
        this.angle = angle;
    }
    public static void joint(PageTree pageA, PageTree pageB,Vector3f[] axis){
        pageA.addJoint(pageB,axis);
        pageB.addJoint(pageA,axis);
    }
    public void addNext(PageTree next){
        this.next.add(next);
    }
    public Vector3f[] getJointAxis(PageTree target){
        return joint.get(target); 
    }
    public int jointCount(){
        return joint.size();
    }
    public Geometry getGeometry(){
        return geom;
    }
    public void addJoint(PageTree joint,Vector3f[] axis){
        this.joint.put(joint, axis);
    }
    public PageTree getPrevTreeNode(){
        return prev;
    }
    public Geometry getPrevGeometry(){
        return prev.getGeometry();
    }
    public ArrayList<PageTree> getNextTreeNodes(){
        return next;
    }
    
    public boolean isNeighbour(PageTree target,int distance){
        return joint.keySet().contains(target);
    }
    
    public void rotateBuffer( Vector3f axisA, Vector3f axisB, float radian){                
        System.out.println("Original:" + translatedAxis1 + translatedAxis2);
        translatedAxis1.set(rotatePoint(translatedAxis1, axisA, axisB, radian));
        translatedAxis2.set(rotatePoint(translatedAxis2, axisA, axisB, radian));
        System.out.println("After:" + translatedAxis1 + translatedAxis2);
        
        for(Vector3f point : translatedVertices){
            point.set(rotatePoint(point, axisA, axisB, radian));
        }
          
        
        for(PageTree childPage: next){
            childPage.rotateBuffer(axisA, axisB, radian);
        }
    }
    
    public void updateMesh(){
        geom.getMesh().getBuffer(VertexBuffer.Type.Position).updateData(BufferUtils.createFloatBuffer(translatedVertices));
        geom.getMesh().createCollisionData();
        for(PageTree child: next){
            child.updateMesh();
        }
    }
    
    public void updateFoldPercent(float percent){
        reset();
        helper(percent);
        updateMesh();
    }
    public void helper(float percent){
        if(prev != null){
            rotateBuffer(translatedAxis1, translatedAxis2, angle*percent);
        }
        for(PageTree nextPage: next){
            nextPage.helper(percent);
        }
    }
    
    public Vector3f rotatePoint(Vector3f point, Vector3f axis1, Vector3f axis2, float radian){
        Plane plane = new Plane();
        Vector3f newPoint = null;
        Vector3f crossVector = axis1.subtract(axis2).cross(point.subtract(axis2)).normalize();
        if(crossVector.distance(Vector3f.ZERO) < FastMath.FLT_EPSILON){
            return point;
        }
        plane.setPlanePoints(axis2, axis1, axis2.add(crossVector));
        if(FastMath.abs(FastMath.abs(radian)-FastMath.PI) < FastMath.FLT_EPSILON){
            //System.out.print("case1");
            newPoint = plane.reflect(point, null);
        }else if(FastMath.abs(FastMath.abs(radian) - FastMath.HALF_PI) < FastMath.FLT_EPSILON){
            //System.out.print("case2");
            Vector3f base = plane.getClosestPoint(point);
            if(radian > 0){
                newPoint = base.add(crossVector.normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            }else{
                newPoint = base.add(crossVector.normalize().mult(-1 * FastMath.abs(plane.pseudoDistance(point))));
            }
        } else{
            if(FastMath.abs(radian) < FastMath.HALF_PI){
                //System.out.print("case3");
                Vector3f base = plane.getClosestPoint(point);
                newPoint = base.add(point.subtract(base).normalize()).add(crossVector.normalize().mult(FastMath.tan(radian)));
                newPoint = base.add(newPoint.subtract(base).normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            }else{
                //System.out.print("case4");
                Vector3f base = plane.getClosestPoint(point);
                newPoint = base.add(base.subtract(point).normalize()).add(crossVector.normalize().mult(-FastMath.tan(radian-FastMath.PI)));
                newPoint = base.add(newPoint.subtract(base).normalize().mult(FastMath.abs(plane.pseudoDistance(point))));
            }
            
        }
        return newPoint;
    }
    public void reset(){
        geom.getMesh().getBuffer(VertexBuffer.Type.Position).updateData(mesh.getData());
        if(prev != null){
           translatedVertices = BufferUtils.getVector3Array((FloatBuffer)mesh.clone().getData());
           translatedAxis1.set(axis1);
           translatedAxis2.set(axis2); 
        }
        for(PageTree child: next){
            child.reset();
        }
    }
}
