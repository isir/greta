package vib.core.animation.bvh;

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
