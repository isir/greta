using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;

namespace NVBG
{
    // Methods to aid xml parsing
    class XMLHelperMethods
    {        
        public static void AttachAttributeToNode(XmlDocument _inputDoc, XmlNode _node, string _attributeName, string _attributeValue)
        {
            _node.Attributes.RemoveNamedItem(_attributeName);
            XmlAttribute attribute = _inputDoc.CreateAttribute(_attributeName);
            attribute.Value = _attributeValue;
            _node.Attributes.Append(attribute);
        }
    }
}
