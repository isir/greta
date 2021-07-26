using System;
using System.Collections.Generic;
using UnityEngine;
using animationparameters;
using audioElements;
using autodeskcharacter;
using thriftImpl;
using time;
using tools;

public class GretaCharacterAnimator : MonoBehaviour
{
    static int NUMBER_OF_FAPS = Enum.GetNames(typeof(FAPType)).Length;
    static int NUMBER_OF_BAPS = 296;

    private List<autodeskcharacter.fapmapper.FapMapper> fapMappers;
    private List<autodeskcharacter.bapmapper.BapMapper> bapMappers;
    private ConcatenateJoints concatenator = new ConcatenateJoints();

    public bool sat = false;
    public float hipsDegrees = -90;
    public float kneesDegrees = 90;

    // from old code
    public bool distantConnection = true;
    public int FAP_RECEIVER_PORT = 9090;
    public int BAP_RECEIVER_PORT = 9900;
    public int AUDIO_RECEIVER_PORT = 9009;
    public int CMD_SENDER_PORT = 9912;
    public string GretaServersHost = "localhost";

    public float UpperBodyAnimationEffect = 0.9f;
    public bool agentPlaying;
    public bool animateAgent = true;
    public string animationID = "noIdea";
    public string animationIDold = "noIdea";

    // Time controler
    public TimeController characterTimer;
    // DistantConnection: Thrift
    public bool thriftConsumerOpened = false;
    // Unity is a server receiving inputs from client

    public AudioFilePlayer audioFilePlayer;
    public FAPReceiver fapReceiver;
    public BAPReceiver bapReceiver;
    public AudioReceiver audioReceiver;
    public CommandSender commandSender;

    private AnimationParametersFrame lastFAPFrame;
    private AnimationParametersFrame lastBAPFrame;
    private int cptFrames;

    // Delay in animation play to absorb network latencies
    private static int ANIM_DELAY = 000; //Add up to 400ms delay when the connection is over a the web or wifi connection

    private AudioSource _currentAudioSource = null;

    public GameObject _characterMesh;

    // Use this for initialization
    void Awake()
    {
        InitCharacterMesh();

        changeTPoseToNPose();
        setUpSkeleton();

        // from old code
        Time.fixedDeltaTime = (float)0.04;
        characterTimer = new TimeController();
        characterTimer.setTimeMillis(0);

        bapReceiver = new BAPReceiver(NUMBER_OF_BAPS, BAP_RECEIVER_PORT);
        fapReceiver = new FAPReceiver(NUMBER_OF_FAPS + 1, FAP_RECEIVER_PORT);
        audioReceiver = new AudioReceiver(AUDIO_RECEIVER_PORT);

        lastFAPFrame = new AnimationParametersFrame(NUMBER_OF_FAPS + 1, 0);
        lastBAPFrame = new AnimationParametersFrame(NUMBER_OF_BAPS, 0);
        cptFrames = 0;
        commandSender = new CommandSender(GretaServersHost, CMD_SENDER_PORT);
        agentPlaying = false;

        _currentAudioSource = getBone("Head").gameObject.AddComponent<AudioSource>();
        audioFilePlayer = new AudioFilePlayer();
    }

    protected void InitCharacterMesh()
    {
        if (_characterMesh == null)
        {
            for (int i = 0; i < transform.childCount; i++)
            {
                Transform t = transform.GetChild(i);
                if (t.gameObject.active)
                {
                    _characterMesh = t.gameObject;
                    break;
                }
            }
        }
        if (_characterMesh == null)
            Debug.LogError("No characterMesh assigned and no active child found");
    }

    public void FixedUpdate()
    {
        if (animateAgent)
        {
            AnimationParametersFrame currentFAPFrame = null;
            AnimationParametersFrame currentBAPFrame = null;

            AudioElement currentAudio = null;

            // Update of frames
            if (distantConnection)
            {
                // uses THRIFT for updating animation

                if (!thriftConsumerOpened)
                {
                    // standard connection
                    if (!fapReceiver.isConnected() && !fapReceiver.isOnConnection())
                    {
                        fapReceiver.startConnection();
                    }
                    else if (!bapReceiver.isConnected() && !bapReceiver.isOnConnection() && fapReceiver.isConnected())
                    {
                        Debug.Log("FAP Receiver started");
                        bapReceiver.startConnection();
                    }
                    else if (!audioReceiver.isConnected() && !audioReceiver.isOnConnection() && bapReceiver.isConnected())
                    {
                        Debug.Log("BAP Receiver started");
                        audioReceiver.startConnection();
                    }
                    else if (!commandSender.isConnected() && !commandSender.isOnConnection() && audioReceiver.isConnected())
                    {
                        Debug.Log("Audio Receiver started");
                        commandSender.startConnection();
                    }
                    else if (commandSender.isConnected())
                    {
                        Debug.Log("Connection Sender started");
                        thriftConsumerOpened = true;
                    }
                }
                else {
                    // FAP animation
                    if (fapReceiver.timer.isSynchronized())
                    {
                        //if (SceneManager.gretaClock <= 0)
                        characterTimer.setTimeMillis(fapReceiver.timer.getTimeMillis() - ANIM_DELAY);// the ANIM_DELAY is to take into account delays on the network
                        SceneManager.gretaClock = (float)characterTimer.getTimeMillis();
                        // Debug.Log(fapReceiver.timer.getTimeMillis()/40 );
                        //currentFAPFrame = fapReceiver.getCurrentFrame (fapReceiver.timer.getTimeMillis () / 40);
                        currentFAPFrame = fapReceiver.getCurrentFrame(characterTimer.getTimeMillis() / 40);
                    }
                    // BAP Animation
                    if (bapReceiver.timer.isSynchronized())
                    {
                        if (SceneManager.gretaClock <= 0)
                        {
                            characterTimer.setTimeMillis(bapReceiver.timer.getTimeMillis() - ANIM_DELAY);// the ANIM_DELAY is to take into account delays on the network
                            SceneManager.gretaClock = (float)(characterTimer.getTimeMillis());
                        }
                        currentBAPFrame = bapReceiver.getCurrentFrame(characterTimer.getTimeMillis() / 40);
                    }
                    // AudioBuffer
                    if (fapReceiver.timer.isSynchronized())
                    { // consumer AUDIO Buffer
                        currentAudio = audioReceiver.getCurrentAudioElement(characterTimer.getTimeMillis() / 40);
                    }
                }
            }

            // Animates agent using local files
            else {
                if (fapReceiver.isConnected())
                {
                    fapReceiver.stopConnector();
                    thriftConsumerOpened = false;
                }
                if (bapReceiver.isConnected())
                {
                    bapReceiver.stopConnector();
                    thriftConsumerOpened = false;
                }
                if (audioReceiver.isConnected())
                {
                    audioReceiver.stopConnector();
                    thriftConsumerOpened = false;
                }
            }

            // Update of animation
            if (currentFAPFrame != null)
            {
                if (lastFAPFrame.isEqualTo(currentFAPFrame))
                {
                    cptFrames++;
                    if (cptFrames > 2)
                    {
                        agentPlaying = false;
                        cptFrames = 0;
                    }
                }
                else {
                    agentPlaying = true;
                    cptFrames = 0;
                    lastFAPFrame = new AnimationParametersFrame(currentFAPFrame);
                }

                applyFapFrame(currentFAPFrame);
            }
            if (currentBAPFrame != null)
            {
                if (lastBAPFrame.isEqualTo(currentBAPFrame))
                {
                    cptFrames++;
                    if (cptFrames > 2)
                    {
                        agentPlaying = false;
                        cptFrames = 0;
                    }
                }
                else {
                    agentPlaying = true;
                    cptFrames = 0;
                    lastBAPFrame = new AnimationParametersFrame(currentBAPFrame);
                }

                applyBapFrame(currentBAPFrame);
            }

            /*EB : START TEST FOR AUDIO BUFFER*/
            if (audioFilePlayer.isNewAudio() || audioReceiver.isNewAudio())
            {
                //EB : I reconstructed the short values computed by cereproc from the byte buffer sent by VIB
                // and used this short value to fill the float buffer needed by the audio clip
                if (currentAudio.getSampleRate()>0 && currentAudio.rawData.Length > 0)
                {
                    int len = currentAudio.rawData.Length / 2;
                    //EB: I couldn't find in Unity how to clean an audio clip nor how to modify its buffer length,
                    // so I prefered to destroy the audio clip (to free the memory) and to create an audio clip
                    // which has the appropriate float buffer size.
                    // In theory the frequency should be provided by the currentAudio object (which should
                    // receive such an information in the message from VIB), but since this is not the case
                    // I hard coded the frequency (47250). It works fine with cereproc, but not with MaryTTS.
                    // For Mary you need to set the frequency to 16000. This is ugly, really!
                    // It should be a input and not hard coded. The problem is that the thrift message doesn't
                    // contain the information at all and I don't want to put my hands in that part of your code.
                    Destroy(_currentAudioSource.clip);

                    _currentAudioSource.clip = AudioClip.Create("text", len, 1, currentAudio.getSampleRate(), false);
                    float[] buffer = new float[len];
                    for (int iPCM = 44; iPCM < len; iPCM++)
                    {
                        float f;
                        short i = (short)((currentAudio.rawData[iPCM * 2 + 1] << 8) | currentAudio.rawData[iPCM * 2]);
                        f = ((float)i) / (float)32768;
                        if (f > 1) f = 1;
                        if (f < -1) f = -1;
                        buffer[iPCM] = f;
                    }
                    _currentAudioSource.clip.SetData(buffer, 0);
                    _currentAudioSource.Play();

                    audioReceiver.setNewAudio(false);
                    audioFilePlayer.setNewAudio(false);
                }
                else {
                    if ((_currentAudioSource != null) && (_currentAudioSource.clip != null))
                    {
                        float offSet = ((float)characterTimer.getTimeMillis() - ((float)currentAudio.getFrameNumber() * 40)) / 1000;
                        int samplesOffset = (int)(_currentAudioSource.clip.frequency * offSet * _currentAudioSource.clip.channels);
                        _currentAudioSource.timeSamples = samplesOffset;
                        _currentAudioSource.Play();
                    }
                    audioReceiver.setNewAudio(false);
                    audioFilePlayer.setNewAudio(false);
                }
            }
        }
        else {
            if (_currentAudioSource != null)
            {
                _currentAudioSource.Stop();
            }
        }
        if (animationIDold != animationID)
        {
            PlayAgentAnimation(animationID);
            animationIDold = animationID;
        }
    }

    public void OnApplicationQuit()
    {
        // close THRIFT consumer if it's used
        if (SceneManager.isThrift)
        {
            if (fapReceiver.isConnected())
            {
                fapReceiver.stopConnector();
            }
            if (bapReceiver.isConnected())
            {
                bapReceiver.stopConnector();
            }
            if (audioReceiver.isConnected())
            {
                audioReceiver.stopConnector();
            }
            if (commandSender.isConnected())
            {
                commandSender.stopConnector();
            }
        }
    }

    public void PlayAgentAnimation(string animationID, InterpersonalAttitude attitude = null)
    {
        animateAgent = true;
        // Send "play" command to distant server
        if (distantConnection)
        {
            if (commandSender.isConnected())
            {
                commandSender.playAnimation(animationID + ".xml", attitude);
            }
            else {
                Debug.LogWarning("AnimationReceiver on host: " + commandSender.getHost() + " and port: " + commandSender.getPort() + " not connected");
            }
        }
    }

    /// <summary>
    /// Notifies GRETA that the given object has changed its position.
    /// The GRETA agent will follow it with its gaze with the given gaze influence.<br/>
    /// If GRETA does not know the object, it will be created in its environment.<br/>
    /// If GRETA knows the object, it will be moved in its environment.<br/>
    /// The object is always represented by a cube in GRETA's environment.
    /// </summary>
    /// <param name="objectToFollow">object to be notified</param>
    /// <param name="gazeInfluence">gaze influence with which to gaze at the object</param>
    public void FollowObjectWithGaze (GameObject objectToFollow,
        GretaObjectTracker.Influence gazeInfluence = GretaObjectTracker.Influence.EYES)
    {
        animateAgent = true;
        // Send "play" command to distant server
        if (distantConnection)
        {
            if (commandSender.isConnected())
            {
                commandSender.SendFollowObjectWithGaze(objectToFollow, gazeInfluence);
            }
            else {
                Debug.LogWarning("AnimationReceiver on host: " + commandSender.getHost() + " and port: " + commandSender.getPort() + " not connected");
            }
        }
    }

    private void setUpSkeleton()
    {
        Quaternion oldRotation = transform.rotation;
        Vector3 oldPosition = transform.position;
        transform.rotation = Quaternion.identity;
        transform.position = Vector3.zero;

        if (sat)
        {
            correctBone("LeftLeg", Quaternion.AngleAxis(kneesDegrees, new Vector3(0, 1, 0)));
            correctBone("RightLeg", Quaternion.AngleAxis(kneesDegrees, new Vector3(0, 1, 0)));
            correctBone("LeftUpLeg", Quaternion.AngleAxis(hipsDegrees, new Vector3(1, 0, 0)));
            correctBone("RightUpLeg", Quaternion.AngleAxis(hipsDegrees, new Vector3(1, 0, 0)));
        }

        // setup the face
        fapMappers = new List<autodeskcharacter.fapmapper.FapMapper>();
        setupFapMapper();

        //setup the body
        bapMappers = new List<autodeskcharacter.bapmapper.BapMapper>();
        setupBapMapper();

        transform.position = oldPosition;
        transform.rotation = oldRotation;
    }

    private void changeTPoseToNPose()
    {
        float clavC = 9;
        Quaternion rclavC = Quaternion.AngleAxis(clavC, new Vector3(0, 0, 1));
        Quaternion lclavC = Quaternion.AngleAxis(clavC, new Vector3(0, 0, -1));

        Quaternion shoulderC = Quaternion.AngleAxis(90 - clavC, new Vector3(0, 0, 1));

        Quaternion rThumbC =
        Quaternion.AngleAxis(-33, new Vector3(0, 1, 0)) *
        Quaternion.AngleAxis(-20, new Vector3(0, 0, 1)) *
        Quaternion.AngleAxis(15, new Vector3(1, 0, 0));

        correctBone("RightShoulder", rclavC);
        correctBone("LeftShoulder", lclavC);
        correctBone("RightArm", shoulderC);
        correctBone("LeftArm", shoulderC);
        correctBone("RightHandThumb1", rThumbC);
        correctBone("LeftHandThumb1", rThumbC);
    }

    private void setupFapMapper()
    {
        float ens = 0;
        float es = 0;
        float mw = 0;
        float mns = 0;
        if (hasBone("LeftEye") && hasBone("RightEye"))
        {
            es = Vector3.Distance(getBone("LeftEye").localPosition, getBone("RightEye").localPosition);
        }
        if (hasBone("LeftEye") && hasBone("RightEye") && hasBone("Nostrils"))
        {
            ens = Vector3.Distance(
            (getBone("LeftEye").localPosition + getBone("RightEye").localPosition) * 0.5f,
            getBone("Nostrils").localPosition
            );
        }
        if (hasBone("LipCornerL") && hasBone("LipCornerR"))
        {
            mw = Vector3.Distance(
            getBone("LipCornerL").localPosition,
            getBone("LipCornerR").localPosition);
        }
        if (hasBone("LipCornerL") && hasBone("LipCornerR") && hasBone("Nostrils"))
        {
            mns = Vector3.Distance(
            (getBone("LipCornerL").localPosition + getBone("LipCornerR").localPosition) * 0.5f,
            getBone("Nostrils").localPosition
            );
        }

        if (hasBone("UpperLidL") && hasBone("LowerLidL"))
        {
            Transform upper = getBone("UpperLidL");
            Transform lower = getBone("LowerLidL");
            float dist = Vector3.Distance(lower.localPosition, upper.localPosition);
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(upper, FAPType.close_t_l_eyelid, new Vector3(dist / 1024f, 0, 0)));
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(lower, FAPType.close_b_l_eyelid, new Vector3(-dist / 1024f, 0, 0)));
        }
        if (hasBone("UpperLidR") && hasBone("LowerLidR"))
        {
            Transform upper = getBone("UpperLidR");
            Transform lower = getBone("LowerLidR");
            float dist = Vector3.Distance(lower.localPosition, upper.localPosition);
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(upper, FAPType.close_t_r_eyelid, new Vector3(dist / 1024f, 0, 0)));
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(lower, FAPType.close_b_r_eyelid, new Vector3(-dist / 1024f, 0, 0)));
        }
        if (hasBone("LeftEye"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Eye(getBone("LeftEye"), FAPType.pitch_l_eyeball, new Vector3(0, -1, 0), FAPType.yaw_l_eyeball, new Vector3(1, 0, 0)));
        }
        if (hasBone("RightEye"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Eye(getBone("RightEye"), FAPType.pitch_r_eyeball, new Vector3(0, -1, 0), FAPType.yaw_r_eyeball, new Vector3(1, 0, 0)));
        }
        if (hasBone("BrowInnerL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.TwoDOF(getBone("BrowInnerL"), FAPType.raise_l_i_eyebrow, new Vector3(-ens / 1024, 0, 0), FAPType.squeeze_l_eyebrow, new Vector3(0, -es / 1024, -es / 2048)));
        }
        if (hasBone("BrowOuterL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(getBone("BrowOuterL"), FAPType.raise_l_o_eyebrow, new Vector3(-ens / 1024, 0, es / 4096)));
        }
        if (hasBone("BrowInnerR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.TwoDOF(getBone("BrowInnerR"), FAPType.raise_r_i_eyebrow, new Vector3(-ens / 1024, 0, 0), FAPType.squeeze_r_eyebrow, new Vector3(0, es / 1024, -es / 2048)));
        }
        if (hasBone("BrowOuterR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(getBone("BrowOuterR"), FAPType.raise_r_o_eyebrow, new Vector3(-ens / 1024, 0, es / 4096)));
        }
        if (hasBone("CheekL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(getBone("CheekL"), FAPType.lift_l_cheek, new Vector3(-ens / 1024, 0, 0)));
        }
        if (hasBone("CheekR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.OneDOF(getBone("CheekR"), FAPType.lift_r_cheek, new Vector3(-ens / 1024, 0, 0)));
        }

        if (hasBone("LipCornerL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Lip(getBone("LipCornerL"),
            FAPType.stretch_l_cornerlip_o, new Vector3(0, mw / 1024, 0),
            FAPType.raise_l_cornerlip_o, new Vector3(-mns / 1024, 0, 0),
            FAPType.stretch_l_cornerlip, new Vector3(1, 0, 0)));
        }
        if (hasBone("LipCornerR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Lip(getBone("LipCornerR"),
            FAPType.stretch_r_cornerlip_o, new Vector3(0, -mw / 1024, 0),
            FAPType.raise_r_cornerlip_o, new Vector3(-mns / 1024, 0, 0),
            FAPType.stretch_r_cornerlip, new Vector3(-1, 0, 0)));
        }

        if (hasBone("LipLowerL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.MidLip(getBone("LipLowerL"),
            FAPType.raise_b_lip_lm_o, new Vector3(-mns / 1024, 0, 0),
            FAPType.push_b_lip, new Vector3(0, 0, -mns / 1024),
            FAPType.raise_b_lip_lm, new Vector3(0, 1, 0),
            FAPType.stretch_l_cornerlip_o, new Vector3(0, mw / 4096, 0)));
        }
        if (hasBone("LipLowerR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.MidLip(getBone("LipLowerR"),
            FAPType.raise_b_lip_rm_o, new Vector3(-mns / 1024, 0, 0),
            FAPType.push_b_lip, new Vector3(0, 0, -mns / 1024),
            FAPType.raise_b_lip_rm, new Vector3(0, 1, 0),
            FAPType.stretch_r_cornerlip_o, new Vector3(0, -mw / 4096, 0)));
        }

        if (hasBone("LipUpperL"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.MidLip(getBone("LipUpperL"),
            FAPType.lower_t_lip_lm_o, new Vector3(mns / 1024, 0, 0),
            FAPType.push_t_lip, new Vector3(0, 0, -mns / 1024),
            FAPType.lower_t_lip_lm, new Vector3(0, -1, 0),
            FAPType.stretch_l_cornerlip_o, new Vector3(0, mw / 4096, 0)));
        }
        if (hasBone("LipUpperR"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.MidLip(getBone("LipUpperR"),
            FAPType.lower_t_lip_rm_o, new Vector3(mns / 1024, 0, 0),
            FAPType.push_t_lip, new Vector3(0, 0, -mns / 1024),
            FAPType.lower_t_lip_rm, new Vector3(0, -1, 0),
            FAPType.stretch_r_cornerlip_o, new Vector3(0, -mw / 4096, 0)));
        }

        if (hasBone("Jaw"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Jaw(getBone("Jaw"),
            FAPType.open_jaw, new Vector3(0, -1, 0), 0.0008f,
            FAPType.shift_jaw, new Vector3(-1, 0, 0), 0.0008f,
            FAPType.thrust_jaw, new Vector3(0, 0, -mns / 1024)));
        }
        if (hasBone("Nostrils"))
        {
            fapMappers.Add(new autodeskcharacter.fapmapper.Nostril(getBone("Nostrils"), FAPType.stretch_l_nose, FAPType.stretch_r_nose, new Vector3(ens / 256, ens / 32, ens / 512)));
        }
    }

    private void setupBapMapper()
    {
        List<JointType> typesUsed = new List<JointType>();

        //spine
        map(typesUsed, "Hips", JointType.HumanoidRoot);
        map(typesUsed, "Head", JointType.skullbase);
        map(typesUsed, "Neck1", JointType.vc5);
        map(typesUsed, "Neck", JointType.vc7);
        map(typesUsed, "Spine4", JointType.vt6);
        map(typesUsed, "Spine3", JointType.vt12);
        map(typesUsed, "Spine2", JointType.vl1);
        map(typesUsed, "Spine1", JointType.vl3);
        map(typesUsed, "Spine", JointType.vl5);
        map(typesUsed, "Spine2V", JointType.vl1, -0.5);
        map(typesUsed, "Spine1V", JointType.vl3, -0.5);
        map(typesUsed, "SpineV", JointType.vl5, -0.5);

        //legs
        map(typesUsed, "LeftUpLeg", "LeftUpLegRoll", JointType.l_hip, 0.5, false);
        map(typesUsed, "LeftLeg", JointType.l_knee);
        map(typesUsed, "LeftFoot", "LeftLegRoll", JointType.l_ankle, 0.5, true);
        map(typesUsed, "LeftToeBase", JointType.l_midtarsal);

        map(typesUsed, "RightUpLeg", "RightUpLegRoll", JointType.r_hip, 0.5, false);
        map(typesUsed, "RightLeg", JointType.r_knee);
        map(typesUsed, "RightFoot", "RightLegRoll", JointType.r_ankle, 0.5, true);
        map(typesUsed, "RightToeBase", JointType.r_midtarsal);

        // rigth arm
        map(typesUsed, "RightShoulder", JointType.r_sternoclavicular);
        mapShoulder(typesUsed, "RightArm", "RightArmRoll", JointType.r_shoulder, JointType.r_acromioclavicular, 0.5);
        map(typesUsed, "RightForeArm", JointType.r_elbow);
        map(typesUsed, "RightHand", "RightForeArmRoll", JointType.r_wrist, 0.75, true);

        map(typesUsed, "RightHandThumb1", JointType.r_thumb1);
        map(typesUsed, "RightHandThumb2", JointType.r_thumb2);
        map(typesUsed, "RightHandThumb3", JointType.r_thumb3);

        map(typesUsed, "RightHandIndex0", JointType.r_index0);
        map(typesUsed, "RightHandIndex1", JointType.r_index1);
        map(typesUsed, "RightHandIndex2", JointType.r_index2);
        map(typesUsed, "RightHandIndex3", JointType.r_index3);

        map(typesUsed, "RightHandMiddle1", JointType.r_middle1);
        map(typesUsed, "RightHandMiddle2", JointType.r_middle2);
        map(typesUsed, "RightHandMiddle3", JointType.r_middle3);

        map(typesUsed, "RightHandRing1", JointType.r_ring1);
        map(typesUsed, "RightHandRing2", JointType.r_ring2);
        map(typesUsed, "RightHandRing3", JointType.r_ring3);

        map(typesUsed, "RightHandPinky0", JointType.r_pinky0);
        map(typesUsed, "RightHandPinky1", JointType.r_pinky1);
        map(typesUsed, "RightHandPinky2", JointType.r_pinky2);
        map(typesUsed, "RightHandPinky3", JointType.r_pinky3);

        //left arm
        map(typesUsed, "LeftShoulder", JointType.l_sternoclavicular);
        mapShoulder(typesUsed, "LeftArm", "LeftArmRoll", JointType.l_shoulder, JointType.l_acromioclavicular, 0.5);

        map(typesUsed, "LeftForeArm", JointType.l_elbow);
        map(typesUsed, "LeftHand", "LeftForeArmRoll", JointType.l_wrist, 0.75, true);

        map(typesUsed, "LeftHandThumb1", JointType.l_thumb1);
        map(typesUsed, "LeftHandThumb2", JointType.l_thumb2);
        map(typesUsed, "LeftHandThumb3", JointType.l_thumb3);

        map(typesUsed, "LeftHandIndex0", JointType.l_index0);
        map(typesUsed, "LeftHandIndex1", JointType.l_index1);
        map(typesUsed, "LeftHandIndex2", JointType.l_index2);
        map(typesUsed, "LeftHandIndex3", JointType.l_index3);

        map(typesUsed, "LeftHandMiddle1", JointType.l_middle1);
        map(typesUsed, "LeftHandMiddle2", JointType.l_middle2);
        map(typesUsed, "LeftHandMiddle3", JointType.l_middle3);

        map(typesUsed, "LeftHandRing1", JointType.l_ring1);
        map(typesUsed, "LeftHandRing2", JointType.l_ring2);
        map(typesUsed, "LeftHandRing3", JointType.l_ring3);

        map(typesUsed, "LeftHandPinky0", JointType.l_pinky0);
        map(typesUsed, "LeftHandPinky1", JointType.l_pinky1);
        map(typesUsed, "LeftHandPinky2", JointType.l_pinky2);
        map(typesUsed, "LeftHandPinky3", JointType.l_pinky3);

        concatenator.setJointToUse(typesUsed);
    }
    private void map(List<JointType> typesUsed, String boneName, JointType joint)
    {
        map(typesUsed, boneName, null, joint, 0, true, 1);
    }

    private void map(List<JointType> typesUsed, String boneName, JointType joint, double scale)
    {
        map(typesUsed, boneName, null, joint, 0, true, scale);
    }

    private void map(List<JointType> typesUsed, String boneName, String twistBoneName, JointType joint, double twistFactor, bool before)
    {
        map(typesUsed, boneName, twistBoneName, joint, twistFactor, before, 1);
    }

    private void map(List<JointType> typesUsed, String boneName, String twistBoneName, JointType joint, double twistFactor, bool before, double scale)
    {
        if (hasBone(boneName))
        {
            List<Vector3> dofs = new List<Vector3>(3);
            List<BAPType> types = new List<BAPType>(3);
            if (joint.rotationX != BAPType.null_bap)
            {
                dofs.Add(new Vector3(1, 0, 0));
                types.Add(joint.rotationX);
            }
            if (joint.rotationY != BAPType.null_bap)
            {
                dofs.Add(new Vector3(0, -1, 0));
                types.Add(joint.rotationY);
            }
            if (joint.rotationZ != BAPType.null_bap)
            {
                dofs.Add(new Vector3(0, 0, -1));
                types.Add(joint.rotationZ);
            }

            if (dofs.Count == 0)
            {
                return;
            }
            if (dofs.Count == 1)
            {
                bapMappers.Add(new autodeskcharacter.bapmapper.OneDOF(getBone(boneName), types[0], dofs[0]));
            }
            if (dofs.Count == 2)
            {
                bapMappers.Add(new autodeskcharacter.bapmapper.TwoDOF(getBone(boneName), types[0], dofs[0], types[1], dofs[1]));
            }
            if (dofs.Count == 3)
            {
                if (twistBoneName != null && hasBone(twistBoneName))
                {
                    if (before)
                    {
                        bapMappers.Add(new autodeskcharacter.bapmapper.YawTwistBeforeMapper(getBone(boneName), getBone(twistBoneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2], twistFactor));
                    }
                    else {
                        bapMappers.Add(new autodeskcharacter.bapmapper.YawTwistAfterMapper(getBone(boneName), getBone(twistBoneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2], twistFactor));
                    }
                }
                else {
                    if (scale == 1)
                    {
                        bapMappers.Add(new autodeskcharacter.bapmapper.ThreeDOF(getBone(boneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2]));
                    }
                    else {
                        autodeskcharacter.bapmapper.ThreeDOFScaled bm = new autodeskcharacter.bapmapper.ThreeDOFScaled(getBone(boneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2]);
                        bm.setScale(scale);
                        bapMappers.Add(bm);
                    }
                }
            }
            typesUsed.Add(joint);
        }
    }

    private void mapShoulder(List<JointType> typesUsed, String boneName, String twistBoneName, JointType shoulderJoint, JointType acromiumJoint, double twistFactor)
    {
        if (hasBone(boneName))
        {
            List<Vector3> dofs = new List<Vector3>(3);
            List<BAPType> types = new List<BAPType>(3);
            dofs.Add(new Vector3(1, 0, 0));
            types.Add(shoulderJoint.rotationX);
            dofs.Add(new Vector3(0, -1, 0));
            types.Add(shoulderJoint.rotationY);
            dofs.Add(new Vector3(0, 0, -1));
            types.Add(shoulderJoint.rotationZ);
            if (hasBone(twistBoneName))
            {
                bapMappers.Add(new autodeskcharacter.bapmapper.YawTwistAfterMapper(getBone(boneName), getBone(twistBoneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2], twistFactor));
            }
            else {
                bapMappers.Add(new autodeskcharacter.bapmapper.ThreeDOF(getBone(boneName), types[0], dofs[0], types[1], dofs[1], types[2], dofs[2]));
            }
            typesUsed.Add(shoulderJoint);
        }
    }

    private void correctBone(string boneName, Quaternion correction)
    {
        Transform bone = findBone(boneName);
        if (bone != null)
        {
            bone.localRotation = correction * bone.localRotation;
        }
    }

    public Transform findBone(string name)
    {
        Transform[] allTransforms = _characterMesh.GetComponentsInChildren<Transform>(true);
        foreach (Transform t in allTransforms)
        {
            if (t.name == name)
            {
                return t;
            }
        }
        return null;
    }

    public Transform getBone(string name)
    {
        return findBone(name);
    }

    public bool hasBone(string name)
    {
        return findBone(name) != null;
    }

    public void applyFapFrame(AnimationParametersFrame fapframe)
    {
        foreach (autodeskcharacter.fapmapper.FapMapper mapper in fapMappers)
        {
            mapper.applyFap(fapframe);
        }
    }

    public void applyBapFrame(AnimationParametersFrame bapframe)
    {
        bapframe = concatenator.concatenateJoints(bapframe);
        foreach (autodeskcharacter.bapmapper.BapMapper mapper in bapMappers)
        {
            mapper.applyBap(bapframe);
        }
    }
}
