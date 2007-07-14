package salve.util;

import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

public class JavassistUtil {
	private JavassistUtil() {

	}

	public static Annotation getAnnot(AnnotationsAttribute attr,
			Class<? extends java.lang.annotation.Annotation> annotType) {
		if (attr != null && attr.numAnnotations() > 0) {

			for (Annotation annot : attr.getAnnotations()) {
				if (annot.getTypeName().equals(annotType.getName())) {
					return annot;
				}
			}
		}
		return null;
	}

	public static Annotation getAnnot(CtClass clazz,
			Class<? extends java.lang.annotation.Annotation> annotType) {
		AnnotationsAttribute attr = (AnnotationsAttribute) clazz.getClassFile()
				.getAttribute(AnnotationsAttribute.visibleTag);
		return getAnnot(attr, annotType);
	}

	public static Annotation getAnnot(CtMethod method,
			Class<? extends java.lang.annotation.Annotation> annotType) {
		AnnotationsAttribute attr = (AnnotationsAttribute) method
				.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
		return getAnnot(attr, annotType);
	}

	public static void removeAnnot(AnnotationsAttribute attr,
			Class<? extends java.lang.annotation.Annotation> toRemove) {
		Annotation[] annots = attr.getAnnotations();
		boolean hasAnnotation = false;
		for (Annotation annot : annots) {
			if (toRemove.getName().equals(annot.getTypeName())) {
				hasAnnotation = true;
			}
		}
		if (!hasAnnotation) {
			return;
		}

		Annotation[] newAnnots = new Annotation[annots.length - 1];
		int pos = 0;
		boolean removed = false;
		for (int i = 0; i < annots.length; i++) {
			final Annotation current = annots[i];
			if (!current.getTypeName().equals(toRemove.getName())) {
				newAnnots[pos] = current;
				pos++;
			}
		}

		attr.setAnnotations(newAnnots);
	}

	public static void removeAnnot(CtClass clazz,
			Class<? extends java.lang.annotation.Annotation> type) {
		AnnotationsAttribute attr = (AnnotationsAttribute) clazz.getClassFile()
				.getAttribute(AnnotationsAttribute.visibleTag);
		removeAnnot(attr, type);
		clazz.setAttribute(AnnotationsAttribute.visibleTag, attr.get());
	}

	public static void removeAnnot(CtMethod method,
			Class<? extends java.lang.annotation.Annotation> type) {
		AnnotationsAttribute attr = (AnnotationsAttribute) method
				.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
		removeAnnot(attr, type);
		method.setAttribute(AnnotationsAttribute.visibleTag, attr.get());
	}

	public static void substituteAnnot(AnnotationsAttribute attr,
			Class<? extends java.lang.annotation.Annotation> type,
			Class<? extends java.lang.annotation.Annotation> substitute) {

		if (attr == null) {
			return;
		}

		boolean modified = false;
		Annotation[] annots = attr.getAnnotations();
		if (annots != null) {
			for (int i = 0; i < annots.length; i++) {
				final Annotation annot = annots[i];
				if (annot.getTypeName().equals(type.getName())) {
					Annotation sub = new Annotation(substitute.getName(), attr
							.getConstPool());
					final Set<String> members = annot.getMemberNames();
					if (members != null) {
						for (String member : members) {
							final MemberValue value = annot
									.getMemberValue(member);
							sub.addMemberValue(member, value);
						}
					}
					annots[i] = sub;
					modified = true;
					break;
				}
			}
			if (modified) {
				attr.setAnnotations(annots);
			}
		}

	}

	public static void substituteAnnot(CtClass clazz,
			Class<? extends java.lang.annotation.Annotation> type,
			Class<? extends java.lang.annotation.Annotation> substitute) {
		substituteAnnot(getAnnotAttr(clazz), type, substitute);

	}

	public static void substituteAnnot(CtMethod method,
			Class<? extends java.lang.annotation.Annotation> type,
			Class<? extends java.lang.annotation.Annotation> substitute) {
		substituteAnnot(getAnnotAttr(method), type, substitute);
	}

	private static AnnotationsAttribute getAnnotAttr(CtClass clazz) {
		return (AnnotationsAttribute) clazz.getClassFile().getAttribute(
				AnnotationsAttribute.visibleTag);
	}

	private static AnnotationsAttribute getAnnotAttr(CtMethod method) {
		return (AnnotationsAttribute) method.getMethodInfo().getAttribute(
				AnnotationsAttribute.visibleTag);
	}

	private static void setAnnotAttr(CtMethod method, AnnotationsAttribute attr) {
		method.setAttribute(AnnotationsAttribute.visibleTag, attr.get());
	}

	public static class Strings {
		private Strings() {

		}

		public static String join(CtClass... items) {
			return joinInto(new StringBuilder(), items).toString();
		}

		public static StringBuilder joinInto(StringBuilder builder,
				CtClass... items) {
			for (int i = 0; i < items.length; i++) {
				builder.append(items[i].getName());
				builder.append(".class");
				if (i < items.length - 1) {
					builder.append(",");
				}
			}
			return builder;
		}
	}
}
