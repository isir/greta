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
package greta.core.animation.common;

/**
 *
 * @author Jing Huang
 */
public abstract class Task {

    protected String _name;
    protected int _priority = 0;
    protected Target _target = new Target();
    protected boolean _actived = false;

    public Task(String name) {
        _name = name;
    }

    public void setPriority(int priority) {
        _priority = priority;
    }

    public int getPriority() {
        return _priority;
    }

    public String getName() {
        return _name;
    }

    public void setTarget(Target target) {
        _target = target;
        _actived = true;
    }

    public Target getTarget() {
        return _target;
    }

    public void setTaskActived(boolean actived) {
        _actived = actived;
    }

    public boolean isActived() {
        return _actived;
    }


    public abstract void launch();
}
