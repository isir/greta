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
package greta.core.signals.gesture;

/**
 * This interface was designed to encapsulate different way for describing a position
 * @author Brian Ravenet
 */
public interface Position {
    public double getX();
    public double getY();
    public double getZ();
    public void setX(double x);
    public void setY(double y);
    public void setZ(double z);
    public void applySpacial(double spc);
    public String getStringPosition();
    public Position getCopy();
}
