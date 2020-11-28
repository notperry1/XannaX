// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mirror;

import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.util.SignaturePrinter;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.DeclaredType;
import java.util.Iterator;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import javax.lang.model.element.TypeElement;

public class TypeHandleSimulated extends TypeHandle
{
    private final /* synthetic */ TypeElement simulatedType;
    
    @Override
    public AnnotationHandle getAnnotation(final Class<? extends Annotation> clazz) {
        return null;
    }
    
    @Override
    public boolean isSimulated() {
        return true;
    }
    
    @Override
    public boolean isImaginary() {
        return false;
    }
    
    private static MethodHandle findMethodRecursive(final TypeHandle typeHandle, final String s, final String s2, final String s3, final boolean b) {
        final TypeElement targetElement = typeHandle.getTargetElement();
        if (targetElement == null) {
            return null;
        }
        final MethodHandle method = TypeHandle.findMethod(typeHandle, s, s2, s3, b);
        if (method != null) {
            return method;
        }
        final Iterator<? extends TypeMirror> iterator = targetElement.getInterfaces().iterator();
        while (iterator.hasNext()) {
            final MethodHandle methodRecursive = findMethodRecursive((TypeMirror)iterator.next(), s, s2, s3, b);
            if (methodRecursive != null) {
                return methodRecursive;
            }
        }
        final TypeMirror superclass = targetElement.getSuperclass();
        if (superclass == null || superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        return findMethodRecursive(superclass, s, s2, s3, b);
    }
    
    private static MethodHandle findMethodRecursive(final TypeMirror typeMirror, final String s, final String s2, final String s3, final boolean b) {
        if (!(typeMirror instanceof DeclaredType)) {
            return null;
        }
        return findMethodRecursive(new TypeHandle((TypeElement)((DeclaredType)typeMirror).asElement()), s, s2, s3, b);
    }
    
    public TypeHandleSimulated(final PackageElement packageElement, final String s, final TypeMirror typeMirror) {
        super(packageElement, s);
        this.simulatedType = (TypeElement)((DeclaredType)typeMirror).asElement();
    }
    
    @Override
    public TypeHandle getSuperclass() {
        return null;
    }
    
    @Override
    protected TypeElement getTargetElement() {
        return this.simulatedType;
    }
    
    @Override
    public MethodHandle findMethod(final String s, final String s2, final boolean b) {
        return new MethodHandle((TypeHandle)null, s, s2);
    }
    
    @Override
    public boolean isPublic() {
        return true;
    }
    
    @Override
    public FieldHandle findField(final String s, final String s2, final boolean b) {
        return new FieldHandle(null, s, s2);
    }
    
    @Override
    public MappingMethod getMappingMethod(final String s, final String s2) {
        final String descriptor = new SignaturePrinter(s, s2).setFullyQualified(true).toDescriptor();
        final MethodHandle methodRecursive = findMethodRecursive(this, s, descriptor, TypeUtils.stripGenerics(descriptor), true);
        return (methodRecursive != null) ? methodRecursive.asMapping(true) : super.getMappingMethod(s, s2);
    }
    
    public TypeHandleSimulated(final String s, final TypeMirror typeMirror) {
        this(TypeUtils.getPackage(typeMirror), s, typeMirror);
    }
    
    @Override
    public String findDescriptor(final MemberInfo memberInfo) {
        return (memberInfo != null) ? memberInfo.desc : null;
    }
}
