package 日志;

import org.junit.Test;
import util.JDBCutils;
import util.ORM编程思想2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * sql存在的问题
 * 脏读
 * 不可重复读
 * 幻读
 *
 * 隔离级别
 * MySQL默认是不可重复读
 *
 * 事务的ACID属性，原子性，一致性，隔离性，持久性
 */
public class test02 {
    public static <T> List<T> test1(Connection connection ,Class<T> clazz, String sql, Object... argh) throws SQLException, IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        //填充占位符
        for (int i = 0; i < argh.length; i++) {
            preparedStatement.setObject(i + 1, argh[i]);
        }
        //执行获取结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        //获取结果集的元数据(getMetaData())
        ResultSetMetaData metaData = resultSet.getMetaData();
        //通过ResultSetMetadata获取结果集中的列（getColumnCount）
        int columnCount = metaData.getColumnCount();
        ArrayList<T> list = new ArrayList<>();
        while (resultSet.next()) {
//            ORM编程思想 orm = new ORM编程思想();
            T t = clazz.newInstance();


            //处理结果集一行数据中的每一个列
            for (int c = 0; c < columnCount; c++) {
                //获取列值（etObject(c + 1)）
                Object object = resultSet.getObject(c + 1);
                //获取每个列的列名(getColumnName(c + 1))
                //获取列的别名 String columnLabel = metaData.getColumnLabel(c + 1);
//                String columnName = metaData.getColumnName(c + 1);
                String columnLabel = metaData.getColumnLabel(c + 1);
                //给orm对象指定某个属性赋值,通过反射
                Field declaredField = clazz.getDeclaredField(columnLabel);
                declaredField.setAccessible(true);
                declaredField.set(t, object);

            }
            list.add(t);
        }

        preparedStatement.close();
        resultSet.close();

        return list;

    }

    /**
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * 在Java代码中可以设置隔离级别
     *
     */

    @Test
    public void test01() throws SQLException, IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Connection connection = JDBCutils.getConnection();
        //获取隔离级别
        System.out.println(connection.getTransactionIsolation());
        //设置隔离级别
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        connection.setAutoCommit(false);
        String sql = "select id,`name` from test where id =?";
        List<ORM编程思想2> orm = test1(connection, ORM编程思想2.class, sql, 1000);
        System.out.println(orm);
       connection.close();

    }
    @Test
    public void test02() throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        Connection connection = JDBCutils.getConnection();
        String sql = "update test set `name` = ? where id = ?";
        JDBCutils.update1(connection,sql,"xyy",1000);
        Thread.sleep(15000);
        System.out.println("修改");
        connection.close();
    }
}
