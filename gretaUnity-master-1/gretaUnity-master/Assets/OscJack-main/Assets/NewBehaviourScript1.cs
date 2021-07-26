using UnityEngine;
using System.Collections;
using UnityEditor;
using System.Collections.Generic;
using System.Xml;
using System.IO;
using System.Text;
using UnityEngine.SceneManagement;
public class MyEditor : Editor
{
    //Export all game scenes to XML format
    [MenuItem("GameObject/ExportXML")]
    static void ExportXML()
    {
        string LevelName = null;
        SceneManager.getGetActiveScene();
        if (LevelName == "")
            Debug.LogError("Please save your scene");
        string filepath = Application.dataPath + @"/StreamingAssets/" + LevelName + ".xml";
        //string filepath = Application.dataPath + @"/StreamingAssets/lv01.xml";
        if (!File.Exists(filepath))
        {
            File.Delete(filepath);
        }
        XmlDocument xmlDoc = new XmlDocument();
        XmlElement root = xmlDoc.CreateElement("gameObjects");
        Debug.Log(UnityEditor.EditorBuildSettings.scenes[0]);
        //Traverse all game scenes
        foreach (UnityEditor.EditorBuildSettingsScene S in UnityEditor.EditorBuildSettings.scenes)
        {
            Debug.Log("123");

            //When the level is enabled
            if (S.enabled)
            {
                //Get the name of the level
                string name = S.path;
                //Open this level
                EditorApplication.OpenScene(name);
                XmlElement scenes = xmlDoc.CreateElement("scenes");
                scenes.SetAttribute("name", name);
                foreach (GameObject obj in Object.FindObjectsOfType(typeof(GameObject)))
                {
                    if (obj.transform.parent == null)
                    {
                        XmlElement gameObject = xmlDoc.CreateElement("gameObjects");
                        gameObject.SetAttribute("name", obj.name);


                        gameObject.SetAttribute("asset", obj.name + ".prefab");
                        XmlElement transform = xmlDoc.CreateElement("transform");
                        XmlElement position = xmlDoc.CreateElement("position");
                        XmlElement position_x = xmlDoc.CreateElement("x");
                        position_x.InnerText = obj.transform.position.x + "";
                        XmlElement position_y = xmlDoc.CreateElement("y");
                        position_y.InnerText = obj.transform.position.y + "";
                        XmlElement position_z = xmlDoc.CreateElement("z");
                        position_z.InnerText = obj.transform.position.z + "";
                        position.AppendChild(position_x);
                        position.AppendChild(position_y);
                        position.AppendChild(position_z);

                        XmlElement rotation = xmlDoc.CreateElement("rotation");
                        XmlElement rotation_x = xmlDoc.CreateElement("x");
                        rotation_x.InnerText = obj.transform.rotation.eulerAngles.x + "";
                        XmlElement rotation_y = xmlDoc.CreateElement("y");
                        rotation_y.InnerText = obj.transform.rotation.eulerAngles.y + "";
                        XmlElement rotation_z = xmlDoc.CreateElement("z");
                        rotation_z.InnerText = obj.transform.rotation.eulerAngles.z + "";
                        rotation.AppendChild(rotation_x);
                        rotation.AppendChild(rotation_y);
                        rotation.AppendChild(rotation_z);

                        XmlElement scale = xmlDoc.CreateElement("scale");
                        XmlElement scale_x = xmlDoc.CreateElement("x");
                        scale_x.InnerText = obj.transform.localScale.x + "";
                        XmlElement scale_y = xmlDoc.CreateElement("y");
                        scale_y.InnerText = obj.transform.localScale.y + "";
                        XmlElement scale_z = xmlDoc.CreateElement("z");
                        scale_z.InnerText = obj.transform.localScale.z + "";

                        scale.AppendChild(scale_x);
                        scale.AppendChild(scale_y);
                        scale.AppendChild(scale_z);

                        transform.AppendChild(position);
                        transform.AppendChild(rotation);
                        transform.AppendChild(scale);

                        gameObject.AppendChild(transform);
                        scenes.AppendChild(gameObject);
                        root.AppendChild(scenes);
                        xmlDoc.AppendChild(root);
                        xmlDoc.Save(filepath);

                    }
                }
            }
        }
        //Refresh the Project view, otherwise you need to refresh manually
        AssetDatabase.Refresh();
        Debug.LogError("Scene saving completed!!");

    }
}
