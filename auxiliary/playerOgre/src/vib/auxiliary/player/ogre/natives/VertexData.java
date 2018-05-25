/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class VertexData extends _Object_{

    public VertexData(long pointer) {
        super(pointer);
    }

    public VertexDeclaration getvertexDeclaration() {
        return new VertexDeclaration(_getvertexDeclaration(getNativePointer()));
    }
    private native long _getvertexDeclaration(long thisPointer);

    public void reorganiseBuffers(VertexDeclaration newdcl) {
        _reorganiseBuffers(getNativePointer(), newdcl.getNativePointer());
    }
    private native void _reorganiseBuffers(long thisPointer, long vertexDeclarationPinter);
    
    @Override
    protected native void delete(long nativePointer);
}
