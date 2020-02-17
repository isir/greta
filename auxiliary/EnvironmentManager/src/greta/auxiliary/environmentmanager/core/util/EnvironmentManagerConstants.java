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
package greta.auxiliary.environmentmanager.core.util;

/**
 *
 * @author Brice Donval
 */
public class EnvironmentManagerConstants {

    /* ---------------------------------------------------------------------- */
    /*                                Network                                 */
    /* ---------------------------------------------------------------------- */

    public static final String Network_StartingPort = "50000";
    public static final String Network_EndingPort   = "50999";

    /* ---------------------------------------------------------------------- */
    /*                                Requests                                */
    /* ---------------------------------------------------------------------- */

    public static final String Request_RegisterReplica = "EnvironmentManager_RegisterReplica";
    public static final String Request_RegisterPrimary = "EnvironmentManager_RegisterPrimary";

    public static final String Request_UnregisterReplica = "EnvironmentManager_UnregisterReplica";
    public static final String Request_UnregisterPrimary = "EnvironmentManager_UnregisterPrimary";

    public static final String Request_GetSyncEnvironment         = "EnvironmentManager_GetSyncEnvironment";
    public static final String Request_UnableToGetSyncEnvironment = "EnvironmentManager_UnableToGetSyncEnvironment";

    public static final String Request_WelcomeEnvironment = "EnvironmentManager_WelcomeEnvironment";

    public static final String Request_WelcomeMPEG4Animatable = "EnvironmentManager_WelcomeMPEG4Animatable";
    public static final String Request_GoodbyeMPEG4Animatable = "EnvironmentManager_GoodbyeMPEG4Animatable";

    public static final String Request_ChangeTree = "EnvironmentManager_ChangeTree";
    public static final String Request_ChangeNode = "EnvironmentManager_ChangeNode";
    public static final String Request_ChangeLeaf = "EnvironmentManager_ChangeLeaf";

    /* ---------------------------------------------------------------------- */
    /*                               Properties                               */
    /* ---------------------------------------------------------------------- */

    public static final String Property_MessageSenderId    = "EnvironmentManager_MessageSenderId";
    public static final String Property_MessageRecipientId = "EnvironmentManager_MessageRecipientId";

    public static final String Property_Time = "EnvironmentManager_Time";

    public static final String Property_XMLContent = "EnvironmentManager_XMLContent";

    public static final String Property_MPEG4AnimatableId = "EnvironmentManager_MPEG4AnimatableId";
    public static final String Property_FAPFrame          = "EnvironmentManager_FAPFrame";
    public static final String Property_BAPFrame          = "EnvironmentManager_BAPFrame";

    public static final String Property_TreeModifType   = "EnvironmentManager_TreeModifType";
    public static final String Property_ChildNodeId     = "EnvironmentManager_ChildNodeId";
    public static final String Property_OldParentNodeId = "EnvironmentManager_OldParentNodeId";
    public static final String Property_NewParentNodeId = "EnvironmentManager_NewParentNodeId";

    public static final String Property_NodeModifType    = "EnvironmentManager_NodeModifType";
    public static final String Property_NodeId           = "EnvironmentManager_NodeId";
    public static final String Property_NodePositionX    = "EnvironmentManager_NodePositionX";
    public static final String Property_NodePositionY    = "EnvironmentManager_NodePositionY";
    public static final String Property_NodePositionZ    = "EnvironmentManager_NodePositionZ";
    public static final String Property_NodeOrientationX = "EnvironmentManager_NodeOrientationX";
    public static final String Property_NodeOrientationY = "EnvironmentManager_NodeOrientationY";
    public static final String Property_NodeOrientationZ = "EnvironmentManager_NodeOrientationZ";
    public static final String Property_NodeOrientationW = "EnvironmentManager_NodeOrientationW";
    public static final String Property_NodeScaleX       = "EnvironmentManager_NodeScaleX";
    public static final String Property_NodeScaleY       = "EnvironmentManager_NodeScaleY";
    public static final String Property_NodeScaleZ       = "EnvironmentManager_NodeScaleZ";

    public static final String Property_LeafModifType = "EnvironmentManager_LeafModifType";
    public static final String Property_LeafId        = "EnvironmentManager_LeafLeafId";
    public static final String Property_LeafReference = "EnvironmentManager_LeafReference";
    public static final String Property_LeafSizeX     = "EnvironmentManager_LeafSizeX";
    public static final String Property_LeafSizeY     = "EnvironmentManager_LeafSizeY";
    public static final String Property_LeafSizeZ     = "EnvironmentManager_LeafSizeZ";

    /* ---------------------------------------------------------------------- */
    /*                                 Values                                 */
    /* ---------------------------------------------------------------------- */

    public static final String Value_MessageForPrimary     = "EnvironmentManager_MessageForPrimary";
    public static final String Value_MessageForAllReplicas = "EnvironmentManager_MessageForAllReplicas";

    public static final String Value_RootNode = "EnvironmentManager_RootNode";

}
