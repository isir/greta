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
package greta.core.util.speech;

import greta.core.util.CharacterManager;
import greta.core.util.audio.Audio;
import greta.core.util.enums.interruptions.ReactionDuration;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.log.Logs;
import greta.core.util.time.SynchPoint;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class contains information about speech.
 * @author Andre-Marie Pez
 * @author Angelo Cafaro
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * greta.core.util.speech.Boundary
 * @composed - - * greta.core.util.speech.PitchAccent
 * @has - - * greta.core.util.speech.Phoneme
 */
public class Speech implements Temporizable{

    /**
     * @return the markers
     */
    public List<TimeMarker> getMarkers() {
        return markers;
    }

    /**
     * @param markers the markers to set
     */
    public void setMarkers(List<TimeMarker> markers) {
        this.markers = markers;
    }
//TODO add sressingpoint ? but it is only aviable with MaryTTS
    private List<Object> speechElements;
    private TimeMarker start;
    private TimeMarker end;
    private List<TimeMarker> markers;
    private List<Boundary> boundaries;
    private List<PitchAccent> pitchaccents;
    private String id;
    private List<Phoneme> phonems;
    private Audio audio;
    private String ref;
    private XMLTree orginalXMLTree;
    private String originalSAPI4Text;
    private String originalText;
    private String language;
    private static final String boundaryRegex = "(.*)\\p{Punct}\\s*\\z"; //used to replace redondante bundaries
    private static final String spaceRegex = "\\s+"; //used to replace multy spaces

    private ReactionType reactionType;
    private ReactionDuration reactionDuration;
    private String generatedSSML; // Specific to cereproc for the moment
    private TTS ttsToUse;
    private CharacterManager cm;

    public Speech(CharacterManager cm){
        this.cm = cm;
        markers = new  ArrayList<TimeMarker>();
        speechElements = new ArrayList<Object>();
        boundaries = new ArrayList<Boundary>();
        pitchaccents = new ArrayList<PitchAccent>();
        phonems = new ArrayList<Phoneme>();
        start = new TimeMarker("start");//must always be the first element
        end = new TimeMarker("end");//must always be the last element
        markers.add(start);
        markers.add(end);
        speechElements.add(start);
        speechElements.add(end);
        reactionType = ReactionType.NONE;
        reactionDuration = ReactionDuration.NONE;
        generatedSSML = null;
    }

    public Speech(Speech s){
        this.cm = s.cm;
        speechElements = new ArrayList<Object>(s.speechElements);
        markers = new ArrayList<TimeMarker>(s.getMarkers());
        start = s.start;
        end = s.end;
        boundaries = new ArrayList<Boundary>(s.boundaries);
        pitchaccents = new ArrayList<PitchAccent>(s.pitchaccents);
        id = s.id;
        if(s.phonems != null) {
            phonems = new ArrayList<Phoneme>(s.phonems);
        }
        audio = s.audio;
        ref = s.ref;
        orginalXMLTree = s.orginalXMLTree;
        originalSAPI4Text = s.originalSAPI4Text;
        originalText = s.originalText;
        language = s.language;
        reactionType = s.reactionType;
        reactionDuration = s.reactionDuration;
        generatedSSML = s.generatedSSML;
    }

    public CharacterManager getCharacterManager(){
        return cm;
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return getMarkers();
    }

    @Override
    public TimeMarker getTimeMarker(String name) {
        for(TimeMarker tm : getMarkers()) {
            if(tm.getName().equals(name)) {
                return tm;
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public void schedule(){
        synchronized (lock){
            ttsToUse = cm.getTTS();
            if(ttsToUse==null){
                Logs.error(this.getClass().getName() + " : no TTS found.");
            }
            else
            {
                if ((this.getInterruptionReactionType() != ReactionType.NONE) && (!ttsToUse.isInterruptionReactionSupported())) {
                    Logs.warning(this.getClass().getName() + ". Interruption reactions not supported by the TTS in use.");
                }

                ttsToUse.setSpeech(this);
                ttsToUse.compute(ttsDoTemporize, ttsDoAudio, ttsDoPhonemes);

                //Copy result phonems :
                if(ttsDoPhonemes) {
                    phonems = new ArrayList<Phoneme>(ttsToUse.getPhonemes());
                }

                //Copy result audio
                if(ttsDoAudio){
                    audio = ttsToUse.getAudio();
                    Speech.addScheduledSpeech(this);
                }
            }
        }
    }

    /**
     * Returns the reference of the audio file.
     * @return the reference of the audio file
     */
    public String getReference(){
        return ref;
    }

    /**
     * Sets the reference of the audio file.
     * @param reference the reference of the audio file
     */
    public void setReference(String reference){
        ref = reference;
    }

    /**
     * Returns the type of reaction to a user's interruption expressed with this Speech
     * @return the reaction type
     */
    public ReactionType getInterruptionReactionType(){
        return reactionType;
    }

    /**
     * Sets the type of reaction to a user's interruption expressed with this Speech
     * @param aReactionType the type of reaction to a user's interruption expressed with this Speech
     */
    public void setInterruptionReactionType(ReactionType aReactionType){
        reactionType = aReactionType;
    }

    /**
     * Returns the duration of the reaction to a user's interruption expressed with this Speech
     * @return the reaction duration
     */
    public ReactionDuration getInterruptionReactionDuration(){
        return reactionDuration;
    }

    /**
     * Sets the duration of the reaction to a user's interruption expressed with this Speech
     * @param aReactionDuration the duration of the reaction to a user's interruption expressed with this Speech
     */
    public void setInterruptionReactionDuration(ReactionDuration aReactionDuration){
        reactionDuration = aReactionDuration;
    }

    /**
     * Returns the {@code String} corresponding to this {@code Speech}.<br/>
     * @return the {@code String} of the original text corresponding to this {@code Speech}
     */
    public String getOriginalText(){
        return originalText;
    }

    /**
     * Returns the {@code Audio} corresponding to this {@code Speech}.<br/>
     * This {@code Audio} should be computed by the temporization of this, according to the TTS options set.
     * @return the {@code Audio} corresponding to this {@code Speech}
     * @see #setTTSOptions(boolean, boolean, boolean) setTTSOptions(boolean, boolean, boolean)
     */
    public Audio getAudio(){
        return audio;
    }

    /**
     * Saves the computed audio in a specific file.
     * @param fileName the name of the file.
     * @see greta.core.util.audio.Audio#save(java.lang.String) Audio.save(String)
     */
    public void saveAudio(String fileName){
        if(audio==null){
            //try to call TTS ?
            Logs.error(this.getClass().getName()+" : can not write in file "+fileName+" : the audio is null.");
            return ;
        }
        audio.save(fileName);
    }

    /**
     * Returns the list of {@code Phonems}.<br/>
     * This list should be computed by the temporization of this, according to the TTS options set.
     * @return the list of {@code Phonems}
     * @see #setTTSOptions(boolean, boolean, boolean) setTTSOptions(boolean, boolean, boolean)
     */
    public List<Phoneme> getPhonems(){
        return phonems;
    }

    public void addPhonem (Phoneme ph){
        phonems.add(ph);
    }

    public void addPhonems ( List<Phoneme> ph){
        phonems.addAll(ph);
    }

    /**
     * Returns the {@code List} of all speech element.<br/>
     * It contains Strings (the text to say), {@code TimeMarkers}, {@code Boundaries} and {@code PitchAccents}.
     * @return the List of all speech element
     */
    public List<Object> getSpeechElements(){
        return speechElements;
    }

    /**
     * Returns the list of {@code Boundaries}.
     * @return the list of {@code Boundaries}
     */
    public List<Boundary> getBoundaries(){
        return boundaries;
    }

    /**
     * Returns the list of {@code PitchAccents}.
     * @return the list of {@code PitchAccents}
     */
    public List<PitchAccent> getPitchAccent(){
        return pitchaccents;
    }

    /**
     * Returns the language of this {@code Speech}.
     * @return the language of this {@code Speech}
     */
    public String getLanguage(){return language;}

    public void setLanguage(String language){
        this.language = language;
    }

    /**
     * Returns the original {@code XMLtree} of this {@code Speech}<br/>
     * If this {@code Speech} was not read from an {@code XMLTree}, this function returns {@code null}
     * @return the original {@code XMLtree} of this {@code Speech}
     */
    public XMLTree getOriginalXML(){
        return orginalXMLTree;
    }

//    /**
//     * Loads speech informations from a {@code String} in SAPI 4 format.
//     * @param sapiText the text in SAPI 4 format
//     */
//    public void readFromSAPI4(String sapiText){
//        originalSAPI4Text = sapiText;
//        Logs.warning("Speech.readFromSAPI4(String) is not already implemented.");
//    }


    /**
     * Translates this {@code Speech} to a {@code String} in SSML format.
     * @return the text in SSML format
     */
    public XMLTree toSSML() throws Exception {
        return toSSML(0.0,0.0, false, null, null);
    }

    /**
     * Translates this {@code Speech} to a {@code String} in SSML format.
     * Instructs a prosody change conforming to the SSML standard with the Pitch and Rate attributes.
     * @param pitch relative change in pitch (10 = +10% in pitch)
     * @return the text in SSML format
     */
    public XMLTree toSSML(double rate, double pitch) throws Exception {
        return toSSML(rate, pitch, false, null, null);
    }

    /**
     * Translates this {@code Speech} to a {@code String} in SSML format.
     * Instructs a prosody change conforming to the SSML standard with the Pitch and Rate attributes.
     * @param pitch relative change in pitch (10 = +10% in pitch)
     * @param replaceBrackets if true, replace the open "&lt;" and closed brackets "&gt;" that are found in text nodes with the two given replacements, respectively, openBracketReplacement and closedBracketReplacement
     * (for internal use with specific TTS engines, e.g. CereProc, in order to include in the speak element TTS specific tags such as <voice>)
     * @param openBracketReplacement
     * @param closedBracketReplacement
     * @return the text in SSML format
     */
    public XMLTree toSSML(double rate, double pitch, boolean replaceBrackets, String openBracketReplacement, String closedBracketReplacement) throws Exception {

        XMLTree toReturn = XML.createTree("speak");
        toReturn.setAttribute("version", "1.0");
        toReturn.setAttribute("xmlns", "http://www.w3.org/2001/10/synthesis");
        toReturn.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        toReturn.setAttribute("xsi:schemaLocation", "http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis/synthesis.xsd");
        toReturn.setAttribute("xml:lang", "en-US");

        XMLTree end = toReturn;
        toReturn = toReturn.createChild("prosody");
        toReturn.setAttribute("rate", String.valueOf(rate)+"%");
        toReturn.setAttribute("pitch", String.valueOf(pitch)+"%");

        String tmpEndPitchAccent = null;
        XMLTree tmpEndPitchAccentXMLNode = null;

        for(int i=1; i<speechElements.size(); ++i){ //skip start and end //am: why?
            Object o = speechElements.get(i);
            if(o instanceof String)
            {
                String textToAdd = (String) o;

                if (replaceBrackets) {
                    textToAdd = textToAdd.replace("<", openBracketReplacement);
                    textToAdd = textToAdd.replace(">", closedBracketReplacement);
                }

                if(tmpEndPitchAccentXMLNode==null)
                {
                    toReturn.addText(textToAdd);
                }
                else{
                    tmpEndPitchAccentXMLNode.addText(textToAdd);
                }
            }
            else{
                if(o instanceof TimeMarker){
                    // time ?
                    if(tmpEndPitchAccent!=null && ((TimeMarker)o).getName().equals(tmpEndPitchAccent))
                    {
                        tmpEndPitchAccent = null;
                        tmpEndPitchAccentXMLNode=null;
                        XMLTree tm = toReturn.createChild("mark");
                        tm.setAttribute("name", ((TimeMarker)o).getName());
                    } else if(tmpEndPitchAccent!=null && !((TimeMarker)o).getName().equals(tmpEndPitchAccent)) {

                        XMLTree tm = tmpEndPitchAccentXMLNode.createChild("mark");
                        tm.setAttribute("name", ((TimeMarker)o).getName());
                    } else if(tmpEndPitchAccent==null) {

                        XMLTree tm = toReturn.createChild("mark");
                        tm.setAttribute("name", ((TimeMarker)o).getName());
                    }

                }
                else{
                    if(o instanceof Boundary){
                        Boundary boundary = (Boundary)o;
                        XMLTree b_node;
                        if(tmpEndPitchAccentXMLNode==null)
                        {
                            b_node = toReturn.createChild("break");
                        }
                        else{
                            b_node = tmpEndPitchAccentXMLNode.createChild("break");
                        }
                        b_node.setAttribute("time", ""+(int)(boundary.getDuration()*1000)+"ms");
                    }
                    else{
                        if(o instanceof PitchAccent){
                            PitchAccent pitchAccent = (PitchAccent)o;
                            XMLTree p_node = toReturn.createChild("emphasis");
                            p_node.setAttribute("level",PitchAccent.stringOfLevel(pitchAccent.getLevel()));
                            tmpEndPitchAccent = "end";
                            TimeMarker pitchAccentEnd  = pitchAccent.getEnd();
                            if(!pitchAccentEnd.getReferences().isEmpty()){
                                String targetName = pitchAccentEnd.getReferences().get(0).getTargetName();
                                if(targetName.startsWith(getId()+":")){
                                    tmpEndPitchAccent = targetName.substring(getId().length()+1);
                                }
                            }
                            tmpEndPitchAccentXMLNode = p_node;
                        }
                    }
                }
            }
        }
        toReturn = end;
        return toReturn;

    }

    /**
     * Translates this {@code Speech} to a {@code String} in SAPI 4 format.
     * @return the text in SAPI 4 format
     */
    public String toSAPI4(){
        String toReturn;
        if(useOriginal && originalSAPI4Text!=null) {
            toReturn = originalSAPI4Text;
        }
        else{
            toReturn = "";
            for(Object o : speechElements){
                if(o instanceof String){
                    Logs.debug(o.toString());
                    toReturn += o;
                }
                else{
                    if(o instanceof TimeMarker){
                        if(!((TimeMarker)o).getName().equalsIgnoreCase("start")
                                && !((TimeMarker)o).getName().equalsIgnoreCase("end"))
                        {
                            toReturn += "\\mrk="+getMarkers().indexOf(o)+"\\ ";
                        }
                    }
                    else{
                        //TODO add an ini parameter
                        /*
                        if(o instanceof Boundary){
                            switch (((Boundary)o).getBoundaryType()){
                                //TODO check if the next code is good
                                case Boundary.L : toReturn+=","; break;
                                case Boundary.H : toReturn+="!"; break;
                                case Boundary.LL : toReturn+="."; break;
                                case Boundary.LH : toReturn+=","; break;
                                case Boundary.HL : toReturn+=";"; break;
                                case Boundary.HH : toReturn+="?"; break;
                            }
                        }
                        */
                    }
                }
            }
        }
        Logs.debug(toReturn);
        return toReturn;
    }

    /**
     * Loads speech informations from an {@code XMLTree}.<br/>
     * The parameter {@code endAsDuration} is there to ensure backward compatibility
     * with the tag end used as a duration :
     * {@code true} to use the old wrong format, {@code false} otherwise.
     * This parameter must be remove as soon as possible.
     * @param tree the {@code XMLTree}
     * @param endAsDuration to ensure backward compatibility
     */
    public void readFromXML(XMLTree tree, boolean endAsDuration){
        orginalXMLTree = tree;
        if(! tree.getName().equalsIgnoreCase("speech")){
            Logs.error(this.getClass().getName()+" : can not read "+tree.getName()+" node.");
            return ;
        }
        id = tree.getAttribute("id");
        if(id.isEmpty()) {
            Logs.error(this.getClass().getName()+" : empty identifier.");
        }
        //TODO check for language specification in BML
        language = tree.getAttribute("language");
        if(language.isEmpty()) {
            language = "english";
        }
        //ENDTODO
        originalText = tree.hasAttribute("text") ? tree.getAttribute("text") : null;
        String audioref = tree.getAttribute("ref");
        if(!audioref.isEmpty()) {
            ref = audioref;
        }
        String startSynchPoint = tree.getAttribute("start");
        if(! startSynchPoint.isEmpty()) {
            getMarkers().get(0).addReference(startSynchPoint);
        }
        else {
            getMarkers().get(0).setValue(0);
        }

        String endSynchPoint = tree.getAttribute("end");
        if(! endSynchPoint.isEmpty()) {
            getEnd().addReference(endSynchPoint);
        }

        ArrayList<Boundary> tempBoundaries = new ArrayList<Boundary>();
        ArrayList<PitchAccent> tempPitchAccents = new ArrayList<PitchAccent>();

        //we only insert text and time marker. pitch accents and boundaries are inserted after
        for(XMLTree child : tree.getChildren()){
            if(child.isTextNode()){
                //Logs.debug(child.getTextValue());
                addSpeechElement(child.getTextValue());
            }
            else{
                if(child.getName().equalsIgnoreCase("boundary")){
                    //<boundary id='b2' type='HL' start='s1:tm5' end='0.3'/>
                    //<mary:boundary id="b23" type="HH" start="speech_uap_1:tm3" duration="400" xmlns:mary="http://mary.dfki.de/2002/MaryXML"/>
                    String b_id = child.getAttribute("id");
                    if(b_id.isEmpty()){
                        Logs.error(this.getClass().getName()+" : invalid boundary identifier.");
                        continue;
                    }
                    String b_start = child.getAttribute("start");
                    if(b_start.isEmpty()){
                        Logs.error(this.getClass().getName()+" : no start found for boundary : "+b_id+".");
                        continue;
                    }
                    int b_type = Boundary.typeOf(child.getAttribute("type"));
                    String b_end = child.getAttribute("end");
                    if(b_end.isEmpty()){
                        if (child.hasAttribute("duration")) {
                            double b_duration = child.getAttributeNumber("duration")/1000.0;//duration from maryxml is in miliseconds
                            tempBoundaries.add(new Boundary(b_id,b_type,b_start,b_duration));
                        }
                        else {
                            tempBoundaries.add(new Boundary(b_id,b_type,b_start));
                        }
                    }
                    else{
                        //take care of the norm ! end must not be a duration !
                        //backward compatibility
                        if(endAsDuration){try{
                            //we test if it's a number
                            double b_duration = Double.parseDouble(b_end);//send an exception if it's not a number
                            //if we are here, no exception was send
                            tempBoundaries.add(new Boundary(b_id,b_type,b_start,b_duration));
                            }catch(Exception e){// it's not a number so it may be a sychpoint
                                tempBoundaries.add(new Boundary(b_id,b_type,b_start,b_end));}
                        }
                        //end backward compatibility
                        else{
                            tempBoundaries.add(new Boundary(b_id,b_type,b_start,b_end));
                        }
                    }
                }
                else{
                    if(child.getName().equalsIgnoreCase("pitchaccent")){
                        //<pitchaccent id='xpa2' type='Lstar' start='s1:tm4' end='s1:tm5' level='medium' importance='1'/>
                        String p_id = child.getAttribute("id");
                        if(p_id.isEmpty()){
                            Logs.error(this.getClass().getName()+" : invalid pitch accent identifier.");
                            continue;
                        }
                        String p_start = child.getAttribute("start");
                        if(p_start.isEmpty()){
                            Logs.error(this.getClass().getName()+" : no start found for boundary : "+p_id+".");
                            continue;
                        }
                        int p_type = PitchAccent.typeOf(child.getAttribute("type"));
                        int p_level = PitchAccent.levelOf(child.getAttribute("level"));
                        double p_importance = child.hasAttribute("importance") ? child.getAttributeNumber("importance") : 1; //from BML dtd
                        String p_end = child.getAttribute("end");
                        if(p_end.isEmpty()){
                            tempPitchAccents.add(new PitchAccent(p_id,p_type,p_level,p_importance,p_start));
                        }
                        else{
                            //take care of the norm ! end must not be a duration !

                            //backward compatibility
                            if(endAsDuration){try{
                                //we test if it's a number
                                double p_duration = Double.parseDouble(p_end);//send an exception if it's not a number
                                //if we are here, no exception was send
                                p_end = p_start + " + " + p_duration;
                            }catch(Exception e){ /* it's not a number so it may be a sychpoint*/ }}
                            //end backward compatibility

                            tempPitchAccents.add(new PitchAccent(p_id,p_type,p_level,p_importance,p_start,p_end));
                        }
                    }
                    else{
                        if(child.getName().equalsIgnoreCase("tm")){
                            //<tm id="tm1" time="0.1" />
                            String t_id = child.getAttribute("id");
                            if(t_id.isEmpty()){
                                Logs.error(this.getClass().getName()+" : invalid time mark identifier.");
                                continue;
                            }
                            TimeMarker t = new TimeMarker(t_id);
                            addSpeechElement(t);
                            String t_time = child.getAttribute("time");
                            if(! t_time.isEmpty()) {
                                t.setValue(child.getAttributeNumber("time"));
                            }
                        }
                        else{
                            if(child.getName().equalsIgnoreCase("mark")){ //from ssml
                                //<mark name="wb1"/>
                                String t_id = child.getAttribute("name");

                                //SEMAINE :
                                //<mark name="speech_uap_1:tm4"/> ! speech identifier inside ?
                                //TODO fixe it in semaine (at Utterance side), it's dirty
                                if(t_id.substring(0, t_id.indexOf(':')).equalsIgnoreCase(id)) {
                                    t_id = t_id.substring(t_id.indexOf(':')+1);
                                }
                                //END SEMAINE

                                if(t_id.isEmpty()){
                                    Logs.error(this.getClass().getName()+" : invalid time mark identifier.");
                                    continue;
                                }
                                TimeMarker t = new TimeMarker(t_id);
                                addSpeechElement(t);
                            }
                            else {
                                if(child.getName().equalsIgnoreCase("description")) {
                                    // nothing
                                }
                                else {

                                    if (child.getName().equalsIgnoreCase("voice")) { // from CereProc TTS
                                        String voiceStartTag = "<voice ";
                                        if (child.hasAttribute("emotion")) {
                                            voiceStartTag+="emotion='" + child.getAttribute("emotion").toString() + "'";
                                        }
                                        voiceStartTag+= ">";
                                        String voiceEndTag = "</voice>";

                                        addSpeechElement(voiceStartTag);

                                        for(XMLTree voiceChild : child.getChildren()) {
                                            if(voiceChild.isTextNode()) {
                                                //Logs.debug(child.getTextValue());
                                                addSpeechElement(voiceChild.getTextValue());
                                            }
                                            else {
                                                if(voiceChild.getName().equalsIgnoreCase("tm")){
                                                    String t_id = voiceChild.getAttribute("id");
                                                    if(t_id.isEmpty()){
                                                        Logs.error(this.getClass().getName()+" : invalid time mark identifier.");
                                                        continue;
                                                    }
                                                    TimeMarker t = new TimeMarker(t_id);
                                                    addSpeechElement(t);
                                                    String t_time = voiceChild.getAttribute("time");
                                                    if(! t_time.isEmpty()) {
                                                        t.setValue(voiceChild.getAttributeNumber("time"));
                                                    }
                                                }

                                            }
                                        }

                                        addSpeechElement(voiceEndTag);
                                    }
                                    else {
                                        String childContent = child.toString();
                                        childContent = childContent.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
                                        addSpeechElement(childContent);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //we suppose that boundaries and pitch accents may not be in the right order
        //so we add them at this end (addSpeechElement put them after corresponding start time marker)
        for(PitchAccent p : tempPitchAccents) {
            addSpeechElement(p);
        }
        for(Boundary b : tempBoundaries) {
            addSpeechElement(b);
        }

    }

    /**
     * Translates this {@code Speech} to an {@code XMLTree}.<br/>
     * The parameter {@code endAsDuration} is there to ensure backward compatibility
     * with the tag end used as a duration :
     * {@code true} to use the old wrong format, {@code false} otherwise.
     * This parameter must be remove as soon as possible.
     * @param endAsDuration to ensure backward compatibility
     * @return the {@code XMLTree}
     */
    public XMLTree toXML(boolean endAsDuration){
        XMLTree toReturn;
        if(useOriginal && orginalXMLTree!=null) {
            toReturn = orginalXMLTree;
        }
        else{
            toReturn = XML.createTree("speech");
            toReturn.setAttribute("id", id);
            TimeMarker start = getMarkers().get(0);
            SynchPoint synchRef = start.getFirstSynchPointWithTarget();
            if(synchRef != null) {
                toReturn.setAttribute("start", synchRef.toString());
            }
            else{
                if(start.concretizeByReferences()) {
                    toReturn.setAttribute("start", ""+start.getValue());
                }
            }
            if(originalText != null) {
                toReturn.setAttribute("text", originalText);
            }
            if(ref != null) {
                toReturn.setAttribute("ref", ref);
            }
            for(int i=1; i<speechElements.size()-1; ++i){ //skip start and end
                Object o = speechElements.get(i);
                if(o instanceof String) {
                    toReturn.addText((String)o);
                }
                else{
                    if(o instanceof TimeMarker){
                        XMLTree tm = toReturn.createChild("tm");
                        tm.setAttribute("id", ((TimeMarker)o).getName());
                        // time ?
                    }
                    else{
                        if(o instanceof Boundary){
                            Boundary boundary = (Boundary)o;
                            XMLTree b_node = toReturn.createChild("boundary");
                            String b_id = boundary.getId();
                            b_node.setAttribute("id", b_id);
                            TimeMarker b_start = boundary.getStart();
                            SynchPoint b_refstart = b_start.getFirstSynchPointWithTarget();
                            if(b_refstart != null) {
                                b_node.setAttribute("start", b_refstart.toString());
                            }
                            else{
                                if(b_start.concretizeByReferences()) {
                                    b_node.setAttribute("start", ""+b_start.getValue());
                                }
                            }
                            double b_duration = boundary.getDuration();
                            if(b_duration>=0) {
                                b_node.setAttribute("end",b_id+":start + "+b_duration);
                            }
                            else{
                                TimeMarker b_end = boundary.getEnd();
                                SynchPoint b_refend = b_end.getFirstSynchPointWithTarget();
                                if(b_refend!=null) {
                                    b_node.setAttribute("end",b_refend.toString());
                                }
                                else{
                                    if(b_end.isConcretized()){

                                        //backward compatibility
                                        if(endAsDuration){
                                            b_node.setAttribute("end", TimeMarker.timeFormat.format(b_end.getValue()-b_start.getValue()));
                                        }
                                        //end backward compatibility
                                        else {
                                            b_node.setAttribute("end",""+b_end.getValue());
                                        }
                                    }
                                    else{ //default value

                                        //backward compatibility
                                        if(endAsDuration){
                                            b_node.setAttribute("end",TimeMarker.timeFormat.format(Boundary.defaultDurationFor(boundary)));
                                        }
                                        //end backward compatibility
                                        else {
                                            b_node.setAttribute("end",b_id+":start + "+Boundary.defaultDurationFor(boundary));
                                        }
                                    }
                                }
                            }
                            b_node.setAttribute("type",Boundary.stringOfType(boundary.getBoundaryType()));
                        }
                        else{
                            if(o instanceof PitchAccent){
                                PitchAccent pitchAccent = (PitchAccent)o;
                                XMLTree p_node = toReturn.createChild("pitchaccent");
                                String p_id = pitchAccent.getId();
                                p_node.setAttribute("id", p_id);
                                TimeMarker p_start = pitchAccent.getStart();
                                SynchPoint p_refstart = p_start.getFirstSynchPointWithTarget();
                                if(p_refstart != null) {
                                    p_node.setAttribute("start", p_refstart.toString());
                                }
                                else{
                                    if(p_start.concretizeByReferences()) {
                                        p_node.setAttribute("start", ""+p_start.getValue());
                                    }
                                }
                                TimeMarker p_end = pitchAccent.getEnd();
                                SynchPoint p_refend = p_end.getFirstSynchPointWithTarget();
                                if(p_refend != null) {
                                    p_node.setAttribute("end", p_refend.toString());
                                }
                                else{
                                    if(p_end.concretizeByReferences()){
                                        //backward compatibility
                                        if(endAsDuration){
                                            p_node.setAttribute("end", TimeMarker.timeFormat.format(p_end.getValue()-p_start.getValue()));
                                        }
                                        //end backward compatibility
                                        else {
                                            p_node.setAttribute("end", ""+p_end.getValue());
                                        }
                                    }
                                }
                                p_node.setAttribute("type",PitchAccent.stringOfType(pitchAccent.getPitchAccentType()));
                                p_node.setAttribute("level",PitchAccent.stringOfLevel(pitchAccent.getLevel()));
                                p_node.setAttribute("importance",""+pitchAccent.getImportance());
                            }
                        }
                    }
                }
            }
            //TODO language, voice ?
        }
        return toReturn;
    }

    public void addSpeechElement(Object o){
        //this Object will be added add the end of the list but before the "end" time marker (added in constructor)
        //Pitch accents and boundaries are added in speechElements juste after the time marker that equals theire start
        int indexToAdd = speechElements.size()-1;
        if(o instanceof Boundary){
            boundaries.add((Boundary)o);
            int temp_index = indexOfTimeMarkerCorrespondingToStartOf((Boundary)o);
            indexToAdd = temp_index == -1 ? indexToAdd : temp_index+1;
            //we try to replace redondante punctuation in the text if present
            int i = indexToAdd-1;
            while(i>=0 && !(speechElements.get(i) instanceof String)) {
                --i;
            }
            if(i>=0){ speechElements.set(i,
                    ((String)speechElements.get(i))
                   // ((String)speechElements.get(i)).replaceFirst(boundaryRegex, "$1 ") //TODO same todo in getSAPI4()
                    );
            }
        }
        if(o instanceof PitchAccent){
            pitchaccents.add((PitchAccent)o);
            int temp_index = indexOfTimeMarkerCorrespondingToStartOf((PitchAccent)o);
            indexToAdd = temp_index == -1 ? indexToAdd : temp_index+1;
        }
        if(o instanceof TimeMarker) {
            getMarkers().add(getMarkers().size()-1,(TimeMarker)o);
        }
        if(o instanceof String){
            String text = (String)o;
            //check if the previous element is also a text
            int before = indexToAdd-1;
            Object o_before = speechElements.get(before);
            if(o_before instanceof String){
                //update the text
                text = ((String)o_before) + " " + text;
                //delete the old text
                speechElements.remove(before);
                //update indexToAdd
                indexToAdd = before;
            }
            o = text.replaceAll(spaceRegex," "); //simplify a multy space characters to only one space
        }
        speechElements.add(indexToAdd,o);
    }

    private int indexOfTimeMarkerCorrespondingToStartOf(Temporizable t){
        SynchPoint s = t.getStart().getFirstSynchPointWithTarget();
        if(s==null) {
            return -1;
        }
        String targetName = s.getTargetName();
        String sourceName = targetName.substring(0,targetName.indexOf(':'));
        if(id.equalsIgnoreCase(sourceName)){
            String name = targetName.substring(targetName.indexOf(':')+1);
            for(int i=0; i<speechElements.size(); ++i){
                Object o  = speechElements.get(i);
                if((o instanceof TimeMarker) && ((TimeMarker)o).getName().equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }
        return -1;
    }


//Static fields :
    //private static TTS ttsToUse;
    private static boolean ttsDoTemporize = true;
    private static boolean ttsDoAudio = true;
    private static boolean ttsDoPhonemes = true;
    private static boolean useOriginal = true;
    private static final Object lock = new Object(); // used to synchronize threads on ttsToUse
    private static ArrayList<Speech> scheduledSpeeches = new ArrayList<Speech>(); // Used to keep track of the speech objects that are scheduled to be played by the Mixer

     /**
     * Adds the {@code Speech} object to the list of scheduled ones (i.e. to be played).
     * @param speechToAdd the {@code Speech} object to add
     */
    public static void addScheduledSpeech(Speech speechToAdd) {
        synchronized (lock){

            // First clean up the finished audios associated to the speech objects in the list
            ListIterator<Speech> iter = scheduledSpeeches.listIterator();
            while (iter.hasNext()) {
                Speech s = iter.next();
                if ((s.getAudio() == null) || (s.getAudio().getPlayingBufferPos() == Audio.PLAYING_BUFFER_POSITION_FINISHED)) {
                    iter.remove();
                }
            }

            // Add to the list
            scheduledSpeeches.add(speechToAdd);

        } // End of synchronized block
    }

    /**
     * Gets the {@code Speech} object that is currently being played, null if none is found, the one with latest start time if multiple ones are being played.
     * @param clearScheduled clears out {@code Speech} objects that are scheduled (i.e. PLAYING_BUFFER_POSITION_FINISHED) in addition to (default behavior) those that have been played already (i.e. PLAYING_BUFFER_POSITION_FINISHED)
     *
     * @return the {@code Speech} corresponding to the {@code Audio} currently being played
     */
    public static Speech getCurrentPlayingScheduledSpeech(boolean clearScheduled) {
        synchronized (lock){


            // Audio start->time and end->estimatedEnd

            ArrayList<Speech> potentialSpeechToReturn = new ArrayList<Speech>();

            ListIterator<Speech> iter = scheduledSpeeches.listIterator();
            while (iter.hasNext()) {

                Speech s = iter.next();

                // Clean up finished ones
                if (s.getAudio().getPlayingBufferPos() == Audio.PLAYING_BUFFER_POSITION_FINISHED) {
                    iter.remove();
                }
                // We remove the next scheduled ones too if clearScheduled is set to true
                else if (s.getAudio().getPlayingBufferPos() == Audio.PLAYING_BUFFER_POSITION_SCHEDULED) {
                    if (clearScheduled) {
                        iter.remove();
                    }
                }
                else if (s.getAudio().getPlayingBufferPos() >= 0) {
                    // We consider currently playing ones
                    potentialSpeechToReturn.add(s);
                }
            }

            if (potentialSpeechToReturn.isEmpty()) {
                return null;
            }
            else {
                // Potential multiple playing Speech objects found, we return the one with the latest start time

                Speech toReturn = potentialSpeechToReturn.get(0);
                long latestStart = toReturn.getAudio().getTimeMillis();
                for (int i = 1; i < potentialSpeechToReturn.size(); i++) {
                    if (potentialSpeechToReturn.get(i).getAudio().getTimeMillis() > latestStart) {
                        toReturn = potentialSpeechToReturn.get(i);
                        latestStart = toReturn.getAudio().getTimeMillis();
                    }
                }
                return toReturn;
            }
        }
    }

    /**
     * Sets the {@code TTS} that all {@code Speech} will use to be scheduled.
     * @param toUse the {@code TTS} to use
     */
    public void setTTS(TTS toUse){
        synchronized (lock){
            ttsToUse = toUse;
            scheduledSpeeches.clear();
        } // End of synchronized block
    }

    /**
     * Returns the {@code TTS} that all {@code Speech} will use to be scheduled.
     * @return the {@code TTS} used
     */
    public TTS getTTS(){
        return ttsToUse;
    }

    /**
     * Sets the options to use with the TTS.<br/>
     * @param doTemporize {@code true} if it really needs the TTS calculates {@code TimeMarkers}
     * @param doAudio {@code true} if it really needs the TTS calculates audio
     * @param doPhonemes {@code true} if it really needs the TTS calculates {@code Phonemes}
     */
    public static void setTTSOptions(boolean doTemporize, boolean doAudio, boolean doPhonemes){
        synchronized (lock){
            ttsDoTemporize = doTemporize;
            ttsDoAudio = doAudio;
            ttsDoPhonemes = doPhonemes;
        }
    }

    /**
     * Sets an option for functions {@code toXML()} and {@code toSAPI4()}.<br/>
     * these functions use this argument to return the original object read with {@code readFromXML} and {@code readFromSAPI4} functions.
     * @param original {@code true} to return the original object, {@code false} otherwise.
     * @see #toXML(boolean)
     * @see #toSAPI4()
     * @see #readFromXML(greta.core.util.xml.XMLTree, boolean)
     */
    public static void setOriginal(boolean original){
        useOriginal = original;
    }


    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

    public String getGeneratedSSML() {
        return generatedSSML;
    }

    public void setGeneratedSSML(String SSML) {
        this.generatedSSML = SSML;
    }

}
