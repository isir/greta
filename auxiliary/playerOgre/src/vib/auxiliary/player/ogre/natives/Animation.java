/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Animation extends _Object_{

    public Animation(long pointer) {
        super(pointer);
    }

    public VertexAnimationTrack createVertexTrack_VAT_POSE(int targetSubmesh) {
        return new VertexAnimationTrack(_createVertexTrack_VAT_POSE(getNativePointer(), targetSubmesh));
    }
    
    private native long _createVertexTrack_VAT_POSE(long thisPointer, int targetSubmesh);

    public VertexAnimationTrack getVertexTrack(int targetSubmesh) {
        return new VertexAnimationTrack(_getVertexTrack(getNativePointer(), targetSubmesh));
    }
    
    private native long _getVertexTrack(long thisPointer, int targetSubmesh);

    @Override
    protected native void delete(long nativePointer);
    
}
