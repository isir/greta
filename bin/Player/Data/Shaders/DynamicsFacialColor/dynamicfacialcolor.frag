uniform sampler2D textureMap;
uniform sampler3D scaleTex;
uniform sampler3D biasTex;
uniform sampler2D melaninTex;
uniform sampler2D hemoglobinMeanFreeTex;
uniform sampler2D skinTex;
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


const int lightNumber = 3;

vec2 Align(vec2 pos) {
    float w, h;
    w = 512;
	h = 256;
    vec2 pixelOffset = vec2(1.0 / w, 1.0 / h) / 2.0;
    return pos * vec2(w - 1, h - 1) / vec2(w, h) + pixelOffset;
}



float CalculateHemoglobin(vec2 texcoord) {
    float scale = 0.0;
    float bias = 0.0;
//texture3D(bumpMap, vec3(st,  depth) );
    //for (int i = 0; i < 9; i++) {
        scale += 1 * texture3D(scaleTex, vec3(texcoord, 0.25)).x; 
        bias += 1 * texture3D(biasTex, vec3(texcoord, 0.25)).x;
    //}

    float mf = texture2D(hemoglobinMeanFreeTex, texcoord).x; 
    return mf * scale + bias;
}




vec4 CalculateAlbedo(vec2 texcoord, float melaninScale, float hemoglobinScale, float melaninBias, float hemoglobinBias) {
    float haeScale = 0.0129 * hemoglobinScale;
    float haeOffset =-0.00145 + hemoglobinBias;
    float melScale = 0.015 * melaninScale;
    float melOffset = -0.0022 + melaninBias;
        
    float melanin = clamp(melScale * texture2D(melaninTex, texcoord).x + melOffset, 0.0, 0.5);
    float melanin_lookup = pow(2.0 * melanin, 1.0 / 3.0);

    float hemoglobin = clamp(haeScale * CalculateHemoglobin(texcoord) + haeOffset, 0.0,  0.32);
    float hemoglobin_lookup = pow(3.125 * hemoglobin, 1.0 / 3.0);

    vec2 alignedLookup = Align(vec2(melanin_lookup, hemoglobin_lookup));
    vec4 color = 1.3 * texture2D(skinTex, alignedLookup);
        
	
    color.rgb = pow(color.rgb, 4.84);

    color.rgb = pow(color.rgb, 1.0 / 2.2);
    color.rgb = clamp(color.rgb, 0.0, 1.0);
    color.rgb = pow(color.rgb, 2.2);

    return 2.0 * color;
}




void main (void) {

	vec4  tex = texture2D(textureMap, gl_TexCoord[0].st);
	float melaninScale = 0.8; 
	float hemoglobinScale = 1.3;
	float melaninBias = 0.0021;
    float hemoglobinBias = 0;
    tex = tex *( vec4(1)+ CalculateAlbedo(gl_TexCoord[0].st, melaninScale, hemoglobinScale, melaninBias, hemoglobinBias) * 1 / 3.1415926f * 5);
	
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

        spec = pow (spec,gl_FrontMaterial.shininess);
        spec = max (0.0, spec);

       diffuseMap += (diffuse * gl_LightSource[il].diffuse);

        /* specularMap += spec * gl_LightSource[il].specular;*/
    }

    diffuseMap *= gl_FrontMaterial.diffuse * tex;
    specularMap *= gl_FrontMaterial.specular;
	
    //gl_FragData[0] = CalculateAlbedo(gl_TexCoord[0].st, melaninScale, hemoglobinScale, melaninBias, hemoglobinBias) * 1 / 3.1415926f;
	gl_FragData[0] = vec4(diffuseMap+(gl_LightModel.ambient * gl_FrontMaterial.ambient * tex)+specularMap);
	//gl_FragData[0] = texture3D(scaleTex, vec3(gl_TexCoord[0].st, 0.25));
}
