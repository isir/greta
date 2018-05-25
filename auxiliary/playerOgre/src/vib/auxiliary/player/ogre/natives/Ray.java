/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Ray extends _Object_ {

    public Ray(long pointer) {
        super(pointer);
    }

    public Ray() {
        super(_instanciate());
    }
    private static native long _instanciate();

    public Vector3 getPoint(double distanceAt) {
        return new Vector3(_getPoint(getNativePointer(), distanceAt));
    }

    private native long _getPoint(long nativePointer, double distanceAt);
    
    @Override
    protected native void delete(long nativePointer);
}
