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
import greta.auxiliary.environmentmanager.core.util.EnvironmentManagerConstants;
import greta.auxiliary.environmentmanager.util.EnvironmentLoader;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.LeafEvent;
import greta.core.util.environment.Node;
import greta.core.util.environment.NodeEvent;
import greta.core.util.environment.TreeEvent;
import greta.core.util.environment.TreeNode;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractPrimaryEnvironmentServer extends AbstractEnvironmentServer {

    public AbstractPrimaryEnvironmentServer(
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
        super(
                protocol,
                MessageSender,
                MessageReceiver,
                FAPSender,
                FAPReceiver,
                BAPSender,
                BAPReceiver,
                AudioSender,
                AudioReceiver
        );

        setPrimaryEnvironmentServerId(this.getIdentifier());
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final boolean isPrimary() {
        return true;
    }

    @Override
    public final String getType() {
        return "Primary";
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void setLocalEnvironment(Environment environment) {
        super.setLocalEnvironment(environment);
        if (environment != null) {
            setLocalMPEG4Animatables();
            environment.addEnvironementListener(this);
        }
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void sendMessage(String message, Map<String, String> details) {
        sendMessageTo(EnvironmentManagerConstants.Value_MessageForAllReplicas, message, details);
    }

    @Override
    public final void sendMessageTo(String recipientId, String message, Map<String, String> details) {
        details.put(EnvironmentManagerConstants.Property_MessageSenderId, this.getIdentifier());
        details.put(EnvironmentManagerConstants.Property_MessageRecipientId, recipientId);
        getMessageSender().send(message, details);
    }

    /* ------------------------------ */

    @Override
    public final void receiveMessage(String message, Map<String, String> details) {

        String senderId = details.get(EnvironmentManagerConstants.Property_MessageSenderId);
        String recipientId = details.get(EnvironmentManagerConstants.Property_MessageRecipientId);

        if (!senderId.equals(this.getIdentifier())) {
            if (recipientId.equals(EnvironmentManagerConstants.Value_MessageForPrimary)) {
                receivedMessageForMe(senderId, message, details);
            }
        }
    }

    private void receivedMessageForMe(String senderId, String message, Map<String, String> details) {
        if (!getReplicaEnvironmentServerIds().contains(senderId)) {
            if (message.equals(EnvironmentManagerConstants.Request_RegisterReplica)) {
                receivedRequestRegisterReplica(senderId, message, details);
            }
        } else {
            if (message.equals(EnvironmentManagerConstants.Request_UnregisterReplica)) {
                receivedRequestUnregisterReplica(senderId, message, details);
            } else if (message.equals(EnvironmentManagerConstants.Request_GetSyncEnvironment)) {
                receivedRequestGetSyncEnvironment(senderId, message, details);
            } else {
                if (getLocalEnvironment() != null) {
                    if (message.equals(EnvironmentManagerConstants.Request_WelcomeMPEG4Animatable)) {
                        receivedRequestWelcomeMPEG4Animatable(senderId, message, details);
                    } else if (message.equals(EnvironmentManagerConstants.Request_GoodbyeMPEG4Animatable)) {
                        receivedRequestGoodbyeMPEG4Animatable(senderId, message, details);
                    } else if (message.equals(EnvironmentManagerConstants.Request_ChangeTree)) {
                        receivedRequestChangeTree(senderId, message, details);
                    } else if (message.equals(EnvironmentManagerConstants.Request_ChangeNode)) {
                        receivedRequestChangeNode(senderId, message, details);
                    } else if (message.equals(EnvironmentManagerConstants.Request_ChangeLeaf)) {
                        receivedRequestChangeLeaf(senderId, message, details);
                    }
                }
            }
        }
    }

    /* -------------------------------------------------- */

    private void sendRequestRegisterPrimaryTo(String recipientId) {

        String message = EnvironmentManagerConstants.Request_RegisterPrimary;

        Map<String, String> details = new HashMap<String, String>();
        details.put(EnvironmentManagerConstants.Property_Time, String.valueOf(Timer.getTimeMillis()));

        sendMessageTo(recipientId, message, details);
    }

    public void sendRequestUnregisterPrimary() {
        sendMessage(EnvironmentManagerConstants.Request_UnregisterPrimary);
    }

    private void sendRequestWelcomeEnvironmentTo(String recipientId) {

        String message = EnvironmentManagerConstants.Request_WelcomeEnvironment;

        Map<String, String> details = new HashMap<String, String>();
        details.put(EnvironmentManagerConstants.Property_XMLContent, getLocalEnvironment().asXML().toString());

        sendMessageTo(recipientId, message, details);
    }

    private void sendRequestsWelcomeMPEG4AnimatablesTo(String recipientId) {
        List<Node> environmentGuests = getLocalEnvironment().getGuests();
        for (Node environmentGuest : environmentGuests) {
            if (environmentGuest instanceof MPEG4Animatable) {
                MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) environmentGuest;
                sendRequestWelcomeMPEG4AnimatableTo(recipientId, mpeg4Animatable);
            }
        }
    }

    private void sendRequestWelcomeMPEG4AnimatableTo(String recipientId, MPEG4Animatable mpeg4Animatable) {

        String message = EnvironmentManagerConstants.Request_WelcomeMPEG4Animatable;

        Map<String, String> details = new HashMap<String, String>();
        details.put(EnvironmentManagerConstants.Property_MPEG4AnimatableId, mpeg4Animatable.getIdentifier());
        details.put(EnvironmentManagerConstants.Property_XMLContent, mpeg4Animatable.asXML().toString());
        details.put(EnvironmentManagerConstants.Property_FAPFrame, mpeg4Animatable.getCurrentFAPFrame().toString());
        details.put(EnvironmentManagerConstants.Property_BAPFrame, mpeg4Animatable.getCurrentBAPFrame().toString());

        sendMessageTo(recipientId, message, details);
    }

    private void sendRequestUnableToGetSyncEnvironmentTo(String recipientId) {
        sendMessageTo(recipientId, EnvironmentManagerConstants.Request_UnableToGetSyncEnvironment);
    }

    /* -------------------------------------------------- */

    private void receivedRequestRegisterReplica(String senderId, String message, Map<String, String> details) {
        addReplicaEnvironmentServerId(senderId);
        sendRequestRegisterPrimaryTo(senderId);
    }

    private void receivedRequestUnregisterReplica(String senderId, String message, Map<String, String> details) {
        removeReplicaEnvironmentServerId(senderId);
    }

    private void receivedRequestGetSyncEnvironment(String senderId, String message, Map<String, String> details) {
        if (getReplicaEnvironmentServerIds().contains(senderId)) {
            if (getLocalEnvironment() != null) {
                sendRequestWelcomeEnvironmentTo(senderId);
                sendRequestsWelcomeMPEG4AnimatablesTo(senderId);
            } else {
                sendRequestUnableToGetSyncEnvironmentTo(senderId);
            }
        }
    }

    private void receivedRequestWelcomeMPEG4Animatable(String senderId, String message, Map<String, String> details) {

        String receivedMPEG4AnimatableId = details.get(EnvironmentManagerConstants.Property_MPEG4AnimatableId);
        String receivedXMLContent = details.get(EnvironmentManagerConstants.Property_XMLContent);
        String receivedFAPFrame = details.get(EnvironmentManagerConstants.Property_FAPFrame);
        String receivedBAPFrame = details.get(EnvironmentManagerConstants.Property_BAPFrame);

        if (!isLocalMPEG4Animatable(receivedMPEG4AnimatableId) && !isDistantMPEG4Animatable(receivedMPEG4AnimatableId)) {

            setPendingNode(receivedMPEG4AnimatableId);
            MPEG4Animatable receivedMPEG4Animatable = EnvironmentLoader.loadMPEG4AnimatableWithBuffer(getLocalEnvironment(), getLocalEnvironment().getRoot(), receivedXMLContent);
            unsetPendingNode(receivedMPEG4AnimatableId);

            if (receivedMPEG4Animatable != null) {

                FAPFrame initialfapFrame = new FAPFrame();
                BAPFrame initialbapFrame = new BAPFrame();

                initialfapFrame.readFromString(receivedFAPFrame);
                initialbapFrame.readFromString(receivedBAPFrame);

                List<FAPFrame> initialFAPFrames = new ArrayList<FAPFrame>();
                List<BAPFrame> initialBAPFrames = new ArrayList<BAPFrame>();

                initialFAPFrames.add(initialfapFrame);
                initialBAPFrames.add(initialbapFrame);

                receivedMPEG4Animatable.performFAPFrames(initialFAPFrames, IDProvider.createID(getIdentifier()));
                receivedMPEG4Animatable.performBAPFrames(initialBAPFrames, IDProvider.createID(getIdentifier()));

                setDistantMPEG4Animatable(receivedMPEG4Animatable);
                sendRequestWelcomeMPEG4Animatable(receivedMPEG4Animatable);
            }
        }
    }

    private void receivedRequestGoodbyeMPEG4Animatable(String senderId, String message, Map<String, String> details) {

        String receivedMPEG4AnimatableId = details.get(EnvironmentManagerConstants.Property_MPEG4AnimatableId);
        MPEG4Animatable receivedMPEG4Animatable = (MPEG4Animatable) getLocalEnvironment().getNode(receivedMPEG4AnimatableId);

        if (receivedMPEG4Animatable != null) {

            if (isLocalMPEG4Animatable(receivedMPEG4AnimatableId) || isDistantMPEG4Animatable(receivedMPEG4AnimatableId)) {

                if (isLocalMPEG4Animatable(receivedMPEG4AnimatableId)) {
                    unsetLocalMPEG4Animatable(receivedMPEG4Animatable);
                } else if (isDistantMPEG4Animatable(receivedMPEG4AnimatableId)) {
                    unsetDistantMPEG4Animatable(receivedMPEG4Animatable);
                }

                setPendingNode(receivedMPEG4AnimatableId);
                getLocalEnvironment().removeNode(receivedMPEG4Animatable);
                unsetPendingNode(receivedMPEG4AnimatableId);

                sendRequestGoodbyeMPEG4Animatable(receivedMPEG4Animatable);
            }
        }
    }

    private void receivedRequestChangeTree(String senderId, String message, Map<String, String> details) {

        String receivedChildNodeId = details.get(EnvironmentManagerConstants.Property_ChildNodeId);
        Node childNode = getLocalEnvironment().getNode(receivedChildNodeId);

        int receivedTreeModifType = Integer.parseInt(details.get(EnvironmentManagerConstants.Property_TreeModifType));
        switch (receivedTreeModifType) {

            case TreeEvent.MODIF_ADD: {

                if (childNode == null) {

                    String receivedXMLContent = details.get(EnvironmentManagerConstants.Property_XMLContent);
                    String receivedNewParentNodeId = details.get(EnvironmentManagerConstants.Property_NewParentNodeId);
                    if (receivedNewParentNodeId.equals(EnvironmentManagerConstants.Value_RootNode)) {
                        receivedNewParentNodeId = getLocalEnvironment().getRoot().getIdentifier();
                    }
                    TreeNode newParentNode = (TreeNode) getLocalEnvironment().getNode(receivedNewParentNodeId);
                    if (newParentNode != null) {
                        setPendingNode(receivedChildNodeId);
                        EnvironmentLoader.loadUnknownChildWithBuffer(getLocalEnvironment(), newParentNode, receivedXMLContent);
                        unsetPendingNode(receivedChildNodeId);
                    }

                    break;
                }
            }

            case TreeEvent.MODIF_MOVE: {

                if (childNode != null) {

                    String receivedNewParentNodeId = details.get(EnvironmentManagerConstants.Property_NewParentNodeId);
                    if (receivedNewParentNodeId.equals(EnvironmentManagerConstants.Value_RootNode)) {
                        receivedNewParentNodeId = getLocalEnvironment().getRoot().getIdentifier();
                    }
                    TreeNode newParentNode = (TreeNode) getLocalEnvironment().getNode(receivedNewParentNodeId);
                    if ((newParentNode != null) && (childNode.getParent() != newParentNode)) {
                        setPendingNode(receivedChildNodeId);
                        getLocalEnvironment().moveNode(childNode, newParentNode);
                        unsetPendingNode(receivedChildNodeId);
                    }
                }

                break;
            }

            case TreeEvent.MODIF_REMOVE: {

                if (childNode != null) {

                    String receivedOldParentNodeId = details.get(EnvironmentManagerConstants.Property_OldParentNodeId);
                    if (receivedOldParentNodeId.equals(EnvironmentManagerConstants.Value_RootNode)) {
                        receivedOldParentNodeId = getLocalEnvironment().getRoot().getIdentifier();
                    }
                    TreeNode oldParentNode = (TreeNode) getLocalEnvironment().getNode(receivedOldParentNodeId);
                    if ((oldParentNode != null) && (childNode.getParent() == oldParentNode)) {
                        setPendingNode(receivedChildNodeId);
                        getLocalEnvironment().removeNode(childNode, oldParentNode);
                        unsetPendingNode(receivedChildNodeId);
                    }
                }

                break;
            }
        }

        sendMessage(message, details);
    }

    private void receivedRequestChangeNode(String senderId, String message, Map<String, String> details) {

        String receivedNodeId = details.get(EnvironmentManagerConstants.Property_NodeId);
        TreeNode node = (TreeNode) getLocalEnvironment().getNode(receivedNodeId);

        if (node != null) {

            int receivedNodeModifType = Integer.parseInt(details.get(EnvironmentManagerConstants.Property_NodeModifType));
            switch (receivedNodeModifType) {

                case NodeEvent.MODIF_POSITION: {

                    double receivedNodePositionX = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodePositionX));
                    double receivedNodePositionY = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodePositionY));
                    double receivedNodePositionZ = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodePositionZ));

                    setPendingNode(receivedNodeId);
                    node.setCoordinates(receivedNodePositionX, receivedNodePositionY, receivedNodePositionZ);
                    unsetPendingNode(receivedNodeId);

                    break;
                }

                case NodeEvent.MODIF_ROTATION: {

                    double receivedNodeOrientationX = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeOrientationX));
                    double receivedNodeOrientationY = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeOrientationY));
                    double receivedNodeOrientationZ = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeOrientationZ));
                    double receivedNodeOrientationW = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeOrientationW));

                    setPendingNode(receivedNodeId);
                    node.setOrientation(receivedNodeOrientationX, receivedNodeOrientationY, receivedNodeOrientationZ, receivedNodeOrientationW);
                    unsetPendingNode(receivedNodeId);

                    break;
                }

                case NodeEvent.MODIF_SCALE: {

                    double receivedNodeScaleX = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeScaleX));
                    double receivedNodeScaleY = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeScaleY));
                    double receivedNodeScaleZ = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_NodeScaleZ));

                    setPendingNode(receivedNodeId);
                    node.setScale(receivedNodeScaleX, receivedNodeScaleY, receivedNodeScaleZ);
                    unsetPendingNode(receivedNodeId);

                    break;
                }
            }

            sendMessage(message, details);
        }
    }

    private void receivedRequestChangeLeaf(String senderId, String message, Map<String, String> details) {

        String receivedLeafId = details.get(EnvironmentManagerConstants.Property_LeafId);
        Leaf leaf = (Leaf) getLocalEnvironment().getNode(receivedLeafId);

        if (leaf != null) {

            int receivedLeafModifType = Integer.parseInt(details.get(EnvironmentManagerConstants.Property_LeafModifType));
            switch (receivedLeafModifType) {

                case LeafEvent.MODIF_REFERENCE: {

                    String receivedLeafReference = details.get(EnvironmentManagerConstants.Property_LeafReference);

                    setPendingNode(receivedLeafId);
                    leaf.setReference(receivedLeafReference);
                    unsetPendingNode(receivedLeafId);

                    break;
                }

                case LeafEvent.MODIF_SIZE: {

                    double receivedLeafSizeX = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_LeafSizeX));
                    double receivedLeafSizeY = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_LeafSizeY));
                    double receivedLeafSizeZ = Double.parseDouble(details.get(EnvironmentManagerConstants.Property_LeafSizeZ));

                    setPendingNode(receivedLeafId);
                    leaf.setSize(receivedLeafSizeX, receivedLeafSizeY, receivedLeafSizeZ);
                    unsetPendingNode(receivedLeafId);

                    break;
                }
            }

            sendMessage(message, details);
        }
    }

}
