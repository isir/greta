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
package greta.auxiliary.environmentmanager.core;

import greta.auxiliary.environmentmanager.core.io.audio.IAudioReceiver;
import greta.auxiliary.environmentmanager.core.io.audio.IAudioSender;
import greta.auxiliary.environmentmanager.core.io.bap.IBAPReceiver;
import greta.auxiliary.environmentmanager.core.io.bap.IBAPSender;
import greta.auxiliary.environmentmanager.core.io.fap.IFAPReceiver;
import greta.auxiliary.environmentmanager.core.io.fap.IFAPSender;
import greta.auxiliary.environmentmanager.core.io.message.IMessageReceiver;
import greta.auxiliary.environmentmanager.core.io.message.IMessageSender;
import greta.core.animation.mpeg4.MPEG4Animatable;
import java.util.Map;

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
