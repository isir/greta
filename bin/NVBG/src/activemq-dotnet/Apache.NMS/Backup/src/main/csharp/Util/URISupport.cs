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
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Globalization;
using System.Reflection;
using System.Text;
#if !NETCF
using System.Web;
#endif

namespace Apache.NMS.Util
{
	/// <summary>
	/// Class to provide support for Uri query parameters which uses .Net reflection
	/// to identify and set properties.
	/// </summary>
	public class URISupport
	{
		/// <summary>
		/// Given a string that could be a Composite Uri that uses syntax not compatible
		/// with the .NET Uri class such as an ActiveMQ failover Uri formatted as
		/// "failover://(tcp://localhost:61616)", the initial '://' must be changed
		/// to ':(' so that the Uri class doesn't attempt to parse the '(tcp:' as
		/// the Uri's Authority as that is not a valid host name.
		/// </summary>
		/// <param name="uriString">
		/// A string that could be a Composite Uri that uses syntax not compatible
		/// with the .NET Uri class
		/// </param>
		public static Uri CreateCompatibleUri(string uriString)
		{
			string sanitized = uriString.Replace("://(", ":(");
			return new Uri(sanitized);
		}

		/// <summary>
		/// Parse a Uri query string of the form ?x=y&amp;z=0
		/// into a map of name/value pairs.
		/// </summary>
		/// <param name="query">The query string to parse. This string should not contain
		/// Uri escape characters.</param>
		public static StringDictionary ParseQuery(String query)
		{
			StringDictionary map = new StringDictionary();

			if(String.IsNullOrEmpty(query))
			{
				return EmptyMap;
			}

			// strip the initial "?"
			if(query.StartsWith("?"))
			{
				query = query.Substring(1);
			}

			// split the query into parameters
			string[] parameters = query.Split('&');
			foreach(string pair in parameters)
			{
				if(pair.Length > 0)
				{
					string[] nameValue = pair.Split('=');

					if(nameValue.Length != 2)
					{
						throw new NMSException(string.Format("Invalid Uri parameter: {0}", query));
					}

					map[UrlDecode(nameValue[0])] = UrlDecode(nameValue[1]);
				}
			}

			return map;
		}

		public static StringDictionary ParseParameters(Uri uri)
		{
			return (uri.Query == null
					? EmptyMap
					: ParseQuery(StripPrefix(uri.Query, "?")));
		}

		/// <summary>
		/// Sets the public properties of a target object using a string map.
		/// This method uses .Net reflection to identify public properties of
		/// the target object matching the keys from the passed map.
		/// </summary>
		/// <param name="target">The object whose properties will be set.</param>
		/// <param name="map">Map of key/value pairs.</param>
		public static void SetProperties(object target, StringDictionary map)
		{
			Type type = target.GetType();

			foreach(string key in map.Keys)
			{
				PropertyInfo prop = type.GetProperty(key,
														BindingFlags.FlattenHierarchy
														| BindingFlags.Public
														| BindingFlags.Instance
														| BindingFlags.IgnoreCase);

				if(null != prop)
				{
					prop.SetValue(target, Convert.ChangeType(map[key], prop.PropertyType, CultureInfo.InvariantCulture), null);
				}
				else
				{
					FieldInfo field = type.GetField(key,
														BindingFlags.FlattenHierarchy
														| BindingFlags.Public
														| BindingFlags.Instance
														| BindingFlags.IgnoreCase);
					if(null != field)
					{
						field.SetValue(target, Convert.ChangeType(map[key], field.FieldType, CultureInfo.InvariantCulture));
					}
					else
					{
						throw new NMSException(string.Format("No such property or field: {0} on class: {1}", key, target.GetType().Name));
					}
				}
			}
		}

		/// <summary>
		/// Sets the public properties of a target object using a string map.
		/// This method uses .Net reflection to identify public properties of
		/// the target object matching the keys from the passed map.
		/// </summary>
		/// <param name="target">The object whose properties will be set.</param>
		/// <param name="map">Map of key/value pairs.</param>
		/// <param name="prefix">Key value prefix.  This is prepended to the property name
		/// before searching for a matching key value.</param>
		public static void SetProperties(object target, StringDictionary map, string prefix)
		{
			Type type = target.GetType();

			List<String> matches = new List<String>();

			foreach(string key in map.Keys)
			{
				if(key.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
				{
					string bareKey = key.Substring(prefix.Length);
					PropertyInfo prop = type.GetProperty(bareKey,
															BindingFlags.FlattenHierarchy
															| BindingFlags.Public
															| BindingFlags.Instance
															| BindingFlags.IgnoreCase);

					if(null != prop)
					{
						prop.SetValue(target, Convert.ChangeType(map[key], prop.PropertyType, CultureInfo.InvariantCulture), null);
					}
					else
					{
						FieldInfo field = type.GetField(bareKey,
															BindingFlags.FlattenHierarchy
															| BindingFlags.Public
															| BindingFlags.Instance
															| BindingFlags.IgnoreCase);
						if(null != field)
						{
							field.SetValue(target, Convert.ChangeType(map[key], field.FieldType, CultureInfo.InvariantCulture));
						}
						else
						{
							throw new NMSException(string.Format("No such property or field: {0} on class: {1}", bareKey, target.GetType().Name));
						}
					}

					// store for later removal.
					matches.Add(key);
				}
			}

			// Remove all the properties we set so they are used again later.
			foreach(string match in matches)
			{
				map.Remove(match);
			}
		}

		public static StringDictionary GetProperties(StringDictionary props, string prefix)
		{
			if(props == null)
			{
				throw new Exception("Properties Object was null");
			}

			StringDictionary result = new StringDictionary();

			foreach(string key in props.Keys)
			{
				if(key.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
				{
					string bareKey = key.Substring(prefix.Length);
					String value = props[key];
					result[bareKey] = value;
				}
			}

			return result;
		}

		public static StringDictionary ExtractProperties(StringDictionary props, string prefix)
		{

			if(props == null)
			{
				throw new Exception("Properties Object was null");
			}

			StringDictionary result = new StringDictionary();
			List<String> matches = new List<String>();

			foreach(string key in props.Keys)
			{
				if(key.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
				{
					String value = props[key];
					result[key] = value;
					matches.Add(key);
				}
			}

			foreach(string match in matches)
			{
				props.Remove(match);
			}

			return result;
		}

		public static String UrlDecode(String s)
		{
#if !NETCF
			return HttpUtility.UrlDecode(s);
#else
            return Uri.UnescapeDataString(s);
#endif
		}

		public static String UrlEncode(String s)
		{
#if !NETCF
			return HttpUtility.UrlEncode(s);
#else
            return Uri.EscapeUriString(s);
#endif
		}

		public static String CreateQueryString(StringDictionary options)
		{
			if(options != null && options.Count > 0)
			{
				StringBuilder rc = new StringBuilder();
				bool first = true;

				foreach(String key in options.Keys)
				{
					string value = options[key];

					if(first)
					{
						first = false;
					}
					else
					{
						rc.Append("&");
					}

					rc.Append(UrlEncode(key));
					rc.Append("=");
					rc.Append(UrlEncode(value));
				}

				return rc.ToString();
			}
			else
			{
				return "";
			}
		}

		public static Uri CreateRemainingUri(Uri originalUri, StringDictionary parameters)
		{
			string s = CreateQueryString(parameters);

			if(String.IsNullOrEmpty(s))
			{
				s = null;
			}

			return CreateUriWithQuery(originalUri, s);
		}

		public class CompositeData
		{
			private String host;
			private String scheme;
			private String path;
			private Uri[] components;
			private StringDictionary parameters;
			private String fragment;

			public Uri[] Components
			{
				get { return components; }
				set { components = value; }
			}

			public String Fragment
			{
				get { return fragment; }
				set { fragment = value; }
			}

			public StringDictionary Parameters
			{
				get { return parameters; }
				set { parameters = value; }
			}

			public String Scheme
			{
				get { return scheme; }
				set { scheme = value; }
			}

			public String Path
			{
				get { return path; }
				set { path = value; }
			}

			public String Host
			{
				get { return host; }
				set { host = value; }
			}

			public Uri toUri()
			{
				StringBuilder sb = new StringBuilder();
				if(scheme != null)
				{
					sb.Append(scheme);
					sb.Append(':');
				}

				if(!string.IsNullOrEmpty(host))
				{
					sb.Append(host);
				}
				else
				{
					sb.Append('(');
					for(int i = 0; i < components.Length; i++)
					{
						if(i != 0)
						{
							sb.Append(',');
						}
						sb.Append(components[i].ToString());
					}
					sb.Append(')');
				}

				if(path != null)
				{
					sb.Append('/');
					sb.Append(path);
				}

				if(parameters.Count != 0)
				{
					sb.Append("?");
					sb.Append(CreateQueryString(parameters));
				}

				if(fragment != null)
				{
					sb.Append("#");
					sb.Append(fragment);
				}

				return new Uri(sb.ToString());
			}
		}

		public static String StripPrefix(String value, String prefix)
		{
			if(value.StartsWith(prefix, StringComparison.InvariantCultureIgnoreCase))
			{
				return value.Substring(prefix.Length);
			}

			return value;
		}

		public static Uri CreateUriWithQuery(Uri uri, string query)
		{
			if(!String.IsNullOrEmpty(query) && !query.StartsWith("?"))
			{
				query = "?" + query;
			}

			if(String.IsNullOrEmpty(uri.Query))
			{
				return new Uri(uri.OriginalString + query);
			}
			else
			{
				string originalUri = uri.OriginalString;

				int queryDelimPos = originalUri.LastIndexOf('?');
				int compositeDelimPos = originalUri.LastIndexOf(')');

				if(queryDelimPos <= compositeDelimPos)
				{
					// No Query or the Query is part of an inner Composite.
					return new Uri(originalUri + query);
				}
				else
				{
					// Outer Uri has a Query or not a Composite Uri with a Query
					string strippedUri = originalUri.Substring(0, queryDelimPos);
					return new Uri(strippedUri + query);
				}
			}
		}

		public static Uri RemoveQuery(Uri original)
		{
			return CreateUriWithQuery(original, null);
		}

		public static CompositeData ParseComposite(Uri uri)
		{
			CompositeData rc = new CompositeData();
			rc.Scheme = uri.Scheme;

			// Start with original URI
			//String ssp = uri.Authority + uri.PathAndQuery;
			String ssp = uri.OriginalString;

			ssp = StripPrefix(ssp, rc.Scheme + ":");
			ssp = StripPrefix(ssp, "//");

			int lastPoundPos = ssp.LastIndexOf("#");
			int lastParendPos = ssp.LastIndexOf(")");

			// Only include a Fragment that's outside any Composte sections.
			if(lastPoundPos > lastParendPos)
			{
				rc.Fragment = ssp.Substring(lastPoundPos);
				ssp = ssp.Substring(0, lastPoundPos);
			}

			// Ensure any embedded URIs don't have malformed authority's by changing
			// them from '://(' which would cause the .NET Uri class to attempt to validate
			// the authority as a hostname with, ':(' which is valid.
			ssp = ssp.Replace("://(", ":(");

			// Handle the composite components
			ParseComposite(uri, rc, ssp);
			return rc;
		}

		/// <summary>
		/// </summary>
		/// <param name="uri"></param>
		/// <param name="rc"></param>
		/// <param name="ssp"></param>
		private static void ParseComposite(Uri uri, CompositeData rc, String ssp)
		{
			String componentString;
			String parms;

			if(!CheckParenthesis(ssp))
			{
				throw new NMSException(uri.ToString() + ": Not a matching number of '(' and ')' parenthesis");
			}

			int p;
			int intialParen = ssp.IndexOf("(");

			if(intialParen >= 0)
			{
				rc.Host = ssp.Substring(0, intialParen);
				p = rc.Host.IndexOf("/");
				if(p >= 0)
				{
					rc.Path = rc.Host.Substring(p);
					rc.Host = rc.Host.Substring(0, p);
				}

				p = ssp.LastIndexOf(")");
				int start = intialParen + 1;
				int len = p - start;
				componentString = ssp.Substring(start, len);
				parms = ssp.Substring(p + 1).Trim();
			}
			else
			{
				componentString = ssp;
				parms = "";
			}

			String[] components = SplitComponents(componentString);
			rc.Components = new Uri[components.Length];
			for(int i = 0; i < components.Length; i++)
			{
				rc.Components[i] = new Uri(components[i].Trim());
			}

			p = parms.IndexOf("?");
			if(p >= 0)
			{
				if(p > 0)
				{
					rc.Path = StripPrefix(parms.Substring(0, p), "/");
				}

				rc.Parameters = ParseQuery(parms.Substring(p + 1));
			}
			else
			{
				if(parms.Length > 0)
				{
					rc.Path = StripPrefix(parms, "/");
				}

				rc.Parameters = EmptyMap;
			}
		}

		private static StringDictionary EmptyMap
		{
			get { return new StringDictionary(); }
		}

		/// <summary>
		/// </summary>
		/// <param name="componentString"></param>
		private static String[] SplitComponents(String componentString)
		{
			ArrayList l = new ArrayList();

			int last = 0;
			int depth = 0;
			char[] chars = componentString.ToCharArray();
			for(int i = 0; i < chars.Length; i++)
			{
				switch(chars[i])
				{
				case '(':
					depth++;
					break;

				case ')':
					depth--;
					break;

				case ',':
					if(depth == 0)
					{
						String s = componentString.Substring(last, i - last);
						l.Add(s);
						last = i + 1;
					}
					break;

				default:
					break;
				}
			}

			String ending = componentString.Substring(last);
			if(ending.Length != 0)
			{
				l.Add(ending);
			}

			String[] rc = new String[l.Count];
			l.CopyTo(rc);
			return rc;
		}

		public static bool CheckParenthesis(String str)
		{
			bool result = true;

			if(str != null)
			{
				int open = 0;
				int closed = 0;

				int i = 0;
				while((i = str.IndexOf('(', i)) >= 0)
				{
					i++;
					open++;
				}

				i = 0;
				while((i = str.IndexOf(')', i)) >= 0)
				{
					i++;
					closed++;
				}

				result = (open == closed);
			}

			return result;
		}
	}
}
