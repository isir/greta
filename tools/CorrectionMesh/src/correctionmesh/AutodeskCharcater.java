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

import correctionmesh.util.Bone;
import correctionmesh.util.Mesh;
import correctionmesh.util.OgreXML;
import correctionmesh.util.SubMesh;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class AutodeskCharcater {

    static private String playerMediaPath = "./Player/Data/media/";


    static private String characterName = "Ingrid";
    static private String baseName = "body";

    static private String exportPath = playerMediaPath + characterName.toLowerCase()+"/export/";
    static private String localTargetPath = characterName.toLowerCase();
    static private String bodyMaterialAfter = characterName+"_Autodesk_Body";
    static private String hairMaterialAfter = characterName+"_Autodesk_Hair";


    static private String inputPath = exportPath+"mesh/";
    static private String bitmapsPath = exportPath+"bitmap/";
    static private String targetName = localTargetPath+"/"+baseName;
    static private String targetPath = playerMediaPath+localTargetPath;
    static private String bodyMaterialBefore = "_Body";
    static private String hairMaterialBefore = "Hair";

    static private String newmeshFile = playerMediaPath+targetName + ".mesh";
    static private String skeltonName = targetName + ".skeleton";
    static private String meterialFile = playerMediaPath+targetName + ".material";
    static private String newskeletonFile = playerMediaPath + skeltonName;


    public static void main(String[] aaa){
        if(!baseName.equalsIgnoreCase("body")){
            bodyMaterialAfter =
                    characterName+"_"+
                    baseName.substring(0, 1).toUpperCase()+baseName.substring(1)+
                    "_Autodesk_Body";
        }
        XMLParser parser = XML.createParser();
        List<Bone> skeletons = new ArrayList<Bone>();
        Mesh m = new Mesh();

        for(File f : new File(inputPath).listFiles()){
            if(f.getName().endsWith(".mesh") || f.getName().endsWith(".skeleton")){
                System.out.println("Convert file "+f.getName()+" to XML");
                CorrectionMesh.convert1dot7(f.getAbsolutePath());
            }
        }
        System.gc();

        for(File f : new File(inputPath).listFiles()){
            if(f.getName().endsWith(".mesh.xml")){
                System.out.println("Read File "+ f.getName());
                Mesh futurSubmesh = OgreXML.readMesh(parser.parseFile(f.getAbsolutePath()));
                System.out.println("\tLoading Skeleton");
                Bone skel = OgreXML.readSkeleton(parser.parseFile(inputPath+futurSubmesh.getSkeleton()+".xml"));
                skeletons.add(skel);
                System.out.println("\tRemove duplicate vertices");
                for(int i=0; i< futurSubmesh.getNumberOfSubMeshes(); ++i){
                    SubMesh sm = futurSubmesh.getSubMesh(i);
                    System.out.println("\tvertices before : "+sm.vertexBuffer.size());
                    sm.mergeVertices(true, true, true, true);
                    System.out.println("\tvertices after : "+sm.vertexBuffer.size());

                }
                System.out.println("\tTranslate");
                futurSubmesh.translate(-skel.getCoordinateX(), -skel.getCoordinateY(), -skel.getCoordinateZ());
                System.out.println("\tMerge meshes");
                for(int i=0; i< futurSubmesh.getNumberOfSubMeshes(); ++i){
                    SubMesh sub = futurSubmesh.getSubMesh(i);
                    if(sub.material.equals(bodyMaterialBefore)){
                        sub.material = bodyMaterialAfter;
                    }
                    if(sub.material.equals(hairMaterialBefore)){
                        sub.material = hairMaterialAfter;
                    }
                    m.addSubMesh(sub);
                }
                System.gc();
            }
        }


        m.setSkeleton(skeltonName);


        Bone skeleton= skeletons.get(0);


        skeleton.setCoordinates(0,0,0);

        System.out.println("Rescale mesh");
        m.scale(1/skeleton.getScaleX(), 1/skeleton.getScaleY(), 1/skeleton.getScaleZ());
        skeleton.setScale(1, 1, 1);


        System.out.println("Rotate mesh");
        Quaternion q  = new Quaternion(new Vec3d(1,0,0), java.lang.Math.PI/2.0);
        for(int i=0; i< m.getNumberOfSubMeshes(); ++i){
            m.getSubMesh(i).rotate(q);
        }
        skeleton.setOrientation(Quaternion.multiplication(q, skeleton.getOrientation()));

        System.out.println("Save skeleton as "+ newskeletonFile);
        CorrectionMesh.saveAndConvert1dot6(OgreXML.writeSkeleton(skeleton), newskeletonFile+".xml");


        System.out.println("Save XML");
        OgreXML.writMesh(m).save(newmeshFile+".xml");
        System.out.println("Convert XML "+ newmeshFile);
        CorrectionMesh.convert1dot6(newmeshFile+".xml");

        createMaterial();

        System.out.println("delete XML files");
        new File(newmeshFile+".xml").delete();
        new File(newskeletonFile+".xml").delete();

        System.out.println("Done. ");
    }

    private static void createMaterial(){
        String colorSuffix = "color.jpg";
        String nmSuffix = "nm.jpg";
        String specSuffix = "spec.jpg";
        String hairColorSuffix = "wig_color.jpg";
        String hairNmSuffix = "wig_nm.jpg";
        String hairSpecSuffix = "wig_spec.jpg";

        String colorSuffixFinal = "color.jpg";
        String nmSuffixFinal = "nm.jpg";
        String specSuffixFinal = "spec.jpg";
        String hairColorSuffixFinal = "hair_color.jpg";
        String hairNmSuffixFinal = "hair_nm.jpg";
        String hairSpecSuffixFinal = "hair_spec.jpg";
        if(!baseName.equalsIgnoreCase("body")){
            colorSuffixFinal = baseName+"_color.jpg";
            nmSuffixFinal = baseName+"_nm.jpg";
            specSuffixFinal = baseName+"_spec.jpg";
        }
        //find bitmaps
        File[] images = new File(bitmapsPath).listFiles();
        File hairColor = null;
        String prefix = null;
        for(File imageFile : images){
            if(imageFile.getName().endsWith(hairColorSuffix)){
                hairColor = imageFile;
                prefix = hairColor.getName().substring(0, hairColor.getName().length() - hairColorSuffix.length());
            }
        }
        if(prefix == null){
            for(File imageFile : images){
                if(imageFile.getName().endsWith(colorSuffix)){
                    prefix = imageFile.getName().substring(0, imageFile.getName().length() - colorSuffix.length());
                }
            }
        }
        if(prefix != null){
            tryToCopyFile(bitmapsPath+prefix+colorSuffix, targetPath+"/"+colorSuffixFinal);
            tryToCopyFile(bitmapsPath+prefix+nmSuffix, targetPath+"/"+nmSuffixFinal);
            tryToCopyFile(bitmapsPath+prefix+specSuffix, targetPath+"/"+specSuffixFinal);

            String hairMaterial = getHairMaterial(hairColor);
            if(hairMaterial!=null){
                if(hairMaterial.isEmpty()){
                    tryToCopyFile(bitmapsPath+prefix+hairColorSuffix, targetPath+"/"+hairColorSuffixFinal);
                    tryToCopyFile(bitmapsPath+prefix+hairNmSuffix, targetPath+"/"+hairNmSuffixFinal);
                    tryToCopyFile(bitmapsPath+prefix+hairSpecSuffix, targetPath+"/"+hairSpecSuffixFinal);
                }
            }

            System.out.println("create Material "+ meterialFile);
            try {
                FileWriter out = new FileWriter(meterialFile);
                out.write("import * from \"autodesk.material\"\n");
                out.write("\n");
                out.write("material "+bodyMaterialAfter+" : base {\n");
                out.write("\tset_texture_alias diffuseMap "+localTargetPath+"/"+colorSuffixFinal+"\n");
                out.write("\tset_texture_alias celShadingMap "+localTargetPath+"/"+colorSuffixFinal+"\n");
                out.write("}\n");
                if(hairMaterial!=null){
                    out.write("material "+hairMaterialAfter+" : ");
                    if(hairMaterial.isEmpty()){
                        out.write("Autodesk_Hair {\n");
                        out.write("\tset_texture_alias diffuseMap "+localTargetPath+"/"+hairColorSuffixFinal+"\n");
                        out.write("\tset_texture_alias celShadingMap "+localTargetPath+"/"+hairColorSuffixFinal+"\n");
                        out.write("}\n");
                    }
                    else{
                        out.write(hairMaterial+" {}\n");
                    }
                }
                out.write("\n");
                out.close();
            } catch (Exception ex) {
            }

        }
    }

    private static void tryToCopyFile(String from, String to){
        File f = new File(from);
        File t = new File(to);
        if(f.exists()){
            try {
                System.out.println("Copy file "+from+" to "+to);
                Files.copy(f.toPath(), t.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("Copy fails");
            }
        }
    }

    private static String getHairMaterial(File hairImageFile){
        if(hairImageFile == null){
            return null;
        }

        //hard code:
        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/blond1.jpg"))){
            return "Autodesk_Blond_Hair_1";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/blond2.jpg"))){
            return "Autodesk_Blond_Hair_2";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/brown1.jpg"))){
            return "Autodesk_Brown_Hair_1";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/brown2.jpg"))){
            return "Autodesk_Brown_Hair_2";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/dark_brown1.jpg"))){
            return "Autodesk_Dark_Brown_Hair_1";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/dark_brown2.jpg"))){
            return "Autodesk_Dark_Brown_Hair_2";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/black1.jpg"))){
            return "Autodesk_Black_Hair_1";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/black2.jpg"))){
            return "Autodesk_Black_Hair_2";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/red1.jpg"))){
            return "Autodesk_Red_Hair_1";
        }

        if(compareFiles(hairImageFile, new File(playerMediaPath+"autodesk_hair/red2.jpg"))){
            return "Autodesk_Red_Hair_2";
        }

        return "";
    }

    private static boolean compareFiles(File f1, File f2){
        try{
            FileInputStream in1 = new FileInputStream(f1);
            FileInputStream in2 = new FileInputStream(f2);
            int read1 = 0;
            int read2 = 0;
            while(read1==read2 && read1!=-1 && read2!=-1){
                read1 = in1.read();
                read2 = in2.read();
            }
            in1.close();
            in2.close();
            return read1==read2 && read1==-1;
        }
        catch(Exception e){
            return false;
        }
    }
}
