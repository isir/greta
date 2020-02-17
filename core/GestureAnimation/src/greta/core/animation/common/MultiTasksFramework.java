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

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Jing Huang
 */
public class MultiTasksFramework {

    protected Skeleton _pSkeleton;
    protected HashMap<String, Task> _tasks = new HashMap<String, Task>();

    public MultiTasksFramework(Skeleton skeleton) {
        _pSkeleton = skeleton;
    }

    public MultiTasksFramework() {
    }

    public void setSkeleton(Skeleton skeleton) {
        _pSkeleton = skeleton;
    }

    public Skeleton getSkeleton() {
        return _pSkeleton;
    }

    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        _tasks.put(task.getName(), task);
    }

    public Task getTask(String name) {
        if (_tasks.containsKey(name)) {
            return _tasks.get(name);
        }
        return null;
    }

    public HashMap<String, Task> getTasks() {
        return _tasks;
    }

    public void setTaskTarget(String taskName, Target target) {
        if (_tasks.containsKey(taskName)) {
            _tasks.get(taskName).setTarget(target);
        }
    }

    public void setTaskActived(String taskName, boolean b) {
        if (_tasks.containsKey(taskName)) {
            _tasks.get(taskName).setTaskActived(b);
        }
    }

    public boolean isTaskActived(String taskName) {
        if (_tasks.containsKey(taskName)) {
            return _tasks.get(taskName).isActived();
        }
        return false;
    }

    public void clearTasks() {
        _tasks.clear();
    }

    public void prepareTasks() {
        if (_pSkeleton != null) {
            _pSkeleton.reset();  //reset chaine

        }
    }

    public void launchTasks() {
        //TODO  different hierachy task
        //prepareTasks();
        Iterator<Task> itor = _tasks.values().iterator();
        while (itor.hasNext()) {
            Task task = itor.next();
            task.launch();
        }
    }
}
