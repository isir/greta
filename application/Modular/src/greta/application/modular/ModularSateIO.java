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
package greta.application.modular;

import greta.application.modular.modules.Style;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModularSateIO {

    private static Locale selectedLanguage = null;

    private static String pathToSave = "./ModularSave.xml";

    private static String markupState = "modular_state";
    private static String markupLocale = "locale";
    private static String attributLocaleSelected = "selected";
    private static String markupLAF = "look_and_feel";
    private static String attributLAFSelected = "selected";
    private static String markupStyle = "style";
    private static String attributStyleSelected = "selected";
    private static String markupPosition = "position";
    private static String attributPositionX = "x";
    private static String attributPositionY = "y";
    private static String markupDimension = "dimension";
    private static String attributDimensionW = "w";
    private static String attributDimensionH = "h";
    private static String markupDivider = "divider";
    private static String attributDividerPosition = "position";
    private static String markupMaximized = "maximized";
    private static String attributMaximizedVertical = "vertical";
    private static String attributMaximizedHorizontal = "horizontal";
    private static String markupLastFile = "last_file";
    private static String attributLastFileLoad = "load";
    private static String attributLastFileName = "file_name";

    public static Locale getSelectedLanguage(){
        if(selectedLanguage==null){
            String savedValue = readLocale();
            if(savedValue!=null){
                Locale l = Modular.getLocaleForLanguage(savedValue);
                if(l==null){
                    setLocaleIfNull(Locale.getDefault());
                }
                else{
                    selectedLanguage = l;
                }
            }
            else{
                setLocaleIfNull(Locale.getDefault());
            }
        }
        return selectedLanguage;
    }

    public static void setSelectedLanguage(Locale l){
        selectedLanguage = l;
        saveLocale(l.getLanguage()+"-"+l.getCountry());
    }


    public static String getLookAndFeel(){
        XMLTree saved = getNode(getTree(), markupLAF);
        if(saved.hasAttribute(attributLAFSelected)){
            return saved.getAttribute(attributLAFSelected);
        }
        return javax.swing.UIManager.getSystemLookAndFeelClassName();
    }

    public static void setLookAndFeel(String lafName){
        XMLTree toSave = getNode(getTree(), markupLAF);
        toSave.setAttribute(attributLAFSelected, lafName);
        saveTree(toSave);
    }

    public static Style.Mapper getStyleMapper(){
        XMLTree saved = getNode(getTree(), markupStyle);
        if(saved.hasAttribute(attributStyleSelected)){
            return Style.getMapper(saved.getAttribute(attributStyleSelected));
        }
        return Style.getMapper();
    }

    public static void setStyleMapper(Style.Mapper mapper){
        XMLTree toSave = getNode(getTree(), markupStyle);
        toSave.setAttribute(attributStyleSelected, mapper.getName());
        saveTree(toSave);
    }

    public static Point getPosition(){
        XMLTree saved = getNode(getTree(), markupPosition);
        if(saved.hasAttribute(attributPositionX) && saved.hasAttribute(attributPositionY)){
            return new Point((int)saved.getAttributeNumber(attributPositionX),(int)saved.getAttributeNumber(attributPositionY));
        }
        return null;
    }

    public static void setPosition(int x, int y){
        XMLTree toSave = getNode(getTree(), markupPosition);
        toSave.setAttribute(attributPositionX, Integer.toString(x));
        toSave.setAttribute(attributPositionY, Integer.toString(y));
        saveTree(toSave);
    }
    public static void setPosition(Point p){
        setPosition(p.x, p.y);
    }

    public static Dimension getDimension(){
        XMLTree saved = getNode(getTree(), markupDimension);
        if(saved.hasAttribute(attributDimensionW) && saved.hasAttribute(attributDimensionH)){
            return new Dimension((int)saved.getAttributeNumber(attributDimensionW),(int)saved.getAttributeNumber(attributDimensionH));
        }
        return null;
    }

    public static void setDimention(int w, int h){
        XMLTree toSave = getNode(getTree(), markupDimension);
        toSave.setAttribute(attributDimensionW, Integer.toString(w));
        toSave.setAttribute(attributDimensionH, Integer.toString(h));
        saveTree(toSave);
    }

    public static void setDimention(Dimension d){
        setDimention(d.width, d.height);
    }

    public static int getDividerPosition(){
        XMLTree saved = getNode(getTree(), markupDivider);
        if(saved.hasAttribute(attributDividerPosition)){
            return (int)saved.getAttributeNumber(attributDividerPosition);
        }
        return 0;
    }

    public static void setDividerPosition(int pos){
        XMLTree toSave = getNode(getTree(), markupDivider);
        toSave.setAttribute(attributDividerPosition, Integer.toString(pos));
        saveTree(toSave);
    }

    public static boolean isVerticalMaximized(){
        XMLTree saved = getNode(getTree(), markupMaximized);
        if(saved.hasAttribute(attributMaximizedVertical)){
            try{
                return Boolean.parseBoolean(saved.getAttribute(attributMaximizedVertical));
            }
            catch(Exception e){}
        }
        return false;
    }

    public static void setVerticalMaximized(boolean isVert){
        XMLTree toSave = getNode(getTree(), markupMaximized);
        toSave.setAttribute(attributMaximizedVertical, Boolean.toString(isVert));
        saveTree(toSave);
    }

    public static boolean isHorizontalMaximized(){
        XMLTree saved = getNode(getTree(), markupMaximized);
        if(saved.hasAttribute(attributMaximizedHorizontal)){
            try{
                return Boolean.parseBoolean(saved.getAttribute(attributMaximizedHorizontal));
            }
            catch(Exception e){}
        }
        return false;
    }

    public static void setHorizontalMaximized(boolean isHorz){
        XMLTree toSave = getNode(getTree(), markupMaximized);
        toSave.setAttribute(attributMaximizedHorizontal, Boolean.toString(isHorz));
        saveTree(toSave);
    }

    public static boolean isLoadLastFile(){
        XMLTree saved = getNode(getTree(), markupLastFile);
        if(saved.hasAttribute(attributLastFileLoad)){
            try{
                return Boolean.parseBoolean(saved.getAttribute(attributLastFileLoad));
            }
            catch(Exception e){}
        }
        return false;
    }

    public static void setLoadLastFile(boolean isLoad){
        XMLTree toSave = getNode(getTree(), markupLastFile);
        toSave.setAttribute(attributLastFileLoad, Boolean.toString(isLoad));
        saveTree(toSave);
    }

    public static String getLastFile(){
        XMLTree saved = getNode(getTree(), markupLastFile);
        if(saved.hasAttribute(attributLastFileName)){
            if((new File(saved.getAttribute(attributLastFileName))).exists()) {
                return saved.getAttribute(attributLastFileName);
            }
        }
        return null;
    }

    public static void setLastFile(String lastFileName){
        XMLTree toSave = getNode(getTree(), markupLastFile);
        toSave.setAttribute(attributLastFileName, lastFileName);
        saveTree(toSave);
    }

    private static void setLocaleIfNull(Locale l){
        if(selectedLanguage==null){
            selectedLanguage = l;
        }
    }

    private static void saveLocale(String value){
        XMLTree toSave = getNode(getTree(), markupLocale);
        toSave.setAttribute(attributLocaleSelected, value);
        saveTree(toSave);
    }

    private static String readLocale(){
        XMLTree saved = getNode(getTree(), markupLocale);
        if(saved.hasAttribute(attributLocaleSelected)){
            return saved.getAttribute(attributLocaleSelected);
        }
        return null;
    }

    private static XMLTree state = null;

    private static XMLTree getTree(){
        if(state==null){
            XMLParser parser = XML.createParser();
            parser.setValidating(false);
            state = parser.parseFile(pathToSave);
            if(state==null || !(state.isNamed(markupState))){
                state = XML.createTree(markupState);
            }
        }
        return state;
    }

    private static void saveTree(XMLTree tree){
        tree.getRootNode().save(pathToSave);
    }

    private static XMLTree getNode(XMLTree tree, String markupName){
        XMLTree root = tree.getRootNode();
        XMLTree localeNode = root.findNodeCalled(markupName);
        if(localeNode==null){
            localeNode = root.createChild(markupName);
        }
        return localeNode;
    }
}
