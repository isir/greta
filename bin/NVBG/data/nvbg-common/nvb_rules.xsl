 <!-- This file maps the derived communicative functions to NVBS. 
 The NVBs are specified in general terms such as 'big_nod' whose BML code is 
 specified in behavior_description.xsl. -->
 
 <xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0"
 >	 

 <xsl:include href="behavior_description.xsl" />
 
<!-- Affirmation: head nod and brow raise -->
<xsl:template name="affirmation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"  />		
	<xsl:param name="posture" /> 
	<xsl:comment>Affirmation</xsl:comment>	

	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="big_nod" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>	
</xsl:template>

<!-- Inclusivity: one big head shake -->
<xsl:template name="inclusivity">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Inclusivity</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="shake">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- intensification: head nod on intensifying word and brow frown -->	
<xsl:template name="intensification">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Intensification</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_frown">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>	
</xsl:template>

<!-- Contrast: head moved to the side and brow raise-->	
<xsl:template name="contrast">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Contrast</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="head_tilt_right" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Response request: head moved to the side and brow raise -->	
<xsl:template name="response_req">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Response Request</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod"	>
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Word search: head tilt, brow raised, gaze away -->
<xsl:template name="word_search">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Word search</xsl:comment>	

	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="head_tilt_right">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
</xsl:template>

<!-- Listing: head moved to one side and to the other on the word 'and' -->	
<xsl:template name="listing">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Listing</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="head_tilt_right" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="head_tilt_left">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Assumption: head nods on the sentence or phrase, brow frown-->
<xsl:template name="assumption">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Assumption</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_frown">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Possibility: several head nods over  the whole sentence, brow raise-->	
<xsl:template name="possibility">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"/>	
	<xsl:param name="posture" /> 
	<xsl:comment>Possibility</xsl:comment>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="small_nod">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- first_NP: head nod-->	
<xsl:template name="first_NP">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" /> 
	<xsl:param name="priority" /> 
	<xsl:param name="posture" /> 
	<xsl:comment>First noun clause nod</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>		
</xsl:template>

<!-- noun_phrase: head nod-->	
<xsl:template name="noun_phrase">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" /> 
	<xsl:param name="posture" /> 
	<xsl:comment>Noun clause nod</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>	
</xsl:template>

<!-- interjection: big head nod-->	
<xsl:template name="interjection">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" /> 
	<xsl:param name="posture" /> 
	<xsl:comment>Interjection</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="big_nod" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>	
</xsl:template>

<!-- compound_sentence: saccade-->	
<xsl:template name="compound_sentence">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority" /> 
	<xsl:param name="posture" /> 
	<xsl:param name="target" /> 
	<xsl:comment>Compound Sentence</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="gaze_saccade" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" /> 
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
		<xsl:with-param name="target" select="$target" />
	</xsl:call-template>	
</xsl:template>
	
<!-- emo_positive: head nod and brow raise-->	
<xsl:template name="emo_positive">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"/>
	<xsl:param name="posture" />	
	<xsl:comment>Emotion Positive</xsl:comment>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise" >
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="concat($ready_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- emo_negative: shake head-->	
<xsl:template name="emo_negative">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"/>
	<xsl:param name="posture" />	
	<xsl:comment>Emotion Negative</xsl:comment>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="shake">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>	

</xsl:template>


<!-- gaze_aversion -->	
<xsl:template name="gaze_aversion">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"/>
	<xsl:param name="posture" />	
	<xsl:comment>Gaze Aversion</xsl:comment>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="gaze_aversion_downleft">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
</xsl:template>

<!-- Rio Lane 	
<xsl:template name="rio">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />
	<xsl:param name="priority"/>
	<xsl:param name="posture" />
	<xsl:comment>Rio</xsl:comment>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="rio_laine">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
		<xsl:with-param name="relax_time" select="$relax_time" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
</xsl:template>-->
<!-- _________________________________________________________________________________
																				ANIMATIONS
      __________________________________________________________________________________ -->
<!-- first_vp Animation: a beat gesture -->	
<xsl:template name="first_VP">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />		
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>first_VP Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_first_VP" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Me Animation -->	
<xsl:template name="me_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />		
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Me Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_me" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>

</xsl:template>

<!-- You Animation -->	
<xsl:template name="you_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>You Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_you" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
	 	<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Negation Animation -->	
<xsl:template name="negation_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />			
	<xsl:param name="stroke"/>		
	<xsl:param name="priority"/>		
	<xsl:param name="posture" /> 
	<xsl:comment>Negation Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_negation" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time"/>	
		<xsl:with-param name="stroke" select="$stroke" />		
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />	
	</xsl:call-template>
</xsl:template>

<!-- Contrast Animation -->	
<xsl:template name="contrast_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />			
	<xsl:param name="stroke"/>		
	<xsl:param name="priority"/>	
	<xsl:param name="posture" /> 
	<xsl:comment>Contrast  Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_contrast" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time"/>	
		<xsl:with-param name="stroke" select="$stroke" />		
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Assumption  Animation -->	
<xsl:template name="assumption_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />			
	<xsl:param name="stroke"/>		
	<xsl:param name="priority"/>	
	<xsl:param name="posture" /> 
	<xsl:comment>Assumption  Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_assumption" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time"/>	
		<xsl:with-param name="stroke" select="$stroke" />	
		<xsl:with-param name="priority" select="$priority" />	
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Rhetorical Animation -->	
<xsl:template name="rhetorical_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Rhetorical Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_rhetorical" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Inclusivity Animation -->	
<xsl:template name="inclusivity_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Inclusivity Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_inclusivity" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Question Animation -->	
<xsl:template name="question_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" />
	<xsl:comment>Question Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_question" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$start_time" />
		<xsl:with-param name="relax_time" select="concat($start_time, '+0.5')" />
		<xsl:with-param name="priority" select="$priority" />	
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
</xsl:template>
	
<!-- Obligation: on 'must' 'have to' 'ought to' 'need to', brow frowned -->
<xsl:template name="obligation_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" />
	<xsl:comment>Obligation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_obligation" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="nod">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$start_time" />
		<xsl:with-param name="relax_time" select="concat($start_time, '+0.5')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>

	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_frown">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$start_time" />
		<xsl:with-param name="relax_time" select="concat($start_time, '+0.5')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Greeting Animation -->	
<xsl:template name="greeting_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />			
	<xsl:param name="stroke"/>		
	<xsl:param name="priority"/>	
	<xsl:param name="posture" /> 
	<xsl:comment>Greeting Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_greeting" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time"/>	
		<xsl:with-param name="stroke" select="$stroke" />		
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
	
	<xsl:text>&#10;	</xsl:text>
	<xsl:call-template name="brow_raise">
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$start_time" />
		<xsl:with-param name="relax_time" select="concat($start_time, '+0.1')" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Right Animation -->	
<xsl:template name="right_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Indicate Right Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_right" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>


<!-- Left Animation -->	
<xsl:template name="left_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Indicate Left Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_left" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Chop Animation -->	
<xsl:template name="chop_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Chop Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_chop" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Rub Neck Animation -->	
<xsl:template name="rubneck_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Rub Neck Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_rubneck" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Rub Neck Loop Animation -->	
<xsl:template name="rubneckloop_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Rub Neck Loop Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_rubneckloop" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Rub Head Animation -->	
<xsl:template name="rubhead_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Rub Head Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_rubhead" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Rub Head  Loop Animation -->	
<xsl:template name="rubheadloop_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Rub Head Loop Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_rubheadloop" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Contemplate Animation -->	
<xsl:template name="contemplate_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Contemplate Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_contemplate" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Contemplate Loop Animation -->	
<xsl:template name="contemplateloop_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Contemplate Loop Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_contemplateloop" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Dismiss RArm Animation: dismiss rarm  -->	
<xsl:template name="dismissrarm_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Dismiss RArm Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_dismissrarm" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- Dismiss RArm and Gaze Aversion Animation: dismiss rarm and gaze aversion downleft -->	
<xsl:template name="dismissrarm_gazeavert_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>Dismiss RArm Animation and Gaze Aversion</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_dismissrarm" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>

	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="gaze_aversion_downleft" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- GrabChin Loop Animation -->	
<xsl:template name="grabchinloop_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>GrabChin Loop Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_grabchinloop" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- horizontal Loop Animation -->	
<xsl:template name="horizontal_animation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time"  />				
	<xsl:param name="stroke" />
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	<xsl:comment>horizontal Animation</xsl:comment>	
	
	<xsl:text>&#10;	</xsl:text>	
	<xsl:call-template name="ani_horizontal" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="start_time" select="$start_time" />			
		<xsl:with-param name="stroke" select="$stroke" />
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
	</xsl:call-template>
</xsl:template>

<!-- fmlbml_gaze: gaze aversion -->
<xsl:template name="fmlbml_gaze">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
  <xsl:param name="relax_time" />
	<xsl:param name="priority"  />		
	<xsl:param name="posture" />
  <xsl:param name="target" />
  <xsl:param name="prev_target" />
  <xsl:comment>FML_BML Gaze</xsl:comment>	

	<xsl:text>&#10;	</xsl:text>
  <xsl:call-template name="gaze_cursory" >
		<xsl:with-param name="participant" select="$participant" />
		<xsl:with-param name="speech_id" select="$speech_id" />
		<xsl:with-param name="ready_time" select="$ready_time" />
    <xsl:with-param name="relax_time" select="$relax_time" /> 
		<xsl:with-param name="priority" select="$priority" />
		<xsl:with-param name="posture" select="$posture" />
    <xsl:with-param name="target" select="$target" />
    <xsl:with-param name="prev_target" select="$prev_target" />
	</xsl:call-template>	
</xsl:template>

   <!-- Idle Gaze Behavior 	-->
   <xsl:template name="idle_gaze">
     <xsl:param name="participant" />
     <xsl:param name="priority" />
     <xsl:param name="target"  />
     <xsl:param name="posture"  />
     <xsl:comment>Idle Gaze Behavior</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="idle_gaze_aversion" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="target" select="$target" />
       <xsl:with-param name="posture" select="$posture" />
     </xsl:call-template>
   </xsl:template>


   <!-- Listener's feedback rules -->
   <!-- Generic feedback -->
   <!-- Attend nod -->
   <xsl:template name="A-NOD">
     <xsl:param name="participant" /> 
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:comment>Attend nod</xsl:comment>
     <!-- <head id="attend_h1" type="NOD" repeats="0.5" amount="1" velocity="3.5" 
     sbm:smooth="0.35" start="0.0" ready="0.2" relax="0.28" end="0.7"/>-->

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="atd_nod" >
       <xsl:with-param name="participant" select="$participant" /> 
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
     </xsl:call-template>
   </xsl:template>

   <!-- Attend Gaze -->
   <xsl:template name="A-GZ">
     <xsl:param name="participant" />  
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" /> 
     <xsl:comment>Attend Gaze</xsl:comment>
     <!-- <head id="attend_h1" type="NOD" repeats="0.5" amount="1" velocity="3.5" 
     sbm:smooth="0.35" start="sp1:T1" ready="sp1:T1+0.2" relax="sp1:T1+0.28" end="sp1:T1+0.7"/>-->

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="atd_gaze" >
       <xsl:with-param name="participant" select="$participant" /> 
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!-- Specific feedback -->
   <!-- Understanding nod -->
   <xsl:template name="U-NOD">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:comment>Understanding Nod</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="understanding" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
     </xsl:call-template>
   </xsl:template>

   <!-- Continue Attend Glance -->
   <xsl:template name="CA-GL">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" />
     <xsl:param name="prev_target" />
     <xsl:comment>Continue Attend Glance</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="atd_glance" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
       <xsl:with-param name="prev_target" select="$prev_target" />
     </xsl:call-template>
   </xsl:template>

   <!-- Gather Information: General Glance -->
   <xsl:template name="GI-GL">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" />
     <xsl:param name="prev_target" />
     <xsl:comment>Gather Info Glance</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gi_glance" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
       <xsl:with-param name="prev_target" select="$prev_target" />
     </xsl:call-template>
   </xsl:template>

   <!-- Gather Information: Furtive Glance -->
   <xsl:template name="GI-FGL">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" />
     <xsl:param name="prev_target" />
     <xsl:comment>Gather Info Glance</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gi_furtive_glance" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
       <xsl:with-param name="prev_target" select="$prev_target" />
     </xsl:call-template>
   </xsl:template>

   <!-- Think -->
   <xsl:template name="THK">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" /> 
     <xsl:comment>Think</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="thk_gaze_cursory" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" /> 
     </xsl:call-template>
   </xsl:template>

   <!-- Partial Understanding -->
   <xsl:template name="UD-CFS">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" />
     <xsl:comment>Partial Understanding</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="partial_understand" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>
   
   <!-- Confusion -->
   <xsl:template name="CFS">
     <xsl:param name="participant" />
     <xsl:param name="priority"  />
     <xsl:param name="posture" />
     <xsl:param name="target" />
     <xsl:comment>Confusion</xsl:comment>

     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="confusion" >
       <xsl:with-param name="participant" select="$participant" />
       <xsl:with-param name="priority" select="$priority" />
       <xsl:with-param name="posture" select="$posture" />
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Emotion Feedback-->
   <!--Joy-->
   <xsl:template name="joy">
     <xsl:param name="target" />
     <xsl:comment>Joy</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="emo-joy" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Fear-->

   <!--Surprise-->
   <xsl:template name="surprise">
     <xsl:param name="target" />
     <xsl:comment>Surprise</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="emo-surprise" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>


   <!--Disgust-->

   <!--Anger-->

   <!--Sadness-->
   <xsl:template name="sadness">
     <xsl:param name="target" />
     <xsl:comment>Sadness</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="emo-sadness" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Attitude Feedback-->
   <!--Agree-->
   <xsl:template name="agree">
     <xsl:param name="target" />
     <xsl:comment>Agree</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="attitude-agree" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Disagree-->
   <xsl:template name="disagree">
     <xsl:param name="target" />
     <xsl:comment>Disagree</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="attitude-disagree" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Like-->
   <xsl:template name="like">
     <xsl:param name="target" />
     <xsl:comment>Like</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="attitude-like" >
       <xsl:with-param name="target" select="$target" />
     </xsl:call-template>
   </xsl:template>

   <!--Dislike-->

   <!--Interested-->

   <!--NotInterested-->

   <!--Transition Feedback-->
   <!--EnterGroup-->
   <!--ExitGroup-->
   
   <!-- add new feedback rule here -->

   <!-- Gaze Behavior-->
   <!-- planning_speech_look_at_hearer -->
   <xsl:template name="planning_speech_look_at_hearer">
     <xsl:param name="target" />
     <xsl:comment>Planning speech look at hearer</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_focus" />
   </xsl:template>

   <!-- speaking -->
   <xsl:template name="speaking">
     <xsl:param name="target" />
     <xsl:comment>Speaking</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_focus" />
   </xsl:template>

   <!-- speech done -->
   <xsl:template name="speech_done">
     <xsl:param name="target" />
     <xsl:comment>Speech done</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_focus" />
   </xsl:template>

   <!--planning_speech_hold_turn-->
   <xsl:template name="planning_speech_hold_turn">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning_speech_hold_turn</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">eyes-offset</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--planning_speech_rejection-->
   <xsl:template name="planning_speech_rejection">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning_speech_rejection</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--planning_speech_rejection_goal_satisfied-->
   <xsl:template name="planning_speech_rejection_goal_satisfied">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning_speech_rejection_goal_satisfied</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">eyes-offset</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--planning_speech_acceptance_reluctant-->
   <xsl:template name="planning_speech_acceptance_reluctant">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning_speech_acceptance_reluctant</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--planning_speech_remembering-->
   <xsl:template name="planning_speech_remembering">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning_speech_remembering</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-up</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--speech_done_hold_turn-->
   <xsl:template name="speech_done_hold_turn">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>speech_done_hold_turn</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">eyes-offset</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--planning-->
   <xsl:template name="planning">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>planning</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">eyes-offset</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--avoidance_unambiguous_situation-->
   <xsl:template name="avoidance_unambiguous_situation">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>avoidance_unambiguous_situation</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--convey_displeasure_both-->
   <xsl:template name="convey_displeasure_both">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>convey_displeasure_both</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--accept_responsibility-->
   <xsl:template name="accept_responsibility">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>accept_responsibility</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--resignation-->
   <xsl:template name="resignation">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>resignation</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--avoidance_distancing-->
   <xsl:template name="avoidance_distancing">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>avoidance_distancing</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--avoidance_wishing_away-->
   <xsl:template name="avoidance_wishing_away">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>avoidance_wishing_away</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--convey_displeasure_single-->
   <xsl:template name="convey_displeasure_single">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>convey_displeasure_single</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

   <!--make_amends-->
   <xsl:template name="make_amends">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>make_amends</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_avert">
       <xsl:with-param name="track">sideways-down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>


   <!-- listen_to_speaker -->
   <xsl:template name="listen_to_speaker">
     <xsl:param name="target" />
     <xsl:comment>listen_to_speaker</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_weak_focus" />
   </xsl:template>

   <!-- interpret_speech -->
   <xsl:template name="interpret_speech">
     <xsl:param name="target" />
     <xsl:comment>interpret_speech</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_weak_focus" />
   </xsl:template>

   <!-- expect_speech -->
   <xsl:template name="expect_speech">
     <xsl:param name="target" />
     <xsl:comment>expect_speech</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_weak_focus" />
   </xsl:template>

   <!-- expect_acknowledgment -->
   <xsl:template name="expect_acknowledgment">
     <xsl:param name="target" />
     <xsl:comment>expect_acknowledgment</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_weak_focus" />
   </xsl:template>

   <!-- expect_repair -->
   <xsl:template name="expect_repair">
     <xsl:param name="target" />
     <xsl:comment>expect_repair</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_weak_focus" />
     <xsl:call-template name="shake_twice" />
   </xsl:template>

   <!-- monitor_goal -->
   <xsl:template name="monitor_goal">
     <xsl:param name="target" />
     <xsl:comment>monitor_goal</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_look" />
   </xsl:template>

   <!-- monitor_goal_refresh -->
   <xsl:template name="monitor_goal_refresh">
     <xsl:param name="target" />
     <xsl:comment>monitor_goal_refresh</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="monitor_goal_refresh" />
   </xsl:template>

   <!-- monitor_expected_effect -->
   <xsl:template name="monitor_expected_effect">
     <xsl:param name="target" />
     <xsl:comment>monitor_expected_effect</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_look" />
   </xsl:template>

   <!-- monitor_expected_effect -->
   <xsl:template name="monitor_expected_action_other_control">
     <xsl:param name="target" />
     <xsl:comment>monitor_expected_action_other_control</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_look" />
   </xsl:template>

   <!-- attend_to_sound_object -->
   <xsl:template name="attend_to_sound_object">
     <xsl:param name="target" />
     <xsl:comment>attend_to_sound_object</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_look" />
   </xsl:template>

   <!--monitor_expected_action_self_control-->
   <xsl:template name="monitor_expected_action_self_control">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:param name="prev_target" />
     <xsl:comment>monitor_expected_action_self_control</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_cursory_2">
       <xsl:with-param name="track">down</xsl:with-param>
       <xsl:with-param name="prev_target" select="$prev_target" />
     </xsl:call-template>
   </xsl:template>

   <!--monitor_expected_action_other_control_negative-->
   <xsl:template name="monitor_expected_action_other_control_negative">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:param name="prev_target" />
     <xsl:comment>monitor_expected_action_other_control_negative</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_cursory_2">
       <xsl:with-param name="track">down</xsl:with-param>
       <xsl:with-param name="prev_target" select="$prev_target" />
     </xsl:call-template>
   </xsl:template>

   <!--seek_social_support-->
   <xsl:template name="seek_social_support">
     <xsl:param name="target" />
     <xsl:param name="track" />
     <xsl:comment>seek_social_support</xsl:comment>
     <xsl:text>&#10;	</xsl:text>
     <xsl:call-template name="gaze_focus">
       <xsl:with-param name="track">down</xsl:with-param>
     </xsl:call-template>
   </xsl:template>

 </xsl:transform>