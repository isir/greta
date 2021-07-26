using OscJack;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using UnityEngine;
using UnityEngine.Animations;
using UnityEngine.Animations.Rigging;

public class DisplayRecursivelyBones : MonoBehaviour
{
    public MultiAimConstraint _constraint;
    public MultiAimConstraint _constraint_chest;
    protected OscEventReceiver _oscReceiver;
    public Transform HeadAim;
    public Transform ChestAim;
    public Transform MoveObject;
    public float ObjecSpeed;
    public bool contains_head = false;
    public Transform t1 = null;
    // Start is called before the first frame update
    void Start()
    {
        _oscReceiver = GetComponent<OscEventReceiver>();

        /*
        GameObject brad = GameObject.Find("Character1");
        GameObject elbow = brad.transform.Find("Camille").gameObject;
        GameObject master = elbow.transform.Find("master").gameObject;
        Debug.Log("MASTER FAIT 1" + master.ToString());
        GameObject reference = master.transform.Find("Reference").gameObject;
        Debug.Log("Reference FAIT 1" + reference.ToString());
        GameObject hips = reference.transform.Find("Hips").gameObject;
        Debug.Log("Hips FAIT 1" + hips.ToString());
        GameObject spine = hips.transform.Find("Spine").gameObject;
        Debug.Log("Spine FAIT 1" + spine.ToString());
        GameObject spine1 = spine.transform.Find("Spine1").gameObject;
        Debug.Log("Spine1 FAIT 1" + spine1.ToString());
        GameObject spine2 = spine1.transform.Find("Spine2").gameObject;
        Debug.Log("Spine2 FAIT 1" + spine2.ToString());
        GameObject spine3 = spine2.transform.Find("Spine3").gameObject;
        Debug.Log("Spine3 FAIT 1" + spine3.ToString());
        GameObject spine4 = spine3.transform.Find("Spine4").gameObject;
        Debug.Log("Spine4 FAIT 1" + spine4.ToString());
        GameObject neck = spine4.transform.Find("Neck").gameObject;
        Debug.Log("Neck FAIT 1" + neck.ToString());
        GameObject neck1 = neck.transform.Find("Neck1").gameObject;
        Debug.Log("Neck1 FAIT 1" + neck1.ToString());
        GameObject head = neck1.transform.Find("Head").gameObject;
        Debug.Log("head" + head.ToString());
        Debug.Log("Test child "+head.transform.GetChild(0));
        //Debug.Log(SceneManager.getGetActiveScene().ToString());
        */
        //RecursiveBones(this.gameObject);
       GameObject character = GameObject.Find("Character1").gameObject;
       GameObject g = character.gameObject.transform.Find("Rig1").gameObject;
       Debug.Log("FASE1");
       GameObject g1 = g.gameObject.transform.Find("HeadAim").gameObject;
       Debug.Log(g1.ToString());
        GameObject target = g.gameObject.transform.Find("#Target").gameObject;
        // A mettre dans le OnDataReceive de OSC Event Receiver
        //.g1.GetComponent("MultiAimConstraint");
        //var wta = new WeightedTransformArray(1);
        // wta.Add(new WeightedTransform(target.transform, 1f));
        //_constraint.data.sourceObjects = wta;
    }

    // Update is called once per frame
    void FixedUpdate()
    {
        MoveToTarget();
    }

    public void OnReceive(Vector3 point)
    {
        Debug.Log("Received point:"+point);
    }

    public void OnReceive(string name)
    {
        if (name.Length < 2)
        {

        }
        else
        {
            Debug.Log("TROVATO " + name);
            RecursiveBones(GameObject.Find("Character1").gameObject);
            if (contains_head)
            {
                Debug.Log("FATTO " + HeadAim);
                _constraint.data.constrainedObject = HeadAim;
                _constraint_chest.data.constrainedObject = ChestAim;
            }
            _constraint.data.offset = new Vector3(90, 0, 0);
            Debug.Log("Offset " + _constraint.data.offset.x);
            // WeightedTransformArray wta = new WeightedTransformArray(1);
            // wta.Add(new WeightedTransform(GameObject.Find("Target").transform, 1f));
            GameObject character = GameObject.Find("Character1").gameObject;
            GameObject g = character.gameObject.transform.Find("Rig1").gameObject;
            GameObject g2 = character.gameObject.transform.Find("MovingObject").gameObject;
            Debug.Log("FASE1");
            GameObject g1 = g.gameObject.transform.Find("HeadAim").gameObject;
            Debug.Log("[INFO 2] " + g1.ToString() + "   " + name);

            GameObject target = g.gameObject.transform.Find(name).gameObject;
            Debug.Log(target);
            var wta = new WeightedTransformArray(0);
            wta.Add(new WeightedTransform(g2.transform, 1f));
            _constraint.data.constrainedObject = HeadAim;
            _constraint_chest.data.constrainedObject = ChestAim;
            _constraint.data.sourceObjects = wta;
            _constraint.data.limits = new Vector2(-90, 130);
            _constraint.data.aimAxis = (MultiAimConstraintData.Axis)Axis.Y;
            _constraint_chest.data.sourceObjects = wta;
            _constraint_chest.data.limits = new Vector2(-110, 110);
            _constraint_chest.data.aimAxis = (MultiAimConstraintData.Axis)Axis.Y;
            _constraint_chest.data.offset = new Vector3(90, 0, 0);
            RigBuilder rigs = GetComponent<RigBuilder>();
            rigs.Build();
            t1 = target.transform;
        }
    }

    public bool RecursiveBones(GameObject parent)
    {
        int i = 0;
        Debug.Log("[INFO NUMBER]:" + parent.transform.childCount+ "   "+parent.name);
        while (parent.transform.childCount != 0)
        {
              Debug.Log("[INFO]:" + parent.transform.GetChild(i).gameObject.name);
              RecursiveBones(parent.transform.GetChild(i).gameObject);
              i++;
            if (parent.name == "Head")
            {
                Debug.Log("HEAD Found" + parent);
                HeadAim = parent.transform;
                
            }
            if (parent.name == "Spine3")
            {
                ChestAim = parent.transform;
            }


            if (i == parent.transform.childCount)
            {
                return false;
            }
                
            }


        return contains_head;
    }

    public void MoveToTarget()
    {
        if(t1!=null)
        {
            Debug.Log("Moving Object from position:" + MoveObject.position + "   to:" + t1.position + "   " + ObjecSpeed * Time.deltaTime);
            MoveObject.position = Vector3.MoveTowards(MoveObject.position, t1.position, ObjecSpeed * Time.fixedDeltaTime);
            Debug.Log("Moving Object new pos:" + MoveObject.position);
        }
    }

}

