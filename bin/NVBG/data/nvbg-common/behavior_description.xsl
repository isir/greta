<!--  This files generates actual BML codes for generic behavior terms (e.g. big_nod, small_nod) -->

<!-- Behaviors
	Head: nod, shake, toss, orient
	Brow: frown, raise, flash
	Mouth: flat, smile, laugh, pucker, frown
-->

<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:external="http://ExternalFunction.xslt.isi.edu"
 xmlns:sbm="http://ict.usc.edu"
 version="2.0"
 >	
 

<!-- ____________________________head movement_________________________________ -->
<!-- big nod -->
<!-- <face id="blink" type="facs" au="45" amount="0.4" start="0" end="0.1" side="BOTH"/>
	<head id="anticipation" type="nod" velocity="1" amount="-0.02" repeats="0.5"/>
	<head id="action" type="nod" velocity="1" amount="0.2" repeats="1" start="anticipation:relax" relax="anticipation:relax+0.8" />
	<head id="overshoot" type="nod" velocity="0.5" amount="0.05" repeats="0.5" start="action:relax"/> -->	
<xsl:template name="big_nod">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />	
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="id">blink</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">45</xsl:attribute>
		<xsl:attribute name="amount">1</xsl:attribute>
		<xsl:attribute name="sbm:duration">0.1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" />-0.5</xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>		
		<xsl:attribute name="amount">-0.02</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />-0.5</xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>		
		<xsl:attribute name="amount">0.5</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>
		<xsl:attribute name="relax">anticipation:relax+1</xsl:attribute>			
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>
		<xsl:attribute name="velocity">0.5</xsl:attribute>		
		<xsl:attribute name="amount">0.05</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- nod -->
<!-- <face id="blink" type="facs" au="45" amount="0.4" start="0" end="0.2" side="BOTH"/>
	<head id="anticipation" type="nod" velocity="1" amount="-0.01" repeats="0.5"/>
	<head id="action" type="nod" velocity="1" amount="0.1" repeats="1" start="anticipation:relax" relax="anticipation:relax+0.5" />
	<head id="overshoot" type="nod" velocity="0.5" amount="0.04" repeats="0.5" start="action:relax"/> -->
<xsl:template name="nod">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="id">blink</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">45</xsl:attribute>
		<xsl:attribute name="amount">1</xsl:attribute>
		<xsl:attribute name="sbm:duration">0.1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />-0.3</xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>		
		<xsl:attribute name="amount">-0.01</xsl:attribute>		
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />-0.3</xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>		
		<xsl:attribute name="amount">0.05</xsl:attribute>		
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>
		<xsl:attribute name="relax">anticipation:relax+0.6</xsl:attribute>			
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>
		<xsl:attribute name="velocity">0.5</xsl:attribute>		
		<xsl:attribute name="amount">0.04</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- small nod -->
<!-- <face id="blink" type="facs" au="45" amount="0.4" start="0" end="0.2" side="BOTH"/>
	<head id="action" type="nod" velocity="1" amount="0.05" repeats="1" />
	<head id="overshoot" type="nod" velocity="1" amount="0.01" repeats="0.5" start="action:relax"/> -->
<xsl:template name="small_nod">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 	
	
	<xsl:element name="face">
		<xsl:attribute name="id">blink</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">45</xsl:attribute>
		<xsl:attribute name="amount">1</xsl:attribute>
		<xsl:attribute name="sbm:duration">0.1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>
		<xsl:attribute name="velocity">1</xsl:attribute>		
		<xsl:attribute name="amount">0.01</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>
		<xsl:attribute name="velocity">1</xsl:attribute>		
		<xsl:attribute name="amount">0.01</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- big shake -->  
<!-- <head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.3" repeats="1" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="big_shake">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.3</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- shake --> 
<!-- <head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.2" repeats="1" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="shake">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />-0.1</xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">arc</xsl:attribute>	
		<xsl:attribute name="type">NOD</xsl:attribute>		
		<xsl:attribute name="amount">0.05</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.2</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- small shake -->
<!-- <head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.1" repeats="1" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="small_shake">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.1</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- shake twice --> 
<!-- <head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.1" repeats="2" velocity="1.6" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="shake_twice">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>
		<xsl:attribute name="velocity">1.6</xsl:attribute>		
		<xsl:attribute name="amount">0.1</xsl:attribute>
		<xsl:attribute name="repeats">2</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- big shake, closed eyes -->
<!-- <face id="heavy_eyelid" type="facs" au="5" amount="-0.5" start="0" stroke=".5" sbm:duration="1" />  
	<head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.3" repeats="1" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="big_shake_eyes_closed">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="id">heavy_eyelid</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">5</xsl:attribute>
		<xsl:attribute name="amount">-0.5</xsl:attribute>
		<xsl:attribute name="stroke">0.5</xsl:attribute>		
		<xsl:attribute name="sbm:duration">1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.3</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>			
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- shake_closed_eyes -->
<!-- <face id="heavy_eyelid" type="facs" au="5" amount="-0.5" start="0" stroke=".5" sbm:duration="1" />  -->
<xsl:template name="shake_eyes_closed">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="id">heavy_eyelid</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">5</xsl:attribute>
		<xsl:attribute name="amount">-0.5</xsl:attribute>
		<xsl:attribute name="stroke">0.5</xsl:attribute>		
		<xsl:attribute name="sbm:duration">1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.2</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>			
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- small_shake_closed_eyes -->
<!--
<face id="heavy_eyelid" type="facs" au="5" amount="-0.5" start="0" stroke=".5" sbm:duration="1" />  
<head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
<head id="action" type="shake" amount="0.1" repeats="1" start="anticipation:relax"/>
<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="snake_eyes_closed">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="id">heavy_eyelid</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">5</xsl:attribute>
		<xsl:attribute name="amount">-0.5</xsl:attribute>
		<xsl:attribute name="stroke">0.5</xsl:attribute>		
		<xsl:attribute name="sbm:duration">1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.1</xsl:attribute>
		<xsl:attribute name="repeats">1</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>			
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- shake twice, closed eyes -->  
<!-- <face id="heavy_eyelid" type="facs" au="5" amount="-0.3" start="0" stroke=".5" />  
	<head id="anticipation" type="shake" amount="-0.03" repeats="0.5"/>
	<head id="action" type="shake" amount="0.1" repeats="2" velocity="1.6" start="anticipation:relax"/>
	<head id="overshoot" type="shake" amount="0.03" repeats="0.5" start="action:relax" /> -->
<xsl:template name="shake_twice_eyes_closed">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 

	<xsl:element name="face">
		<xsl:attribute name="id">heavy_eyelid</xsl:attribute>	
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">5</xsl:attribute>
		<xsl:attribute name="amount">-0.5</xsl:attribute>
		<xsl:attribute name="stroke">0.5</xsl:attribute>		
		<xsl:attribute name="sbm:duration">1</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">anticipation</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>	
		<xsl:attribute name="amount">-0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute> -->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
	
	<xsl:element name="head">
		<xsl:attribute name="id">action</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>
		<xsl:attribute name="velocity">1.6</xsl:attribute>		
		<xsl:attribute name="amount">0.1</xsl:attribute>
		<xsl:attribute name="repeats">2</xsl:attribute>
		<xsl:attribute name="start">anticipation:relax</xsl:attribute>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>

	<xsl:element name="head">
		<xsl:attribute name="id">overshoot</xsl:attribute>	
		<xsl:attribute name="type">SHAKE</xsl:attribute>		
		<xsl:attribute name="amount">0.03</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:attribute name="start">action:relax</xsl:attribute>	
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>
</xsl:template>

<!-- Move to right (orient) -->
<xsl:template name="head_to_right">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">ORIENT</xsl:attribute>
		<xsl:attribute name="amount">0.15</xsl:attribute>
		<xsl:attribute name="angle">0.5</xsl:attribute>
		<xsl:attribute name="direction">RIGHT</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- Moved to left (orient) -->
<xsl:template name="head_to_left">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">ORIENT</xsl:attribute>
		<xsl:attribute name="amount">0.15</xsl:attribute>
		<xsl:attribute name="angle">0.5</xsl:attribute>
		<xsl:attribute name="direction">LEFT</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!--Head up (orient) -->
<xsl:template name="head_up">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">ORIENT</xsl:attribute>
		<xsl:attribute name="amount">0.15</xsl:attribute>
		<xsl:attribute name="angle">0.5</xsl:attribute>
		<xsl:attribute name="direction">UP</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- Head down (orient) -->
<xsl:template name="head_down">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">ORIENT</xsl:attribute>
		<xsl:attribute name="amount">0.15</xsl:attribute>
		<xsl:attribute name="angle">0.5</xsl:attribute>
		<xsl:attribute name="direction">DOWN</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- Head tilt to right -->
<!-- <head type="TOSS" amount="-0.25" repeats="0.5" start="0" ready="0.5" relax="2.5" end="2.8"/> -->
<xsl:template name="head_tilt_right">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">TOSS</xsl:attribute>
		<xsl:attribute name="amount">-0.25</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:choose>
			<xsl:when test="$speech_id!=''">	
				<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>		-->	
				<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+2</xsl:attribute>	
				<xsl:attribute name="end"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+2.3</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>				
				<xsl:attribute name="relax">2.5</xsl:attribute>
				<xsl:attribute name="end">2.8</xsl:attribute>	
			</xsl:otherwise>
		</xsl:choose>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- Head tilt to left -->
<!-- <head type="TOSS" amount="0.25" repeats="0.5" start="0" ready="0.5" relax="2.5" end="2.8"/> -->
<xsl:template name="head_tilt_left">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="head">
		<xsl:attribute name="type">TOSS</xsl:attribute>
		<xsl:attribute name="amount">0.25</xsl:attribute>
		<xsl:attribute name="repeats">0.5</xsl:attribute>
		<xsl:choose>
			<xsl:when test="$speech_id!=''">	
				<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>		-->	
				<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+2</xsl:attribute>	
				<xsl:attribute name="end"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+2.3</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>				
				<xsl:attribute name="relax">2.5</xsl:attribute>
				<xsl:attribute name="end">2.8</xsl:attribute>	
			</xsl:otherwise>
		</xsl:choose>		
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!--  _________________________________eyebrow movements_________________________________  -->
<!-- frown -->
<xsl:template name="brow_frown">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">4</xsl:attribute> 
		<xsl:attribute name="amount">0.8</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>	
</xsl:template>

<!-- raise -->
<xsl:template name="brow_raise">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="type">facs</xsl:attribute>
		<xsl:attribute name="au">2</xsl:attribute>
		<xsl:attribute name="amount">1.0</xsl:attribute>
		<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>
		<xsl:attribute name="side">both</xsl:attribute>
		<xsl:attribute name="sbm:smooth">1</xsl:attribute>
		<!--<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>-->
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
	</xsl:element>		
</xsl:template>


<!--  _________________________________ mouth movements_________________________________  -->
<!-- smile -->
<xsl:template name="smile">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="face">
		<xsl:attribute name="type">MOUTH</xsl:attribute>
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>-->
		<xsl:attribute name="relax"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$relax_time" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="shape">smile</xsl:attribute>
	</xsl:element>	
</xsl:template>

<!--  _________________________________ gaze movements_________________________________  -->
<!-- gaze aversion downleft -->
<xsl:template name="gaze_aversion_downleft">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="gaze">
		<xsl:attribute name="target">captain-kirk</xsl:attribute>
		<xsl:attribute name="direction">DOWNLEFT</xsl:attribute>
		<xsl:attribute name="angle">20</xsl:attribute>		
	</xsl:element>	
</xsl:template>

<!-- rio_laine -->
<xsl:template name="rio_laine">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="ready_time" />
	<xsl:param name="relax_time" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="gaze">
		<xsl:attribute name="target"><xsl:value-of select="./@target" /></xsl:attribute>	
	<!--	<xsl:attribute name="concat('sbm','&#58;','joint-range'">CHEST EYES</xsl:attribute>	
		<xsl:element name="sbm&#58;head"><xsl:attribute name="pitch">-20</xsl:attribute></xsl:element>
		<xsl:element name="sbm&#58;chest"><xsl:attribute name="pitch">60</xsl:attribute></xsl:element>	-->
	</xsl:element>	
</xsl:template>

<!--  _________________________________ animations_________________________________  -->
<!-- first_vp_gesture animation -->
<xsl:template name="ani_first_VP">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute> -->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('first_VP', $posture, $participant )" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>


<!-- you_gesture animation -->
<xsl:template name="ani_you">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 

	<xsl:variable name="character">
		<xsl:value-of select="preceding::node()/@id" />
	</xsl:variable>
				
	<xsl:element name="animation">
		<!--<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>  -->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>		
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('you_animation', $posture, $participant )" /></xsl:attribute> 
	<!--	<xsl:attribute name="name">
			<xsl:choose>
				<xsl:when test="preceding::node()/@id='doctor'">
					<xsl:value-of select="$ani_negation_doctor" />
				</xsl:when>
				<xsl:otherwise> <xsl:value-of select="$ani_you_doctor" /></xsl:otherwise>
			</xsl:choose>			
		</xsl:attribute>
	-->	
	</xsl:element>	
</xsl:template>

<!-- me_gesture animation -->
<xsl:template name="ani_me">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute> -->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('me_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- negation_gesture animation -->
<xsl:template name="ani_negation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 

	<xsl:variable name="character">
		<xsl:value-of select="preceding::node()/@id" />
	</xsl:variable>
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>			
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('negation_animation', $posture, $participant )" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- contrast_gesture animation -->
<xsl:template name="ani_contrast">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('contrast_animation', $posture, $participant )" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- assumption_gesture animation -->
<xsl:template name="ani_assumption">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute> -->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('assumption_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- rhetorical_gesture animation -->
<xsl:template name="ani_rhetorical">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute> -->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('rhetorical_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- inclusivity_gesture animation -->
<xsl:template name="ani_inclusivity">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('inclusivity_animation', $posture, $participant )" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- question_gesture animation -->
<xsl:template name="ani_question">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('question_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- obligation_gesture animation -->
<xsl:template name="ani_obligation">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('obligation_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- greeting_gesture animation -->
<xsl:template name="ani_greeting">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('greeting_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>


<!-- right_gesture animation -->
<xsl:template name="ani_right">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('right_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- left_gesture animation -->
<xsl:template name="ani_left">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('left_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- chop_gesture animation -->
<xsl:template name="ani_chop">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('chop_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>


<!-- rubneck_gesture animation -->
<xsl:template name="ani_rubneck">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('rubneck_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>


<!-- rubneckloop_gesture animation -->
<xsl:template name="ani_rubneckloop">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('rubneckloop_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- rubhead_gesture animation -->
<xsl:template name="ani_rubhead">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('rubhead_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- rubheadloop_gesture animation -->
<xsl:template name="ani_rubheadloop">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('rubheadloop_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- contemplate_gesture animation -->
<xsl:template name="ani_contemplate">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('contemplate_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- contemplateloop_gesture animation -->
<xsl:template name="ani_contemplateloop">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('contemplateloop_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- dismissrarm_gesture animation -->
<xsl:template name="ani_dismissrarm">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('dismissrarm_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>


<!-- grabchinloop_gesture animation -->
<xsl:template name="ani_grabchinloop">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('grabchinloop_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

<!-- horizontal_gesture animation -->
<xsl:template name="ani_horizontal">
	<xsl:param name="participant" />
	<xsl:param name="speech_id" />
	<xsl:param name="start_time" />
	<xsl:param name="stroke" />	
	<xsl:param name="priority" />
	<xsl:param name="posture" /> 
	
	<xsl:element name="animation">
		<!-- <xsl:attribute name="ready"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$start_time" /></xsl:attribute>-->
		<xsl:attribute name="stroke"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$stroke" /></xsl:attribute>
		<xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
		<xsl:attribute name="name"> <xsl:value-of select="external:get-animation('horizontal_animation', $posture, $participant)" /></xsl:attribute> 
	</xsl:element>	
</xsl:template>

  <!-- gaze: cursory -->
  <xsl:template name="gaze_cursory">
    <xsl:param name="participant" />
    <xsl:param name="speech_id" />
    <xsl:param name="ready_time" />
    <xsl:param name="relax_time" />
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />
    <xsl:param name="prev_target" />

    <xsl:element name="gaze">
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK EYES</xsl:attribute>
      <xsl:attribute name="start"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>
      <xsl:attribute name="end"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+1.5</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="target">
        <xsl:value-of select="$prev_target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK EYES</xsl:attribute>
      <xsl:attribute name="start"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+1.5</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>
  </xsl:template>
  
  <!-- gaze: saccade -->
  <xsl:template name="gaze_saccade">
    <xsl:param name="participant" />
    <xsl:param name="speech_id" />
    <xsl:param name="ready_time" />
    <xsl:param name="relax_time" />
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />

    <xsl:element name="gaze">
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="direction">RIGHT</xsl:attribute>
      <xsl:attribute name="angle">5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="start"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" /></xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>
	<xsl:element name="gaze">
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="direction">LEFT</xsl:attribute>
      <xsl:attribute name="angle">5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="start"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+0.5</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>
	<xsl:element name="gaze">
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="start"><xsl:value-of select="$speech_id" />:<xsl:value-of select="$ready_time" />+1.0</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- gaze: idle_gaze_aversion -->
  <xsl:template name="idle_gaze_aversion">
    <xsl:param name="participant" />
    <xsl:param name="priority" />
    <xsl:param name="target" />
    <xsl:param name="posture" />
    <xsl:variable name="offset">
      <xsl:value-of select="external:get-animation('idle_gaze', $posture, $participant)" />
    </xsl:variable>

    <xsl:element name="gaze">
      <xsl:attribute name="target">
        <xsl:value-of select="$target" />
      </xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="$priority" />
      </xsl:attribute>
      <xsl:attribute name="direction">
        <xsl:value-of select="$offset" />
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="$offset='POLAR 0'">
          <xsl:attribute name="angle">0</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
  
  
  <!--listener's feedback-->
  <!--Generic behavior-->
  <!--attend nod:atd_nod-->
  <!-- <head id="attend_h1" type="NOD" repeats="0.5" amount="1" velocity="3.5" sbm:smooth="0.35" 
  start="0.0" ready="0.2" relax="0.28" end="0.7"/>-->
  <xsl:template name="atd_nod">
    <xsl:param name="participant" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />

    <xsl:element name="head">      
      <xsl:attribute name="id">atd_h1</xsl:attribute>
      <xsl:attribute name="type">NOD</xsl:attribute>
      <xsl:attribute name="amount">1</xsl:attribute>
      <xsl:attribute name="repeats">0.5</xsl:attribute>
      <xsl:attribute name="velocity">3.5</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.35</xsl:attribute> 
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="ready">0.2</xsl:attribute>
      <xsl:attribute name="relax">0.28</xsl:attribute>
      <xsl:attribute name="end">0.7</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>
  </xsl:template>
  
  <!--attend gaze:atd_gaze-->
  <!--<gaze id="g1" target="user" sbm:roll = "5" start="attend_h1:start" end="attend_h1:end" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="50 50 20">
	 <sbm:head pitch="-5" heading="-8"/>
    </gaze>-->  
  <xsl:template name="atd_gaze">
    <xsl:param name="participant" />
    <xsl:param name="id" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />

    <xsl:element name="gaze">
      <xsl:attribute name="id">atd_g1</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">3</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">300 300 80</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>      
      <xsl:element name="sbm:head">
        <xsl:attribute name="pitch">-5</xsl:attribute>
        <xsl:attribute name="heading">-8</xsl:attribute>
      </xsl:element> 
    </xsl:element>
  </xsl:template>
  
  <!--Understanding : understand nod + inner & outer brow raiser + head tilt-->
  <!-- 
  <head id="ud_h1" type="NOD" repeats="1" amount="1" velocity="3" sbm:smooth="0.6" start="0" ready="0.7" relax="1.0" end="2.0"/>
  <face id="agree_f1" type="facs" au="101" side="left" amount="0.2" start="attend_h2:start" end="attend_h2:end" sbm:rampup ="0.33" sbm:duration="1.0" sbm:rampdown ="0.4"/>
  <face id="agree_f1" type="facs" au="101" side="right" amount="0.2" start="attend_h2:start" end="attend_h2:end" sbm:rampup ="0.33" sbm:duration="1.0" sbm:rampdown ="0.4"/>
  <face id="agree_f2" type="facs" au="102" side="left" amount="0.6" start ="agree_f1:start" end ="agree_f1:end" sbm:rampup ="0.33" sbm:rampdown ="0.4"/>
  <face id="agree_f2" type="facs" au="102" side="right" amount="0.6" start ="agree_f1:start" end ="agree_f1:end" sbm:rampup ="0.33" sbm:rampdown ="0.4"/>
  
  <gaze id="g1" target="user" sbm:roll = "-5" start="attend_h2:start" end="attend_h2:end" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="50 50 20">
    <sbm:head pitch="-5" heading="8"/>
  </gaze>
  <gaze id="g2" target="user" start="agree_f2:end" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="50 50 20">
    <sbm:head pitch="0" heading="0"/>
  </gaze>
  -->
   <xsl:template name="understanding">
    <xsl:param name="participant" />
    <xsl:param name="priority" />
    <xsl:param name="posture" />

    <xsl:element name="head">      
      <xsl:attribute name="id">ud_h1</xsl:attribute>
      <xsl:attribute name="type">NOD</xsl:attribute>
      <xsl:attribute name="amount">1</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="velocity">3</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.6</xsl:attribute> 
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="ready">0.7</xsl:attribute>
      <xsl:attribute name="relax">1.0</xsl:attribute>
      <xsl:attribute name="end">2.0</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
    </xsl:element>

     <xsl:element name="face">
       <xsl:attribute name="id">agree_f1</xsl:attribute>
       <xsl:attribute name="type">facs</xsl:attribute>
       <xsl:attribute name="au">1</xsl:attribute>
       <xsl:attribute name="amount">0.2</xsl:attribute> 
       <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
       <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
       <xsl:attribute name="sbm:duration">1.0</xsl:attribute>
       <xsl:attribute name="start">ud_h1:start</xsl:attribute>       
       <xsl:attribute name="end">ud_h1:end</xsl:attribute>
       <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
     </xsl:element>
     
     <xsl:element name="face">
       <xsl:attribute name="id">agree_f2</xsl:attribute>
       <xsl:attribute name="type">facs</xsl:attribute>
       <xsl:attribute name="au">2</xsl:attribute>
       <xsl:attribute name="amount">0.6</xsl:attribute> 
       <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
       <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
       <xsl:attribute name="sbm:duration">1.0</xsl:attribute>
       <xsl:attribute name="start">agree_f1:start</xsl:attribute>       
       <xsl:attribute name="end">agree_f1:end</xsl:attribute>
       <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
     </xsl:element>
  </xsl:template>

   <!--continue attend glance:atd_glance-->
  <!-- <gaze id="atd_gl1" target="ranger" start="0.0" end="0.8" sbm:roll = "3" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="950 1000 340"/>
   <gaze id="atd_gl2" target="user" start="atd_gl1:end+0.5" end="atd_gl1:end+1.5" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="800 800 300"/>
   -->  
  <xsl:template name="atd_glance">
    <xsl:param name="participant" />
    <xsl:param name="id" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />
    <xsl:param name="prev_target" />

    <xsl:element name="gaze">
      <xsl:attribute name="id">atd_gl1</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">3</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">950 1000 340</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="end">0.8</xsl:attribute>   
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">atd_gl2</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$prev_target" /></xsl:attribute>      
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">800 800 300</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
       <xsl:attribute name="start">atd_gl1:end+0.5</xsl:attribute>
       <xsl:attribute name="end">atd_gl1:end+1.5</xsl:attribute> 
    </xsl:element>
  </xsl:template>
  
     <!--Gather information glance:gi_glance-->
  <!-- 
  gaze at target->vertical saccade scan target->gaze back to prev_target
 <gaze id="gi_gl1" target="cur_target" start="0.0" end="0.8" sbm:roll="2" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="950 1000 340"/>
<gaze id="gi_gl2" target="cur_target" start="gi_gl1:end+0.2" end="gi_gl1:end+0.23" sbm:roll="2" sbm:joint-range="EYES" sbm:joint-speed="700 700 100" direction="DOWNLEFT" angle="3"/>
<gaze id="gi_gl3" target="cur_target" start="gi_gl2:end" end="gi_gl2:end+0.03" sbm:roll="2" sbm:joint-range="EYES" sbm:joint-speed="700 700 100" direction="DOWN" angle="5"/>
<gaze id="gi_gl4" target="cur_target" start="gi_gl3:end+0.2" end="gi_gl3:end+0.45" sbm:roll="2" sbm:joint-range="EYES" sbm:joint-speed="700 700 50"/>
<gaze id="gi_gl5" target="prev_target" start="gi_gl4:end+0.5" end="gi_gl4:end+1.5" sbm:joint-range="NECK HEAD EYES" sbm:joint-speed="700 700 200"/>
-->
  <xsl:template name="gi_glance">
    <xsl:param name="participant" />
    <xsl:param name="id" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />
    <xsl:param name="prev_target" />

    <xsl:element name="gaze">
      <xsl:attribute name="id">gi_gl1</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">2</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">950 1000 340</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="end">0.8</xsl:attribute>   
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">gi_gl2</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>      
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
       <xsl:attribute name="sbm:joint-speed">700 700 100</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">gi_gl1:end+0.2</xsl:attribute>
      <xsl:attribute name="end">gi_gl1:end+0.23</xsl:attribute> 
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">gi_gl3</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>      
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
       <xsl:attribute name="sbm:joint-speed">700 700 100</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">gi_gl2:end</xsl:attribute>
      <xsl:attribute name="end">gi_gl2:end+0.03</xsl:attribute> 
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">gi_gl4</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>      
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
       <xsl:attribute name="sbm:joint-speed">700 700 50</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">gi_gl3:end+0.2</xsl:attribute>
      <xsl:attribute name="end">gi_gl3:end+0.45</xsl:attribute> 
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">gi_gl5</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">gi_gl4:end+0.5</xsl:attribute>
      <xsl:attribute name="end">gi_gl4:end+1.5</xsl:attribute>   
    </xsl:element>
  </xsl:template>
  
      <!--Gather information furtive glance:gi_furtive_glance-->
  <!-- 
  <gaze id="gi_fgl1" target="cur_target" start="0" end="0.3" sbm:joint-range="EYES" direction="UP" angle="3" sbm:joint-speed="700 700 200"/>
<gaze id="gi_fgl2" target="cur_target" start="gi_fgl1:end" end="gi_gl1:end+0.03" sbm:joint-range="EYES" direction="DOWNRIGHT" angle="3" sbm:joint-speed="700 700 200"/>
<gaze id="gi_fgl3" target="cur_target" start="gi_fgl2:end" end="gi_gl2:end+0.2" sbm:joint-range="EYES" direction="DOWN" angle="5" sbm:joint-speed="700 700 200"/>
<gaze id="gi_fgl4" target="cur_target" start="gi_fgl3:end" end="gi_gl3:end+0.3" sbm:joint-range="EYES" sbm:joint-speed="700 700 200"/>
<gaze id="gi_fgl5" target="prev_target" start="gi_fgl4:end+0.05" end="gi_gl4:end+0.4" sbm:joint-range="EYES" sbm:joint-speed="700 700 200"/>
   -->  
  <xsl:template name="gi_furtive_glance">
    <xsl:param name="participant" />
    <xsl:param name="id" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />
    <xsl:param name="prev_target" />

    <xsl:element name="gaze">
      <xsl:attribute name="id">gi_fgl1</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="end">0.3</xsl:attribute>   
    </xsl:element>
    
    <xsl:element name="gaze">
      <xsl:attribute name="id">gi_fgl2</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="direction">DOWNRIGHT</xsl:attribute>
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="start">gi_fgl1:end</xsl:attribute>
      <xsl:attribute name="end">gi_fgl1:end+0.03</xsl:attribute>   
    </xsl:element>    
    
    <xsl:element name="gaze">
      <xsl:attribute name="id">gi_fgl3</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="direction">DOWN</xsl:attribute>
      <xsl:attribute name="angle">5</xsl:attribute>
      <xsl:attribute name="start">gi_fgl2:end</xsl:attribute>
      <xsl:attribute name="end">gi_fgl2:end+0.2</xsl:attribute>   
    </xsl:element>
    
    <xsl:element name="gaze">
      <xsl:attribute name="id">gi_fgl4</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">gi_fgl3:end</xsl:attribute>
      <xsl:attribute name="end">gi_fgl3:end+0.3</xsl:attribute>   
    </xsl:element>
    
     <xsl:element name="gaze">
      <xsl:attribute name="id">gi_fgl5</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$prev_target" /></xsl:attribute>      
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
       <xsl:attribute name="start">gi_gl4+0.05</xsl:attribute>
       <xsl:attribute name="end">gi_gl4+0.4</xsl:attribute> 
    </xsl:element>
  </xsl:template>
  
   <!-- Think: thk_gaze_cursory-->
  <!-- 
<gaze id="thk_gl1" target="cur_target" start="0" end="0.3" direction="UP" angle="3" sbm:roll="-5" sbm:joint-range="HEAD EYES" sbm:joint-speed="100 100 200">
<sbm:head pitch="2" heading="-8"/>
</gaze>
<gaze id="thk_gl2" target="cur_target" start="thk_gl1:end" end="gi_gl1:end+0.2" sbm:joint-range="EYES" direction="POLAR 45" angle="10"/>
<gaze id="thk_gl3" target="cur_target" start="thk_gl2:end" end="gi_gl2:end+0.2" sbm:joint-range="EYES" direction="POLAR 60" angle="15"/>
<gaze id="thk_gl4" target="cur_target" start="thk_gl3:end+0.5" end="gi_gl3:end+0.8" sbm:joint-range="EYES" direction="POLAR 45" angle="7">
<sbm:eyes heading="-8"/>
</gaze>
<gaze id="thk_gl5" target="cur_target" start="thk_gl4:end" end="gi_gl4:end+0.2" sbm:joint-range="EYES" direction="POLAR 60" angle="15"/>
<gaze id="thk_gl6" target="cur_target" start="thk_gl5:end+0.5" end="gi_gl5:end+0.8" sbm:joint-range="EYES" direction="POLAR 80" angle="15"/>
<gaze id="thk_gl7" target="cur_target" start="thk_gl6:end+1" end="gi_gl6:end+1.5" sbm:joint-range="HEAD EYES" sbm:joint-speed="50 50 25"/>
   -->  
  <xsl:template name="thk_gaze_cursory">
    <xsl:param name="participant" />
    <xsl:param name="id" /> 
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" /> 

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl1</xsl:attribute>   
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">-5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">700 700 200</xsl:attribute>
      <xsl:attribute name="direction">UP</xsl:attribute>
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="end">0.3</xsl:attribute>
      <xsl:element name="sbm:eyes">
        <xsl:attribute name="pitch">-2</xsl:attribute>
        <xsl:attribute name="heading">-10</xsl:attribute>
      </xsl:element>
      <xsl:element name="sbm:head">
        <xsl:attribute name="pitch">2</xsl:attribute>
        <xsl:attribute name="heading">-8</xsl:attribute>
      </xsl:element>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl2</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="direction">POLAR 45</xsl:attribute>
      <xsl:attribute name="angle">10</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">thk_gl1:end</xsl:attribute>
      <xsl:attribute name="end">thk_gl1:end+0.2</xsl:attribute>
    </xsl:element>
    
    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl3</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute> 
      <xsl:attribute name="direction">POLAR 60</xsl:attribute>
      <xsl:attribute name="angle">15</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /> </xsl:attribute>
      <xsl:attribute name="start">thk_gl2:end</xsl:attribute>
      <xsl:attribute name="end">thk_gl2:end+0.2</xsl:attribute> 
    </xsl:element> 

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl4</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="direction">POLAR 45</xsl:attribute>
      <xsl:attribute name="angle">7</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">thk_gl3:end+0.5</xsl:attribute>
      <xsl:attribute name="end">thk_gl3:end+0.8</xsl:attribute>
      <xsl:element name="sbm:eyes">
        <xsl:attribute name="heading">-8</xsl:attribute>
      </xsl:element>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl5</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="direction">POLAR 60</xsl:attribute>
      <xsl:attribute name="angle">15</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">thk_gl4:end</xsl:attribute>
      <xsl:attribute name="end">thk_gl4:end+0.2</xsl:attribute>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl6</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:joint-range">EYES</xsl:attribute>
      <xsl:attribute name="direction">POLAR 80</xsl:attribute>
      <xsl:attribute name="angle">15</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">thk_gl5:end+0.5</xsl:attribute>
      <xsl:attribute name="end">thk_gl5:end+0.8</xsl:attribute> 
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">thk_gl7</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute> 
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">50 50 40</xsl:attribute> 
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">thk_gl6:end+1</xsl:attribute>
      <xsl:attribute name="end">thk_gl6:end+1.5</xsl:attribute>
    </xsl:element> 
  </xsl:template>
  
  <!--Partial Understanding-->
  <!--<head id="h1" type="NOD" repeats="1" amount="1" velocity="3" sbm:smooth="0.6" start="0" ready="0.7" relax="1.0" end="2.0"/>
<face id ="f1" type="facs" au="4" amount="0.5" start="h1:start" relax="h1:relax"
    sbm:rampup ="0.2"  sbm:rampdown ="0.4"/>
<gaze id="g1" target="cur_target" sbm:roll = "-5" start="h1:start" end="h1:end" sbm:joint-range="HEAD EYES" sbm:joint-speed="50 50 20">
  <sbm:head pitch="-5"/>
</gaze>
<gaze id="g2" target="cur_target" start="g1:end" sbm:joint-range="HEAD EYES" sbm:joint-speed="50 50 20"/>
-->
  <xsl:template name="partial_understand">
    <xsl:param name="participant" />
    <xsl:param name="id" />
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />  

    <xsl:element name="head">
      <xsl:attribute name="id">pu_h1</xsl:attribute>
      <xsl:attribute name="type">NOD</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="amount">0.5</xsl:attribute>
      <xsl:attribute name="velocity">3</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="ready">0.7</xsl:attribute>
      <xsl:attribute name="relax">1.0</xsl:attribute>
      <xsl:attribute name="end">2.0</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.6</xsl:attribute>
    </xsl:element>
    
    <xsl:element name="face">
      <xsl:attribute name="id">pu_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">4</xsl:attribute>
      <xsl:attribute name="amount">0.5</xsl:attribute>
      <xsl:attribute name="start">pu_h1:start</xsl:attribute>
      <xsl:attribute name="relax">pu_h1:relax</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">pu_g1</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">-5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">50 50 20</xsl:attribute>
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">pu_f1:start</xsl:attribute>
      <xsl:attribute name="end">pu_f1:relax</xsl:attribute>
      <xsl:element name="sbm:head">
        <xsl:attribute name="pitch">-5</xsl:attribute>
      </xsl:element>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">pu_g2</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="$target" />
      </xsl:attribute>
      <xsl:attribute name="sbm:roll">-5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">50 50 20</xsl:attribute>
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="$priority" />
      </xsl:attribute>
      <xsl:attribute name="start">pu_g1:end+0.2</xsl:attribute>
      <xsl:attribute name="relax">pu_f1:end</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Confusion-->
  <!--<face id ="f1" type="facs" au="4" amount="0.5" start="0" relax="1.8"  
    sbm:rampup ="0.2"  sbm:rampdown ="0.4"/>
<head id="h1" type="SHAKE" repeats="1" amount="0.5" velocity="3" sbm:smooth="0.85" start="f1:ready" ready="f1:ready+0.75" relax="f1:ready+0.8" end="f1:ready+1.0"/>
<head id ="h2" type="SHAKE" repeats="1" amount="0.15" velocity="2.5" sbm:smooth="0.85" start="h1:end" ready="h1:end+0.15" relax="h1:end+0.3" end="h1:end+0.5"/>
<gaze id="g1" target="ranger" direction="RIGHT" sbm:roll = "-5" start="f1:start" end="f1:relax" sbm:joint-range="HEAD EYES" sbm:joint-speed="50 50 20">
  <sbm:head pitch="-5" heading="0"/>
</gaze>
<gaze id="g2" target="ranger" direction="RIGHT" start="g1:end" relax="f1:end" sbm:joint-range="HEAD EYES" sbm:joint-speed="50 50 20"/>-->
  <xsl:template name="confusion">
    <xsl:param name="participant" />
    <xsl:param name="id" />
    <xsl:param name="priority" />
    <xsl:param name="posture" />
    <xsl:param name="target" />

    <xsl:element name="face">
      <xsl:attribute name="id">cfs_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">4</xsl:attribute>
      <xsl:attribute name="amount">0.5</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="relax">1.8</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
    </xsl:element>

    <xsl:element name="head">
      <xsl:attribute name="id">cfs_h1</xsl:attribute>
      <xsl:attribute name="type">SHAKE</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="amount">0.5</xsl:attribute>
      <xsl:attribute name="velocity">3</xsl:attribute>
      <xsl:attribute name="start">cfs_f1:ready</xsl:attribute>
      <xsl:attribute name="ready">cfs_f1:ready+0.75</xsl:attribute>
      <xsl:attribute name="relax">cfs_f1:ready+0.8</xsl:attribute>
      <xsl:attribute name="end">cfs_f1:ready+1</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.85</xsl:attribute> 
    </xsl:element>

    <xsl:element name="head">
      <xsl:attribute name="id">cfs_h2</xsl:attribute>
      <xsl:attribute name="type">SHAKE</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="amount">0.15</xsl:attribute>
      <xsl:attribute name="velocity">2.5</xsl:attribute>
      <xsl:attribute name="start">cfs_h1:end</xsl:attribute>
      <xsl:attribute name="ready">cfs_f1:end+0.15</xsl:attribute>
      <xsl:attribute name="relax">cfs_f1:end+0.3</xsl:attribute>
      <xsl:attribute name="end">cfs_f1:end+5</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.85</xsl:attribute>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="id">cfs_g1</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="$target" />
      </xsl:attribute>
      <xsl:attribute name="sbm:roll">-5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">50 50 20</xsl:attribute> 
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">cfs_f1:start</xsl:attribute>
      <xsl:attribute name="end">cfs_f1:relax</xsl:attribute>
      <xsl:element name="sbm:head">
        <xsl:attribute name="pitch">-5</xsl:attribute> 
      </xsl:element>
    </xsl:element> 
    
    <xsl:element name="gaze">
      <xsl:attribute name="id">cfs_g2</xsl:attribute>
      <xsl:attribute name="target"><xsl:value-of select="$target" /></xsl:attribute>
      <xsl:attribute name="sbm:roll">-5</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="sbm:joint-speed">50 50 20</xsl:attribute> 
      <xsl:attribute name="angle">3</xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="$priority" /></xsl:attribute>
      <xsl:attribute name="start">cfs_g1:end+0.2</xsl:attribute>
      <xsl:attribute name="relax">cfs_f1:end</xsl:attribute> 
    </xsl:element>
  </xsl:template>

  <!--Emotion feedback-->
  <!--Joy-->
  <!--  <face id="joy_f1" type="facs" au="12" amount="1" start="0" ready="0.5" relax="2.5" end="3.5" sbm:rampup ="0.5" sbm:rampdown ="0.8"/>
<face id="joy_f2" type="facs" au="6" amount="0.2" start="joy_f1:start-0.05" ready="joy_f1:ready" relax="joy_f1:relax" end="joy_f1:end+0.03" sbm:rampup ="0.2" sbm:rampdown ="2.03"/>
  <face id="joy_f3" type="facs" au="27" amount="0.1" start ="joy_f1:start+0.12" relax ="joy_f1:relax+0.15" sbm:rampup ="0.33" sbm:rampdown ="0.45"/>
  <face id="joy_f4" type="facs" au="1" amount="0.15" start="joy_f1:start+0.1" relax="joy_f1:relax+0.15" sbm:rampup ="0.33" sbm:duration="1.0" sbm:rampdown ="0.4"/>
  <face id="joy_f5" type="facs" au="2" amount="0.6" start ="joy_f4:start" relax ="joy_f4:relax" sbm:rampup ="0.33" sbm:rampdown ="0.4"/>-->

  <xsl:template name="emo-joy">
    <xsl:element name="face">
      <xsl:attribute name="id">joy_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">12</xsl:attribute>
      <xsl:attribute name="amount">1</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.5</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.8</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="ready">0.5</xsl:attribute>
      <xsl:attribute name="relax">2.5</xsl:attribute>
      <xsl:attribute name="end">3.5</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">joy_f2</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">6</xsl:attribute>
      <xsl:attribute name="amount">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">2.03</xsl:attribute>
      <xsl:attribute name="start">joy_f1:start-0.05</xsl:attribute>
      <xsl:attribute name="ready">joy_f1:ready</xsl:attribute>
      <xsl:attribute name="relax">joy_f1:relax</xsl:attribute>
      <xsl:attribute name="end">joy_f1:end+0.03</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">joy_f3</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">27</xsl:attribute>
      <xsl:attribute name="amount">0.1</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.45</xsl:attribute>
      <xsl:attribute name="start">joy_f1:start+0.12</xsl:attribute>
      <xsl:attribute name="relax">joy_f1:relax+0.15</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">joy_f4</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">1</xsl:attribute>
      <xsl:attribute name="amount">0.15</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
      <xsl:attribute name="sbm:duration">1.0</xsl:attribute>
      <xsl:attribute name="start">joy_f1:start+0.1</xsl:attribute>
      <xsl:attribute name="relax">joy_f1:relax+0.15</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">joy_f5</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">2</xsl:attribute>
      <xsl:attribute name="amount">0.6</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
      <xsl:attribute name="sbm:duration">1.0</xsl:attribute>
      <xsl:attribute name="start">joy_f4:start</xsl:attribute>
      <xsl:attribute name="relax">joy_f4:relax</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Fear-->

  <!--Surprise-->
  <!--   <face id="surprise_f1" type="facs" au="1" start="0" ready="0.33" relax="1" end="1.3" amount="0.1" sbm:rampup ="0.33" sbm:rampdown ="0.4"/>
      <face id="surprise_f2" type="facs" au="2" amount="0.5" start ="surprise_f1:start" relax="surprise_f1:relax" end ="surprise_f1:end" sbm:rampup ="0.33" sbm:rampdown ="0.4"/>
      <face id="surprise_f3" type="facs" au="5" amount="0.25" start ="surprise_f1:start+0.03" relax="surprise_f1:relax-0.01" end ="surprise_f1:end-0.01" sbm:rampup ="0.25" sbm:rampdown ="0.25"/>
    <face id="surprise_f4" type="facs" au="25" amount="0.1" start="surprise_f1:start+0.2" relax="surprise_f1:relax" end="surprise_f1:end-0.05" sbm:rampup ="0.3" sbm:rampdown ="0.5"/>
    <face id="surprise_f5" type="facs" au="27" amount="0.05" start="surprise_f1:start+0.2" relax="surprise_f1:relax" end="surprise_f1:end-0.1" sbm:rampup ="0.3" sbm:rampdown ="0.5"/>  
    -->
  <xsl:template name="emo-surprise">
    <xsl:element name="face">
      <xsl:attribute name="id">surprise_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">1</xsl:attribute>
      <xsl:attribute name="amount">0.1</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="ready">0.33</xsl:attribute>
      <xsl:attribute name="relax">1.0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">surprise_f2</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">2</xsl:attribute>
      <xsl:attribute name="amount">0.5</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.4</xsl:attribute>
      <xsl:attribute name="start">surprise_f1:start</xsl:attribute>
      <xsl:attribute name="relax">surprise_f1:relax</xsl:attribute>
      <xsl:attribute name="end">surprise_f1:end</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">surprise_f3</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">5</xsl:attribute>
      <xsl:attribute name="amount">0.25</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.25</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.25</xsl:attribute>
      <xsl:attribute name="start">surprise_f1:start+0.03</xsl:attribute>
      <xsl:attribute name="relax">surprise_f1:relax-0.01</xsl:attribute>
      <xsl:attribute name="end">surprise_f1:end</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">surprise_f4</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">25</xsl:attribute>
      <xsl:attribute name="amount">0.1</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.3</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.5</xsl:attribute>
      <xsl:attribute name="start">surprise_f1:start+0.2</xsl:attribute>
      <xsl:attribute name="relax">surprise_f1:relax</xsl:attribute>
      <xsl:attribute name="end">surprise_f1:end-0.05</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">surprise_f5</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">27</xsl:attribute>
      <xsl:attribute name="amount">0.05</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.3</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.5</xsl:attribute>
      <xsl:attribute name="start">surprise_f1:start+0.2</xsl:attribute>
      <xsl:attribute name="relax">surprise_f1:relax</xsl:attribute>
      <xsl:attribute name="end">surprise_f1:end-0.1</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Disgust-->

  <!--Anger-->

  <!--Sadness-->
  <!--<face type="facs" id ="sad_f1" au="1" amount="0.7" start ="0" ready="1.03"  relax="3.5" 
 sbm:rampup ="0.33" sbm:rampdown ="0.5"/>
<face type="facs" id ="sad_f2" au="4" amount="0.2" start ="sad_f1:start"  ready = "sad_f1:relax"
 sbm:rampup ="0.4" sbm:rampdown ="0.5"/> -->
  <xsl:template name="emo-sadness">
    <xsl:element name="face">
      <xsl:attribute name="id">sad_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">1</xsl:attribute>
      <xsl:attribute name="amount">0.7</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.33</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.5</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="ready">1.03</xsl:attribute>
      <xsl:attribute name="relax">3.5</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">sad_f2</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">4</xsl:attribute>
      <xsl:attribute name="amount">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.4</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">0.5</xsl:attribute>
      <xsl:attribute name="start">sad_f1:start</xsl:attribute>
      <xsl:attribute name="relax">sad_f1:relax</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Attitude Feedback-->
  <!--Agree-->
  <!--<head id="agree_h1" type="NOD" repeats="1.5" amount="0.8" velocity="3" sbm:smooth="0.7" start="0" ready="0.25" relax="0.8" end="1.5"/>-->
  <xsl:template name="attitude-agree">
    <xsl:element name="head">
      <xsl:attribute name="id">agree_h1</xsl:attribute>
      <xsl:attribute name="type">NOD</xsl:attribute>
      <xsl:attribute name="amount">0.8</xsl:attribute>
      <xsl:attribute name="repeats">1.5</xsl:attribute>
      <xsl:attribute name="velocity">3</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.7</xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="ready">0.25</xsl:attribute>
      <xsl:attribute name="relax">0.8</xsl:attribute>
      <xsl:attribute name="end">1.5</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Disagree-->
  <!--<gaze id="disagree_g1" target="foo"/>
<head id="h1" type ="shake" repeats="1" amount ="0.55" velocity ="1" sbm:smooth="0.4"/>
<head id="h2" type ="shake" repeats="1" amount ="0.4" velocity ="1.5" sbm:smooth="0.7" start = "h1:end-0.1"/>-->
  <xsl:template name="attitude-disagree">
    <xsl:param name="target" />
    <xsl:element name="gaze">
      <xsl:attribute name="id">disagree_g1</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="$target" />
      </xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="end">0.5</xsl:attribute>
    </xsl:element>

    <xsl:element name="head">
      <xsl:attribute name="id">disagree_h1</xsl:attribute>
      <xsl:attribute name="type">SHAKE</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="amount">0.55</xsl:attribute>
      <xsl:attribute name="velocity">1</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.4</xsl:attribute>
      <xsl:attribute name="start">disagree_g1:end</xsl:attribute>
    </xsl:element>

    <xsl:element name="head">
      <xsl:attribute name="id">disagree_h2</xsl:attribute>
      <xsl:attribute name="type">SHAKE</xsl:attribute>
      <xsl:attribute name="repeats">1</xsl:attribute>
      <xsl:attribute name="amount">0.4</xsl:attribute>
      <xsl:attribute name="velocity">1.5</xsl:attribute>
      <xsl:attribute name="sbm:smooth">0.7</xsl:attribute>
      <xsl:attribute name="start">disagree_h1:end-0.1</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Like-->
  <!--<face id="like_f1" type="facs" au="12" amount="1" start="0" ready="0.8" relax="2.0" end="3.0" sbm:rampup ="0.6" sbm:rampdown ="1.0"/>
  <face id="like_f2" type="facs" au="6" amount="0.2" start="like_f1:start-0.05" ready="like_f1:ready" relax="like_f1:relax" end="like_f1:end+0.02" sbm:rampup ="0.2" sbm:rampdown ="1.03"/>
-->
  <xsl:template name="attitude-like">
    <xsl:element name="face">
      <xsl:attribute name="id">like_f1</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">12</xsl:attribute>
      <xsl:attribute name="amount">1</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.6</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">1</xsl:attribute>
      <xsl:attribute name="start">0</xsl:attribute>
      <xsl:attribute name="ready">0.8</xsl:attribute>
      <xsl:attribute name="relax">2.0</xsl:attribute>
      <xsl:attribute name="end">3.0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="face">
      <xsl:attribute name="id">like_f2</xsl:attribute>
      <xsl:attribute name="type">facs</xsl:attribute>
      <xsl:attribute name="au">6</xsl:attribute>
      <xsl:attribute name="amount">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampup">0.2</xsl:attribute>
      <xsl:attribute name="sbm:rampdown">1.03</xsl:attribute>
      <xsl:attribute name="start">like_f1:start-0.05</xsl:attribute>
      <xsl:attribute name="ready">like_f1:ready</xsl:attribute>
      <xsl:attribute name="relax">like_f1:relax</xsl:attribute>
      <xsl:attribute name="end">like_f1:end+0.03</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--Dislike-->

  <!--Interested-->

  <!--NotInterested-->

  <!--Transition Feedback-->
  <!--EnterGroup-->
  <!--ExitGroup-->
<!--add more feedback rule here-->

  <!--Gaze Behavior-->
  <!-- gaze focus -->
  <!-- <gaze angle="0" direction="POLAR 0" id="focus_g1" priority="4" target="target"/> -->
  <xsl:template name="gaze_focus">
    <xsl:param name="id" />
    <xsl:param name="target" />
    <xsl:param name="track" />
    <xsl:element name="gaze">
      <xsl:attribute name="id">focus_g1</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="./@target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- gaze weak focus -->
  <!-- <gaze angle="0" direction="POLAR 0" id="focus_g1" priority="4" target="target"/> -->
  <xsl:template name="gaze_weak_focus">
    <xsl:param name="id" />
    <xsl:param name="target" />
    <xsl:element name="gaze">
      <xsl:attribute name="id">weakfocus_g1</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="./@target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- gaze look -->
  <!-- <gaze angle="0" direction="POLAR 0" id="focus_g1" priority="4" target="target"/> -->
  <xsl:template name="gaze_look">
    <xsl:param name="id" />
    <xsl:param name="target" />
    <xsl:element name="gaze">
      <xsl:attribute name="id">weakfocus_g1</xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="./@target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!--gaze avert-->
  <xsl:template name="gaze_avert">
    <xsl:param name="id" />
    <xsl:param name="target" />
    <xsl:param name="track" />


    <xsl:element name="gaze">
      <xsl:attribute name="id">avert_g1</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">NECK HEAD EYES</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="./@target" />
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="$track='eyes-offset'">
          <xsl:attribute name="direction">POLAR 260</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='eyes'">
          <xsl:attribute name="direction">POLAR 100</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='down'">
          <xsl:attribute name="direction">POLAR 180</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='sideways-down'">
          <xsl:attribute name="direction">POLAR 135</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='up'">
          <xsl:attribute name="direction">POLAR 0</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='sideways-up'">
          <xsl:attribute name="direction">POLAR 45</xsl:attribute>
          <xsl:attribute name="angle">20</xsl:attribute>
        </xsl:when>
        <xsl:when test="$track='other'">
          <xsl:attribute name="direction">POLAR 100</xsl:attribute>
          <xsl:attribute name="angle">10</xsl:attribute>
        </xsl:when>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <!--gaze cursory-->
  <xsl:template name="gaze_cursory_2">
    <xsl:param name="id" />
    <xsl:param name="target" />
    <xsl:param name="track" />
    <xsl:param name="prev_target" />

    <xsl:element name="gaze">
      <xsl:attribute name="target">
        <xsl:value-of select="$target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="start">0.0</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>

    <xsl:element name="gaze">
      <xsl:attribute name="target">
        <xsl:value-of select="$prev_target" />
      </xsl:attribute>
      <xsl:attribute name="direction">POLAR 0</xsl:attribute>
      <xsl:attribute name="angle">0</xsl:attribute>
      <xsl:attribute name="sbm:joint-range">HEAD EYES</xsl:attribute>
      <xsl:attribute name="start">1.5</xsl:attribute>
      <xsl:attribute name="priority">
        <xsl:value-of select="./@priority" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

    <!-- add new rule here -->


</xsl:transform>