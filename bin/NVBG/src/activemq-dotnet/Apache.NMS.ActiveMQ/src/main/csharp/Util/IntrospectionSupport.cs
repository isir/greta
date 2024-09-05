/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Reflection;
using System.Globalization;
using System.Collections.Generic;
using System.Collections.Specialized;

using Apache.NMS;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// Utility class used to provide conveince methods that apply named property
    /// settings to objects.
    /// </summary>
    public class IntrospectionSupport
    {
        /// <summary>
        /// Sets the public properties of a target object using a string map.
        /// This method uses .Net reflection to identify public properties of
        /// the target object matching the keys from the passed map.
        /// </summary>
        /// <param name="target">The object whose properties will be set.</param>
        /// <param name="map">Map of key/value pairs.</param>
        public static void SetProperties(object target, StringDictionary map)
        {
            SetProperties(target, map, "");
        }

        /// <summary>
        /// Sets the public properties of a target object using a string map.
        /// This method uses .Net reflection to identify public properties of
        /// the target object matching the keys from the passed map.
        /// </summary>
        /// <param name="target">The object whose properties will be set.</param>
        /// <param name="map">Map of key/value pairs.</param>
        /// <param name="prefix">Key value prefix.  This is prepended to the property name
        /// before searching for a matching key value.</param>
        public static void SetProperties(object target, StringDictionary map, string prefix)
        {
            Tracer.DebugFormat("SetProperties called with target: {0}, and prefix: {1}",
                               target.GetType().Name, prefix);

            foreach(string key in map.Keys)
            {
                if(key.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
                {
                    string propertyName = key.Substring(prefix.Length);

                    // Process all member assignments at this level before processing
                    // any deeper member assignments.
                    if(!propertyName.Contains("."))
                    {
                        MemberInfo member = FindPropertyInfo(target, propertyName);

                        if(member == null)
                        {
                            throw new NMSException(string.Format("No such property or field: {0} on class: {1}", propertyName, target.GetType().Name));
                        }

                        try
                        {
                            if(member.MemberType == MemberTypes.Property)
                            {
                                PropertyInfo property = member as PropertyInfo;
                                property.SetValue(target, Convert.ChangeType(map[key], property.PropertyType, CultureInfo.InvariantCulture), null);
                            }
                            else
                            {
                                FieldInfo field = member as FieldInfo;
                                field.SetValue(target, Convert.ChangeType(map[key], field.FieldType, CultureInfo.InvariantCulture));
                            }
                        }
                        catch(Exception ex)
                        {
                            throw NMSExceptionSupport.Create("Error while attempting to apply option.", ex);
                        }
                    }
                }
            }

            IList<string> propertiesSet = new List<string>();

            // Now process any compound assignments, ensuring that once we recurse into an
            // object we don't do it again as there could be multiple compunds element assignments
            // and they'd have already been processed recursively.
            foreach(string key in map.Keys)
            {
                if(key.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
                {
                    string propertyName = key.Substring(prefix.Length);

                    if(propertyName.Contains("."))
                    {
                        string newTargetName = propertyName.Substring(0, propertyName.IndexOf('.'));
                        string newPrefix = prefix + newTargetName + ".";

                        if(!propertiesSet.Contains(newPrefix))
                        {
                            MemberInfo member = FindPropertyInfo(target, newTargetName);
                            object newTarget = GetUnderlyingObject(member, target);
                            SetProperties(newTarget, map, newPrefix);
                            propertiesSet.Add(newPrefix);
                        }
                    }
                }
            }
        }

        private static object GetUnderlyingObject(MemberInfo member, object target)
        {
            object result = null;

            if(member.MemberType == MemberTypes.Field)
            {
                FieldInfo field = member as FieldInfo;

                if(field.FieldType.IsPrimitive)
                {
                    throw new NMSException("The field given is a priomitive type: " + member.Name);
                }

                result = field.GetValue(target);
            }
            else
            {
                PropertyInfo property = member as PropertyInfo;
                MethodInfo getter = property.GetGetMethod();

                if(getter == null)
                {
                    throw new NMSException("Cannot access member: " + member.Name);
                }

                result = getter.Invoke(target, null);
            }

            if(result == null)
            {
                throw new NMSException(String.Format("Could not retrieve the value of member {0}."), member.Name);
            }

            return result;
        }

        private static MemberInfo FindPropertyInfo(object target, string name)
        {
            BindingFlags flags = BindingFlags.FlattenHierarchy
                               | BindingFlags.Public
                               | BindingFlags.Instance
                               | BindingFlags.IgnoreCase;

            Type type = target.GetType();

            MemberInfo member = type.GetProperty(name, flags);

            if(member == null)
            {
                member = type.GetField(name, flags);
            }

            return member;
        }

    }
}

