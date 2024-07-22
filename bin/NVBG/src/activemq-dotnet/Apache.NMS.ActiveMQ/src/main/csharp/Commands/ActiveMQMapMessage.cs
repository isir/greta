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

using System.IO;
using Apache.NMS.ActiveMQ.OpenWire;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Commands
{
	public class ActiveMQMapMessage : ActiveMQMessage, IMapMessage
	{
		public const byte ID_ACTIVEMQMAPMESSAGE = 25;

		private PrimitiveMap body;
        private PrimitiveMapInterceptor typeConverter;

		public override byte GetDataStructureType()
		{
			return ID_ACTIVEMQMAPMESSAGE;
		}

        public override void ClearBody()
        {
            this.body = null;
            this.typeConverter = null;
            base.ClearBody();
        }

        public override bool ReadOnlyBody 
        {
            get 
            {
                return base.ReadOnlyBody;
            }
            
            set 
            {
                if(this.typeConverter != null)
                {
                    this.typeConverter.ReadOnly = true;
                }
                
                base.ReadOnlyBody = value;
            }
        }

        
		public IPrimitiveMap Body
		{
			get
			{
				if(this.body == null)
				{					
					if(this.Content != null && this.Content.Length > 0)
					{
	                    MemoryStream buffer = new MemoryStream(this.Content);
						Stream source = buffer;

						if(this.Connection != null && this.Compressed)
						{
                            source = this.Connection.CompressionPolicy.CreateDecompressionStream(source);
						}

						this.body = PrimitiveMap.Unmarshal(source);
					}
					else
					{
						this.body = new PrimitiveMap();
					}

	                this.typeConverter = new PrimitiveMapInterceptor(this, this.body);
				}

                return this.typeConverter;
			}
		}

		public override void BeforeMarshall(OpenWireFormat wireFormat)
		{
            if (this.Content == null && this.body != null && this.body.Count > 0)
			{
				MemoryStream buffer = new MemoryStream();
				Stream target = buffer;

                if(this.Connection != null && this.Connection.UseCompression)
                {
                    target = this.Connection.CompressionPolicy.CreateCompressionStream(target);
					this.Compressed = true;
                }
                
				this.body.Marshal(target);
				target.Close();
                
				this.Content = buffer.ToArray();
			}

			Tracer.Debug("BeforeMarshalling, content is: " + Content);

			base.BeforeMarshall(wireFormat);
		}
	}
}
