
#version 120

attribute vec3 vertex;
attribute vec3 normal;
attribute vec3 color;

uniform vec2 scale;
uniform vec3 rotation;

varying vec4 fragPosition;
varying vec3 fragColor;
varying vec3 fragNormal;

mat4 rotX(float angle) {
  return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, cos(angle), -sin(angle), 0.0,
    0.0, sin(angle), cos(angle), 0.0,
    0.0, 0.0, 0.0, 1.0);
}

mat4 rotY(float angle) {
  return mat4(
    cos(angle), 0.0, sin(angle), 0.0,
    0.0, 1.0, 0.0, 0.0,
    -sin(angle), 0.0, cos(angle), 0.0,
    0.0, 0.0, 0.0, 1.0);
}

mat4 rotZ(float angle) {
  return mat4(
    cos(angle), sin(angle), 0.0, 0.0,
    -sin(angle), cos(angle), 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0);
}

void main() {

  mat4 scale = mat4(mat2(scale.x, 0.0, 0.0, scale.y)) *
               mat4(mat3(0.5));
  mat3 invScale = mat3(1.0/scale[0][0], 0.0, 0.0,
                       0.0, 1.0/scale[1][1], 0.0,
                       0.0, 0.0, 1.0/scale[2][2]);

  // convert degrees to radians
  vec3 rotation = rotation / 360.0 * 2.0 * 3.14159;
  mat4 rotate = rotX(rotation.x) * rotY(rotation.y) * rotZ(rotation.z);

  mat4 trans = scale * rotate;
  mat3 normTrans = transpose(transpose(mat3(rotate)) * invScale);

  fragColor = color;
  fragNormal = normalize(normTrans * normal);
  fragPosition = trans * vec4(vertex, 1.0);
  gl_Position = fragPosition;
}

