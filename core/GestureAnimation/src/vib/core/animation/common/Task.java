/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

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
