1.上传脚本到对应的主机
2.建议用root权限运行，切换执行用户为root：su - root
3.cd 到脚本的路径
4.测试脚本是否正常 sh -n repair_security_baseline-20210312.sh ，如果没有报错则是正常的。
5.sh repair_security_baseline-20210127.sh  运行。


常见报错处理：
1、如果出现$'\r' command not found报错
执行yum -y install dos2unix 和 dos2unix repair_security_baseline-20210312.sh 。