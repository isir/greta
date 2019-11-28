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
package vib.core.animation.optimization;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Huang
 */
public class LinearRegressionTest {
    
    public LinearRegressionTest() {
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
     * Test of converge method, of class LinearRegression.
     */
    @Test
    public void testConverge() {
        System.out.println("converge");
        LinearRegression instance = null;
        double expResult = 0.0;
        double result = instance.converge();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of solve method, of class LinearRegression.
     */
    @Test
    public void testSolve() {
        System.out.println("solve");
        LinearRegression instance = null;
        RealMatrix expResult = null;
        RealMatrix result = instance.solve();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDirlength method, of class LinearRegression.
     */
    @Test
    public void testGetDirlength() {
        System.out.println("getDirlength");
        LinearRegression instance = null;
        double expResult = 0.0;
        double result = instance.getDirlength();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDirlength method, of class LinearRegression.
     */
    @Test
    public void testSetDirlength() {
        System.out.println("setDirlength");
        double dirlength = 0.0;
        LinearRegression instance = null;
        instance.setDirlength(dirlength);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPrecsion method, of class LinearRegression.
     */
    @Test
    public void testGetPrecsion() {
        System.out.println("getPrecsion");
        LinearRegression instance = null;
        double expResult = 0.0;
        double result = instance.getPrecsion();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPrecsion method, of class LinearRegression.
     */
    @Test
    public void testSetPrecsion() {
        System.out.println("setPrecsion");
        double precsion = 0.0;
        LinearRegression instance = null;
        instance.setPrecsion(precsion);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxStep method, of class LinearRegression.
     */
    @Test
    public void testGetMaxStep() {
        System.out.println("getMaxStep");
        LinearRegression instance = null;
        int expResult = 0;
        int result = instance.getMaxStep();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMaxStep method, of class LinearRegression.
     */
    @Test
    public void testSetMaxStep() {
        System.out.println("setMaxStep");
        int maxStep = 0;
        LinearRegression instance = null;
        instance.setMaxStep(maxStep);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
