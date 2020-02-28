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
package greta.core.animation.mocap;

import greta.core.animation.Frame;
import greta.core.util.math.Quaternion;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jing Huang
 */
public class MocapAvixsenseLoader {

    private Scanner _scan;
    private String _fileName;
    MotionSequence ms = null;

    public MotionSequence load(String fileName) {


        _fileName = fileName;
        ms = new MotionSequence(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MocapAvixsenseLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<Frame> frames = new ArrayList<Frame>();
        while (true) {
            try {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                Frame frame = new Frame();
                String[] tokens = s.split(" ");
                String empty = tokens[0];
                String pointnb = tokens[1];
                int nb = Integer.valueOf(pointnb);
                String empty2 = tokens[2];
                int idx = 2;


                for (int i = 0; i < nb; ++i) {

                    String name = tokens[++idx];
                    String p = tokens[++idx];

                    idx += 3;
                    double x = Float.valueOf(tokens[++idx]);
                    double y = Float.valueOf(tokens[++idx]);
                    double z = Float.valueOf(tokens[++idx]);

                    Quaternion q = eulerToQuat(x,y,z);
//                    Quaternion q0 = new Quaternion();
//                    q0.setAxisAngle(new Vec3d(1, 0, 0), z / 180.0f * 3.1415f);
//                    Quaternion q1 = new Quaternion();
//                    q1.setAxisAngle(new Vec3d(0, 1, 0), x / 180.0f * 3.1415f);
//                    Quaternion q2 = new Quaternion();
//                    q2.setAxisAngle(new Vec3d(0, 0, 1), y / 180.0f * 3.1415f);
//
//                    q = Quaternion.multiplication(q, q1);
//                    q = Quaternion.multiplication(q, q0);
//                    q = Quaternion.multiplication(q, q2);
//
//                    q.fromEulerXYZByAngle(x, y, z);
                    q.normalize();
                    frame.addRotation(name, q);
                    //System.out.println("class AVIXSENSE "+name + " "+q);
                }
                frames.add(frame);

            } catch (IOException ex) {
                Logger.getLogger(MocapAvixsenseLoader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        ms.setSequence(processOrientationToRotation(frames));
        return ms;
    }

    ArrayList<Frame> processOrientationToRotation(ArrayList<Frame> frames) {
        ArrayList<Frame> out = new ArrayList<Frame>();
        int i = 0;
        for (Frame frame : frames) {
            Frame f = new Frame();
            HashMap<String, Quaternion> rs = frame.getRotations();
            for (String name : rs.keySet()) {
                if (name.equalsIgnoreCase("L5")) {
                    f.addRotation(name, Quaternion.multiplication( rs.get("PELVIS").inverse() ,rs.get(name)));
                } else if (name.equalsIgnoreCase("L3")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("L5").inverse(), rs.get(name)));
                } else if (name.equalsIgnoreCase("T12")) {
                    f.addRotation(name, Quaternion.multiplication( rs.get("L3").inverse(), rs.get(name)));
                } else if (name.equalsIgnoreCase("T8")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("T12").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("Neck")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("T8").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("Head")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("NECK").inverse(),rs.get(name)));
                }
                else if (name.equalsIgnoreCase("LEFT_HAND")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("LEFT_FOREARM").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("LEFT_FOREARM")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("LEFT_UPPER_ARM").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("LEFT_UPPER_ARM")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("LEFT_SHOULDER").inverse(),rs.get(name)));
                }else if (name.equalsIgnoreCase("LEFT_SHOULDER")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("T8").inverse(),rs.get(name)));
                }
                else if (name.equalsIgnoreCase("RIGHT_HAND")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("RIGHT_FOREARM").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("RIGHT_FOREARM")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("RIGHT_UPPER_ARM").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("RIGHT_UPPER_ARM")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("RIGHT_SHOULDER").inverse(),rs.get(name)));
                }else if (name.equalsIgnoreCase("RIGHT_SHOULDER")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("T8").inverse(),rs.get(name)));
                }
                else if (name.equalsIgnoreCase("RIGHT_FOOT")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("RIGHT_LOWER_LEG").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("RIGHT_LOWER_LEG")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("RIGHT_UPPER_LEG").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("RIGHT_UPPER_LEG")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("PELVIS").inverse(),rs.get(name)));
                }

                else if (name.equalsIgnoreCase("LEFT_FOOT")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("LEFT_LOWER_LEG").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("LEFT_LOWER_LEG")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("LEFT_UPPER_LEG").inverse(),rs.get(name)));
                } else if (name.equalsIgnoreCase("LEFT_UPPER_LEG")) {
                    f.addRotation(name, Quaternion.multiplication(rs.get("PELVIS").inverse(),rs.get(name)));
                }
            }
            i++;
            if (i == 3) {
                out.add(f);
                i = 0;
            }
        }
        return out;
    }

    Quaternion getQ(double eulerX, double eulerY, double eulerZ) {
        Quaternion roll = new Quaternion((double) Math.sin((double) eulerX * 0.5), 0, 0, (double) Math.cos((double) eulerX * 0.5));
        Quaternion pitch = new Quaternion(0, (double) Math.sin(eulerY * 0.5f), 0, (double) Math.cos(eulerY * 0.5f));
        Quaternion yaw = new Quaternion(0, 0, (double) Math.sin(eulerZ * 0.5f), (double) Math.cos(eulerZ * 0.5f));
        return Quaternion.multiplication(Quaternion.multiplication(roll, yaw), pitch);
    }

    Quaternion eulerToQuat(double x, double y, double z) {
        x = degToRad(x);
        y = degToRad(y);
        z = degToRad(z);
        return getQ(z, x, y);
    }

    double degToRad(double f) {
        return f * 0.017453293f;
    }

    double radToDeg(double f) {
        return f * 57.29577951f;
    }
}
