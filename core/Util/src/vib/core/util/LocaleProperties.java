/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class LocaleProperties extends IniManager{

    LocaleProperties(){
        super(Locale.UK.getLanguage()+"-"+Locale.UK.getCountry());
    }

    @Override
    protected String getFileName(String definition) {
        return "./Locale/"+definition+".ini";
    }

    @Override
    protected BufferedReader getBufferedReader(String fileName) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
    }



}
