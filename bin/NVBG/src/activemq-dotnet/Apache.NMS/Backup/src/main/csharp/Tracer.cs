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
	public sealed class Tracer
	{
		private static ITrace s_trace = null;

		// prevent instantiation of this class. All methods are static.
		private Tracer()
		{
		}

		public static ITrace Trace
		{
			get { return s_trace; }
			set { s_trace = value; }
		}

		public static bool IsDebugEnabled
		{
			get { return s_trace != null && s_trace.IsDebugEnabled; }
		}

		public static bool IsInfoEnabled
		{
			get { return s_trace != null && s_trace.IsInfoEnabled; }
		}

		public static bool IsWarnEnabled
		{
			get { return s_trace != null && s_trace.IsWarnEnabled; }
		}

		public static bool IsErrorEnabled
		{
			get { return s_trace != null && s_trace.IsErrorEnabled; }
		}

		public static bool IsFatalEnabled
		{
			get { return s_trace != null && s_trace.IsFatalEnabled; }
		}

		public static void Debug(object message)
		{
			if(IsDebugEnabled)
			{
				s_trace.Debug(message.ToString());
			}
		}

		public static void DebugFormat(string format, params object[] args)
		{
			if(IsDebugEnabled)
			{
				s_trace.Debug(string.Format(format, args));
			}
		}

		public static void Info(object message)
		{
			if(IsInfoEnabled)
			{
				s_trace.Info(message.ToString());
			}
		}

		public static void InfoFormat(string format, params object[] args)
		{
			if(IsInfoEnabled)
			{
				s_trace.Info(string.Format(format, args));
			}
		}

		public static void Warn(object message)
		{
			if(IsWarnEnabled)
			{
				s_trace.Warn(message.ToString());
			}
		}

		public static void WarnFormat(string format, params object[] args)
		{
			if(IsWarnEnabled)
			{
				s_trace.Warn(string.Format(format, args));
			}
		}

		public static void Error(object message)
		{
			if(IsErrorEnabled)
			{
				s_trace.Error(message.ToString());
			}
		}

		public static void ErrorFormat(string format, params object[] args)
		{
			if(IsErrorEnabled)
			{
				s_trace.Error(string.Format(format, args));
			}
		}

		public static void Fatal(object message)
		{
			if(IsFatalEnabled)
			{
				s_trace.Fatal(message.ToString());
			}
		}

		public static void FatalFormat(string format, params object[] args)
		{
			if(IsFatalEnabled)
			{
				s_trace.Fatal(string.Format(format, args));
			}
		}
	}
}
