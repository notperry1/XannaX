// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.invoke;

import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import java.util.List;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.mixin.injection.code.Injector;

public abstract class InvokeInjector extends Injector
{
    protected final /* synthetic */ String annotationType;
    
    protected AbstractInsnNode invokeHandlerWithArgs(final Type[] array, final InsnList list, final int[] array2, final int n, final int n2) {
        if (!this.isStatic) {
            list.add(new VarInsnNode(25, 0));
        }
        this.pushArgs(array, list, array2, n, n2);
        return this.invokeHandler(list);
    }
    
    protected AbstractInsnNode invokeHandlerWithArgs(final Type[] array, final InsnList list, final int[] array2) {
        return this.invokeHandlerWithArgs(array, list, array2, 0, array.length);
    }
    
    protected void checkTarget(final Target target) {
        this.checkTargetModifiers(target, true);
    }
    
    @Override
    protected void sanityCheck(final Target target, final List<InjectionPoint> list) {
        super.sanityCheck(target, list);
        this.checkTarget(target);
    }
    
    protected abstract void injectAtInvoke(final Target p0, final InjectionNodes.InjectionNode p1);
    
    protected int[] storeArgs(final Target target, final Type[] array, final InsnList list, final int n) {
        final int[] generateArgMap = target.generateArgMap(array, n);
        this.storeArgs(array, list, generateArgMap, n, array.length);
        return generateArgMap;
    }
    
    protected void checkTargetForNode(final Target target, final InjectionNodes.InjectionNode injectionNode) {
        if (!target.isCtor || target.indexOf(injectionNode.getCurrentTarget()) > target.indexOf(target.findSuperInitNode())) {
            this.checkTargetModifiers(target, true);
            return;
        }
        if (!this.isStatic) {
            throw new InvalidInjectionException(this.info, "Pre-super " + this.annotationType + " invocation must be static in " + this);
        }
    }
    
    @Override
    protected void inject(final Target obj, final InjectionNodes.InjectionNode injectionNode) {
        if (!(injectionNode.getCurrentTarget() instanceof MethodInsnNode)) {
            throw new InvalidInjectionException(this.info, this.annotationType + " annotation on is targetting a non-method insn in " + obj + " in " + this);
        }
        this.injectAtInvoke(obj, injectionNode);
    }
    
    protected void storeArgs(final Type[] array, final InsnList list, final int[] array2, final int n, final int n2) {
        for (int i = n2 - 1; i >= n; --i) {
            list.add(new VarInsnNode(array[i].getOpcode(54), array2[i]));
        }
    }
    
    protected final void checkTargetModifiers(final Target target, final boolean b) {
        if (b && target.isStatic != this.isStatic) {
            throw new InvalidInjectionException(this.info, "'static' modifier of handler method does not match target in " + this);
        }
        if (!b && !this.isStatic && target.isStatic) {
            throw new InvalidInjectionException(this.info, "non-static callback method " + this + " targets a static method which is not supported");
        }
    }
    
    public InvokeInjector(final InjectionInfo injectionInfo, final String annotationType) {
        super(injectionInfo);
        this.annotationType = annotationType;
    }
    
    protected void pushArgs(final Type[] array, final InsnList list, final int[] array2, final int n, final int n2) {
        for (int i = n; i < n2; ++i) {
            list.add(new VarInsnNode(array[i].getOpcode(21), array2[i]));
        }
    }
}
