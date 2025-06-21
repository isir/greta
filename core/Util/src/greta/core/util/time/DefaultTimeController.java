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
package greta.core.util.time;

/**
 * This {@code TimeController} use the system time.
 * @author Andre-Marie Pez
 */
public class DefaultTimeController implements TimeController{

    /**
     * The offset between the current time and the system one. (in milliseconds)
     */
    private long offset = System.currentTimeMillis();

    @Override
    public long getTimeMillis(){
        return System.currentTimeMillis() - offset;
    }

    @Override
    public void setTimeMillis(long milliSeconds){
        offset = System.currentTimeMillis() - milliSeconds;
    }

    @Override
    public double getTime(){
        return getTimeMillis() / 1000.0;
    }

    @Override
    public void setTime(double seconds){
        setTimeMillis((long)(seconds * 1000.0));
    }

    @Override
    public void sleep(long millis) {
        try {Thread.sleep(millis);} catch (Exception ex) {}
    }

}
