package cn.feng.transform.impl.bozar;

import cn.feng.hierarchy.Hierarchy;
import cn.feng.transform.Transformer;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class BozarString extends Transformer {
    private void trans(MethodNode methodNode) {
        Arrays.stream(methodNode.instructions.toArray())
                .filter(node -> node.getOpcode() == NEW)
                .map(TypeInsnNode.class::cast)
                .filter(node -> node.desc.equals("java/lang/String"))
                .filter(node -> node.getNext().getOpcode() == DUP)
                .filter(node -> node.getNext().getNext().getOpcode() == ALOAD)
                .filter(node -> node.getNext().getNext().getNext().getOpcode() == INVOKESPECIAL)
                .forEach(node -> {
                    AbstractInsnNode current = node;

                    int endIndex = methodNode.instructions.indexOf(node);
                    int startIndex;
                    int storeIndex = ((VarInsnNode) node.getNext().getNext()).var;

                    while (current.getPrevious() != null && !((current = current.getPrevious()).getOpcode() == ASTORE && ((VarInsnNode) current).var == storeIndex)) {
                    }

                    startIndex = methodNode.instructions.indexOf(current);
                    if (startIndex == -1 || !isInteger(current.getPrevious().getPrevious()))
                        return;

                    byte[] bytes = new byte[getInteger(current.getPrevious().getPrevious())];
                    for (int i = startIndex; i < endIndex; i++) {
                        AbstractInsnNode insn = methodNode.instructions.get(i);
                        if (isInteger(insn) && isInteger(insn.getNext()) && insn.getNext().getNext().getOpcode() == BASTORE) {
                            bytes[getInteger(insn)] = (byte) getInteger(insn.getNext());
                        }
                    }

                    AbstractInsnNode insertBefore = node.getNext().getNext().getNext().getNext();
                    getInstructionsBetween(current.getPrevious().getPrevious(), node.getNext().getNext().getNext(), true, true)
                            .forEach(methodNode.instructions::remove);

                    methodNode.instructions.insertBefore(insertBefore, new LdcInsnNode(new String(bytes)));
                });
    }

    @Override
    public void transform(ClassNode node, Hierarchy hierarchy) {
        for (MethodNode methodNode : node.methods) {
            trans(methodNode);
        }
    }
}
