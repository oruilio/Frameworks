Mybatis-个人简易实现

功能:
- 自定义接口
- XML中定义接口方法对应的 SQL 语句
- 根据 XML，通过 JDK 动态代理(反射)完成自定义接口的具体实现
- 自动解析结果集，映射成 XML 中配置的 JavaBean

技术:   
- XML解析+动态代理
