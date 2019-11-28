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
package vib.core.animation.rbdl;

import vib.core.animation.math.SpatialVector6d;
import vib.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
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
public class DynamicsTest {

    DModel model = new DModel();

    public DynamicsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        System.out.println("setUp");

        DBody body = new DBody(1, new Vector3d(1, 2, 0), new Vector3d(1, 1, 1));
        DJoint joint = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        model.addBody(0, SpatialTransform.translate(new Vector3d()), joint, body, "testbody1");
        
        DBody body1 = new DBody(1, new Vector3d(1, 2, 0), new Vector3d(1, 1, 1));
        DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        model.addBody(1, SpatialTransform.translate(new Vector3d(2,4,0)), joint1, body1, "testbody2");
    }

    @After
    public void tearDown() {
        System.out.println("tearDown");
    }

    /**
     * Test of forwardDynamics method, of class Dynamics.
     */
    @Test
    public void testForwardDynamics() {
        System.out.println("forwardDynamics");
        ArrayRealVector q = new ArrayRealVector(2);
        ArrayRealVector qDot = new ArrayRealVector(2);
        ArrayRealVector out_qDDot = new ArrayRealVector(2);
        
        ArrayRealVector q2 = new ArrayRealVector(2);
        ArrayRealVector qDot2 = new ArrayRealVector(2);
        ArrayRealVector out_qDDot2 = new ArrayRealVector(2);
        
        ArrayRealVector tau = new ArrayRealVector(2);
        ArrayList<SpatialVector6d> f_ext = new ArrayList<SpatialVector6d>();
        double dt = 0.03;
        for (int i = 0; i < 3000; ++i) {
           
            out_qDDot.set(0);
            out_qDDot2.set(0);
            Dynamics.forwardDynamics(model, q, qDot, tau, out_qDDot, f_ext);
            Dynamics.forwardDynamicsLagrangian(model, q2, qDot2, tau, out_qDDot2, f_ext);
            
            qDot = qDot.add(out_qDDot.mapMultiply(dt));
            q = q.add(qDot.mapMultiply(dt));
            
            qDot2 = qDot2.add(out_qDDot2.mapMultiply(dt));
            q2 = q2.add(qDot2.mapMultiply(dt));
            
            if(i%10 == 0){ 
                System.out.println("<------------------");
                System.out.println("acce: " + out_qDDot);
                System.out.println("acce2: " + out_qDDot2);
                System.out.println("vit: " + qDot); System.out.println("vit2: " + qDot2);
                System.out.println("rotation: " + q);System.out.println("rotation2: " + q2);
                System.out.println("------------------>");
            }
        }
        System.out.println("accelaration: " + out_qDDot);
        System.out.println("forwardDynamics end");

    }

    /**
     * Test of forwardDynamicsLagrangian method, of class Dynamics.
     */
    @Test
    public void testForwardDynamicsLagrangian() {
        System.out.println("forwardDynamicsLagrangian");
//        ArrayRealVector q = new ArrayRealVector(2);
//        ArrayRealVector qDot = new ArrayRealVector(2);
//        ArrayRealVector out_qDDot = new ArrayRealVector(2);
//        ArrayRealVector tau = new ArrayRealVector(2);
//        ArrayList<SpatialVector6d> f_ext = new ArrayList<SpatialVector6d>();
//        double dt = 0.03;
//        for (int i = 0; i < 3000; ++i) {
//            out_qDDot.set(0);
//            Dynamics.forwardDynamicsLagrangian(model, q, qDot, tau, out_qDDot, f_ext);
//            
//            qDot = qDot.add(out_qDDot.mapMultiply(dt));
//            q = q.add(qDot.mapMultiply(dt));
//            if(i%100 == 0){ 
//                System.out.println("<------------------");
//                System.out.println("acce: " + out_qDDot);System.out.println("vit: " + qDot);
//                System.out.println("rotation: " + q);
//                System.out.println("------------------>");
//            }
//        }
//        
 //       System.out.println("accelaration: " + out_qDDot);
        System.out.println("forwardDynamicsLagrangian end");
    }

    /**
     * Test of inverseDynamics method, of class Dynamics.
     */
    @Test
    public void testInverseDynamics() {
        System.out.println("inverseDynamics");
//        ArrayRealVector q = new ArrayRealVector(2);
//        q.setEntry(0, 3);
//        q.setEntry(1, 1.1);
//        ArrayRealVector qDot = new ArrayRealVector(2);
//        qDot.setEntry(0, 0.1);
//        qDot.setEntry(1, 0.21);
//        ArrayRealVector qDDot = new ArrayRealVector(2);
//        qDDot.setEntry(0, 0.3);
//        qDDot.setEntry(1, 0.1);
//        ArrayRealVector out_tau = new ArrayRealVector(2);
//        ArrayList<SpatialVector6d> f_ext = new ArrayList<SpatialVector6d>();
//        
//        Dynamics.inverseDynamics(model, q, qDot, qDDot, out_tau, f_ext);
//        System.out.println("tau: "+out_tau);
        System.out.println("inverseDynamics end");

    }

    /**
     * Test of compositeRigidBodyAlgorithm method, of class Dynamics.
     */
    @Test
    public void testCompositeRigidBodyAlgorithm() {
        System.out.println("compositeRigidBodyAlgorithm");

//        ArrayRealVector q = new ArrayRealVector(2);
//        q.setEntry(0, 3);
//        q.setEntry(1, 1.1);
//        Array2DRowRealMatrix out_H = new Array2DRowRealMatrix(model.dof_count, model.dof_count);
//        boolean update_kinematics = true;
//        Dynamics.compositeRigidBodyAlgorithm(model, q, out_H, update_kinematics);
        System.out.println("compositeRigidBodyAlgorithm end");
    }

}
