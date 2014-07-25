mybatis-utils
=============

1. mybatis代码自动生成工具mybatis-generator-maven-plugin；
	使用方法：修改/src/main/resources/generatorConfig.xml之后，
		运行命令：mvn org.mybatis.generator:mybatis-generator-maven-plugin:1.3.1:generate；
2. 生成mapper.xml的辅助工具类MapperXmlUtil;
3. 物理分页拦截器及缓存拦截器；
