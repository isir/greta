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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author Andre-Marie Pez
 */
public class JarMaker {

    private static final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    public static boolean isCompilationEnabled() {
        return javac != null;
    }
    private ArrayList<String> externaldeps = new ArrayList<String>();
    private ArrayList<String> internaldeps = new ArrayList<String>();
    private ArrayList<SourceFile> sourceFiles = new ArrayList<SourceFile>();
    private String mainClass = null;
    private boolean standAlone = false; //is "true" legal ?

    public void addLib(String lib) {
        addLib(lib, false);
    }

    public void addLib(String lib, boolean internal) {
        if (internal) {
            internaldeps.add(lib);
        } else {
            externaldeps.add(lib);
        }
    }

    public void addSource(String fullClassName, String sourceCode) {
        sourceFiles.add(new SourceFile(fullClassName, sourceCode));
    }

    public void addSource(SourceFile source) {
        sourceFiles.add(source);
    }

    public void setMainClass(String fullClassName) {
        mainClass = fullClassName;
    }

    public void setStandAlone(boolean standAlone) {
        this.standAlone = standAlone;
    }

    public void doJar(String jarFileName, boolean addSourceFiles) {
        if (!isCompilationEnabled()) {
            return;
        }
        HeapFileManager fileManager = new HeapFileManager(javac.getStandardFileManager(null, Locale.getDefault(), null));

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        ArrayList<String> options = new ArrayList<String>();
        options.add("-source");
        options.add("6");
        options.add("-target");
        options.add("6");

        boolean succed = javac.getTask(null, fileManager, diagnostics, options, null, sourceFiles).call();

        if (succed) {
            //we can create the new jar file
            try {
                //create the file
                JarOutputStream jarOutputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFileName)));

                //create the manifest
                Manifest man = new Manifest();
                Attributes attributes = man.getMainAttributes();
                attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
                if (mainClass != null) {
                    attributes.put(Attributes.Name.MAIN_CLASS, mainClass);
                }
                String classpath = "";
                if (!standAlone) {
                    for (String lib : internaldeps) {
                        classpath += lib + " ";
                    }
                }
                for (String lib : externaldeps) {
                    classpath += lib + " ";
                }
                if (!classpath.isEmpty()) {
                    attributes.put(Attributes.Name.CLASS_PATH, classpath.trim());
                }
                attributes.putValue("Created-By", "Modular (auto generated)");


                ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF");
                jarOutputStream.putNextEntry(entry);
                man.write(jarOutputStream);
                jarOutputStream.closeEntry();

                //add the compiled files
                for (ClassFile clazz : fileManager.getClasses()) {
                    entry = new ZipEntry(clazz.getFileName());
                    jarOutputStream.putNextEntry(entry);
                    jarOutputStream.write(clazz.getOutputStream().toByteArray());
                    jarOutputStream.closeEntry();
                }
                if(addSourceFiles){
                    for(SourceFile source : sourceFiles){
                        entry = new ZipEntry(source.getFileName());
                        jarOutputStream.putNextEntry(entry);
                        jarOutputStream.write(source.getSourceCode().getBytes());
                        jarOutputStream.closeEntry();
                    }
                }

                //add internal dependencies if standalone
                if (standAlone) {
                    for (String lib : internaldeps) {
                        JarInputStream jarDep = new JarInputStream(new BufferedInputStream(new FileInputStream(lib)));
                        ZipEntry entryDep;
                        while ((entryDep = jarDep.getNextEntry()) != null) {
                            if (!entryDep.isDirectory()) {
                                entry = new ZipEntry(entryDep.getName());
                                jarOutputStream.putNextEntry(entry);
                                ZipMaker.copy(jarDep, jarOutputStream);
                                jarOutputStream.closeEntry();
                            }
                        }

                    }
                }
                entry = new ZipEntry("icon.png");
                jarOutputStream.putNextEntry(entry);
                ZipMaker.copy(this.getClass().getClassLoader().getResourceAsStream(Modular.iconRessourceName), jarOutputStream);
                jarOutputStream.closeEntry();

                //ok it's finished
                jarOutputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.err.println("Error on line " + diagnostic.getLineNumber() + ": " + diagnostic);
            }
        }
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
