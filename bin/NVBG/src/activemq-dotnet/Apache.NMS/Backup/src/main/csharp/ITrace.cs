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
	/// The ITrace interface is used internally by ActiveMQ to log messages.
	/// The client aplication may provide an implementation of ITrace if it wishes to
	/// route messages to a specific destination.
	/// </summary>
	/// <remarks>
	/// <para>
	/// Use the <see cref="Tracer"/> class to register an instance of ITrace as the
	/// active trace destination.
	/// </para>
	/// </remarks>
	public interface ITrace
	{
		void Debug(string message);
		void Info(string message);
		void Warn(string message);
  		void Error(string message);
		void Fatal(string message);

		bool IsDebugEnabled { get; }
		bool IsInfoEnabled { get; }
		bool IsWarnEnabled { get; }
		bool IsErrorEnabled { get; }
		bool IsFatalEnabled { get; }
	}
}


