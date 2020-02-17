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
import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)
 * jhuang@telecom-paristech.fr dynamics model from featherstone
 * http://royfeatherstone.org/spatial/
 */
public class Dynamics {

    public static boolean debug = false;

    public static void forwardDynamics(DModel model, ArrayRealVector q, ArrayRealVector qDot,
            ArrayRealVector tau, ArrayRealVector out_qDDot, ArrayList<SpatialVector6d> f_ext) {

        SpatialVector6d spatial_gravity = new SpatialVector6d(0., 0., 0., model.gravity.getEntry(0), model.gravity.getEntry(1), model.gravity.getEntry(2));
        int i = 0;

        if (debug) {
            System.out.println("Q          = " + q);
            System.out.println("QDot       = " + qDot);
            System.out.println("Tau        = " + tau);
            System.out.println("---");
        }

        // Reset the velocity of the root body
        model.v.get(0).setZero();

        for (i = 1; i < model.mBodies.size(); i++) {
            int q_index = model.mJoints.get(i).q_index;
            SpatialTransform X_J = new SpatialTransform();
            SpatialVector6d v_J = new SpatialVector6d();
            SpatialVector6d c_J = new SpatialVector6d();
            int lambda = model.lambda.get(i);

            DModel.jcalc(model, i, X_J, v_J, c_J, q, qDot);

            model.X_lambda.set(i, X_J.multiple(model.X_T.get(i)));

            if (lambda != 0) {
                model.X_base.set(i, model.X_lambda.get(i).multiple(model.X_base.get(lambda)));
            } else {
                model.X_base.set(i, model.X_lambda.get(i));
            }
            model.v.set(i, model.X_lambda.get(i).apply(model.v.get(lambda)).add(v_J));

            if (debug) {
                System.out.println("X_J (" + i + "): " + X_J);
                System.out.println("v_J (" + i + "):" + v_J);
                System.out.println("v_lambda" + i + ":" + model.v.get(lambda));
                System.out.println("X_base (" + i + "):" + model.X_base.get(i));
                System.out.println("X_lambda (" + i + "):" + model.X_lambda.get(i));
                System.out.println("SpatialVelocity (" + i + "): " + model.v.get(i));
            }

            model.c.set(i, c_J.add(SpatialVector6d.crossM(model.v.get(i), v_J)));
            model.IA.set(i, new SpatialMatrix6d(model.mBodies.get(i).mSpatialInertia));

            model.pA.set(i, SpatialVector6d.crossF(model.v.get(i), model.IA.get(i).multiple(model.v.get(i))));

            if (f_ext.size() > 0 && !f_ext.get(i).isZero()) {
                if (debug) {
                    System.out.println("External force (" + i + ") = " + model.X_base.get(i).toMatrixAdjoint().operate(f_ext.get(i)));
                }
                model.pA.set(i, model.pA.get(i).substract(new SpatialVector6d(model.X_base.get(i).toMatrixAdjoint().operate(f_ext.get(i)))));
            }
        }
        if (debug) {
            System.out.println("--- first loop ---");
        }

        for (i = model.mBodies.size() - 1; i > 0; i--) {
            int q_index = model.mJoints.get(i).q_index;

            if (model.mJoints.get(i).mDoFCount == 3) {
                model.multdof3_U.set(i, model.IA.get(i).multiple(model.multdof3_S.get(i)));

                Matrix3d mx = model.multdof3_S.get(i).transposeMultiple(model.multdof3_U.get(i));
                model.multdof3_Dinv.set(i, new Matrix3d(new LUDecomposition(mx).getSolver().getInverse()));

                Vector3d tau_temp = new Vector3d(tau.getEntry(q_index), tau.getEntry(q_index + 1), tau.getEntry(q_index + 2));

                model.multdof3_u.set(i, tau_temp.substract(new Vector3d(model.multdof3_S.get(i).transposeMultiple(model.pA.get(i)))));

//			LOG << "multdof3_u[" << i << "] = " << model.multdof3_u[i].transpose() << std::endl;
                int lambda = model.lambda.get(i);
                if (lambda != 0) {
                    SpatialMatrix6d Ia = model.IA.get(i).substract(new SpatialMatrix6d(model.multdof3_U.get(i).multiply(model.multdof3_Dinv.get(i).multiply(model.multdof3_U.get(i).transpose()))));
                    SpatialVector6d pa = model.pA.get(i).add(Ia.multiple(model.c.get(i)).add(model.multdof3_U.get(i).multiple(model.multdof3_Dinv.get(i).multiple(model.multdof3_u.get(i)))));

                    model.IA.get(lambda).addToSelf(new SpatialMatrix6d(model.X_lambda.get(i).toMatrixTranspose().multiply(Ia.multiply(model.X_lambda.get(i).toMatrix()))));
                    model.pA.get(lambda).addToSelf(model.X_lambda.get(i).applyTranspose(pa));
                    if (debug) {
                        System.out.println("pA[" + lambda + "] = " + model.pA.get(lambda).transpose());
                    }
                }
            } else {
                model.U.set(i, model.IA.get(i).multiple(model.S.get(i)));
                model.d.setEntry(i, model.S.get(i).dotProduct(model.U.get(i)));
                model.u.setEntry(i, tau.getEntry(q_index) - model.S.get(i).dotProduct(model.pA.get(i)));
//			LOG << "u[" << i << "] = " << model.u[i] << std::endl;

                int lambda = model.lambda.get(i);
                if (lambda != 0) {
                    SpatialMatrix6d Ia = model.IA.get(i).substract(new SpatialMatrix6d(model.U.get(i).toMatrix().multiply((model.U.get(i).divide(model.d.getEntry(i))).transpose())));
                    double v = model.u.getEntry(i) / model.d.getEntry(i);

                    SpatialVector6d pa = new SpatialVector6d(SpatialVector6d.add(model.pA.get(i), Ia.multiple(model.c.get(i)), model.U.get(i).multiple(v)));

                    model.IA.get(lambda).addToSelf(new SpatialMatrix6d(SpatialMatrix6d.multiple(model.X_lambda.get(i).toMatrixTranspose(), Ia, model.X_lambda.get(i).toMatrix())));
                    model.pA.get(lambda).addToSelf(model.X_lambda.get(i).applyTranspose(pa));
                    if (debug) {
                        System.out.println("pA[" + lambda + "] = " + model.pA.get(lambda).transpose());
                    }
                }
            }
        }

        model.a.set(0, spatial_gravity.multiple(-1.));

        for (i = 1; i < model.mBodies.size(); i++) {
            int q_index = model.mJoints.get(i).q_index;
            int lambda = model.lambda.get(i);
            SpatialTransform X_lambda = model.X_lambda.get(i);

            model.a.set(i, X_lambda.apply(model.a.get(lambda).add(model.c.get(i))));
            if (debug) {
                System.out.println("a'[" + i + "] = " + model.a.get(i).transpose());
            }

            if (model.mJoints.get(i).mDoFCount == 3) {

                Vector3d qdd_temp = model.multdof3_Dinv.get(i).multiple(model.multdof3_u.get(i).substract(model.multdof3_U.get(i).transposeMultiple(model.a.get(i))));
                out_qDDot.setEntry(q_index, qdd_temp.getEntry(0));
                out_qDDot.setEntry(q_index + 1, qdd_temp.getEntry(1));
                out_qDDot.setEntry(q_index + 2, qdd_temp.getEntry(2));

                model.a.get(i).addToSelf(model.multdof3_S.get(i).multiple(qdd_temp));

            } else {
                out_qDDot.setEntry(q_index, (1. / model.d.getEntry(i)) * (model.u.getEntry(i) - model.U.get(i).dotProduct(model.a.get(i))));
                model.a.get(i).addToSelf(model.S.get(i).multiple(out_qDDot.getEntry(q_index)));
            }
        }
        if (debug) {
            System.out.println("QDDot = " + out_qDDot);
        }

    }

    public static void forwardDynamicsLagrangian(DModel model, ArrayRealVector q, ArrayRealVector qDot,
            ArrayRealVector tau, ArrayRealVector out_qDDot, ArrayList<SpatialVector6d> f_ext) {

        if (debug) {
            System.out.println("-------- " + "__func__" + " --------");
        }

        Array2DRowRealMatrix H = new Array2DRowRealMatrix(model.dof_count, model.dof_count);

        ArrayRealVector C = new ArrayRealVector(model.dof_count);

        // we set QDDot to zero to compute C properly with the InverseDynamics
        // method.
        out_qDDot.mapMultiply(0);

        inverseDynamics(model, q, qDot, out_qDDot, C, f_ext);
        compositeRigidBodyAlgorithm(model, q, H, false);

        if (debug) {
            System.out.println("A = " + H);
            System.out.println("b = " + C.mapMultiply(-1).add(tau));
        }

        DecompositionSolver solver = new LUDecomposition(H).getSolver();
        RealVector value = solver.solve(C.mapMultiply(-1).add(tau));
        for (int i = 0; i < out_qDDot.getDimension(); ++i) {
            out_qDDot.setEntry(i, value.getEntry(i));
        }
//#ifndef RBDL_USE_SIMPLE_MATH
//	switch (linear_solver) {
//		case (LinearSolverPartialPivLU) :
//			QDDot = H.partialPivLu().solve (C * -1. + Tau);
//			break;
//		case (LinearSolverColPivHouseholderQR) :
//			QDDot = H.colPivHouseholderQr().solve (C * -1. + Tau);
//			break;
//		default:
//			LOG << "Error: Invalid linear solver: " << linear_solver << std::endl;
//			assert (0);
//			break;
//	}
//#else
//	bool solve_successful = LinSolveGaussElimPivot (H, C * -1. + Tau, QDDot);
//	assert (solve_successful);
//#endif
        if (debug) {
            System.out.println("x = " + out_qDDot);
        }

    }

    public static void inverseDynamics(DModel model, ArrayRealVector q, ArrayRealVector qDot,
            ArrayRealVector qDDot, ArrayRealVector out_tau, ArrayList<SpatialVector6d> f_ext) {

        SpatialVector6d spatial_gravity = new SpatialVector6d(0., 0., 0., model.gravity.getEntry(0), model.gravity.getEntry(1), model.gravity.getEntry(2));
        int i;

        // Reset the velocity of the root body
        model.v.get(0).setZero();
        model.a.set(0, spatial_gravity.multiple(-1.));

        for (i = 1; i < model.mBodies.size(); i++) {
            int q_index = model.mJoints.get(i).q_index;
            SpatialTransform X_J = new SpatialTransform();
            SpatialVector6d v_J = new SpatialVector6d();
            SpatialVector6d c_J = new SpatialVector6d();
            int lambda = model.lambda.get(i);

            DModel.jcalc(model, i, X_J, v_J, c_J, q, qDot);

            model.X_lambda.set(i, X_J.multiple(model.X_T.get(i)));

            if (lambda == 0) {
                model.X_base.set(i, model.X_lambda.get(i));
                model.v.set(i, v_J);
                SpatialVector6d grav = spatial_gravity.multiple(-1.);
                model.a.set(i, model.X_base.get(i).apply(grav));

                if (model.mJoints.get(i).mDoFCount == 3) {
                    RealVector v = model.multdof3_S.get(i).operate(new Vector3d(qDDot.getEntry(q_index), qDDot.getEntry(q_index + 1), qDDot.getEntry(q_index + 2)));
                    model.a.set(i, model.a.get(i).add(new SpatialVector6d(v)));
                } else {
                    model.a.set(i, model.a.get(i).add(model.S.get(i).multiple(qDDot.getEntry(q_index))));
                }
            } else {
                model.X_base.set(i, model.X_lambda.get(i).multiple(model.X_base.get(lambda)));
                model.v.set(i, model.X_lambda.get(i).apply(model.v.get(lambda)).add(v_J));
                model.c.set(i, c_J.add(SpatialVector6d.crossM(model.v.get(i), v_J)));
                model.a.set(i, model.X_lambda.get(i).apply(model.a.get(lambda)).add(model.c.get(i)));

                if (model.mJoints.get(i).mDoFCount == 3) {
                    Vector3d omegadot_temp = new Vector3d(qDDot.getEntry(q_index), qDDot.getEntry(q_index + 1), qDDot.getEntry(q_index + 2));
                    model.a.set(i, model.a.get(i).add(new SpatialVector6d(model.multdof3_S.get(i).operate(omegadot_temp))));
                } else {
                    model.a.set(i, model.a.get(i).add(model.S.get(i).multiple(qDDot.getEntry(q_index))));
                }
            }

            model.f.set(i, new SpatialVector6d(model.mBodies.get(i).mSpatialInertia.operate(model.a.get(i)))
                    .add(
                            SpatialVector6d.crossF(model.v.get(i), new SpatialVector6d(model.mBodies.get(i).mSpatialInertia.operate(model.v.get(i))))
                    ));
            if (f_ext.size() > 0 && !f_ext.get(i).isZero()) {
                SpatialVector6d sv = new SpatialVector6d(model.X_base.get(i).toMatrixAdjoint().operate(f_ext.get(i)));
                model.f.set(i, model.f.get(i).add(sv.multiple(-1)));
            }
        }
        if (debug) {
            System.out.println("-- first loop --");
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.println("X_base[" + i + "] = ");
                System.out.println(model.X_base.get(i));
            }
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.print("v[" + i + "] = ");
                System.out.println(model.v.get(i).transpose());
            }
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.println("a[" + i + "] = " + model.a.get(i).transpose());
            }
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.println("f[" + i + "] = " + model.f.get(i).transpose());
            }
        }
        for (i = model.mBodies.size() - 1; i > 0; i--) {
            int q_index = model.mJoints.get(i).q_index;
            int lambda = model.lambda.get(i);

            if (model.mJoints.get(i).mDoFCount == 3) {
                Vector3d tau_temp = model.multdof3_S.get(i).transposeMultiple(model.f.get(i));
                out_tau.setEntry(q_index, tau_temp.getEntry(0));
                out_tau.setEntry(q_index + 1, tau_temp.getEntry(1));
                out_tau.setEntry(q_index + 2, tau_temp.getEntry(2));
            } else {
                out_tau.setEntry(q_index, model.S.get(i).dotProduct(model.f.get(i)));
            }

            if (lambda != 0) {
                model.f.set(lambda, model.f.get(lambda).add(model.X_lambda.get(i).applyTranspose(model.f.get(i))));
            }
        }
        if (debug) {
            System.out.println("-- second loop");
            System.out.println("Tau = " + out_tau);
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.println("f[" + i + "] = " + model.f.get(i).transpose());
            }
            for (i = 0; i < model.mBodies.size(); i++) {
                System.out.println("S[" + i + "] = " + model.S.get(i).transpose());
            }
        }
    }

    public static void compositeRigidBodyAlgorithm(DModel model, ArrayRealVector q, Array2DRowRealMatrix out_H, boolean update_kinematics) {
        assert (out_H.getRowDimension() == model.dof_count && out_H.getColumnDimension() == model.dof_count);
        int i = 0;
        for (i = 1; i < model.mBodies.size(); i++) {
            if (update_kinematics) {
                DModel.jcalc_X_lambda_S(model, i, q);
            }
            //The spatial inertia of body
            model.Ic.get(i).createFromMatrix(model.mBodies.get(i).mSpatialInertia);
        }

        for (i = model.mBodies.size() - 1; i > 0; i--) {
            int lambda = model.lambda.get(i);

            if (lambda != 0) {
                model.Ic.set(lambda, model.Ic.get(lambda).add(model.X_lambda.get(i).applyTranspose(model.Ic.get(i))));
            }

            int dof_index_i = model.mJoints.get(i).q_index;

            if (model.mJoints.get(i).mDoFCount == 3) {
                Matrix63d F_63 = new Matrix63d(model.Ic.get(i).toMatrix().multiply(model.multdof3_S.get(i)));
                Matrix3d H_temp = new Matrix3d(model.multdof3_S.get(i).transpose().multiply(F_63));

                out_H.setSubMatrix(H_temp.getData(), dof_index_i, dof_index_i);

                int j = i;
                int dof_index_j = dof_index_i;

                while (model.lambda.get(j) != 0) {
                    F_63 = new Matrix63d(model.X_lambda.get(j).toMatrixTranspose().multiply(F_63));
                    j = model.lambda.get(j);
                    dof_index_j = model.mJoints.get(j).q_index;

                    if (model.mJoints.get(j).mDoFCount == 3) {
                        Matrix3d H_temp2 = new Matrix3d(F_63.transpose().multiply(model.multdof3_S.get(j)));

                        out_H.setSubMatrix(H_temp2.getData(), dof_index_i, dof_index_j);
                        out_H.setSubMatrix(H_temp2.transpose().getData(), dof_index_j, dof_index_i);
                    } else {
                        Vector3d H_temp2 = new Vector3d(F_63.transpose().operate(model.S.get(j)));
                        out_H.setSubMatrix(H_temp2.toMatrix().getData(), dof_index_i, dof_index_j);
                        out_H.setSubMatrix(H_temp2.toTransposeMatrix().getData(), dof_index_j, dof_index_i);
                    }
                }
            } else {
                SpatialVector6d F = model.Ic.get(i).multiple(model.S.get(i));
                out_H.setEntry(dof_index_i, dof_index_i, model.S.get(i).dotProduct(F));

                int j = i;
                int dof_index_j = dof_index_i;

                while (model.lambda.get(j) != 0) {
                    F = model.X_lambda.get(j).applyTranspose(F);
                    j = model.lambda.get(j);
                    dof_index_j = model.mJoints.get(j).q_index;

                    if (model.mJoints.get(j).mDoFCount == 3) {
                        Array2DRowRealMatrix m = F.transpose().multiply(model.multdof3_S.get(j));
                        Vector3d H_temp2 = new Vector3d(m.getRow(0));

                        out_H.setSubMatrix(H_temp2.toTransposeMatrix().getData(), dof_index_i, dof_index_j);
                        out_H.setSubMatrix(H_temp2.toMatrix().getData(), dof_index_j, dof_index_i);
                    } else {
                        out_H.setEntry(dof_index_i, dof_index_j, F.dotProduct(model.S.get(j)));
                        out_H.setEntry(dof_index_j, dof_index_i, out_H.getEntry(dof_index_i, dof_index_j));
                    }
                }
            }
        }

    }
}
