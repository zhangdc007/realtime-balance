# 使用基础镜像，例如Ubuntu
FROM azul/zulu-openjdk:21.0.5

RUN mkdir -p /opt/app/
# 将应用程序文件添加到容器中,gz包含了jar配置文件
ADD target/realtime-balance-1.0-SNAPSHOT.tar.gz /opt/app/
WORKDIR /opt/app/
EXPOSE 9001
# 设置默认启动脚本
CMD ["sh", "/opt/app/transaction/my-service.sh", "run"]
