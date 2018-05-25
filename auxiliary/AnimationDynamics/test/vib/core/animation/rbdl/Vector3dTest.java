/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.rbdl;

import vib.core.animation.math.Matrix3d;
import vib.core.animation.math.Vector3d;
import org.apache.commons.math3.linear.RealVector;
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
public class Vector3dTest {
    
    public Vector3dTest() {
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
     * Test of set method, of class Vector3d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Vector3d instance = new Vector3d();
        instance.set(x, y, z);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toCrossMatrix method, of class Vector3d.
     */
    @Test
    public void testToCrossMatrix() {
        System.out.println("toCrossMatrix");
        Vector3d instance = new Vector3d();
        Matrix3d expResult = null;
        Matrix3d result = instance.toCrossMatrix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cross method, of class Vector3d.
     */
    @Test
    public void testCross() {
        System.out.println("cross");
        Vector3d v = null;
        Vector3d instance = new Vector3d();
        Vector3d expResult = null;
        Vector3d result = instance.cross(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyData method, of class Vector3d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        RealVector arv = null;
        Vector3d instance = new Vector3d();
        Vector3d expResult = null;
        Vector3d result = instance.copyData(arv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of normalize method, of class Vector3d.
     */
    @Test
    public void testNormalize() {
        System.out.println("normalize");
        Vector3d instance = new Vector3d();
        instance.normalize();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zero method, of class Vector3d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        Vector3d expResult = null;
        Vector3d result = Vector3d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
