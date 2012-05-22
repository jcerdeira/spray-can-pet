resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

resolvers+=Classpaths.typesafeResolver

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.11"))

addSbtPlugin("com.github.mpeltonen"    % "sbt-idea"   % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.5.0")
