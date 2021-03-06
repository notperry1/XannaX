// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.points;

import java.util.ListIterator;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

@AtCode("JUMP")
public class JumpInsnPoint extends InjectionPoint
{
    private final /* synthetic */ int ordinal;
    private final /* synthetic */ int opCode;
    
    public JumpInsnPoint(final InjectionPointData injectionPointData) {
        this.opCode = injectionPointData.getOpcode(-1, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 198, 199, -1);
        this.ordinal = injectionPointData.getOrdinal();
    }
    
    @Override
    public boolean find(final String s, final InsnList list, final Collection<AbstractInsnNode> collection) {
        boolean b = false;
        int n = 0;
        for (final AbstractInsnNode abstractInsnNode : list) {
            if (abstractInsnNode instanceof JumpInsnNode && (this.opCode == -1 || abstractInsnNode.getOpcode() == this.opCode)) {
                if (this.ordinal == -1 || this.ordinal == n) {
                    collection.add(abstractInsnNode);
                    b = true;
                }
                ++n;
            }
        }
        return b;
    }
}
