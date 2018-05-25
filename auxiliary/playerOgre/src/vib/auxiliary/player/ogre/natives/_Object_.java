/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
abstract class _Object_ {
    
    private long pointer;
    private boolean gcMustDeleteMe = false;
    
    _Object_(long pointer){
        this.pointer = pointer;
    }
    
    public long getNativePointer(){
        return pointer;
    }
    
    /**
     * Please don't use it as possible
     * @param p 
     */
    void setNativePointer(long p){
        pointer = p;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof _Object_){
            return pointer == ((_Object_)o).pointer;
        }
        return false;
    }
    
    public boolean isNull(){
        return pointer==0;
    }
    
    protected void gcMustDeleteThat(boolean b){
        gcMustDeleteMe = b;
    }
    
    protected abstract void delete(long nativePointer);
    
    public void delete(){
        if(pointer != 0){
            delete(getNativePointer());
            pointer = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if(gcMustDeleteMe){
            delete();
        }
        super.finalize();
    }
}
