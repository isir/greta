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
package greta.mgik.core.animation;

import greta.core.animation.math.Vector3d;
import greta.mgik.core.animation.Skeleton.Joint;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class BVHLoader {
    double scale = 0.01;
    private Scanner _scan;
    private String _fileName;
    public Skeleton sk;

    public void load(String fileName) {
        //InputStream in = info.openStream();
        _fileName = fileName;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(_fileName);
            _scan = new Scanner(fis);
            _scan.useLocale(Locale.US);
            loadFromScanner();
            sk.update();
        } catch (IOException ex) {
            Logger.getLogger(BVHLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(BVHLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void loadFromScanner() throws IOException {
        String token = _scan.next();
        if (token.equals("HIERARCHY")) {
            token = _scan.next();
            if (token.equals("ROOT")) {
                token = _scan.next();
                readJoint(token, sk, -1);
                token = _scan.next();
            }
        }
//        if (token.equals("MOTION")) {
//            _scan.next();
//            _animation.setNbFrames(_scan.nextInt());
//            _scan.next();
//            _scan.next();
//            _animation.setFrameTime(_scan.nextFloat());
//            for (int i = 0; i < _animation.getNbFrames(); i++) {
//                readChanelsValue(skeleton);
//            }
//
//        }

    }

    private Joint readJoint(String name, Skeleton skelet, int parent) {
        Joint joint = null;
        String token = _scan.next();
        if (token.equals("{")) {
            token = _scan.next();
            Vector3d offset = null;
            if (token.equals("OFFSET")) {
                offset = new Vector3d(_scan.nextFloat()*scale, _scan.nextFloat()*scale, _scan.nextFloat()*scale);
                token = _scan.next();
            }
            int nbChan = -1;
            ArrayList<String> order = new ArrayList<String>();
            if (token.equals("CHANNELS")) {
                nbChan = _scan.nextInt();
                for (int i = 0; i < nbChan; i++) {
                    String ty = _scan.next();
                    order.add(ty);
                }
                token = _scan.next();
            }
            joint = skelet.addJoint(parent, nbChan, offset, name);
            for(int i = 0; i < nbChan; i++){
                String ax = order.get(i);
                if(ax.equalsIgnoreCase("Zposition")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(0,0,1));
                }else if(ax.equalsIgnoreCase("Yposition")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(0,1,0));
                }else if(ax.equalsIgnoreCase("Xposition")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(1,0,0));
                }else if(ax.equalsIgnoreCase("Zrotation")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(0,0,1));
                }else if(ax.equalsIgnoreCase("Yrotation")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(0,1,0));
                }else if(ax.equalsIgnoreCase("Xrotation")){
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(1,0,0));
                }else{
                    sk.m_dim_axis.set(joint.m_dims.get(i), new Vector3d(0,0,0));
                    System.out.println("load joint axis error " +name+" "+i);
                }
            }

            while (token.equals("JOINT") || token.equals("End")) {
                if (token.equals("End")) {
                    skelet.m_endeffectors.add(joint.m_index);
                    readJoint(/*joint.m_name+*/_scan.next(), skelet, joint.m_index);
                } else {
                    readJoint(_scan.next(), skelet, joint.m_index);
                }
                token = _scan.next();
            }
        }

        return joint;
    }

//    private void readChanelsValue(Skeleton skeleton) {
//        if (skeleton != null) {
//            BVHFrame f = _animation.getChannel().clone();
//            for (Joint j : skeleton.getJoints()) {
//
//                String name = j.getName();
//                BVHChannel channel = f.getValue(name);
//                if (channel != null) {
//                    //System.out.println("class BVHLoader: error: readChanelsValue()");
//                    for (int i = 0; i < channel.getOrder().size(); ++i) {
//                        channel.getValues().add(_scan.nextDouble());
//                    }
//                }
//                // bvhChannel.getValues().add(_scan.nextFloat());
//            }
//            _animation.getSequence().add(f);
//        }
//    }
    public static void main(String[] args) {
        BVHLoader loader = new BVHLoader();
        loader.load("C:\\Users\\Jing\\Downloads\\bvh\\test-data\\Animations\\10_02.bvh");
    }
}
