package untils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;

public class JDBCUtils {

    //获取链接
    public static Connection getConnection() throws Exception{
        Connection connection = null;
        try {

            ComboPooledDataSource dataSource = new ComboPooledDataSource("testc3p0");
            connection = dataSource.getConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  connection;
    }

    //释放资源
    public static void release (Connection connection, Statement statement, ResultSet resultSet){
        try {

            if (connection != null) connection.close();
            if (statement !=null) statement.close();
            if (resultSet != null) resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
