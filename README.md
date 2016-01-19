### BenchmarkScoresServer and BenchmarkScoresClient

REST API comprising:

* a Scala server application which aggregates benchmark score data from an online API (not included), and
* a Java client application that can access this aggregated by-country data from the server.

The two executable JARs and their source code are included.

BenchmarkScoresServer is a Scala application that makes outgoing HTTP requests to the online API and updates its internal data according to the responses. It also hosts an HTTP interface at localhost that can serve average data to incoming requests from clients. Libraries used are akka, spray-client, spray-httpx, spray-json, with Scala version 2.10.5 and JRE 1.8.0. Sbt, sbt-assembly and sbt-eclipse were used for dependencies and building.

BenchmarkScoresClient is a simple Java commandline application which connects to the server and can send HTTP requests according to user input and print the results. Only the standard Java library is used.

### How to run

The root of this package contains two .bat files to start the server and client respectively. Alternatively, in the command line:

>cd \BenchmarkScoresServer\target\scala-2.10
>
>java -jar BenchmarkScoresServer.jar

and

>cd \BenchmarkScoresClient
>
>java -jar BenchmarkScoresClient.jar



### How to build

Building BenchmarkScoresServer requires sbt (Scala build tool). The build.sbt and project/assembly.sbt definition files contain the required dependencies for building. Navigate to /BenchmarkScoresServer/ on the command line and type 'sbt', then 'update', then 'assembly'. The fat JAR containing the application and libraries is built to BenchmarkScoresServer\target\scala-2.10\BenchmarkScoresServer.jar. The source files are at BenchmarkScoresServer/src/main.


To build BenchmarkScoresClient, for example in Eclipse right-click the project, select Export, Runnable JAR file, and set the destination as BenchmarkScoresClient\BenchmarkScoresClient.jar, and select Extract required libraries into generated JAR. The source files are at BenchmarkScoresClient/src/main.