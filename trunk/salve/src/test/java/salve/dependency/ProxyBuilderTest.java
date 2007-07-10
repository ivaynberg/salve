package salve.dependency;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javassist.ClassClassPath;
import javassist.ClassPool;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import salve.dependency.Dependency;
import salve.dependency.DependencyLibrary;
import salve.dependency.Key;
import salve.dependency.Locator;
import salve.dependency.impl.ProxyBuilder;

public class ProxyBuilderTest {
	private static ClassPool pool;
	private static Class personProxy;

	@Test
	public void testProxyBuilderOnClass() throws Exception {
		final Person person = new Person();

		DependencyLibrary.clear();
		DependencyLibrary.addLocator(new Locator() {

			public Object locate(Key key) {
				Assert
						.assertTrue(key.getDependencyClass().equals(
								Person.class));
				return person;
			}

		});

		Person person2 = (Person) personProxy.getConstructor(
				new Class[] { Key.class }).newInstance(
				new Object[] { Holder.__key$person });

		person2.setName("name");
		Assert.assertEquals(person2.getName(), "name");
	}

	@Test
	public void testProxyBuilderOnInterface() throws Exception {
		ProxyBuilder builder = new ProxyBuilder(pool, EmailSender.class);
		Class proxy = builder.build().toClass();

		final EmailSenderImpl sender = new EmailSenderImpl();

		DependencyLibrary.clear();
		DependencyLibrary.addLocator(new Locator() {

			public Object locate(Key key) {
				Assert.assertTrue(key.getDependencyClass().equals(
						EmailSender.class));
				return sender;
			}

		});

		EmailSender sender2 = (EmailSender) proxy.getConstructor(
				new Class[] { Key.class }).newInstance(
				new Object[] { Holder.__key$sender });

		sender2.send("from", "to", "msg");
		Assert.assertTrue(sender.isSendCalled());
		Assert.assertEquals(sender, sender2);
		Assert.assertEquals(sender.hashCode(), sender2.hashCode());
		Assert.assertEquals(sender.toString(), sender2.toString());

	}

	@Test
	public void testSerializationWeaving() throws Exception {
		final Person person = new Person();
		try {
			pipe(person);
			Assert.fail("Person should not be serializable");
		} catch (Exception e) {
			// noop
		}

		DependencyLibrary.clear();
		DependencyLibrary.addLocator(new Locator() {

			public Object locate(Key key) {
				Assert.assertNotNull(key);
				Assert
						.assertTrue(key.getDependencyClass().equals(
								Person.class));
				return person;
			}

		});

		Person person2 = (Person) personProxy.getConstructor(
				new Class[] { Key.class }).newInstance(
				new Object[] { Holder.__key$person });

		Assert.assertNotNull(person2);

		Person person3 = (Person) pipe(person2);

		Assert.assertNotNull(person3);

		person3.setName("bob");
		Assert.assertEquals("bob", person.getName());

	}

	private Object pipe(Object o) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();

	}

	@BeforeClass
	public static void init() throws Exception {
		initPool();
		initProxy();
	}

	private static void initPool() {
		pool = new ClassPool(ClassPool.getDefault());
		pool.appendClassPath(new ClassClassPath(ProxyBuilderTest.class));
	}

	private static void initProxy() throws Exception {
		personProxy = new ProxyBuilder(pool, Person.class).build().toClass();
	}

	public interface EmailSender {
		void send(String from, String to, String msg);
	}

	public static class Holder {
		@Dependency
		public static final Key __key$person = new Key(Person.class,
				Holder.class, "person");

		@Dependency
		public static final Key __key$sender = new Key(EmailSender.class,
				Holder.class, "sender");

		@Dependency
		private Person person;

		@Dependency
		private EmailSender sender;

	}

	public static class Person {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	private class EmailSenderImpl implements EmailSender {
		private boolean sendCalled = false;

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj == null) {
				return false;
			} else {
				return obj instanceof EmailSender;
			}
		}

		public boolean isSendCalled() {
			return sendCalled;
		}

		public void send(String from, String to, String msg) {
			sendCalled = true;
		}
	}
}
