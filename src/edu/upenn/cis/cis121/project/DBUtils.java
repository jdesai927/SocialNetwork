package edu.upenn.cis.cis121.project;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An implementation of some basic database utilities.
 * 
 * @author Julia Stoyanovich (jstoy@cis.upenn.edu)
 * 
 */
public class DBUtils {

  public static Connection _conn;

  /**
   * Open a database connection.
   * 
   * @param user
   * @param pass
   * @param SID
   * @param host
   * @return database connection
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public static Connection openDBConnection(String user, String pass,
      String SID, String host, int port) throws SQLException,
      ClassNotFoundException {

    if ((_conn == null) || _conn.isClosed()) {
      String driver = "oracle.jdbc.driver.OracleDriver";
      String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + SID;
      String username = user;
      String password = pass;
      Class.forName(driver);
      
      _conn = DriverManager.getConnection(url, username, password);
    }
    return _conn;
  }

  /**
   * Test the connection.
   * @return 'servus' if the connection is open.  Otherwise an exception will be thrown.
   * @throws SQLException
   */
  public static String testConnection() throws SQLException {
    Statement st = _conn.createStatement();
    ResultSet rs = st.executeQuery("select 'servus' res from dual");
    String res = "";
    while (rs.next()) {
      res = rs.getString("res");
    }
    rs.close();
    st.close();
    return res;
  }

  /**
   * Close the database connection.
   * @throws SQLException if connection cannot be closed.
   */
  public static void closeDBConnection() throws SQLException {
    if ((_conn != null) && !_conn.isClosed()) {
      _conn.close();
    }
  }

  /**
   * Get an integer that is returned as a result of a query.
   * @param query
   * @return result
   * @throws SQLException
   */
  public static int getIntFromDB(String query)
      throws SQLException {

    Statement st = _conn.createStatement();
    ResultSet rs = st.executeQuery(query);
    int ret = Integer.MIN_VALUE;
    if (rs.next()) {
      ret = rs.getInt(1);
    }
    rs.close();
    st.close();
    return ret;
  }

  /**
   * Execute an update or a delete query.
   * @param query
   * @throws SQLException
   */
  public static void executeUpdate(String query)
      throws SQLException {

    Statement st = _conn.createStatement();
    st.executeUpdate(query);
    st.close();
  }

}
