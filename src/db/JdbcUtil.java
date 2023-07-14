package db;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JdbcUtil {	
	public static Connection getCon() {
		System.out.println("<도서관과 세션 연결중>");
		Properties prop = new Properties();
		Connection con = null;
		try {
			prop.load(new FileReader("jdbc.properties"));
			String url = prop.getProperty("url");
			String id = prop.getProperty("id");
			String pwd = prop.getProperty("pwd");
			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection(url, id, pwd);
			System.out.println("<연결성공>");
			return con;
		}catch (IOException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e1){
			e1.printStackTrace();
		}catch (SQLException e2) {
			e2.printStackTrace();
		}
		return con;
	}
	
	public static void close(Connection con) {
		try {
			if (con != null) con.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(Statement stmt) {
		try {
			if (stmt != null) stmt.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(ResultSet rs) {
		try {
			if (rs != null) rs.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(Connection con, Statement stmt, ResultSet rs) {
		try {
			if (con != null) con.close();
			if (stmt != null) stmt.close();
			if (rs != null) rs.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
