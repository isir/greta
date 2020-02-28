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
import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class SpatialTransform {

    protected Matrix3d E;
    protected Vector3d r;

    public SpatialTransform() {
        this.E = new Matrix3d();
        this.E.toIdentity();
        this.r = new Vector3d();
    }

    public SpatialTransform(Matrix3d E, Vector3d r) {
        this.E = E;
        this.r = r;
    }

    public void set(SpatialTransform t){
        this.E = new Matrix3d(t.E);
        this.r = new Vector3d(t.r);
    }

    /**
     * Same as X * v.
     *
     * @param v_sp
     * @return returns (E * w, - E * rxw + E * v)
     */
    public SpatialVector6d apply(SpatialVector6d v_sp) {
        Vector3d v_rxw = new Vector3d(
                v_sp.getEntry(3) - r.getEntry(1) * v_sp.getEntry(2) + r.getEntry(2) * v_sp.getEntry(1),
                v_sp.getEntry(4) - r.getEntry(2) * v_sp.getEntry(0) + r.getEntry(0) * v_sp.getEntry(2),
                v_sp.getEntry(5) - r.getEntry(0) * v_sp.getEntry(1) + r.getEntry(1) * v_sp.getEntry(0));
        return new SpatialVector6d(
                E.getEntry(0, 0) * v_sp.getEntry(0) + E.getEntry(0, 1) * v_sp.getEntry(1) + E.getEntry(0, 2) * v_sp.getEntry(2),
                E.getEntry(1, 0) * v_sp.getEntry(0) + E.getEntry(1, 1) * v_sp.getEntry(1) + E.getEntry(1, 2) * v_sp.getEntry(2),
                E.getEntry(2, 0) * v_sp.getEntry(0) + E.getEntry(2, 1) * v_sp.getEntry(1) + E.getEntry(2, 2) * v_sp.getEntry(2),
                E.getEntry(0, 0) * v_rxw.getEntry(0) + E.getEntry(0, 1) * v_rxw.getEntry(1) + E.getEntry(0, 2) * v_rxw.getEntry(2),
                E.getEntry(1, 0) * v_rxw.getEntry(0) + E.getEntry(1, 1) * v_rxw.getEntry(1) + E.getEntry(1, 2) * v_rxw.getEntry(2),
                E.getEntry(2, 0) * v_rxw.getEntry(0) + E.getEntry(2, 1) * v_rxw.getEntry(1) + E.getEntry(2, 2) * v_rxw.getEntry(2)
        );
    }

    /**
     * Same as X^T * f.
     *
     *
     * @param f_sp
     * @return \returns (E^T * n + rx * E^T * f, E^T * f)
     */
    public SpatialVector6d applyTranspose(SpatialVector6d f_sp) {
        Vector3d E_T_f = new Vector3d(
                E.getEntry(0, 0) * f_sp.getEntry(3) + E.getEntry(1, 0) * f_sp.getEntry(4) + E.getEntry(2, 0) * f_sp.getEntry(5),
                E.getEntry(0, 1) * f_sp.getEntry(3) + E.getEntry(1, 1) * f_sp.getEntry(4) + E.getEntry(2, 1) * f_sp.getEntry(5),
                E.getEntry(0, 2) * f_sp.getEntry(3) + E.getEntry(1, 2) * f_sp.getEntry(4) + E.getEntry(2, 2) * f_sp.getEntry(5)
        );

        return new SpatialVector6d(
                E.getEntry(0, 0) * f_sp.getEntry(0) + E.getEntry(1, 0) * f_sp.getEntry(1) + E.getEntry(2, 0) * f_sp.getEntry(2) - r.getEntry(2) * E_T_f.getEntry(1) + r.getEntry(1) * E_T_f.getEntry(2),
                E.getEntry(0, 1) * f_sp.getEntry(0) + E.getEntry(1, 1) * f_sp.getEntry(1) + E.getEntry(2, 1) * f_sp.getEntry(2) + r.getEntry(2) * E_T_f.getEntry(0) - r.getEntry(0) * E_T_f.getEntry(2),
                E.getEntry(0, 2) * f_sp.getEntry(0) + E.getEntry(1, 2) * f_sp.getEntry(1) + E.getEntry(2, 2) * f_sp.getEntry(2) - r.getEntry(1) * E_T_f.getEntry(0) + r.getEntry(0) * E_T_f.getEntry(1),
                E_T_f.getEntry(0),
                E_T_f.getEntry(1),
                E_T_f.getEntry(2)
        );
    }

    /**
     * Same as X^* I X^{-1}
     *
     * @param rbi
     * @return
     */
    public SpatialRigidBodyInertia apply(SpatialRigidBodyInertia rbi) {
        return new SpatialRigidBodyInertia(rbi.mass, E.multiple(rbi.center.substract(r.multiple(rbi.mass))),
                E.multiple(
                        rbi.inertia.add(r.toCrossMatrix().multiple(rbi.center.toCrossMatrix())).add(
                                ((rbi.center.substract(r.multiple(rbi.mass))).toCrossMatrix().multiple(r.toCrossMatrix())))
                ).multiple(E.transpose())
        );
    }

    /**
     * Same as X^T I X
     *
     * @param rbi
     * @return
     */
    public SpatialRigidBodyInertia applyTranspose(SpatialRigidBodyInertia rbi) {
        Vector3d E_T_mr = E.transpose().multiple(rbi.center).add(r.multiple(rbi.mass));
        return new SpatialRigidBodyInertia(rbi.mass, E_T_mr,
                E.transpose().multiple(rbi.inertia.multiple(E)).substract(
                        r.toCrossMatrix().multiple((E.transpose().multiple(rbi.center)).toCrossMatrix())).substract(
                        E_T_mr.toCrossMatrix().multiple(r.toCrossMatrix())));
    }

    public SpatialVector6d applyAdjoint(SpatialVector6d f_sp) {
        Vector3d En_rxf = E.multiple(f_sp.getUpper()).substract(r.cross(f_sp.getLower()));
        return new SpatialVector6d(
                En_rxf.getEntry(0),
                En_rxf.getEntry(1),
                En_rxf.getEntry(2),
                E.getEntry(0, 0) * f_sp.getEntry(3) + E.getEntry(0, 1) * f_sp.getEntry(4) + E.getEntry(0, 2) * f_sp.getEntry(5),
                E.getEntry(1, 0) * f_sp.getEntry(3) + E.getEntry(1, 1) * f_sp.getEntry(4) + E.getEntry(1, 2) * f_sp.getEntry(5),
                E.getEntry(2, 0) * f_sp.getEntry(3) + E.getEntry(2, 1) * f_sp.getEntry(4) + E.getEntry(2, 2) * f_sp.getEntry(5)
        );
    }

    public SpatialMatrix6d toMatrix() {
        Matrix3d _Erx = E.multiple(r.toCrossMatrix());
        SpatialMatrix6d result = new SpatialMatrix6d();
        result.setSubMatrix(E.getData(), 0, 0);
        result.setSubMatrix(new Matrix3d().getData(), 0, 3);
        result.setSubMatrix(_Erx.multiple(-1).getData(), 3, 0);
        result.setSubMatrix(E.getData(), 3, 3);
        return result;
    }

    public SpatialMatrix6d toMatrixAdjoint() {
        Matrix3d _Erx = E.multiple(r.toCrossMatrix());
        SpatialMatrix6d result = new SpatialMatrix6d();
        result.setSubMatrix(E.getData(), 0, 0);
        result.setSubMatrix(_Erx.multiple(-1).getData(), 0, 3);
        result.setSubMatrix(new Matrix3d().getData(), 3, 0);
        result.setSubMatrix(E.getData(), 3, 3);
        return result;
    }

    public SpatialMatrix6d toMatrixTranspose() {
        Matrix3d _Erx = E.multiple(r.toCrossMatrix());
        SpatialMatrix6d result = new SpatialMatrix6d();
        result.setSubMatrix(E.transpose().getData(), 0, 0);
        result.setSubMatrix(_Erx.transpose().multiple(-1).getData(), 0, 3);
        result.setSubMatrix(new Matrix3d().getData(), 3, 0);
        result.setSubMatrix(E.transpose().getData(), 3, 3);
        return result;
    }

    public SpatialTransform inverse() {
        return new SpatialTransform(E.transpose(), E.multiple(r).multiple(-1));
    }

    public SpatialTransform multiple(SpatialTransform XT) {
        return new SpatialTransform(E.multiple(XT.E), XT.r.add(XT.E.transpose().multiple(r)));
    }

    public void multipleIntoSelf(SpatialTransform XT) {
        r = XT.r.add(XT.E.transpose().multiple(r));
        E = E.multiple(XT.E);
    }

    @Override
    public String toString() {
        return "spatial transform: translate " + r + " rotate " + E.toString();
    }

    public static SpatialTransform rot(double angle_rad, Vector3d axis) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);

        return new SpatialTransform(
                new Matrix3d(
                        axis.getEntry(0) * axis.getEntry(0) * (1.0f - c) + c,
                        axis.getEntry(1) * axis.getEntry(0) * (1.0f - c) + axis.getEntry(2) * s,
                        axis.getEntry(0) * axis.getEntry(2) * (1.0f - c) - axis.getEntry(1) * s,
                        axis.getEntry(0) * axis.getEntry(1) * (1.0f - c) - axis.getEntry(2) * s,
                        axis.getEntry(1) * axis.getEntry(1) * (1.0f - c) + c,
                        axis.getEntry(1) * axis.getEntry(2) * (1.0f - c) + axis.getEntry(0) * s,
                        axis.getEntry(0) * axis.getEntry(2) * (1.0f - c) + axis.getEntry(1) * s,
                        axis.getEntry(1) * axis.getEntry(2) * (1.0f - c) - axis.getEntry(0) * s,
                        axis.getEntry(2) * axis.getEntry(2) * (1.0f - c) + c
                ),
                new Vector3d(0., 0., 0.)
        );
    }

    public static SpatialTransform rotX(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new SpatialTransform(
                new Matrix3d(
                        1., 0., 0.,
                        0., c, s,
                        0., -s, c
                ),
                new Vector3d(0., 0., 0.)
        );
    }

    public static SpatialTransform rotY(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new SpatialTransform(
                new Matrix3d(
                        c, 0., -s,
                        0., 1., 0.,
                        s, 0., c
                ),
                new Vector3d(0., 0., 0.)
        );
    }

    public static SpatialTransform rotZ(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new SpatialTransform(
                new Matrix3d(
                        c, s, 0.,
                        -s, c, 0.,
                        0., 0., 1.
                ),
                new Vector3d(0., 0., 0.)
        );
    }

    public static SpatialTransform translate(Vector3d r) {
        Matrix3d m = new Matrix3d();
        m.toIdentity();
        return new SpatialTransform(m, r);
    }


}
