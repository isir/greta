/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.application.modular.modules;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class Connector {

    public static List<Connector> connectors = new ArrayList<Connector>();
    private String id;
    List<Object> usedWith;
    private Class in;
    private Class out;
    private Method connect;
    private Method disconnect;
    private boolean connectInToOut;
    private boolean disconnectInToOut;
    private boolean connectToNull;
    private boolean disconnectToNull;
    private String style;

    private Connector(String id, boolean unique, Class in, Class out, Method connect, boolean connectMode, boolean connectToNull, Method disconnect, boolean disconnectMode, boolean disconnectToNull, String style) {
        this.id = id;
        this.usedWith = unique? new ArrayList<Object>() : null;
        this.in = in;
        this.out = out;
        this.connect = connect;
        this.disconnect = disconnect;
        this.connectInToOut = connectMode;
        this.disconnectInToOut = disconnectMode;
        this.connectToNull = connectToNull;
        this.disconnectToNull = disconnectToNull;
        this.style = style;
    }

    public static List<Connector> findConnectors(Object in, Object out) {
        List<Connector> result = new ArrayList<Connector>();
        for (Connector connector : connectors) {
            if (connector.isIn(in) && connector.isOut(out)) {
                result.add(connector);
            }
        }
        return result;
    }

    public static Connector findConnector(String id) {
        for (Connector connector : connectors) {
            if (connector.id.equals(id)) {
                return connector;
            }
        }
        return null;
    }

    public static void createConnector(String id, boolean unique, Class in, Class out, Method connect, boolean connectMode, boolean connectToNull, Method disconnect, boolean disconnectMode, boolean disconnectToNull, String style) {
        connectors.add(new Connector(id, unique, in, out, connect, connectMode, connectToNull, disconnect, disconnectMode, disconnectToNull, style));
    }

    public static boolean isInput(Object in) {
        for (Connector connector : connectors) {
            if (connector.isIn(in)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOutput(Object out) {
        for (Connector connector : connectors) {
            if (connector.isOut(out)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasConnector(Object in, Object out) {
        for (Connector connector : connectors) {
            if (connector.isIn(in) && connector.isOut(out)) {
                return true;
            }
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getStyle() {
        return style;
    }

    public boolean isUnique(){
        return usedWith != null;
    }

    public Class getInClass(){
        return in;
    }

    public boolean isIn(Object i) {
        return in.isInstance(i) && (usedWith==null || !usedWith.contains(i));
    }

    public Class getOutClass(){
        return out;
    }

    public boolean isOut(Object o) {
        return out.isInstance(o);
    }

    public boolean connect(Object i, Object o) {
        if (connect != null) {
            try {
                /*if(i instanceof TTSController)
                    System.out.println("debug");*/
                if (connectInToOut) {
                    connect.invoke(i, new Object[]{
                                connectToNull ? null : o
                            });
                } else {
                    connect.invoke(o, new Object[]{
                                connectToNull ? null : i
                            });
                }
                if(usedWith!=null) {
                    usedWith.add(i);
                }
                return true;
            } catch (Exception ex) {
                System.out.println(String.format("connection failled between %s and %s: %s",
                        in.getCanonicalName(), out.getCanonicalName(), ex.getLocalizedMessage()));
            }
        }
        return false;
    }

    public boolean disconnect(Object i, Object o) {
        if (disconnect != null) {
            try {
                if (disconnectInToOut) {
                    disconnect.invoke(i, new Object[]{
                                disconnectToNull ? null : o
                            });
                } else {
                    disconnect.invoke(o, new Object[]{
                                disconnectToNull ? null : i
                            });
                }
                if(usedWith!=null) {
                    usedWith.remove(i);
                }
                return true;
            } catch (Exception ex) {
                System.out.println("disconnection failled between "
                        + in.getCanonicalName() + " and "
                        + out.getCanonicalName());
            }
        }
        return false;
    }

    String getConnectionCode(Module in, Module out) {
        if (connectInToOut) {
            return in.getObjectVariableName()+"."+connect.getName()+"("+(connectToNull ? "null" : out.getObjectVariableName())+");\n";
        }
        else{
            return out.getObjectVariableName()+"."+connect.getName()+"("+(connectToNull ? "null" : in.getObjectVariableName())+");\n";
        }
    }
}
