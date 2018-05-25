/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class SubMesh extends _Object_ {

    public SubMesh(long pointer) {
        super(pointer);
    }

    public VertexData getvertexData() {
        return new VertexData(_getvertexData(getNativePointer()));
    }
    private native long _getvertexData(long p);
    
    public boolean getuseSharedVertices() {
        return _getuseSharedVertices(getNativePointer());
    }
    private native boolean _getuseSharedVertices(long p);
    
    @Override
    protected native void delete(long nativePointer);
}
