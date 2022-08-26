package greta.core.signals;

/*
    This file is part of VHMsg written by Edward Fast at 
    University of Southern California's Institute for Creative Technologies.
    http://www.ict.usc.edu
    Copyright 2008 Edward Fast, University of Southern California

    VHMsg is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    VHMsg is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with VHMsg.  If not, see <http://www.gnu.org/licenses/>.
*/



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
