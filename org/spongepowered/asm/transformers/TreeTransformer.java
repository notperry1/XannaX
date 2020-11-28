// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.transformers;

import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.service.ILegacyClassTransformer;

public abstract class TreeTransformer implements ILegacyClassTransformer
{
    private /* synthetic */ ClassNode classNode;
    private /* synthetic */ ClassReader classReader;
    
    protected final ClassNode readClass(final byte[] array, final boolean b) {
        final ClassReader classReader = new ClassReader(array);
        if (b) {
            this.classReader = classReader;
        }
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 8);
        return classNode;
    }
    
    protected final ClassNode readClass(final byte[] array) {
        return this.readClass(array, true);
    }
    
    protected final byte[] writeClass(final ClassNode classNode) {
        if (this.classReader != null && this.classNode == classNode) {
            this.classNode = null;
            final MixinClassWriter mixinClassWriter = new MixinClassWriter(this.classReader, 3);
            this.classReader = null;
            classNode.accept(mixinClassWriter);
            return mixinClassWriter.toByteArray();
        }
        this.classNode = null;
        final MixinClassWriter mixinClassWriter2 = new MixinClassWriter(3);
        classNode.accept(mixinClassWriter2);
        return mixinClassWriter2.toByteArray();
    }
}
