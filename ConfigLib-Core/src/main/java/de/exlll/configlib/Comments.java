package de.exlll.configlib;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.NestedComment;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instances of this class contain all comments of a {@link Configuration} class
 * and its fields.
 */
public final class Comments {
    private final List<String> classComments;
    private final Map<String, List<String>> fieldComments;

    private Comments(List<String> classComments,
                     Map<String, List<String>> fieldComments) {
        this.classComments = classComments;
        this.fieldComments = fieldComments;
    }

    static Comments ofClass(Class<?> cls) {
        List<String> classComments = getComments(cls);
        Map<String, List<String>> fieldComments = new HashMap<>();
        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> isCommented(field) || isNestedCommented(field))
                .forEach(field -> {
                    if (isCommented(field)) {
                        fieldComments.put(field.getName(), Comments.getComments(field));
                    }
                    if (isNestedCommented(field)) {
                        controlInComment(field, fieldComments, "");
                    }
                });
        return new Comments(classComments, fieldComments);
    }

    private static void controlInComment(Field field, Map<String, List<String>> map, String main) {
        Class clazz = field.getType();
        if (main.equals("") || main.isEmpty()) {
            main = field.getName() + ".";
        }
        if (clazz.getDeclaredFields().length > 0) {
            String finalMain = main;
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> isCommented(f) || isNestedCommented(f))
                    .forEach(f -> {
                        if (isCommented(f)) {
                            map.put(finalMain + f.getName(), Comments.getComments(f));
                        }
                        if (isNestedCommented(f)) {
                            controlInComment(f, map, finalMain + f.getName() + ".");
                        }
                    });
        }
    }

    private static boolean isCommented(AnnotatedElement element) {
        return element.isAnnotationPresent(Comment.class);
    }

    private static boolean isNestedCommented(AnnotatedElement element) {
        return element.isAnnotationPresent(NestedComment.class);
    }

    private static List<String> getComments(AnnotatedElement element) {
        Comment comment = element.getAnnotation(Comment.class);
        return (comment != null)
                ? Arrays.asList(comment.value())
                : Collections.emptyList();
    }

    /**
     * Returns if the {@code Configuration} this {@code Comments} object belongs to
     * has class comments.
     *
     * @return true, if {@code Configuration} has class comments.
     */
    public boolean hasClassComments() {
        return !classComments.isEmpty();
    }

    /**
     * Returns if the {@code Configuration} this {@code Comments} object belongs to
     * has field comments.
     *
     * @return true, if {@code Configuration} has field comments.
     */
    public boolean hasFieldComments() {
        return !fieldComments.isEmpty();
    }

    /**
     * Returns a list of class comments.
     *
     * @return list of class comments
     */
    public List<String> getClassComments() {
        return classComments;
    }

    /**
     * Returns lists of field comments mapped by field name.
     *
     * @return lists of field comments by field name
     */
    public Map<String, List<String>> getFieldComments() {
        return fieldComments;
    }
}
