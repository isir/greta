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
