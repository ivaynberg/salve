/**
 * 
 */
package salve.dependency.model;

import salve.dependency.Dependency;
import salve.dependency.InjectionStrategy;

public class User {
	public static final String SYSTEM_EMAIL = "system@sytem.com";

	public static final String REG_EMAIL = "welcome";

	public static final String REGGED_EMAIL = "user registered";

	private String name;

	private String email;

	@Dependency
	private EmailSender mailSender;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private UserStore store;

	public void register() {

		store.save(this);
		mailSender.send(SYSTEM_EMAIL, email, REG_EMAIL);
		mailSender.send(SYSTEM_EMAIL, SYSTEM_EMAIL, REGGED_EMAIL);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EmailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(EmailSender mailSender) {
		this.mailSender = mailSender;
	}

	public UserStore getStore() {
		return store;
	}

	public void setStore(UserStore store) {
		this.store = store;
	}

}