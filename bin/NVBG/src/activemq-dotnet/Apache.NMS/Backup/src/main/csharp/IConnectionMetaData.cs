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

namespace Apache.NMS
{
    /// <summary>
    /// Provides information describing the NMS IConnection instance.
    /// </summary>
    public interface IConnectionMetaData
    {
        /// <value>
        /// Get the Major version number of the NMS API this Provider supports.
        /// </value>
        int NMSMajorVersion{ get; }
        
        /// <value>
        /// Get the Minor version number of the NMS API this Provider supports.
        /// </value>
        int NMSMinorVersion{ get; }
        
        /// <value>
        /// Get the name of this NMS Provider. 
        /// </value>
        string NMSProviderName{ get; }
        
        /// <value>
        /// Gets a formatted string detailing the NMS API version this Provider supports.  
        /// </value>
        string NMSVersion{ get; }
        
        /// <value>
        /// Gets a String array of all the NMSX property names this NMS Provider supports.
        /// </value>
        string[] NMSXPropertyNames{ get; }
        
        /// <value>
        /// Gets the Providers Major version number. 
        /// </value>
        int ProviderMajorVersion{ get; }

        /// <value>
        /// Gets the Providers Minor version number. 
        /// </value>
        int ProviderMinorVersion{ get; }

        /// <value>
        /// Gets a formatted string detailing the version of this NMS Provider.  
        /// </value>
        string ProviderVersion{ get; }
    }
}
