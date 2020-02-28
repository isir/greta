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
package greta.core.animation.kinematics;

import greta.core.animation.math.Vector3d;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class JacobianIKSolverTest {

    public JacobianIKSolverTest() {
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
     * Test of solve method, of class JacobianIKSolver.
     */
    @Test
    public void testSolve() {
        System.out.println("solve");
        Vector3d target = new Vector3d(1,1,1);

        IKChain chain = new IKChain("chain");
        IKJoint j0 = new IKJoint(chain, -1, 3, "j0");
        chain.m_axis.get(j0.m_dims.get(0).m_idx).set(0,0,1);
        chain.m_axis.get(j0.m_dims.get(1).m_idx).set(0,1,0);
        chain.m_axis.get(j0.m_dims.get(2).m_idx).set(1,0,0);
        chain.m_localTranslations.get(j0.m_dims.get(0).m_idx).set(0,0,0);
        //chain.m_anglelimites.get(j0.m_dims.get(0).m_idx).set(0, 3.14);

        IKJoint j1 = new IKJoint(chain, 0, 1, "j1");
        chain.m_axis.get(j1.m_dims.get(0).m_idx).set(1,0,0);
        chain.m_anglelimites.get(j1.m_dims.get(0).m_idx).set(-3, 0);
        chain.m_localTranslations.get(j1.m_dims.get(0).m_idx).set(0,1,0);


        IKJoint j2 = new IKJoint(chain, 1, 3, "j2");
        chain.m_axis.get(j2.m_dims.get(0).m_idx).set(0,0,1);
        chain.m_axis.get(j2.m_dims.get(1).m_idx).set(0,1,0);
        chain.m_axis.get(j2.m_dims.get(2).m_idx).set(1,0,0);
        chain.m_localTranslations.get(j2.m_dims.get(0).m_idx).set(0,1,0);


//        chain._joints.add(j0);
//        j0._maxs.set(0, 0.4);
//        j1._maxs.set(0, 0.4);
//        chain._joints.add(j1);
//        chain._joints.add(j2);


        JacobianIKSolver instance = new JacobianIKSolver(chain);
        boolean r = instance.solve(target);
        boolean expResult = false;
//        boolean result = instance.solve(target);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
