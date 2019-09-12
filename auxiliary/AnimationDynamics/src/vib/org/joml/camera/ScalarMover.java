/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 *//*
 * (C) Copyright 2015 Kai Burjack

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package org.joml.camera;

public class ScalarMover {

    public double maxAcceleration = 200.0f;
    public double maxDeceleration = 200.0f;
    public double current;
    public double target;
    public double velocity;

    public void update(float elapsedTimeInSeconds) {
        if (current == target) {
            return;
        }
        double currentToTarget = target - current;
        double directStopDistance = (velocity * velocity) / (2.0 * maxDeceleration);
        double acceleration = 0.0;
        if (velocity * currentToTarget > 0.0 && directStopDistance >= Math.abs(currentToTarget)) {
            /* Decelerate */
            double directDec = maxDeceleration;
            acceleration = (currentToTarget < 0.0 ? -1 : 1) * -directDec;
        } else {
            /* Accelerate */
            double directAcc = maxAcceleration;
            acceleration = (currentToTarget < 0.0 ? -1 : 1) * directAcc;
        }
        velocity += acceleration * elapsedTimeInSeconds;
        double way = velocity * elapsedTimeInSeconds;
        if (velocity * currentToTarget > 0.0 && Math.abs(way) > Math.abs(currentToTarget)) {
            /* We would move too far */
            velocity = 0.0;
            current = target;
        } else {
            current += way;
        }
    }

}
