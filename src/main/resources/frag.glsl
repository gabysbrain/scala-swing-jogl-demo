
#version 120

varying vec4 fragPosition;
varying vec3 fragColor;
varying vec4 fragNormal;

void main() {
  // Just using diffuse light and light color is always white
  vec3 direction = normalize(vec3(2.0, 2.0, 2.0) - fragPosition.xyz);
  float diffuse = max(0.0, dot(normalize(fragNormal.xyz), direction)) + 0.6;
  gl_FragColor = vec4(min(diffuse * fragColor, vec3(1.0)), 1.0);
}

