package nohorjo.unauth;

import java.sql.SQLException;

import nohorjo.crypto.EncryptionException;
import nohorjo.dbservice.unauth.UnauthDAO;

public class UnauthorisedAccessException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8695894832882276296L;

	public UnauthorisedAccessException(String remoteAddress, String info) {
		this(remoteAddress, info, null);
	}

	public UnauthorisedAccessException(String remoteAddress, String info, EncryptionException e) {
		super("Unauthorised access attempt by " + remoteAddress + ": " + info, e);
		recordUnauth(remoteAddress, info);
	}

	private void recordUnauth(String remoteAddress, String info) {
		try {
			new UnauthDAO().recordAccessAttempt(remoteAddress, info);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
