resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

resolvers+=Classpaths.typesafeResolver

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.11"))

addSbtPlugin("com.github.mpeltonen"    % "sbt-idea"   % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.5.0")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.10")
