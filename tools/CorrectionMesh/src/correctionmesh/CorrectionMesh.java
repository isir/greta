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
