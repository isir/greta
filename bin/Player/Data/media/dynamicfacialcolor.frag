#version 150 compatibility
#extension GL_EXT_gpu_shader4: enable
uniform sampler2D textureMap;
uniform sampler2D normalMap;
uniform sampler3D bumpMap;
uniform float id;

uniform int textureIndex[13];
uniform float textureValue[13];
uniform int nbTextureApplied;


varying vec4 P;
varying vec3 N;
varying vec4 pEye;
varying vec4 p;

const int lightNumber = 3;




vec2 Align(vec2 pos, Texture2D tex) {
    float w, h;
    skinTex.GetDimensions(w, h);
    vec2 pixelOffset = vec2(1.0 / w, 1.0 / h) / 2.0;
    return pos * vec2(w - 1, h - 1) / vec2(w, h) + pixelOffset;
}


float CalculateHemoglobin(vec2 texcoord) {
    float scale = 0.0;
    float bias = 0.0;

    for (int i = 0; i < 9; i++) {
        scale += shapeWeights[i] * scaleTex[i].Sample(AnisotropicSampler16, texcoord);
        bias += shapeWeights[i] * biasTex[i].Sample(AnisotropicSampler16, texcoord);
    }

    float mf = hemoglobinMeanFreeTex.Sample(AnisotropicSampler16, texcoord);
    return mf * scale + bias;
}


vec4 CalculateAlbedo(vec2 texcoord, float melaninScale, float hemoglobinScale, float melaninBias, float hemoglobinBias) {
    float haeScale = 0.0129 * hemoglobinScale;
    float haeOffset =-0.00145 + hemoglobinBias;
    float melScale = 0.015 * melaninScale;
    float melOffset = -0.0022 + melaninBias;
        
    float melanin = clamp(melScale * melaninTex.Sample(AnisotropicSampler16, texcoord) + melOffset, 0.0, 0.5);
    float melanin_lookup = pow(2.0 * melanin, 1.0 / 3.0);

    float hemoglobin = clamp(haeScale * CalculateHemoglobin(texcoord) + haeOffset, 0.0,  0.32);
    float hemoglobin_lookup = pow(3.125 * hemoglobin, 1.0 / 3.0);

    vec2 alignedLookup = Align(vec2(melanin_lookup, hemoglobin_lookup), skinTex);
    vec4 color = 1.3 * skinTex.Sample(LinearSampler, alignedLookup);
        
    //color.rgb *= color.rgb;
    color.rgb = pow(color.rgb, 4.84);

    vec4 hair = hairTex.Sample(AnisotropicSampler16, texcoord);
    color.rgb = pow(color.rgb, 1.0 / 2.2);
    color.rgb -= 0.7 * vec3(0.66, 1.0, 1.0) * (1.0 * vec3(0.82, 1.0, 1.0) * hair.rgb * hair.a + 1.0 * vec3(2.0 * 0.57, 1.0, 1.0) * hair.rgb * (1.0 - hair.a));
    color.rgb = clamp(color.rgb, 0.0, 1.0);
    color.rgb = pow(color.rgb, 2.2);

    return 2.0 * color;
}




void main (void) {
    vec4  tex = texture2D(textureMap, gl_TexCoord[0].st);

    vec3 n = normalize(gl_NormalMatrix * N);
    vec4  normalV = texture2D(normalMap, gl_TexCoord[0].st);

    gl_FragData[3] = vec4(pEye);
    gl_FragData[4] = vec4(n, id);
    gl_FragData[1] = vec4(gl_LightModel.ambient * gl_FrontMaterial.ambient * tex);
    vec4 diffuseMap;
    vec4 specularMap;

    vec3 normPerturb;

    float height = getHeightFromBumpMap(gl_TexCoord[0].st);
    normPerturb = PerturbNormal (pEye.xyz , n ,  height);
    n = normPerturb;

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

        specularMap += spec * gl_LightSource[il].specular;
    }

    diffuseMap *= gl_FrontMaterial.diffuse * tex;
    specularMap *= gl_FrontMaterial.specular;

    gl_FragData[0] = vec4(diffuseMap+(gl_LightModel.ambient * gl_FrontMaterial.ambient * tex)+specularMap);
}
