/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.rbdl;

import vib.core.animation.math.Vector4d;
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
public class Vector4dTest {
    
    public Vector4dTest() {
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
     * Test of set method, of class Vector4d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double w = 0.0;
        Vector4d instance = new Vector4d();
        instance.set(x, y, z, w);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyData method, of class Vector4d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        RealVector arv = null;
        Vector4d instance = new Vector4d();
        Vector4d expResult = null;
        Vector4d result = instance.copyData(arv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zero method, of class Vector4d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        Vector4d expResult = null;
        Vector4d result = Vector4d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
