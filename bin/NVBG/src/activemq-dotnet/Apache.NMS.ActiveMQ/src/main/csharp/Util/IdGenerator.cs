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
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace Apache.NMS.ActiveMQ.Util
{
    public class IdGenerator
    {
        private static readonly String UNIQUE_STUB;
        private static int instanceCount;
        private static readonly String hostName;
        private readonly String seed;
        private long sequence;
    
        static IdGenerator()
        {
            String stub = "-1-" + DateTime.Now.Ticks;
            hostName = "localhost";

            try
            {
                hostName = Dns.GetHostName();
                IPEndPoint endPoint = new IPEndPoint(IPAddress.Any, 0);
                Socket tempSocket = new Socket(endPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                tempSocket.Bind(endPoint);
                stub = "-" + ((IPEndPoint)tempSocket.LocalEndPoint).Port + "-" + DateTime.Now.Ticks + "-";
                Thread.Sleep(100);
                tempSocket.Close();
            }
            catch(Exception ioe)
            {
                Tracer.Warn("could not generate unique stub: " + ioe.Message);
            }

            UNIQUE_STUB = stub;
        }
    
        /**
         * Construct an IdGenerator
         */
        public IdGenerator(String prefix)
        {
            lock(UNIQUE_STUB)
            {
                this.seed = prefix + UNIQUE_STUB + (instanceCount++) + ":";
            }
        }

        public IdGenerator() : this("ID:" + hostName)
        {
        }
    
        /// <summary>
        /// As we have to find the hostname as a side-affect of generating a unique
        /// stub, we allow it's easy retrevial here
        /// </summary>
        public static String HostName
        {
            get { return hostName; }
        }

        /// <summary>
        /// Generate a Unique Id
        /// </summary>
        /// <returns>
        /// A <see cref="String"/>
        /// </returns>
        public String GenerateId()
        {
            lock(UNIQUE_STUB)
            {
                return this.seed + (this.sequence++);
            }
        }

        /// <summary>
        /// Generate a unique ID - that is friendly for a URL or file system
        /// </summary>
        /// <returns>
        /// A <see cref="String"/>
        /// </returns>
        public String GenerateSanitizedId()
        {
            String result = GenerateId();
            result = result.Replace(':', '-');
            result = result.Replace('_', '-');
            result = result.Replace('.', '-');
            return result;
        }
    
        /// <summary>
        /// From a generated id - return the seed (i.e. minus the count)
        /// </summary>
        /// <param name="id">
        /// A <see cref="String"/>
        /// </param>
        /// <returns>
        /// A <see cref="String"/>
        /// </returns>
        public static String GetSeedFromId(String id)
        {
            String result = id;

            if(id != null)
            {
                int index = id.LastIndexOf(':');
                if(index > 0 && (index + 1) < id.Length)
                {
                    result = id.Substring(0, index + 1);
                }
            }

            return result;
        }
    
        /// <summary>
        /// From a generated id - return the generator count
        /// </summary>
        /// <param name="id">
        /// A <see cref="String"/>
        /// </param>
        /// <returns>
        /// A <see cref="System.Int64"/>
        /// </returns>
        public static long GetSequenceFromId(String id)
        {
            long result = -1;
            if(id != null)
            {
                int index = id.LastIndexOf(':');
    
                if(index > 0 && (index + 1) < id.Length)
                {
                    String numStr = id.Substring(index + 1, id.Length);
                    result = Int64.Parse(numStr);
                }
            }
            return result;
        }

        /// <summary>
        /// Does a proper compare on the ids
        /// </summary>
        /// <param name="id1">
        /// A <see cref="String"/>
        /// </param>
        /// <param name="id2">
        /// A <see cref="String"/>
        /// </param>
        /// <returns>
        /// A <see cref="System.Int32"/>
        /// </returns>
        public static int Compare(String id1, String id2)
        {
            int result = -1;

            String seed1 = IdGenerator.GetSeedFromId(id1);
            String seed2 = IdGenerator.GetSeedFromId(id2);

            if(seed1 != null && seed2 != null)
            {
                result = seed1.CompareTo(seed2);

                if(result == 0)
                {
                    long count1 = IdGenerator.GetSequenceFromId(id1);
                    long count2 = IdGenerator.GetSequenceFromId(id2);
                    result = (int)(count1 - count2);
                }
            }

            return result;
        }
    }
}
