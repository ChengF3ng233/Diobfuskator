package cn.feng.transform.impl.clean;

import cn.feng.transform.Transformer;
import cn.feng.util.asm.Access;
import cn.feng.util.asm.InstructionModifier;
import cn.feng.util.asm.Instructions;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * @Author: ChengFeng
 * @CreateTime: 2024-05-02
 */
public class VariableTransformer extends Transformer {
    @Override
    public void transform(ClassNode node) {
        node.methods.forEach(this::processMethod);
    }

    private void processMethod(MethodNode method) {
        final List<VarInsnNode> varInstrs = StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(i -> i.getType() == AbstractInsnNode.VAR_INSN).map(i -> (VarInsnNode) i)
                .toList();

        HashSet<Integer> loadVars = new HashSet<>();
        if (!Access.isStatic(method.access)) {
            loadVars.add(0);
        }
        varInstrs.stream().filter(Instructions::isLoadVarInsn).map(i -> i.var).forEach(loadVars::add);

        InstructionModifier modifier = new InstructionModifier();
        varInstrs.stream().filter(i -> Instructions.isStoreVarInsn(i) && !loadVars.contains(i.var)).forEach(i -> {
            if (Instructions.isWideVarInsn(i))
                modifier.replace(i, new InsnNode(POP2));
            else
                modifier.replace(i, new InsnNode(POP));
        });
        modifier.apply(method);
    }
}
