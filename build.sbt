
name := "scala-swing demo"

organization := "com.tomtorsneyweir"

libraryDependencies ++= Seq(
  "org.jogamp.gluegen" % "gluegen-rt-main" % "2.0.2",
  "org.jogamp.jogl" % "jogl-all-main" % "2.0.2"
)

// Add a dependency for scala-swing
libraryDependencies <<= (scalaVersion, libraryDependencies) {(sv, deps) =>
  deps :+ ("org.scala-lang" % "scala-swing" % sv)
}

