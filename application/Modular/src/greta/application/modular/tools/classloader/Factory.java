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
package greta.application.modular.tools.classloader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 *
 * @author Andre-Marie Pez
 */
public class Factory {
    private static boolean useCustom;

    protected static Constructor FileURLMapper_constructor_URL;
    protected static Method FileURLMapper_exists;
    protected static Method FileURLMapper_getPath;
    protected static Method ParseUtil_decode;
    protected static Method ParseUtil_encodePath;
    protected static Method ParseUtil_fileToEncodedURL;
    protected static Class FileURLConnection;
    protected static Method PerfCounter_addElapsedTimeFrom;
    protected static Method PerfCounter_getReadClassBytesTime;
    protected static Class ByteBuffered;
    protected static Method ByteBuffered_getByteBuffer;
    protected static Method MetaIndex_forJar_File;
    protected static Method MetaIndex_mayContain_String;
    protected static Method JarIndex_getJarIndex_JarFile_MetaIndex;
    protected static Method JarIndex_get_String;
    protected static Method JarIndex_merge_JarIndex_String;
    protected static Method JarIndex_getJarFiles;

    static {
        useCustom = false;
        try {
            Class FileURLMapper = Class.forName("sun.misc.FileURLMapper");
            FileURLMapper_constructor_URL = FileURLMapper.getConstructor(URL.class);
            FileURLMapper_exists = FileURLMapper.getMethod("exists");
            FileURLMapper_getPath = FileURLMapper.getMethod("getPath");
            Class ParseUtil = Class.forName("sun.net.www.ParseUtil");
            ParseUtil_decode = ParseUtil.getMethod("decode", String.class);
            ParseUtil_encodePath = ParseUtil.getMethod("encodePath", String.class, boolean.class);
            ParseUtil_fileToEncodedURL = ParseUtil.getMethod("fileToEncodedURL",File.class);
            FileURLConnection = Class.forName("sun.net.www.protocol.file.FileURLConnection");
            Class PerfCounter = Class.forName("sun.misc.PerfCounter");
            PerfCounter_addElapsedTimeFrom = PerfCounter.getMethod("addElapsedTimeFrom", long.class);
            PerfCounter_getReadClassBytesTime = PerfCounter.getMethod("getReadClassBytesTime");
            ByteBuffered = Class.forName("sun.nio.ByteBuffered");
            ByteBuffered_getByteBuffer = ByteBuffered.getMethod("getByteBuffer");
            Class MetaIndex = Class.forName("sun.misc.MetaIndex");
            MetaIndex_forJar_File = MetaIndex.getMethod("forJar", File.class);
            MetaIndex_mayContain_String = MetaIndex.getMethod("mayContain", String.class);
            Class JarIndex = Class.forName("sun.misc.JarIndex");
            JarIndex_getJarIndex_JarFile_MetaIndex = JarIndex.getMethod("getJarIndex", JarFile.class, MetaIndex);
            JarIndex_get_String = JarIndex.getMethod("get", String.class);
            JarIndex_merge_JarIndex_String = JarIndex.getMethod("merge", JarIndex, String.class);
            JarIndex_getJarFiles = JarIndex.getMethod("getJarFiles");

            useCustom = true;
        } catch (Throwable t){
        }
    }

    public static ClassLoader newClassLoader(URL[] url, ClassLoader parent){
        if(useCustom){
            return new CustomURLClassLoader(url, parent);
        }
        else {
            return new URLClassLoader(url, parent);
        }
    }
}
