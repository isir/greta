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
package greta.auxiliary.environmentmanager.core.io.fap;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;

/**
 *
 * @author Brice Donval
 */
public interface IFAPReceiver extends FAPFrameEmitter {

    public IEnvironmentServer getEnvironmentServer();

    public MPEG4Animatable getMPEG4Animatable();

    /* ---------------------------------------------------------------------- */

    public String getHost();

    public void setHost(String host);

    /* -------------------------------------------------- */

    public String getPort();

    public void setPort(String port);

    /* -------------------------------------------------- */

    public void hasConnected();

    public void hasDisconnected();

    /* -------------------------------------------------- */

    public void destroy();

    public void onDestroy();

}
