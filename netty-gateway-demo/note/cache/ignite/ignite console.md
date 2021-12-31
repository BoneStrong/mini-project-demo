

# 一、GridGain Web-console安装
GridGain Web-console是官方推出的免费不开源工具

分为两部分：

- webconsole ：提供页面展示的web服务
- webconsole-agent:：作为side car和ignite server node部署在同一个主机，将ignite server node的状态通过 websocket发送给webconsole



下载地址

https://www.gridgain.com/resources/download#webconsole



安装步骤：

1、解压压缩包 gridgain-web-console-on-premise-2020.02.00.zip

2、启动web console

chmod +x web-console.sh


#后台运行
nohup web-console.sh  >webconsole.log  2>&1  &

3.打开webconsole界面，下载webconsole-agent

年金资管平台组 > ignite监控安装 > image2020-4-1_15-49-38.png

4.解压这个webconsole agent到ignite server节点上

5.修改配置参数
```shell
tokens=b16e727f-03f8-4ba9-b605-19085d1e27f5
server-uri=http://localhost:3000   #这里更新为webconsole的地址
#Uncomment following options if needed:
node-uri=http://30.23.77.251:8888 #这里更新为ignite-server的地址
#node-login=ignite
#node-password=ignite
#driver-folder=./jdbc-drivers
#Uncomment and configure following SSL options if needed:
#node-key-store=client.jks
#node-key-store-password=MY_PASSWORD
#node-trust-store=ca.jks
#node-trust-store-password=MY_PASSWORD
#server-key-store=client.jks
#server-key-store-password=MY_PASSWORD
#server-trust-store=ca.jks
#server-trust-store-password=MY_PASSWORD
#passwords-key-store=Path to key store that keeps encrypted passwords
#cipher-suites=CIPHER1,CIPHER2,CIPHER3
```

启动webconsole agent即可

二、ignite-monitor server
以开发环境为例，监控服务ignite-monitor-server的地址为：http://40.23.77.252:9999, 对prometheus暴露的接口为 /prometheus



prometheus配置文件 prometheus.yml内容如下
```yaml
# my global config
global:
scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
# scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
alertmanagers:
- static_configs:
    - targets:
      # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
# - "first_rules.yml"
# - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
# The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
- job_name: 'ignite-monitor'

  scrape_interval: 5s
  scrape_timeout: 5s
  metrics_path: /prometheus
  scheme: http

  static_configs:
    - targets: ['localhost:9090','40.23.77.252:9999'] #这里配置ignite-monitor-server地址
      简单以docker容器作为示例
```

```shell
mkdir -p /opt/dzz/prometheus/

#复制上面prometheus.yml到这个目录，启动prometheus
docker run -d -p 9090:9090 --name dzz-prometheus -v /opt/dzz/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus


#配置grafanana
docker run -d --name=dzz-grafana -p 3000:3000 grafana/grafana
```

访问 http://127.0.0.1:3000 默认账号密码admin/admin 第一次登陆需要修改密码

查看
rest接口相关命令 MetricsCommand   CacheMetrics





 