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
