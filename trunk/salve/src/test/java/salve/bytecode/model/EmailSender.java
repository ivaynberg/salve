/**
 * 
 */
package salve.bytecode.model;

public interface EmailSender {
	void send(String from, String to, String msg);
}