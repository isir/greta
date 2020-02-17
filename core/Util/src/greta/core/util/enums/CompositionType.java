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
