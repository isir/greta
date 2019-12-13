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

import greta.core.animation.math.Matrix3d;
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
