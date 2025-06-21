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
