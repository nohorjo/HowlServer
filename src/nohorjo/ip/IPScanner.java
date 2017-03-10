package nohorjo.ip;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nohorjo.crypto.AESEncryptor;
import nohorjo.http.HttpOperation;

/**
 * Checks the public dynamic IP address and keeps a watch on it, uploading to
 * wordpress if it's changed
 * 
 * @author muhammed
 *
 */
public class IPScanner {

	private static final HttpOperation httpOperation = new HttpOperation();
	private static final AESEncryptor aes = new AESEncryptor();

	private static String currentIP;

	/**
	 * Creates a new thread that scans and updates the public IP address
	 */
	public static void init() {
		System.out.println("Getting public IP address");
		while (true) {
			try {
				updateIP(httpOperation.doGet("http://ipinfo.io/ip"));
				break;
			} catch (Exception e1) {
				e1.printStackTrace();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						String newIP = httpOperation.doGet("http://ipinfo.io/ip");
						if (!newIP.equals(currentIP)) {
							updateIP(newIP);
						}
						Thread.sleep(60000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();

	}

	/**
	 * Updates the IP address on
	 * <a href="https://vermisa.wordpress.com/2016/12/30/piip/" >wordpress<a>,
	 * encrypting it first
	 * 
	 * @param newIP
	 *            the IP address to update
	 * @throws Exception
	 *             on HTTP errors
	 */
	protected static void updateIP(String newIP) throws Exception {
		System.out.println("Setting IP address to " + newIP);
		currentIP = newIP;
		String highlander_comment_nonce = null;
		for (String line : httpOperation.doGet("https://vermisa.wordpress.com/2016/12/30/piip/", "wordpress")
				.split("\n")) {
			if (line.contains("highlander_comment_nonce")) {
				Matcher m = Pattern.compile("value=\"[a-z0-9]{10}").matcher(line);
				if (m.find()) {
					highlander_comment_nonce = m.group().split("\"")[1];
					break;
				}
			}
		}
		System.out.println("NONCE: " + highlander_comment_nonce);
		String enc = aes.encrypt("7449271314", newIP);
		System.out.println("Encrypted IP: " + enc);
		System.out
				.println("POST response: " + httpOperation.doPost("https://vermisa.wordpress.com/wp-comments-post.php",
						"highlander_comment_nonce=" + highlander_comment_nonce,
						"_wp_http_referer=%2F2016%2F12%2F30%2Fpiip%2F", "hc_post_as=guest",
						"comment=" + URLEncoder.encode("£_£".replace("_", enc), "UTF-8"), "comment_post_ID=22",
						"comment_parent=0"));
	}
}
