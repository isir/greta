/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.body;

import vib.core.animation.CharacterLowerBody;
import vib.core.animation.Frame;
import vib.core.animation.Skeleton;
import vib.core.util.math.Vec3d;

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
