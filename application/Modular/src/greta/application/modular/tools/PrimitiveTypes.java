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

import java.lang.reflect.Method;

/**
 *
 * @author Andre-Marie Pez
 */
public class PrimitiveTypes {

    private static int classIndex = 0;
    private static int xmlNameIndex = 1;
    private static int iniMagagerCallIndex = 2;
    private static int evaluationClass = 3;
    private static int evaluationFunction = 4;

    private static Object[][] map = {
        //class, xml name, IniMagager call, evaluation function (must be static and use only one String as argument)
        {String.class, "string", "getValueString", PrimitiveTypes.class, "assertString"},
        {boolean.class, "boolean", "getValueBoolean", PrimitiveTypes.class, "assertBoolean"},
        {byte.class, "byte", "getValueInt", Byte.class, "valueOf"},
        {short.class, "short", "getValueInt", Short.class,"valueOf"},
        {int.class, "integer", "getValueInt", Integer.class, "valueOf"},
        {long.class, "long", "getValueInt", Long.class, "valueOf"},
        {float.class, "float", "getValueDouble", Float.class, "valueOf"},
        {double.class, "double", "getValueDouble", Double.class, "valueOf"}};

    public static Class getClassOf(String name) {
        return (Class) map[indexOf(name, xmlNameIndex)][classIndex];
    }

    public static String getNameOf(Class clazz) {
        return (String) map[indexOf(clazz, classIndex)][xmlNameIndex];
    }

    public static String getIniManagerCall(String name) {
        return (String) map[indexOf(name, xmlNameIndex)][iniMagagerCallIndex];
    }

    public static String[] getNames(){
        String[] names = new String[map.length];
        for (int i = 0; i < map.length; ++i) {
            names[i] = (String) map[i][xmlNameIndex];
        }
        return names;
    }

    public static Class[] getClasses(){
        Class[] classes = new Class[map.length];
        for (int i = 0; i < map.length; ++i) {
            classes[i] = (Class) map[i][classIndex];
        }
        return classes;
    }

    private static int indexOf(Object toSearch, int column) {
        for (int i = 0; i < map.length; ++i) {
            if (map[i][column].equals(toSearch)) {
                return i;
            }
        }
        return 0;
    }

    public static boolean isCorrectValue(String className, String value){
        return isCorrectValue(indexOf(className, xmlNameIndex), value);
    }

    public static boolean isCorrectValue(Class clazz, String value){
        return isCorrectValue(indexOf(clazz, classIndex), value);
    }

    private static boolean isCorrectValue(int index, String value){
        try{
            Method m = ((Class)map[index][evaluationClass]).getMethod((String)map[index][evaluationFunction], String.class);
            m.invoke(null, value); //suppose to crash if it is not correct ?
            return true;
        } catch(Throwable t) {}
        return false;
    }

    public static void assertString(String s){}
    public static void assertBoolean(String s) throws Exception{
        if( ! ("true".equals(s) || "false".equals(s))){
            throw new Exception();
        }
    }
}
