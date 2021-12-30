

## 一、节点发现
q:新节点加入或节点重启时，节点频繁创建线程去建立TCP连接

s:原因可能：

部署环境问题。比如dev环境网络不稳定，或是防火墙响应端口未开放
资源不足。比如连接远程节点的主机内存空间不够导致频繁fullGC,影响通信线程执行。或是主机有其他程序占用资源干扰
ignite本身配置问题。比如失败处理handler配置，sockect配置
ipv6问题，ignite目前不支持ipv6，建议程序启动时配置JVM参数 -Djava.net.preferIPv4Stack=true
jdk版本问题，建议使用同一版本jdk。另外openjdk和jdk启动参数有略微的差异
开发环境网络不稳定的情况下，可以采取调整超时时间来减少通信失败
节点发现配置超时时间

```shell
        <property name="discoverySpi">
            <bean class="org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi">
                <property name="localPort" value="47500"/>
                <!-- 只配置一个发现端口，减少网络开销 -->
                <property name="localPortRange" value="1"/>
                <!--配置网络和响应超时，减少失败，默认是5秒，开发环境配置10s-->
                <property name="ackTimeout" value="10000"/>
                <property name="networkTimeout" value="10000"/>
                <property name="ipFinder">
                    <bean class="org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder">

                        <property name="addresses">
                            <list>
                                <value>127.0.0.1:47500</value>
                            </list>
                        </property>
                    </bean>
                </property>
            </bean>
        </property>
```

全局的网络超时（不建议配置）

```xml
<bean class="org.apache.ignite.configuration.IgniteConfiguration">
    <!--配置对等类加载器，用来远程执行类加载-->
    <property name="peerClassLoadingEnabled" value="true"/>

    <property name="networkTimeout" value="10000"/>

    <!-- 设为0则关闭metrics日志-->
    <property name="metricsLogFrequency" value="0"/>

    <property name="workDirectory" value="/app/ignite/work"/>
</bean>
```

## 二、表名大小写
表名及表字段默认是大写，如何自定义配置大小写？
可以使用缓存配置建立缓存表，设置以下属性为true即可使用当前的表名和字段名大小写格式，不会使用默认的大写 cacheConfiguration.setSqlEscapeAll(true);



## 三、sql join数据丢失
现象：多表联查时发现数据丢失，比如A,B表join查询。

原因：ignite是分布式缓存，A,B表的数据分散在不同节点，多表联查时，ignite为了最大性能，默认是采用各节点数据join后汇总，而不是A,B表汇总后join.

- 解决方式一、可以设置以下参数强制数据进行多节点join.

SqlFieldsQuery you_sql_qry = new SqlFieldsQuery("you sql").setDistributedJoins(true);
- 解决方式二、对表数据进行并置关联。需要提前对业务数据进行处理



## 四、服务节点之间建立连接时间较长
现象：服务之间长时间无数据交互，系统判定为空闲连接杀掉，有新数据交互建立新连接花费时间较长。

原因：节点建立连接会话前会存储机器名、本地ip、网卡ip。和其他节点建立连接时按以上顺序建立连接，如果本地dns没有配置机器名映射，会导致超时重试3次，花费时间15s左右。

解决方式：所有集群节点在/etc/hosts文件配置 <机器名：ip》映射





## 五、缓存表字段无法进行类型变更
（相似问题：缓存表如何进行字段添加？这个比字段类型变更简单很多，删除缓存表后重建缓存表即可）

现象; 缓存表tableA有String 类型的coloumA字段。调用destroyCache()接口删除原先的缓存表后，将coloumA字段修改为Integer类型，服务端报类型不匹配，需要String类型的coloumA字段，字段类型修改失败

原因：destroyCache并没有删除原先的cacheMetadata()，ignite无删除binaryType的接口，只能使用新的binaryType指向cache Entity

- 解决方案一：新建CacheEntity,即新建java 类，设置为原先的cacheName

- 解决方案二：ignite 控制脚本删除缓存元数据（需要ignite服务端 版本 >=2.9.1）

可以先在$IGNITE_HOME/work/db/marshaller/目录下查找是否有相关的元数据 grep "com.dzz.xzz.hh.server.issue.ChangeDao" -nr
 1. 首先为control.sh或者control.bat 设置jvm参数 -DIGNITE_ENABLE_EXPERIMENTAL_COMMAND=true ，支持过期命令
```shell
if %ERRORLEVEL% equ 0 (
if "%CONTROL_JVM_OPTS%" == "" set CONTROL_JVM_OPTS=-Xms256m -Xmx1g -DIGNITE_ENABLE_EXPERIMENTAL_COMMAND=true
) else (
if "%CONTROL_JVM_OPTS%" == "" set CONTROL_JVM_OPTS=-Xms256m -Xmx1g -DIGNITE_ENABLE_EXPERIMENTAL_COMMAND=true
)
```

 2. 参看缓存已保存的元数据类型 typeName替换为需要更改字段类型的类名
```shell
control.sh --meta details --typeName com.dzz.xzz.hh.server.issue.ChangeDao
#--------------------------------------------------------------------------------
#typeId=0xF124D539 (-249244359)
#typeName=com.dzz.xzz.hh.server.issue.ChangeDao
#Fields:
#  name=name, type=int, fieldId=0x337A8B (3373707)
#  name=id, type=int, fieldId=0xD1B (3355)
#Schemas:
#  schemaId=0x39DCF1F3 (970781171), fields=[id, name]

```
 3.删除缓存元数据
```shell
control.sh  --meta remove --typeName com.dzz.xzz.hh.server.issue.ChangeDao
```
 4.调用接口删除缓存 igniteClient.destroyCache(CACHE_NAME)即可;


## 六、批量删除/更新建议
使用批量删除removeAll()/putAll()接口时，建议使用TreeSet,，乱序的hashSet可能导致分布式节点时删除/插入数据发生死锁.

有大批量数据导入建议使用流导入

引入数据加载依赖：

```xml
<dependency>
    <artifactId>DZZ-platform-ignite-convert</artifactId>
    <groupId>com.DZZ.hehe</groupId>
    <version>1.0.3.RELEASE</version>
</dependency>

```
//load过程无数据转换
new MiniStreamLoadUtils(igniteMiniClientManager).loadNormalStream(list.stream())

tips:数据导入过程可以自定义数据转换，demo可见瘦客户端接入文档



## 七、IN 子查询
现象：in子查询有时候查不到数据

原因：ignite 的sql引擎不能分布式的支持in查询

INSERT、MERGE语句中的SELECT查询，以及由UPDATE和DELETE操作生成的SELECT查询也是分布式的，可以以并置或非并置的模式执行。

但是，如果WHERE子句中有一个子查询，那么其只能以并置的方式执行。

比如，考虑下面的查询：
```roomsql
DELETE FROM Person WHERE id IN
(SELECT personId FROM Salary s WHERE s.amount > 2000);
```

SQL引擎会生成一个SELECT查询，来获取要删除的条目列表。该查询是分布式的，在整个集群中执行，大致如下：
```roomsql
SELECT _key, _val FROM Person WHERE id IN
(SELECT personId FROM Salary s WHERE s.amount > 2000);
```

但是，IN子句中的子查询（SELECT personId FROM Salary ）并不是分布式的，只能在节点的本地可用数据集上执行。



## 八、使用过程中发现的一些查询限制及建议
字段类型为自定义类型（非java基础数据类型）时，比如Cat类，枚举类型时，无法使用索引。   原因：自定义类型存储在ignite是二进制形式，无法使用索引
字段类型为自定义类型时，作为where条件查询时可能失效。    原因同1
设置联合主键时，创建缓存表后只能进行select、无法进行update和insert。不建议设置多字段的对象复合主键，建议构建拼接字符串主键，比如c1_c2_c3
频繁查询字段尽量使用@QuerySqlField (index = true)注解创建索引
多表查询较慢时，确认多表联查的字段是否建立了索引，没有索引时每行数据join都会进行全表扫描，会严重影响查询速度


## 九、分区数据丢失
现象： 未开启持久化，集群节点故障导致部分表分区异常，查询时报lost partition

解决方案：恢复故障节点，重置缓存分区状态
control.sh --cache reset_lost_partitions cacheName

 