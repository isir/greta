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
 */

package vib.auxiliary.environmentmanager.core;

import java.util.Map;
import vib.auxiliary.environmentmanager.core.io.audio.IAudioReceiver;
import vib.auxiliary.environmentmanager.core.io.audio.IAudioSender;
import vib.auxiliary.environmentmanager.core.io.bap.IBAPReceiver;
import vib.auxiliary.environmentmanager.core.io.bap.IBAPSender;
import vib.auxiliary.environmentmanager.core.io.fap.IFAPReceiver;
import vib.auxiliary.environmentmanager.core.io.fap.IFAPSender;
import vib.auxiliary.environmentmanager.core.io.message.IMessageReceiver;
import vib.auxiliary.environmentmanager.core.io.message.IMessageSender;
import vib.core.animation.mpeg4.MPEG4Animatable;

/**
 *
 * @author Brice Donval
 */
public interface IEnvironmentServer {

    public String getIdentifier();

    /* -------------------------------------------------- */

    public boolean isPrimary();

    public String getType();

    public String getProtocol();

    public String getHost();

    public String getStartingPort();

    public String getEndingPort();

    /* ---------------------------------------------------------------------- */

    public MPEG4Animatable getLocalMPEG4Animatable(String mpeg4AnimatableId);

    public MPEG4Animatable getDistantMPEG4Animatable(String mpeg4AnimatableId);

    public Map<String, MPEG4Animatable> getLocalMPEG4Animatables();

    public Map<String, MPEG4Animatable> getDistantMPEG4Animatables();

    /* ---------------------------------------------------------------------- */

    public IMessageSender getMessageSender();

    public IMessageReceiver getMessageReceiver();

    /* -------------------------------------------------- */

    public IFAPSender getFAPSenderOf(String mpeg4AnimatableId);

    public IFAPReceiver getFAPReceiverOf(String mpeg4AnimatableId);

    /* -------------------------------------------------- */

    public IBAPSender getBAPSenderOf(String mpeg4AnimatableId);

    public IBAPReceiver getBAPReceiverOf(String mpeg4AnimatableId);

    /* -------------------------------------------------- */

    public IAudioSender getAudioSenderOf(String mpeg4AnimatableId);

    public IAudioReceiver getAudioReceiverOf(String mpeg4AnimatableId);

    /* ---------------------------------------------------------------------- */

    public void onHostChange(String host);

    public void onPortRangeChange(String startingPort, String endingPort);

    /* ---------------------------------------------------------------------- */

    public void sendMessage(String message);

    public void sendMessage(String message, Map<String, String> details);

    /* ------------------------------ */

    public void sendMessageTo(String recipientId, String message);

    public void sendMessageTo(String recipientId, String message, Map<String, String> details);

    /* -------------------------------------------------- */

    public void receiveMessage(String message, Map<String, String> details);

    /* ---------------------------------------------------------------------- */

    public void onDestroy();

}
