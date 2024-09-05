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
using System.Collections.Generic;

using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Util;
using Apache.NMS.ActiveMQ.Transactions;

namespace Apache.NMS.ActiveMQ
{
    /// <summary>
    /// Policy class used to configure the options associated with TX
    /// recovery.
    /// </summary>
    public class NetTxRecoveryPolicy : ICloneable
    {
        private static readonly FactoryFinder<RecoveryLoggerFactoryAttribute, IRecoveryLoggerFactory> FACTORY_FINDER =
            new FactoryFinder<RecoveryLoggerFactoryAttribute, IRecoveryLoggerFactory>();

        private static readonly IDictionary<String, Type> LOGGER_FACTORY_TYPES = new Dictionary<String, Type>();

        private IRecoveryLogger recoveryLogger = new RecoveryFileLogger();

        public void RegisterRecoveryLoggerFactory(string scheme, Type factoryType)
        {
            LOGGER_FACTORY_TYPES[scheme] = factoryType;
        }

        public string RecoveryLoggerType
        {
            get { return this.recoveryLogger != null ? this.recoveryLogger.LoggerType : ""; }
            set
            {
                if(string.IsNullOrEmpty(value))
                {
                    throw new NMSException(String.Format("Recovery Logger name invalid: [{0}]", value));
                }

                IRecoveryLoggerFactory factory = null;

                try
                {
                    factory = NewInstance(value.ToLower());
                }
                catch(NMSException)
                {
                    throw;
                }
                catch(Exception e)
                {
                    throw NMSExceptionSupport.Create("Error creating Recovery Logger", e);
                }

                this.recoveryLogger = factory.Create();
            }
        }

        public IRecoveryLogger RecoveryLogger
        {
            get { return this.recoveryLogger; }
            set { this.recoveryLogger = value; }
        }

        private static IRecoveryLoggerFactory NewInstance(string scheme)
        {
            try
            {
                Type factoryType = FindLoggerFactory(scheme);

                if(factoryType == null)
                {
                    throw new Exception("NewInstance failed to find a match for id = " + scheme);
                }

                return (IRecoveryLoggerFactory) Activator.CreateInstance(factoryType);
            }
            catch(Exception ex)
            {
                Tracer.WarnFormat("NewInstance failed to create an IRecoveryLoggerFactory with error: {1}", ex.Message);
                throw;
            }
        }

        private static Type FindLoggerFactory(string scheme)
        {
            if(LOGGER_FACTORY_TYPES.ContainsKey(scheme))
            {
                return LOGGER_FACTORY_TYPES[scheme];
            }

            try
            {
                Type factoryType = FACTORY_FINDER.FindFactoryType(scheme);
                LOGGER_FACTORY_TYPES[scheme] = factoryType;
                return factoryType;
            }
            catch
            {
                throw new NMSException("Failed to find Factory for Recovery Logger type: " + scheme);
            }
        }

        public Object Clone()
        {
            return this.MemberwiseClone();
        }        
    }
}

