name := "load-balancer"
organization in ThisBuild := "io.pusteblume"
scalaVersion in ThisBuild := "2.13.2"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    provider
  )
lazy val provider = project
  .settings(
    name := "provider",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)

lazy val dependencies =
  new {
    val logbackV        = "1.2.3"
    val logstashV       = "4.11"
    val scalaLoggingV   = "3.9.2"
    val slf4jV          = "1.7.25"
    val typesafeConfigV = "1.3.1"
    val akkaV           = "2.6.6"
    val akkaHttpV       = "10.1.12"
    val scalatestV      = "3.1.2"
    val scalacheckV     = "1.14.1"
    val uuidV           = "0.3.1"

    val logback         = "ch.qos.logback"             % "logback-classic"          % logbackV
    val logstash        = "net.logstash.logback"       % "logstash-logback-encoder" % logstashV
    val scalaLogging    = "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingV
    val slf4j           = "org.slf4j"                  % "jcl-over-slf4j"           % slf4jV
    val typesafeConfig  = "com.typesafe"               % "config"                   % typesafeConfigV
    val akka            = "com.typesafe.akka"          %% "akka-stream"             % akkaV
    val akkaTestKit     = "com.typesafe.akka"          %% "akka-testkit"            % akkaV
    val akkaHttp        = "com.typesafe.akka"          %% "akka-http"               % akkaHttpV
    val akkaHttpTestKit = "com.typesafe.akka"          %% "akka-http-testkit"       % akkaHttpV
    val scalatest       = "org.scalatest"              %% "scalatest"               % scalatestV
    val scalacheck      = "org.scalacheck"             %% "scalacheck"              % scalacheckV
    val uuid            = "io.jvm.uuid"                %% "scala-uuid"              % uuidV

  }

lazy val commonDependencies = Seq(
  dependencies.logback,
  dependencies.logstash,
  dependencies.scalaLogging,
  dependencies.slf4j,
  dependencies.typesafeConfig,
  dependencies.akka,
  dependencies.akkaHttp,
  dependencies.uuid,
  dependencies.akkaTestKit     % "test",
  dependencies.akkaHttpTestKit % "test",
  dependencies.scalatest       % "test",
  dependencies.scalacheck      % "test"
)

lazy val settings =
commonSettings ++
wartremoverSettings ++
scalafmtSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val wartremoverSettings = Seq(
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.Throw)
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtTestOnCompile := true,
    scalafmtVersion := "1.2.0"
  )

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)
