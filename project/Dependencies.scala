import sbt._

object Dependencies {
  private val zioVersion = "1.0.0-RC21-2"
  private val awsSdkVersion = "1.11.820"

  lazy val awsDynamoDb = "com.amazonaws" % "aws-java-sdk-dynamodb" % awsSdkVersion
  lazy val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
  lazy val awsSQS = "com.amazonaws" % "aws-java-sdk-sqs" % awsSdkVersion
  lazy val awsStateMachine = "com.amazonaws" % "aws-java-sdk-stepfunctions" % awsSdkVersion
  lazy val zio = "dev.zio" %% "zio" % zioVersion
  lazy val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
  lazy val upickle = "com.lihaoyi" %% "upickle" % "1.1.0"
  lazy val awsLambda = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
  lazy val http = "org.scalaj" %% "scalaj-http" % "2.4.2"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.9"
  lazy val commonsCsv = "org.apache.commons" % "commons-csv" % "1.8"
}
