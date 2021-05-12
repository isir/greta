<xsl:transform  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="2.0" >		
 
	<xsl:include href="nvb_rules.xsl" />
	
	<xsl:template match="fml" >
	<!--	<xsl:copy-of select="./turn" />
		<xsl:copy-of select="./affect" /> -->							
	</xsl:template>	
	
	<!-- <xsl:template match="bml" priority="10"> -->
	<xsl:template match="bml" priority="10">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>	
			<xsl:for-each select="//rule">							
				<xsl:call-template name="fire_rules" />
			</xsl:for-each>
			<xsl:for-each select="//feedbackrule">
				<xsl:call-template name="fire_feedbacks" />
			</xsl:for-each>
			<xsl:for-each select="//gazereason">
				<xsl:call-template name="fire_gazes" />
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- process parsed_result-->	
	<xsl:template match="parsed_result" >
	<xsl:text>&#10;	</xsl:text>

	<!-- print parsed_result
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>  
	<xsl:text>&#10;	</xsl:text> -->
	
		<!--<xsl:element name="required">-->
		<xsl:element name="speech">
			<xsl:attribute name="id"><xsl:value-of select="./@id" /></xsl:attribute>
			<xsl:attribute name="ref"><xsl:value-of select="./@ref" /></xsl:attribute>
			<xsl:attribute name="type">application/ssml+xml</xsl:attribute> 
			<xsl:text>&#10;	</xsl:text>
			<xsl:for-each select="//mark">	
				<xsl:copy-of select="."/>		
				<xsl:choose>
					<xsl:when test="name(following-sibling::node()[1])='rule'">
						<xsl:value-of select="following-sibling::node()[2]/text()" />
					</xsl:when>
					<xsl:otherwise>						
						<xsl:value-of select="following-sibling::node()[1]/text()" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&#10;	</xsl:text>
			</xsl:for-each>
		</xsl:element>				
		<xsl:text>&#10;	</xsl:text>

		<!--</xsl:element>
		<xsl:text>&#10;	</xsl:text> -->
	</xsl:template>
	
	<!-- omit these nodes -->
	<xsl:template match="speech" />
  <xsl:template match="request" />
  <xsl:template match="status" />
  <xsl:template match="marked_sentence" /> 
  <xsl:template match="declaration" /> 

  <!-- copy all the other nodes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>

	</xsl:template>

	<!-- *******************************************************************************************************************
	+ Rules to fire
	******************************************************************************************************************* -->
	<!-- template fire_rules -->
	<xsl:template name="fire_rules">		
	
		<!-- get the rule type, speech id,  time markers for ready and relax -->		
		<xsl:variable name="rule_name">
			<xsl:value-of select="./@type" />
		</xsl:variable>
		
		<xsl:variable name="priority">
			<xsl:value-of select="./@priority" />
		</xsl:variable> 		
		
		<xsl:variable name="sp_id">
			<xsl:value-of select="ancestor::node()/@id" />
		</xsl:variable>

    <xsl:variable name="utter_id">
      <xsl:value-of select="./@utt_id" />
    </xsl:variable>

    <xsl:variable name="start"/> 
    <xsl:variable name="ready">
			<xsl:value-of select="following::mark[1]/@name" />
		</xsl:variable> 		
		
		<xsl:variable name="relax">
			<xsl:choose> 
				<xsl:when test="string-length(following-sibling::mark[3]/@name)!=0">
					<xsl:value-of select="following::mark[3]/@name" />  
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="following::mark[2]/@name" /> 
				</xsl:otherwise>
			</xsl:choose> 
			
		</xsl:variable>	
		<xsl:variable name="ani_start">			
			<xsl:choose>
				<xsl:when test="string-length(preceding::mark[4]/@name)=0">T0</xsl:when>
				<xsl:otherwise><xsl:value-of select="preceding::mark[4]/@name"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="ani_stroke">			
			<xsl:choose>
				<xsl:when test="$ani_start='T0'">T3</xsl:when>
				<xsl:otherwise><xsl:value-of select="$ready"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

    <xsl:variable name="end"/> 
 
    
		<xsl:text>&#10;	</xsl:text>	
		<!-- call the appropriate template -->
		<xsl:choose>
			<xsl:when test="$rule_name='affirmation'">
			
			<xsl:element name="rule">			
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="affirmation" >		
					<xsl:with-param name="participant" select="./@participant"/>			
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />		
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
			</xsl:element>
			</xsl:when>
					
			<xsl:when test="$rule_name='inclusivity'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="inclusivity" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>			
			
			<xsl:when test="$rule_name='intensification'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="intensification" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				
				</xsl:element>
			</xsl:when>
			
			<xsl:when test="$rule_name='contrast'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="contrast" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>			
			
			<xsl:when test="$rule_name='reponse_req'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="response_req" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
			</xsl:element>
			</xsl:when>
			
			<xsl:when test="$rule_name='word_search'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="word_search" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />		
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>			
			
			<xsl:when test="$rule_name='listing'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="listing">
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>
			
			<xsl:when test="$rule_name='assumption'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="assumption">
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>			
			
			<xsl:when test="$rule_name='possibility'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="possibility" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />		
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>
						
			<xsl:when test="$rule_name='first_NP'"> 	
			<xsl:if test="string-length($ready)!=0">			
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>	
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
					<xsl:call-template name="first_NP" >	
					<xsl:with-param name="participant" select="./@participant"/>							
						<xsl:with-param name="speech_id" select="$sp_id" />
						<xsl:with-param name="ready_time" select="$ready" />					 
						<xsl:with-param name="relax_time" select="$relax" />			
						<xsl:with-param name="priority" select="./@priority" />	
						<xsl:with-param name="posture" select="./@pose"/>
					</xsl:call-template>		
					</xsl:element>
				</xsl:if>					
			</xsl:when>
			
			<xsl:when test="$rule_name='noun_phrase'">	
				<xsl:if test="string-length($ready)!=0">			
				<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>	
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
					<xsl:call-template name="noun_phrase" >	
					<xsl:with-param name="participant" select="./@participant"/>							
						<xsl:with-param name="speech_id" select="$sp_id" />
						<xsl:with-param name="ready_time" select="$ready" />					 
						<xsl:with-param name="relax_time" select="$relax" />	
						<xsl:with-param name="priority" select="./@priority" />	
						<xsl:with-param name="posture" select="./@pose"/>
					</xsl:call-template>		
					</xsl:element>				
				</xsl:if>
			</xsl:when>				
			
			<xsl:when test="$rule_name='interjection'">	
			<xsl:if test="string-length($ready)!=0">			
				<xsl:element name="rule">
				<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
				<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
				<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>	
				<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
				<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
				<xsl:text>&#10;	</xsl:text>	
		
				<xsl:call-template name="interjection" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />	
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>		
				</xsl:element>				
			</xsl:if>
			</xsl:when>		
			
			<xsl:when test="$rule_name='compound_sentence'">	
			<xsl:if test="string-length($ready)!=0">			
				<xsl:element name="rule">
				<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
				<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>          
				<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
				<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
				<xsl:attribute name="priority"> <xsl:value-of select="./@priority" /></xsl:attribute>
				<xsl:attribute name="target"><xsl:value-of select="./@target" /></xsl:attribute>
				<xsl:text>&#10;	</xsl:text>

				<xsl:call-template name="compound_sentence" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />
					<xsl:with-param name="posture" select="./@pose"/>
					<xsl:with-param name="target" select="./@target"/>
				</xsl:call-template>		
				</xsl:element>			
			</xsl:if>
			</xsl:when>							
			
			<xsl:when test="$rule_name='emo_positive'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="emo_positive" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='emo_negative'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="emo_negative" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='gaze_aversion'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="gaze_aversion" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="ready_time" select="$ready" />					 
					<xsl:with-param name="relax_time" select="$relax" />
					<xsl:with-param name="priority" select="./@priority" />	
					<xsl:with-param name="posture" select="./@pose"/>
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='first_VP'">	
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
				
				<xsl:call-template name="first_VP" >		
					<xsl:with-param name="participant" select="./@participant"/>						
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>				
					<xsl:with-param name="stroke" select="$ani_stroke" />
					<xsl:with-param name="priority" select="./@priority" />										
					<xsl:with-param name="posture" select="./@pose"/>			 
				</xsl:call-template>	
				</xsl:element>									
			</xsl:when>	
		
		
			<xsl:when test="$rule_name='me_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="me_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />			
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>					
				</xsl:call-template>		
				</xsl:element>
			</xsl:when>	
			
			<xsl:when test="$rule_name='you_animation'">	
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>	
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
				
				<xsl:call-template name="you_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>				
					<xsl:with-param name="stroke" select="$ani_stroke" />
					<xsl:with-param name="priority" select="./@priority" />								
					<xsl:with-param name="posture" select="./@pose"/>		
				</xsl:call-template>	
				</xsl:element>									
			</xsl:when>		
			
			<xsl:when test="$rule_name='negation_animation'">	
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>	
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="negation_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				 
				</xsl:call-template>					
				</xsl:element>								
			</xsl:when>		
			
			<xsl:when test="$rule_name='contrast_animation'">			
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>	
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="contrast_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />	
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>						 
				</xsl:call-template>			
				</xsl:element>							
			</xsl:when>		
			
			<xsl:when test="$rule_name='assumption_animation'">	
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>			
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="assumption_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />	
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>					 
				</xsl:call-template>				
				</xsl:element>						
			</xsl:when>	
			
			<xsl:when test="$rule_name='rhetorical_animation'">				
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="rhetorical_animation" >	
					<xsl:with-param name="participant" select="./@participant"/>							
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>					 
				</xsl:call-template>		
				</xsl:element>								
			</xsl:when>	
			
			<xsl:when test="$rule_name='inclusivity_animation'">		
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="inclusivity_animation" >
					<xsl:with-param name="participant" select="./@participant"/>								
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>							 
				</xsl:call-template>		
				</xsl:element>								
			</xsl:when>	
			
			<xsl:when test="$rule_name='question_animation'">		
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="question_animation" >		
					<xsl:with-param name="participant" select="./@participant"/>						
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>									 
				</xsl:call-template>		
				</xsl:element>								
			</xsl:when>

			<xsl:when test="$rule_name='obligation_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="obligation_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>	
			
			<xsl:when test="$rule_name='greeting_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="greeting_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>	

			 
		

			<xsl:when test="$rule_name='right_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="right_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='left_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="left_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>
		
			<xsl:when test="$rule_name='chop_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="chop_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='rubneck_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="rubneck_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='rubneckloop_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="rubneckloop_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='rubhead_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="rubhead_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='rubheadloop_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="rubheadloop_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>			
			
			<xsl:when test="$rule_name='contemplate_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="contemplate_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='contemplateloop_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="contemplateloop_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='dismissrarm_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="dismissrarm_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='dismissrarm_gazeavert_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="dismissrarm_gazeavert_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='grabchinloop_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="grabchinloop_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

			<xsl:when test="$rule_name='horizontal_animation'">
			<xsl:element name="rule">
			<xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
			<xsl:attribute name="ready"><xsl:value-of select="$ani_start" /></xsl:attribute>
			<xsl:attribute name="relax"><xsl:value-of select="$ani_stroke" /></xsl:attribute>		
			<xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
			<xsl:text>&#10;	</xsl:text>	
			
				<xsl:call-template name="horizontal_animation" >
					<xsl:with-param name="participant" select="./@participant"/>
					<xsl:with-param name="speech_id" select="$sp_id" />
					<xsl:with-param name="start_time" select="$ani_start"/>
					<xsl:with-param name="stroke" select="$ani_stroke" />		
					<xsl:with-param name="priority" select="./@priority" />			
					<xsl:with-param name="posture" select="./@pose"/>				
				</xsl:call-template>
				</xsl:element>
			</xsl:when>

      <xsl:when test="$rule_name='intensification'">
        <xsl:element name="rule">
          <xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
          <xsl:attribute name="ready">
            <xsl:value-of select="$ready" />
          </xsl:attribute>          
          <xsl:attribute name="type">
            <xsl:value-of select="$rule_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="intensification" >
            <xsl:with-param name="participant" select="./@participant"/>
            <xsl:with-param name="speech_id" select="$sp_id" />
            <xsl:with-param name="ready_time" select="$ready" />
            <xsl:with-param name="relax_time" select="$relax" />
            <xsl:with-param name="priority" select="./@priority" />
            <xsl:with-param name="posture" select="./@pose"/>
          </xsl:call-template>

        </xsl:element>
      </xsl:when>

      <xsl:when test="$rule_name='fmlbml_gaze'">        
        <xsl:element name="rule">
          <xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
          <xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>          
          <xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
          <xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
          <xsl:attribute name="priority"> <xsl:value-of select="./@priority" /></xsl:attribute>
          <xsl:attribute name="target"><xsl:value-of select="./@target" /></xsl:attribute>
          <xsl:attribute name="prev_target"><xsl:value-of select="./@prev_target" /></xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="fmlbml_gaze" >
            <xsl:with-param name="participant" select="./@participant"/>
            <xsl:with-param name="speech_id" select="$sp_id" />
            <xsl:with-param name="ready_time" select="$ready" />
            <xsl:with-param name="relax_time" select="$relax" />
            <xsl:with-param name="priority" select="./@priority" />
            <xsl:with-param name="posture" select="./@pose"/>
            <xsl:with-param name="target" select="./@target"/>
            <xsl:with-param name="prev_target" select="./@prev_target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <xsl:when test="$rule_name='idle_gaze'">
        <xsl:element name="rule">
          <xsl:attribute name="participant"><xsl:value-of select="./@participant" /></xsl:attribute>
          <xsl:attribute name="ready"><xsl:value-of select="$ready" /></xsl:attribute>
          <xsl:attribute name="relax"><xsl:value-of select="$relax" /></xsl:attribute>
          <xsl:attribute name="type"><xsl:value-of select="$rule_name" /></xsl:attribute>
          <xsl:attribute name="priority"><xsl:value-of select="./@priority" /></xsl:attribute>
          <xsl:attribute name="speaker"><xsl:value-of select="./@speaker" /></xsl:attribute>
          <xsl:attribute name="listener"><xsl:value-of select="./@listener" /></xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="idle_gaze" >
            <xsl:with-param name="participant" select="./@participant"/>
            <xsl:with-param name="priority" select="./@priority" />
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>
      
     
  
      <!-- add new rule here -->
      
    </xsl:choose>
	</xsl:template>


  <!-- *******************************************************************************************************************
	+ Feedback Rules to fire
	******************************************************************************************************************* -->
  <!-- template fire_rules -->
  <xsl:template name="fire_feedbacks">

    <!-- get the rule type,   time markers for start, ready, relax and end -->
    <xsl:variable name="feedback_name">
      <xsl:value-of select="./@type" />
    </xsl:variable>
    <xsl:variable name="priority">
      <xsl:value-of select="./@priority" />
    </xsl:variable>
    <xsl:variable name="target">
      <xsl:value-of select="./@target" />
    </xsl:variable>

    <xsl:choose>

      <!--Generic feedback-->
      <!--Attend Nod-->
      <xsl:when test="$feedback_name='AttendNod'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="A-NOD" />
        </xsl:element>
      </xsl:when>

      <!--Attend Gaze-->
      <xsl:when test="$feedback_name='AttendGaze'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="A-GZ" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Specific feedback-->
      <!--Understanding Nod-->
      <xsl:when test="$feedback_name='Understand'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="U-NOD" >
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Continue Attend Glance-->
      <xsl:when test="$feedback_name='ContinueAttendGlance'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="CA-GL" >
            <xsl:with-param name="target" select="./@target"/>
            <xsl:with-param name="prev_target" select="./@prev_target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Gather Information: General Glance-->
      <xsl:when test="$feedback_name='GatherInfo-Glance'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="GI-GL" >
            <xsl:with-param name="target" select="./@target"/>
            <xsl:with-param name="prev_target" select="./@prev_target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Gather Information: Furtive Glance-->
      <xsl:when test="$feedback_name='GatherInfo-FurtiveGlance'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="GI-FGL" >
            <xsl:with-param name="target" select="./@target"/>
            <xsl:with-param name="prev_target" select="./@prev_target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Think: gaze aversion-->
      <xsl:when test="$feedback_name='Think'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="THK" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Partial Understanding-->
      <xsl:when test="$feedback_name='PartialUnderstand'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="UD-CFS" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Confusion-->
      <xsl:when test="$feedback_name='Confused'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:attribute name="emotion">
            <xsl:value-of select="./@emotion" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="CFS" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Emotion feedback-->
      <!--joy-->
      <xsl:when test="$feedback_name='joy'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="joy" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>


      <!--Fear-->

      <!--Surprise-->
      <xsl:when test="$feedback_name='surprise'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="surprise" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Disgust-->
      <!--Anger-->

      <!--Sadness-->
      <xsl:when test="$feedback_name='sadness'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="sadness" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Attitude Feedback-->
      <!--Agree-->
      <xsl:when test="$feedback_name='agree'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="agree" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>


      <!--Disagree-->
      <xsl:when test="$feedback_name='disagree'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="disagree" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Like-->
      <xsl:when test="$feedback_name='like'">
        <xsl:element name="feedback">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$feedback_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="posture">
            <xsl:value-of select="./@posture" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>

          <xsl:call-template name="like" >
            <xsl:with-param name="target" select="./@target"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--Dislike-->
      <!--Interested-->
      <!--NotInterested-->

      <!--Transition Feedback-->
      <!--EnterGroup-->
      <!--ExitGroup-->
      <!-- add new feedback rules here -->

    </xsl:choose>
  </xsl:template>

  <!-- *******************************************************************************************************************
	+ Gazes Rules to fire
	******************************************************************************************************************* -->
  <!-- template fire_gazes -->
  <xsl:template name="fire_gazes">

    <!-- get the rule type,   time markers for start, ready, relax and end -->
    <xsl:variable name="gazereason_name">
      <xsl:value-of select="./@type" />
    </xsl:variable>
    <xsl:variable name="priority">
      <xsl:value-of select="./@priority" />
    </xsl:variable>
    <xsl:variable name="target">
      <xsl:value-of select="./@target" />
    </xsl:variable>
    <xsl:variable name="prev_target">
      <xsl:value-of select="./@prev_target" />
    </xsl:variable>
    <xsl:variable name="track">
      <xsl:value-of select="$track" />
    </xsl:variable>

    <xsl:choose>
      <!--planning_speech_look_at_hearer-->
      <xsl:when test="$gazereason_name='planning_speech_look_at_hearer'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_look_at_hearer"/>
        </xsl:element>
      </xsl:when>

      <!--speaking-->
      <xsl:when test="$gazereason_name='speaking'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="speaking"/>
        </xsl:element>
      </xsl:when>

      <!--speech done-->
      <xsl:when test="$gazereason_name='speech_done'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="speech_done"/>
        </xsl:element>
      </xsl:when>

      <!--planning_speech_hold_turn-->
      <xsl:when test="$gazereason_name='planning_speech_hold_turn'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_hold_turn">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--planning_speech_rejection-->
      <xsl:when test="$gazereason_name='planning_speech_rejection'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_rejection">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--planning_speech_rejection_goal_satisfied-->
      <xsl:when test="$gazereason_name='planning_speech_rejection_goal_satisfied'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_rejection_goal_satisfied">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--planning_speech_acceptance_reluctant-->
      <xsl:when test="$gazereason_name='planning_speech_acceptance_reluctant'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_acceptance_reluctant">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--planning_speech_remembering-->
      <xsl:when test="$gazereason_name='planning_speech_remembering'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning_speech_remembering">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--speech_done_hold_turn-->
      <xsl:when test="$gazereason_name='speech_done_hold_turn'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="speech_done_hold_turn">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--listen_to_speaker-->
      <xsl:when test="$gazereason_name='listen_to_speaker'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="listen_to_speaker"/>
        </xsl:element>
      </xsl:when>

      <!--interpret_speech-->
      <xsl:when test="$gazereason_name='interpret_speech'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="interpret_speech"/>
        </xsl:element>
      </xsl:when>

      <!--expect_speech-->
      <xsl:when test="$gazereason_name='expect_speech'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="expect_speech"/>
        </xsl:element>
      </xsl:when>

      <!--expect_acknowledgment-->
      <xsl:when test="$gazereason_name='expect_acknowledgment'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="expect_acknowledgment"/>
        </xsl:element>
      </xsl:when>

      <!--expect_repair-->
      <xsl:when test="$gazereason_name='expect_repair'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="expect_repair"/>
        </xsl:element>
      </xsl:when>

      <!--planning-->
      <xsl:when test="$gazereason_name='planning'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="planning">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--avoidance_unambiguous_situation-->
      <xsl:when test="$gazereason_name='avoidance_unambiguous_situation'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="avoidance_unambiguous_situation">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--convey_displeasure_both-->
      <xsl:when test="$gazereason_name='convey_displeasure_both'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="convey_displeasure_both">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--accept_responsibility-->
      <xsl:when test="$gazereason_name='accept_responsibility'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="accept_responsibility">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--make_amends-->
      <xsl:when test="$gazereason_name='make_amends'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="make_amends">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--resignation-->
      <xsl:when test="$gazereason_name='resignation'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="resignation">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--avoidance_distancing-->
      <xsl:when test="$gazereason_name='avoidance_distancing'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="avoidance_distancing">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--avoidance_wishing_away-->
      <xsl:when test="$gazereason_name='avoidance_wishing_away'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="avoidance_wishing_away">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--convey_displeasure_single-->
      <xsl:when test="$gazereason_name='convey_displeasure_single'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="convey_displeasure_single">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--monitor_goal-->
      <xsl:when test="$gazereason_name='monitor_goal'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_goal"/>
        </xsl:element>
      </xsl:when>

      <!--monitor_goal_refresh-->
      <xsl:when test="$gazereason_name='monitor_goal_refresh'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_goal_refresh"/>
        </xsl:element>
      </xsl:when>

      <!--monitor_expected_effect-->
      <xsl:when test="$gazereason_name='monitor_expected_effect'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_expected_effect"/>
        </xsl:element>
      </xsl:when>

      <!--monitor_expected_action_other_control-->
      <xsl:when test="$gazereason_name='monitor_expected_action_other_control'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_expected_action_other_control"/>
        </xsl:element>
      </xsl:when>

      <!--attend_to_sound_object-->
      <xsl:when test="$gazereason_name='attend_to_sound_object'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="attend_to_sound_object"/>
        </xsl:element>
      </xsl:when>

      <!--monitor_expected_action_self_control-->
      <xsl:when test="$gazereason_name='monitor_expected_action_self_control'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_expected_action_self_control">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
            <xsl:with-param name="prev_target" select="./@prev_target" />
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--monitor_expected_action_other_control_negative-->
      <xsl:when test="$gazereason_name='monitor_expected_action_other_control_negative'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="monitor_expected_action_other_control_negative">
            <xsl:with-param name="track">
              <xsl:value-of select="$track" />
            </xsl:with-param>
            <xsl:with-param name="prev_target" select="./@prev_target" />
          </xsl:call-template>
        </xsl:element>
      </xsl:when>

      <!--seek_social_support-->
      <xsl:when test="$gazereason_name='seek_social_support'">
        <xsl:element name="gazereason">
          <xsl:attribute name="participant">
            <xsl:value-of select="./@participant" />
          </xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="$gazereason_name" />
          </xsl:attribute>
          <xsl:attribute name="priority">
            <xsl:value-of select="./@priority" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="./@target" />
          </xsl:attribute>
          <xsl:attribute name="track">
            <xsl:value-of select="$track" />
          </xsl:attribute>
          <xsl:text>&#10;	</xsl:text>
          <xsl:call-template name="seek_social_support"/>
        </xsl:element>
      </xsl:when>
      <!-- add new gaze rules here -->
    </xsl:choose>
  </xsl:template>
  
</xsl:transform>

