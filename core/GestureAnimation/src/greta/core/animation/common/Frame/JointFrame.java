/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
