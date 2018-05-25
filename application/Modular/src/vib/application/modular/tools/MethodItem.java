/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.tools;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class MethodItem implements Comparable<MethodItem> {

    int preferedOrder = 0;

    MethodItem(int preferedOrder) {
        this.preferedOrder = preferedOrder;
    }

    @Override
    public String toString() {
        return getMethodCaller() + "." + getMethodName() + "(" + getMethodParam() + ")";
    }

    @Override
    public int compareTo(MethodItem o) {
        if (preferedOrder == o.preferedOrder) {
            return String.CASE_INSENSITIVE_ORDER.compare(getMethodName(), o.getMethodName());
        }
        return preferedOrder - o.preferedOrder;
    }

    public abstract String getMethodName();

    public abstract String getMethodParam();

    public abstract String getMethodCaller();

    public abstract void appply();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MethodItem) {
            MethodItem met = (MethodItem) obj;
            return getMethodName().equals(met.getMethodName())
                    && getMethodCaller().equals(met.getMethodCaller());
        }
        return false;
    }

}
