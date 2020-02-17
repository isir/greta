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
package greta.core.util.animation;

/**
 *
 * @author Jing Huang
 */
public class AnimationFramesIO {

//    public static void write(List<AnimationFrame> frames, String fileName) {
//        FileOutputStream fop = null;
//        File file;
//        String content = "";
//        for (AnimationFrame frame : frames) {
//            //head
//            int sizet = frame.getTranslations().size();
//            int sizer = frame.getRotations().size();
//            int fn = frame.getFrameNumber();
//            content += "frame "+fn +" size "+sizer +" "+sizet+ "\n";
//            //rotation
//            List<Quaternion> q = frame.getRotations();
//            int idx = 0;
//            for (Quaternion v : q) {
//                content +="r "+ idx+" "+ v.x() + " " + v.y() + " " + v.z() + " " + v.w() + "\n";
//                idx++;
//            }
//            //translation
//            idx = 0;
//            List<Vec3d> tr = frame.getTranslations();
//            for (Vec3d v : tr) {
//                content +="t "+ idx+" "+v.x() + " " + v.y() + " " + v.z() + "\n";
//                idx++;
//            }
//            //finish
//            content += "\n";
//        }
//        try {
//            file = new File(fileName);
//            fop = new FileOutputStream(file);
//
//            // if file doesnt exists, then create it
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            // get the content in bytes
//            byte[] contentInBytes = content.getBytes();
//
//            fop.write(contentInBytes);
//            fop.flush();
//            fop.close();
//
//            System.out.println("Done");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (fop != null) {
//                    fop.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    //TODO
//    public static void read(String fileName, List<AnimationFrame> frames) {
//        try {
//            File file = new File(fileName);
//            FileReader fileReader = new FileReader(file);
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            StringBuffer stringBuffer = new StringBuffer();
//            String line;
//            AnimationFrame current = null;
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] tokens = line.split(" ");
//                if(tokens.length <= 0){
//                    continue;
//                }else if(tokens[0].equalsIgnoreCase("frame")){
//                    int frame = Integer.parseInt(tokens[1]);
//                    int ts = Integer.parseInt(tokens[3]);
//                    int rs = Integer.parseInt(tokens[4]);
//                    current = new AnimationFrame(ts, rs);
//                    frames.add(current);
//                }else if(tokens[0].equalsIgnoreCase("r")){
//                    int idx = Integer.parseInt(tokens[1]);
//                    int x = Integer.parseInt(tokens[2]);
//                    int y = Integer.parseInt(tokens[3]);
//                    int z = Integer.parseInt(tokens[4]);
//                    int w = Integer.parseInt(tokens[5]);
//                    current.setRotation(idx, new Quaternion(x,y,z,w));
//                }else if(tokens[0].equalsIgnoreCase("t")){
//                    int idx = Integer.parseInt(tokens[1]);
//                    int x = Integer.parseInt(tokens[2]);
//                    int y = Integer.parseInt(tokens[3]);
//                    int z = Integer.parseInt(tokens[4]);
//                    current.setTranslation(idx, new Vec3d(x,y,z));
//                }
//            }
//            fileReader.close();
//            System.out.println("Contents of file:");
//            System.out.println(stringBuffer.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
