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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractReplicaEnvironmentServer extends AbstractEnvironmentServer {

    public AbstractReplicaEnvironmentServer(
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
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final boolean isPrimary() {
        return false;
    }

    @Override
    public final String getType() {
        return "Replica";
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void setLocalEnvironment(Environment environment) {
        super.setLocalEnvironment(environment);
        if (environment != null) {
            sendRequestGetSyncEnvironment();
        }
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void sendMessage(String message, Map<String, String> details) {
        sendMessageTo(EnvironmentManagerConstants.Value_MessageForPrimary, message, details);
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

        if (recipientId.equals(EnvironmentManagerConstants.Value_MessageForAllReplicas)) {
            receivedMessageForAllReplicas(senderId, message, details);
        } else if (recipientId.equals(this.getIdentifier())) {
            receivedMessageForMe(senderId, recipientId, message, details);
        }
    }

    private void receivedMessageForAllReplicas(String senderId, String message, Map<String, String> details) {
        if (getPrimaryEnvironmentServerId().equals(senderId)) {
            if (message.equals(EnvironmentManagerConstants.Request_UnregisterPrimary)) {
                receivedRequestUnregisterPrimary(senderId, message, details);
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

    private void receivedMessageForMe(String senderId, String recipientId, String message, Map<String, String> details) {
        if (getPrimaryEnvironmentServerId() == null) {
            if (message.equals(EnvironmentManagerConstants.Request_RegisterPrimary)) {
                receivedRequestRegisterPrimary(senderId, message, details);
            }
        } else if (getPrimaryEnvironmentServerId().equals(senderId)) {
            if (message.equals(EnvironmentManagerConstants.Request_WelcomeEnvironment)) {
                receivedRequestWelcomeEnvironment(senderId, message, details);
            } else if (message.equals(EnvironmentManagerConstants.Request_WelcomeMPEG4Animatable)) {
                receivedRequestWelcomeMPEG4Animatable(senderId, message, details);
            } else if (message.equals(EnvironmentManagerConstants.Request_UnableToGetSyncEnvironment)) {
                receivedRequestUnableToGetSyncEnvironment(senderId, message, details);
            }
        }
    }

    /* -------------------------------------------------- */

    public void sendRequestRegisterReplica() {
        sendMessage(EnvironmentManagerConstants.Request_RegisterReplica);
    }

    public void sendRequestUnregisterReplica() {
        sendMessage(EnvironmentManagerConstants.Request_UnregisterReplica);
    }

    private void sendRequestGetSyncEnvironment() {
        sendMessage(EnvironmentManagerConstants.Request_GetSyncEnvironment);
    }

    private void sendRequestsWelcomeMPEG4Animatables() {
        List<Node> environmentGuests = getLocalEnvironment().getGuests();
        for (Node environmentGuest : environmentGuests) {
            if (environmentGuest instanceof MPEG4Animatable) {
                MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) environmentGuest;
                sendRequestWelcomeMPEG4Animatable(mpeg4Animatable);
            }
        }
    }

    /* -------------------------------------------------- */

    private void receivedRequestRegisterPrimary(String senderId, String message, Map<String, String> details) {
        setPrimaryEnvironmentServerId(senderId);
        Timer.setTimeMillis(Long.parseLong(details.get(EnvironmentManagerConstants.Property_Time)));
        if (getLocalEnvironment() != null) {
            sendRequestGetSyncEnvironment();
        }
    }

    private void receivedRequestUnregisterPrimary(String senderId, String message, Map<String, String> details) {
        setPrimaryEnvironmentServerId(null);
    }

    private void receivedRequestWelcomeEnvironment(String senderId, String message, Map<String, String> details) {

        setLocalMPEG4Animatables();
        sendRequestsWelcomeMPEG4Animatables();

        String receivedXMLContent = details.get(EnvironmentManagerConstants.Property_XMLContent);
        EnvironmentLoader.loadEnvironmentWithBuffer(getLocalEnvironment(), receivedXMLContent);

        getLocalEnvironment().addEnvironementListener(this);
    }

    private void receivedRequestUnableToGetSyncEnvironment(String senderId, String message, Map<String, String> details) {
        if (getLocalEnvironment() != null) {
            sendRequestGetSyncEnvironment();
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
        }
    }

}
