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
package greta.core.animation.bvh;

import greta.core.animation.Joint;
import greta.core.animation.Skeleton;
import greta.core.util.math.Vec3d;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jing HUANG
 */
public class BVHLoader {

    private Scanner _scan;
    private String _fileName;
    //BVHAnimData _data;
    private BVHAnimation _animation;

    public BVHAnimation load(String fileName) {
        //InputStream in = info.openStream();
        _fileName = fileName;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(_fileName);
            _scan = new Scanner(fis);
            _scan.useLocale(Locale.US);
            loadFromScanner();
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
        return _animation;
    }

    private void loadFromScanner() throws IOException {
        _animation = new BVHAnimation(_fileName);
        Skeleton skeleton = new Skeleton(_fileName);
        _animation.setSkeleton(skeleton);

        String token = _scan.next();
        if (token.equals("HIERARCHY")) {
            token = _scan.next();
            if (token.equals("ROOT")) {
                token = _scan.next();
                readJoint(token, skeleton, null);
                token = _scan.next();
            }
        }
        if (token.equals("MOTION")) {
            _scan.next();
            _animation.setNbFrames(_scan.nextInt());
            _scan.next();
            _scan.next();
            _animation.setFrameTime(_scan.nextFloat());
            for (int i = 0; i < _animation.getNbFrames(); i++) {
                readChanelsValue(skeleton);
            }

        }

    }

    private Joint readJoint(String name, Skeleton skelet, Joint parent) {
        Joint joint = skelet.createJoint(name, -1);
        joint.setParent(parent);
        String token = _scan.next();
        if (token.equals("{")) {
            token = _scan.next();
            if (token.equals("OFFSET")) {
                joint.setLocalPosition(new Vec3d(_scan.nextFloat(), _scan.nextFloat(), _scan.nextFloat()));
                token = _scan.next();
            }
            if (token.equals("CHANNELS")) {
                BVHChannel channel = new BVHChannel();
                ArrayList<String> order = new ArrayList<String>();
                int nbChan = _scan.nextInt();
                for (int i = 0; i < nbChan; i++) {
                    order.add(_scan.next());
                }
                channel.setOrder(order);
                _animation.getChannel().addValue(name, channel);
                token = _scan.next();
            }
            while (token.equals("JOINT") || token.equals("End")) {
                if(token.equals("End")){
                    readJoint(joint.getName()+_scan.next(), skelet, joint);
                }else{
                    readJoint(_scan.next(), skelet, joint);
                }
                token = _scan.next();
            }
        }

        return joint;
    }

    private void readChanelsValue(Skeleton skeleton) {
        if (skeleton != null) {
            BVHFrame f = _animation.getChannel().clone();
            for (Joint j : skeleton.getJoints()) {

                String name = j.getName();
                BVHChannel channel = f.getValue(name);
                if (channel != null) {
                    //System.out.println("class BVHLoader: error: readChanelsValue()");
                    for (int i = 0; i < channel.getOrder().size(); ++i) {
                        channel.getValues().add(_scan.nextDouble());
                    }
                }
                // bvhChannel.getValues().add(_scan.nextFloat());
            }
            _animation.getSequence().add(f);
        }
    }

    public BVHAnimation getAnimation() {
        return _animation;
    }


    public static void main(String[] args) {
        BVHLoader loader = new BVHLoader();
        loader.load("C:\\Users\\Jing\\Downloads\\bvh\\test-data\\Animations\\10_02.bvh");
    }
}
