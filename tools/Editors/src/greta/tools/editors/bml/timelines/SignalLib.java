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
