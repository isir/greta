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
public class SpatialRigidBodyInertia {

    double mass;
    Vector3d center;
    Matrix3d inertia;

    public SpatialRigidBodyInertia() {
        mass = 0;
        center = new Vector3d();
        inertia = new Matrix3d();
    }

    public SpatialRigidBodyInertia(double mass, Vector3d center, Matrix3d inertia) {
        this.mass = mass;
        this.center = center;
        this.inertia = inertia;
    }

    public SpatialVector6d multiple(SpatialVector6d mv) {
        Vector3d mv_upper = mv.getUpper();
        Vector3d mv_lower = mv.getLower();

        Vector3d res_upper = inertia.multiple(mv_upper).add(center.cross(mv_lower));
        Vector3d res_lower = mv_lower.multiple(mass).substract(center.cross(mv_upper));

        return new SpatialVector6d(res_upper, res_lower);
    }

    public SpatialRigidBodyInertia add(SpatialRigidBodyInertia srbi) {
        return new SpatialRigidBodyInertia(mass + srbi.mass, center.add(srbi.center), inertia.add(srbi.inertia));
    }

    public void createFromMatrix(SpatialMatrix6d Ic) {
        mass = Ic.getEntry(3, 3);
        center.set(-Ic.getEntry(1, 5), Ic.getEntry(0, 5), -Ic.getEntry(0, 4));
        inertia = new Matrix3d(Ic.getSubMatrix(0, 2, 0, 2));
    }

    public SpatialMatrix6d toMatrix() {
        SpatialMatrix6d result = new SpatialMatrix6d();
        result.setSubMatrix(inertia.getData(), 0, 0);
        result.setSubMatrix(center.toCrossMatrix().getData(), 0, 3);
        result.setSubMatrix(center.toCrossMatrix().scalarMultiply(-1).getData(), 3, 0);
        Matrix3d m = new Matrix3d();
        m.toMIdentity(mass);
        result.setSubMatrix(m.getData(), 3, 3);
        return result;
    }

    @Override
    public String toString(){
        return "spatial rigid body inertia: mass "+ mass + " center " + center.toString() + " inertia " + inertia.toString();
    }
}
