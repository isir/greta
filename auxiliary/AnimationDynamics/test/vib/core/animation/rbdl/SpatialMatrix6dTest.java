/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.rbdl;

import vib.core.animation.math.SpatialVector6d;
import vib.core.animation.math.SpatialMatrix6d;
import vib.core.animation.math.Matrix3d;
import vib.core.animation.math.Matrix63d;
import vib.core.animation.math.Vector3d;
import org.apache.commons.math3.linear.RealMatrix;
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
public class SpatialMatrix6dTest {
    
    public SpatialMatrix6dTest() {
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
     * Test of set method, of class SpatialMatrix6d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 0.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 0.0;
        double v11 = 0.0;
        double v12 = 0.0;
        double v13 = 0.0;
        double v14 = 0.0;
        double v15 = 0.0;
        double v16 = 0.0;
        double v17 = 0.0;
        double v18 = 0.0;
        double v19 = 0.0;
        double v20 = 0.0;
        double v21 = 0.0;
        double v22 = 0.0;
        double v23 = 0.0;
        double v24 = 0.0;
        double v25 = 0.0;
        double v26 = 0.0;
        double v27 = 0.0;
        double v28 = 0.0;
        double v29 = 0.0;
        double v30 = 0.0;
        double v31 = 0.0;
        double v32 = 0.0;
        double v33 = 0.0;
        double v34 = 0.0;
        double v35 = 0.0;
        double v36 = 0.0;
        SpatialMatrix6d instance = new SpatialMatrix6d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zero method, of class SpatialMatrix6d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = SpatialMatrix6d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of identity method, of class SpatialMatrix6d.
     */
    @Test
    public void testIdentity() {
        System.out.println("identity");
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = SpatialMatrix6d.identity();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyData method, of class SpatialMatrix6d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        RealMatrix arv = null;
        SpatialMatrix6d instance = new SpatialMatrix6d();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.copyData(arv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRotation method, of class SpatialMatrix6d.
     */
    @Test
    public void testGetRotation() {
        System.out.println("getRotation");
        SpatialMatrix6d instance = new SpatialMatrix6d();
        Matrix3d expResult = null;
        Matrix3d result = instance.getRotation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTranslation method, of class SpatialMatrix6d.
     */
    @Test
    public void testGetTranslation() {
        System.out.println("getTranslation");
        SpatialMatrix6d instance = new SpatialMatrix6d();
        Vector3d expResult = null;
        Vector3d result = instance.getTranslation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of spatial_adjoint method, of class SpatialMatrix6d.
     */
    @Test
    public void testSpatial_adjoint() {
        System.out.println("spatial_adjoint");
        SpatialMatrix6d m = null;
        SpatialMatrix6d instance = new SpatialMatrix6d();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.spatial_adjoint(m);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sptial_inverse method, of class SpatialMatrix6d.
     */
    @Test
    public void testSptial_inverse() {
        System.out.println("sptial_inverse");
        SpatialMatrix6d instance = new SpatialMatrix6d();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.sptial_inverse();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiple method, of class SpatialMatrix6d.
     */
    @Test
    public void testMultiple_SpatialVector6d() {
        System.out.println("multiple");
        SpatialVector6d v = null;
        SpatialMatrix6d instance = new SpatialMatrix6d();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.multiple(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiple method, of class SpatialMatrix6d.
     */
    @Test
    public void testMultiple_Matrix63d() {
        System.out.println("multiple");
        Matrix63d v = null;
        SpatialMatrix6d instance = new SpatialMatrix6d();
        Matrix63d expResult = null;
        Matrix63d result = instance.multiple(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
