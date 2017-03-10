package nohorjo.dbservice.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import nohorjo.dbservice.ConnectionManager;

public class ConnectionLogDAO {
	public void recordConnection(String remoteAddress, boolean isConnect) throws SQLException {
		System.out.println(
				"Recording " + (isConnect ? "connection" : "disconnection") + " activity to: " + remoteAddress);
		String sql = "INSERT INTO connection_log (remote_address, type) VALUES (?,?)";
		try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, remoteAddress);
			ps.setString(2, isConnect ? "Connect" : "Disconnect");
			ps.execute();
		}
	}
}
