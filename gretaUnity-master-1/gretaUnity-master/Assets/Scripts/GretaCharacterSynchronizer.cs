using UnityEngine;
using thriftImpl;

/// <summary>
/// Behaviour script to synchronize a character in GRETA.
/// </summary>
[RequireComponent(typeof(GretaCharacterAnimator))]
public class GretaCharacterSynchronizer : MonoBehaviour
{
    /// <summary>The Thrift command sender linked to our GRETA instance.</summary>
    private CommandSender _commandSender;

    /// <summary>The animation script linked to the GRETA agent we want to add behaviours to.</summary>
    protected GretaCharacterAnimator CharacterAnimScript;

    /// <summary>The character which position, orientation and scale have to be synchronized and reproduced in the GRETA environment.</summary>
	protected GameObject character;

    /// <summary>The character's head.</summary>
	private GameObject characterHead;
    /// <summary>The character's left eye.</summary>
	private GameObject characterLeftEye;
    /// <summary>The character's right eye.</summary>
	private GameObject characterRightEye;
    /// <summary>The character's mouth.</summary>
	private GameObject characterMouth;
    /// <summary>The character's left hand.</summary>
	private GameObject characterLeftHand;
    /// <summary>The character's right hand.</summary>
	private GameObject characterRightHand;
    /// <summary>The character's left foot.</summary>
	private GameObject characterLeftFoot;
    /// <summary>The character's right foot.</summary>
	private GameObject characterRightFoot;

    /// <summary>
    /// Indicates whether we've done the initialization of the character synchronized in GRETA or not yet.<br/>
    /// This way, we give the character's initial position once, and then just synchronize it when it change.
    /// </summary>
    private bool _instantiated;

    void Start()
    {
        CharacterAnimScript = GetComponent<GretaCharacterAnimator>();
        character = CharacterAnimScript._characterMesh;
         _commandSender = CharacterAnimScript.commandSender;

        character.transform.hasChanged = false;

        characterHead = CharacterAnimScript.getBone("Head").gameObject;
        characterLeftEye = CharacterAnimScript.getBone("LeftEye").gameObject;
        characterRightEye = CharacterAnimScript.getBone("RightEye").gameObject;
        characterMouth = CharacterAnimScript.getBone("TongueF").gameObject;
        characterLeftHand = CharacterAnimScript.getBone("LeftHand").gameObject;
        characterRightHand = CharacterAnimScript.getBone("RightHand").gameObject;
        characterLeftFoot = CharacterAnimScript.getBone("LeftFoot").gameObject;
        characterRightFoot = CharacterAnimScript.getBone("RightFoot").gameObject;
    }

    void LateUpdate()
    {
        // Using late update so that the position values we send are taken after all possible calculations (physics, etc).

        if (!_instantiated)
        {
            if (!_commandSender.isConnected()) { return; }

            // Initialise the GRETA environment if it hasn't been done before.
            _commandSender.NotifyCharacter(
                character,
                characterHead,
                characterLeftEye, characterRightEye,
                characterMouth,
                characterLeftHand, characterRightHand,
                characterLeftFoot, characterRightFoot);
            character.transform.hasChanged = false;

            _instantiated = true;
        }
        else
        {
            if (character.transform.hasChanged)
            {
                _commandSender.NotifyCharacter(
                    character,
                    characterHead,
                    characterLeftEye, characterRightEye,
                    characterMouth,
                    characterLeftHand, characterRightHand,
                    characterLeftFoot, characterRightFoot);
                character.transform.hasChanged = false;
            }
        }
    }
}
