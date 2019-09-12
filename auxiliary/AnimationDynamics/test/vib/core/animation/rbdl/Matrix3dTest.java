/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.rbdl;

import vib.core.animation.math.Matrix3d;
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
public class Matrix3dTest {
    
    public Matrix3dTest() {
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
     * Test of set method, of class Matrix3d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 10.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 1.10;
        double v7 = 2.0;
        double v8 = 3.0;
        double v9 = 0.0;
        Matrix3d instance = new Matrix3d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9);
        System.out.println("set test: " +instance);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of toIdentity method, of class Matrix3d.
     */
    @Test
    public void testToIdentity() {
        System.out.println("toIdentity");
        Matrix3d instance = new Matrix3d();
        instance.toIdentity();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toIdentity test: " +instance);
    }

    /**
     * Test of toMIdentity method, of class Matrix3d.
     */
    @Test
    public void testToMIdentity() {
        System.out.println("toMIdentity");
        double m = 11.0;
        Matrix3d instance = new Matrix3d();
        instance.toMIdentity(m);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toMIdentity test: " +instance);
    }

    /**
     * Test of toZero method, of class Matrix3d.
     */
    @Test
    public void testToZero() {
        System.out.println("toZero");
        Matrix3d instance = new Matrix3d();
        instance.toZero();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toMIdentity test: " +instance);
    }

    /**
     * Test of zero method, of class Matrix3d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");
        Matrix3d expResult = new Matrix3d();
        expResult.toZero();
        Matrix3d result = Matrix3d.zero();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("zero test: " +result);
    }

    /**
     * Test of identity method, of class Matrix3d.
     */
    @Test
    public void testIdentity() {
        System.out.println("identity");
        Matrix3d expResult = new Matrix3d();
        expResult.toIdentity();
        Matrix3d result = Matrix3d.identity();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("identity test: " +result);
    }

    /**
     * Test of copyData method, of class Matrix3d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 3.0;
        double v4 = 0.0;
        double v5 = 2.0;
        double v6 = 0.0;
        double v7 = 1.0;
        double v8 = 0.0;
        double v9 = 9.0;
        Matrix3d instance = new Matrix3d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9);

        Matrix3d result = instance.copyData(instance);

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("copyData test: " + instance + result);
    }

    /**
     * Test of transpose method, of class Matrix3d.
     */
    @Test
    public void testTranspose() {
        System.out.println("transpose");
        double v1 = 0.0;
        double v2 = 3.4;
        double v3 = 3.0;
        double v4 = 0.7;
        double v5 = 2.0;
        double v6 = 0.0;
        double v7 = 1.0;
        double v8 = 0.5;
        double v9 = 9.0;
        Matrix3d instance = new Matrix3d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9);
  
        Matrix3d result = instance.transpose();

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("transpose test: " + instance + result);
    }

    /**
     * Test of multiple method, of class Matrix3d.
     */
    @Test
    public void testMultiple() {
        System.out.println("multiple");
        Vector3d v = new Vector3d(1,7,1.6);
        double v1 = 0.0;
        double v2 = 3.4;
        double v3 = 3.0;
        double v4 = 0.7;
        double v5 = 2.0;
        double v6 = 0.0;
        double v7 = 1.0;
        double v8 = 0.5;
        double v9 = 9.0;
        Matrix3d instance = new Matrix3d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9);
 
        Vector3d result = instance.multiple(v);
        
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("multiple test: " + result);
    }

    /**
     * Test of rotX method, of class Matrix3d.
     */
    @Test
    public void testRotX() {
        System.out.println("rotX");
        double angle_rad = 10.0;
 
        Matrix3d result = Matrix3d.rotX(angle_rad);
        
        // TODO review the generated test code and remove the default call to fail.
       System.out.println("rotX test: "+ angle_rad + " "+ result);
    }

    /**
     * Test of rotY method, of class Matrix3d.
     */
    @Test
    public void testRotY() {
        System.out.println("rotY");
        double angle_rad = 8.0;

        Matrix3d result = Matrix3d.rotY(angle_rad);
 
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("rotY test: "+ angle_rad + " "+ result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of rotZ method, of class Matrix3d.
     */
    @Test
    public void testRotZ() {
        System.out.println("rotZ");
        double angle_rad = 6.0;

        Matrix3d result = Matrix3d.rotZ(angle_rad);

        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        System.out.println("rotZ test: "+ angle_rad + " "+ result);
    }
    
}
