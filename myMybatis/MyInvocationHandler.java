package batis;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class MyInvocationHandler implements InvocationHandler {
    private Object object;

    //为目标对象创建动态代理对象
    public Object getInstance(Class cls) {
        Object newProxyInstance = Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class[]{cls},
                this
        );

        return newProxyInstance;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1.解析config.xml文件，获取数据库连接参数
        Map<String, String> dataSourceProperty = getDataSourceProperty();

        //2.使用C3P0创建数据库连接对象
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(dataSourceProperty.get("driver"));
        dataSource.setJdbcUrl(dataSourceProperty.get("url"));
        dataSource.setUser(dataSourceProperty.get("username"));
        dataSource.setPassword(dataSourceProperty.get("password"));

        Connection connection = dataSource.getConnection();

        //3. 执行SQL，获取结果集
        Map<String, String> map = parseStatement(method.getName());

        //4. 根据实体类解析结果集，将数据库字段映射为Java对象
        Object obj = createObj(connection, map.get("sql"), map.get("parameterType"), map.get("resultType"), args);


        return obj;
    }

    /**
     * 执行SQL，映射结果集
     */
    public Object createObj(Connection connection, String sql, String parameterType, String resultType, Object[] args){

        Object obj = null;

        try {
            //执行相应SQL,获取结果集
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if(parameterType != null){
                switch(parameterType){
                    case "java.lang.Integer":
                        preparedStatement.setInt(1, (Integer)args[0]);
                        break;
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            //将结果集映射为相应java对象
            obj = parseObject(resultSet, Class.forName(resultType));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;

    }

    public Object parseObject(ResultSet resultSet, Class cls) throws Exception{

        List list = new ArrayList();
        while(resultSet.next()){
            Object o = cls.getConstructor(null).newInstance(null);
            helper(resultSet,cls, o);
            list.add(o);
        }
        if(list.size()==1) return list.get(0);

        return list;
    }

    public void helper(ResultSet resultSet, Class cls, Object o){

        try {

            ResultSetMetaData metaData = resultSet.getMetaData();

            for(int i=1; i<=metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                Object value = null;
                switch (columnTypeName) {
                    case "INT":
                        value = resultSet.getInt(columnName);
                        break;
                    case "VARCHAR":
                        value = resultSet.getString(columnName);
                        break;
                }

                // 给相应object赋值
                String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                Field declaredField = cls.getDeclaredField(columnName);
                Method method = cls.getMethod(methodName, declaredField.getType());
                method.invoke(o, value);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * 解析SQL语句所在XML文件，获取Statement
     * @return
     */
    public Map<String, String> parseStatement(String methodName){

        Map<String,String> map = new HashMap<>();

        try {
            //解析XML
            SAXReader reader = new SAXReader();
            Document doc =  reader.read("src/main/java/batis/MoneyMapper.xml");

            Element rootElement = doc.getRootElement();
            Iterator<Element> iterator = rootElement.elementIterator();
            while(iterator.hasNext()){
                Element next = iterator.next();
                if(next.attributeValue("id").equals(methodName)){
                    map.put("sql", next.getText().trim());
                    map.put("parameterType", next.attributeValue("parameterType"));
                    map.put("resultType", next.attributeValue("resultType"));
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return map;
    }



    /**
        解析config.xml文件， 获取数据库连接参数
     */
    public Map<String, String> getDataSourceProperty() {

        Map<String, String> map = null;

        try {
            //使用dom4j读取config.xml文件
            SAXReader reader = new SAXReader();
            Document doc = reader.read("src/main/resources/config.xml");
            map = new HashMap<>();

            //解析XML文件 -- config.xml
            Element rootElement = doc.getRootElement();   // configuration
            Iterator<Element> iterator = rootElement.elementIterator();
            while(iterator.hasNext()){
                Element next = iterator.next();           // environments层 & mappers层
                if ("environments".equals(next.getName())){         // 获取environments层
                    Iterator<Element> iterator1 = next.elementIterator();
                    while(iterator1.hasNext()){
                        Element next1 = iterator1.next();    //environment层
                        Iterator<Element> iterator2 = next1.elementIterator();
                        while(iterator2.hasNext()){
                            Element next2 = iterator2.next();    //transactionManager层 & dataSource层
                            if("dataSource".equals(next2.getName())){
                                Iterator<Element> iterator3 = next2.elementIterator();
                                while(iterator3.hasNext()) {
                                    Element next3 = iterator3.next();    //property层
                                    String name = next3.attributeValue("name");
                                    String value = next3.attributeValue("value");
                                    map.put(name, value);
                                }
                            }
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return map;
    }
}
