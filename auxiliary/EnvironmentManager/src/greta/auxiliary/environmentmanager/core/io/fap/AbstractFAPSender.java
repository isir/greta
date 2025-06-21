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
import greta.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;
import greta.core.animation.mpeg4.MPEG4Animatable;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractFAPSender implements IFAPSender {

    private final IEnvironmentManagerCore environmentServer;

    private final MPEG4Animatable mpegAnimatable;

    /* ---------------------------------------------------------------------- */

    public AbstractFAPSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        this.environmentServer = (IEnvironmentManagerCore) environmentServer;
        this.mpegAnimatable = mpegAnimatable;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final IEnvironmentServer getEnvironmentServer() {
        return environmentServer;
    }

    @Override
    public final MPEG4Animatable getMPEG4Animatable() {
        return mpegAnimatable;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void hasConnected() {
        environmentServer.fapSenderHasConnected(getPort());
    }

    @Override
    public final void hasDisconnected() {
        environmentServer.fapSenderHasDisconnected(getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public final void destroy() {
        onDestroy();
        environmentServer.fapSenderHasDisappeared();
    }

}
