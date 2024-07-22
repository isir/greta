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
    /// A client uses a QueueBrowser object to look at messages on a queue without removing them.
    ///
    /// The Enumeration method returns a java.util.Enumeration that is used to scan the queue's
    /// messages. It may be an enumeration of the entire content of a queue, or it may contain
    /// only the messages matching a message selector.
    ///
    /// Messages may be arriving and expiring while the scan is done. The NMS API does not
    /// require the content of an enumeration to be a static snapshot of queue content. Whether
    /// these changes are visible or not depends on the NMS provider.
    /// </summary>
    public interface IQueueBrowser : System.Collections.IEnumerable, System.IDisposable
    {
        /// <summary>
        /// Closes the QueueBrowser.
        /// </summary>
        /// <exception cref="Apache.NMS.NMSException">
        /// If NMS Provider fails to close the Browser for some reason.
        /// </exception>
        void Close();

        /// <value>
        /// Gets this queue browser's message selector expression.  If no Message
        /// selector was specified than this method returns null.
        /// </value>
        /// <exception cref="Apache.NMS.NMSException">
        /// If NMS Provider fails to get the Message Selector for some reason.
        /// </exception>
        string MessageSelector { get; }

        /// <value>
        /// Gets the queue associated with this queue browser.
        /// </value>
        /// <exception cref="Apache.NMS.NMSException">
        /// If NMS Provider fails to retrieve the IQueue associated with the Browser
        /// doe to some internal error.
        /// </exception>
        IQueue Queue { get; }

    }
}
