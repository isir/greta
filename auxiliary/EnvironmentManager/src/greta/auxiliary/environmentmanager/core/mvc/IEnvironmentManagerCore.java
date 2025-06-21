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
package greta.auxiliary.environmentmanager.core.mvc;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.environment.Environment;
import java.util.List;

/**
 *
 * @author Brice Donval
 */
public interface IEnvironmentManagerCore extends IEnvironmentServer {

    public EnvironmentManagerController getController();

    /* -------------------------------------------------- */

    public void messageSenderHasConnected(String port);

    public void messageSenderHasDisconnected(String port);

    public void messageSenderHasDisappeared();

    /* ------------------------------ */

    public void messageReceiverHasConnected(String port);

    public void messageReceiverHasDisconnected(String port);

    public void messageReceiverHasDisappeared();

    /* -------------------------------------------------- */

    public void fapSenderHasConnected(String port);

    public void fapSenderHasDisconnected(String port);

    public void fapSenderHasDisappeared();

    /* ------------------------------ */

    public void fapReceiverHasConnected(String port);

    public void fapReceiverHasDisconnected(String port);

    public void fapReceiverHasDisappeared();

    /* -------------------------------------------------- */

    public void bapSenderHasConnected(String port);

    public void bapSenderHasDisconnected(String port);

    public void bapSenderHasDisappeared();

    /* ------------------------------ */

    public void bapReceiverHasConnected(String port);

    public void bapReceiverHasDisconnected(String port);

    public void bapReceiverHasDisappeared();

    /* -------------------------------------------------- */

    public void audioSenderHasConnected(String port);

    public void audioSenderHasDisconnected(String port);

    public void audioSenderHasDisappeared();

    /* ------------------------------ */

    public void audioReceiverHasConnected(String port);

    public void audioReceiverHasDisconnected(String port);

    public void audioReceiverHasDisappeared();

    /* -------------------------------------------------- */

    public void hostHasChanged(String host);

    public void portRangeHasChanged(String startingPort, String endingPort);

    /* ---------------------------------------------------------------------- */

    public void setLocalEnvironment(Environment localEnvironment);

    public Environment getLocalEnvironment();

    /* -------------------------------------------------- */

    public boolean isLocalMPEG4Animatable(String mpeg4AnimatableId);

    public boolean isDistantMPEG4Animatable(String mpeg4AnimatableId);

    /* ------------------------------ */

    public void setLocalMPEG4Animatable(MPEG4Animatable mpeg4Animatable);

    public void setDistantMPEG4Animatable(MPEG4Animatable mpeg4Animatable);

    /* ------------------------------ */

    public void unsetLocalMPEG4Animatable(MPEG4Animatable mpeg4Animatable);

    public void unsetDistantMPEG4Animatable(MPEG4Animatable mpeg4Animatable);

    /* ---------------------------------------------------------------------- */

    public String getPrimaryEnvironmentServerId();

    public void setPrimaryEnvironmentServerId(String environmentServerId);

    /* -------------------------------------------------- */

    public List<String> getReplicaEnvironmentServerIds();

    public void addReplicaEnvironmentServerId(String environmentServerId);

    public void removeReplicaEnvironmentServerId(String environmentServerId);

}
