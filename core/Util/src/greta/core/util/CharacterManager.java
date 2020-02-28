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

import greta.core.util.environment.Environment;
import greta.core.util.log.Logs;
import greta.core.util.speech.TTS;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains informations about characters that can be used by the system.<br/>
 * A default character is set at the initialization. Other character can be added with the function {@code addCharacter(String)}
 * or can be set as the character to use currently with {@code setCharacter(String)}.<br/>
 * If a parameter of the current character is changed or if an other character is set, all {@code CharacterDependent} added
 * with {@code add(CharacterDependent)} will be informed that a change occurs.
 * @author Andre-Marie Pez
 * @author Fajrian Yunus
 * @see greta.core.util.CharacterDependent CharacterDependent
 */
public class CharacterManager {

    //private static final String DEFAULT_CHARACTER_NAME = "CAMILLE";
    private static final String DEFAULT_CHARACTER_KEY = "DEFAULT_CHARACTER";

    private static CharacterManager staticInstance;
    private static int count=0;


    private Map<String, String> characterMapFile;
    private IniManager characterDefinitions;
    private List<CharacterDependent> dependents;
    private String currentCaracterName;

    private String currentCharacterId; //TODO find a better way to give the id from the environment
    public String currentCameraId;
    private String id;
    private TTS tts;

    private greta.core.util.environment.TreeNode currentCharacterHeadFromUnity;

    static{
        getStaticInstance();
    }
    private final Environment env;

    public String toString(){
        return id;
    }

    public CharacterManager(Environment env, String id){
        this.id = id;
        this.env = env;
        dependents = new ArrayList<>();
        characterMapFile = new HashMap<String, String>();
        currentCaracterName = "DEFAULT_CHARACTER";
        String fileName = IniManager.getGlobals().getValueString(DEFAULT_CHARACTER_KEY);
        /*if (!(new File(fileName)).exists()) {
            currentCaracterName = DEFAULT_CHARACTER_NAME;
            fileName = characterMapFile.get(DEFAULT_CHARACTER_NAME);
        }*/
        characterMapFile.put(currentCaracterName, (new File(fileName)).getAbsolutePath());
        characterDefinitions = new IniManager((new File(fileName)).getAbsolutePath());
        setCharacter(IniManager.getGlobals().getValueString("CURRENT_CHARACTER"));
        this.currentCharacterHeadFromUnity = null;
        count++;
        Logs.info(String.format("CharacterManager '%s' created",id));
    }

    public CharacterManager(Environment env){
        this(env,"CharacterManager-"+(count));
    }

    public static CharacterManager getStaticInstance(){
        if(staticInstance==null)
            staticInstance = new CharacterManager(null,"CharacterManager-static");
        return staticInstance;
    }

    public Environment getEnvironment(){
        return env;
    }

    /**
     * Adds a {@code CharacterDependent}.<br/>
     * All {@code CharacterDependent} added will be informed when the character change.
     * @param dependent the {@code CharacterDependent} to add
     */
    public void add(CharacterDependent dependent) {
        // System.out.println(String.format("Adding to %s : %s",toString(),dependent.toString()));
        if( ! dependents.contains(dependent)) {
            dependents.add(dependent);
        }
    }

    /**
     * Removes a {@code CharacterDependent}.<br/>
     * This {@code CharacterDependent} will not be informed when the character change.
     * @param dependent the {@code CharacterDependent} to remove
     */
    public void remove(CharacterDependent dependent){
        try{
            dependents.remove(dependent);
        }catch(Exception e){/* remove may throw an exception but we dont take care of it */}
    }

    /**
     * Removes all {@code CharacterDependent} added.
     */
    public void clearListOfDependents(){
        dependents.clear();
    }

    /**
     * Send a notification to all {@code CharacterDependent} added.
     */
    public void notifyChanges() {
        for (CharacterDependent dependent : dependents) {
            dependent.onCharacterChanged();
            //System.out.println(String.format("%s notifying %s",toString(),dependent.toString()));
        }
    }

    /**
     * Set a charater as current charater and informs all {@code CharacterDependent} that the character has changed.<br/>
     * If the character is unknown, this fuction tries to add it.
     * @param name the name of the character to set
     * @see #addCharacter(java.lang.String)
     */
    public void setCharacter(String name) {
        String fileName = fileNameOfCharacter(name);

        if (fileName != null) {
            currentCaracterName = name;
            characterDefinitions.setDefinition(fileName);
            notifyChanges();
        }
    }

    /**
     * Adds a new charater.<br/>
     * The name of the corresponding ini file of this character must be the value of the parameter, in the global ini file, that have the same name.<br/>
     * i.e. if {@code name="prudence"}, in the global ini file you must have {@code prudence=prud.ini}.
     * @param name the name of the character to add
     */
    public void addCharacter(String name) {
        if (characterMapFile.get(name) == null) { //else it is already added
            String fileName = IniManager.getGlobals().getValueString(name);
            if (!fileName.isEmpty()) {
                fileName = (new File(fileName)).getAbsolutePath();
                characterMapFile.put(name, fileName);
                characterDefinitions.addDefinition(fileName);
            }
        }
    }

    /**
     * Returns the value of a parameter from the default character.
     * @param name the name of the parameter
     * @return the value from the default character
     */
    public String getDefaultValueString(String name) {
        return characterDefinitions.getDefault(name).getParamValue();
    }

    /**
     * Returns the list of all known values of one specific parameter.
     * @param name the name of the parameter
     * @return the list of all known values
     */
    public List<String> getAllValuesString(String name) {
        List<IniParameter> params = characterDefinitions.getAllFromOne(name);
        ArrayList<String> values = new ArrayList<String>(params.size());
        for (IniParameter param : params) {
            values.add(param.getParamValue());
        }
        return values;
    }

    /**
     * Returns the value of a IniParameter as a boolean.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns {@code false}.
     * @param name the name of the parameter
     * @return the boolean value of the parameter
     */
    public boolean getValueBoolean(String name) {
        return characterDefinitions.getValueBoolean(name);
    }

    /**
     * Returns the value of a parameter as an integer.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.
     * @param name the name of the parameter
     * @return the integer value of the parameter
     */
    public int getValueInt(String name) {
        return characterDefinitions.getValueInt(name);
    }

    /**
     * Returns the value of a parameter as a double.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.0f
     * @param name the name of the parameter
     * @return the double value of the parameter
     */
    public double getValueDouble(String name) {
        return characterDefinitions.getValueDouble(name);
    }

    /**
     * Returns the value of a parameter as a string.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the empty string "".
     * @param name the name of the parameter
     * @return the string value of the parameter
     */
    public String getValueString(String name) {
        return characterDefinitions.getValueString(name);
    }

    /**
     * Returns the value of a IniParameter as a boolean.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns {@code false}.
     * @param name the name of the parameter
     * @param characterName the name of the character
     * @return the boolean value of the parameter
     */
    public boolean getValueBoolean(String name, String characterName) {
        return characterDefinitions.getValueBoolean(name, fileNameOfCharacter(characterName));
    }

    /**
     * Returns the value of a parameter as an integer.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.
     * @param name the name of the parameter
     * @param characterName the name of the character
     * @return the integer value of the parameter
     */
    public int getValueInt(String name, String characterName) {
        return characterDefinitions.getValueInt(name, fileNameOfCharacter(characterName));
    }

    /**
     * Returns the value of a parameter as a double.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the value -999999999.0f
     * @param name the name of the parameter
     * @param characterName the name of the character
     * @return the double value of the parameter
     */
    public double getValueDouble(String name, String characterName) {
        return characterDefinitions.getValueDouble(name, fileNameOfCharacter(characterName));
    }

    /**
     * Returns the value of a parameter as a string.<br/>
     * This method returns the value of the parameters if found,
     * otherwise returns the empty string "".
     * @param name the name of the parameter
     * @param characterName the name of the character
     * @return the string value of the parameter
     */
    public String getValueString(String name, String characterName) {
        return characterDefinitions.getValueString(name, fileNameOfCharacter(characterName));
    }

    /**
     * Sets the value of a parameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueBoolean(String name, boolean value) {
        characterDefinitions.setValueBoolean(name, value);
        notifyChanges();
    }

    /**
     * Sets the value of a parameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueInt(String name, int value) {
        characterDefinitions.setValueInt(name, value);
        notifyChanges();
    }

    /**
     * Sets the value of a parameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueDouble(String name, double value) {
        characterDefinitions.setValueDouble(name, value);
        notifyChanges();
    }

    /**
     * Sets the value of a parameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setValueString(String name, String value) {
        characterDefinitions.setValueString(name, value);
        notifyChanges();
    }

    /**
     * add or set a value in the current character.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void addValueString(String name, String value){
        characterDefinitions.addValueString(name, value);
        notifyChanges();
    }

    /**
     * Returns the name of the current character.
     * @return the name of the current character
     */
    public String getCurrentCharacterName() {
        return currentCaracterName;
    }

    /**
     * Returns the name of the default character.
     * @return the name of the default character
     */
    public String getDefaultCharacterName() {
        String def_agent_fileName = IniManager.getGlobals().getValueString(DEFAULT_CHARACTER_KEY);
        String default_agent = fileNameOfCharacter(def_agent_fileName);
        return default_agent;
    }

    /**
     * Returns the file name of the current character.
     * @return the file name of the current character
     */
    public String getCurrentCharacterFile() {
        return fileNameOfCharacter(currentCaracterName);
    }

    /**
     * Returns the file name of the default character.
     * @return the file name of the default character
     */
    public String getDefaultCharacterFile() {
        return fileNameOfCharacter(DEFAULT_CHARACTER_KEY);
    }

    /**
     * Returns the {@code IniManager} used to manage Ini files of chararters.
     * @return the {@code IniManager} used
     */
    public IniManager getIniManager() {
        return characterDefinitions;
    }

    private String fileNameOfCharacter(String characterName) {
        String fileName = characterMapFile.get(characterName);

        if (fileName == null) {
            addCharacter(characterName);
            //retry to get the file name :
            fileName = characterMapFile.get(characterName);
        }
        return fileName;
    }

    /**
     * @return the tts
     */
    public TTS getTTS() {
        return tts;
    }

    public void setTTS(TTS tts){
        this.tts = tts;
    }

    /**
     * @return the currentCharacterId
     */
    public String getCurrentCharacterId() {
        return currentCharacterId;
    }

    /**
     * @param currentCharacterId the currentCharacterId to set
     */
    public void setCurrentCharacterId(String currentCharacterId) {
        this.currentCharacterId = currentCharacterId;
    }

    public greta.core.util.environment.TreeNode getCurrentCharacterHeadFromUnity() {
        return this.currentCharacterHeadFromUnity;
    }

    public void setCurrentCharacterHeadFromUnity(greta.core.util.environment.TreeNode currentCharacterHeadFromUnity) {
        this.currentCharacterHeadFromUnity = currentCharacterHeadFromUnity;
    }
}
