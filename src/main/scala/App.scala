
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.event.ValueChanged

object App extends SimpleSwingApplication {

  // This returns the first window for the app
  def top = new MainFrame {
    title = "Cube demo"

    val cube = new CubePanel
    val controls = new ControlPanel

    contents = new BorderPanel {
      layout(cube) = Center
      layout(controls) = South
    }

    size = new Dimension(640, 480)

    // Need references to the sliders for the reaction matching
    val xAngle = controls.xAngle
    val yAngle = controls.yAngle
    val zAngle = controls.zAngle

    // Listen to the sliders
    listenTo(xAngle)
    listenTo(yAngle)
    listenTo(zAngle)
    listenTo(cube)

    // A partial function of events
    reactions += {
      case ValueChanged(`xAngle`) =>
        cube.xAngle = xAngle.value.toFloat
      case ValueChanged(`yAngle`) =>
        cube.yAngle = yAngle.value.toFloat
      case ValueChanged(`zAngle`) =>
        cube.zAngle = zAngle.value.toFloat
      case AngleUpdate(`cube`, deltaX, deltaY) =>
        xAngle.value += deltaX.toInt
        yAngle.value += deltaY.toInt
    }
  }

}

