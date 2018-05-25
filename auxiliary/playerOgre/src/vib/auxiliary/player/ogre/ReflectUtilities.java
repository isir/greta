/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre;

import java.awt.Component;
import java.awt.Container;

/**
 * Contains some functions to access to non-public api.
 * @author Andre-Marie Pez
 */
public class ReflectUtilities {

    /**
     * Returns the window identifier of an AWT {@code Component}<br/>
     * Works on sun's JVM. Maybe on an other one but it's not sure.
     * @param comp an AWT {@code Component}
     * @return the window id of the {@code Component}
     */
    public static String getWindowId(Component comp) {
        try {
            //try to get the Component Peer
            Object componentPeer = invokeMethod(comp, "getPeer");
            try { //windows
                return "" + ((Long) invokeMethod(componentPeer, "getHWnd"));
            } catch (Exception ex1) { //not windows
                try { //linux
                    return "" + ((Long) invokeMethod(componentPeer, "getWindow"));
                } catch (Exception ex2) { //mac ?
                    vib.core.util.log.Logs.error("can not get window handle");
                }
            }
        } catch (Exception ex) {
            vib.core.util.log.Logs.error("can not get component peer");
        }
        return "";
    }

    /**
     * Tries to unregister an AWT {@code Component} from the graphic thread.<br/>
     * It only used when closing the Ogre's primary window. We don't want to destroy it
     * (it must be alive during the whole process) but AWT must ignor it.<br/>
     * Be carefull, this function calls Sun's specific methods.
     * @param c an AWT {@code Component}
     */
    public static void unregister(Component c){
        if(c instanceof Container){
            Container cc = (Container)c;
            for(Component child : cc.getComponents()){
                unregister(child);
            }
        }
//        invokeMethod(AWTAutoShutdown.getInstance(),"unregisterPeer", c, AWTAccessor.getComponentAccessor().getPeer(c));
        try {
            Object awtAutoShutdownInstance = invokeStaticMethod("sun.awt.AWTAutoShutdown", "getInstance");
            Object componentAccessor = invokeStaticMethod("sun.awt.AWTAccessor", "getComponentAccessor");
            invokeDeclaredMethod(awtAutoShutdownInstance, "unregisterPeer", c, invokeDeclaredMethod(componentAccessor, "getPeer", c));
        } catch (Exception ex) {}
    }

    /**
     * Tries to find a specific class knowing its name.
     * @param className the full class name.
     * @return the {@code Class} object.
     * @throws ClassNotFoundException if the class is not found
     */
    private static Class getClass(String className) throws ClassNotFoundException{
        return ClassLoader.getSystemClassLoader().loadClass(className);
    }

    /**
     * Reflectivity shortcut.<br/>
     * Calls a public method, it may be inherited for super class.
     * @param o the object from which the method is called.
     * @param methodName the name of the method.
     * @param args the argument of the method.
     * @return the result of the method.
     * @throws Exception if there is something wrong...
     */
    private static Object invokeMethod(Object o, String methodName, Object ... args) throws Exception{
        Class c = o.getClass();
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(methodName)) {
                m.setAccessible(true);
                    Object ret = m.invoke(o, args);
                    return ret;
            }
        }
        throw new Exception("Could not find method \"" + methodName + "\"");
    }

    /**
     * Reflectivity shortcut.<br/>
     * Calls a public or protected method, it can not be inherited for super class.
     * @param o the object from which the method is called.
     * @param methodName the name of the method.
     * @param args the argument of the method.
     * @return the result of the method.
     * @throws Exception if there is something wrong...
     */
    private static Object invokeDeclaredMethod(Object o, String methodName, Object ... args) throws Exception{
        Class c = o.getClass();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                m.setAccessible(true);
                    Object ret = m.invoke(o, args);
                    return ret;
            }
        }
        throw new Exception("Could not find method \"" + methodName + "\"");
    }

    /**
     * Reflectivity shortcut.<br/>
     * Calls a public or protected static method.
     * @param className the class name from which the method is called.
     * @param methodName the name of the method
     * @param args the argument of the method.
     * @return the result of the method.
     * @throws Exception if there is something wrong...
     */
    private static Object invokeStaticMethod(String className, String methodName, Object ... args) throws Exception{
        Class c = getClass(className);
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                m.setAccessible(true);
                    Object ret = m.invoke(null, args);
                    return ret;
            }
        }
        throw new Exception("Could not find method \"" + methodName + "\"");
    }
}
