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
package greta.application.modular.tools;

import greta.application.modular.ModularXMLFile;
import greta.application.modular.tools.classloader.Factory;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.ProgressMonitor;

/**
 *
 * @author Andre-Marie Pez
 */
public class ToolBox {


    private static interface classChecker {public void check(Class c);}
    private static final List<classChecker> checkers;
    static{
        checkers = new ArrayList<classChecker>();
        checkers.add(new classChecker(){public void check(Class c){ c.getSuperclass(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getInterfaces(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getConstructors(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getDeclaredConstructors(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getMethods(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getDeclaredMethods(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getFields(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getDeclaredFields(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getClasses(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getDeclaredClasses(); }});
        checkers.add(new classChecker(){public void check(Class c){ c.getGenericInterfaces(); }});
    }

    private static URL[] getLibURLs(String... libIDs){
        List<String> pathToLoad = ModularXMLFile.getLibPaths(libIDs);
        URL[] urls = new URL[pathToLoad.size()];
        int i=0;
        for(String path : pathToLoad){
            try {
                urls[i] = new File(path).getCanonicalFile().toURI().toURL();
                ++i;
            } catch (Exception ex) {}
        }
        return urls;
    }

    public static ClassLoader getClassLoaderForLib(String... libIDs){
        return new URLClassLoader(getLibURLs(libIDs));
    }

    public static ClassLoader getClassLoaderForLib_NullParent(String... libIDs){
        return Factory.newClassLoader(getLibURLs(libIDs), null);
    }

    public static List<Method> getMethodsReturning(String refClass, Class returnClass, ClassLoader cl){
        List<Method> methods = new LinkedList<Method>();
        try {
            Class c1 = cl.loadClass(refClass);
            Class c2 = returnClass;
            for(Method m : c1.getMethods()){
                try {
                    Class retyrnType = m.getReturnType();
                    Class[] param = m.getParameterTypes();
                    if(c2.equals(retyrnType) && param!=null && param.length==0){
                        methods.add(m);
                    }
                } catch (Throwable ex) {}
            }
        } catch (Throwable ex) {}
        return methods;
    }


    public static List<Method> getMethodsUsing(Class refClass, Class argClass){
        List<Method> methods = new LinkedList<Method>();
        try {
            Class c1 = refClass;
            Class c2 = argClass;
            for(Method m : c1.getMethods()){
                try {
                    Class[] param = m.getParameterTypes();
                    if(param!=null && param.length==1 && param[0].isAssignableFrom(c2)){
                        methods.add(m);
                    }
                } catch (Throwable ex) {}
            }
        } catch (Throwable ex) {}
        return methods;
    }

    public static List<Method> getMethodsUsing(String refClass, Class argClass, ClassLoader cl){
        try {
            Class c1 = cl.loadClass(refClass);
            return getMethodsUsing(c1, argClass);
        } catch (Exception ex) {
            return new LinkedList<Method>();
        }
    }

    public static List<Method> getMethodsUsing(String refClass, String argClass, ClassLoader cl){
        try {
            Class c1 = cl.loadClass(refClass);
            Class c2 = cl.loadClass(argClass);
            return getMethodsUsing(c1, c2);
        } catch (Exception ex) {
            return new LinkedList<Method>();
        }
    }

    public static List<List<Method>> getMethodBeetween(String class1, String class2, ClassLoader cl){
        List<List<Method>> toReturn = new ArrayList<List<Method>>(2);

        List<Method> c1UsingC2 = getMethodsUsing(class1, class2, cl);
        toReturn.add(c1UsingC2);

        List<Method> c2UsingC1 = getMethodsUsing(class2, class1, cl);
        toReturn.add(c2UsingC1);

        return toReturn;
    }

    public static List<List<Method>> getMethodBeetween(String class1, String lib1, String class2, String lib2){
        return getMethodBeetween(class1, class2, getClassLoaderForLib(lib1, lib2));
    }

    public static List<List<Method>> getMethodBeetween(XMLTree object1, XMLTree object2){
        return getMethodBeetween(object1.getAttribute("class"), object1.getAttribute("lib_id"), object2.getAttribute("class"), object2.getAttribute("lib_id"));
    }

    public static List<Method> getMethodsUsing(XMLTree object1, Class primitiveClass){
        return getMethodsUsing(object1.getAttribute("class"), object1.getAttribute("lib_id"), primitiveClass);
    }

    public static List<Method> getMethodsUsing(String clazz, String lib, Class primitiveClass){
        return getMethodsUsing(clazz, primitiveClass, getClassLoaderForLib(lib));
    }

    public static List<Method> getMethodsReturned(XMLTree object1, Class primitiveClass){
        return getMethodsReturning(object1.getAttribute("class"), object1.getAttribute("lib_id"), primitiveClass);
    }

    public static List<Method> getMethodsReturning(String clazz, String lib, Class primitiveClass){
        return getMethodsReturning(clazz, primitiveClass, getClassLoaderForLib(lib));
    }

    public static boolean inheritsFromJFrame(String class1, String lib1){
        ClassLoader cl = getClassLoaderForLib(lib1);

        try {
            Class c1 = cl.loadClass(class1);
            Class jframeClass = cl.loadClass("javax.swing.JFrame");
            return jframeClass.isAssignableFrom(c1);
        } catch (Exception ex) {}
        return false;
    }

    public static boolean inheritsFromJFrame(XMLTree object1){
        return inheritsFromJFrame(object1.getAttribute("class"), object1.getAttribute("lib_id"));
    }

    public static boolean containsMethodUsing(Class callingClass, String methodName, Class argClass){
        try {
            for(Method m : callingClass.getMethods()){
                if(m.getName().equals(methodName)){
                    Class[] param = m.getParameterTypes();
                    if(param == null){
                        if(argClass == null){
                            return true;
                        }
                    }
                    else{
                        if(param.length==0 && argClass == null){
                            return true;
                        }
                        if(param.length==1 && argClass!=null && param[0].isAssignableFrom(argClass)){
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean containsMethodUsing(XMLTree callingObject, String methodName, Class argClass){
        if(callingObject==null){
            return false;
        }
        ClassLoader cl = getClassLoaderForLib(callingObject.getAttribute("lib_id"));
        try {
            Class callingClass = cl.loadClass(callingObject.getAttribute("class"));
            return containsMethodUsing(callingClass, methodName, argClass);
        } catch (Exception ex) {}
        return false;
    }

    public static boolean containsMethodUsing(XMLTree callingObject, String methodName, XMLTree argObject){
        if(callingObject==null || argObject == null){
            return false;
        }
        ClassLoader cl = getClassLoaderForLib(callingObject.getAttribute("lib_id"), argObject.getAttribute("lib_id"));
        try {
            Class callingClass = cl.loadClass(callingObject.getAttribute("class"));
            Class argClass = cl.loadClass(argObject.getAttribute("class"));
            return containsMethodUsing(callingClass, methodName, argClass);
        } catch (Exception ex) {}
        return false;
    }

    public static boolean containsMethodReturning(Class callingClass, String methodName, Class returnClass){
        try {
            for(Method m : callingClass.getMethods()){
                if(m.getName().equals(methodName)){
                    Class ret = m.getReturnType();
                    Class[] param = m.getParameterTypes();
                    if(returnClass.equals(ret) && param!=null && param.length==0){
                        return true;
                    }
                }
            }
        } catch (Exception ex) {}
        return false;
    }

    public static boolean containsMethodReturning(XMLTree callingObject, String methodName, Class argClass) {
        if(callingObject==null){
            return false;
        }
        ClassLoader cl = getClassLoaderForLib(callingObject.getAttribute("lib_id"));
        try {
            Class callingClass = cl.loadClass(callingObject.getAttribute("class"));
            return containsMethodReturning(callingClass, methodName, argClass);
        } catch (Exception ex) {}
        return false;
    }

    private static void addClassError(List<String> classes, String classError){
        if( ! classes.contains(classError)){
            classes.add(classError);
        }
    }
    private static String formatClassName(String className){
        if(className.startsWith("L")&&className.endsWith(";")){
            className = className.substring(1, className.length()-1);
        }
        if(className.startsWith("[L")&&className.endsWith(";")){
            className = className.substring(2, className.length()-1);
        }
        return className.replace("/", ".");
    }

    public static List<String> checkLibraryLoading(XMLTree lib) {
        return checkLibraryLoading(lib, null);
    }

    public static List<String> checkLibraryLoading(XMLTree lib, ProgressMonitor progress) {
        try {
            JarFile jar = new JarFile(lib.getAttribute("path"));
            Enumeration<JarEntry> entries = jar.entries();
            ClassLoader cl = getClassLoaderForLib_NullParent(lib.getAttribute("id"));
            LinkedList<String> classesMissing = new LinkedList<String>();
            boolean showProgress = progress != null;
            int otherLinkErrors = 0;
            if(showProgress){
                progress.setMinimum(0);
                progress.setMaximum(jar.size());
            }
            int step = 0;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(showProgress){
                    if(progress.isCanceled()){
                        jar.close();
                        return null;
                    }
                    progress.setNote(entry.getName());
                    progress.setProgress(step++);
                }

                if (entry.getName().endsWith(".class")) {
                    String classNameInJar = entry.getName().substring(0, entry.getName().length() - ".class".length()).replace("/", ".");
                    try {
                        Class c = cl.loadClass(classNameInJar);
                        for(classChecker check : checkers){
                            try{
                                check.check(c);
                            }
                            //catch for other check
                            catch(NoClassDefFoundError err){
                                addClassError(classesMissing, formatClassName(err.getMessage()));
                            } catch(TypeNotPresentException exp){
                                addClassError(classesMissing, formatClassName(exp.typeName()));
                            } catch(LinkageError err){
                                otherLinkErrors++;
                            }
                        }
                    }
                    //catch for class loading
                    catch (ClassNotFoundException ex) {
                        addClassError(classesMissing, formatClassName(ex.getMessage()));
                    } catch(NoClassDefFoundError err){
                        addClassError(classesMissing, formatClassName(err.getMessage()));
                    } catch(TypeNotPresentException exp){
                        addClassError(classesMissing, formatClassName(exp.typeName()));
                    } catch(LinkageError err){
                        otherLinkErrors++;
                    }
                }
            }
            jar.close();
            if(otherLinkErrors>0){
                classesMissing.add("Found "+otherLinkErrors+" link error"+(otherLinkErrors>1?"s":""));
            }
            if(classesMissing.size()>0){
                return classesMissing;
            }
        } catch (Throwable ex) {System.out.println(ex);}
        return null;
    }

    public static List<String> findLibrariesFor(List<String> classes) {
        return findLibrariesFor(classes, null);
    }

    public static List<String> findLibrariesFor(List<String> classes, ProgressMonitor progress) {
        LinkedList<String> libs = new LinkedList<String>();
        LinkedList<String> founds = new LinkedList<String>();
        List<XMLTree> libsXML = ModularXMLFile.getLibs().getChildrenElement();
        boolean showProgress = progress != null;
        if (showProgress) {
            progress.setMinimum(0);
            progress.setMaximum(libsXML.size());
        }
        int step = 0;
        for(XMLTree lib : libsXML){
            if (showProgress) {
                if (progress.isCanceled()) {
                    return libs;
                }
                if(lib.isNamed("lib")){
                    progress.setNote(lib.getAttribute("id"));
                }
                progress.setProgress(step++);
            }
            if(lib.isNamed("lib")){
                try {
                    JarFile jar = new JarFile(lib.getAttribute("path"));
                    Enumeration<JarEntry> entries = jar.entries();
                    boolean contains = false;
                    while(entries.hasMoreElements()){
                        JarEntry entry = entries.nextElement();
                        if(entry.getName().endsWith(".class")){
                            String classNameInJar = entry.getName().substring(0, entry.getName().length()-".class".length()).replace("/", ".");
                            if(classes.contains(classNameInJar)){
                                founds.add(classNameInJar);
                                contains = true;
                            }
                        }
                    }
                    if(contains){
                        libs.add(lib.getAttribute("id"));
                    }
                } catch (Exception ex) {}
            }
        }
        classes.removeAll(founds);
        return libs;
    }

}
