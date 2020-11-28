// 
// Decompiled by Procyon v0.5.36
// 

package net.jodah.typetools;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;
import sun.misc.Unsafe;
import java.util.HashMap;
import java.util.Collections;
import java.util.WeakHashMap;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.util.Map;

public final class TypeResolver
{
    private static final /* synthetic */ Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
    private static /* synthetic */ Method GET_CONSTANT_POOL;
    private static /* synthetic */ Method GET_CONSTANT_POOL_METHOD_AT;
    private static final /* synthetic */ Map<String, Method> OBJECT_METHODS;
    private static volatile /* synthetic */ boolean CACHE_ENABLED;
    private static final /* synthetic */ Double JAVA_VERSION;
    private static /* synthetic */ Method GET_CONSTANT_POOL_SIZE;
    private static /* synthetic */ boolean RESOLVES_LAMBDAS;
    private static final /* synthetic */ Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> TYPE_VARIABLE_CACHE;
    
    public static <T, S extends T> Class<?>[] resolveRawArguments(final Class<T> clazz, final Class<S> clazz2) {
        return resolveRawArguments(resolveGenericType(clazz, clazz2), clazz2);
    }
    
    private static Member getConstantPoolMethodAt(final Object obj, final int i) {
        try {
            return (Member)TypeResolver.GET_CONSTANT_POOL_METHOD_AT.invoke(obj, i);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public static <T, S extends T> Class<?> resolveRawArgument(final Class<T> clazz, final Class<S> clazz2) {
        return resolveRawArgument(resolveGenericType(clazz, clazz2), clazz2);
    }
    
    private static boolean isDefaultMethod(final Method method) {
        return TypeResolver.JAVA_VERSION >= 1.8 && method.isDefault();
    }
    
    public static void disableCache() {
        TypeResolver.TYPE_VARIABLE_CACHE.clear();
        TypeResolver.CACHE_ENABLED = false;
    }
    
    private TypeResolver() {
    }
    
    public static Class<?> resolveRawArgument(final Type obj, final Class<?> clazz) {
        final Class<?>[] resolveRawArguments = resolveRawArguments(obj, clazz);
        if (resolveRawArguments == null) {
            return Unknown.class;
        }
        if (resolveRawArguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument for generic type " + obj + " but found " + resolveRawArguments.length);
        }
        return resolveRawArguments[0];
    }
    
    static {
        TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
        TypeResolver.CACHE_ENABLED = true;
        OBJECT_METHODS = new HashMap<String, Method>();
        JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));
        try {
            final Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>)new PrivilegedExceptionAction<Unsafe>() {
                @Override
                public Unsafe run() throws Exception {
                    final Field declaredField = Unsafe.class.getDeclaredField("theUnsafe");
                    declaredField.setAccessible(true);
                    return (Unsafe)declaredField.get(null);
                }
            });
            TypeResolver.GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool", (Class<?>[])new Class[0]);
            final Class<?> forName = Class.forName((TypeResolver.JAVA_VERSION < 9.0) ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool");
            TypeResolver.GET_CONSTANT_POOL_SIZE = forName.getDeclaredMethod("getSize", (Class<?>[])new Class[0]);
            TypeResolver.GET_CONSTANT_POOL_METHOD_AT = forName.getDeclaredMethod("getMethodAt", Integer.TYPE);
            final long objectFieldOffset = unsafe.objectFieldOffset(AccessibleObject.class.getDeclaredField("override"));
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL, objectFieldOffset, true);
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL_SIZE, objectFieldOffset, true);
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL_METHOD_AT, objectFieldOffset, true);
            TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(TypeResolver.GET_CONSTANT_POOL.invoke(Object.class, new Object[0]), new Object[0]);
            for (final Method method : Object.class.getDeclaredMethods()) {
                TypeResolver.OBJECT_METHODS.put(method.getName(), method);
            }
            TypeResolver.RESOLVES_LAMBDAS = true;
        }
        catch (Exception ex) {}
        final HashMap<Class<?>, Class<?>> m = new HashMap<Class<?>, Class<?>>();
        m.put(Boolean.TYPE, Boolean.class);
        m.put(Byte.TYPE, Byte.class);
        m.put(Character.TYPE, Character.class);
        m.put(Double.TYPE, Double.class);
        m.put(Float.TYPE, Float.class);
        m.put(Integer.TYPE, Integer.class);
        m.put(Long.TYPE, Long.class);
        m.put(Short.TYPE, Short.class);
        m.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap((Map<?, ?>)m);
    }
    
    public static Class<?> resolveRawClass(final Type type, final Class<?> clazz) {
        return resolveRawClass(type, clazz, null);
    }
    
    private static int getConstantPoolSize(final Object obj) {
        try {
            return (int)TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(obj, new Object[0]);
        }
        catch (Exception ex) {
            return 0;
        }
    }
    
    private static boolean isAutoBoxingMethod(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        return method.getName().equals("valueOf") && parameterTypes.length == 1 && parameterTypes[0].isPrimitive() && wrapPrimitives(parameterTypes[0]).equals(method.getDeclaringClass());
    }
    
    private static Class<?> resolveRawClass(Type type, final Class<?> clazz, final Class<?> clazz2) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return resolveRawClass(((ParameterizedType)type).getRawType(), clazz, clazz2);
        }
        if (type instanceof GenericArrayType) {
            return Array.newInstance(resolveRawClass(((GenericArrayType)type).getGenericComponentType(), clazz, clazz2), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            final TypeVariable typeVariable = (TypeVariable)type;
            type = getTypeVariableMap(clazz, clazz2).get(typeVariable);
            type = ((type == null) ? resolveBound(typeVariable) : resolveRawClass(type, clazz, clazz2));
        }
        return (Class<?>)((type instanceof Class) ? ((Class)type) : Unknown.class);
    }
    
    private static void populateTypeArgs(final ParameterizedType parameterizedType, final Map<TypeVariable<?>, Type> map, final boolean b) {
        if (parameterizedType.getRawType() instanceof Class) {
            final TypeVariable[] typeParameters = ((Class)parameterizedType.getRawType()).getTypeParameters();
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (parameterizedType.getOwnerType() != null) {
                final Type ownerType = parameterizedType.getOwnerType();
                if (ownerType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)ownerType, map, b);
                }
            }
            for (int i = 0; i < actualTypeArguments.length; ++i) {
                final TypeVariable typeVariable = typeParameters[i];
                final Type type = actualTypeArguments[i];
                if (type instanceof Class) {
                    map.put(typeVariable, type);
                }
                else if (type instanceof GenericArrayType) {
                    map.put(typeVariable, type);
                }
                else if (type instanceof ParameterizedType) {
                    map.put(typeVariable, type);
                }
                else if (type instanceof TypeVariable) {
                    final TypeVariable<Class<T>> typeVariable2 = (TypeVariable<Class<T>>)type;
                    if (b) {
                        final TypeVariable<Class<T>> typeVariable3 = map.get(typeVariable);
                        if (typeVariable3 != null) {
                            map.put(typeVariable2, typeVariable3);
                            continue;
                        }
                    }
                    Type resolveBound = map.get(typeVariable2);
                    if (resolveBound == null) {
                        resolveBound = resolveBound(typeVariable2);
                    }
                    map.put(typeVariable, resolveBound);
                }
            }
        }
    }
    
    private static Class<?> wrapPrimitives(final Class<?> clazz) {
        return clazz.isPrimitive() ? TypeResolver.PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }
    
    private static void populateSuperTypeArgs(final Type[] array, final Map<TypeVariable<?>, Type> map, final boolean b) {
        for (final Type type : array) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType)type;
                if (!b) {
                    populateTypeArgs(parameterizedType, map, b);
                }
                final Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    populateSuperTypeArgs(((Class)rawType).getGenericInterfaces(), map, b);
                }
                if (b) {
                    populateTypeArgs(parameterizedType, map, b);
                }
            }
            else if (type instanceof Class) {
                populateSuperTypeArgs(((Class)type).getGenericInterfaces(), map, b);
            }
        }
    }
    
    public static Type resolveBound(final TypeVariable<?> typeVariable) {
        final Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Unknown.class;
        }
        Type resolveBound = bounds[0];
        if (resolveBound instanceof TypeVariable) {
            resolveBound = resolveBound((TypeVariable<?>)resolveBound);
        }
        return (resolveBound == Object.class) ? Unknown.class : resolveBound;
    }
    
    private static void populateLambdaArgs(final Class<?> clazz, final Class<?> clazz2, final Map<TypeVariable<?>, Type> map) {
        if (TypeResolver.RESOLVES_LAMBDAS) {
            for (final Method method : clazz.getMethods()) {
                if (!isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers()) && !method.isBridge()) {
                    final Method method2 = TypeResolver.OBJECT_METHODS.get(method.getName());
                    if (method2 == null || !Arrays.equals(method.getTypeParameters(), method2.getTypeParameters())) {
                        final Type genericReturnType = method.getGenericReturnType();
                        final Type[] genericParameterTypes = method.getGenericParameterTypes();
                        final Member memberRef = getMemberRef(clazz2);
                        if (memberRef == null) {
                            return;
                        }
                        if (genericReturnType instanceof TypeVariable) {
                            final Class<?> wrapPrimitives = wrapPrimitives((memberRef instanceof Method) ? ((Method)memberRef).getReturnType() : ((Constructor<?>)memberRef).getDeclaringClass());
                            if (!wrapPrimitives.equals(Void.class)) {
                                map.put((TypeVariable<?>)genericReturnType, wrapPrimitives);
                            }
                        }
                        final Class<?>[] array = (memberRef instanceof Method) ? ((Method)memberRef).getParameterTypes() : ((Constructor<?>)memberRef).getParameterTypes();
                        int n = 0;
                        if (genericParameterTypes.length > 0 && genericParameterTypes[0] instanceof TypeVariable && genericParameterTypes.length == array.length + 1) {
                            map.put((TypeVariable<?>)genericParameterTypes[0], memberRef.getDeclaringClass());
                            n = 1;
                        }
                        int n2 = 0;
                        if (genericParameterTypes.length < array.length) {
                            n2 = array.length - genericParameterTypes.length;
                        }
                        for (int n3 = 0; n3 + n2 < array.length; ++n3) {
                            if (genericParameterTypes[n3] instanceof TypeVariable) {
                                map.put((TypeVariable<?>)genericParameterTypes[n3 + n], wrapPrimitives(array[n3 + n2]));
                            }
                        }
                        return;
                    }
                }
            }
        }
    }
    
    public static void enableCache() {
        TypeResolver.CACHE_ENABLED = true;
    }
    
    public static Type resolveGenericType(final Class<?> clazz, final Type type) {
        Class obj;
        if (type instanceof ParameterizedType) {
            obj = (Class)((ParameterizedType)type).getRawType();
        }
        else {
            obj = (Class)type;
        }
        if (clazz.equals(obj)) {
            return type;
        }
        if (clazz.isInterface()) {
            for (final Type type2 : obj.getGenericInterfaces()) {
                final Type resolveGenericType;
                if (type2 != null && !type2.equals(Object.class) && (resolveGenericType = resolveGenericType(clazz, type2)) != null) {
                    return resolveGenericType;
                }
            }
        }
        final Type genericSuperclass = obj.getGenericSuperclass();
        final Type resolveGenericType2;
        if (genericSuperclass != null && !genericSuperclass.equals(Object.class) && (resolveGenericType2 = resolveGenericType(clazz, genericSuperclass)) != null) {
            return resolveGenericType2;
        }
        return null;
    }
    
    public static Class<?>[] resolveRawArguments(final Type type, final Class<?> clazz) {
        Class<?>[] array = null;
        Class<?> clazz2 = null;
        if (TypeResolver.RESOLVES_LAMBDAS && clazz.isSynthetic()) {
            final Class clazz3 = (Class)((type instanceof ParameterizedType && ((ParameterizedType)type).getRawType() instanceof Class) ? ((ParameterizedType)type).getRawType() : ((type instanceof Class) ? ((Class)type) : null));
            if (clazz3 != null && clazz3.isInterface()) {
                clazz2 = (Class<?>)clazz3;
            }
        }
        if (type instanceof ParameterizedType) {
            final Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();
            array = (Class<?>[])new Class[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; ++i) {
                array[i] = resolveRawClass(actualTypeArguments[i], clazz, clazz2);
            }
        }
        else if (type instanceof TypeVariable) {
            array = (Class<?>[])new Class[] { resolveRawClass(type, clazz, clazz2) };
        }
        else if (type instanceof Class) {
            final TypeVariable[] typeParameters = ((Class)type).getTypeParameters();
            array = (Class<?>[])new Class[typeParameters.length];
            for (int j = 0; j < typeParameters.length; ++j) {
                array[j] = resolveRawClass(typeParameters[j], clazz, clazz2);
            }
        }
        return array;
    }
    
    private static Member getMemberRef(final Class<?> obj) {
        Object invoke;
        try {
            invoke = TypeResolver.GET_CONSTANT_POOL.invoke(obj, new Object[0]);
        }
        catch (Exception ex) {
            return null;
        }
        Member member = null;
        for (int i = getConstantPoolSize(invoke) - 1; i >= 0; --i) {
            final Member constantPoolMethod = getConstantPoolMethodAt(invoke, i);
            if (constantPoolMethod != null && (!(constantPoolMethod instanceof Constructor) || !constantPoolMethod.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda"))) {
                if (!constantPoolMethod.getDeclaringClass().isAssignableFrom(obj)) {
                    member = constantPoolMethod;
                    if (!(constantPoolMethod instanceof Method)) {
                        break;
                    }
                    if (!isAutoBoxingMethod((Method)constantPoolMethod)) {
                        break;
                    }
                }
            }
        }
        return member;
    }
    
    private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> clazz, final Class<?> clazz2) {
        final Reference<Map<TypeVariable<?>, Type>> reference = TypeResolver.TYPE_VARIABLE_CACHE.get(clazz);
        Map<TypeVariable<?>, Type> referent = (reference != null) ? reference.get() : null;
        if (referent == null) {
            referent = new HashMap<TypeVariable<?>, Type>();
            if (clazz2 != null) {
                populateLambdaArgs(clazz2, clazz, referent);
            }
            populateSuperTypeArgs(clazz.getGenericInterfaces(), referent, clazz2 != null);
            Type type = clazz.getGenericSuperclass();
            for (Class<?> obj = clazz.getSuperclass(); obj != null && !Object.class.equals(obj); obj = obj.getSuperclass()) {
                if (type instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)type, referent, false);
                }
                populateSuperTypeArgs(obj.getGenericInterfaces(), referent, false);
                type = obj.getGenericSuperclass();
            }
            for (Class<?> enclosingClass = clazz; enclosingClass.isMemberClass(); enclosingClass = enclosingClass.getEnclosingClass()) {
                final Type genericSuperclass = enclosingClass.getGenericSuperclass();
                if (genericSuperclass instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)genericSuperclass, referent, clazz2 != null);
                }
            }
            if (TypeResolver.CACHE_ENABLED) {
                TypeResolver.TYPE_VARIABLE_CACHE.put(clazz, new WeakReference<Map<TypeVariable<?>, Type>>(referent));
            }
        }
        return referent;
    }
    
    public static final class Unknown
    {
        private Unknown() {
        }
    }
}
