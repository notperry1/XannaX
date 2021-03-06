// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.throwables;

import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.throwables.MixinException;

public class InvalidMixinException extends MixinException
{
    private final /* synthetic */ IMixinInfo mixin;
    
    public InvalidMixinException(final IMixinInfo mixin, final String s, final Throwable t) {
        super(s, t);
        this.mixin = mixin;
    }
    
    public InvalidMixinException(final IMixinInfo mixin, final String s) {
        super(s);
        this.mixin = mixin;
    }
    
    public InvalidMixinException(final IMixinContext mixinContext, final Throwable t) {
        this(mixinContext.getMixin(), t);
    }
    
    public InvalidMixinException(final IMixinContext mixinContext, final String s) {
        this(mixinContext.getMixin(), s);
    }
    
    public InvalidMixinException(final IMixinContext mixinContext, final String s, final Throwable t) {
        super(s, t);
        this.mixin = mixinContext.getMixin();
    }
    
    public IMixinInfo getMixin() {
        return this.mixin;
    }
    
    public InvalidMixinException(final IMixinInfo mixin, final Throwable t) {
        super(t);
        this.mixin = mixin;
    }
}
