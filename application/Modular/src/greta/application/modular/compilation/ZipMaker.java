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
package greta.application.modular.compilation;

import greta.application.modular.Modular;
import greta.core.util.IniManager;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Andre-Marie Pez
 */
public class ZipMaker {


    private ArrayList<String> files = new ArrayList<String>();

    public void addFile(String fileName) {
        if(!files.contains(fileName)) {
            files.add(fileName);
        }
    }

    public void doZip(final String zipFileName, final boolean showProgress, final Component parent) {
        SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                String zipSimpleName = getRelativePathInTheCurrentDir(zipFileName);
                try {
                    ProgressMonitor progress = null;
                     if(showProgress){
                        progress = new ProgressMonitor(parent, zipSimpleName, "", 0, 1);
                        progress.setMillisToPopup(0);
                        progress.setMillisToDecideToPopup(0);
                    }
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
                    List<String> allfiles = getAllFilesToAdd(files);
                    if(showProgress){
                        progress.setMaximum(allfiles.size());
                    }
                    int step = 0;
                    for(String fileName : allfiles){
                        String entryName = getRelativePathInTheCurrentDir(fileName);
                        if(showProgress){
                            if(progress.isCanceled()){
                                zipOutputStream.close();
                                progress.close();
                                return null;
                            }
                            progress.setNote(entryName);
                            progress.setProgress(step++);
                        }
                        if(entryName!=null){
                            try{
                                ZipEntry entry = new ZipEntry(entryName);
                                zipOutputStream.putNextEntry(entry);
                                if(new File(fileName).isFile()){
                                    InputStream is = new BufferedInputStream(new FileInputStream(fileName));
                                    copy(is, zipOutputStream);
                                    is.close();
                                }
                                zipOutputStream.closeEntry();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    zipOutputStream.close();
                    if(showProgress){
                        progress.close();
                        JOptionPane.showMessageDialog(parent, zipSimpleName+" Done!", null, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Modular.icon));
                    }

                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if(showProgress){
                    JOptionPane.showMessageDialog(parent, zipSimpleName+" KO :(", "ko :(", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

        };
        task.execute();
    }


    public static List<String> getAllFilesToAdd(List<String> files){
        List<String> all = new LinkedList<String>();

        /* old good part
        for(String regexPath : files){
            for(String resolved : findCorrespondingFiles(regexPath)){
                try {
                    resolved = new File(resolved).getCanonicalPath();
                    if(!all.contains(resolved)) {
                        all.add(resolved);
                    }
                }
                catch (IOException ex) {ex.printStackTrace();}
            }
        }
        //*/ //end of old part


        //* test a new part

        String fullRegex = "";
        for(String regexPath : files){
            if(regexPath.contains("?")||regexPath.contains("*")){
                if( ! fullRegex.isEmpty()){
                    fullRegex += "|";
                }
                fullRegex += generatesJavaRegex(regexPath);
            }
            else{
                try {
                    regexPath = new File(regexPath).getCanonicalPath();
                    if(!all.contains(regexPath)) {
                        all.add(regexPath);
                    }
                }
                catch (IOException ex) {ex.printStackTrace();}
            }

        }
        if( ! fullRegex.isEmpty()){
            LinkedList<String> founds = new LinkedList<String>();
            findRecurse(new File(IniManager.getProgramPath()), Pattern.compile(fullRegex), founds);
            for(String regexPath : founds){
                try {
                    regexPath = new File(regexPath).getCanonicalPath();
                    if(!all.contains(regexPath)) {
                        all.add(regexPath);
                    }
                }
                catch (IOException ex) {ex.printStackTrace();}
            }
        }
        //*/ //end of new part
        return all;
    }

    private String getRelativePathInTheCurrentDir(String aPath){
        URI uri = new File(IniManager.getProgramPath()).toURI().relativize(new File(aPath).toURI());
        if(uri.isAbsolute()){
            System.out.println(aPath+" can not be added");
            return null; //not in the bin and cannot be added in the zip
        }
        return uri.getPath();
    }

    private static List<String> findCorrespondingFiles(String regexFileName){
        List<String> founds = new LinkedList<String>();
        if(regexFileName.contains("?")||regexFileName.contains("*")){

            int pos = regexFileName.replaceAll("[\\ \\?]", "*").indexOf("*");
            if(pos<0){
                pos = regexFileName.length();
            }
            String base = regexFileName.substring(0, pos);
            File f = new File(base);
            if( ! f.exists() || ! f.isDirectory()){
                f = f.getParentFile();
            }
            if(f ==null){
                f = new File(IniManager.getProgramPath());
            }
            regexFileName = (new File(IniManager.getProgramPath()).toURI().resolve(base).toString()+regexFileName.substring(pos)).
                    replaceAll("\\\\", "/").
                    replaceAll("\\.", "\\\\.").
                    replaceAll("\\*\\*", "\\\\DotStar\\\\").
                    replaceAll("\\*", "[^/]*").
                    replaceAll("\\\\DotStar\\\\", ".*").
                    replaceAll("\\?", ".");
//            System.out.println(regexFileName+" "+f);
            Pattern pattern = Pattern.compile(regexFileName);
            findRecurse(f, pattern, founds);
        }
        else{
            founds.add(regexFileName);
        }
        return founds;
    }

    private static String generatesJavaRegex(String regexFileName){
        int pos = regexFileName.replaceAll("[\\ \\?]", "*").indexOf("*");
        if(pos<0){
            pos = regexFileName.length();
        }
        regexFileName = (new File(IniManager.getProgramPath()).toURI().resolve(regexFileName.substring(0, pos)).toString()+regexFileName.substring(pos)).
                replaceAll("\\\\", "/").
                replaceAll("\\.", "\\\\.").
                replaceAll("\\*\\*", "\\\\DotStar\\\\").
                replaceAll("\\*", "[^/]*").
                replaceAll("\\\\DotStar\\\\", ".*").
                replaceAll("\\?", ".");
        return regexFileName;
    }

    private static void findRecurse(File dir, Pattern pattern,  List<String> founds){
        for(File f : dir.listFiles()){
            if(f.isDirectory()){
                findRecurse(f, pattern, founds);
            }
            else{
                if(pattern.matcher(f.toURI().normalize().toString().replaceAll("%20", " ")).matches()) {
                    founds.add(f.getAbsolutePath());
                }
            }
        }
    }

    static void copy(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = in.read(buffer, 0, buffer.length)) > -1) {
            out.write(buffer, 0, read);
            if (read < buffer.length) {
                break;
            }
        }
    }
}
