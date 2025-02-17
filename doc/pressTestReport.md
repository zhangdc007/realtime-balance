# 压测报告
压测环境

```yaml
mac Pro m1 pro 16GB
```
使用JDK21 jar 包启动，本地启动jmeter 压测，jmeter任务配置：

[office](Pq-n8qR7yBXv-sASz4pxXepAru3p4JxF0OZFsxO9Lkw.jmx)

同时开jprofile分析线程和CPU，GC，内存情况是否正常

启动JVM参数

```yaml
/Users/zhangdaochuan/Library/Java/JavaVirtualMachines/azul-21.0.5/Contents/Home/bin/java --add-opens java.base/java.lang=ALL-UNNAMED -Denv=dev -Dprocess_num= -Xmx2688M -Xms2688M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:+ParallelRefProcEnabled -server -Duser.timezone=GMT+08 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/Users/zhangdaochuan/Documents/git_project/hsbc/transaction/target/transaction/logs/dump.hprof -XX:ErrorFile=/Users/zhangdaochuan/Documents/git_project/hsbc/transaction/target/transaction/logs/hs_err_pid%p%t.log -jar /Users/zhangdaochuan/Documents/git_project/hsbc/transaction/target/transaction/transaction-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```
JVM堆内存2.75G

GC情况

![image](images/f_KlTOWhwKbrYNaf28BBh1MblwrRK9-fTM4MprJt7sM.png)

开启虚拟线程后，不论Tomcat还是undertow都没有线程池限制，这里还是需要进一步评估不限制虚拟线程的影响

![image](images/rjlrOJi0mAlpuDnKX9stHXBJC9MS8RroTH5QiVkK-N0.png)

在压测中，真实线程一直很稳定，总线程在20个，没有线程泄漏问题

![image](images/Y9C7Uzf47hB6aALXHfeGwIQbhXedC29Z_m4DoFXYPVI.png)

用jemete 禁止了删除和查询全部接口，性能可以到580/s，总请求可以到2290/s 

高峰CPU消耗400%，占本机总CPU约57%

可以看到线程那里

![image](images/MTeCpaH-ta88csa6b-h2RDbI_kPrMC5Jle5Kxz7QlLQ.png)

去除jprpfile在进行压测

CPU400%情况下，每个请求可以达到870/s，总请求3447/s 耗时 avg 13\~25ms

![image](images/4BeEkIfXbnq_2Qmk6tUtD4ef0wOpTuVrlNIRSufRQhQ.png)



