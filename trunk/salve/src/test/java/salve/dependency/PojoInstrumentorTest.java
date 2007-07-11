package salve.dependency;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import salve.dependency.impl.PojoInstrumentor;
import salve.dependency.model.EmailSender;
import salve.dependency.model.User;
import salve.dependency.model.UserStore;

// TODO align "mailsender" and "emailsender" names in code
public class PojoInstrumentorTest {
	private static final String USER_CLASS_NAME = "salve.dependency.model.User";

	private ClassPool pool;

	private Locator locator;

	private Class userClass;

	@Before
	public void before() throws Exception {
		initDependencyLibrary();
		initPool();
		initUserClass();
	}

	@Test
	public void testPojoInstrumentor() throws Exception {
		try {

			User user = (User) userClass.newInstance();

			user.setName("jon doe");
			user.setEmail("jon@doe.com");

			EmailSender es = EasyMock.createMock(EmailSender.class);
			UserStore us = EasyMock.createMock(UserStore.class);

			// expect two lookups of email sender, one for each invocation of
			// user#register()
			EasyMock.expect(
					locator.locate(new KeyImpl(EmailSender.class, User.class,
							"mailSender"))).andReturn(es).times(2);

			// expect a single lookup of store as it should be cached in the
			// field
			EasyMock.expect(
					locator.locate(new KeyImpl(UserStore.class, User.class,
							"store"))).andReturn(us).times(1);

			// setup expectations from sideffects of user#register()
			es.send(User.SYSTEM_EMAIL, user.getEmail(), User.REG_EMAIL);
			EasyMock.expectLastCall().times(2);
			es.send(User.SYSTEM_EMAIL, User.SYSTEM_EMAIL, User.REGGED_EMAIL);
			EasyMock.expectLastCall().times(2);
			us.save(user);
			EasyMock.expectLastCall().times(2);

			// execute the test
			EasyMock.replay(locator, es, us);

			user.register();
			user.register();

			EasyMock.verify(locator, es, us);

			UserStore ust = user.getStore();

			// make sure we replaced field write with a noop for an injected
			// field
			user.setStore(new UserStore() {

				public void save(User person) {
				}
			});
			Assert.assertTrue(ust == user.getStore());

			// make sure we replaced field write with a noop for a removed field
			user.setMailSender(new EmailSender() {

				public void send(String from, String to, String msg) {
				}

			});

			EasyMock.reset(locator);
			EasyMock.expect(
					locator.locate(new KeyImpl(EmailSender.class, User.class,
							"mailSender"))).andReturn(es);
			EasyMock.replay(locator);
			Assert.assertTrue(user.getMailSender() == es);
			EasyMock.verify(locator);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void initDependencyLibrary() {
		locator = EasyMock.createMock(Locator.class);
		DependencyLibrary.clear();
		DependencyLibrary.addLocator(locator);
	}

	private void initPool() {
		pool = new ClassPool(ClassPool.getDefault());
		pool.appendClassPath(new ClassClassPath(PojoInstrumentorTest.class));
	}

	private void initUserClass() throws Exception {
		CtClass user1 = pool.get(USER_CLASS_NAME);
		PojoInstrumentor inst = new PojoInstrumentor(user1);
		inst.instrument();
		CtClass user2 = inst.getInstrumented();
		userClass = user2.toClass();
	}

}
