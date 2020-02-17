/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.application.modular.tools;

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
