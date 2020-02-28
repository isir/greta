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
package correctionmesh;

import greta.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie
 */
public class CorrectionMesh {

    //path from bin
    public static String ogreXMLConverter1dot6 = "../tools/CorrectionMesh/ogreXMLConverter/1.6/OgreXMLConverter.exe";
    public static String ogreXMLConverter1dot7 = "../tools/CorrectionMesh/ogreXMLConverter/1.7/OgreXMLConverter.exe";
    public static String ogreXMLConverterArgs = " -e -r -q ";

    public static void saveAndConvert1dot6(XMLTree mesh, String fileName){
        mesh.save(fileName);
        exec(ogreXMLConverter1dot6+ogreXMLConverterArgs+fileName, true);
    }

    public static void convert1dot6(String fileName){
        exec(ogreXMLConverter1dot6+ogreXMLConverterArgs+fileName, true);
    }

    public static void convert1dot7(String fileName){
        exec(ogreXMLConverter1dot7+ogreXMLConverterArgs+fileName, true);
    }

    public static void startModular(){
        exec("java -jar Modular.jar", false);
    }

    private static void exec(String commandLine, boolean wait){

        try {
            Process p = Runtime.getRuntime().exec(commandLine);
            while(wait){
                try{
                    p.exitValue();
                    wait = false; //end of process
                }
                catch(Exception e){
                    int r = 0;
                    while(r!=-1){
                        if(r!=-1){
                            r = p.getErrorStream().read();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] aaa){
        startModular();
    }
}
