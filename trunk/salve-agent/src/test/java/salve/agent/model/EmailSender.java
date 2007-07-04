/**
 * 
 */
package salve.agent.model;

public interface EmailSender {
	void send(String from, String to, String msg);
}