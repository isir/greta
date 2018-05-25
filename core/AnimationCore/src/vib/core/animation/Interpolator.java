package vib.core.animation;

import java.util.List;
/**
 *
 * @author Jing Huang
 */


public interface Interpolator <T>{
    public List<T> interpolate(T t1, T t2, int nb);
    //abstract public List<T> interpolate(List<T> t);
}
