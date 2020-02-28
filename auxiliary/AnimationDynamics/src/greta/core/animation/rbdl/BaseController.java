/*
 * This file is part of the auxiliaries of Greta.
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
package greta.core.animation.rbdl;

/**
 *
 * @author Jing Huang
 *
 */
public interface BaseController{

    public abstract void update(double dt);
    public abstract String getName() ;
    public abstract void setName(String name) ;
    public abstract int getIndex() ;
    public abstract void setIndex(int index);
    public abstract void setActive(boolean active);
    public abstract boolean isActive();
}
