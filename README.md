# 工具介绍
&nbsp;&nbsp;&nbsp;&nbsp;本工具包主要集成了目前在项目开发过程中个人经常会使用到的一些工具类，对工具类进行了一下简单的封装。工具包目前集成了**通用响应实体**、对象拷贝、集合转换、加密工具、格式化工具、随机中文、JSR校验、常用自定义异常、swagger-ui和驼峰转换等工具。


  &nbsp;&nbsp;&nbsp;&nbsp;工具包已经发布到maven中央仓库，目前最新的版本1.0.1-release。其引用座为：
  
		<dependency>
			<groupId>com.yishuifengxiao.common</groupId>
			<artifactId>common-tool</artifactId>
			<version>${版本号}</version>
		</dependency>

---
		
## 工具介绍
-com<br/>
--yishui<br/>
----common<br/>
------tool<br/>
------- bean &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 对象操作有关的工具类<br/>
-------  collections &nbsp; &nbsp; 集合操作有关的工具类<br/>
-------  convert &nbsp; &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;转换操作有关的工具类<br/>
-------  encoder &nbsp; &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;加密有关的工具类<br/>
-------  exception &nbsp; &nbsp;&nbsp; 常用异常类<br/>
-------  format &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 格式化相关的工具类<br/>
-------  random &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 随机工具类<br/>
-------  response &nbsp; &nbsp; &nbsp;&nbsp;&nbsp;通用响应实体<br/>
-------  utils &nbsp; &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;其他工具类<br/>
-------  validate &nbsp; &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;JSR校验相关的工具类<br/>

## 版本更新说明
**1.1.0 版本更新** 
- 修改文件包命名
- 增加对象空属性判断
- 更新自定义异常封装


**1.0.0 版本更新** 
- 更新版本号为1.0.0
- 修改maven坐标
- 升级spring-boot版本为最新的2.1.1
- 增加一些常用的异常类封装

<br/><br/>

**0.2.1 版本更新**

- 修改版本号为0.2.1
- 优化公有返回实体类，增加注释和封装

<br/><br/>

**0.2.0 版本更新**
- 修改版本号为0.2.0
- 增加基于spring的对象拷贝实现类
- 优化项目目录分类

<br/><br/>

**0.1.0 版本更新**
- 修改版本号为0.1.0
- 修改swagger-ui的版本由2.2.2提升为2.9.2
- 增加swagger-bootstrap-ui界面


## 参与贡献
1. Fork 本项目
1. 新建 Feat_xxx 分支
1. 提交代码
1. 新建 Pull Request

##有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* 邮件(zhiyubujian#163.com, 把#换成@)
* QQ: 979653327
* 开源中国: [@止于不见](https://gitee.com/zhiyubujian)
  