// OSC Jack - Open Sound Control plugin for Unity
// https://github.com/keijiro/OscJack

using UnityEngine;
using System;
using System.Reflection;
using System.Collections.Generic;

namespace OscJack
{
    [AddComponentMenu("OSC/Property Sender")]
    public sealed class OscPropertySender : MonoBehaviour
    {
        #region Editable fields

        [SerializeField] string _ipAddress = "127.0.0.1";
        [SerializeField] int _udpPort = 9100;
        [SerializeField] string _oscAddress = "/unity";
        [SerializeField] string _dataSource = null;
        [SerializeField] string _propertyName = "";
        [SerializeField] bool _keepSending = false;

        #endregion


        #region Internal members

        OscClient _client;
        PropertyInfo _propertyInfo;
        string data = "";
        List<string> lista = new List<string>();
        void UpdateSettings()
        {
            _client = OscMaster.GetSharedClient(_ipAddress, _udpPort);

            if (_dataSource != null && !string.IsNullOrEmpty(_propertyName))
                _propertyInfo = _dataSource.GetType().GetProperty(_propertyName);
            else
                _propertyInfo = null;
            _dataSource = "string";
        }

        #endregion

        #region MonoBehaviour implementation

        void Start()
        {
            UpdateSettings();
        }

        void OnValidate()
        {
            if (Application.isPlaying) UpdateSettings();
        }

        void Update()
        {

            //Debug.Log("Update");
            if (_dataSource == "string")
            {
                //Debug.Log("[INFO]:" + "haha");
                Send((string) "");
            }
        }

        #endregion

        #region Sender methods

        int _intValue = Int32.MaxValue;

        public void Send(int data)
        {
            if (!_keepSending && data == _intValue) return;
            _client.Send(_oscAddress, data);
            _intValue = data;
        }

        float _floatValue = Single.MaxValue;

        public void Send(float data)
        {
            if (!_keepSending && data == _floatValue) return;
            _client.Send(_oscAddress, data);
            _floatValue = data;
        }

        Vector2 _vector2Value = new Vector2(Single.MaxValue, 0);

        public void Send(Vector2 data)
        {
            if (!_keepSending && data == _vector2Value) return;
            _client.Send(_oscAddress, data.x, data.y);
            _vector2Value = data;
        }

        Vector3 _vector3Value = new Vector3(Single.MaxValue, 0, 0);

        public void Send(Vector3 data)
        {
            if (!_keepSending && data == _vector3Value) return;
            _client.Send(_oscAddress, data.x, data.y, data.z);
            _vector3Value = data;
        }

        Vector4 _vector4Value = new Vector4(Single.MaxValue, 0, 0, 0);

        public void Send(Vector4 data)
        {
            if (!_keepSending && data == _vector4Value) return;
            _client.Send(_oscAddress, data.x, data.y, data.z, data.w);
            _vector4Value = data;
        }

        Vector2Int _vector2IntValue = new Vector2Int(Int32.MaxValue, 0);

        public void Send(Vector2Int data)
        {
            if (!_keepSending && data == _vector2IntValue) return;
            _client.Send(_oscAddress, data.x, data.y);
            _vector2IntValue = data;
        }

        Vector3Int _vector3IntValue = new Vector3Int(Int32.MaxValue, 0, 0);

        public void Send(Vector3Int data)
        {
            if (!_keepSending && data == _vector3IntValue) return;
            _client.Send(_oscAddress, data.x, data.y, data.z);
            _vector3IntValue = data;
        }

        string _stringValue = string.Empty;

        public void Send(string data)
        {

            if (!_keepSending && data == _stringValue) return;
            data = "";
            _stringValue = "";
            _stringValue = RecursiveBones(GameObject.Find("Character1").gameObject);
           // Debug.Log("[INFO]:" + _stringValue);
            _client.Send(_oscAddress, _stringValue);
            //Debug.Log("Message sent");
        }

        #endregion

        public String RecursiveBones(GameObject parent)
        {
            int i = 0;
            //Debug.Log("Bool:"+parent.name+"  " + lista.Contains(parent.name));
            if ((parent.name.Contains("Character") || parent.name.Contains("#") ) && !lista.Contains(parent.name))
            {
                //Debug.Log("Object Added "+ parent.name);
                data = data + "," + parent.name;
                lista.Add(parent.name);
            }
            //Debug.Log("[INFO NUMBER]:" + parent.transform.childCount + "   " + parent.name);
            while (parent.transform.childCount != 0)
            {
                //Debug.Log("[INFO]:" + parent.transform.GetChild(i).gameObject.name);
                RecursiveBones(parent.transform.GetChild(i).gameObject);
                i++;
                if (parent.name == "Head")
                {
                    //Debug.Log("HEAD Found" + parent);
                }
                if (i == parent.transform.childCount)
                {
                    
                    return data;
                }

            }

            return data;
        }
    }


}
