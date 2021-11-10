import entity.Money;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import untils.JDBCUtils;

import java.sql.Connection;
import java.util.List;

public class DBUtilsDemo {
    public static void main(String[] args) throws Exception{

        Connection connection = JDBCUtils.getConnection();
        QueryRunner queryRunner = new QueryRunner();
        String sql = "select * from money";
//        String sql = "Select * from money where id = ?";
        List<Money> money = queryRunner.query(connection, sql, new MyListHandler<>(Money.class));
        System.out.println(money);

//        for(Money m: list){
//            System.out.println(m);
//        }

//        String sql = "insert into  money(name, money) values(?,?)";
//        int i = queryRunner.update(connection,sql,"Susan",1000);
//
//
//        System.out.println(i);

        JDBCUtils.release(connection,null,null);
    }
}


