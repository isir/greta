/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class VertexAnimationTrack extends _Object_{

    public VertexAnimationTrack(long pointer) {
        super(pointer);
    }

    public VertexPoseKeyFrame createVertexPoseKeyFrame(float timePos) {
        return new VertexPoseKeyFrame(_createVertexPoseKeyFrame(getNativePointer(), timePos));
    }
    
    private native long _createVertexPoseKeyFrame(long thisPointer, float timePos);
    
    @Override
    protected native void delete(long nativePointer);
}
