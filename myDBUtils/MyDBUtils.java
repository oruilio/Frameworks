import org.apache.commons.dbutils.ResultSetHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyDBUtils<T> implements ResultSetHandler<T> {

    private Class cls;

    public MyDBUtils(Class cls){
        this.cls = cls;
    }



    @Override
    public T handle(ResultSet resultSet) throws SQLException {
        T res = null;

        try {
            //创建返回结果
            res = (T) cls.getConstructor(null).newInstance(null);

            Map<String, Object> databaseMap = new HashMap<>();
            Map<String, Class> entityMap = new HashMap<>();


            //1-处理resultSet
            while(resultSet.next()){                     //指针下移一行

                ResultSetMetaData metaData = resultSet.getMetaData();     //获取当前行数据结构

                for(int i=1; i<=metaData.getColumnCount(); i++){          //将当前行所有数据一一取出
                    String columnName = metaData.getColumnName(i);
                    String columnType = metaData.getColumnTypeName(i);
                    Object value = null;
                    switch (columnType) {
                        case "VARCHAR":
                            value = resultSet.getString(columnName);
                            break;
                        case "INT":
                            value = resultSet.getInt(columnName);
                            break;
                    }
                    databaseMap.put(columnName, value);                    //存一个量的名字和值
                }
            }

            //2-获取entity结构
            Field[] fields = cls.getDeclaredFields();
            for(Field f: fields){
                entityMap.put(f.getName(), f.getType());
            }


            //3-映射database和entity
            setProperties(databaseMap,entityMap,res);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return res;
    }

    public void setProperties(Map<String, Object> databaseMap, Map<String, Class> entityMap, T res) throws Exception{
        Set<String> keys = databaseMap.keySet();
        for(String k: keys){
            callSetter(k, databaseMap.get(k), entityMap.get(k), res);
        }
    }

    public void callSetter(String var, Object val, Class type, T res) throws Exception{
        String methodName = "set" + var.substring(0,1).toUpperCase() + var.substring(1);
        Method set = cls.getDeclaredMethod(methodName, type);
        set.invoke(res, val);
    }
}
