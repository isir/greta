using UnityEngine;

[RequireComponent(typeof(GretaCharacterAnimator))]
public class AnimationCommandSenderTester : MonoBehaviour {

    public string animationID;

    private GretaCharacterAnimator _charAnimScript;

    void Start()
    {
        _charAnimScript = GetComponent<GretaCharacterAnimator>();
    }

    // Update is called once per frame
    void Update ()
    {
        if (Input.GetKeyUp(KeyCode.T) && animationID != null && animationID.Trim().Length > 0)
        {
            _charAnimScript.PlayAgentAnimation(animationID);
        }
    }
}