package nohorjo.howlserver;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import nohorjo.common.AutoDeletingSet;
import nohorjo.common.CommonUtils;
import nohorjo.crypto.EncryptionException;
import nohorjo.crypto.TimeBasedEncryptor;
import nohorjo.dbservice.ConnectionManager;
import nohorjo.dbservice.connection.ConnectionLogDAO;
import nohorjo.dbservice.location.LocationDAO;
import nohorjo.delegation.Action;
import nohorjo.ip.IPScanner;
import nohorjo.socket.SocketServer;
import nohorjo.unauth.UnauthorisedAccessException;

public class HowlServer {

	private static final String KEY = Settings.getProperty("ENC_PASSWORD");
	private static final int PORT = Integer.parseInt(KEY.substring(0, 4));
	private static final byte EOT = '\4';

	private static final SocketServer server = new SocketServer(PORT);
	private static final TimeBasedEncryptor tbe = new TimeBasedEncryptor();
	private static final Set<String> recentlyUsed = new AutoDeletingSet<>(60000);
	private static Map<String, String> sb = new HashMap<>();

	public static void main(String[] args) throws IOException, SQLException {
		ConnectionManager.test();
		IPScanner.init();
		Action onReceive = new Action() {
			Map<String, String> messages = new HashMap<>();

			@Override
			public Object run(Object... args) {
				byte data = (byte) args[0];
				String remoteAddress = (String) args[1];
				String message = messages.remove(remoteAddress);
				if (message == null) {
					message = "";
				}
				if (data == EOT) {
					processMessage(message, remoteAddress);
				} else {
					message += (char) data;
					messages.put(remoteAddress, message);
				}
				return null;
			}
		};
		Action onNewConnection = new Action() {
			@Override
			public Object run(Object... args) {
				logConnection(((Socket) args[0]).getRemoteSocketAddress().toString(), true);
				return null;
			}
		};
		Action onDisconnect = new Action() {
			@Override
			public Object run(Object... args) {
				logConnection(((Socket) args[0]).getRemoteSocketAddress().toString(), false);
				return null;
			}
		};
		server.setActions(onReceive, onNewConnection, onDisconnect);
		server.start();
		System.out.println("Server started on port: " + PORT);
		while (true)
			continue;
	}

	protected static void processMessage(String message, String remoteAddress) {
		try {
			try {
				if (!recentlyUsed.add(message)) {
					throw new UnauthorisedAccessException(remoteAddress, "Repeated command received: " + message);
				}
				String decrypted = tbe.decrypt(KEY, message);
				String[] messageData = decrypted.split(":");
				System.out.println("Decrypted message = " + decrypted);
				LocationDAO dao = new LocationDAO();
				switch (messageData[0]) {
				case "H":
					server.sendAll(tbe.encrypt(KEY, "hb").getBytes());
					server.sendAll(EOT);
					break;
				case "RETRY":
					messageData[1] = sb.remove(messageData[1]);
				case "FORWARD":
					String forwardMessage = messageData[1];
					String encrypted = (tbe.encrypt(KEY, forwardMessage));
					sb.put(encrypted, forwardMessage);
					server.sendAll(encrypted.getBytes());
					server.sendAll(EOT);
					break;
				case "LOCATION":
					dao.recordLocation(messageData[1]);
					break;
				case "GET":
					LinkedList<String[]> locations = dao.getLastNLocations(Integer.parseInt(messageData[1]));
					server.send(remoteAddress, Base64.getEncoder().encode(CommonUtils.serialize(locations)));
					server.send(remoteAddress, EOT);
					break;
				}
			} catch (UnauthorisedAccessException e) {
				throw e;
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			} catch (EncryptionException e) {
				throw new UnauthorisedAccessException(remoteAddress, "Unable to decrypt message: " + message, e);
			}
		} catch (UnauthorisedAccessException e) {
			e.printStackTrace();
		}
	}

	protected static void logConnection(String remoteSocketAddress, boolean isConnect) {
		ConnectionLogDAO dao = new ConnectionLogDAO();
		try {
			dao.recordConnection(remoteSocketAddress, isConnect);
		} catch (SQLException e) {
			System.err.println("Could not record connection " + (isConnect ? "to " : "from ") + remoteSocketAddress);
			e.printStackTrace();
		}
	}

}
