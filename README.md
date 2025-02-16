# transaction

#### Introduction
A bank transaction management system  
Simulates a banking transaction system, providing CRUD interfaces with basic validation and exception handling.
#### directory structure
```
transaction/
├── bin/
├── pressTest/     stress test jmeter script and report
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
├── .gitignore
├── API.md          API doc
├── Dockerfile   
├── LICENSE
├── README.md
├── assembly.xml     make tar 
└── pom.xml
```
#### Software Architecture
Software architecture description

1. Provides CRUD interfaces for the transaction software.
2. Implements basic unit tests for methods in Dao, Service, and Controller.
3. Implements exception handling and API log interception.
4. Logging is done using log4j2, with asynchronous logging introduced via Disruptor to improve performance.  
   Logs are categorized into root.log, error.log, and api.log.
5. Caching is implemented using Caffeine.
6. The web server uses Undertow, which performs better than Tomcat.
7. Virtual threads are enabled with JDK 21, and after load testing, no thread leaks were observed.
8. The `common-lang` package is used, with tools like `StringUtils` included.
9. Provides the `my-service.sh` script with default JVM parameters for service startup.
10. The Dockerfile is provided to build the image based on Azul JDK 21.
11. Stress testing is done by JMeter 5.6.3. On  Macbook pro( M1 pro 16G), with 400% CPU usage, the total throughput is 3447 QPS, and the average response time is between 13-25ms. The JMeter script can be found in `jmeter.jmx`, and the graphical report is in the attached `pressTest.pdf`.

#### How to Run this Project
1. in maven 3.8.8 & JDK21
2. run :mvn package
3. run :tar -zxvf {projectDir}/target/transaction-1.0-SNAPSHOT.tar.gz
4. if in linux/Unix os:sh {projectDir}/target/transaction/my-service.sh run
   if in window java -jar {projectDir}/target/transaction/transaction-1.0-SNAPSHOT.jar
#### API Documentation
See `API.md`

