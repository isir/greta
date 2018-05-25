/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
