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
package greta.application.modular.modules;

import greta.core.util.log.Logs;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 * @author Andre-Marie Pez
 */
public class Library {

    private String id;
    private String libFile;
    private List<Library> dependencies;
    private List<String> neededFiles;

    private Library(String id, String libFileName){
        this.id = id;
        this.libFile = libFileName;
        dependencies = new ArrayList<Library>();
        neededFiles = new ArrayList<String>();
    }

    public String getId(){
        return id;
    }

    public String getFileName(){
        return libFile;
    }

    // dependencies part
    public void addDependency(Library dependency){
        if(dependency!=null && !isDependentOf(dependency)){
            dependencies.add(dependency);
        }
    }

    public List<Library> getDependencies(){
        return dependencies;
    }

    public List<Library> getAllDependencies(){
        Parser p = new Parser();
        p.parseDependencies(this);
        return p.getParsedLibraries();
    }

    public List<String> getDependenciesFileNames(){
        return new FileNamesParser().parseDependencies(this);
    }

    public List<String> getDependenciesIds(){
        return new IdParser().parseDependencies(this);
    }

    public boolean isDependentOf(Library dependency){
        return new DependencyFinder(dependency).parseDependencies(this);
    }

    // needed files part
    public void addFile(String fileName){
        if(fileName != null && !neededFiles.contains(fileName)) {
            neededFiles.add(fileName);
        }
    }

    public List<String> getNeededFiles(){
        return neededFiles;
    }

    public List<String> getAllNeededFiles(){
        return new NeededFileNamesParser().parseDependencies(this);
    }

    // both files and libs
    public List<String> getAllNeededFilesAndLibs(){
        return new NeededFileAndDependenciesParser().parseDependencies(this);
    }

    public void load() {
        ClassLoader systemCL = ClassLoader.getSystemClassLoader();
        if (systemCL instanceof URLClassLoader) {
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                method.setAccessible(true);
                method.invoke(systemCL, new Object[]{getURLFromPath(libFile)});
                System.setProperty("java.class.path", System.getProperty("java.class.path")+";"+(new File(libFile).getCanonicalPath()));//needed to compile
            } catch (Exception ex) {
                Logs.error("Fail to load library: \"%s\"\n"+ id);
            }
        }
    }

    public static boolean jarContainsClass(String jarPath, String className){
        JarFile jar;
        try {
            jar = new JarFile(jarPath);
        } catch (Exception ex) {
            return false;
        }
        return jar.getJarEntry(className.replace(".", "/").concat(".class")) != null;
    }
    public boolean containsClass(String className){
        return jarContainsClass(libFile, className);
    }

    private static URL getURLFromURI(URI uri)
            throws MalformedURLException {
        return uri.toURL();
    }

    private static URL getURLFromFile(File f)
            throws MalformedURLException, IOException {
        return getURLFromURI(f.getCanonicalFile().toURI());
    }

    private static URL getURLFromPath(String path)
            throws MalformedURLException, IOException {
        return getURLFromFile(new File(path));
    }

    // parsers
    private static class Parser <R>{
        R result;
        protected ArrayList<Library> callers = new ArrayList<Library>();

        public final R parseDependencies(Library toParse){
            if( ! callers.contains(toParse)){
                callers.add(toParse);
                fill(toParse);
                for(Library dep : toParse.dependencies){
                    parseDependencies(dep);
                }
            }
            return getResult();
        }

        public void fill(Library with){}

        public final List<Library> getParsedLibraries(){
            return callers;
        }

        public R getResult(){
            return result;
        }

    }

    private static class DependencyFinder extends Parser<Boolean>{
        Library dep;
        DependencyFinder(Library libToCheck){
            dep = libToCheck;
            result = false;
        }
        @Override
        public void fill(Library with){
            result = result || with.equals(dep);
        }
    }

    private static class IdParser extends Parser<List<String>>{
        IdParser(){
            result = new ArrayList<String>();
        }
        @Override
        public void fill(Library with){
            result.add(with.id);
        }
    }

    private static class FileNamesParser extends Parser<List<String>>{
        FileNamesParser(){
            result = new ArrayList<String>();
        }
        @Override
        public void fill(Library with){
            result.add(with.libFile);
        }
    }

    private static class NeededFileNamesParser extends Parser<List<String>>{
        public NeededFileNamesParser(){
            result = new ArrayList<String>();
        }
        @Override
        public void fill(Library with){
            for(String neededFiles : with.neededFiles){
                addFile(neededFiles);
            }
        }
        protected void addFile(String fileName){
            if( ! result.contains(fileName)){
                result.add(fileName);
            }
        }
    }

    private static class NeededFileAndDependenciesParser extends NeededFileNamesParser{
        @Override
        public List<String> getResult() {
            for(Library lib : callers){
                addFile(lib.libFile);
            }
            return super.getResult();
        }
    }

    @Override
    public String toString() {
        return "Library id=\""+id+"\" path=\""+libFile+"\" dependencies:"+dependencies.size()+" files:"+neededFiles.size();
    }


    //STATIC FIELDS

    private static ArrayList<Library> knownLibraries = new ArrayList<Library>();

    public static Library createLibrary(String id, String fileName){
        Library lib = new Library(id, fileName);
        knownLibraries.add(lib);
        return lib;
    }

    public static boolean isKnown(String libId){
        for(Library lib : knownLibraries){
            if(libId.equals(lib.id)){
                return true;
            }
        }
        return false;
    }

    public static Library getLibrary(String libId){
        for(Library lib : knownLibraries){
            if(libId.equals(lib.id)){
                return lib;
            }
        }
        return null;
    }

    public static List<Library> getAllDependenciesFor(List<Library> libs){
        Parser p = new Parser();
        for(Library lib : libs){
            p.parseDependencies(lib);
        }
        return p.getParsedLibraries();
    }


}
