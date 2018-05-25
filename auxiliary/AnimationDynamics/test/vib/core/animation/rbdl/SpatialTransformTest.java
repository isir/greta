/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.rbdl;

import vib.core.animation.math.SpatialVector6d;
import vib.core.animation.math.SpatialMatrix6d;
import vib.core.animation.math.Vector3d;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jing Huang
 */
public class SpatialTransformTest {
    
    public SpatialTransformTest() {
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

    /**
     * Test of apply method, of class SpatialTransform.
     */
    @Test
    public void testApply_SpatialVector6d() {
        System.out.println("apply");
        SpatialVector6d v_sp = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.apply(v_sp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of applyTranspose method, of class SpatialTransform.
     */
    @Test
    public void testApplyTranspose_SpatialVector6d() {
        System.out.println("applyTranspose");
        SpatialVector6d f_sp = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.applyTranspose(f_sp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of apply method, of class SpatialTransform.
     */
    @Test
    public void testApply_SpatialRigidBodyInertia() {
        System.out.println("apply");
        SpatialRigidBodyInertia rbi = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialRigidBodyInertia expResult = null;
        SpatialRigidBodyInertia result = instance.apply(rbi);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of applyTranspose method, of class SpatialTransform.
     */
    @Test
    public void testApplyTranspose_SpatialRigidBodyInertia() {
        System.out.println("applyTranspose");
        SpatialRigidBodyInertia rbi = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialRigidBodyInertia expResult = null;
        SpatialRigidBodyInertia result = instance.applyTranspose(rbi);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of applyAdjoint method, of class SpatialTransform.
     */
    @Test
    public void testApplyAdjoint() {
        System.out.println("applyAdjoint");
        SpatialVector6d f_sp = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.applyAdjoint(f_sp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toMatrix method, of class SpatialTransform.
     */
    @Test
    public void testToMatrix() {
        System.out.println("toMatrix");
        SpatialTransform instance = new SpatialTransform();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.toMatrix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toMatrixAdjoint method, of class SpatialTransform.
     */
    @Test
    public void testToMatrixAdjoint() {
        System.out.println("toMatrixAdjoint");
        SpatialTransform instance = new SpatialTransform();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.toMatrixAdjoint();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toMatrixTranspose method, of class SpatialTransform.
     */
    @Test
    public void testToMatrixTranspose() {
        System.out.println("toMatrixTranspose");
        SpatialTransform instance = new SpatialTransform();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.toMatrixTranspose();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of inverse method, of class SpatialTransform.
     */
    @Test
    public void testInverse() {
        System.out.println("inverse");
        SpatialTransform instance = new SpatialTransform();
        SpatialTransform expResult = null;
        SpatialTransform result = instance.inverse();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiple method, of class SpatialTransform.
     */
    @Test
    public void testMultiple() {
        System.out.println("multiple");
        SpatialTransform XT = null;
        SpatialTransform instance = new SpatialTransform();
        SpatialTransform expResult = null;
        SpatialTransform result = instance.multiple(XT);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multipleIntoSelf method, of class SpatialTransform.
     */
    @Test
    public void testMultipleIntoSelf() {
        System.out.println("multipleIntoSelf");
        SpatialTransform XT = null;
        SpatialTransform instance = new SpatialTransform();
        instance.multipleIntoSelf(XT);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SpatialTransform.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SpatialTransform instance = new SpatialTransform();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rot method, of class SpatialTransform.
     */
    @Test
    public void testRot() {
        System.out.println("rot");
        double angle_rad = 0.0;
        Vector3d axis = null;
        SpatialTransform expResult = null;
        SpatialTransform result = SpatialTransform.rot(angle_rad, axis);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rotX method, of class SpatialTransform.
     */
    @Test
    public void testRotX() {
        System.out.println("rotX");
        double angle_rad = 0.0;
        SpatialTransform expResult = null;
        SpatialTransform result = SpatialTransform.rotX(angle_rad);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rotY method, of class SpatialTransform.
     */
    @Test
    public void testRotY() {
        System.out.println("rotY");
        double angle_rad = 0.0;
        SpatialTransform expResult = null;
        SpatialTransform result = SpatialTransform.rotY(angle_rad);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rotZ method, of class SpatialTransform.
     */
    @Test
    public void testRotZ() {
        System.out.println("rotZ");
        double angle_rad = 0.0;
        SpatialTransform expResult = null;
        SpatialTransform result = SpatialTransform.rotZ(angle_rad);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of translate method, of class SpatialTransform.
     */
    @Test
    public void testTranslate() {
        System.out.println("translate");
        Vector3d r = null;
        SpatialTransform expResult = null;
        SpatialTransform result = SpatialTransform.translate(r);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
