/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.ssi;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import static greta.auxiliary.ssi.SSITypes.INVALID_OR_EMPTY_DOUBLE_FEATURE_VALUE;
import static greta.auxiliary.ssi.SSITypes.INVALID_OR_EMPTY_INTEGER_FEATURE_VALUE;
import greta.auxiliary.ssi.SSITypes.SSIFeatureNames;
import greta.auxiliary.ssi.SSITypes.SSIPitchDirectionValues;
import greta.auxiliary.ssi.SSITypes.SSIPitchValues;
import greta.auxiliary.ssi.SSITypes.SSIVoiceEnergyValues;
import greta.auxiliary.ssi.SSIXMLToFrameTranslator.SSIParsingFilterOption;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Angelo Cafaro
 */
public class SSIXMLToFrameTranslator extends TextReceiver implements SSIFrameEmitter {
    private SSIFrameToSignal ssiFtS;

    public enum SSIParsingFilterOption {
        all("All"),
        prosody_only("Prosody Only"),
        head_only("Head Only"),
        body_only("Body Only"),
        age_gender_only("AGender Only"),
        emax_only("EMax Only"),
        ARIA_all("ARIA");

        public static final int size = values().length;

        public static SSIParsingFilterOption get(int ordinal){
            return (ordinal < 0 || ordinal >= size) ? all : values()[ordinal];
        }

        private String name;

        SSIParsingFilterOption(String name) {
            this.name = name;
        }

        /**
         * @return the friendly name of the enum option
         */
        @Override
        public String toString() {
            return name;
        }
    };

    private List<SSIFramePerfomer> ssiPerformers;
    private XMLParser ssiMessageParser; //xml parser
    private SSIParsingFilterOption parsingFilterOption;

    private boolean _tempDoPrintFrame;

    public SSIXMLToFrameTranslator() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "SSI");
    }

    /**
     * Constructor
     *
     * @param host Semaine server's IP
     * @param port Semaine server's port
     * @param topic SSI's activeMQ topic
     */
    public SSIXMLToFrameTranslator(String host, String port, String topic){
        super(host, port, topic);

        parsingFilterOption = SSIParsingFilterOption.all;
        ssiPerformers = new ArrayList<SSIFramePerfomer>();
        ssiMessageParser = XML.createParser();
        ssiMessageParser.setValidating(false);

        //_tempDoPrintFrame = true;
        _tempDoPrintFrame = false;
    }

    public void SetParserFilter(int choice)
    {
       parsingFilterOption = SSIParsingFilterOption.get(choice);

       //_tempDoPrintFrame = true;
    }

    /**
     * Handler of SSI messages, called every 100ms
     *
     * @param content SSI message's content
     * @param properties SSI message's properties
     */
    @Override
    protected void onMessage(String content, Map<String, Object> properties) {

        // Get the XML Tree from the message
        XMLTree ssiMessage = ssiMessageParser.parseBuffer(content.toString());

        // Return if no message
        if(ssiMessage == null) return;

        // Create a request to send
        String request = "SSI";
        ID id = IDProvider.createID(request);

        // Parse the message
        SSIFrame ssiFrame = SSIMessageToFrame(ssiMessage);

        // Send the frame to performers
        for(SSIFramePerfomer performer : ssiPerformers){
            performer.performSSIFrame(ssiFrame, id);
        }
        if(ssiFtS!=null){
            ssiFtS.performSSIFrame(ssiFrame, id);
        }
    }

    private SSIFrame SSIMessageToFrame(XMLTree ssiMessage) {

        // Create a new SSI Frame to fill in
        SSIFrame frame = new SSIFrame(Timer.getCurrentFrameNumber());

        SSIMessageToFrameProsody(ssiMessage, frame);

        SSIMessageToFrameHead(ssiMessage, frame);

        SSIMessageToFrameBody(ssiMessage, frame);

        SSIMessageToFrameAgender(ssiMessage, frame);

        SSIMessageToFrameEmax(ssiMessage, frame);

        if (_tempDoPrintFrame) {

            System.out.print(frame);
            //_tempDoPrintFrame = false;
        }

        return frame;
    }

    private void SSIMessageToFrameEmax(XMLTree ssiMessage, SSIFrame frame) {
        switch (parsingFilterOption) {
            case all:
            case ARIA_all:
            case emax_only:
                XMLTree face1 = ssiMessage.findNodeCalled("face-1");
                if (face1 != null) {
                    String id = face1.getAttribute("id");
                    frame.applyValue(SSIFeatureNames.emax_face1_id, ParseIntAdvanced(id));

                    XMLTree emotion = face1.findNodeCalled("emotion");

                    applyDoubleValue(frame, emotion, "neutral", SSIFeatureNames.emax_face1_neutral);
                    applyDoubleValue(frame, emotion, "anger", SSIFeatureNames.emax_face1_anger);
                    applyDoubleValue(frame, emotion, "disgust", SSIFeatureNames.emax_face1_disgust);
                    applyDoubleValue(frame, emotion, "fear", SSIFeatureNames.emax_face1_fear);
                    applyDoubleValue(frame, emotion, "happiness", SSIFeatureNames.emax_face1_happiness);
                    applyDoubleValue(frame, emotion, "sadness", SSIFeatureNames.emax_face1_sadness);
                    applyDoubleValue(frame, emotion, "surprised", SSIFeatureNames.emax_face1_surprised);
                }

                XMLTree face2 = ssiMessage.findNodeCalled("face-2");
                if (face2 != null) {
                    String id = face2.getAttribute("id");
                    frame.applyValue(SSIFeatureNames.emax_face2_id, ParseIntAdvanced(id));

                    XMLTree emotion = face2.findNodeCalled("emotion");

                    applyDoubleValue(frame, emotion, "neutral", SSIFeatureNames.emax_face2_neutral);
                    applyDoubleValue(frame, emotion, "anger", SSIFeatureNames.emax_face2_anger);
                    applyDoubleValue(frame, emotion, "disgust", SSIFeatureNames.emax_face2_disgust);
                    applyDoubleValue(frame, emotion, "fear", SSIFeatureNames.emax_face2_fear);
                    applyDoubleValue(frame, emotion, "happiness", SSIFeatureNames.emax_face2_happiness);
                    applyDoubleValue(frame, emotion, "sadness", SSIFeatureNames.emax_face2_sadness);
                    applyDoubleValue(frame, emotion, "surprised", SSIFeatureNames.emax_face2_surprised);
                }
                break;
        }
    }

    private void SSIMessageToFrameAgender(XMLTree ssiMessage, SSIFrame frame) {
        switch (parsingFilterOption) {
            case all:
            case ARIA_all:
            case age_gender_only:
                XMLTree agenderNode = ssiMessage.findNodeCalled("agender");
                if (agenderNode != null) {
                    XMLTree voiceNode = agenderNode.findNodeCalled("voice");
                    String active = voiceNode.getAttribute("active");
                    frame.applyValue(SSIFeatureNames.agender_active, ParseIntAdvanced(active));

                    XMLTree gender = voiceNode.findNodeCalled("gender");

                    applyDoubleValue(frame, gender, "male", SSIFeatureNames.agender_gender_male);
                    applyDoubleValue(frame, gender, "female", SSIFeatureNames.agender_gender_female);
                    applyDoubleValue(frame, gender, "child", SSIFeatureNames.agender_gender_child);

                    XMLTree age = voiceNode.findNodeCalled("age");

                    applyDoubleValue(frame, age, "child", SSIFeatureNames.agender_age_child);
                    applyDoubleValue(frame, age, "youth", SSIFeatureNames.agender_age_youth);
                    applyDoubleValue(frame, age, "adult", SSIFeatureNames.agender_age_adult);
                    applyDoubleValue(frame, age, "senior", SSIFeatureNames.agender_age_senior);
                }
                break;
        }
    }

    private void applyDoubleValue(SSIFrame frame, XMLTree root, String nodeName, SSIFeatureNames ssiFeature) {
        XMLTree node = root.findNodeCalled(nodeName);
        XMLTree textNode = node.findNodeCalled(XML.TEXT_NODE_NAME);
        String textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
        frame.applyValue(ssiFeature, ParseDoubleAdvanced(textNodeValue));
    }

    private void SSIMessageToFrameBody(XMLTree ssiMessage, SSIFrame frame) {
        if ((parsingFilterOption == SSIParsingFilterOption.all) || (parsingFilterOption == SSIParsingFilterOption.body_only) ){
            XMLTree bodyNode = ssiMessage.findNodeCalled("body");
            if (bodyNode != null) {

                XMLTree textNode = bodyNode.findNodeCalled("leanposture").findNodeCalled(XML.TEXT_NODE_NAME);
                String textNodeValue = (textNode != null)?textNode.getTextValue():null;
                frame.applyValue(SSIFeatureNames.body_posture_lean, ParseDoubleAdvanced(textNodeValue));

                textNode = bodyNode.findNodeCalled("openness").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null)?textNode.getTextValue():null;
                frame.applyValue(SSIFeatureNames.body_arms_openness, ParseDoubleAdvanced(textNodeValue));

                textNode = bodyNode.findNodeCalled("overallactivity").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null)?textNode.getTextValue():null;
                frame.applyValue(SSIFeatureNames.body_overall_activity, ParseDoubleAdvanced(textNodeValue));

                textNode = bodyNode.findNodeCalled("energy").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null)?textNode.getTextValue():null;
                frame.applyValue(SSIFeatureNames.body_hands_energy, ParseDoubleAdvanced(textNodeValue));

                List<XMLTree> bodyNodeChildren = bodyNode.getChildren();
                for (XMLTree bodyChild : bodyNodeChildren) {

                    if (bodyChild.isNamed("gesture")) {
                        String featureName = bodyChild.getAttribute("name").trim();
                        XMLTree gestureTextNode = bodyChild.findNodeCalled(XML.TEXT_NODE_NAME);
                        String gestureTextNodeValue = (gestureTextNode != null)?gestureTextNode.getTextValue():null;

                        if (featureName.equalsIgnoreCase("ArmsOpen")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_arms_open, ParseIntAdvanced(gestureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("ArmsCrossed")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_arms_crossed, ParseIntAdvanced(gestureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("LeftHandHeadTouch")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_left_hand_head_touch, ParseIntAdvanced(gestureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("RightHandHeadTouch")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_right_hand_head_touch, ParseIntAdvanced(gestureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("LeanFront")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_lean_front, ParseIntAdvanced(gestureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("LeanBack")) {
                            frame.applyValue(SSIFeatureNames.body_gesture_lean_back, ParseIntAdvanced(gestureTextNodeValue));
                        }
                    } // End if gesture child
                } // End for body node children
            } // End of Body Node
        } // End of Body Part
    }

    private void SSIMessageToFrameHead(XMLTree ssiMessage, SSIFrame frame) {
        if ((parsingFilterOption == SSIParsingFilterOption.all) || (parsingFilterOption == SSIParsingFilterOption.head_only)) {
            XMLTree headNode = ssiMessage.findNodeCalled("head");
            if (headNode != null) {

                XMLTree textNode = headNode.findNodeCalled("headposition").findNodeCalled("xpos").findNodeCalled(XML.TEXT_NODE_NAME);
                String textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_position_x, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headposition").findNodeCalled("ypos").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_position_y, ParseDoubleAdvanced(textNodeValue));

                // ********************* adding z position of the head *****************
                textNode = headNode.findNodeCalled("headposition").findNodeCalled("zpos").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_position_z, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headorientation").findNodeCalled("roll").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_orientation_roll, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headorientation").findNodeCalled("pitch").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_orientation_pitch, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headorientation").findNodeCalled("yaw").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_orientation_yaw, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headorientation").findNodeCalled("headfocus").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_focus, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headorientation").findNodeCalled("headtilt").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_tilt, ParseDoubleAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headnod").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_nod_cat, ParseIntAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("headshake").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_shake_cat, ParseIntAdvanced(textNodeValue));

                textNode = headNode.findNodeCalled("smile").findNodeCalled(XML.TEXT_NODE_NAME);
                textNodeValue = (textNode != null) ? textNode.getTextValue() : null;
                frame.applyValue(SSIFeatureNames.head_smile, ParseDoubleAdvanced(textNodeValue));
            } // End of Head Node
        } // End of Head Part
    }

    private void SSIMessageToFrameProsody(XMLTree ssiMessage, SSIFrame frame) {
        // Parse the file depending on the filtering options
        if ((parsingFilterOption == SSIParsingFilterOption.all) || (parsingFilterOption == SSIParsingFilterOption.prosody_only) ){
            XMLTree prosodyNode = ssiMessage.findNodeCalled("prosody");
            if (prosodyNode != null) {

                // Voice Node
                XMLTree voiceNode = prosodyNode.findNodeCalled("voice");
                if (voiceNode != null) {
                    frame.applyValue(SSIFeatureNames.prosody_voice_activity, ParseIntAdvanced(voiceNode.getAttribute("activity")));
                    frame.applyValue(SSIFeatureNames.prosody_voice_systemtime, ParseIntAdvanced(voiceNode.getAttribute("system-time")));
                    frame.applyValue(SSIFeatureNames.prosody_voice_duration, ParseIntAdvanced(voiceNode.getAttribute("duration")));
                    List<XMLTree> voiceChildren = voiceNode.getChildren();
                    for (XMLTree voiceChild : voiceChildren) {

                        XMLTree voiceTextChild = voiceChild.findNodeCalled(XML.TEXT_NODE_NAME);

                        if (voiceChild.isNamed("speech")) {
                            frame.applyValue(SSIFeatureNames.prosody_voice_speech_prob, ParseDoubleAdvanced((voiceTextChild != null)?voiceTextChild.getTextValue():null));
                        }
                        else if (voiceChild.isNamed("laughter")) {
                            frame.applyValue(SSIFeatureNames.prosody_voice_laughter_prob, ParseDoubleAdvanced((voiceTextChild != null)?voiceTextChild.getTextValue():null));
                        }
                    }
                } // End voice node

                // Praat Node
                XMLTree praatNode = prosodyNode.findNodeCalled("praat");
                if (praatNode != null) {

                    List<XMLTree> praatChildren = praatNode.getChildren();
                    for (XMLTree praatChild : praatChildren) {
                        String featureName = praatChild.getAttribute("name").trim();
                        XMLTree praatFeatureTextNode = praatChild.findNodeCalled(XML.TEXT_NODE_NAME);
                        String praatFeatureTextNodeValue = (praatFeatureTextNode != null)?praatFeatureTextNode.getTextValue():null;

                        if (featureName.equalsIgnoreCase("Pitch median (Hz)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pitch_median_hz, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pitch mean (Hz)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pitch_mean_hz, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pitch sd (Hz)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pitch_sd_hz, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pitch min (Hz)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pitch_min_hz, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pitch max (Hz)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pitch_max_hz, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pulses number")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pulses_number, ParseIntAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Pulses per sec (pulses/sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_pulses_per_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Periods number")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_periods_number, ParseIntAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Period mean (sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_period_mean_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Period sd (sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_period_sd_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Fraction locally unvoiced frames (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_fraction_locally_unvoiced_frames_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Voice breaks number")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_voice_breaks_number, ParseIntAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Voice breaks degree (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_voice_breaks_degree_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Jitter local (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_jitter_local_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Jitter local abs (sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_jitter_local_abs_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Jitter rap (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_jitter_rap_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Jitter ppq5 (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_jitter_ppq5_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Jitter ddp (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_jitter_ddp_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer local (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_local_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer local (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_local_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer apq3 (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_apq3_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer apq5 (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_apq5_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer apq11 (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_apq11_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Shimmer dda (%)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_shimmer_dda_100, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Harmonicity mean autocor")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_harmonicity_mean_autocor, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Harmonicity mean noise-to-harmonics ratio")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_harmonicity_mean_noise_harmonics_ratio, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Harmonicity mean harmonics-to-noise ratio (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_harmonicity_mean_harmonics_noise_ratio_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Speechrate duration (sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_speechrate_duration_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Speechrate voiced count")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_speechrate_voiced_count, ParseIntAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Speechrate (syllables/sec)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_speechrate_syllables_per_sec, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Intensity minimum (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_intensity_minimum_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Intensity maximum (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_intensity_maximum_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Intensity median (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_intensity_median_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }
                        else if (featureName.equalsIgnoreCase("Intensity average (dB)")) {
                            frame.applyValue(SSIFeatureNames.prosody_praat_intensity_average_db, ParseDoubleAdvanced(praatFeatureTextNodeValue));
                        }

                    } // End of for praat children
                } // End praat node

                // Opensmile Node
                XMLTree openSmileNode = prosodyNode.findNodeCalled("opensmile");
                if (openSmileNode != null) {

                    List<XMLTree> openSmileChildren = openSmileNode.getChildren();
                    for (XMLTree openSmileChild : openSmileChildren) {

                        if (openSmileChild.isNamed("feature")) {

                            String featureName = openSmileChild.getAttribute("name").trim();

                            if (featureName.equalsIgnoreCase("Pitch")) {

                                XMLTree openSmileChildTextNode = openSmileChild.findNodeCalled(XML.TEXT_NODE_NAME);
                                String pitchStringValue = (openSmileChildTextNode != null)?openSmileChildTextNode.getTextValue():null;
                                SSIPitchValues pitchValue = SSIPitchValues.none;

                                if (pitchStringValue != null)  {
                                    if (pitchStringValue.equalsIgnoreCase("Low")){
                                        pitchValue = SSIPitchValues.low;
                                    }
                                    else if (pitchStringValue.equalsIgnoreCase("Normal")){
                                        pitchValue = SSIPitchValues.normal;
                                    }
                                    else if (pitchStringValue.equalsIgnoreCase("High")){
                                        pitchValue = SSIPitchValues.high;
                                    }
                                }

                                frame.applyValue(SSIFeatureNames.prosody_opensmile_pitch_cat, pitchValue.ordinal());
                            }
                            else if (featureName.equalsIgnoreCase("PitchDirection")) {

                                XMLTree openSmileChildTextNode = openSmileChild.findNodeCalled(XML.TEXT_NODE_NAME);
                                String pitchDirStringValue = (openSmileChildTextNode != null)?openSmileChildTextNode.getTextValue():null;
                                SSIPitchDirectionValues pitchDirValue = SSIPitchDirectionValues.none;

                                if (pitchDirStringValue != null) {
                                    if (pitchDirStringValue.equalsIgnoreCase("Rise")){
                                        pitchDirValue = SSIPitchDirectionValues.rise;
                                    }
                                    else if (pitchDirStringValue.equalsIgnoreCase("Fall")){
                                        pitchDirValue = SSIPitchDirectionValues.fall;
                                    }
                                    else if (pitchDirStringValue.equalsIgnoreCase("Rise-Fall")){
                                        pitchDirValue = SSIPitchDirectionValues.rise_fall;
                                    }
                                    else if (pitchDirStringValue.equalsIgnoreCase("Fall-Rise")){
                                        pitchDirValue = SSIPitchDirectionValues.fall_rise;
                                    }
                                }

                                frame.applyValue(SSIFeatureNames.prosody_opensmile_pitch_direction_cat, pitchDirValue.ordinal());
                            }
                            else if (featureName.equalsIgnoreCase("Energy")) {

                                XMLTree openSmileChildTextNode = openSmileChild.findNodeCalled(XML.TEXT_NODE_NAME);
                                String energyStringValue = (openSmileChildTextNode != null)?openSmileChildTextNode.getTextValue():null;
                                SSIVoiceEnergyValues energyValue = SSIVoiceEnergyValues.none;

                                if (energyStringValue != null) {

                                    if (!energyStringValue.equals("Low") && !energyStringValue.equals("Medium") && !energyStringValue.equals("High")){
                                        double energy = Double.parseDouble(energyStringValue);
                                        frame.applyValue(SSIFeatureNames.prosody_opensmile_energy_cat, energy);
                                    }else{
                                        if (energyStringValue.equalsIgnoreCase("Low")){
                                            energyValue = SSIVoiceEnergyValues.low;
                                        }
                                        else if (energyStringValue.equalsIgnoreCase("Medium")){
                                            energyValue = SSIVoiceEnergyValues.medium;
                                        }
                                        else if (energyStringValue.equalsIgnoreCase("High")){
                                            energyValue = SSIVoiceEnergyValues.high;
                                        }

                                        frame.applyValue(SSIFeatureNames.prosody_opensmile_energy_cat, energyValue.ordinal());
                                    }
                                }


                            }
                        } // End of if children is feature
                    } // End for opensmile children

                    // Node GenevaMinimalFeatureSet
                    XMLTree genevaNode = openSmileNode.findNodeCalled("GenevaMinimalFeatureSet");
                    if (genevaNode != null) {

                        List<XMLTree> genevaChildren = genevaNode.getChildren();
                        for (XMLTree genevaChild : genevaChildren) {

                            String featureName = genevaChild.getAttribute("name").trim();
                            XMLTree genevaChildTextNode = genevaChild.findNodeCalled(XML.TEXT_NODE_NAME);
                            String featureValue = (genevaChildTextNode != null)?genevaChildTextNode.getTextValue():null;
                            double featureValueDouble = ParseDoubleAdvanced(featureValue);

                            if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_amean")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_a_mean, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_stddevNorm")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_stddevNorm, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_percentile20")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile20, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_percentile50")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile50, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_percentile80")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile80, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("F0semitoneFrom55Hz_sma3nz_pctlrange0-2")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_pctlrange0_2, featureValueDouble);
                            }
                            else if (featureName.equalsIgnoreCase("StddevUnvoicedSegmentLength")) {
                                frame.applyValue(SSIFeatureNames.prosody_geneva_UnvoicedSegmentLength_stddev, featureValueDouble);
                            }
                        } // End for geneva children
                    } // End of Geneva Node

                } // End Opensmile Node

                // MS-SPEECH Node (i.e. keyword)
                XMLTree msspeechNode = prosodyNode.findNodeCalled("keyword");
                if (msspeechNode != null) {
                    XMLTree msspeechTextNode = msspeechNode.findNodeCalled(XML.TEXT_NODE_NAME);
                    String msspeechTextValue = (msspeechTextNode != null)?msspeechTextNode.getTextValue():null;

                    if (msspeechTextValue != null) {
                        if (msspeechTextValue.startsWith("[type:msspeech")) {
                            // Option "semantics_prolog" used in SocialCueRecognition/pipes/process_sapi.pipeline of SSI

                            // These are examples of strings received when the option "semantics_prolog" is set:
                            //(a) [type:msspeech,uttr:'next',conf:0.303878,data:[type:data,conf:0.0288396,data:[function:next]]]
                            //(b) [type:msspeech,uttr:'See you',conf:0.731549,data:[type:data,conf:0.318097,data:[function:goodbye]]]

                            // 1. We extract the first pair of strings (in the first example string (a): "uttr:'next',conf:0.303878")
                            int uttrIndex = msspeechTextValue.indexOf("uttr:");
                            int dataIndex = msspeechTextValue.indexOf(",data:");
                            String extractedString = msspeechTextValue.substring(uttrIndex, dataIndex);

                            // We obtain the two "uttr:'next'" and "conf:0.303878" parts
                            String[] extractedParts = extractedString.split(",");

                            // We obtain the value at the right of "uttr:'next'" which is the keyword (in single quotes)
                            String keywordPart = extractedParts[0].split(":")[1];

                            // We stripes the quotes
                            String keyword = keywordPart.replaceAll("'", "");

                            // We obtain the value at the right of "conf:0.303878" which is the confidence
                            double keywordConfidence = Double.parseDouble(extractedParts[1].split(":")[1]);

                            // We apply the values to the SSI Frame
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_keyword, keyword);
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_keyword_confidence, keywordConfidence);

                            // 2. We extract the second pair of strings (in the first example string (a): "[type:data,conf:0.0288396,data:[function:next]]]")

                            // Get the start and end index for the function confidence value
                            int functionConfidenceStartIndex = msspeechTextValue.indexOf("conf:",dataIndex) + 5; // Note +5 to the obtained index considers the lenght of "conf:"
                            int functionConfidenceEndIndex = msspeechTextValue.indexOf(",data:",functionConfidenceStartIndex);

                            // We extract the function confidence string value
                            String extractedFunctionConfidenceString = msspeechTextValue.substring(functionConfidenceStartIndex, functionConfidenceEndIndex);

                            // We obtain the double value
                            double functionConfidence = Double.parseDouble(extractedFunctionConfidenceString);

                            // Get the start and end index for the function value
                            int functionStartIndex = msspeechTextValue.indexOf("function:") + 9; // Note +9 to the obtained index considers the lenght of "function:"
                            int functionEndIndex = msspeechTextValue.indexOf("]]]",functionStartIndex);

                            // We extract the function string value
                            String function = msspeechTextValue.substring(functionStartIndex, functionEndIndex);

                            // We apply the values to the SSI Frame
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_function, function);
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_function_confidence, functionConfidence);

                        }
                        else {
                            // Option "keyword" used in SocialCueRecognition/pipes/process_sapi.pipeline of SSI
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_keyword, msspeechTextValue);
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_keyword_confidence, -1.0d);
                            frame.applyValue(SSIFeatureNames.prosody_msspeech_function_confidence, -1.0d);
                        }
                    }
                }

            } // End prosody node
        } // End of Prosody Part
    }

    private int ParseIntAdvanced(String input) {

        if (input == null) return INVALID_OR_EMPTY_INTEGER_FEATURE_VALUE;

        input = input.trim();

        if (input.isEmpty()) return INVALID_OR_EMPTY_INTEGER_FEATURE_VALUE;

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {

            try {
                double floatValue = Double.parseDouble(input);
                return ((int) floatValue);
            }
            catch (NumberFormatException ex2) {
                return INVALID_OR_EMPTY_INTEGER_FEATURE_VALUE;
            }

        }

    }

    private double ParseDoubleAdvanced(String input) {

        if (input == null) return INVALID_OR_EMPTY_DOUBLE_FEATURE_VALUE;

        input = input.trim();

        if (input.isEmpty()) return INVALID_OR_EMPTY_DOUBLE_FEATURE_VALUE;

        try {
            return Double.parseDouble(input);
        }
        catch (NumberFormatException ex) {
            return INVALID_OR_EMPTY_DOUBLE_FEATURE_VALUE;
        }
    }

    @Override
    public void addSSIFramePerformer(SSIFramePerfomer ssifp) {
        ssiPerformers.add(ssifp);
    }

    @Override
    public void removeSSIFramePerformer(SSIFramePerfomer ssifp) {
        ssiPerformers.remove(ssifp);
    }

    public void setSSIFrameToSignal(SSIFrameToSignal ssiFtS){
        this.ssiFtS = ssiFtS;
    }

}
