/*
 * This file is part of Greta.
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
package greta.core.animation.common.Frame;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class JointFrame {
    public greta.core.util.math.Quaternion _localrotation = new greta.core.util.math.Quaternion();
    public Vec3d _translation = new Vec3d();
    public JointFrame(JointFrame jf){
        _localrotation = new greta.core.util.math.Quaternion(jf._localrotation);
        _translation = new Vec3d(jf._translation);
    }
    public JointFrame(){
    }

    static public JointFrame interpolate(JointFrame jf0, JointFrame jf1, double f){
        JointFrame jf = new JointFrame();
        jf._localrotation = Quaternion.slerp(jf0._localrotation, jf1._localrotation, (double)f, true);
        jf._translation = Vec3d.interpolation(jf0._translation, jf1._translation, (double)f);
        return jf;
    }
}
