
import scala.swing.{Component, Publisher}
import scala.swing.BorderPanel
import scala.swing.event.ComponentEvent
import scala.swing.event.{MouseDragged, MousePressed}

import javax.media.opengl._
import javax.media.opengl.awt.GLJPanel

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.glsl.ShaderUtil

case class AngleUpdate(source:Component, deltaX:Float, deltaX:Float)
  extends ComponentEvent

class CubePanel extends Component with Publisher {

  // openGL initialization stuff
  val glProfile = GLProfile.getDefault
  val glCapabilities = new GLCapabilities(glProfile)

  // The peer maps between scala-swing and java-swing
  override lazy val peer = new GLJPanel(glCapabilities) with SuperMixin

  // Map the openGL events to scala functions
  peer.addGLEventListener(new GLEventListener {
    def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) =
      CubePanel.this.reshape(drawable, x, y, width, height)
    def init(drawable:GLAutoDrawable) = 
      CubePanel.this.init(drawable)
    def dispose(drawable:GLAutoDrawable) = 
      CubePanel.this.dispose(drawable)
    def display(drawable:GLAutoDrawable) = 
      CubePanel.this.display(drawable)
  })

  // Rotation angles
  var xAngle = 0f
  var yAngle = 0f
  var zAngle = 0f

  // Geometry, etc for the cube
  val vertices = Array(
    -1.0f, -1.0f, -1.0f, // Back face
    -1.0f, 1.0f, -1.0f,
    1.0f, 1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    -1.0f, -1.0f, -1.0f, // Left face
    -1.0f, -1.0f, 1.0f,
    -1.0f, 1.0f, 1.0f,
    -1.0f, 1.0f, -1.0f,
    -1.0f, -1.0f, 1.0f, // Front face
    1.0f, -1.0f, 1.0f,
    1.0f, 1.0f, 1.0f,
    -1.0f, 1.0f, 1.0f,
    1.0f, -1.0f, -1.0f, // Right face
    1.0f, 1.0f, -1.0f,
    1.0f, 1.0f, 1.0f,
    1.0f, -1.0f, 1.0f,
    -1.0f, 1.0f, -1.0f, // Top face
    -1.0f, 1.0f, 1.0f,
    1.0f, 1.0f, 1.0f,
    1.0f, 1.0f, -1.0f,
    -1.0f, -1.0f, -1.0f, // Bottom face
    1.0f, -1.0f, -1.0f,
    1.0f, -1.0f, 1.0f,
    -1.0f, -1.0f, 1.0f)
  val normals = Array(
    0f, 0f, -1f, // Back face
    0f, 0f, -1f,
    0f, 0f, -1f,
    0f, 0f, -1f,
    -1f, 0f, 0f, // Left face
    -1f, 0f, 0f, 
    -1f, 0f, 0f, 
    -1f, 0f, 0f, 
    0f, 0f, 1f, // Front face
    0f, 0f, 1f,
    0f, 0f, 1f,
    0f, 0f, 1f,
    1f, 0f, 0f, // Right face
    1f, 0f, 0f,
    1f, 0f, 0f,
    1f, 0f, 0f,
    0f, 1f, 0f, // Top face
    0f, 1f, 0f,
    0f, 1f, 0f,
    0f, 1f, 0f,
    0f, -1f, 0f, // Bottom face
    0f, -1f, 0f,
    0f, -1f, 0f,
    0f, -1f, 0f)
  val colors = Array(
    0f, 0f, 1f, // Back face
    0f, 0f, 1f,
    0f, 0f, 1f,
    0f, 0f, 1f,
    1f, 0f, 0f, // Left face
    1f, 0f, 0f,
    1f, 0f, 0f,
    1f, 0f, 0f,
    0f, 1f, 0f, // Front face
    0f, 1f, 0f,
    0f, 1f, 0f,
    0f, 1f, 0f,
    1f, 1f, 0f, // Right face
    1f, 1f, 0f,
    1f, 1f, 0f,
    1f, 1f, 0f,
    1f, 0f, 1f, // Top face
    1f, 0f, 1f,
    1f, 0f, 1f,
    1f, 0f, 1f,
    0f, 1f, 1f, // Bottom face
    0f, 1f, 1f,
    0f, 1f, 1f,
    0f, 1f, 1f)
  val indices = Array(
    0, 1, 2,    // Back face
    2, 3, 0,
    4, 5, 6,    // Left face
    6, 7, 4,
    8, 9, 10,   // Front face
    10, 11, 8, 
    12, 13, 14, // Right face
    14, 15, 12,
    16, 17, 18, // Top face
    18, 19, 16,
    20, 21, 22, // Bottom face
    22, 23, 20)

  // OpenGL buffer objects
  var vertexBuffer = -1
  var normalBuffer = -1
  var colorBuffer = -1
  var indexBuffer = -1

  // Shader program
  var programId = -1

  // These determine how the cube rotates
  var rotation = Array(0f, 1f, 0f, 0f) // Start with y-axis rotation
  var scale = Array(1f, 1f) // Scaling for the screen size

  // set up the animation loop
  val animator = new FPSAnimator(peer, 60)
  animator.start

  // Set up the mouse listener here
  var dragStart = new java.awt.Point
  listenTo(mouse.moves, mouse.clicks)
  reactions += {
    case MousePressed(src, point, mods, clicks, triggersPopup) =>
      dragStart = point
    case MouseDragged(src, point, mods) => 
      // The difference is what's important
      val xDiff = point.x - dragStart.x
      val yDiff = point.y - dragStart.y

      val newXAngle = yDiff / 2f
      val newYAngle = xDiff / 2f

      // reset the drag start to the next event
      dragStart = point

      publish(AngleUpdate(this, newXAngle, newYAngle))
  }

  def init(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2ES2

    gl.glEnable(GL.GL_DEPTH_TEST)
    gl.glDepthFunc(GL.GL_LESS)

    // set the clear color once and for all
    gl.glClearColor(0f, 0f, 0f, 1f)

    // create all the openGL buffers
    {
      var tmp = Array(-1, -1, -1, -1)
      gl.glGenBuffers(tmp.size, tmp, 0)
      vertexBuffer = tmp(0)
      normalBuffer = tmp(1)
      colorBuffer = tmp(2)
      indexBuffer = tmp(3)
    }

    // Create the shader program
    programId = {
      val vertexSource = readResource("vert.glsl")
      val fragSource = readResource("frag.glsl")
      val progId = gl.glCreateProgram
      val vertShaderId = createShader(gl, GL2ES2.GL_VERTEX_SHADER, vertexSource)
      val fragShaderId = createShader(gl, GL2ES2.GL_FRAGMENT_SHADER, fragSource)
      gl.glAttachShader(progId, vertShaderId)
      gl.glAttachShader(progId, fragShaderId)
      gl.glLinkProgram(progId)
      val ok = ShaderUtil.isProgramStatusValid(gl, progId, 
                                               GL2ES2.GL_LINK_STATUS)
      if(!ok) {
        throw new GLException(ShaderUtil.getProgramInfoLog(gl, progId))
      }
      progId
    }

    // Send all the vertex data down
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer)
    val vDataBuf = Buffers.newDirectFloatBuffer(vertices)
    vDataBuf.rewind
    gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.size*Buffers.SIZEOF_FLOAT,
                    vDataBuf, GL.GL_STATIC_DRAW)

    // Send all the normal data down
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normalBuffer)
    val nDataBuf = Buffers.newDirectFloatBuffer(normals)
    nDataBuf.rewind
    gl.glBufferData(GL.GL_ARRAY_BUFFER, normals.size*Buffers.SIZEOF_FLOAT,
                    nDataBuf, GL.GL_STATIC_DRAW)
    
    // Send all the color data down
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, colorBuffer)
    val cDataBuf = Buffers.newDirectFloatBuffer(colors)
    cDataBuf.rewind
    gl.glBufferData(GL.GL_ARRAY_BUFFER, colors.size*Buffers.SIZEOF_FLOAT,
                    cDataBuf, GL.GL_STATIC_DRAW)
    
    // Send all the indices down
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
    val iDataBuf = Buffers.newDirectIntBuffer(indices)
    iDataBuf.rewind
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.size*Buffers.SIZEOF_INT,
                    iDataBuf, GL.GL_STATIC_DRAW)
  }

  def dispose(drawable:GLAutoDrawable) = {}

  def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
    // Scale to whatever is smaller
    scale = if(width > height) {
      Array(height.toFloat/width.toFloat, 1f)
    } else {
      Array(1f, width.toFloat/height.toFloat)
    }
  }

  def display(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2ES2

    // Clear the screen
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)

    // Ensure the shader is enabled
    gl.glUseProgram(programId)

    // Set up all the attribute pointers
    var vertexAttrib = gl.glGetAttribLocation(programId, "vertex")
    var normalAttrib = gl.glGetAttribLocation(programId, "normal")
    val colorAttrib = gl.glGetAttribLocation(programId, "color")
    gl.glEnableVertexAttribArray(vertexAttrib)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer)
    gl.glVertexAttribPointer(vertexAttrib, 3, GL.GL_FLOAT, false, 0, 0)
    gl.glEnableVertexAttribArray(normalAttrib)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normalBuffer)
    gl.glVertexAttribPointer(normalAttrib, 3, GL.GL_FLOAT, false, 0, 0)
    gl.glEnableVertexAttribArray(colorAttrib)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, colorBuffer)
    gl.glVertexAttribPointer(colorAttrib, 3, GL.GL_FLOAT, false, 0, 0)

    // Ensure the scale is set correctly
    gl.glUniform2fv(gl.glGetUniformLocation(programId, "scale"), 1,
                    scale, 0)

    // Send down the angles
    gl.glUniform3f(gl.glGetUniformLocation(programId, "rotation"), 
                   xAngle, yAngle, zAngle)
    /*
    gl.glUniform4fv(gl.glGetUniformLocation(programId, "rotationQ"), 1,
                    rotation, 0)
                    */
    
    // Do the draw
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.glDrawElements(GL.GL_TRIANGLES, indices.size, GL.GL_UNSIGNED_INT, 0)
    //gl.glDrawElements(GL.GL_TRIANGLES, 3, GL.GL_UNSIGNED_INT, 0)
  }

  protected def createShader(gl:GL2ES2, typeId:Int, source:String) = {
    val shaderId = gl.glCreateShader(typeId)
    gl.glShaderSource(shaderId, 1, Array(source), null)
    gl.glCompileShader(shaderId)
    val ok = ShaderUtil.isShaderStatusValid(gl, shaderId, GL2ES2.GL_COMPILE_STATUS, null)
    if(!ok) {
      throw new javax.media.opengl.GLException(
        source.split("\n").zipWithIndex.map {case (ln:String, no:Int) =>
          println((no+1) + ": " + ln)
        }.reduceLeft(_ + "\n" + _) + "\n\n" +
        ShaderUtil.getShaderInfoLog(gl, shaderId))
    }
    shaderId
  }

  protected def readResource(name:String) = {
    val stream = getClass.getResourceAsStream(name)
    val outString = new StringBuffer
    val buf:Array[Byte] = Array.fill(4096)(0)
    var len = stream.read(buf)
    while(len > 0) {
      outString.append(new String(buf))
      len = stream.read(buf)
    }
    outString.toString.trim
  }
  
}

