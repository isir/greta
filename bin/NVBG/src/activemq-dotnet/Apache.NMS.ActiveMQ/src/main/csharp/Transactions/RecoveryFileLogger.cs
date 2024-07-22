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
using System.IO;
using System.Reflection;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;

using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transactions
{
    public class RecoveryFileLogger : IRecoveryLogger
    {
        private readonly object syncRoot = new object();

        private string location;
        private bool autoCreateLocation;
        private string resourceManagerId;

        public RecoveryFileLogger()
        {
            // Set the path by default to the location of the executing assembly.
            // May need to change this to current working directory, not sure.
            this.location = string.Empty;
        }

        /// <summary>
        /// The Unique Id of the Resource Manager that this logger is currently
        /// logging recovery information for.
        /// </summary>
        public string ResourceManagerId
        {
            get { return this.resourceManagerId; }
        }

        /// <summary>
        /// The Path to the location on disk where the recovery log is written
        /// to and read from.
        /// </summary>
        public string Location
        {
            get
            {
                if(string.IsNullOrEmpty(this.location))
                {
                    return Directory.GetCurrentDirectory();
                }

                return this.location;
            }

            set
            {
                this.location = Uri.UnescapeDataString(value);
            }
        }

        /// <summary>
        /// Indiciate that the Logger should create and sibdirs of the
        /// given location that don't currently exist.
        /// </summary>
        public bool AutoCreateLocation
        {
            get { return this.autoCreateLocation; }
            set { this.autoCreateLocation = value; }
        }

        public void Initialize(string resourceManagerId)
        {
            this.resourceManagerId = resourceManagerId;

            // Test if the location configured is valid.
            if(!Directory.Exists(Location))
            {
                if(AutoCreateLocation)
                {
                    try
                    {
                        Directory.CreateDirectory(Location);
                    }
                    catch(Exception ex)
                    {
                        Tracer.Error("Failed to create log directory: " + ex.Message);
                        throw NMSExceptionSupport.Create(ex);
                    }
                }
                else
                {
                    throw new NMSException("Configured Recovery Log Location does not exist: " + location);
                }
            }
        }

        public void LogRecoveryInfo(XATransactionId xid, byte[] recoveryInformation)
        {
            if (recoveryInformation == null || recoveryInformation.Length == 0)
            {
                return;
            }

            try
            {
                string filename = this.CreateFilename(xid);

                RecoveryInformation info = new RecoveryInformation(xid, recoveryInformation);
                Tracer.Debug("Serializing Recovery Info to file: " + filename);

                IFormatter formatter = new BinaryFormatter();
                using (FileStream recoveryLog = new FileStream(filename, FileMode.OpenOrCreate, FileAccess.Write))
                {
                    formatter.Serialize(recoveryLog, info);
                }

            }
            catch (Exception ex)
            {
                Tracer.Error("Error while storing TX Recovery Info, message: " + ex.Message);
                throw;
            }
        }

        public KeyValuePair<XATransactionId, byte[]>[] GetRecoverables()
        {
            IList<RecoveryInformation> infos = this.TryOpenRecoveryInfoFile();

            KeyValuePair<XATransactionId, byte[]>[] results = new KeyValuePair<XATransactionId, byte[]>[infos.Count];

            int index = 0;
            foreach (RecoveryInformation info in infos)
            {
                results[index++] = new KeyValuePair<XATransactionId, byte[]>(info.Xid, info.TxRecoveryInfo);
            }

            return results;
        }

        public void LogRecovered(XATransactionId xid)
        {
            try
            {
                string filename = this.CreateFilename(xid);

                Tracer.Debug("Attempting to remove stale Recovery Info file: " + filename);

                File.Delete(filename);
            }
            catch (Exception ex)
            {
                Tracer.Debug("Caught Exception while removing stale RecoveryInfo file: " + ex.Message);
            }
        }

        public void Purge()
        {
            lock (this.syncRoot)
            {
                try
                {
                    IEnumerable<string> files = this.GetFilesForResourceManagerId();

                    foreach (var file in files)
                    {
                        Tracer.Debug("Attempting to remove stale Recovery Info file: " + file);
                        File.Delete(file);
                    }
                }
                catch (Exception ex)
                {
                    Tracer.Debug("Caught Exception while removing stale RecoveryInfo file: " + ex.Message);
                }
            }
        }

        private IEnumerable<string> GetFilesForResourceManagerId()
        {
            return Directory.GetFiles(this.Location, this.ResourceManagerId + "_*.bin");
        }

        public string LoggerType
        {
            get { return "file"; }
        }

        #region Recovery File Opeations

        private string CreateFilename(XATransactionId xaTransactionId)
        {
            return string.Format(
                "{0}{1}{2}_{3}.bin", 
                this.Location, 
                Path.DirectorySeparatorChar, 
                this.ResourceManagerId,
                GetHexValue(xaTransactionId));
        }
        
        private static string GetHexValue(XATransactionId xid)
        {
            string transactionIdHexValue = BitConverter.ToString(xid.GlobalTransactionId);
            return transactionIdHexValue.Replace("-", string.Empty);
        }

        [Serializable]
        private sealed class RecoveryInformation
        {
            private byte[] txRecoveryInfo;
            private byte[] globalTxId;
            private byte[] branchId;
            private int formatId;

            public RecoveryInformation(XATransactionId xaId, byte[] recoveryInfo)
            {
                this.Xid = xaId;
                this.txRecoveryInfo = recoveryInfo;
            }

            public byte[] TxRecoveryInfo
            {
                get { return this.txRecoveryInfo; }
                set { this.txRecoveryInfo = value; }
            }

            public XATransactionId Xid
            {
                get
                {
                    XATransactionId xid = new XATransactionId();
                    xid.BranchQualifier = this.branchId;
                    xid.GlobalTransactionId = this.globalTxId;
                    xid.FormatId = this.formatId;

                    return xid;
                }

                set
                {
                    this.branchId = value.BranchQualifier;
                    this.globalTxId = value.GlobalTransactionId;
                    this.formatId = value.FormatId;
                }
            }
        }

        private IList<RecoveryInformation> TryOpenRecoveryInfoFile()
        {
            List<RecoveryInformation> result = new List<RecoveryInformation>();

            IEnumerable<string> files = this.GetFilesForResourceManagerId();

            foreach (var file in files)
            {
                Tracer.Debug("Checking for Recoverable Transactions filename: " + file);
                try
                {
                    using (FileStream recoveryLog = new FileStream(file, FileMode.Open, FileAccess.Read))
                    {
                        Tracer.Debug("Found Recovery Log File: " + file);
                        IFormatter formatter = new BinaryFormatter();
                        result.Add(formatter.Deserialize(recoveryLog) as RecoveryInformation);
                    }
                }
                catch (Exception ex)
                {
                    Tracer.ErrorFormat("Error while opening Recovery file {0} error message: {1}", file, ex.Message);
                }
            }

            return result;
        }

        #endregion
    }
}

