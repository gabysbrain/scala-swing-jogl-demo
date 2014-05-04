
import scala.swing.GridPanel
import scala.swing.{Label, Slider}

class ControlPanel extends GridPanel(3, 2) {

  // Sliders to control the angle of the cube
  val xAngle = new Slider {
    min = 0
    max = 360
  }
  val yAngle = new Slider {
    min = 0
    max = 360
  }
  val zAngle = new Slider {
    min = 0
    max = 360
  }

  // Add everything to the grid
  contents += new Label("X Angle")
  contents += xAngle
  contents += new Label("Y Angle")
  contents += yAngle
  contents += new Label("Z Angle")
  contents += zAngle
}

