package io.github.sashirestela.cleverclient.support;

import java.lang.reflect.Method;

public class ReturnType {
    private static final String ASYNC = "java.util.concurrent.CompletableFuture";
    private static final String STREAM = "java.util.stream.Stream";
    private static final String LIST = "java.util.List";
    private static final String INPUTSTREAM = "java.io.InputStream";
    private static final String STRING = "java.lang.String";

    private static final String REGEX = "[<>]";
    private static final String JAVA_PCK = "java";

    private String fullClassName;
    private String[] returnTypeArray;
    private int size;
    private int firstIndex;
    private int lastIndex;
    private int prevLastIndex;

    public ReturnType(String fullClassName) {
        this.fullClassName = fullClassName;
        returnTypeArray = fullClassName.split(REGEX, 0);
        size = returnTypeArray.length;
        firstIndex = 0;
        lastIndex = size - 1;
        prevLastIndex = lastIndex - 1;
    }

    public ReturnType(Method method) {
        this(method.getGenericReturnType().getTypeName());
    }

    public String toString() {
        return fullClassName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public Class<?> getBaseClass() {
        return getClass(lastIndex);
    }

    public Class<?> getGenericClassIfExists() {
        return isGeneric() ? getClass(prevLastIndex) : null;
    }

    private Class<?> getClass(int index) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(returnTypeArray[index]);
        } catch (ClassNotFoundException e) {
            // This shouldn't happen
        }
        return clazz;
    }

    public Category category() {
        if (isAsync()) {
            return asyncCategory();
        } else {
            return syncCategory();
        }
    }

    private Category asyncCategory() {
        if (isStream()) {
            return Category.ASYNC_STREAM;
        } else if (isList()) {
            return Category.ASYNC_LIST;
        } else if (isGeneric()) {
            return Category.ASYNC_GENERIC;
        } else if (isObject()) {
            return Category.ASYNC_OBJECT;
        } else if (isBinary()) {
            return Category.ASYNC_BINARY;
        } else if (isPlainText()) {
            return Category.ASYNC_PLAIN_TEXT;
        } else {
            return null;
        }
    }

    private Category syncCategory() {
        if (isStream()) {
            return Category.SYNC_STREAM;
        } else if (isList()) {
            return Category.SYNC_LIST;
        } else if (isGeneric()) {
            return Category.SYNC_GENERIC;
        } else if (isObject()) {
            return Category.SYNC_OBJECT;
        } else if (isBinary()) {
            return Category.SYNC_BINARY;
        } else if (isPlainText()) {
            return Category.SYNC_PLAIN_TEXT;
        } else {
            return null;
        }
    }

    public boolean isAsync() {
        return size > 1 && ASYNC.equals(returnTypeArray[firstIndex]);
    }

    public boolean isStream() {
        return size > 1 && STREAM.equals(returnTypeArray[prevLastIndex]);
    }

    public boolean isList() {
        return size > 1 && LIST.equals(returnTypeArray[prevLastIndex]);
    }

    public boolean isGeneric() {
        return ((isAsync() && size > 2) || (!isAsync() && size > 1))
                && !returnTypeArray[prevLastIndex].startsWith(JAVA_PCK);
    }

    public boolean isObject() {
        return !isInputStream() && !isString() && (size == 1 || (size == 2 && isAsync()));
    }

    public boolean isBinary() {
        return isInputStream() && (size == 1 || (size == 2 && isAsync()));
    }

    public boolean isPlainText() {
        return isString() && (size == 1 || (size == 2 && isAsync()));
    }

    private boolean isInputStream() {
        return INPUTSTREAM.equals(returnTypeArray[lastIndex]);
    }

    private boolean isString() {
        return STRING.equals(returnTypeArray[lastIndex]);
    }

    public enum Category {
        ASYNC_STREAM,
        ASYNC_LIST,
        ASYNC_GENERIC,
        ASYNC_OBJECT,
        ASYNC_BINARY,
        ASYNC_PLAIN_TEXT,
        SYNC_STREAM,
        SYNC_LIST,
        SYNC_GENERIC,
        SYNC_OBJECT,
        SYNC_BINARY,
        SYNC_PLAIN_TEXT;
    }
}