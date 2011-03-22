package org2.eclipse.dltk.compiler;

/**
 * Exception thrown by a scanner when encountering lexical errors.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 */
public class InvalidInputException extends Exception {

	private static final long serialVersionUID = 2909732853499731592L; // backward
																		// compatible

	/**
	 * Creates a new exception with no detail message.
	 */
	public InvalidInputException() {
		super();
	}

	/**
	 * Creates a new exception with the given detail message.
	 * 
	 * @param message
	 *            the detail message
	 */
	public InvalidInputException(String message) {
		super(message);
	}
}
