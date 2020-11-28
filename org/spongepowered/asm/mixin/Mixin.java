// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface Mixin {
    int priority() default 1000;
    
    boolean remap() default true;
    
    String[] targets() default {};
    
    Class<?>[] value() default {};
}
