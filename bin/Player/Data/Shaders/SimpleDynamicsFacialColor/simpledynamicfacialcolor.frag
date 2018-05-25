uniform sampler2D textureMap;
uniform sampler2D baseTexture;
uniform sampler2D surpriseTexture;
uniform sampler2D smileTexture;
uniform sampler2D sadTexture;
uniform sampler2D neutralTexture;
uniform sampler2D fearTexture;
uniform sampler2D exerciseTexture;
uniform sampler2D disgustTexture;
uniform sampler2D alcoholTexture;
uniform sampler2D angerTexture;


uniform float id;

/*
uniform int textureIndex[13];
uniform float textureValue[13];
uniform int nbTextureApplied;
*/
varying vec4 P;
varying vec3 N;
varying vec4 pEye;
varying vec4 p;


const int lightNumber = 2;


void main (void) {

	vec4  tex = texture2D(textureMap, gl_TexCoord[0].st);
	vec4  base = texture2D(baseTexture, gl_TexCoord[0].st);
	vec4  surprise = texture2D(surpriseTexture, gl_TexCoord[0].st);
	vec4  smile = texture2D(smileTexture, gl_TexCoord[0].st);
	vec4  sad = texture2D(sadTexture, gl_TexCoord[0].st);
	vec4  neutral = texture2D(neutralTexture, gl_TexCoord[0].st);
	vec4  fear = texture2D(fearTexture, gl_TexCoord[0].st);
	vec4  exercise = texture2D(exerciseTexture, gl_TexCoord[0].st);
	vec4  disgust = texture2D(disgustTexture, gl_TexCoord[0].st);
	vec4  alcohol = texture2D(alcoholTexture, gl_TexCoord[0].st);
	vec4  anger = texture2D(angerTexture, gl_TexCoord[0].st);
	
	//compose expression
	vec4 express;
	{
		express = alcohol;
	}
	vec4 diffuseMap;
    vec4 specularMap;
	vec3 n = normalize(gl_NormalMatrix * N);
	for(int il = 0 ; il <lightNumber; ++ il){
        vec3 lv =  gl_LightSource[il].position.xyz- pEye.xyz;
        vec3 l = normalize(lv);
        float diffuse   = max (dot (l, n), 0.0);

        vec3 v = normalize (-pEye.xyz);
        vec3 h = normalize(l+v);

        float spec = max(dot(n, h), 0.0);

        //spec = pow (spec, gl_FrontMaterial.shininess);
		spec = pow (spec, 16);
        spec = max (0.0, spec);

       diffuseMap += (diffuse * gl_LightSource[il].diffuse);
	    //diffuseMap += (diffuse);
        //specularMap += spec * gl_LightSource[il].specular;
		specularMap += spec * vec4(0.5,0.5,0.5,1);
    }

    diffuseMap *= 1;
    specularMap *= 0.1;//gl_FrontMaterial.specular;
	
	//gl_FragData[0] = vec4(tex* (diffuseMap  + vec4(0.3)));
	gl_FragData[0] = vec4((diffuseMap * 1 + vec4(0.5)) * base * express) + specularMap;
	//gl_FragData[0] =  specularMap;
	//gl_FragData[0] = vec4(base * express);
	//gl_FragData[0] = vec4(tex*diffuseMap*base * express);
   
}
