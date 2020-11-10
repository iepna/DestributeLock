package com.tp.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Database {

    private static DataSource dataSource = null;

    static {
        Map properties = new HashMap<String, String>();
        properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.mysql.jdbc.Driver");
        properties.put(DruidDataSourceFactory.PROP_URL, "jdbc:mysql://10.2.38.5:3306/extmail?useUnicode=true&characterEncoding=utf8");
        properties.put(DruidDataSourceFactory.PROP_USERNAME, "root");
        properties.put(DruidDataSourceFactory.PROP_PASSWORD, "Passwd#2019");
        properties.put(DruidDataSourceFactory.PROP_MAXACTIVE, "100");
        properties.put(DruidDataSourceFactory.PROP_INITIALSIZE, "1");
        properties.put(DruidDataSourceFactory.PROP_MAXWAIT, "60000");
        properties.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, "60000");
        properties.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, "300000");
        properties.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, "select 1 from dual");
        properties.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, "true");
        properties.put(DruidDataSourceFactory.PROP_TESTONBORROW, "false");
        properties.put(DruidDataSourceFactory.PROP_TESTONRETURN, "false");
        properties.put(DruidDataSourceFactory.PROP_MAXOPENPREPAREDSTATEMENTS, "20");
        try {
            dataSource =  DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入或更新数据库
     * @param sql
     * @return
     */
    public static Integer insertOrUpdate(String sql){
        Integer flag = 0;
        Connection connection = null;
        PreparedStatement ps = null;

        try{
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            flag = ps.executeUpdate();
        }catch (Exception e){
            //e.printStackTrace();
        }finally {
            if (connection!=null){
                try{
                    connection.close();
                }catch (Exception e){
                    connection = null;
                }
            }
            if (ps!=null){
                try{
                    ps.close();
                }catch (Exception e){
                    ps = null;
                }
            }
        }

        return flag;
    }

    /**
     * 插入或更新数据库
     * @param resouceName
     * @return
     */
    public static Map<String,Object> selectByResourceName(String resouceName){
        Map result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "select id,resource_name,share,version,`desc`,update_time from resource where resource_name = ?";

        try{
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1,resouceName);
            rs = ps.executeQuery();
            if (rs.next()){
                result = new HashMap();
                result.put("id",rs.getInt(1));
                result.put("resource_name",rs.getString(2));
                result.put("share",rs.getString(3));
                result.put("version",rs.getLong(4));
                result.put("desc",rs.getString(5));
                result.put("update_time",rs.getTimestamp(6));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection!=null){
                try{
                    connection.close();
                }catch (Exception e){
                    connection = null;
                }
            }
            if (ps!=null){
                try{
                    ps.close();
                }catch (Exception e){
                    ps = null;
                }
            }
            if (rs!=null){
                try{
                    rs.close();
                }catch (Exception e){
                    rs = null;
                }
            }
        }

        return result;
    }

    /**
     * 插入或更新数据库
     * @param owner
     * @return
     */
    public static void lock(String owner){
        Map result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "select id,resource_name,owner,`desc`,update_time from resource_lock where id = 43 for update";

        try{
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            System.out.println("---获取锁---,owner = " + owner);

            Thread.sleep(1000);
            System.out.println("---处理业务---,owner = " + owner);

            connection.commit();
            System.out.println("---释放锁---,owner = " + owner);


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection!=null){
                try{
                    connection.close();
                }catch (Exception e){
                    connection = null;
                }
            }
            if (ps!=null){
                try{
                    ps.close();
                }catch (Exception e){
                    ps = null;
                }
            }
            if (rs!=null){
                try{
                    rs.close();
                }catch (Exception e){
                    rs = null;
                }
            }
        }
    }
}
