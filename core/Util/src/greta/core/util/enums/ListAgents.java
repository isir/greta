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
package greta.core.util.enums;

import greta.core.util.log.Logs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author donat
 */
public class ListAgents {

    private String fileName = "ListAgents.ini";
    private List<String> Agents = new ArrayList<String>();

    public ListAgents(){

        if ((new File(fileName)).exists()) {
            try {
                BufferedReader in = getBufferedReader(fileName);
                String ligne;
                while ((ligne = in.readLine()) != null) {
                    ligne = ligne.trim();
                    if (!ligne.startsWith("//") && !ligne.startsWith("#") && !ligne.startsWith(";") && !ligne.isEmpty()) {
                        Agents.add(ligne.toUpperCase().trim());
                    }
                }
                in.close();
            } catch (Exception e) {
                Logs.warning(this.getClass().getName() + " : Can not open " + fileName + " : " + e.getMessage());
            }
        } else {
            Logs.warning(this.getClass().getName() + " : Can not find " + fileName);
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
