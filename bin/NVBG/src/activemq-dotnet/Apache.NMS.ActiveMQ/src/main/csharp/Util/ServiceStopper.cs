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

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// Simple IStoppable service stopper class.  Can be used to Stop multiple
    /// IStoppable instances without throwing an exception.  Once all services
    /// have been stopped, the first thrown exception can be fired. 
    /// </summary>
    public class ServiceStopper
    {
        private Exception firstException;

        public void Stop(IStoppable service)
        {
            try
            {
                service.Stop();
            }
            catch(Exception e)
            {
                OnException(service, e);
            }
        }

        public void ThrowFirstException()
        {
            if (firstException != null)
            {
                throw firstException;
            }
        }

        private void OnException(object owner, Exception e)
        {
            LogError(owner, e);
            if (firstException == null)
            {
                firstException = e;
            }
        }

        private void LogError(object owner, Exception e)
        {
            Tracer.WarnFormat("Error stopping service:[{0}] exception given: {1}", owner, e);                              
        }
    }
}

