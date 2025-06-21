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
package greta.core.animation.locomotion;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class State {

    public enum ID {
        ROOT,
        HIP_R,
        KNEE_R,
        HIP_L,
        KNEE_L,
        ANKLE_R,
        ANKLE_L,
    };

    public double[] _currentState = new double[7];
    public final static int nrStates = 100;
    public State(){
        _currentState[0] = 0;
        _currentState[1] = 0;
        _currentState[2] = 0;
        _currentState[3] = 0;
        _currentState[4] = 0;
        _currentState[5] = 0;
        _currentState[6] = 0;

//        _currentState[0] = 0;
//        _currentState[1] = -0.4;
//        _currentState[2] = 0.1;
//        _currentState[3] = 0.7;
//        _currentState[4] = 0.05;
//        _currentState[5] = -.2;
//        _currentState[6] = -.2;


//        _currentState[0] = 0.25;
//        _currentState[1] = 0;
//        _currentState[2] = 0.92;
//        _currentState[3] = 0.7;
//        _currentState[4] = 0.05;
//        _currentState[5] = -0.44;
//        _currentState[6] = -.2;

    }
    public void setState(double[] state){
        for(int i =0; i < 7; ++i){
            _currentState[i] = state[i];
        }
    }

    public static double[] _desireState = new double[nrStates];
    static int _index = 0;
    public static void setWalk() {
        _index = 0;
        _desireState[_index] = 0.03;
        ++_index;
        _desireState[_index] = -0.4;
        ++_index;
        _desireState[_index] = 1.1;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.05;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;


        _desireState[_index] = -0.02;
        ++_index;
        _desireState[_index] = 0.5;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = -0.2;
        ++_index;
        _desireState[_index] = 0.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;


        _desireState[_index] = 0.03;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.05;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = -0.4;
        ++_index;
        _desireState[_index] = 1.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;


        _desireState[_index] = -0.02;
        ++_index;
        _desireState[_index] = -0.2;
        //_desireState[_index] = -0.1;
        ++_index;
        _desireState[_index] = 0.1;
        ++_index;
        _desireState[_index] = 0.5;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;
        _desireState[_index] = -.1;
        ++_index;

    }

//    public static void setWalk() {
//        _desireState = new double[nrStates];
//        _desireState[_index] = -0.1;
//        ++_index;
//        _desireState[_index] = -0.4;
//        ++_index;
//        _desireState[_index] = 1.1;
//        ++_index;
//        _desireState[_index] = 0.7;
//        ++_index;
//        _desireState[_index] = 0.05;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//
//
//        _desireState[_index] = -0.1;
//        ++_index;
//        _desireState[_index] = 0;
//        ++_index;
//        _desireState[_index] = 0.05;
//        ++_index;
//        _desireState[_index] = 0;
//        ++_index;
//        _desireState[_index] = 0.1;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//
//
//        _desireState[_index] = -0.1;
//        ++_index;
//        _desireState[_index] = 0.7;
//        ++_index;
//        _desireState[_index] = 0.05;
//        ++_index;
//        _desireState[_index] = -0.4;
//        ++_index;
//        _desireState[_index] = 1.1;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//
//        _desireState[_index] = -0.1;
//        ++_index;
//        _desireState[_index] = 0;
//        ++_index;
//        _desireState[_index] = 0.1;
//        ++_index;
//        _desireState[_index] = 0;
//        ++_index;
//        _desireState[_index] = 0.05;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//        _desireState[_index] = -.2;
//        ++_index;
//
//    }

    public static void setCrouch() {
        _desireState = new double[nrStates];
        _desireState[_index] = 0.18;
        ++_index;
        _desireState[_index] = -1.1;
        ++_index;
        _desireState[_index] = 2.17;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.05;
        ++_index;
        _desireState[_index] = 0.97;
        ++_index;
        _desireState[_index] = -0.62;
        ++_index;
        _desireState[_index] = -0.44;
        ++_index;


        _desireState[_index] = 0.25;
        ++_index;
        _desireState[_index] = 0.7;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.1;
        ++_index;
        _desireState[_index] = 0.92;
        ++_index;
        _desireState[_index] = -.2;
        ++_index;
        _desireState[_index] = -.44;
        ++_index;


        _desireState[_index] = 0.18;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.05;
        ++_index;
        _desireState[_index] = 0.97;
        ++_index;
        _desireState[_index] = -1.1;
        ++_index;
        _desireState[_index] = 2.17;
        ++_index;
        _desireState[_index] = -0.44;
        ++_index;
        _desireState[_index] = -.62;
        ++_index;


        _desireState[_index] = 0.25;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.1;
        ++_index;
        _desireState[_index] = 0.92;
        ++_index;
        _desireState[_index] = 0.7;
        ++_index;
        _desireState[_index] = 0.05;
        ++_index;
        _desireState[_index] = -.44;
        ++_index;
        _desireState[_index] = -.2;
        ++_index;

    }

    public static void setStop() {
        _index = 28;
        _desireState[_index] = 0;
        ++_index;
        _desireState[_index] = 0;
        ++_index;
        _desireState[_index] = 0;
        ++_index;
        _desireState[_index] = 0;
        //_desireState[_index] = -0.05;
        ++_index;
        _desireState[_index] = 0;
        ++_index;
        _desireState[_index] = 0;
        ++_index;
        _desireState[_index] = 0;
        ++_index;
    }
}
