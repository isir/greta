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
