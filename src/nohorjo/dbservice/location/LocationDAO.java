package nohorjo.dbservice.location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import nohorjo.dbservice.ConnectionManager;

public class LocationDAO {

	public void recordLocation(String latLong) throws SQLException {
		String sql = "INSERT INTO location (lat_long) VALUES (?)";
		try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, latLong);
			ps.execute();
		}
	}

	public LinkedList<String[]> getLastNLocations(int limit) throws SQLException {
		LinkedList<String[]> locationData = new LinkedList<>();
		String sql = "SELECT lat_long, time_of_log FROM location ORDER BY time_of_log DESC LIMIT ?";
		try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String[] ld = new String[2];
					ld[0] = rs.getString("lat_long");
					ld[1] = rs.getTimestamp("time_of_log").toString();
					locationData.add(ld);
				}
			}
		}
		return locationData;
	}
}
