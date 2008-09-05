/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.util.asm;

import salve.asmlib.Type;
import salve.asmlib.signature.SignatureReader;
import salve.asmlib.signature.SignatureVisitor;

/**
 * Asm related utilities
 * 
 * @author ivaynberg
 * 
 */
public class AsmUtil {
	public static class Pair<K, V> {
		private K key;
		private V value;

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public void setValue(V value) {
			this.value = value;
		}
	}

	public static class Types {
		public static Type CLASS = Type.getType(Class.class);

		private Types() {

		}
	}

	private static final String DOUBLEDESC = "Ljava/lang/Double;";
	private static final String FLOATDESC = "Ljava/lang/Float;";
	private static final String LONGDESC = "Ljava/lang/Long;";
	private static final String INTEGERDESC = "Ljava/lang/Integer;";
	private static final String SHORTDESC = "Ljava/lang/Short;";
	private static final String BYTEDESC = "Ljava/lang/Byte;";

	private static final String BOOLEANDESC = "Ljava/lang/Boolean;";

	private static final String CHARDESC = "Ljava/lang/Character;";

	/**
	 * Checks if type is byte or Byte
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is byte or Byte, false otherwise
	 */
	public static boolean isByte(Type type) {
		return Type.BYTE == type.getSort() || "Ljava/lang/Byte;".equals(type.getDescriptor());
	}

	/**
	 * Checks if type is double or Double
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is double or Double, false otherwise
	 */
	public static boolean isDouble(Type type) {
		return Type.DOUBLE == type.getSort() || "Ljava/lang/Double;".equals(type.getDescriptor());
	}

	/**
	 * Checks if type is float or Float
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is float or Float, false otherwise
	 */
	public static boolean isFloat(Type type) {
		return Type.FLOAT == type.getSort() || "Ljava/lang/Float;".equals(type.getDescriptor());
	}

	/**
	 * Checks if type is int or Integer
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is int or Integer, false otherwise
	 */
	public static boolean isInteger(Type type) {
		return Type.INT == type.getSort() || "Ljava/lang/Integer;".equals(type.getDescriptor());
	}

	/**
	 * Checks if type is long or Long
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is long or Long, false otherwise
	 */
	public static boolean isLong(Type type) {
		return Type.LONG == type.getSort() || "Ljava/lang/Long;".equals(type.getDescriptor());
	}

	/**
	 * Checks if type is primitive
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is primitive
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY;
	}

	public static boolean isSet(int value, int flag) {
		return (value & flag) > 0;
	}

	/**
	 * Checks if type is short or Short
	 * 
	 * @param type
	 *            type to check
	 * @return true if type is short or Short, false otherwise
	 */

	public static boolean isShort(Type type) {
		return Type.SHORT == type.getSort() || "Ljava/lang/Short;".equals(type.getDescriptor());
	}

	public static String parseListTypeFromSignature(final String signature) {
		final Pair<String, String> types = new Pair<String, String>();

		class Visitor extends SignatureVisitorAdapter {
			private int mode;

			@Override
			public void visitClassType(String name) {
				if (mode == 0) {
					// do nothing
				} else if (mode == 1) {
					types.setKey(name);
				} else {
					throw new InvalidSignatureException(signature);
				}
			}

			@Override
			public SignatureVisitor visitTypeArgument(char wildcard) {
				mode++;
				return this;
			}

		}

		SignatureReader reader = new SignatureReader(signature);
		reader.accept(new Visitor());
		if (types.getKey() == null) {
			throw new InvalidSignatureException(signature);
		}
		return types.getKey();
	}

	public static Pair<String, String> parseMapTypesFromSignature(final String signature) {
		final Pair<String, String> types = new Pair<String, String>();
		class Visitor extends SignatureVisitorAdapter {
			private int mode;

			@Override
			public void visitClassType(String name) {
				if (mode == 0) {
					// do nothing
				} else if (mode == 1) {
					types.setKey(name);
				} else if (mode == 2) {
					types.setValue(name);
				} else {
					throw new InvalidSignatureException(signature);
				}
			}

			@Override
			public SignatureVisitor visitTypeArgument(char wildcard) {
				mode++;
				return this;
			}

		}

		SignatureReader reader = new SignatureReader(signature);
		reader.accept(new Visitor());
		if (types.getKey() == null || types.getValue() == null) {
			throw new InvalidSignatureException(signature);
		}
		return types;
	}

	/**
	 * Converts type to its primitive equivalent. If provided type is already a
	 * primitive this method is a noop.
	 * 
	 * @param type
	 *            type to convert to primitive
	 * @return primitive equivalent of the type
	 * @throws IllegalArgumentException
	 *             if type doesnt have a primitive counterpart
	 */
	public static Type toPrimitive(Type type) {
		int sort = type.getSort();
		if (sort == Type.ARRAY) {
			throw new IllegalArgumentException("Type `" + type.toString() + "` does not have a primitive counterpart");
		}
		if (sort == Type.OBJECT) {
			String desc = type.getDescriptor();
			if (DOUBLEDESC.equals(desc)) {
				return Type.DOUBLE_TYPE;
			} else if (FLOATDESC.equals(desc)) {
				return Type.FLOAT_TYPE;
			} else if (LONGDESC.equals(desc)) {
				return Type.LONG_TYPE;
			} else if (INTEGERDESC.equals(desc)) {
				return Type.INT_TYPE;
			} else if (SHORTDESC.equals(desc)) {
				return Type.SHORT_TYPE;
			} else if (BYTEDESC.equals(desc)) {
				return Type.BYTE_TYPE;
			} else if (BOOLEANDESC.equals(desc)) {
				return Type.BOOLEAN_TYPE;
			} else if (CHARDESC.equals(desc)) {
				return Type.CHAR_TYPE;
			} else {
				throw new IllegalStateException("Cannot convert type `" + desc + "` to its primitive counterpart`");
			}
		} else {
			return type;
		}

	}

	private AsmUtil() {

	}

}
