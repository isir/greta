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
package greta.application.modular.tools;

import greta.application.modular.ModularXMLFile;
import greta.application.modular.modules.Style;
import greta.application.modular.tools.StyleListModel.StyleElement;
import greta.core.util.xml.XMLTree;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author Andre-Marie Pez
 */
public class StyleListModel extends AbstractListModel<StyleElement>{


    ArrayList<StyleElement> styles = new ArrayList<StyleElement>();

    public StyleListModel(){
        styles = new ArrayList<StyleElement>();
        reload();
    }

    public void reload(){
        styles.clear();
        List<XMLTree> stylesXML = ModularXMLFile.getStyles().getChildrenElement();
        styles.ensureCapacity(stylesXML.size());
        for(XMLTree styleXML : stylesXML){
            if(styleXML.isNamed("style")){
                this.styles.add(new StyleElement(styleXML));
            }
        }
        Collections.sort(styles);
    }

    @Override
    public int getSize() {
        return styles.size();
    }

    @Override
    public StyleElement getElementAt(int index) {
        return styles.get(index);
    }

    public StyleElement createStyle(){
        XMLTree styleXML = ModularXMLFile.getStyles().createChild("style");
        StyleElement style = new StyleElement(styleXML);
        styles.add(style);
        Collections.sort(styles);
        int index = styles.indexOf(style);
        fireIntervalAdded(this, index, index);
        return style;
    }

    public void deleteStyle(StyleElement style){
        style.style.getParent().removeChild(style.style);
        int index = styles.indexOf(style);
        styles.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void styleNameChanged(StyleElement style){
//
//        int prev_index = styles.indexOf(style);
//
        Collections.sort(styles);
//
//        int new_index = styles.indexOf(style);
//
//        if(prev_index==new_index){
//            fireContentsChanged(this, new_index, new_index);
//        }
//        else{
//            fireIntervalRemoved(this, prev_index, prev_index);
//            fireIntervalAdded(this, new_index, new_index);
//        }
    }


    public class StyleElement implements Comparable<StyleElement>{

        XMLTree style;
        NameChanger nc;

        private StyleElement(XMLTree style){
            this.style = style;
            updateNameChanger(false);
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int compareTo(StyleElement o) {
            return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
        }

        public void setName(String name){
            nc.ApplyNewName(name);
            styleNameChanged(this);
        }

        public String getName(){
            return style.getAttribute("name");
        }

        public void setColorString(String stringColor){
            style.setAttribute("color", stringColor);
        }

        public void setColor(Color color){
            setColorString(color==null? null : Style.convertColor(color));
        }


        public Color getColor(){
            return Style.convertColor(getColorString());
        }

        public String getColorString(){
            return style.getAttribute("color");
        }

        public void setEdgeColorString(String stringColor){
            style.setAttribute("edge-color", stringColor);
        }

        public boolean isColorSet() {
            return style.hasAttribute("color");
        }

        public void setEdgeColor(Color color) {
            setEdgeColorString(color==null? null : Style.convertColor(color));
        }

        public Color getEdgeColor() {
            return Style.convertColor(getEdgeColorString());
        }

        public String getEdgeColorString() {
            return Style.ensureEdgeColor(getColorString(), style.getAttribute("edge-color"));
        }

        public boolean isEdgeColorSet(){
            return style.hasAttribute("edge-color");
        }

        public String getStart() {
            String start = style.getAttribute("edge-start");
            if(start.isEmpty()){
                start = "none";
            }
            return start;
        }

        public void setStart(String bound){
            if(bound.isEmpty() || bound.equals("none")){
                bound = null;
            }
            style.setAttribute("edge-start", bound);
        }

        public String getEnd() {
            String start = style.getAttribute("edge-end");
            if(start.isEmpty()){
                start = "arrow";
            }
            return start;
        }

        public void setEnd(String bound){
            if(bound.isEmpty() || bound.equals("arrow")){
                bound = null;
            }
            style.setAttribute("edge-end", bound);
        }

        public String getDash() {
            return style.getAttribute("edge-dash");
        }

        public void setDash(String dash) {
            style.setAttribute("edge-dash", dash!=null && dash.isEmpty() ? null : dash);
        }

        void updateNameChanger(boolean reference) {
            nc = new NameChanger(style, "name", reference);
        }
    }
}
