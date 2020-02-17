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

/**
 *
 * @author Brice Donval
 */
public class EnvironmentManagerController {

    private final IEnvironmentManagerCore model;
    private EnvironmentManagerFrame view;

    /* ---------------------------------------------------------------------- */

    public EnvironmentManagerController(IEnvironmentManagerCore core) {
        model = core;
    }

    /* ---------------------------------------------------------------------- */

    public void setView(EnvironmentManagerFrame frame) {
        if (view != frame) {
            view = frame;
            view.setController(this);
            view.init(model.getType(), model.getProtocol(), model.getHost(), model.getStartingPort(), model.getEndingPort());
        }
    }

    /* ---------------------------------------------------------------------- */

    public void messageSenderHasConnected(String port) {
        if (view != null) {
            view.messageSenderHasConnected(port);
        }
    }

    public void messageSenderHasDisconnected(String port) {
        if (view != null) {
            view.messageSenderHasDisconnected(port);
        }
    }

    public void messageSenderHasDisappeared() {
        if (view != null) {
            view.messageSenderHasDisappeared();
        }
    }

    /* ------------------------------ */

    public void messageReceiverHasConnected(String port) {
        if (view != null) {
            view.messageReceiverHasConnected(port);
        }
    }

    public void messageReceiverHasDisconnected(String port) {
        if (view != null) {
            view.messageReceiverHasDisconnected(port);
        }
    }

    public void messageReceiverHasDisappeared() {
        if (view != null) {
            view.messageReceiverHasDisappeared();
        }
    }

    /* -------------------------------------------------- */

    public void fapSenderHasConnected(String port) {
        if (view != null) {
            view.fapSenderHasConnected(port);
        }
    }

    public void fapSenderHasDisconnected(String port) {
        if (view != null) {
            view.fapSenderHasDisconnected(port);
        }
    }

    public void fapSenderHasDisappeared() {
        if (view != null) {
            view.fapSenderHasDisappeared();
        }
    }

    /* ------------------------------ */

    public void fapReceiverHasConnected(String port) {
        if (view != null) {
            view.fapReceiverHasConnected(port);
        }
    }

    public void fapReceiverHasDisconnected(String port) {
        if (view != null) {
            view.fapReceiverHasDisconnected(port);
        }
    }

    public void fapReceiverHasDisappeared() {
        if (view != null) {
            view.fapReceiverHasDisappeared();
        }
    }

    /* -------------------------------------------------- */

    public void bapSenderHasConnected(String port) {
        if (view != null) {
            view.bapSenderHasConnected(port);
        }
    }

    public void bapSenderHasDisconnected(String port) {
        if (view != null) {
            view.bapSenderHasDisconnected(port);
        }
    }

    public void bapSenderHasDisappeared() {
        if (view != null) {
            view.bapSenderHasDisappeared();
        }
    }

    /* ------------------------------ */

    public void bapReceiverHasConnected(String port) {
        if (view != null) {
            view.bapReceiverHasConnected(port);
        }
    }

    public void bapReceiverHasDisconnected(String port) {
        if (view != null) {
            view.bapReceiverHasDisconnected(port);
        }
    }

    public void bapReceiverHasDisappeared() {
        if (view != null) {
            view.bapReceiverHasDisappeared();
        }
    }

    /* -------------------------------------------------- */

    public void audioSenderHasConnected(String port) {
        if (view != null) {
            view.audioSenderHasConnected(port);
        }
    }

    public void audioSenderHasDisconnected(String port) {
        if (view != null) {
            view.audioSenderHasDisconnected(port);
        }
    }

    public void audioSenderHasDisappeared() {
        if (view != null) {
            view.audioSenderHasDisappeared();
        }
    }

    /* ------------------------------ */

    public void audioReceiverHasConnected(String port) {
        if (view != null) {
            view.audioReceiverHasConnected(port);
        }
    }

    public void audioReceiverHasDisconnected(String port) {
        if (view != null) {
            view.audioReceiverHasDisconnected(port);
        }
    }

    public void audioReceiverHasDisappeared() {
        if (view != null) {
            view.audioReceiverHasDisappeared();
        }
    }

    /* ---------------------------------------------------------------------- */

    public void hostHasChanged(String host) {
        if (model != null) {
            model.hostHasChanged(host);
        }
    }

    public void portRangeHasChanged(String startingPort, String endingPort) {
        if (model != null) {
            model.portRangeHasChanged(startingPort, endingPort);
        }
    }

}
