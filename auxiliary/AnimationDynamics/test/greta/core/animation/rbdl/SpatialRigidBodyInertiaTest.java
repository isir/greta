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
public class SpatialRigidBodyInertiaTest {
    
    public SpatialRigidBodyInertiaTest() {
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
     * Test of multiple method, of class SpatialRigidBodyInertia.
     */
    @Test
    public void testMultiple() {
        System.out.println("multiple");
        SpatialVector6d mv = null;
        SpatialRigidBodyInertia instance = new SpatialRigidBodyInertia();
        SpatialVector6d expResult = null;
        SpatialVector6d result = instance.multiple(mv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class SpatialRigidBodyInertia.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        SpatialRigidBodyInertia srbi = null;
        SpatialRigidBodyInertia instance = new SpatialRigidBodyInertia();
        SpatialRigidBodyInertia expResult = null;
        SpatialRigidBodyInertia result = instance.add(srbi);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createFromMatrix method, of class SpatialRigidBodyInertia.
     */
    @Test
    public void testCreateFromMatrix() {
        System.out.println("createFromMatrix");
        SpatialMatrix6d Ic = null;
        SpatialRigidBodyInertia instance = new SpatialRigidBodyInertia();
        instance.createFromMatrix(Ic);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toMatrix method, of class SpatialRigidBodyInertia.
     */
    @Test
    public void testToMatrix() {
        System.out.println("toMatrix");
        SpatialRigidBodyInertia instance = new SpatialRigidBodyInertia();
        SpatialMatrix6d expResult = null;
        SpatialMatrix6d result = instance.toMatrix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SpatialRigidBodyInertia.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SpatialRigidBodyInertia instance = new SpatialRigidBodyInertia();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
