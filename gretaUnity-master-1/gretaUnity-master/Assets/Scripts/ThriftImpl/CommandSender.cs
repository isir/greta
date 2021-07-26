using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using thrift.gen_csharp;
using thrift.services;

namespace thriftImpl
{
    public class CommandSender : Sender
    {
        private int _cpt; // automatically initialized to 0

        public CommandSender() : this(DEFAULT_THRIFT_HOST, DEFAULT_THRIFT_PORT)
        {
        }

        public CommandSender(string host, int port) : base(host, port)
        {
        }

        public void playAnimation(string animationID, InterpersonalAttitude attitude = null)
        {
            if (isConnected())
            {
                Message message = new Message
                {
                    Type = "animID",
                    Time = 0,
                    Id = _cpt.ToString(),
                    String_content = animationID
                };

                // Add property about social attitude model
                if (attitude != null)
                {
                    message.Properties = new Dictionary<string, string>
                    {
                        {"Dominance", attitude.Dominance.ToString()},
                        {"Liking", attitude.Liking.ToString()}
                    };
                }

                _cpt++;
                ThreadPool.QueueUserWorkItem((stateInfo) => { send(message); });
            }
            else {
                Debug.Log("animationReceiver on host: " + getHost() + " and port: " + getPort() + " not connected");
            }
        }

        /// <summary>
        /// Notifies GRETA that the given character has changed its position.<br/>
        /// </summary>
        /// <param name="gameCharacter">character to be notified</param>
        /// <param name="gameCharacterHead">character's head to be notified</param>
        /// <param name="gameCharacterLeftEye">character's left eye to be notified</param>
        /// <param name="gameCharacterRightEye">character's right eye to be notified</param>
        /// <param name="gameCharacterMouth">character's mouth to be notified</param>
        /// <param name="gameCharacterLeftHand">character's left hand to be notified</param>
        /// <param name="gameCharacterRightHand">character's right hand to be notified</param>
        /// <param name="gameCharacterLeftFoot">character's left foot to be notified</param>
        /// <param name="gameCharacterRightFoot">character's right foot to be notified</param>
        public void NotifyCharacter(
            GameObject gameCharacter,
            GameObject gameCharacterHead = null,
            GameObject gameCharacterLeftEye = null, GameObject gameCharacterRightEye = null,
            GameObject gameCharacterMouth = null,
            GameObject gameCharacterLeftHand = null, GameObject gameCharacterRightHand = null,
            GameObject gameCharacterLeftFoot = null, GameObject gameCharacterRightFoot = null)
        {
            SendCharacterMessage(
                gameCharacter,
                gameCharacterHead,
                gameCharacterLeftEye, gameCharacterRightEye,
                gameCharacterMouth,
                gameCharacterLeftHand, gameCharacterRightHand,
                gameCharacterLeftFoot, gameCharacterRightFoot);
        }

        /// <summary>
        /// Notifies GRETA that the given object has changed its position.<br/>
        /// If GRETA does not know the object, it will be created in its environment.<br/>
        /// If GRETA knows the object, it will be moved in its environment.<br/>
        /// The object is always represented by a cube in GRETA's environment.
        /// </summary>
        /// <param name="gameObject">object to be notified</param>
        public void NotifyObject(GameObject gameObject)
        {
            SendObjectMessage(gameObject);
        }

        /// <summary>
        /// Notifies GRETA that the given object has changed its position.
        /// The GRETA agent will follow it with its gaze with the given gaze influence.<br/>
        /// If GRETA does not know the object, it will be created in its environment.<br/>
        /// If GRETA knows the object, it will be moved in its environment.<br/>
        /// The object is always represented by a cube in GRETA's environment.
        /// </summary>
        /// <param name="gameObject">object to be notified</param>
        /// <param name="gazeInfluence">gaze influence with which to gaze at the object</param>
        public void SendFollowObjectWithGaze(GameObject gameObject,
            GretaObjectTracker.Influence gazeInfluence = GretaObjectTracker.Influence.EYES)
        {
            SendObjectMessage(gameObject, true, gazeInfluence);
        }

        /// <summary>
        /// Notifies GRETA that the given character has changed its position.
        /// </summary>
        /// <param name="gameCharacter">character to be notified</param>
        /// <param name="gameCharacterHead">character's head to be notified</param>
        /// <param name="gameCharacterLeftEye">character's left eye to be notified</param>
        /// <param name="gameCharacterRightEye">character's right eye to be notified</param>
        /// <param name="gameCharacterMouth">character's mouth to be notified</param>
        /// <param name="gameCharacterLeftHand">character's left hand to be notified</param>
        /// <param name="gameCharacterRightHand">character's right hand to be notified</param>
        /// <param name="gameCharacterLeftFoot">character's left foot to be notified</param>
        /// <param name="gameCharacterRightFoot">character's right foot to be notified</param>
        private void SendCharacterMessage(
            GameObject gameCharacter,
            GameObject gameCharacterHead = null,
            GameObject gameCharacterLeftEye = null, GameObject gameCharacterRightEye = null,
            GameObject gameCharacterMouth = null,
            GameObject gameCharacterLeftHand = null, GameObject gameCharacterRightHand = null,
            GameObject gameCharacterLeftFoot = null, GameObject gameCharacterRightFoot = null)
        {
            if (isConnected())
            {
                Vector3 position = gameCharacter.transform.position;
                Quaternion orientation = gameCharacter.transform.rotation;
                Vector3 scale = gameCharacter.transform.localScale;

                Message message = new Message
                {
                    Type = "character",
                    Time = 0,
                    Id = _cpt.ToString(),
                    // Some coordinates have to be flipped because GRETA doesn't handle coordinates the same way as Unity
                    // The X axis for position is reversed in GRETA, as well as the Y and Z axis for rotation.
                    Properties = new Dictionary<string, string>
                    {
                        {"id", gameCharacter.name},
                        {"position.x", (-(position.x)).ToString()},
                        {"position.y", (position.y).ToString()},
                        {"position.z", (position.z).ToString()},
                        {"orientation.x", orientation.x.ToString()},
                        {"orientation.y", (-orientation.y).ToString()},
                        {"orientation.z", (-orientation.z).ToString()},
                        {"orientation.w", orientation.w.ToString()},
                        {"scale.x", scale.x.ToString()},
                        {"scale.y", scale.y.ToString()},
                        {"scale.z", scale.z.ToString()}
                    }
                };

                _cpt++;
                ThreadPool.QueueUserWorkItem((stateInfo) => { send(message); });

                if (gameCharacterHead != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_head", gameCharacterHead);

                if (gameCharacterLeftEye != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_left_eye", gameCharacterLeftEye);

                if (gameCharacterRightEye != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_right_eye", gameCharacterRightEye);

                if (gameCharacterMouth != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_mouth", gameCharacterMouth);

                if (gameCharacterLeftHand != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_left_hand", gameCharacterLeftHand);

                if (gameCharacterRightHand != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_right_hand", gameCharacterRightHand);

                if (gameCharacterLeftFoot != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_left_foot", gameCharacterLeftFoot);

                if (gameCharacterRightFoot != null)
                    SendCharacterMemberMessage(gameCharacter.name, "character_right_foot", gameCharacterRightFoot);
            }
            else
            {
                Debug.Log("commandReceiver on host: " + getHost() + " and port: " + getPort() + " not connected");
            }
        }

        /// <summary>
        /// Notifies GRETA that the given character's member has changed its position.
        /// </summary>
        /// <param name="gameCharacterName">character's name to be notified</param>
        /// <param name="characterMember">character's member name to be notified</param>
        /// <param name="gameCharacterMember">character's member to be notified</param>
        private void SendCharacterMemberMessage(string gameCharacterName, string characterMember, GameObject gameCharacterMember)
        {
            if (isConnected())
            {
                Vector3 position = gameCharacterMember.transform.position;
                Quaternion orientation = gameCharacterMember.transform.rotation;
                Vector3 scale = gameCharacterMember.transform.localScale;
                Vector3 shift = orientation * new Vector3(0.5f * scale.x, -0.5f * scale.y, -0.5f * scale.z);

                Message message = new Message
                {
                    Type = characterMember,
                    Time = 0,
                    Id = _cpt.ToString(),
                    // Some coordinates have to be flipped because GRETA doesn't handle coordinates the same way as Unity
                    // The X axis for position is reversed in GRETA, as well as the Y and Z axis for rotation.
                    // Coordinates also have to be changed because objects in GRETA have their pivot at their bottom,
                    //     while objects in Unity have their pivot in their center
                    Properties = new Dictionary<string, string>
                    {
                        {"id", gameCharacterName + "_" + gameCharacterMember.name},
                        {"position.x", (-(position.x + shift.x)).ToString()},
                        {"position.y", (position.y + shift.y).ToString()},
                        {"position.z", (position.z + shift.z).ToString()},
                        {"orientation.x", orientation.x.ToString()},
                        {"orientation.y", (-orientation.y).ToString()},
                        {"orientation.z", (-orientation.z).ToString()},
                        {"orientation.w", orientation.w.ToString()},
                        {"scale.x", scale.x.ToString()},
                        {"scale.y", scale.y.ToString()},
                        {"scale.z", scale.z.ToString()}
                    }
                };

                _cpt++;
                ThreadPool.QueueUserWorkItem((stateInfo) => { send(message); });
            }
            else
            {
                Debug.Log("commandReceiver on host: " + getHost() + " and port: " + getPort() + " not connected");
            }
        }

        /// <summary>
        /// Notifies GRETA that the given object has changed its position.
        /// The GRETA agent will follow it with its gaze with the given gaze influence if gaze is set to true.<br/>
        /// If GRETA does not know the object, it will be created in its environment.<br/>
        /// If GRETA knows the object, it will be moved in its environment.<br/>
        /// The object is always represented by a cube in GRETA's environment.
        /// </summary>
        /// <param name="gameObject">object to be notified</param>
        /// <param name="gaze">whether or not to gaze at the object</param>
        /// <param name="gazeInfluence">gaze influence with which to gaze at the object</param>
        private void SendObjectMessage(GameObject gameObject, bool gaze = false, GretaObjectTracker.Influence gazeInfluence = GretaObjectTracker.Influence.EYES)
        {
            if (isConnected())
            {
                Vector3 position = gameObject.transform.position;
                Quaternion orientation = gameObject.transform.rotation;
                Vector3 scale = gameObject.transform.localScale;
                Vector3 shift = orientation * new Vector3(0.5f * scale.x, -0.5f * scale.y, -0.5f * scale.z);

                Message message = new Message
                {
                    Type = "object",
                    Time = 0,
                    Id = _cpt.ToString(),
                    // Some coordinates have to be flipped because GRETA doesn't handle coordinates the same way as Unity
                    // The X axis for position is reversed in GRETA, as well as the Y and Z axis for rotation.
                    // Coordinates also have to be changed because objects in GRETA have their pivot at their bottom,
                    //     while objects in Unity have their pivot in their center
                    Properties = new Dictionary<string, string>
                    {
                        {"id", gameObject.name},
                        {"position.x", (-(position.x + shift.x)).ToString()},
                        {"position.y", (position.y + shift.y).ToString()},
                        {"position.z", (position.z + shift.z).ToString()},
                        {"orientation.x", orientation.x.ToString()},
                        {"orientation.y", (-orientation.y).ToString()},
                        {"orientation.z", (-orientation.z).ToString()},
                        {"orientation.w", orientation.w.ToString()},
                        {"scale.x", scale.x.ToString()},
                        {"scale.y", scale.y.ToString()},
                        {"scale.z", scale.z.ToString()}
                    }
                };

                GretaObjectMetadata metadata = gameObject.GetComponent<GretaObjectMetadata>();
                if (metadata != null)
                {
                    GameObject objectToGazeAt = metadata.objectToGazeAt;
                    if (objectToGazeAt != null)
                        message.Properties.Add("metadata.objectToGazeAt", objectToGazeAt.name);
                }

                if (gaze)
                {
                    message.Properties.Add("gaze", "true");
                    message.Properties.Add("influence", gazeInfluence.ToString());
                }

                _cpt++;
                ThreadPool.QueueUserWorkItem((stateInfo) => { send(message); });
            }
            else
            {
                Debug.Log("commandReceiver on host: " + getHost() + " and port: " + getPort() + " not connected");
            }
        }
    }
}
