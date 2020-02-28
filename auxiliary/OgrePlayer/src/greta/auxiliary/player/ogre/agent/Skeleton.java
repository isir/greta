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
package greta.auxiliary.player.ogre.agent;

import greta.auxiliary.player.ogre.Ogre;
import greta.auxiliary.player.ogre.OgreThread;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SkeletonInstance;

/**
 *
 * @author Andre-Marie Pez
 */
public class Skeleton {

    private java.util.List<Entity> bones;
    private static boolean showSkeletonAxis = Ogre.DEBUG;

    public Skeleton(final Entity body) {
        Ogre.callSync(new OgreThread.Callback() {

            @Override
            public void run() {
                SceneManager sceneManager = body._getManager();
                SkeletonInstance skel = body.getSkeleton();
                double scaleFactor = 0.05;
                bones = new java.util.ArrayList<Entity>(skel.getNumBones());
                for (int i = 0; i < skel.getNumBones(); i++) {
                    vib.auxiliary.player.ogre.natives.Bone currentBone = skel.getBone(i);
                    if (showSkeletonAxis) {
                        Entity deb = Ogre.createEntity(sceneManager, currentBone.getName() + "-axis-" + Math.random(), "axes.mesh", false);
                        body.attachObjectToBone(currentBone.getName(), deb)
                                .scale(scaleFactor, scaleFactor, scaleFactor);

                        bones.add(deb);
                    }
                    for (int j = 0; j < currentBone.numChildren(); ++j) {
                        String childname = currentBone.getChild_getName(j);
                        if (skel.hasBone(childname)) {
                            vib.auxiliary.player.ogre.natives.Bone child = skel.getBone(childname);

                            Entity bone = Ogre.createEntity(sceneManager, child.getName() + "-bone-" + Math.random(), "bone.mesh", false);
                            vib.auxiliary.player.ogre.natives.TagPoint point = body.attachObjectToBone(currentBone.getName(), bone);
                            greta.core.util.math.Vec3d pos = Ogre.convert(child.getPosition());
                            double bonesize = pos.length();
                            point.scale(bonesize, bonesize, bonesize);
                            greta.core.util.math.Vec3d up = new greta.core.util.math.Vec3d(0, 1, 0);
                            greta.core.util.math.Vec3d axis = up.cross3(pos);
                            float angle = (float) Math.acos(up.dot3(pos) / bonesize);
                            greta.core.util.math.Quaternion q = new greta.core.util.math.Quaternion(axis, angle);
                            point.setOrientation(Ogre.convert(q));
                            bones.add(bone);
                        }
                    }
                }
            }
        });
    }

    public void setVisible(boolean visible){
        for(Entity bone : bones){
            bone.setVisible(visible);
        }
    }
}
