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
