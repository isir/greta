/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.rbdl;

import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jing Huang
 */
public class SpatialVector6dTest {
    
    public SpatialVector6dTest() {
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
     * Test of set method, of class SpatialVector6d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        double v4 = 0.0;
        double v5 = 0.0;
        SpatialVector6d instance = new SpatialVector6d();
        instance.set(v0, v1, v2, v3, v4, v5);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZero method, of class SpatialVector6d.
     */
    @Test
    public void testSetZero() {
        System.out.println("setZero");
        SpatialVector6d instance = new SpatialVector6d();
        instance.setZero();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zero method, of class SpatialVector6d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        SpatialVector6d expResult = null;
        SpatialVector6d result = SpatialVector6d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyData method, of class SpatialVector6d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        RealVector arv = null;
        SpatialVector6d instance = new SpatialVector6d();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.copyData(arv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUpper method, of class SpatialVector6d.
     */
    @Test
    public void testGetUpper() {
        System.out.println("getUpper");
        SpatialVector6d instance = new SpatialVector6d();
        Vector3d expResult = null;
        Vector3d result = instance.getUpper();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLower method, of class SpatialVector6d.
     */
    @Test
    public void testGetLower() {
        System.out.println("getLower");
        SpatialVector6d instance = new SpatialVector6d();
        Vector3d expResult = null;
        Vector3d result = instance.getLower();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crossM method, of class SpatialVector6d.
     */
    @Test
    public void testCrossM_0args() {
        System.out.println("crossM");
        SpatialVector6d instance = new SpatialVector6d();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.crossM();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crossM method, of class SpatialVector6d.
     */
    @Test
    public void testCrossM_SpatialVector6d_SpatialVector6d() {
        System.out.println("crossM");
        SpatialVector6d v1 = null;
        SpatialVector6d v2 = null;
        SpatialVector6d expResult = null;
        SpatialVector6d result = SpatialVector6d.crossM(v1, v2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crossF method, of class SpatialVector6d.
     */
    @Test
    public void testCrossF_0args() {
        System.out.println("crossF");
        SpatialVector6d instance = new SpatialVector6d();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.crossF();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crossF method, of class SpatialVector6d.
     */
    @Test
    public void testCrossF_SpatialVector6d_SpatialVector6d() {
        System.out.println("crossF");
        SpatialVector6d v1 = null;
        SpatialVector6d v2 = null;
        SpatialVector6d expResult = null;
        SpatialVector6d result = SpatialVector6d.crossF(v1, v2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isZero method, of class SpatialVector6d.
     */
    @Test
    public void testIsZero() {
        System.out.println("isZero");
        SpatialVector6d instance = new SpatialVector6d();
        boolean expResult = false;
        boolean result = instance.isZero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
