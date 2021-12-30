# 一、安装前准备

## 1.1 环境准备

### 1.1.1 jdk安装

这里采用官网直接下载的oracle jdk8,使用ftp传入jdk-8u172-linux-x64.tar.gz（也可以使用openjdk8）

```shell
mkdir -p /usr/java
tar -zxvf jdk-8u221-linux-x64.tar.gz -C /usr/java
```

#### 配置JAVA_HOME,JRE_HOME

```shell
echo '  
JAVA_HOME=/usr/java/jdk1.8.0_221
JRE_HOME=$JAVA_HOME/jre
PATH=$PATH:$JAVA_HOME/bin
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export JAVA_HOME JRE_HOME PATH CLASSPATH'  >> /etc/profile

source /etc/profile
```

#### 检查安装是否成功

```shell
java -version
```

## 1.2 linux配置

### 1.2.1 文件描述符数量配置

系统级文件描述符，以root用户修改

```shell
vi /etc/sysctl.conf
#修改 fs.file-max = 300000 #数值也可以修改更大
#执行以下命令使配置生效
cat /proc/sys/fs/file-max
#验证配置
sysctl fs.file-max
```

进程级文件描述符

```shell
#对于打开文件描述符，一个合理的最大值是32768。
#设置数量
ulimit -n 32768 -u 32768
```

也可以采用直接修改配置文件修改

```shell
vi /etc/security/limits.conf

#修改如下参数
#- soft    nofile          32768
#- hard    nofile          32768

vi /etc/security/limits.d/90-nproc.conf

#- soft nproc 32768
```

### 1.2.2 SWAP设置

当内存的使用达到阈值时，操作系统就会开始进行从内存到磁盘的页面交换，交换会显著影响Ignite节点进程的性能，这个问题可以通过调整操作系统参数来避免。

定义内核交换内存页面的积极程度。较高的值将增加攻击性，较低的值将减少交换量。建议将此值设为10，以避免交换延迟

如果开启了ignite的原生持久化，也可以将其配置为0。现在默认关闭交换

```shell
sysctl -w vm.swappiness=10
```

# 二、安装

安装ignite有多种方式，这里直接采用官网下载的bin包安装

```shell
mkdir -p /opt/ignite
cd /opt/ignite
#内网无法下载，建议ftp下载传入
wget -O apache-ignite.zip http://mirrors.tuna.tsinghua.edu.cn/apache//ignite/2.7.5/apache-ignite-2.7.5-bin.zip
#安装解压命令
yum install -y zip unzip
unzip apache-ignite-2.7.5-bin.zip
cd apache-ignite-2.7.5-bin
```

# 三、ignite配置

ignite主要配置在${IGNITE_HOME}/config/default-config.xml

## 3.1 IGNITE_HOME配置

```shell
echo -e ' IGNITE_HOME=/opt/ignite/apache-ignite-2.7.5-bin\nexport IGNITE_HOME' >> /etc/profile
source /etc/profile
#查看环境变量
echo $IGNITE_HOME
```

## 3.2 缓存配置

### 3.2.1 内存配置

ignite支持将数据储存在堆内内存和堆外内存，这里我们使用堆外内存。

以开发环境4c8g的机器举例设置,jvm使用3.5G内存，直接内存4G

Ignite节点默认会至多消耗本地可用内存的20%，大多数情况下这也是唯一需要调整的参数，要修改默认内存区大小

这里我们设置使用本机4G直接内存作为固化内存

```xml

<bean class="org.apache.ignite.configuration.IgniteConfiguration">

    <!-- Redefining maximum memory size for the cluster node usage. -->
    <property name="dataStorageConfiguration">
        <bean class="org.apache.ignite.configuration.DataStorageConfiguration">


            <!-- 设置页缓存的并发级别 -->
            <property name="concurrencyLevel" value="4"/>
            <!-- 设置页缓存为 4 KB -->
            <property name="pageSize" value="4096"/>
            <!-- 默认内存区设置大小 -->
            <property name="defaultDataRegionConfiguration">
                <bean class="org.apache.ignite.configuration.DataRegionConfiguration">
                    <property name="name" value="Default_Region"/>
                    <!-- 设置初始大小为512M. -->
                    <property name="initialSize" value="#{512L * 1024 * 1024}"/>
                    <!-- 设置内存区域大小 -->
                    <property name="maxSize" value="#{4L * 1024 * 1024 * 1024}"/>
                    <!-- 设置缓存淘汰策略  -->
                    <property name="pageEvictionMode" value="RANDOM_2_LRU"/>


                    <!-- 开启该内存区的指标收集：内存占用的页数，使用比率  -->
                    <property name="metricsEnabled" value="true"/>
                </bean>
            </property>
        </bean>
    </property>
</bean>
```

### 3.2.2 页缓存配置

对应上面pagesize的设定

#### 查看系统默认页缓存大小

getconf PAGE_SIZE

## 3.3 日志配置

这里采用log4j2

首先添加依赖

```shell
#添加日志依赖
mv $IGNITE_HOME/libs/optional/ignite-log4j2/*.jar $IGNITE_HOME/libs
#添加rest接口依赖
mv $IGNITE_HOME/libs/optional/ignite-rest-http/*.jar $IGNITE_HOME/libs/
```

ignite配置log4j2

```xml

<bean class="org.apache.ignite.configuration.IgniteConfiguration">
    <property name="gridLogger">
        <bean class="org.apache.ignite.logger.log4j2.Log4J2Logger">
            <constructor-arg type="java.lang.String" value="log4j2.xml"/>
        </bean>
    </property>
    <!-- Other Ignite configurations -->
    ...
</bean>
```

在/config下新建log4j2.xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="60">
    <Appenders>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{ISO8601}][%-5p][%t][%c{1}]%notEmpty{[%markerSimpleName]} %m%n"/>
            <ThresholdFilter level="ERROR" onMatch="DENY" onMismatch="ACCEPT"/>
        </Console>

        <Console name="CONSOLE_ERR" target="SYSTEM_ERR">
            <PatternLayout pattern="[%d{ISO8601}][%-5p][%t][%c{1}]%notEmpty{[%markerSimpleName]} %m%n"/>
        </Console>

        <Routing name="FILE">
            <Routes pattern="$${sys:nodeId}">
                <Route>
                    <RollingFile name="Rolling-${sys:nodeId}"
                                 fileName="${sys:IGNITE_HOME}/work/log/ignite-${sys:nodeId}-%d{yyyy-MM-dd}.log"
                                 filePattern="${sys:IGNITE_HOME}/work/log/ignite-${sys:nodeId}-%i-%d{yyyy-MM-dd}.log.gz">
                        <PatternLayout pattern="[%d{ISO8601}][%-5p][%t][%c{1}]%notEmpty{[%markerSimpleName]} %m%n"/>
                        <Policies>
                            <TimeBasedTriggeringPolicy interval="6" modulate="true"/>
                            <SizeBasedTriggeringPolicy size="10 MB"/>
                        </Policies>
                    </RollingFile>
                </Route>
            </Routes>
        </Routing>
    </Appenders>

    <Loggers>
        <!--
        <Logger name="org.apache.ignite" level=DEBUG/>
        -->

        <!--
            Uncomment to disable courtesy notices, such as SPI configuration
            consistency warnings.
        -->
        <!--
        <Logger name="org.apache.ignite.CourtesyConfigNotice" level=OFF/>
        -->

        <Logger name="org.springframework" level="WARN"/>
        <Logger name="org.eclipse.jetty" level="WARN"/>

        <!--
        Avoid warnings about failed bind attempt when multiple nodes running on the same host.
        -->
        <Logger name="org.eclipse.jetty.util.log" level="ERROR"/>
        <Logger name="org.eclipse.jetty.util.component" level="ERROR"/>

        <Logger name="com.amazonaws" level="WARN"/>

        <Root level="INFO">
            <!-- Uncomment to enable logging to console. -->
            <!--
            <AppenderRef ref="CONSOLE" level="DEBUG"/>
            -->

            <!-- <AppenderRef ref="CONSOLE_ERR" level="ERROR"/> -->
            <AppenderRef ref="FILE" level="INFO"/>
        </Root>
    </Loggers>
</Configuration>
```

## 3.4 jmx监控配置

ignite启动时默认会校验端口是否会占用，每次port num+1 ，这样启动时可能会造成jmx端口不固定。生产可以设置固定端口。

ignite的启动脚本会调用/bin/include目录下functions.sh脚本进行端口设置，需要在这里修改

```text
#findAvailableJmxPort() {
#    JMX_PORT=`"$JAVA" -cp "${IGNITE_LIBS}"  org.apache.ignite.internal.util.portscanner.GridJmxPortFinder`

#添加 -DIGNITE_JMX_PORT=49112
findAvailableJmxPort() {
JMX_PORT=`"$JAVA" -cp "${IGNITE_LIBS}" -DIGNITE_JMX_PORT=49112 org.apache.ignite.internal.util.portscanner.GridJmxPortFinder`


```

> ignite jmx连接请会产生大量的metrics对象，可以配置关闭jmx监控，通过http接口获取一些监控数据，启动时配置参数-nojmx即可关闭

# 四、启动ignite

综合以上配置，我们的ignite配置文件为default-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean class="org.apache.ignite.configuration.IgniteConfiguration">
        <!--配置对等类加载器，用来远程执行类加载-->
        <property name="peerClassLoadingEnabled" value="true"/>

        <!-- 设为0则关闭metrics日志-->
        <property name="metricsLogFrequency" value="0"/>


        <property name="gridLogger">
            <bean class="org.apache.ignite.logger.log4j2.Log4J2Logger">
                <constructor-arg type="java.lang.String" value="config/log4j2.xml"/>
            </bean>
        </property>

        <!-- 内存区域设置，默认设置为直接内存 -->
        <property name="dataStorageConfiguration">
            <bean class="org.apache.ignite.configuration.DataStorageConfiguration">

                <!-- 设置页缓存的并发级别 -->
                <property name="concurrencyLevel" value="4"/>
                <!-- 设置页缓存为 4 KB -->
                <property name="pageSize" value="4096"/>

                <!-- 默认内存区设置大小 -->
                <property name="defaultDataRegionConfiguration">
                    <bean class="org.apache.ignite.configuration.DataRegionConfiguration">
                        <property name="name" value="Default_Region"/>
                        <!-- 设置初始大小为512M. -->
                        <property name="initialSize" value="#{512L * 1024 * 1024}"/>
                        <!-- 设置内存区域大小 -->
                        <property name="maxSize" value="#{4L * 1024 * 1024 * 1024}"/>
                        <!-- 设置缓存淘汰策略  -->
                        <property name="pageEvictionMode" value="RANDOM_2_LRU"/>

                        <!-- 开启该内存区的指标收集：内存占用的页数，使用比率  -->
                        <property name="metricsEnabled" value="true"/>

                        <!-- 开启ignite原生持久化-->
                        <!--<property name="persistenceEnabled" value="true"/>-->

                    </bean>
                </property>
            </bean>
        </property>

        <!--  这里暂时采用静态ip发现，后续采用consul注册发现-->
        <property name="discoverySpi">
            <bean class="org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi">
                <property name="localPort" value="47500"/>
                <!-- Changing local port range. This is an optional action. -->
                <property name="localPortRange" value="3"/>
                <property name="ipFinder">
                    <bean class="org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder">
                        <property name="addresses">
                            <list>
                                <!-- 替换为实际的ip，端口确定一个，减少通信交互 -->
                                <value>40.23.9.216:47500</value>
                                <value>40.23.10.83:47500</value>
                                <value>40.23.11.165:47500</value>
                            </list>
                        </property>
                    </bean>
                </property>
            </bean>
        </property>


        <property name="communicationSpi">
            <bean class="org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi">
                <property name="localPort" value="47100"/>
                <property name="localPortRange" value="3"/>
            </bean>
        </property>

    </bean>
</beans>
```

#### jvm参数设置

```shell
#创建存放gc日志的文件夹
mkdir -p /opt/ignite/jvmlog/logs
mkdir -p /opt/ignite/heapdump

#修改配置文件 $IGNITE_HOME/bin/ignite.sh
#找到
#if [ -z "$JVM_OPTS" ] ; then
#    JVM_OPTS="-Xms1g -Xmx1g -server -XX:MaxMetaspaceSize=256m"
#fi
#替换成
JVM_OPTS="-Xms3g -Xmx3g -server \
-DIGNITE_JETTY_PORT=8888 \
-Djava.net.preferIPv4Stack=true \
-XX:MaxDirectMemorySize=4g \
-XX:+UseG1GC \
-XX:+AlwaysPreTouch \
-XX:+ScavengeBeforeFullGC \
-XX:+DisableExplicitGC \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/path/to/heapdump \
-XX:+ExitOnOutOfMemoryError \
-XX:+CrashOnOutOfMemoryError \
-XX:+PrintGCDetails \
-Xloggc:/opt/ignite/jvmlog/logs/log.txt"
```

有些jvm参数在jdk9后就不再生效，开发环境有jdk9,11发现这个问题，包括以下常用参数，建议不使用：

```text
-XX:OnOutOfMemoryError=“kill -9 %p” \
-XX:+PrintAdaptiveSizePolicy
-XX:+PrintGCTimeStamps \
-XX:+PrintGCDateStamps \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=10 \
-XX:GCLogFileSize=100M \
```

启动ignite

### 后台运行

```shell
nohup $IGNITE_HOME/bin/ignite.sh >/dev/null 2>&1 & 
```

结果查看
```shell
$IGNITE_HOME/bin/control.sh --user ignite --password ignite --baseline
```

如果配置了持久化分区，需要额外进行节点的基线拓扑激活

```shell
$IGNITE_HOME/bin/control.sh --set-state ACTIVE
#也可以使用api请求激活节点
curl http://host:port/ignite?cmd=setstate\&state=ACTIVE
```

节点状态查看

```shell
$IGNITE_HOME/bin/control.sh --user ignite --password ignite --state
```

三、gitlab devops安装
 