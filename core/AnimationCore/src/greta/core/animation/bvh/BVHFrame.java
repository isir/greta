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
package greta.core.animation.bvh;

import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class BVHFrame {

    private HashMap<String, BVHChannel> _values = new HashMap<String, BVHChannel>();

    public BVHFrame() {
    }

    public BVHFrame(BVHFrame ref) {
        for (String name : ref.getValues().keySet()) {
            _values.put(name, ref.getValue(name).clone());
        }
    }

    @Override
    public BVHFrame clone() {
        return new BVHFrame(this);
    }

    public HashMap<String, BVHChannel> getValues() {
        return _values;
    }

    public void setValues(HashMap<String, BVHChannel> _values) {
        this._values = _values;
    }

    public void addValue(String name, BVHChannel value) {
        this._values.put(name, value);
    }

    public BVHChannel getValue(String name) {
        return _values.get(name);
    }
}
