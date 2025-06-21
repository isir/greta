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
package greta.core.animation.common.IK;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class IKMassSpring {

    String _name;
    ArrayList<IKSpring> _springs = new ArrayList<IKSpring>();
    ArrayList<IKMass> _nodes = new ArrayList<IKMass>();

    public IKMassSpring(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void clear() {
        _springs.clear();
        _nodes.clear();
    }

    public int createMass() {
        IKMass node = new IKMass(true);
        return addMass(node);
    }

    public int createMass(Vec3d position, double mass, boolean movable) {
        IKMass node = new IKMass(position, mass, movable);
        int index = addMass(node);
        return index;
    }

    public int addMass(IKMass ikmass) {
        int index = _nodes.size();
        _nodes.add(ikmass);
        return index;
    }

    public IKMass getMass(int handle) {
        if (handle >= _nodes.size() || handle < 0) {
            return null;
        }
        return _nodes.get(handle);
    }

    public int createSpring(int handle_node1, int handle_node2, double stiffness, double damping) {
        IKSpring spring = new IKSpring(this, handle_node1, handle_node2, stiffness, damping);
        int index = addSpring(spring);
        return index;
    }
     public int createSpring(int handle_node1, int handle_node2) {
        IKSpring spring = new IKSpring(this, handle_node1, handle_node2);
        int index = addSpring(spring);
        return index;
    }

    public int addSpring(IKSpring spring) {
        int index = _springs.size();
        _springs.add(spring);
        return index;
    }

    public IKSpring getSpring(int handle) {
        if (handle >= _springs.size() || handle < 0) {
            return null;
        }
        return _springs.get(handle);
    }

    public ArrayList<IKMass> getMasses() {
        return _nodes;
    }

    public ArrayList<IKSpring> getSprings() {
        return _springs;
    }

    public void computeInternalforceOfSpring(ArrayList<IKSpring> springs) {
        for (int i = 0; i < springs.size(); ++i) {
            IKSpring spring = springs.get(i);
            spring.computerInternalForce();
        }
    }

    public void moveMasses(ArrayList<IKMass> masses) {
        for (int i = 0; i < masses.size(); ++i) {
            IKMass node = masses.get(i);
            node.move();
        }
    }

    public void update(int steps) {
        int n = 0;
        while (n < steps) {
            computeInternalforceOfSpring(_springs);
            moveMasses(_nodes);
            n++;
        }
    }
}
