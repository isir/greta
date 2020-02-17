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
