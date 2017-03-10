import java.io.IOException;

import nohorjo.crypto.EncryptionException;
import nohorjo.crypto.TimeBasedEncryptor;
import nohorjo.delegation.Action;
import nohorjo.socket.SocketClient;

public class Call {
	private static final byte EOT = '\4';
	private static final String KEY = "7449271314";
	private static TimeBasedEncryptor tbe = new TimeBasedEncryptor();

	public static void main(String[] args) throws IOException, EncryptionException {
		String message = args[0];
		if (message.trim().equals("")) {
			return;
		}
		message = "FORWARD:" + message;
		SocketClient client = getClient();
		client.connect();
		client.send(tbe.encrypt(KEY, message).getBytes());
		client.send(EOT);
		client.close();
		System.out.println("Forwarded message: " + message);
	}

	private static SocketClient getClient() {
		SocketClient client;
		while (true) {
			try {
				client = new SocketClient("80.3.190.65", 7449);
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		client.setActions(new Action() {

			@Override
			public Object run(Object... args) {
				return null;
			}
		}, new Action() {

			@Override
			public Object run(Object... args) {
				return null;
			}
		});
		return client;
	}
}
