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

namespace Apache.NMS.Util
{
	public class DateUtils
	{
		/// <summary>
		/// The start of the Windows epoch
		/// </summary>
		public static readonly DateTime windowsEpoch = new DateTime(1601, 1, 1, 0, 0, 0, 0);
		/// <summary>
		/// The start of the Java epoch
		/// </summary>
		public static readonly DateTime javaEpoch = new DateTime(1970, 1, 1, 0, 0, 0, 0);
		
		/// <summary>
		/// The difference between the Windows epoch and the Java epoch
		/// in milliseconds.
		/// </summary>
		public static readonly long epochDiff; /* = 1164447360000L; */

		static DateUtils()
		{
			epochDiff = (javaEpoch.ToFileTimeUtc() - windowsEpoch.ToFileTimeUtc())
							/ TimeSpan.TicksPerMillisecond;
		}

		public static long ToJavaTime(DateTime dateTime)
		{
			return (dateTime.ToFileTime() / TimeSpan.TicksPerMillisecond) - epochDiff;
		}

		public static DateTime ToDateTime(long javaTime)
		{
			return DateTime.FromFileTime((javaTime + epochDiff) * TimeSpan.TicksPerMillisecond);
		}

		public static long ToJavaTimeUtc(DateTime dateTime)
		{
			return (dateTime.ToFileTimeUtc() / TimeSpan.TicksPerMillisecond) - epochDiff;
		}

		public static DateTime ToDateTimeUtc(long javaTime)
		{
			return DateTime.FromFileTimeUtc((javaTime + epochDiff) * TimeSpan.TicksPerMillisecond);
		}
	}
}
