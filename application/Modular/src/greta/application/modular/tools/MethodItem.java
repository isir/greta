/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
