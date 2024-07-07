## 这里仅公开导航器！

 * 因为是拿[学长代码](https://github.com/yangxiaozhao/NCEPU_SoftwareEngineering_Experiment)改的，我没征求人家同意,所以只把学长已经公开了的导航器代码放上来，别的请私信我索要，遗憾的是我不一定看私信。
   已经经过我大幅修改，相关参与者还有通义灵码，此外，不愿透露姓名的董先生帮我看了看算法。

## 控制器

* 控制器使用java语言开发,并使用Maven进行构建和管理，开发环境为IDEA，打包为origin_control-1.0-SNAPSHOT.jar文件，在命令行中运行即可。


## 导航器

* 导航器使用java语言开发,并使用Maven进行构建和管理，开发环境为IDEA，打包为self_daohang-1.0-SNAPSHOT.jar，在命令行中运行。

## 小车

* 小车使用java语言开发,并使用Maven进行构建和管理，开发环境为IDEA，打包为origin_car-1.0-SNAPSHOT.jar，在命令行中运行。


* 运行小车时应当附带参数，其参数格式为car+数字，数字应当从1开始，随小车数量依次增大，最大为4，每次命令会启动名为参数的一台小车，各小车独立运行。

## 显示及回放组件

* 显示组件使用C#语言开发，C#语言使用.NET SDK (5.0.416)，IDE使用Visual Studio Community 2019，
使用NuGet包（CSRedisCore、Newtonsoft.Json）进行Redis读写和Json反序列化。已将项目打包成MapExploreView.exe，双击运行即可。

* 当地图编辑完毕，并且程序正常运行时，显示组件退出也没关系了，可以点击观看按钮随时回去观战。

## 运行须知

* JAVA的SDK应当使用Oracle OpenJDK version 17.0.1，经过我们实际测试，新版本的各种JDK均可运行，不必刻意修改。

* Maven相关依赖请参考源代码文件包中的pom.xml，在此不一一列出，显示组件额外使用了Fody和Costura.Fody便于打包，如果你希望在本地运行本代码，请安装RabbitMQ和Redis的server端，包括Erlang，修改代码里的IP和用户名，密码，建议你下载一个Another Redis Desktop Manager监视自己的Redis。

*  Redis version=5.0.14.1，RabbitMQ server version=3.6.5，请关闭redis对外来连接的protect mode，RabbitMQ应当添加账户和密码，guest是不能被其他主机访问的，在浏览器输入http://localhost:15672/#/users,点击Admin，添加一个用户，然后点击已有的用户名那里，给他set permission。

* 运行顺序为 

    1.打开显示组件，设置地图和障碍物。
    
    2.部署小车。
    
    3.打开导航器。

    4.打开控制器，并点击Start按钮。
  ## 存在的问题
  
  1. 不支持多个导航器同时运行，可能会造成争抢，异常退出，并导致控制器找不到Car信息，无法发消息，小车收不到消息，所以也不写入Car信息。
  2. 只支持四台车，属于是我懒得改了，因为显示组件那里把车数给定义死了，我也没修改。
  3. 显示组件有时候会出现数组越界的情况，主要位于MapView那里，我不知道为什么，加了一个如果目标位置为空就返回的if语句，但是其实治标不治本。
  4. 程序结束时，可能会出现某些队列为空的报错，没关系。
  5. 导航器的RedisWR里有时候出现jedis.exist返回值不匹配的问题，可能是线程并发引起的，我猜可以用pool避免，我设置了重试处理，基本没问题，但是如果出现事务在撤销前没有multi的报错，会导致整个程序崩溃，我也不知道怎么办。
