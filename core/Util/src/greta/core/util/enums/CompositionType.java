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
package greta.core.util.enums;

/**
 * This is an enumeration of animation composition types.
 *
 * @author Andre-Marie Pez
 */
public enum CompositionType {

    /**
     * The associated animation must replace the current animation
     */
    replace,
    /**
     * The associated animation must be blened with the current animation
     */
    blend,
    /**
     * The associated animation must be added after the current animation
     */
    append;

    public static CompositionType interpret(String name, CompositionType defaultCompositionType) {
        try {
            return valueOf(name);
        } catch (Throwable t) {
            return defaultCompositionType;
        }
    }

    public static CompositionType interpret(String name) {
        return interpret(name, blend);
    }
}
