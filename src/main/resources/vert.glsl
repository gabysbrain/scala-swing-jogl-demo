
#version 120

attribute vec3 vertex;
attribute vec3 normal;
attribute vec3 color;

uniform vec2 scale;
uniform vec3 rotation;

varying vec4 fragPosition;
varying vec3 fragColor;
varying vec4 fragNormal;

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
    cos(angle), -sin(angle), 0.0, 0.0,
    sin(angle), cos(angle), 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0);
}

void main() {

  mat4 scale = mat4(mat2(scale.x, 0.0, 0.0, scale.y)) *
               mat4(0.5, 0.0, 0.0, 0.0,
                    0.0, 0.5, 0.0, 0.0,
                    0.0, 0.0, 0.5, 0.0,
                    0.0, 0.0, 0.0, 1.0);

  // convert the quaternion to opengl matrix
  //mat4 rotate = q2mtx(rotationQ);
  vec3 rotation = rotation / 360.0 * 2.0 * 3.14159;
  mat4 rotate = rotX(rotation.x) * rotY(rotation.y) * rotZ(rotation.z);

  mat4 trans = scale * rotate;
  mat4 normTrans = mat4(transpose(mat3(rotate)));

  fragColor = color;
  fragNormal = normTrans * vec4(normal, 1.0);
  fragPosition = trans * vec4(vertex, 1.0);
  gl_Position = fragPosition;
}

