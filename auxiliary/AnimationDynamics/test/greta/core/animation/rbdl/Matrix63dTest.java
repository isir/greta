/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.rbdl;

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.Matrix63d;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import org.apache.commons.math3.linear.RealMatrix;
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
public class Matrix63dTest {
    
    public Matrix63dTest() {
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
     * Test of set method, of class Matrix63d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        double v4 = 3.0;
        double v5 = 0.0;
        double v6 = 1.0;
        double v7 = 0.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 5.0;
        double v11 = 0.0;
        double v12 = 5.0;
        double v13 = 0.0;
        double v14 = 8.0;
        double v15 = 9.0;
        double v16 = 11.0;
        double v17 = 0.0;
        double v18 = 0.0;
        Matrix63d instance = new Matrix63d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("set test: " +instance);
    }

    /**
     * Test of toZero method, of class Matrix63d.
     */
    @Test
    public void testToZero() {
        System.out.println("toZero");
        Matrix63d instance = new Matrix63d();
        instance.toZero();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toZero test: " +instance);
    }

    /**
     * Test of copyData method, of class Matrix63d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        RealMatrix arv = null;
        Matrix63d instance = new Matrix63d();
        Matrix63d expResult = null;
        Matrix63d result = instance.copyData(arv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zero method, of class Matrix63d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        Matrix63d expResult = null;
        Matrix63d result = Matrix63d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transposeMultiple method, of class Matrix63d.
     */
    @Test
    public void testTransposeMultiple_SpatialVector6d() {
        System.out.println("transposeMultiple");
        SpatialVector6d v = null;
        Matrix63d instance = new Matrix63d();
        Vector3d expResult = null;
        Vector3d result = instance.transposeMultiple(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transposeMultiple method, of class Matrix63d.
     */
    @Test
    public void testTransposeMultiple_Matrix63d() {
        System.out.println("transposeMultiple");
        Matrix63d v = new Matrix63d();
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        double v4 = 3.0;
        double v5 = 0.0;
        double v6 = 1.0;
        double v7 = 0.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 5.0;
        double v11 = 0.0;
        double v12 = 5.0;
        double v13 = 0.0;
        double v14 = 8.0;
        double v15 = 9.0;
        double v16 = 11.0;
        double v17 = 0.0;
        double v18 = 0.0;
        v.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18);
        Matrix63d instance = new Matrix63d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18);
        Matrix3d result = instance.transposeMultiple(v);
        //assertEquals(expResult, result);
        System.out.println("transpose test: " +instance +" "+instance.transpose());
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiple method, of class Matrix63d.
     */
    @Test
    public void testMultiple() {
        System.out.println("multiple");
        Vector3d v = null;
        Matrix63d instance = new Matrix63d();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.multiple(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
