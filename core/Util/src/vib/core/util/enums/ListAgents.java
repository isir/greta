/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.enums;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import vib.core.util.IniParameter;
import vib.core.util.log.Logs;

/**
 *
 * @author donat
 */
public class ListAgents {
    
    private String filename = "ListAgents.ini";
    private List<String> Agents = new ArrayList<String>();
    
    public ListAgents(){

        if ((new File(filename)).exists()) {
            try {
                BufferedReader in = getBufferedReader(filename);
                String ligne;
                while ((ligne = in.readLine()) != null) {
                    ligne = ligne.trim();
                    if (!ligne.startsWith("//") && !ligne.startsWith("#") && !ligne.startsWith(";") && !ligne.isEmpty()) {
                        Agents.add(ligne.toUpperCase().trim());  
                    }          
                }
                in.close();
            } catch (Exception e) {
                Logs.warning(this.getClass().getName() + " : Can not open " + filename + " : " + e.getMessage());
            }
        } else {
            Logs.warning(this.getClass().getName() + " : Can not find " + filename);
        }
    }
    
    protected BufferedReader getBufferedReader(String fileName) throws Exception{
        return new BufferedReader(new FileReader(fileName));
    }

    /**
     * @return the Agents
     */
    public List<String> getAgents() {
        return Agents;
    }

    /**
     * @param Agents the Agents to set
     */
    public void setAgents(List<String> Agents) {
        this.Agents = Agents;
    }
    
}
