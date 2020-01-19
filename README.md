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



**1.2.1 版本更新** 
- 更新依赖的springboot的版本为2.1.1.RELEASE
- 增加主键的雪花生成器功能
- 优化异常类封装体系
- 增加对象空属性过滤方法
- 修复一些其他bug

<br/><br/>



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


<a target="_blank" href="https://www.jetbrains.com/?from=common-tool"><img border="0" src="https://images.gitee.com/uploads/images/2020/0119/090541_0eb1faab_400404.png" alt="IDEA支持" title="IDEA支持"></a> 

  