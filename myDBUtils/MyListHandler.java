import org.apache.commons.dbutils.ResultSetHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class MyListHandler<T> implements ResultSetHandler<List<T>> {

    private Class cls;

    public MyListHandler(Class cls){
        this.cls = cls;
    }


    @Override
    public List<T> handle(ResultSet resultSet) throws SQLException {

        List<T> list = new ArrayList<>();

        while(resultSet.next()){
            list.add(helper(resultSet));
        }


        return list;
    }

    public T helper(ResultSet resultSet) throws SQLException {

        T ele = null;

        try {
            ele = (T) cls.getConstructor(null).newInstance(null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Map<String, Object> databaseMap = new HashMap<>();
        Map<String, Class> entityMap = new HashMap<>();

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

        Field[] fields = cls.getDeclaredFields();
        for(Field f: fields){
            entityMap.put(f.getName(), f.getType());
        }

        setProperties(databaseMap,entityMap,ele);

        return ele;

    }


    public void setProperties(Map<String, Object> databaseMap, Map<String, Class> entityMap, T res){
        try {
            Set<String> keys = databaseMap.keySet();
            for(String k: keys){
                callSetter(k, databaseMap.get(k), entityMap.get(k), res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callSetter(String var, Object val, Class type, T res) {
        try {
            String methodName = "set" + var.substring(0,1).toUpperCase() + var.substring(1);
            Method set = cls.getDeclaredMethod(methodName, type);
            set.invoke(res, val);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
