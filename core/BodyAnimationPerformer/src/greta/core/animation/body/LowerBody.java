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
package greta.core.animation.body;

import greta.core.animation.CharacterLowerBody;
import greta.core.animation.Skeleton;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class LowerBody  extends ExpressiveFrame {
    Vec3d _offset;
    public LowerBody(double time, Vec3d offset, Skeleton sk){
        _offset = offset;
        CharacterLowerBody boy = new CharacterLowerBody();
        boy.setRootOffset(_offset);
        boy.setSkeleton(sk);
        boy.compute();

        for (String name : boy.getRotations().keySet()) {
            sk.getJoint(name).setLocalRotation(boy.getRotations().get(name));
        }
        sk.getJoint(0).setLocalPosition(_offset);
        sk.update();
        this.addRotations(boy.getFrame().getRotations());
        this.setRootTranslation(boy.getFrame().getRootTranslation());
    }

}
