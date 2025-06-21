/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.signals;

import java.util.Map;


/**
 * This is a simple event class that contains the message
 */
public class MessageEvent extends java.util.EventObject
{
    private static final long serialVersionUID = 1;

    private String m_message;
    private Map<String,?> m_map;

    public MessageEvent( Object source, String message, Map<String,?> map )
    {
        super( source );
        m_message = message;
        m_map = map;
    }

    public String toString()
    {
        return m_message;
    }
    
    public Map<String, ?> getMap()
    {
        return m_map;
    }
}
