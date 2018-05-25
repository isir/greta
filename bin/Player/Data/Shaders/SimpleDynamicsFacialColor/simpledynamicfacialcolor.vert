//drawmesh pass whole scene
#version 150 compatibility
#extension GL_EXT_gpu_shader4: enable
varying vec3 N;
varying vec4 pEye;
varying vec4 p;
varying vec4 P;

void main() {
    gl_TexCoord[0]  =  gl_TextureMatrix[0] * gl_MultiTexCoord0;
    P = gl_Vertex;
 
    N = (gl_Normal);
    
    pEye = gl_ModelViewMatrix * (gl_Vertex);
    p = gl_ProjectionMatrix * pEye;

    gl_Position = ftransform();
	//float offset = pow(max(dot(normalize(gl_NormalMatrix * N), vec3(0,-1,0)), 0), 4) * 0.1;
//gl_Position = vec4(p.x,p.y - offset,p.z,p.w); ;
}
