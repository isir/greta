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
 */

package vib.core.animation.optimization;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
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
public class GradientDescentSolverTest {
    
    public GradientDescentSolverTest() {
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
     * Test of solve method, of class GradientDescentSolver.
     */
    @Test
    public void testSolve() {
        System.out.println("solve");
        GradientDescentSolver instance = new GradientDescentSolver(new TestQuadraticFunction());
        BFGSSolver instance2 = new BFGSSolver(new TestQuadraticFunction());
        RealMatrix v = new Array2DRowRealMatrix(1, 1);
        v.setEntry(0, 0, 13.1231111);
        instance.setInitX(v);
        instance2.setInitX(v);
        RealMatrix expResult = new Array2DRowRealMatrix(1, 1);
        expResult.setEntry(0, 0, 0);
        RealMatrix result = instance.solve();
        RealMatrix result2 = instance2.solve();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
