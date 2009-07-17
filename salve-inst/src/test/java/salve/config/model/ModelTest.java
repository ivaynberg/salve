package salve.config.model;

import junit.framework.TestCase;
import salve.loader.ClassLoaderLoader;
import salve.model.ClassModel;
import salve.model.FieldModel;
import salve.model.MethodModel;
import salve.model.ProjectModel;

public class ModelTest extends TestCase {

	public void test() {
		ClassLoader loader = getClass().getClassLoader();
		ProjectModel model = new ProjectModel(new ClassLoaderLoader(loader));

		ClassModel cm = model.getClass(Person.class.getName().replace(".", "/"));
		assertNotNull(cm);

		FieldModel name = cm.getField("name");
		assertNotNull(name);

		FieldModel address = cm.getField("address");
		assertNotNull(address);

		MethodModel getName = cm.getMethod("getName", "()Ljava/lang/String;");
		assertNotNull(getName);

		MethodModel setName = cm.getMethod("setName", "(Ljava/lang/String;)V");
		assertNotNull(setName);
	}

	public void testMethodOverrides() {
		ClassLoader loader = getClass().getClassLoader();
		ProjectModel model = new ProjectModel(new ClassLoaderLoader(loader));

		ClassModel cm = model.getClass(ContactInfo.class.getName().replace(".", "/"));
		assertNotNull(cm);

		assertNull(cm.getMethod("getPhone", "()Ljava/lang/String;").getSuper());

		MethodModel sm = cm.getMethod("getStreet", "()Ljava/lang/String;").getSuper();
		assertNotNull(sm);
		assertEquals(Address.class.getName().replace(".", "/"), sm.getClassModel().getName());
		assertEquals("getStreet", sm.getName());
		assertEquals("()Ljava/lang/String;", sm.getDesc());
	}
}
