package nohorjo.dbservice.unauth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import nohorjo.dbservice.ConnectionManager;

public class UnauthDAO {

	public void recordAccessAttempt(String remoteAddr, String info) throws SQLException {
		String sql = "INSERT INTO unauth (remote_address, info) VALUES (?,?)";
		try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, remoteAddr);
			ps.setString(2, info);
			ps.execute();
		}
	}
}
