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
package greta.auxiliary.environmentmanager.core;

import greta.auxiliary.environmentmanager.core.io.audio.IAudioReceiver;
import greta.auxiliary.environmentmanager.core.io.audio.IAudioSender;
import greta.auxiliary.environmentmanager.core.io.bap.IBAPReceiver;
import greta.auxiliary.environmentmanager.core.io.bap.IBAPSender;
import greta.auxiliary.environmentmanager.core.io.fap.IFAPReceiver;
import greta.auxiliary.environmentmanager.core.io.fap.IFAPSender;
import greta.auxiliary.environmentmanager.core.io.message.IMessageReceiver;
import greta.auxiliary.environmentmanager.core.io.message.IMessageSender;
import greta.auxiliary.environmentmanager.core.mvc.EnvironmentManagerController;
import greta.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;
import greta.auxiliary.environmentmanager.core.util.EnvironmentManagerConstants;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.audio.Mixer;
import greta.core.util.environment.Environment;
import greta.core.util.environment.EnvironmentEventListener;
import greta.core.util.environment.LeafEvent;
import greta.core.util.environment.Node;
import greta.core.util.environment.NodeEvent;
import greta.core.util.environment.TreeEvent;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractEnvironmentServer implements IEnvironmentManagerCore, EnvironmentEventListener {

    private final ID identifier;

    /* ------------------------------ */

    private final Class<? extends IMessageSender> MessageSender;
    private final Class<? extends IMessageReceiver> MessageReceiver;

    private final Class<? extends IFAPSender> FAPSender;
    private final Class<? extends IFAPReceiver> FAPReceiver;

    private final Class<? extends IBAPSender> BAPSender;
    private final Class<? extends IBAPReceiver> BAPReceiver;

    private final Class<? extends IAudioSender> AudioSender;
    private final Class<? extends IAudioReceiver> AudioReceiver;

    /* ------------------------------ */

    private String protocol;
    private String host;
    private String startingPort;
    private String endingPort;

    /* -------------------------------------------------- */

    private Environment localEnvironment;

    private final List<String> pendingNodes;

    private final Map<String, MPEG4Animatable> localMPEG4Animatables;
    private final Map<String, MPEG4Animatable> distantMPEG4Animatables;

    /* -------------------------------------------------- */

    private String primaryEnvironmentServerId;
    private final List<String> replicaEnvironmentServerIds;

    /* -------------------------------------------------- */

    private IMessageSender messageSender;
    private IMessageReceiver messageReceiver;

    private final Map<String, IFAPSender> fapSenders;
    private final Map<String, IFAPReceiver> fapReceivers;

    private final Map<String, IBAPSender> bapSenders;
    private final Map<String, IBAPReceiver> bapReceivers;

    private final Map<String, IAudioSender> audioSenders;
    private final Map<String, IAudioReceiver> audioReceivers;

    /* -------------------------------------------------- */

    private EnvironmentManagerController controller;

    /* ---------------------------------------------------------------------- */

    public AbstractEnvironmentServer(
            String protocol,
            Class<? extends IMessageSender> MessageSender,
            Class<? extends IMessageReceiver> MessageReceiver,
            Class<? extends IFAPSender> FAPSender,
            Class<? extends IFAPReceiver> FAPReceiver,
            Class<? extends IBAPSender> BAPSender,
            Class<? extends IBAPReceiver> BAPReceiver,
            Class<? extends IAudioSender> AudioSender,
            Class<? extends IAudioReceiver> AudioReceiver
    ) {
        this.identifier = IDProvider.createID(getClass().getSimpleName());

        this.MessageSender = MessageSender;
        this.MessageReceiver = MessageReceiver;

        this.FAPSender = FAPSender;
        this.FAPReceiver = FAPReceiver;

        this.BAPSender = BAPSender;
        this.BAPReceiver = BAPReceiver;

        this.AudioSender = AudioSender;
        this.AudioReceiver = AudioReceiver;

        this.protocol = protocol;
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.host = "0.0.0.0";
        }
        this.startingPort = EnvironmentManagerConstants.Network_StartingPort;
        this.endingPort = EnvironmentManagerConstants.Network_EndingPort;

        this.pendingNodes = Collections.synchronizedList(new ArrayList<String>());

        this.localMPEG4Animatables = Collections.synchronizedMap(new HashMap<String, MPEG4Animatable>());
        this.distantMPEG4Animatables = Collections.synchronizedMap(new HashMap<String, MPEG4Animatable>());

        this.replicaEnvironmentServerIds = Collections.synchronizedList(new ArrayList<String>());

        try {
            messageSender = MessageSender.getConstructor(IEnvironmentServer.class).newInstance(this);
            messageReceiver = MessageReceiver.getConstructor(IEnvironmentServer.class).newInstance(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.fapSenders = Collections.synchronizedMap(new HashMap<String, IFAPSender>());
        this.fapReceivers = Collections.synchronizedMap(new HashMap<String, IFAPReceiver>());

        this.bapSenders = Collections.synchronizedMap(new HashMap<String, IBAPSender>());
        this.bapReceivers = Collections.synchronizedMap(new HashMap<String, IBAPReceiver>());

        this.audioSenders = Collections.synchronizedMap(new HashMap<String, IAudioSender>());
        this.audioReceivers = Collections.synchronizedMap(new HashMap<String, IAudioReceiver>());

        try {
            controller = EnvironmentManagerController.class.getConstructor(IEnvironmentManagerCore.class).newInstance(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {

        setLocalEnvironment(null);

        messageSender.destroy();
        messageReceiver.destroy();

        onDestroy();

        super.finalize();
    }

    /* ---------------------------------------------------------------------- */
    /*                           IEnvironmentServer                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public final String getIdentifier() {
        return "[PID" + identifier.getPID() + "] " + identifier.getSource() + "(" + identifier.getNumber() + ")";
    }

    /* -------------------------------------------------- */

    @Override
    public final String getProtocol() {
        return protocol;
    }

    @Override
    public final String getHost() {
        return host;
    }

    @Override
    public final String getStartingPort() {
        return startingPort;
    }

    @Override
    public final String getEndingPort() {
        return endingPort;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void setLocalEnvironment(Environment environment) {

        Environment oldLocalEnvironment = localEnvironment;
        localEnvironment = environment;

        if (oldLocalEnvironment != null) {

            oldLocalEnvironment.removeEnvironementListener(this);

            for (MPEG4Animatable localMPEG4Animatable : new ArrayList<MPEG4Animatable>(localMPEG4Animatables.values())) {
                unsetLocalMPEG4Animatable(localMPEG4Animatable);
                sendRequestGoodbyeMPEG4Animatable(localMPEG4Animatable);
            }

            for (MPEG4Animatable distantMPEG4Animatable : new ArrayList<MPEG4Animatable>(distantMPEG4Animatables.values())) {
                unsetDistantMPEG4Animatable(distantMPEG4Animatable);
                oldLocalEnvironment.removeNode(distantMPEG4Animatable);
            }
        }
    }

    protected void setLocalMPEG4Animatables() {
        List<Node> environmentGuests = localEnvironment.getGuests();
        for (Node environmentGuest : environmentGuests) {
            if (environmentGuest instanceof MPEG4Animatable) {
                MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) environmentGuest;
                setLocalMPEG4Animatable(mpeg4Animatable);
            }
        }
    }

    @Override
    public final Environment getLocalEnvironment() {
        return localEnvironment;
    }

    /* -------------------------------------------------- */

    protected final void setPendingNode(String nodeId) {
        pendingNodes.add(nodeId);
    }

    protected final void unsetPendingNode(String nodeId) {
        pendingNodes.remove(nodeId);
    }

    protected final boolean isPendingNode(String nodeId) {
        return pendingNodes.contains(nodeId);
    }

    protected final List<String> getPendingNodes() {
        return pendingNodes;
    }

    /* -------------------------------------------------- */

    @Override
    public final boolean isLocalMPEG4Animatable(String mpeg4AnimatableId) {
        return localMPEG4Animatables.containsKey(mpeg4AnimatableId);
    }

    @Override
    public final boolean isDistantMPEG4Animatable(String mpeg4AnimatableId) {
        return distantMPEG4Animatables.containsKey(mpeg4AnimatableId);
    }

    /* ------------------------------ */

    @Override
    public final void setLocalMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String mpeg4AnimatableId = mpeg4Animatable.getIdentifier();

        if (!isLocalMPEG4Animatable(mpeg4AnimatableId)) {

            try {
                IFAPSender fapSender = FAPSender.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);
                IBAPSender bapSender = BAPSender.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);
                IAudioSender audioSender = AudioSender.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);

                mpeg4Animatable.addFAPFramePerformer(fapSender);
                mpeg4Animatable.addBAPFramePerformer(bapSender);
                mpeg4Animatable.addAudioPerformer(audioSender);

                fapSenders.put(mpeg4AnimatableId, fapSender);
                bapSenders.put(mpeg4AnimatableId, bapSender);
                audioSenders.put(mpeg4AnimatableId, audioSender);

                localMPEG4Animatables.put(mpeg4AnimatableId, mpeg4Animatable);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public final void setDistantMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String mpeg4AnimatableId = mpeg4Animatable.getIdentifier();

        if (!isDistantMPEG4Animatable(mpeg4AnimatableId)) {

            try {
                IFAPReceiver fapReceiver = FAPReceiver.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);
                IBAPReceiver bapReceiver = BAPReceiver.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);
                IAudioReceiver audioReceiver = AudioReceiver.getConstructor(IEnvironmentServer.class, MPEG4Animatable.class).newInstance(this, mpeg4Animatable);

                fapReceiver.addFAPFramePerformer(mpeg4Animatable);
                bapReceiver.addBAPFramePerformer(mpeg4Animatable);
                audioReceiver.addAudioPerformer(mpeg4Animatable);

                fapReceivers.put(mpeg4AnimatableId, fapReceiver);
                bapReceivers.put(mpeg4AnimatableId, bapReceiver);
                audioReceivers.put(mpeg4AnimatableId, audioReceiver);

                distantMPEG4Animatables.put(mpeg4AnimatableId, mpeg4Animatable);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /* ------------------------------ */

    @Override
    public final void unsetLocalMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String mpeg4AnimatableId = mpeg4Animatable.getIdentifier();

        if (localMPEG4Animatables.get(mpeg4AnimatableId) != null) {

            IFAPSender fapSender = fapSenders.get(mpeg4AnimatableId);
            IBAPSender bapSender = bapSenders.get(mpeg4AnimatableId);
            IAudioSender audioSender = audioSenders.get(mpeg4AnimatableId);

            mpeg4Animatable.removeFAPFramePerformer(fapSender);
            mpeg4Animatable.removeBAPFramePerformer(bapSender);
            mpeg4Animatable.removeAudioPerformer(audioSender);

            fapSenders.remove(mpeg4AnimatableId);
            bapSenders.remove(mpeg4AnimatableId);
            audioSenders.remove(mpeg4AnimatableId);

            fapSender.destroy();
            bapSender.destroy();
            audioSender.destroy();

            localMPEG4Animatables.remove(mpeg4AnimatableId);
        }
    }

    @Override
    public final void unsetDistantMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String mpeg4AnimatableId = mpeg4Animatable.getIdentifier();

        if (distantMPEG4Animatables.get(mpeg4AnimatableId) != null) {

            IFAPReceiver fapReceiver = fapReceivers.get(mpeg4AnimatableId);
            IBAPReceiver bapReceiver = bapReceivers.get(mpeg4AnimatableId);
            IAudioReceiver audioReceiver = audioReceivers.get(mpeg4AnimatableId);

            fapReceiver.removeFAPFramePerformer(mpeg4Animatable);
            bapReceiver.removeBAPFramePerformer(mpeg4Animatable);
            audioReceiver.removeAudioPerformer(mpeg4Animatable);

            fapReceivers.remove(mpeg4AnimatableId);
            bapReceivers.remove(mpeg4AnimatableId);
            audioReceivers.remove(mpeg4AnimatableId);

            fapReceiver.destroy();
            bapReceiver.destroy();
            audioReceiver.destroy();

            distantMPEG4Animatables.remove(mpeg4AnimatableId);
        }
    }

    /* ------------------------------ */

    @Override
    public final MPEG4Animatable getLocalMPEG4Animatable(String mpeg4AnimatableId) {
        return localMPEG4Animatables.get(mpeg4AnimatableId);
    }

    @Override
    public final MPEG4Animatable getDistantMPEG4Animatable(String mpeg4AnimatableId) {
        return distantMPEG4Animatables.get(mpeg4AnimatableId);
    }

    @Override
    public final Map<String, MPEG4Animatable> getLocalMPEG4Animatables() {
        return localMPEG4Animatables;
    }

    @Override
    public final Map<String, MPEG4Animatable> getDistantMPEG4Animatables() {
        return distantMPEG4Animatables;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final String getPrimaryEnvironmentServerId() {
        return primaryEnvironmentServerId;
    }

    @Override
    public final void setPrimaryEnvironmentServerId(String environmentServerId) {
        primaryEnvironmentServerId = environmentServerId;
    }

    /* -------------------------------------------------- */

    @Override
    public final List<String> getReplicaEnvironmentServerIds() {
        return replicaEnvironmentServerIds;
    }

    @Override
    public final void addReplicaEnvironmentServerId(String environmentServerId) {
        replicaEnvironmentServerIds.add(environmentServerId);
    }

    @Override
    public final void removeReplicaEnvironmentServerId(String environmentServerId) {
        replicaEnvironmentServerIds.remove(environmentServerId);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final IMessageSender getMessageSender() {
        return messageSender;
    }

    @Override
    public final IMessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /* -------------------------------------------------- */

    @Override
    public final IFAPSender getFAPSenderOf(String mpeg4AnimatableId) {
        return fapSenders.get(mpeg4AnimatableId);
    }

    @Override
    public final IFAPReceiver getFAPReceiverOf(String mpeg4AnimatableId) {
        return fapReceivers.get(mpeg4AnimatableId);
    }

    /* -------------------------------------------------- */

    @Override
    public final IBAPSender getBAPSenderOf(String mpeg4AnimatableId) {
        return bapSenders.get(mpeg4AnimatableId);
    }

    @Override
    public final IBAPReceiver getBAPReceiverOf(String mpeg4AnimatableId) {
        return bapReceivers.get(mpeg4AnimatableId);
    }

    /* -------------------------------------------------- */

    @Override
    public final IAudioSender getAudioSenderOf(String mpeg4AnimatableId) {
        return audioSenders.get(mpeg4AnimatableId);
    }

    @Override
    public final IAudioReceiver getAudioReceiverOf(String mpeg4AnimatableId) {
        return audioReceivers.get(mpeg4AnimatableId);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final EnvironmentManagerController getController() {
        return controller;
    }

    /* -------------------------------------------------- */

    @Override
    public final void messageSenderHasConnected(String port) {
        controller.messageSenderHasConnected(port);
    }

    @Override
    public final void messageSenderHasDisconnected(String port) {
        controller.messageSenderHasDisconnected(port);
    }

    @Override
    public final void messageSenderHasDisappeared() {
        controller.messageSenderHasDisappeared();
    }

    /* ------------------------------ */

    @Override
    public final void messageReceiverHasConnected(String port) {
        controller.messageReceiverHasConnected(port);
    }

    @Override
    public final void messageReceiverHasDisconnected(String port) {
        controller.messageReceiverHasDisconnected(port);
    }

    @Override
    public final void messageReceiverHasDisappeared() {
        controller.messageReceiverHasDisappeared();
    }

    /* -------------------------------------------------- */

    @Override
    public final void fapSenderHasConnected(String port) {
        controller.fapSenderHasConnected(port);
    }

    @Override
    public final void fapSenderHasDisconnected(String port) {
        controller.fapSenderHasDisconnected(port);
    }

    @Override
    public final void fapSenderHasDisappeared() {
        controller.fapSenderHasDisappeared();
    }

    /* ------------------------------ */

    @Override
    public final void fapReceiverHasConnected(String port) {
        controller.fapReceiverHasConnected(port);
    }

    @Override
    public final void fapReceiverHasDisconnected(String port) {
        controller.fapReceiverHasDisconnected(port);
    }

    @Override
    public final void fapReceiverHasDisappeared() {
        controller.fapReceiverHasDisappeared();
    }

    /* -------------------------------------------------- */

    @Override
    public final void bapSenderHasConnected(String port) {
        controller.bapSenderHasConnected(port);
    }

    @Override
    public final void bapSenderHasDisconnected(String port) {
        controller.bapSenderHasDisconnected(port);
    }

    @Override
    public final void bapSenderHasDisappeared() {
        controller.bapSenderHasDisappeared();
    }

    /* ------------------------------ */

    @Override
    public final void bapReceiverHasConnected(String port) {
        controller.bapReceiverHasConnected(port);
    }

    @Override
    public final void bapReceiverHasDisconnected(String port) {
        controller.bapReceiverHasDisconnected(port);
    }

    @Override
    public final void bapReceiverHasDisappeared() {
        controller.bapReceiverHasDisappeared();
    }

    /* -------------------------------------------------- */

    @Override
    public final void audioSenderHasConnected(String port) {
        controller.audioSenderHasConnected(port);
    }

    @Override
    public final void audioSenderHasDisconnected(String port) {
        controller.audioSenderHasDisconnected(port);
    }

    @Override
    public final void audioSenderHasDisappeared() {
        controller.audioSenderHasDisappeared();
    }

    /* ------------------------------ */

    @Override
    public final void audioReceiverHasConnected(String port) {
        controller.audioReceiverHasConnected(port);
    }

    @Override
    public final void audioReceiverHasDisconnected(String port) {
        controller.audioReceiverHasDisconnected(port);
    }

    @Override
    public final void audioReceiverHasDisappeared() {
        controller.audioReceiverHasDisappeared();
    }

    /* -------------------------------------------------- */

    @Override
    public final void hostHasChanged(String host) {
        this.host = host;
        onHostChange(host);
    }

    /* ------------------------------ */

    @Override
    public final void portRangeHasChanged(String startingPort, String endingPort) {
        this.startingPort = startingPort;
        this.endingPort = endingPort;
        onPortRangeChange(startingPort, endingPort);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void sendMessage(String message) {
        sendMessage(message, new HashMap<String, String>());
    }

    @Override
    public final void sendMessageTo(String recipientId, String message) {
        sendMessageTo(recipientId, message, new HashMap<String, String>());
    }

    /* -------------------------------------------------- */

    protected final void sendRequestWelcomeMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String message = EnvironmentManagerConstants.Request_WelcomeMPEG4Animatable;

        Map<String, String> details = new HashMap<String, String>();
        details.put(EnvironmentManagerConstants.Property_MPEG4AnimatableId, mpeg4Animatable.getIdentifier());
        details.put(EnvironmentManagerConstants.Property_XMLContent, mpeg4Animatable.asXML().toString());
        details.put(EnvironmentManagerConstants.Property_FAPFrame, mpeg4Animatable.getCurrentFAPFrame().toString());
        details.put(EnvironmentManagerConstants.Property_BAPFrame, mpeg4Animatable.getCurrentBAPFrame().toString());

        sendMessage(message, details);
    }

    protected final void sendRequestGoodbyeMPEG4Animatable(MPEG4Animatable mpeg4Animatable) {

        String message = EnvironmentManagerConstants.Request_GoodbyeMPEG4Animatable;

        Map<String, String> details = new HashMap<String, String>();
        details.put(EnvironmentManagerConstants.Property_MPEG4AnimatableId, mpeg4Animatable.getIdentifier());

        sendMessage(message, details);
    }

    /* ---------------------------------------------------------------------- */
    /*                        EnvironmentEventListener                        */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onTreeChange(TreeEvent te) {

        if (!isPendingNode(te.childNode.getIdentifier())) {

            if (!(te.childNode instanceof Mixer)) {

                String message = EnvironmentManagerConstants.Request_ChangeTree;

                Map<String, String> details = new HashMap<String, String>();
                details.put(EnvironmentManagerConstants.Property_TreeModifType, String.valueOf(te.modifType));
                details.put(EnvironmentManagerConstants.Property_ChildNodeId, te.getIdChildNode());

                switch (te.modifType) {

                    case TreeEvent.MODIF_ADD: {

                        if (te.childNode instanceof MPEG4Animatable) {

                            MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) te.childNode;
                            if (!isDistantMPEG4Animatable(mpeg4Animatable.getIdentifier())) {
                                setLocalMPEG4Animatable(mpeg4Animatable);
                                sendRequestWelcomeMPEG4Animatable(mpeg4Animatable);
                            }

                        } else {

                            details.put(EnvironmentManagerConstants.Property_XMLContent, te.childNode.asXML().toString());
                            if (te.getIdNewParentNode().equals(localEnvironment.getRoot().getIdentifier())) {
                                details.put(EnvironmentManagerConstants.Property_NewParentNodeId, EnvironmentManagerConstants.Value_RootNode);
                            } else {
                                details.put(EnvironmentManagerConstants.Property_NewParentNodeId, te.getIdNewParentNode());
                            }
                            sendMessage(message, details);

                        }

                        break;
                    }

                    case TreeEvent.MODIF_MOVE: {

                        if (te.getIdNewParentNode().equals(localEnvironment.getRoot().getIdentifier())) {
                            details.put(EnvironmentManagerConstants.Property_NewParentNodeId, EnvironmentManagerConstants.Value_RootNode);
                        } else {
                            details.put(EnvironmentManagerConstants.Property_NewParentNodeId, te.getIdNewParentNode());
                        }
                        sendMessage(message, details);

                        break;
                    }

                    case TreeEvent.MODIF_REMOVE: {

                        if (te.childNode instanceof MPEG4Animatable) {

                            MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) te.childNode;
                            if (isLocalMPEG4Animatable(mpeg4Animatable.getIdentifier())) {
                                unsetLocalMPEG4Animatable(mpeg4Animatable);
                            } else if (isDistantMPEG4Animatable(mpeg4Animatable.getIdentifier())) {
                                unsetDistantMPEG4Animatable(mpeg4Animatable);
                            }
                            sendRequestGoodbyeMPEG4Animatable(mpeg4Animatable);

                        } else {

                            if (te.getIdOldParentNode().equals(localEnvironment.getRoot().getIdentifier())) {
                                details.put(EnvironmentManagerConstants.Property_OldParentNodeId, EnvironmentManagerConstants.Value_RootNode);
                            } else {
                                details.put(EnvironmentManagerConstants.Property_OldParentNodeId, te.getIdOldParentNode());
                            }
                            sendMessage(message, details);

                        }

                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onNodeChange(NodeEvent ne) {

        if (!isPendingNode(ne.node.getIdentifier())) {

            if (!(ne.node instanceof Mixer)) {

                String message = EnvironmentManagerConstants.Request_ChangeNode;

                Map<String, String> details = new HashMap<String, String>();
                details.put(EnvironmentManagerConstants.Property_NodeModifType, String.valueOf(ne.modifType));
                details.put(EnvironmentManagerConstants.Property_NodeId, ne.getIdNode());

                switch (ne.modifType) {

                    case NodeEvent.MODIF_POSITION: {

                        details.put(EnvironmentManagerConstants.Property_NodePositionX, String.valueOf(ne.node.getCoordinates().x()));
                        details.put(EnvironmentManagerConstants.Property_NodePositionY, String.valueOf(ne.node.getCoordinates().y()));
                        details.put(EnvironmentManagerConstants.Property_NodePositionZ, String.valueOf(ne.node.getCoordinates().z()));
                        sendMessage(message, details);

                        break;
                    }

                    case NodeEvent.MODIF_ROTATION: {

                        details.put(EnvironmentManagerConstants.Property_NodeOrientationX, String.valueOf(ne.node.getOrientation().x()));
                        details.put(EnvironmentManagerConstants.Property_NodeOrientationY, String.valueOf(ne.node.getOrientation().y()));
                        details.put(EnvironmentManagerConstants.Property_NodeOrientationZ, String.valueOf(ne.node.getOrientation().z()));
                        details.put(EnvironmentManagerConstants.Property_NodeOrientationW, String.valueOf(ne.node.getOrientation().w()));
                        sendMessage(message, details);

                        break;
                    }

                    case NodeEvent.MODIF_SCALE: {

                        details.put(EnvironmentManagerConstants.Property_NodeScaleX, String.valueOf(ne.node.getScale().x()));
                        details.put(EnvironmentManagerConstants.Property_NodeScaleY, String.valueOf(ne.node.getScale().y()));
                        details.put(EnvironmentManagerConstants.Property_NodeScaleZ, String.valueOf(ne.node.getScale().z()));
                        sendMessage(message, details);

                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLeafChange(LeafEvent le) {

        if (!isPendingNode(le.leaf.getIdentifier())) {

            String message = EnvironmentManagerConstants.Request_ChangeLeaf;

            Map<String, String> details = new HashMap<String, String>();
            details.put(EnvironmentManagerConstants.Property_LeafModifType, String.valueOf(le.modifType));
            details.put(EnvironmentManagerConstants.Property_LeafId, le.getIdLeaf());

            switch (le.modifType) {

                case LeafEvent.MODIF_REFERENCE: {

                    details.put(EnvironmentManagerConstants.Property_LeafReference, le.leaf.getReference());
                    sendMessage(message, details);

                    break;
                }

                case LeafEvent.MODIF_SIZE: {

                    details.put(EnvironmentManagerConstants.Property_LeafSizeX, String.valueOf(le.leaf.getSize().x()));
                    details.put(EnvironmentManagerConstants.Property_LeafSizeY, String.valueOf(le.leaf.getSize().y()));
                    details.put(EnvironmentManagerConstants.Property_LeafSizeZ, String.valueOf(le.leaf.getSize().z()));
                    sendMessage(message, details);

                    break;
                }
            }
        }
    }

}
