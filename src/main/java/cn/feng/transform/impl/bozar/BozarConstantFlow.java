package cn.feng.transform.impl.bozar;

import cn.feng.hierarchy.Hierarchy;
import cn.feng.transform.Transformer;
import cn.feng.util.ASMHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;

public class BozarConstantFlow extends Transformer {
    @Override
    public void transform(ClassNode node, Hierarchy hierarchy) {
        node.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMHelper::isNumber)
                .filter(it -> it.getNext() instanceof LabelNode)
                .filter(it -> it.getPrevious() instanceof LabelNode)
                .filter(it -> it.getPrevious().getPrevious().getOpcode() == GOTO)
                .filter(it -> isNumber(it.getPrevious().getPrevious().getPrevious()))
                .forEach(insn -> {
                    int index = methodNode.instructions.indexOf(insn);
                    if (index - 10 < 0 || methodNode.instructions.size() < index + 9)
                        return;

                    AbstractInsnNode beforeStart = methodNode.instructions.get(index - 10);
                    AbstractInsnNode beforeEnd = insn.getPrevious(); //methodNode.instructions.get(index - 1); //insn.getPrevious()

                    AbstractInsnNode afterStart = insn.getNext(); //methodNode.instructions.get(index + 1); //insn.getNext()
                    AbstractInsnNode afterEnd = methodNode.instructions.get(index + 9);

                    if (isLong(beforeStart) && afterEnd instanceof LabelNode) {
                        getInstructionsBetween(
                                beforeStart,
                                beforeEnd,
                                true,
                                true
                        ).forEach(methodNode.instructions::remove);

                        getInstructionsBetween(
                                afterStart,
                                afterEnd,
                                true,
                                false
                        ).forEach(methodNode.instructions::remove);
                    }
                }));
    }
}
