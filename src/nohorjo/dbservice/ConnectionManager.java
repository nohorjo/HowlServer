package nohorjo.dbservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nohorjo.howlserver.Settings;

public class ConnectionManager {

	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(Settings.getProperty("DB_URL"), Settings.getProperty("DB_USER"),
				Settings.getProperty("DB_PASSWORD"));
	}

	public static void test() throws SQLException {
		try (Connection c = getConnection();
				PreparedStatement p = c.prepareStatement("SELECT 'System is live';");
				ResultSet r = p.executeQuery()) {
			r.next();
			System.out.println(r.getString(1));
		}
	}
}
