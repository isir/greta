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
package greta.core.animation.common;

import greta.core.animation.common.body.Arm;
import greta.core.animation.common.body.Body;
import greta.core.animation.common.body.Head;
import greta.core.animation.common.body.Torse;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * the process for merging
 * all key informations into final key body frame
 * @author Jing Huang
 */
public class KeyFrameMixer {

    static double EPS = 0.00001f;

    static public LinkedList<Body> mix(List<Arm> left, List<Arm> right, List<Torse> torse, double timeThreshold) {
        LinkedList<Body> keyframegroupList = new LinkedList<Body>();
        for (Arm a : left) {
            Body g = getBodys(keyframegroupList, a.getTime(), timeThreshold);
            g.setLeftArm(a);
        }
        for (Arm a : right) {
            Body g = getBodys(keyframegroupList, a.getTime(), timeThreshold);
            g.setRightArm(a);
        }
        for (Torse a : torse) {
            Body g = getBodys(keyframegroupList, a.getTime(), timeThreshold);
            g.setTorse(a);
        }
        Collections.sort(keyframegroupList, new Comparator<Body>() {
            @Override
            public int compare(Body o1, Body o2) {
                return (int) Math.signum(o1.getTime() - o2.getTime());
            }
        });

        for (int i = 0; i < keyframegroupList.size(); ++i) {
            Body b = keyframegroupList.get(i);
            if (b.getTorse() == null) {
                b.setTorse(getCurrentTorso(torse, b.getTime()));
            }
        }
        return keyframegroupList;
    }

    static Body getBodys(List<Body> glist, double time, double threshold) {
        for (Body g : glist) {
            if (java.lang.Math.abs(g.getTime() - time) < threshold) {
                return g;
            }
        }
        Body g = new Body(time);
        glist.add(g);
        return g;
    }

    public static Torse getCurrentTorso(List<Torse> torses, double time) {
        if (torses.size() == 0) {
            return null;
        }

        if (torses.size() == 1) {
            Torse t = torses.get(0);
            double timeT = t.getTime();
            System.out.println("only one torso key frame in KeyFrameMixer");
            return null;
        }

        Torse t1 = null, t2 = null;
        double time1 = 0, time2 = 0;
        for (int i = 0; i < torses.size() - 1; ++i) {
            t1 = torses.get(i);
            time1 = t1.getTime();
            t2 = torses.get(i + 1);
            time2 = t2.getTime();
            if (time1 < time && time < time2) {
                break;
            }
        }

        if (time < time1) {
            return torses.get(0);
        } else if (time > time2) {
            return torses.get(torses.size() - 1);
        } else if (time1 < time && time < time2) {
            Torse nTorse = new Torse(time);
            double ratio = (time - time1) / (time2 - time1);
            Quaternion interpo = Quaternion.slerp(t1.getRotation(), t2.getRotation(), ratio, true);
            nTorse.setRotation(interpo);
            return nTorse;
        }

/*
        for (int i = 0; i < torses.size(); ++i) {
            Torse t = torses.get(i);
            if (Math.abs(time - t.getTime()) < EPS) {
                if (i == 0) {
                    return null;
                }
                return t;
            } else if (time < t.getTime()) {
                if (i - 1 == 0) {
                    return null;
                } else if (i - 1 > 0) {
                    Torse nTorse = new Torse(time);
                    Torse previousTorse = torses.get(i - 1);
                    double ratio = (time - nTorse.getTime()) / (t.getTime() - previousTorse.getTime());
                    Quaternion interpo = Quaternion.slerp(previousTorse.getRotation(), t.getRotation(), ratio, true);
                    nTorse.setRotation(interpo);
                    return nTorse;

                } else {
                    return t;
                }
            }
        }
*/
        return torses.get(torses.size() - 1);
    }

    public static Head getCurrentHead(List<Head> heads, double time) {
        if (heads.size() == 0) {
            return null;
        }
        for (int i = 0; i < heads.size(); ++i) {
            Head t = heads.get(i);
            if (Math.abs(time - t.getTime()) < EPS) {
                if (i == 0) {
                    return null;
                }
                return t;
            } else if (time < t.getTime()) {
                if (i - 1 == 0) {
                    return null;
                } else if (i - 1 > 0) {
                    Head nhead = new Head(time);
                    Head previousHead = heads.get(i - 1);
                    double ratio = (time - nhead.getTime()) / (t.getTime() - previousHead.getTime());
                    Quaternion interpo = Quaternion.slerp(previousHead.getRotation(), t.getRotation(), ratio, true);
                    nhead.setRotation(interpo);
                    return nhead;

                } else {
                    return t;
                }
            }
        }

        return heads.get(heads.size() - 1);

    }

    public static Arm getCurrentArm(List<Arm> arms, double time) {
        if (arms.size() == 0) {
            return null;
        }
        for (int i = 0; i < arms.size(); ++i) {
            Arm t = arms.get(i);
            if (Math.abs(time - t.getTime()) < EPS) {
                if (i == 0) {
                    return null;
                }
                return t;
            } else if (time < t.getTime()) {
                if (i - 1 == 0) {
                    return null;
                } else if (i - 1 > 0) {
                    Arm nhand = new Arm(time);
                    nhand.setSide(t.getSide());
                    Arm previousArm = arms.get(i - 1);
                    double ratio = (time - nhand.getTime()) / (t.getTime() - previousArm.getTime());
                    nhand.setTarget(Target.interpolate(previousArm.getTarget(), t.getTarget(), ratio));
                    nhand.setUpDirectionVector(Vec3d.interpolation(previousArm.getUpDirectionVector(), t.getUpDirectionVector(), time));
//                    System.out.println(previousArm.getWrist());
//                    System.out.println(t.getWrist());
                    nhand.setWrist(Quaternion.slerp(previousArm.getWrist(), t.getWrist(), ratio, true), t.isWristLocalOrientation());

                    return nhand;

                } else {
                    return t;
                }
            }
        }

        return arms.get(arms.size() - 1);

    }
}
