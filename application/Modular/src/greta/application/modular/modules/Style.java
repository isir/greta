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

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class Style {

    private static List<Style> knownStyles;

    private static Mapper currentStyleMapper;
    private static List<Mapper> allStyleMapper;
    private Map<String, Object> styleMap;
    private String styleName;
    public static String DEFAULT_COLOR = "0xFDFDFD";

    static {
        knownStyles = new ArrayList<Style>();
        allStyleMapper = new ArrayList<Mapper>(3);
        allStyleMapper.add(new Basic());
        allStyleMapper.add(new Original());
        allStyleMapper.add(new Soft());
        currentStyleMapper = allStyleMapper.get(2);//Soft
    }

    public static List<Style> getAllStyles() {
        return knownStyles;
    }


    public static Mapper getMapper(String targetMapper){
        for(Mapper m : allStyleMapper){
            if(m.getName().equals(targetMapper)){
                return m;
            }
        }
        return currentStyleMapper;
    }


    public static Mapper getMapper(){
        return currentStyleMapper;
    }

    public static void setMapper(Mapper mapper){
        currentStyleMapper = mapper;
    }

    public static List<Mapper> getMappers(){
        return allStyleMapper;
    }

    public static Map<String, Object> getCurrentDefaultEdgesMap() {
        return currentStyleMapper.defaultEdgeMap;
    }

    public static Map<String, Object> getCurrentDefautlVertexMap() {
        return currentStyleMapper.defaultVertexMap;
    }

    public static String ensureEdgeColor(String fillColor, String edgeColor){
        if(edgeColor==null || edgeColor.isEmpty()){
            edgeColor = convertColor(convertColor(fillColor).darker().darker());
        }
        return edgeColor;
    }

    public static void createNewStyle(String styleName, String fillColor, String edgeColor, String dashPattern, String startForm, String endForm, String vAlign) {
        edgeColor = ensureEdgeColor(fillColor, edgeColor);
        if(dashPattern==null){
            dashPattern = "";
        }
        if(startForm==null || startForm.isEmpty()){
            startForm = "none";
        }
        if(endForm==null || endForm.isEmpty()){
            endForm = "arrow";
        }
        if(vAlign==null || vAlign.isEmpty()){
            vAlign = mxConstants.ALIGN_CENTER;
        }
        for(Mapper mapper : allStyleMapper){
            knownStyles.addAll(Arrays.asList(mapper.createNewStyle(styleName, fillColor, edgeColor, dashPattern, startForm, endForm, vAlign)));
        }
    }

    public static String convertDashPatern(String dash){
        double factor = 5;
        int spaceCount = 0;
        int dashSize = 0;
        String result = "";
        int finalspace = 0;
        for(char c : dash.toCharArray()){
            if(c==' '){
                spaceCount++;
                if(dashSize!=0){
                    if(!result.isEmpty()){
                        result+=" ";
                    }
                    result+=(int)(dashSize*factor+0.5);
                    dashSize=0;
                }
            }
            else{
                dashSize++;
                if(spaceCount!=0){
                    if(result.isEmpty()){
                        finalspace = spaceCount;
                    }
                    else{
                        result+=" "+(int)(spaceCount*factor+0.5);
                    }
                    spaceCount=0;
                }
            }
        }
        if(dashSize!=0){
            if(!result.isEmpty()){
                result+=" ";
            }
            result+=(int)(dashSize*factor+0.5);
            if(finalspace==0){
                finalspace = 1;
            }
        }
        if( (!result.isEmpty()) && spaceCount+finalspace!=0){
            result+=" "+(int)((spaceCount+finalspace)*factor+0.5);
        }
        return result;
    }

    public static String convertForm(String form){
        if("oval".equalsIgnoreCase(form)) {
            return mxConstants.ARROW_OVAL;
        }
        if("diamond".equalsIgnoreCase(form)){
            return mxConstants.ARROW_DIAMOND;
        }
        if("arrow".equalsIgnoreCase(form)){
            return mxConstants.ARROW_CLASSIC;
        }
        return "";
    }

    public Style(String name, Map<String, Object> attributes) {
        styleMap = attributes;
        styleName = name;
    }

    public String getName() {
        return styleName;
    }

    public Map<String, Object> getMap() {
        return styleMap;
    }

    public static String convertColor(Color c){
        return "0x"+Integer.toHexString(c.getRGB())
                .substring(2);//remove the alpha component
    }


    public static Color convertColor(String c){
        return Color.decode(c==null || c.isEmpty() ? DEFAULT_COLOR : c);
    }

    public static Color scaleColor(Color c, double factor) {
        return new Color(Math.max((int)(c.getRed()  *factor), 0),
                         Math.max((int)(c.getGreen()*factor), 0),
                         Math.max((int)(c.getBlue() *factor), 0),
                         c.getAlpha());
    }

    public static String scaleColor(String c, double factor) {
        return convertColor(scaleColor(convertColor(c), factor));
    }

    public static abstract class Mapper{
        String name;
        HashMap<String, Object> defaultEdgeMap;
        HashMap<String, Object> defaultVertexMap;

        Mapper(String name){
            this.name = name;
            defaultEdgeMap = new HashMap<String, Object>();
            defaultVertexMap = new HashMap<String, Object>();

            defaultVertexMap.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE); // rectangle
            defaultVertexMap.put(mxConstants.STYLE_WHITE_SPACE, "wrap"); // wrap the text
            defaultVertexMap.put(mxConstants.STYLE_OVERFLOW, "hidden"); // hide the overflowing text
            defaultVertexMap.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_SOUTH);
            defaultVertexMap.put(mxConstants.STYLE_STROKEWIDTH, 1);
            defaultVertexMap.put(mxConstants.STYLE_FONTSTYLE, 0);
            defaultVertexMap.put(mxConstants.STYLE_OPACITY, 100);
            defaultVertexMap.put(mxConstants.STYLE_SHADOW, false); // show shadow


            defaultEdgeMap.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_SIDETOSIDE);
            defaultEdgeMap.put(mxConstants.STYLE_EDITABLE, false);
            defaultEdgeMap.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
            defaultEdgeMap.put(mxConstants.STYLE_STROKEWIDTH, 1);
            defaultEdgeMap.put(mxConstants.STYLE_DASHED, false);

            fillMaps();
        }

        public String getName(){
            return name;
        }
        private void fillMaps(){
            fillDefaultEdgeMap();
            fillDefaultVertexMap();
        }

        public void setupModule(mxGraph graph, Module m){
            putStyle(graph, m.getCell(), getVertexStyle(m.getInfo().style), false);
        }

        public void highLightModule(mxGraph graph, Module m){
            putStyle(graph, m.getCell(), getVertexHighLightStyle(m.getInfo().style), false);
        }

        public void greyModule(mxGraph graph, Module m){
            putStyle(graph, m.getCell(), getVertexGreyStyle(m.getInfo().style), false);
        }

        public void unGreyModule(mxGraph graph, Module m){
            setupModule(graph, m);
        }

        public void unHighLightModule(mxGraph graph, Module m){
            setupModule(graph, m);
        }

        public void checkConnectionStyle(mxGraph graph, Connection connection){
            checkConnectionStyle(graph, connection, false);
        }

        public void checkConnectionStyle(mxGraph graph, Connection connection, boolean force){
            if(connection.isConnected()){
                putStyle(graph, connection.getCell(), getEdgeStyle(connection.getConnector().getStyle()), force);
            }
            else{
                putStyle(graph, connection.getCell(), getEdgeBrokenStyle(connection.getConnector().getStyle()), force);
            }
        }

        private void putStyle(mxGraph graph, mxCell cell, String styleName, boolean force){
            if(cell.getStyle()==null || force || ! cell.getStyle().equals(styleName)){
                graph.getModel().setStyle(cell, styleName);
            }
        }

        protected abstract void fillDefaultEdgeMap();
        protected abstract void fillDefaultVertexMap();
        protected abstract Style[] createNewStyle(String name, String vertexColor, String edgeColor, String dashPattern, String startForm, String endForm, String vAlign);
        public abstract Color getHighLightColor();
        public abstract String getVertexStyle(String styleName);
        public abstract String getVertexHighLightStyle(String styleName);
        public abstract String getVertexGreyStyle(String styleName);
        public abstract String getEdgeStyle(String styleName);
        public abstract String getEdgeBrokenStyle(String styleName);

        protected Style[] asArray(Style... styles){
            return styles;
        }

    }


    public static class Basic extends Mapper{
        public Basic(){
            super("Basic");
        }

        @Override
        protected void fillDefaultEdgeMap() {
            defaultEdgeMap.put(mxConstants.STYLE_STROKECOLOR, "black");
            defaultEdgeMap.put(mxConstants.STYLE_FONTCOLOR, "black"); // font color : black
            defaultEdgeMap.put(mxConstants.STYLE_ROUNDED, false);
            defaultEdgeMap.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);

            Style style = new Style("[Basic][edge]", defaultEdgeMap);
            knownStyles.add(style);

            //create broken edge style
            HashMap<String, Object> brokenMap = new HashMap<String, Object>(defaultEdgeMap);
            brokenMap.put(mxConstants.STYLE_STROKECOLOR, "0xAAAAAA");
            brokenMap.put(mxConstants.STYLE_FONTCOLOR, "0xAAAAAA");
            Style broken = new Style("[Basic][edge][broken]", brokenMap);
            knownStyles.add(broken);
        }

        @Override
        protected void fillDefaultVertexMap() {
            defaultVertexMap.put(mxConstants.STYLE_ROUNDED, false); // rounded angles
            defaultVertexMap.put(mxConstants.STYLE_FONTCOLOR, "black"); // font color : black
            defaultVertexMap.put(mxConstants.STYLE_STROKECOLOR, "black"); // border color : black
            defaultVertexMap.put(mxConstants.STYLE_FILLCOLOR, "white"); // fill color : white
            defaultVertexMap.put(mxConstants.STYLE_GRADIENTCOLOR, "white");
            Style style = new Style("[Basic]", defaultVertexMap);
            knownStyles.add(style);
        }

        @Override
        protected Style[] createNewStyle(String name, String vertexColor, String edgeColor, String dashPattern, String startForm, String endForm, String vAlign) {
            return asArray();
        }

        @Override
        public Color getHighLightColor() {
            return Color.DARK_GRAY;
        }

        @Override
        public String getVertexStyle(String styleName) {
            return "[Basic]";
        }

        @Override
        public String getVertexHighLightStyle(String styleName) {
            return "[Basic]";
        }

        @Override
        public String getEdgeStyle(String styleName) {
            return "[Basic][edge]";
        }

        @Override
        public String getEdgeBrokenStyle(String styleName) {
            return "[Basic][edge][broken]";
        }

        @Override
        public String getVertexGreyStyle(String styleName) {
            return "[Basic][broken]";
        }

    }

    public static class Original extends Mapper{

        public Original(){
            super("Original");
        }

        @Override
        protected void fillDefaultEdgeMap() {
            defaultEdgeMap.put(mxConstants.STYLE_STROKECOLOR, "black");
            defaultEdgeMap.put(mxConstants.STYLE_FONTCOLOR, "black");
            defaultEdgeMap.put(mxConstants.STYLE_ROUNDED, true);

            Style style = new Style(getEdgeStyle(""), defaultEdgeMap);
            knownStyles.add(style);

            //create broken edge style
            HashMap<String, Object> brokenMap = new HashMap<String, Object>(defaultEdgeMap);
            brokenMap.put(mxConstants.STYLE_STROKECOLOR, "0xAAAAAA");
            brokenMap.put(mxConstants.STYLE_FONTCOLOR, "0xAAAAAA");
            Style broken = new Style(getEdgeBrokenStyle(""), brokenMap);
            knownStyles.add(broken);
        }

        @Override
        protected void fillDefaultVertexMap() {
            defaultVertexMap.put(mxConstants.STYLE_ROUNDED, true); // rounded angles
            defaultVertexMap.put(mxConstants.STYLE_FONTCOLOR, "black"); // font color : black
            defaultVertexMap.put(mxConstants.STYLE_STROKECOLOR, "black"); // border color : black
            defaultVertexMap.put(mxConstants.STYLE_FILLCOLOR, "white"); // fill color : white
            defaultVertexMap.put(mxConstants.STYLE_GRADIENTCOLOR, "white");
            defaultVertexMap.put(mxConstants.STYLE_SHADOW, true); // show shadow
        }

        @Override
        protected Style[] createNewStyle(String name, String vertexColor, String edgeColor, String dashPattern, String startForm, String endForm, String vAlign) {
            HashMap<String, Object> newStyleMap = new HashMap<String, Object>(defaultVertexMap);
            newStyleMap.put(mxConstants.STYLE_FILLCOLOR, vertexColor);
            newStyleMap.put(mxConstants.STYLE_GRADIENTCOLOR, vertexColor);
            newStyleMap.put(mxConstants.STYLE_VERTICAL_ALIGN, vAlign);
            Style style = new Style(getVertexStyle(name), newStyleMap);
            return asArray(style);
        }

        @Override
        public Color getHighLightColor() {
            return Color.green;
        }

        @Override
        public String getVertexStyle(String styleName) {
           return "[Original]"+styleName;
        }

        @Override
        public String getVertexHighLightStyle(String styleName) {
            return "[Original]"+styleName;
        }

        @Override
        public String getEdgeStyle(String styleName) {
            return "[Original][edge]";
        }

        @Override
        public String getEdgeBrokenStyle(String styleName) {
            return "[Original][edge][broken]";
        }

        @Override
        public String getVertexGreyStyle(String styleName) {
            return "[Original][broken]"+styleName;
        }
    }

    public static class Soft extends Mapper{
        Color invisible = new Color(0,0,0,0);
        public Soft(){
            super("Soft");
        }

        @Override
        protected void fillDefaultEdgeMap() {
            defaultEdgeMap.put(mxConstants.STYLE_STROKECOLOR, "black");
            defaultEdgeMap.put(mxConstants.STYLE_FONTCOLOR, "black");
            defaultEdgeMap.put(mxConstants.STYLE_ROUNDED, true);
        }

        @Override
        protected void fillDefaultVertexMap() {
            defaultVertexMap.put(mxConstants.STYLE_ROUNDED, true); // rounded angles
            defaultVertexMap.put(mxConstants.STYLE_FILLCOLOR, "white"); // fill color : white
            defaultVertexMap.put(mxConstants.STYLE_FONTCOLOR, convertColor(Color.white.darker().darker()));
            defaultVertexMap.put(mxConstants.STYLE_STROKECOLOR, convertColor(Color.white.darker().darker().darker()));
            defaultVertexMap.put(mxConstants.STYLE_OPACITY, 90);
            defaultVertexMap.put(mxConstants.STYLE_GRADIENTCOLOR, convertColor(Color.white.darker()));
            defaultVertexMap.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
            defaultVertexMap.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        }

        @Override
        protected Style[] createNewStyle(String name, String vertexColor, String edgeColor, String dashPattern, String startForm, String endForm, String vAlign) {
            HashMap<String, Object> styleMap = new HashMap<String, Object>(defaultVertexMap);
            HashMap<String, Object> styleHighLightMap = new HashMap<String, Object>(defaultVertexMap);
            HashMap<String, Object> styleGreyedHighLightMap = new HashMap<String, Object>(defaultVertexMap);
            HashMap<String, Object> edgeMap = new HashMap<String, Object>(defaultEdgeMap);
            HashMap<String, Object> brokenEdgeMap = new HashMap<String, Object>(defaultEdgeMap);

            //vertex:
            styleMap.put(mxConstants.STYLE_FONTCOLOR, scaleColor(vertexColor, 0.3));
            styleMap.put(mxConstants.STYLE_STROKECOLOR, scaleColor(vertexColor, 0.2));
            styleMap.put(mxConstants.STYLE_FILLCOLOR, vertexColor);
            styleMap.put(mxConstants.STYLE_GRADIENTCOLOR, scaleColor(vertexColor, 0.75));

            styleHighLightMap.put(mxConstants.STYLE_FONTCOLOR, scaleColor(vertexColor, 0.7));
            styleHighLightMap.put(mxConstants.STYLE_FILLCOLOR, "0xFFFFFF");
            styleHighLightMap.put(mxConstants.STYLE_GRADIENTCOLOR, vertexColor);
            styleHighLightMap.put(mxConstants.STYLE_STROKECOLOR, scaleColor(vertexColor, 0.7));
            styleHighLightMap.put(mxConstants.STYLE_STROKEWIDTH, 2.5);

            styleGreyedHighLightMap.put(mxConstants.STYLE_FONTCOLOR, scaleColor(vertexColor, 0.3));
            styleGreyedHighLightMap.put(mxConstants.STYLE_FILLCOLOR, "0xaaaaaa");
            styleGreyedHighLightMap.put(mxConstants.STYLE_STROKECOLOR, scaleColor(vertexColor, 0.3));
            styleGreyedHighLightMap.put(mxConstants.STYLE_DASH_PATTERN, "1 3");
            styleGreyedHighLightMap.put(mxConstants.STYLE_DASHED, true);

            //edge:
            String dash = convertDashPatern(dashPattern);
            if(!dash.isEmpty()){
                edgeMap.put(mxConstants.STYLE_DASHED, true);
                edgeMap.put(mxConstants.STYLE_DASH_PATTERN, dash);
            }
            edgeMap.put(mxConstants.STYLE_STROKECOLOR, edgeColor);
            edgeMap.put(mxConstants.STYLE_FONTCOLOR, edgeColor);
            edgeMap.put(mxConstants.STYLE_STARTARROW, convertForm(startForm));
            edgeMap.put(mxConstants.STYLE_ENDARROW, convertForm(endForm));


            brokenEdgeMap.put(mxConstants.STYLE_STROKECOLOR, edgeColor);
            brokenEdgeMap.put(mxConstants.STYLE_FONTCOLOR, edgeColor);
            brokenEdgeMap.put(mxConstants.STYLE_DASHED, true);
            brokenEdgeMap.put(mxConstants.STYLE_DASH_PATTERN, "1 3");
            brokenEdgeMap.put(mxConstants.STYLE_STARTARROW, convertForm(startForm));
            brokenEdgeMap.put(mxConstants.STYLE_STARTFILL, false);
            brokenEdgeMap.put(mxConstants.STYLE_ENDARROW, convertForm(endForm));
            brokenEdgeMap.put(mxConstants.STYLE_ENDFILL, false);


            if("diamond".equalsIgnoreCase(startForm)){
                edgeMap.put(mxConstants.STYLE_STARTSIZE, 8);
                brokenEdgeMap.put(mxConstants.STYLE_STARTSIZE, 8);
            }
            if("diamond".equalsIgnoreCase(endForm)){
                edgeMap.put(mxConstants.STYLE_ENDSIZE, 8);
                brokenEdgeMap.put(mxConstants.STYLE_ENDSIZE, 8);
            }
            //newStyleMap.put(mxConstants.STYLE_VERTICAL_ALIGN, vAlign);
            if(mxConstants.ALIGN_TOP.equals(vAlign)){
                styleMap.put(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
                styleHighLightMap.put(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
                styleGreyedHighLightMap.put(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
                edgeMap.put(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
                brokenEdgeMap.put(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
            }

            return asArray(
                    new Style(getVertexStyle(name), styleMap),
                    new Style(getVertexHighLightStyle(name), styleHighLightMap),
                    new Style(getVertexGreyStyle(name), styleGreyedHighLightMap),
                    new Style(getEdgeStyle(name), edgeMap),
                    new Style(getEdgeBrokenStyle(name), brokenEdgeMap)
                    );
        }

        @Override
        public Color getHighLightColor() {
            return invisible;
        }

        @Override
        public String getVertexStyle(String styleName) {
            return "[Soft]"+styleName;
        }

        @Override
        public String getVertexHighLightStyle(String styleName) {
            return "[Soft][HighLight]"+styleName;
        }

        @Override
        public String getEdgeStyle(String styleName) {
            return "[Soft][edge]"+styleName;
        }

        @Override
        public String getEdgeBrokenStyle(String styleName) {
            return "[Soft][edge][broken]"+styleName;
        }

        @Override
        public String getVertexGreyStyle(String styleName) {
            return "[Soft][Greyed]"+styleName;
        }
    }
}
