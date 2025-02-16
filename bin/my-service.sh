#!/bin/sh
# ******************************************************
# Author        : zhangdaochuan
# Filename      : my-service.sh
# Description   : Java微服务在容器环境启动初始化脚本
# 支持JDK8和JDK17以上版本，ZGC配置
# 调试 sh my-service.sh rundev 或者 startdev
# ******************************************************

#jar包名称
MAIN_JAR=realtime-balance-1.0-SNAPSHOT.jar
#环境如果没有传入就用pro，可以通过 export RUN_ENV=dev && export RUN_ENV=dev && sh my-service.sh run 设置
RUN_ENV=${ENV:-dev}
#服务类型，默认为API服务,响应优先
RUN_TYPE=${RUN_TYPE:-APISVC}
#apollo 的环境，因为pre环境是运行在pro 上，使用pre集群，所以Apollo的配置环境还是pro
CFG_ENV=${CFG_ENV:-dev}
# debug端口
DEBUG_PORT=${DEBUG_PORT:-5005}
# 是否JDK17以上版本 默认是
JDK17=${JDK17:-1}
#JDK17 默认是G1，JDK8 根据内存大小自动切换，只有JDK17以上 可以切换为ZGC
ZGC=${ZGC:-0}
# 是否docker=1 默认不是0
DOCKER=${DOCKER:-0}
# 是否开启GC日志，默认0不开启，1开启
GC_LOG=${GC_LOG:-0}
#CPU 限制，0为不限制，预留外部传入参数
CPU_LIMIT=${CPU_LIMIT:-0}
# 未做内存限制时将变量设置为0，预留外部传入参数 export MEM_LIMIT=8000 使用默认4G CMS配置，大于 8G 8192 会改为 G1
MEM_LIMIT=${MEM_LIMIT:-0}
# 容器对内存 监控90%上限，JVM 自适应设置上限82%，留一部分给堆外
MEM_RATE=${MEM_RATE:-0.82}}
#新生代占比JVM 堆大小比例，推荐是3/8 ，默认是0.4，只有JDK8的 CMS 需要
MN_RATE=${MN_RATE:-0.4}
#预留内存，会在rate 比例下在减去这个预留，默认是100MB
MEM_RESERV=${MEM_RESERV:-100}
#元数据区。一般128MB就够了
METASPACESIZE_LIMIT=${METASPACESIZE_LIMIT:-128}
cur_dir=$(cd `dirname $0`; pwd)
WORK_DIR=$cur_dir
cd $WORK_DIR

# 取默认Java home
JAVA_HOME=${JAVA_HOME}/bin/
#docker 会安装好对应的JDK 配置好环境变量
JAVA_EXE=java

# 开发使用的参数,最简单的 1G
export JAVA_OPT_DEV="-server -Xms1g -Xmx1g -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="$cur_dir"/logs/HeapDumpOnOutOfMemoryError -jar"

# -d 参数判断 $folder 是否存在
if [ -d "${JAVA_HOME}" ]; then
    export JAVA_EXE=${JAVA_HOME}${JAVA_EXE}
fi
# JDK9之后模块化的问题
if [  ${JDK17} -eq 1 ];then
    export JAVA_EXE=$JAVA_EXE" --add-opens java.base/java.lang=ALL-UNNAMED "
fi

#提前初始化logs
if [ ! -d "${WORK_DIR}/logs" ]; then
  mkdir ${WORK_DIR}/logs
fi

# 获取容器内存限制.
get_mem_limit()
{
  if [[ -f /sys/fs/cgroup/memory/memory.stat ]];then
    memory_limit=$(awk '/hierarchical_memory_limit/ {printf("%d", $2/1024/1024)}' /sys/fs/cgroup/memory/memory.stat)
    # 如果内存总量大于限制内存，说明容器是开启了内存限制的，此时取内存限制值，单位为MB
    # 存在部分物理机，安装了容器服务，但是没有正确设置，这时候限制就是超级大，这里进行规避，认为 100MB < 64000MB 比较合理
    if [[ $memory_limit -gt 500 ]] && [[ $memory_limit -lt 64000 ]] ;then
        export MEM_LIMIT=${memory_limit}
    elif [[ $memory_limit -gt 64000 ]] ;then
        echo 'MEM_LIMIT is great than 64000MB, it not reasonable, will use default set!'
    else
        # 未做内存限制，则使用默认值
        echo 'MEM_LIMIT is less than 500MB, will use default set!'
    fi
  fi
}

# 获取容器CPU限制.
get_cpu_limit()
{
  if [[ -f /sys/fs/cgroup/cpu/cpu.cfs_quota_us ]];then
    cfs_quota_us=$(cat /sys/fs/cgroup/cpu/cpu.cfs_quota_us)
    cfs_period_us=$(cat /sys/fs/cgroup/cpu/cpu.cfs_period_us)
    cpu_limit=$(expr ${cfs_quota_us} / ${cfs_period_us})
    if [[ ${cpu_limit} -eq 0 ]];then
        # 未做内存限制，则使用默认值
        # export CPU_LIMIT=0
        echo 'CPU_LIMIT is 0 ,will use default limit!'
    else
        export CPU_LIMIT=${cpu_limit}
    fi
  fi
}
if [  ${DOCKER} -eq 1 ];then
    get_mem_limit
    get_cpu_limit
fi


# 若未限制内存则采用4G默认配置
if [  ${MEM_LIMIT} -eq 0  ];then
    #JDK8 用CMS JDK8 用G1
    if [  ${JDK17} -eq 1 ];then
        #ZGC 需要开启
       if [  ${ZGC} -eq 1 ];then
            JAVA_OPT_EXT="-Xmx2688M -Xms2688M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M
            -XX:+UseZGC
            "
       else
            JAVA_OPT_EXT="-Xmx2688M -Xms2688M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M
            -XX:+DisableExplicitGC
            -XX:MaxGCPauseMillis=100
            -XX:+UseG1GC
            -XX:+ParallelRefProcEnabled
            "
       fi
    else
        JAVA_OPT_EXT="-Xmx2688M -Xms2688M -Xmn960M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M
        -XX:+CMSClassUnloadingEnabled
        -XX:CMSInitiatingOccupancyFraction=70
        -XX:+CMSScavengeBeforeRemark
        -XX:+DisableExplicitGC
        -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses
        -XX:+ParallelRefProcEnabled
        -XX:+UseCMSInitiatingOccupancyOnly
        -XX:+UseConcMarkSweepGC
        -XX:+UseParNewGC
        "
    fi
else
    # 计算JVM堆内存 限制上限,减去 METASPACESIZE_LIMIT - MEM_RESERV
    XMXS_LIMIT=$(echo ${MEM_LIMIT} | awk '{printf("%d", $0*'$MEM_RATE'-'$METASPACESIZE_LIMIT'-'$MEM_RESERV')}')
    # 新生代占比40% JVM堆内存
    XMN_LIMIT=$(echo ${XMXS_LIMIT} | awk '{printf("%d", $0*'$MN_RATE')}')
    echo 'Xmx='$XMXS_LIMIT' Xmn='$XMN_LIMIT' MaxMetaspaceSize='$METASPACESIZE_LIMIT
    # JDK8 小于6G内存配置 配置CMS好点
    if [ ${MEM_LIMIT} -lt 6144 ];then
        # JDK17
        if [  ${JDK17} -eq 1 ];then
            if [  ${ZGC} -eq 1 ];then
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+UseZGC
                "
            else
                # 默认都是G1
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+DisableExplicitGC
                -XX:MaxGCPauseMillis=100
                -XX:+UseG1GC
                -XX:+ParallelRefProcEnabled
                "
            fi
        else
            # JDK8 适用于API接口服务类型，相应优先CMS
            if [[ "${RUN_TYPE}"  == "APISVC" ]];then
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -Xmn${XMN_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+CMSClassUnloadingEnabled
                -XX:CMSInitiatingOccupancyFraction=70
                -XX:+CMSScavengeBeforeRemark
                -XX:+DisableExplicitGC
                -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses
                -XX:+ParallelRefProcEnabled
                -XX:+UseCMSInitiatingOccupancyOnly
                -XX:+UseConcMarkSweepGC
                -XX:+UseParNewGC
                "
            # 适用于纯后台服务类型，使用吞吐优先GC
            else
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -Xmn${XMN_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+UseParallelGC
                -XX:+UseParallelOldGC
                "
            fi
        fi

    # 大于6G内存配置 G1
    else
        #JDK17
        if [  ${JDK17} -eq 1 ];then
            if [  ${ZGC} -eq 1 ];then
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+UseZGC
                "
            else
                # 默认都是G1
                JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
                -XX:+DisableExplicitGC
                -XX:MaxGCPauseMillis=100
                -XX:+UseG1GC
                -XX:+ParallelRefProcEnabled
                "
            fi
        #JDK8
        else
            JAVA_OPT_EXT="-Xmx${XMXS_LIMIT}M -Xms${XMXS_LIMIT}M -XX:MaxMetaspaceSize=${METASPACESIZE_LIMIT}M -XX:MetaspaceSize=${METASPACESIZE_LIMIT}M
            -XX:+DisableExplicitGC
            -XX:MaxGCPauseMillis=100
            -XX:+UseG1GC
            -XX:+ParallelRefProcEnabled
            "
        fi
    fi
fi

 # 当存在CPU限制的时候，设置可用CPU数量
if [[ ${CPU_LIMIT} > 0 ]];then
    JAVA_OPT_EXT="${JAVA_OPT_EXT} -XX:ActiveProcessorCount=${CPU_LIMIT}"
fi

# 拼接参数, 容器指定的 JAVA_OPT 拼接到最后，以支持手工覆盖自动计算的参数
JAVA_OPT="$JAVA_OPT_EXT
        -server
        -Duser.timezone=GMT+08
        -XX:+HeapDumpOnOutOfMemoryError
        -XX:HeapDumpPath=$WORK_DIR/logs/dump.hprof
        -XX:ErrorFile=$WORK_DIR/logs/hs_err_pid%p%t.log
        ${JAVA_OPT}"

if [  ${GC_LOG} -eq 1 ];then
JAVA_OPT="${JAVA_OPT}
        -XX:+PrintGCDateStamps
        -XX:+PrintGCDetails
        -XX:+PrintGCTimeStamps
        -XX:+PrintHeapAtGC
        -XX:+PrintGCApplicationStoppedTime
        -Xloggc:$WORK_DIR/logs/gc%t.log"
fi

getpid()
{
    PID=`ps -ef | grep $MAIN_JAR | grep spring | awk '{print $2}'`
    if [ -z "${PID}" ]; then
         echo "not fond pid"
   else
         echo ${PID}
   fi

}

#启动java，交由上层 supervisor 管理java进程
run()
{
    exec $JAVA_EXE -Denv=$CFG_ENV -Dprocess_num=$process_num $JAVA_OPT -jar $cur_dir"/"$MAIN_JAR --spring.profiles.active=$RUN_ENV
}

#启动java，默认使用dev环境，并开启远程调试端口
rundev()
{
    exec $JAVA_EXE -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$DEBUG_PORT -Denv=dev -Dprocess_num=$process_num $JAVA_OPT_DEV -jar $cur_dir"/"$MAIN_JAR --spring.profiles.active=dev
}

start()
{
    PID=`getpid`
    if [ -z "${PID}" ]; then
        echo "Error! $MAIN_JAR is running, pid: ${PID}"
        exit -1
    fi

    nohup $JAVA_EXE -Denv=$CFG_ENV -Dprocess_num=$process_num $JAVA_OPT -jar $cur_dir"/"$MAIN_JAR --spring.profiles.active=$RUN_ENV >/dev/null 2>&1 &

    sleep 2
    PID=`getpid`
    if [ ! -z "${PID}" ]; then
       echo "start success!"
    else
       echo "start fail!"
    fi

}
#开发模式的JVM参数
startdev()
{
    PID=`getpid`
    if [ ! -z "${PID}" ]; then
        echo "Error! $MAIN_JAR is running, pid: ${PID}"
        exit -1
    fi

    nohup $JAVA_EXE -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$DEBUG_PORT -Denv=dev -Dprocess_num=$process_num $JAVA_OPT_DEV -jar $cur_dir"/"$MAIN_JAR --spring.profiles.active=dev >/dev/null 2>&1 &

    sleep 2
    PID=`getpid`
    if [ ! -z "${PID}" ]; then
       echo "start success!"
    fi

}

stop()
{
    PID=`getpid`
    if [ ! -z "${PID}" ]; then
        echo "stop "$PID
        kill ${PID}
    fi

    sleep 3

    PID=`getpid`
    if [ -z "${PID}" ]; then
       echo "stop fail!"
    else
       echo "stop success!"
    fi
}

restart()
{
    stop

    start
}

usage()
{
    echo "Usage: $0 start/startdev/restart/stop/run/rundev"
}

if [ ! -z "$1" ] ; then
    if [ "$1" == "start" ]; then
        start
    elif [ "$1" == "restart" ] ; then
        restart
    elif [ "$1" == "startdev" ] ; then
        startdev
    elif [ "$1" == "run" ] ; then
        run
    elif [ "$1" == "stop" ] ; then
        stop
    elif [ "$1" == "getpid" ] ; then
        getpid
    elif [ "$1" == "rundev" ] ; then
        rundev
    else
        echo "Err! $1 is not a correct cmd!"
    fi
else
    usage
fi

