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
package greta.tools.editors.bml.timelines;

import greta.core.signals.Signal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie
 */
public abstract class SignalLib<P extends Signal> {

    public abstract void applyOn(P signal, String className, String instanceName);
    //public abstract P get(String className, String instanceName); // Angelo: commented because never called by the classes that extend SignalLib
    public abstract String getClassNameOf(P signal);
    public abstract String getInstanceNameOf(P signal);
    public abstract List<String> getAllInstanceNamesOf(String className);
    public abstract List<String> getAllClassNames();

    public Map<String, List<String>> buildLib(){
        HashMap<String, List<String>> lib = new HashMap<String, List<String>>();
        List<String> keys = this.getAllClassNames();
        for(String key : keys){
            List<String> instances = this.getAllInstanceNamesOf(key);
            Collections.sort(instances, String.CASE_INSENSITIVE_ORDER);
            lib.put(key, instances);
        }
        return lib;
    }

}
