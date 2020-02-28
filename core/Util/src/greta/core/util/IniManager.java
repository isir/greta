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
package greta.core.util;

import greta.core.util.log.Logs;
import greta.core.util.parameter.ParameterSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class manages ini files.<br/>
 * Calling the constructor load the default ini file in memory.<br/>
 * Then you can call other methods to retrieve the parameters that you want
 * or add and set a new definition of parameters.<br/>
 * At any time you can save all the stored parameters to an ini file.
 * @see greta.core.util.IniParameter IniParameter
 * @see greta.core.util.parameter.ParameterSet ParameterSet
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.util.IniParameter
 */
public class IniManager extends ParameterSet<IniParameter> {

//static field :
    private static final String defaultIniFileName = "./Greta.ini";
    private static final IniManager globalIniManager = new IniManager(); //Singleton

    private static String programPath;
    private static LocaleProperties localeProperties;
    private static NumberFormat numberFormat;

    private static double javaVersion;

    static {
        programPath = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setInfinity(""+Double.POSITIVE_INFINITY);
        symbols.setNaN(""+Double.NaN);
        numberFormat = new DecimalFormat("#0.000", symbols);
        localeProperties = new LocaleProperties();

        String version = System.getProperty("java.version");
        int pos = 0, count = 0;
        for ( ; pos<version.length() && count < 2; pos ++) {
            if (version.charAt(pos) == '.') {
                count ++;
            }
        }
        javaVersion = Double.parseDouble (version.substring (0, pos-1));
    }

    public static double getJavaVersion(){
        return javaVersion;
    }

    /**
     * Get the access to the global IniManager.
     * @return the global IniManager
     */
    public static IniManager getGlobals() {
        return globalIniManager;
    }

    /**
     * Get the absolute path of the current running file.<br/>
     * It is the path where the program is, not where it is running.
     * @return the absolute path of the program
     */
    public static String getProgramPath() {
        return programPath;
    }

    public static String relativiseFromProgramPath(String fileName){
//        String relativeFileName = fileName;
//        if(fileName.startsWith(IniManager.getProgramPath())){
//            relativeFileName = "."+relativeFileName.substring(IniManager.getProgramPath().length());
//        }
//        return relativeFileName;
        URI uri = new File(programPath).toURI().relativize(new File(fileName).toURI());
        return uri.getPath();
    }
     /**
     * Get the common {@code NumberFormat}.<br/>
     * It is usefull write and parse floating numbers in the same syntaxes.
     * @return the common {@code NumberFormat}
     */
    public static NumberFormat getNumberFormat() {
        return numberFormat;
    }

     /**
     * Returns the value of a locale property.<br/>
     * This method returns the value corresponding the name given in the Default {@code Locale}.
     * @param name the name of the property
     * @return the string value of a locale property
     * @see java.util.Locale
     */
    public static String getLocaleProperty(String name){
        return getLocaleProperty(name, Locale.getDefault());
    }

    /**
     * Returns the value of a locale property.<br/>
     * This method returns the value corresponding the name given in the given {@code Locale}.
     * @param name the name of the property
     * @param locale the target {@code Locale}
     * @return the string value of a locale property
     * @see java.util.Locale
     */
    public static String getLocaleProperty(String name, Locale locale) {
        IniParameter param = localeProperties.get(name, locale.getLanguage()+"-"+locale.getCountry());
        String result = param==null ? name : param.getParamValue();
        return result==null ? name : result;
    }

//constructors :
    /**
     * Construct an IniManger object with a target file name.
     * @param fileName the name of the ini file
     */
    public IniManager(String fileName) {
        super(fileName);
    }

    /**
     * Construct an IniManger object with the default ini file.
     */
    public IniManager() {
        this(defaultIniFileName);
    }

//Methods :
    /**
     * Read an ini file.
     * @param definition the name of the file
     * @return a list of IniParameter
     * @see greta.core.util.parameter.ParameterSet#load(java.lang.String) ParameterSet.load(String)
     */
    @Override
    protected List<IniParameter> load(String definition) {

        ArrayList<IniParameter> params = new ArrayList<IniParameter>();
        String fileName = getFileName(definition);
        if ((new File(fileName)).exists()) {
            try {
                BufferedReader in = getBufferedReader(fileName);
                String ligne;
                while ((ligne = in.readLine()) != null) {
                    ligne = ligne.trim();
                    if (!ligne.startsWith("//") && !ligne.startsWith("#") && !ligne.startsWith(";")) {
                        int equalIndex = ligne.indexOf("=");
                        if (equalIndex > 0) {
                            String paramName = ligne.substring(0, equalIndex).toUpperCase().trim();
                            String paramValue = ligne.substring(equalIndex + 1).trim();
                            if( ! paramName.isEmpty()) {
                                params.add(new IniParameter(paramName, paramValue));
                            }
                        }
                    }
                }

                in.close();
            } catch (Exception e) {
                Logs.warning(this.getClass().getName() + " : Can not open " + fileName + " : " + e.getMessage());
            }
        } else {
            Logs.warning(this.getClass().getName() + " : Can not find " + fileName);
        }

        return params;
    }

    /**
     * Returns a {@code BufferedReader} to read the specified file.
     * @param fileName the name of the ini file
     * @return a reader to read the file
     * @throws Exception
     */
    protected BufferedReader getBufferedReader(String fileName) throws Exception{
        return new BufferedReader(new FileReader(fileName));
    }

    /**
     * Returns the name of the file corresponding to the specified definition.
     * @param definition the definition
     * @return the file name
     */
    protected String getFileName(String definition){
        return definition;
    }

    /**
     * Returns the value of a IniParameter as a boolean.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns {@code false}.
     * @param name the name of the parameter
     * @return the boolean value of the parameter
     */
    public boolean getValueBoolean(String name) {
        String value = getValueString(name);
        if (!value.equals("")) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
            } //the string is not a boolean value
        }
        return false;
    }

    /**
     * Returns the value of a IniParameter as an integer.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.
     * @param name the name of the parameter
     * @return the integer value of the parameter
     */
    public int getValueInt(String name) {
        String value = getValueString(name);
        if (!value.equals("")) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
            } //the string is not an integer value
        }
        return -999999999;
    }

    /**
     * Returns the value of a IniParameter as a double.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.0f
     * @param name the name of the parameter
     * @return the double value of the parameter
     */
    public double getValueDouble(String name) {
        String value = getValueString(name);
        if (!value.equals("")) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
            } //the string is not a double value
        }
        return -999999999;
    }

    /**
     * Returns the value of a IniParameter as a string.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the empty string "".
     * @param name the name of the parameter
     * @return the string value of the parameter
     */
    public String getValueString(String name) {
        IniParameter param = get(name);
        return param == null ? "" : param.getParamValue();
    }

    /**
     * Sets the value of a IniParameter.<br/>
     * Given the name of a IniParameter and a boolean value, it stores the value
     * in the IniParameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueBoolean(String name, boolean value) {
        setValueString(name, Boolean.toString(value));
    }

    /**
     * Sets the value of a IniParameter.<br/>
     * Given the name of a IniParameter and an integer value, it stores the value
     * in the IniParameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueInt(String name, int value) {
        setValueString(name, Integer.toString(value));
    }

    /**
     * Sets the value of a IniParameter.<br/>
     * Given the name of a IniParameter and a double value, it stores the value
     * in the IniParameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueDouble(String name, double value) {
        setValueString(name, Double.toString(value));
    }

    /**
     * Sets the value of a IniParameter.<br/>
     * Given the name of a IniParameter and a string value, it stores the value
     * in the IniParameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueString(String name, String value) {
        IniParameter param = get(name);
        if (param != null) {
            param.setParamValue(value);
        }
    }

    /**
     * add or set a value in the current definition.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void addValueString(String name, String value){
        if(!getCurrentDefinition().contains(name)){
            getCurrentDefinition().addParameter(new IniParameter(name, value));
        }
        else{
            setValueString(name, value);
        }

        if(getDefaultDefinition()!=getCurrentDefinition() && !getDefaultDefinition().contains(name)){
            getDefaultDefinition().addParameter(new IniParameter(name, value));
        }
    }

    /**
     * Write the stored parameters to an ini file.<br/>
     * Given the name of an ini file it writes the parameters.
     * @param iniFileName the name of the ini file to be write
     */
    public void saveAs(String iniFileName) {
        save(iniFileName, this.getAll());
    }

    @Override
    protected void save(String definition, List<IniParameter> paramToSave) {
        try {
            FileWriter out = new FileWriter(definition);
            for(IniParameter param : paramToSave){
                out.write(param.getParamName()+" = "+param.getParamValue()+"\n");
            }
            out.close();
        } catch (Exception ex) {
            Logs.warning(this.getClass().getName() + " : Can not write in the file " + definition);
        }
    }

    /**
     * Returns the value of a IniParameter as a boolean.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns {@code false}.
     * @param name the name of the parameter
     * @param definition the name of the definition
     * @return the boolean value of the parameter
     */
    public boolean getValueBoolean(String name, String definition) {
        String value = getValueString(name, definition);
        if (!value.equals("")) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
            } //the string is not a boolean value
        }
        return false;
    }

    /**
     * Returns the value of a IniParameter as an integer.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.
     * @param name the name of the parameter
     * @param definition the name of the definition
     * @return the integer value of the parameter
     */
    public int getValueInt(String name, String definition) {
        String value = getValueString(name, definition);
        if (!value.equals("")) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
            } //the string is not an integer value
        }
        return -999999999;
    }

    /**
     * Returns the value of a IniParameter as a double.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.0f
     * @param name the name of the parameter
     * @param definition the name of the definition
     * @return the double value of the parameter
     */
    public double getValueDouble(String name, String definition) {
        String value = getValueString(name, definition);
        if (!value.equals("")) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
            } //the string is not a double value
        }
        return -999999999;
    }

    /**
     * Returns the value of a IniParameter as a string.<br/>
     * Given the name of a IniParameter contained in the ini file.
     * This method returns the value of the parameters if found,
     * otherwise returns the empty string "".
     * @param name the name of the parameter
     * @param definition the name of the definition
     * @return the string value of the parameter
     */
    public String getValueString(String name, String definition) {
        IniParameter param = get(name, definition);
        return param == null ? "" : param.getParamValue();
    }
}
