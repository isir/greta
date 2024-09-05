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

namespace Apache.NMS.ActiveMQ.Commands
{
	public abstract class ActiveMQTempDestination : ActiveMQDestination
	{
        private Connection connection;

		/// <summary>
		/// Method GetDestinationType
		/// </summary>
		/// <returns>An int</returns>
		public override int GetDestinationType()
		{
			// TODO: Implement this method
			return 0;
		}

		/// <summary>
		/// Method CreateDestination
		/// </summary>
		/// <returns>An ActiveMQDestination</returns>
		/// <param name="name">A  String</param>
		public override ActiveMQDestination CreateDestination(String name)
		{
			// TODO: Implement this method
			return null;
		}

		abstract override public DestinationType DestinationType
		{
			get;
		}

		public const byte ID_ActiveMQTempDestination = 0;

		protected ActiveMQTempDestination() : base()
		{
		}

		protected ActiveMQTempDestination(String name) : base(name)
		{
		}

        public Connection Connection
        {
            get { return this.connection; }
            set { this.connection = value; }
        }

		public override byte GetDataStructureType()
		{
			return ID_ActiveMQTempDestination;
		}

		public override Object Clone()
		{
			// Since we are a derived class use the base's Clone()
			// to perform the shallow copy. Since it is shallow it
			// will include our derived class. Since we are derived,
			// this method is an override.
			ActiveMQTempDestination o = (ActiveMQTempDestination) base.Clone();

			// Now do the deep work required
			// If any new variables are added then this routine will
			// likely need updating

			return o;
		}

        public void Delete()
        {
            if(this.connection != null)
            {
                this.connection.DeleteTemporaryDestination(this);
            }
        }

	}
}

