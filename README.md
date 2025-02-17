# realtime-balance-system

#### Introduction
A bank real-time balance transaction system  
a banking transaction system, providing CRUD interfaces with basic validation and exception handling.
#### directory structure
```
realtime-balance/
├── bin/
├── pressTest/     stress test jmeter script and report
├── doc/
│   ├── API.md     API doc
│   ├── ddl.sql    database sql
├── src/
│   ├── main/
│   │   ├── java/{com.mybank.balance.transaction}
│   │   │   ├── cache/     
│   │   │   ├── common/
│   │   │   ├── controller/
│   │   │   ├── dao/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── schedualer/   Scheduled retry tasks
│   │   │   ├── service/
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

### Architecture Diagram

```plaintext
+-------------------------------------------+
|            Kubernetes Cluster             |
|                                           |
|                  HPA                      |
|  +-------------+      +-----------+       | 
|  |             |      |           |       |  
|  |  Web App    |      |  Web App  |       |  
|  | (webflux)   |      | (webflux) |       |
|  |             |      |           |       |   
|  +-------------+      +-----------+       |  
|          | \          /     |             |
+----------+------------------+-------------+
           |    \    /        |           
           |      \ /         |           
           v      / \         v           
  +---------------+    +----------------+       
  |   MySQL 5.7   |    |   Redis 6.2    |       
  |               |    |                |       
  +---------------+    +----------------+       


```
### Technical selection
- web server 
  - Spring Boot 3.4
  - Spring web flux
  - Spring Data JPA
  - r2dbc
  - redis-reactive
  - log4j2
- DB
  - Mysql 5.7
- Cache
  - redis 6.2


Software architecture description

1. Provides CRUD interfaces for the realtime balance system.
2. Implements basic unit tests for methods in Dao, Service, and Controller.
3. Implements exception handling and API log interception.
4. Logging is done using log4j2, with asynchronous logging introduced via Disruptor to improve performance.  
   Logs are categorized into root.log, error.log, and api.log.
5. Caching is implemented using Redis 6.2 and redis-reactive.
6. The web server uses webflux, which performs better than Servlet(Tomecat etc.). 
   Virtual threads in JDK 21 will pin at sychronized code,performance is lees than webflux, see [https://blog.csdn.net/dyc87112/article/details/135686924).
7. Provides the `my-service.sh` script with default JVM parameters for service startup.
8. The Dockerfile is provided to build the image based on Azul JDK 21.
9. Stress testing is done by JMeter 5.6.3. On  Macbook pro( M1 pro 16G), with 400% CPU usage, the total throughput is 3447 QPS, and the average response time is between 13-25ms. The JMeter script can be found in `jmeter.jmx`, and the graphical report is in the attached `pressTest.pdf`.

#### How to Run this Project
1. in maven 3.8.8 & JDK21
2. run :mvn package
3. run :tar -zxvf {projectDir}/target/realtime-balance-1.0-SNAPSHOT.tar.gz
4. if in linux/Unix/Mac os:sh {projectDir}/target/realtime-balance/my-service.sh run
   if in window java -jar {projectDir}/target/realtime-balance/realtime-balance-1.0-SNAPSHOT.jar
#### API Documentation
See /doc/API.md
#### deploy script
See Dockerfile
#### Core interface design
See /doc/设计文档.md
#### Stress test
See /pressTest/pressTest.pdf
