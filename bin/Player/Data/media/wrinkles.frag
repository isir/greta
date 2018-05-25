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

int slidesdouble = 2;   //cause of 3d texture interpolation,  i define slides by index x 2, one slide empty one slide fill  12 filled slides into 24 slides

float getHeightFromBumpMap(vec2 st)
{	
	float height = 0;
	for(int i = 0; i < nbTextureApplied; ++i)
	{
		float depth = float( float(textureIndex[i] * slidesdouble) + 0.01) / float(13.0 * slidesdouble);
		vec4  bump = texture3D(bumpMap, vec3(st,  depth) );
		height += bump.x  * textureValue[i];
	
	}
	
	//height = texture3D(bumpMap, vec3(st,  21.0/24.0) ).x;
	return height ;
}


vec3 PerturbNormal1 ( vec3 surf_pos , vec3 surf_norm , float height )
{
	vec3 vSigmaS = dFdx ( surf_pos );
	vec3 vSigmaT = dFdy ( surf_pos );
	vec3 vN = surf_norm ; // normalized
	vec3 vR1 = cross ( vSigmaT ,vN);
	vec3 vR2 = cross (vN , vSigmaS );
	float fDet = dot ( vSigmaS , vR1 );
	float dBs = dFdx ( height );
	float dBt = dFdy ( height );
	vec3 vSurfGrad =
	sign ( fDet ) * ( dBs * vR1 + dBt * vR2 );
	return normalize ( abs ( fDet )*vN - vSurfGrad );
}


vec3 PerturbNormal2 ( vec3 surf_pos , vec3 surf_norm , float height )
{
	vec3 vSigmaS = dFdx ( surf_pos );
	vec3 vSigmaT = dFdy ( surf_pos );
	vec3 vN = surf_norm ; // normalized
	vec3 vR1 = cross ( vSigmaT ,vN);
	vec3 vR2 = cross (vN , vSigmaS );
	float fDet = dot ( vSigmaS , vR1 );
	
	
	vec2 TexDx = dFdx (gl_TexCoord[0].st);
	vec2 TexDy = dFdy (gl_TexCoord[0].st);
	vec2 STll = gl_TexCoord[0].st ;
	vec2 STlr = gl_TexCoord[0].st + TexDx ;
	vec2 STul = gl_TexCoord[0].st + TexDy ;
	float Hll = getHeightFromBumpMap( STll );
	float Hlr = getHeightFromBumpMap( STlr );
	float Hul = getHeightFromBumpMap( STul );
	float dBs = Hlr - Hll ;
	float dBt = Hul - Hll ;
	
	
	vec3 vSurfGrad =
	sign ( fDet ) * ( dBs * vR1 + dBt * vR2 );
	return normalize ( abs ( fDet )*vN - vSurfGrad );
}

vec3 PerturbNormal ( vec3 surf_pos , vec3 surf_norm , float height )
{
	return PerturbNormal2(surf_pos , surf_norm , height);
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
