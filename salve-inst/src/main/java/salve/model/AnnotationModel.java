package salve.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import salve.asmlib.Type;

public class AnnotationModel {
	public static class ArrayField extends Field {
		private final List<Field> values = new ArrayList<Field>(1);

		public ArrayField(String name) {
			super(FieldType.ARRAY, name);
		}

		void add(Field field) {
			values.add(field);
		}

		@Override
		public Object getValue() {
			return values;
		}

	}

	public static class EnumField extends Field {
		private final String desc;
		private final String value;

		public EnumField(String name, String desc, String value) {
			super(FieldType.ENUM, name);
			this.value = value;
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public Object getValue() {
			return value;
		}

	}

	public abstract static class Field {
		private final String name;
		private final FieldType type;

		public Field(FieldType type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public FieldType getType() {
			return type;
		}

		public abstract Object getValue();

	}

	public static enum FieldType {
		VALUE, ENUM, ANNOTATION, ARRAY;
	}

	public static class ValueField extends Field {
		private final Object value;

		public ValueField(String name, Object value) {
			super(FieldType.VALUE, name);
			this.value = value;
		}

		@Override
		public Object getValue() {
			return value;
		}

	}

	private final String desc;

	private final boolean visible;

	private final Map<String, Field> fields = new LinkedHashMap<String, Field>();

	public AnnotationModel(String desc, boolean visible) {
		this.desc = desc;
		this.visible = visible;
	}

	AnnotationModel add(Field field) {
		fields.put(field.getName(), field);
		return this;
	}

	public String getDesc() {
		return desc;
	}

	public Field getField(String name) {
		return fields.get(name);
	}

	public String getName() {
		return Type.getType(desc).getInternalName();
	}
}
