// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import java.io.OutputStream;
import org.spongepowered.asm.util.Bytecode;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.Debug;
import org.apache.logging.log4j.LogManager;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import org.spongepowered.asm.lib.tree.FieldNode;
import java.util.Deque;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.Set;
import org.spongepowered.asm.util.ClassSignature;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.struct.SourceMap;
import java.util.SortedSet;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import java.util.Map;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

class TargetClassContext extends ClassContext implements ITargetClassContext
{
    private final /* synthetic */ Map<String, Target> targetMethods;
    private /* synthetic */ boolean applied;
    private final /* synthetic */ ClassNode classNode;
    private final /* synthetic */ SortedSet<MixinInfo> mixins;
    private final /* synthetic */ String sessionId;
    private final /* synthetic */ SourceMap sourceMap;
    private final /* synthetic */ ClassInfo classInfo;
    private /* synthetic */ int nextUniqueMethodIndex;
    private static final /* synthetic */ Logger logger;
    private /* synthetic */ boolean forceExport;
    private final /* synthetic */ MixinEnvironment env;
    private final /* synthetic */ String className;
    private final /* synthetic */ ClassSignature signature;
    private /* synthetic */ int nextUniqueFieldIndex;
    private final /* synthetic */ Set<MethodNode> mixinMethods;
    private final /* synthetic */ Extensions extensions;
    
    FieldNode findAliasedField(final Deque<String> deque, final String anObject) {
        final String anObject2 = deque.poll();
        if (anObject2 == null) {
            return null;
        }
        for (final FieldNode fieldNode : this.classNode.fields) {
            if (fieldNode.name.equals(anObject2) && fieldNode.desc.equals(anObject)) {
                return fieldNode;
            }
        }
        return this.findAliasedField(deque, anObject);
    }
    
    void methodMerged(final MethodNode methodNode) {
        if (!this.mixinMethods.remove(methodNode)) {
            TargetClassContext.logger.debug("Unexpected: Merged unregistered method {}{} in {}", new Object[] { methodNode.name, methodNode.desc, this });
        }
    }
    
    String getUniqueName(final FieldNode fieldNode) {
        return String.format("fd%s$%s$%s", this.sessionId.substring(30), fieldNode.name, Integer.toHexString(this.nextUniqueFieldIndex++));
    }
    
    private MethodNode findAliasedMethod(final Deque<String> deque, final String s, final boolean b) {
        final String s2 = deque.poll();
        if (s2 == null) {
            return null;
        }
        for (final MethodNode methodNode : this.classNode.methods) {
            if (methodNode.name.equals(s2) && methodNode.desc.equals(s)) {
                return methodNode;
            }
        }
        if (b) {
            for (final MethodNode methodNode2 : this.mixinMethods) {
                if (methodNode2.name.equals(s2) && methodNode2.desc.equals(s)) {
                    return methodNode2;
                }
            }
        }
        return this.findAliasedMethod(deque, s);
    }
    
    MethodNode findAliasedMethod(final Deque<String> deque, final String s) {
        return this.findAliasedMethod(deque, s, false);
    }
    
    void addMixinMethod(final MethodNode methodNode) {
        this.mixinMethods.add(methodNode);
    }
    
    String getClassName() {
        return this.className;
    }
    
    void applyMixins() {
        if (this.applied) {
            throw new IllegalStateException("Mixins already applied to target class " + this.className);
        }
        this.applied = true;
        this.createApplicator().apply(this.mixins);
        this.applySignature();
        this.upgradeMethods();
        this.checkMerges();
    }
    
    SourceMap getSourceMap() {
        return this.sourceMap;
    }
    
    private MixinApplicatorStandard createApplicator() {
        if (this.classInfo.isInterface()) {
            return new MixinApplicatorInterface(this);
        }
        return new MixinApplicatorStandard(this);
    }
    
    String getUniqueName(final MethodNode methodNode, final boolean b) {
        return String.format(b ? "%2$s_$md$%1$s$%3$s" : "md%s$%s$%s", this.sessionId.substring(30), methodNode.name, Integer.toHexString(this.nextUniqueMethodIndex++));
    }
    
    @Override
    public ClassInfo getClassInfo() {
        return this.classInfo;
    }
    
    private void applySignature() {
        this.getClassNode().signature = this.signature.toString();
    }
    
    Extensions getExtensions() {
        return this.extensions;
    }
    
    List<FieldNode> getFields() {
        return this.classNode.fields;
    }
    
    Target getTargetMethod(final MethodNode methodNode) {
        if (!this.classNode.methods.contains(methodNode)) {
            throw new IllegalArgumentException("Invalid target method supplied to getTargetMethod()");
        }
        final String string = methodNode.name + methodNode.desc;
        Target target = this.targetMethods.get(string);
        if (target == null) {
            target = new Target(this.classNode, methodNode);
            this.targetMethods.put(string, target);
        }
        return target;
    }
    
    TargetClassContext(final MixinEnvironment env, final Extensions extensions, final String sessionId, final String className, final ClassNode classNode, final SortedSet<MixinInfo> mixins) {
        this.targetMethods = new HashMap<String, Target>();
        this.mixinMethods = new HashSet<MethodNode>();
        this.env = env;
        this.extensions = extensions;
        this.sessionId = sessionId;
        this.className = className;
        this.classNode = classNode;
        this.classInfo = ClassInfo.fromClassNode(classNode);
        this.signature = this.classInfo.getSignature();
        this.mixins = mixins;
        this.sourceMap = new SourceMap(classNode.sourceFile);
        this.sourceMap.addFile(this.classNode);
    }
    
    MethodNode findMethod(final Deque<String> deque, final String s) {
        return this.findAliasedMethod(deque, s, true);
    }
    
    private void checkMerges() {
        for (final MethodNode methodNode : this.mixinMethods) {
            if (!methodNode.name.startsWith("<")) {
                TargetClassContext.logger.debug("Unexpected: Registered method {}{} in {} was not merged", new Object[] { methodNode.name, methodNode.desc, this });
            }
        }
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
    
    void processDebugTasks() {
        if (!this.env.getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
            return;
        }
        final AnnotationNode visible = Annotations.getVisible(this.classNode, Debug.class);
        if (visible != null) {
            this.forceExport = Boolean.TRUE.equals(Annotations.getValue(visible, "export"));
            if (Boolean.TRUE.equals(Annotations.getValue(visible, "print"))) {
                Bytecode.textify(this.classNode, System.err);
            }
        }
        for (final MethodNode methodNode : this.classNode.methods) {
            final AnnotationNode visible2 = Annotations.getVisible(methodNode, Debug.class);
            if (visible2 != null && Boolean.TRUE.equals(Annotations.getValue(visible2, "print"))) {
                Bytecode.textify(methodNode, System.err);
            }
        }
    }
    
    @Override
    String getClassRef() {
        return this.classNode.name;
    }
    
    void mergeSignature(final ClassSignature classSignature) {
        this.signature.merge(classSignature);
    }
    
    List<MethodNode> getMethods() {
        return this.classNode.methods;
    }
    
    @Override
    public ClassNode getClassNode() {
        return this.classNode;
    }
    
    SortedSet<MixinInfo> getMixins() {
        return this.mixins;
    }
    
    boolean isExportForced() {
        return this.forceExport;
    }
    
    boolean isApplied() {
        return this.applied;
    }
    
    @Override
    public String toString() {
        return this.className;
    }
    
    String getSessionId() {
        return this.sessionId;
    }
}
