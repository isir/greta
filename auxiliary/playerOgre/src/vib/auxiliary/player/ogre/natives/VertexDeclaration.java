/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class VertexDeclaration extends _Object_{

    public VertexDeclaration(long pointer) {
        super(pointer);
    }

    public VertexDeclaration getAutoOrganisedDeclaration(boolean b, boolean b0) {
        return new VertexDeclaration(_getAutoOrganisedDeclaration(getNativePointer(), b, b0));
    }
    
    private native long _getAutoOrganisedDeclaration(long thisPointer, boolean b, boolean b0);
    
    @Override
    protected native void delete(long nativePointer);
}
