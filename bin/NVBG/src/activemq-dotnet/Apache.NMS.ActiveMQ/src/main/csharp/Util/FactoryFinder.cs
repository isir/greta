/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Reflection;
using System.Collections.Generic;

namespace Apache.NMS.ActiveMQ.Util
{
    public class FactoryFinder<AttributeType, FactoryType> where AttributeType : FactoryAttribute
    {
        private static IDictionary<string, bool> DEFAULT_EXCLUDES;

        static FactoryFinder()
        {
            DEFAULT_EXCLUDES = new Dictionary<string, bool>();

            DEFAULT_EXCLUDES.Add("mscorlib", true);
            DEFAULT_EXCLUDES.Add("System", true);
            DEFAULT_EXCLUDES.Add("Mono", true);
            DEFAULT_EXCLUDES.Add("Microsoft", true);
            DEFAULT_EXCLUDES.Add("nunit", true);
        }

        public Type FindFactoryType(string factoryId)
        {
            try
            {
                // Look in this assembly first as its most likely to be the source, if we
                // don't find it here then we expand out to all the currently loaded
                // assemblies in the current AppDomain.  We could also start searching
                // through all referenced assemblies in all the currently loaded ones but
                // that could get out of hand so try this first.
                Type result = this.SearchAssembly(Assembly.GetExecutingAssembly(), factoryId);

                if(result == null)
                {
                    Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();

                    foreach(Assembly assembly in assemblies)
                    {
                        if(!IsExcluded(assembly))
                        {
                            result = SearchAssembly(assembly, factoryId);

                            if(result != null)
                            {
                                break;
                            }
                        }
                    }
                }

                return result;
            }
            catch
            {
                return null;
            }
        }

        private bool IsExcluded(Assembly assembly)
        {
            if(assembly.Equals(Assembly.GetExecutingAssembly()))
            {
                return true;
            }

            string name = assembly.GetName().Name;

            foreach(string key in DEFAULT_EXCLUDES.Keys)
            {
                if(name.StartsWith(key))
                {
                    return true;
                }
            }

            return false;
        }

        private Type SearchAssembly(Assembly assembly, string factoryId)
        {
            Tracer.DebugFormat("Searching Assembly: {0} for factory of the id: {1}",
                               assembly.GetName().Name, factoryId);

            Type[] types = assembly.GetTypes();

            foreach(Type type in types)
            {
                object[] attributes = type.GetCustomAttributes(false);
                foreach(Attribute attribute in attributes)
                {
                    if(attribute is AttributeType)
                    {
                        FactoryAttribute factoryAttribute = (FactoryAttribute)attribute;
                        if(factoryAttribute.FactoryIdentifier.Equals(factoryId))
                        {
                            Tracer.DebugFormat("Found the Factory of type {0} for id: {1}",
                                               type.ToString(), factoryId);
                            return type;
                        }
                    }
                }
            }

            return null;
        }
    }
}

