
#version 120

varying vec4 fragPosition;
varying vec3 fragColor;
varying vec3 fragNormal;

void main() {
  // Just using diffuse light and light color is always white
  vec3 direction = normalize(vec3(2.0, 2.0, -2.0) - fragPosition.xyz);
  float diffuse = max(0.0, dot(fragNormal, direction));
  float ambient = 0.3;
  gl_FragColor = vec4(min((ambient + diffuse) * fragColor, vec3(1.0)), 1.0);
}

