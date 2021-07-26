using System.Collections;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;


[InitializeOnLoad]
public class NewBehaviourScript : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        GetAllObjectsInScene();
        Debug.Log("START");
    }

    // Update is called once per frame
    void Update()
    {
        GetAllObjectsInScene();
        Debug.Log("UPDATE");
    }

    private static List<GameObject> GetAllObjectsInScene()
    {
        List<GameObject> objectsInScene = new List<GameObject>();

        foreach (GameObject go in Resources.FindObjectsOfTypeAll(typeof(GameObject)) as GameObject[])
        {
            if (go.hideFlags != HideFlags.None)
                continue;

            if (UnityEditor.PrefabUtility.GetPrefabType(go) == UnityEditor.PrefabType.Prefab || UnityEditor.PrefabUtility.GetPrefabType(go) == UnityEditor.PrefabType.ModelPrefab)
                continue;

            objectsInScene.Add(go);
        }
        return objectsInScene;
    }
}