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
package greta.core.util.file;

import greta.core.util.IniManager;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Andre-Marie Pez
 */
public class TempFileManager {

    private static File tempDir; //temp directory
    private static ArrayList<String> usedPrefixes; //contains all known prefixes
    private static long DEFAULT_LIVE_TIME = -1;
    static{
        tempDir = new File(IniManager.getProgramPath()+"./temp/");
        usedPrefixes = new ArrayList<String>();

        if((!tempDir.exists()) || (!tempDir.isDirectory())){
            tempDir.mkdir();
        }
    }


    private static String getUnicPrefix(String wantedPrefix){
        if( ! isValidPrefix(wantedPrefix)){
            wantedPrefix = "temp";
        }
        if(isPrefixConfused(wantedPrefix)){
            return getUnicPrefix("_"+wantedPrefix);
        }
        usedPrefixes.add(wantedPrefix.toLowerCase());
        return wantedPrefix;
    }

    private static boolean isValidPrefix(String wantedPrefix){
        if(wantedPrefix==null || wantedPrefix.isEmpty()){
            return false;
        }
        for(int i=0; i< wantedPrefix.length(); ++i){
            if(wantedPrefix.charAt(i) != '_') {
                return true;
            }
        }
        return false;
    }

    private static boolean isPrefixConfused(String wantedPrefix){
        String wantedToLowerCase = wantedPrefix.toLowerCase();
        for(String usedPrefix : usedPrefixes){
            if(usedPrefix.startsWith(wantedToLowerCase) || wantedToLowerCase.startsWith(usedPrefix)){
                return true;
            }
        }
        return false;
    }

    private String prefix;
    private String suffix;
    private long defaultDelay; //a negative value means "when java shuts down"

    public TempFileManager(){
        this(null, null, DEFAULT_LIVE_TIME);
    }

    public TempFileManager(String prefix){
        this(prefix, null, DEFAULT_LIVE_TIME);
    }

    public TempFileManager(String prefix, String suffix){
        this(prefix, suffix, DEFAULT_LIVE_TIME);
    }

    public TempFileManager(String prefix, long defaultDelay){
        this(prefix, null, defaultDelay);
    }

    public TempFileManager(String prefix, String suffix, long defaultDelay){
        this.prefix = getUnicPrefix(prefix);
        this.suffix = suffix;
        this.defaultDelay = defaultDelay;
    }

    public String getNewFileName(){
        return getNewFileName(suffix, true, defaultDelay);
    }

    public String getNewFileName(String suffix){
        return getNewFileName(suffix, true, defaultDelay);
    }

    public String getNewFileName(boolean toDelete){
        return getNewFileName(suffix, toDelete, defaultDelay);
    }

    public String getNewFileName(long delay){
        return getNewFileName(suffix, true, delay);
    }

    public String getNewFileName(boolean toDelete, long delay){
        return getNewFileName(suffix, toDelete, delay);
    }

    public String getNewFileName(String suffix, long delay){
        return getNewFileName(suffix, true, delay);
    }

    public String getNewFileName(String suffix, boolean toDelete){
        return getNewFileName(suffix, toDelete, defaultDelay);
    }

    public String getNewFileName(String suffix, boolean toDelete, long delay){
        File target = getNewFile(suffix, toDelete, delay);
        try {
            return target.getCanonicalPath();
        } catch (Exception ex) {}
        return target.getAbsolutePath();
    }


    public File getNewFile(){
        return getNewFile(suffix, true, defaultDelay);
    }

    public File getNewFile(String suffix){
        return getNewFile(suffix, true, defaultDelay);
    }

    public File getNewFile(boolean toDelete){
        return getNewFile(suffix, toDelete, defaultDelay);
    }

    public File getNewFile(long delay){
        return getNewFile(suffix, true, delay);
    }

    public File getNewFile(boolean toDelete, long delay){
        return getNewFile(suffix, toDelete, delay);
    }

    public File getNewFile(String suffix, long delay){
        return getNewFile(suffix, true, delay);
    }

    public File getNewFile(String suffix, boolean toDelete){
        return getNewFile(suffix, toDelete, defaultDelay);
    }

    public File getNewFile(String suffix, boolean toDelete, long delay){
        File target = checkFile(prefix, suffix==null ? "" : suffix, 0);
        if(toDelete){
            target.deleteOnExit();
            if(delay >= 0){
                DeleteFileThread.createThread(target, delay);
            }
        }
        return target;
    }

    /*
     * check for a non-existing file
     */
    private File checkFile(String prefix, String suffix, long count){
        File f = new File(tempDir, prefix+"_"+count+suffix);
        if(f.exists()){
            return checkFile(prefix, suffix, ++count);
        }
        return f;
    }
}
