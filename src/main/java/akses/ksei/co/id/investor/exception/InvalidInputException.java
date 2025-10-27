package akses.ksei.co.id.investor.exception;

public class InvalidInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidInputException() {
	}

	public InvalidInputException(String msg) {
		super(msg);
	}

	public InvalidInputException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InvalidInputException(Throwable cause) {
		super(cause);
	}

}
