# 简化版rpc工程。
# 1、用zookeeper作为服务发现中心
# 2、用netty作为通信工具
# 3、protolbuffer 序列化



-------------------------------------------------------------------------
注意curator的版本和服务器zk的安装的版本
The are currently two released versions of Curator, 2.x.x and 3.x.x:

Curator 2.x.x - compatible with both ZooKeeper 3.4.x and ZooKeeper 3.5.x
Curator 3.x.x - compatible only with ZooKeeper 3.5.x and includes support
for new features such as dynamic reconfiguration, etc.
-------------------------------------------------------------------------