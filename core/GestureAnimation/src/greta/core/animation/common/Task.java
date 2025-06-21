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
