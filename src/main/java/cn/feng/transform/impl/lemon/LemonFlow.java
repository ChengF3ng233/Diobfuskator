package cn.feng.transform.impl.lemon;

import cn.feng.hierarchy.Hierarchy;
import cn.feng.transform.Transformer;
import cn.feng.util.InstructionModifier;
import org.objectweb.asm.tree.*;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class LemonFlow extends Transformer {
    @Override
    public void transform(ClassNode node, Hierarchy hierarchy) {
        node.fields.removeIf(field -> (field.value instanceof String && field.name.startsWith("lemon") && isChinese((String) field.value)) || field.name.equals("Ꮸ"));


        for (MethodNode method : node.methods) {
            InstructionModifier modifier = new InstructionModifier();
            for (AbstractInsnNode insn : method.instructions) {

                // SWAP
                if (insn.getOpcode() == SWAP && insn.getNext() != null && insn.getNext().getOpcode() == SWAP) {
                    modifier.removeAll(insn, insn.getNext());
                }

                if (insn instanceof FieldInsnNode fieldInsn
                        && fieldInsn.name.equals("Ꮸ") && fieldInsn.desc.equals("J") && fieldInsn.getOpcode() == GETSTATIC) {

                    int index = method.instructions.indexOf(insn);

                    if (insn.getNext().getOpcode() == GOTO) {
                        AbstractInsnNode switchNode = ((JumpInsnNode) insn.getNext()).label.getNext().getNext();
                        if (switchNode instanceof LookupSwitchInsnNode) {
                            LabelNode before = ((LookupSwitchInsnNode) switchNode).dflt;
                            if (before.getPrevious().getOpcode() == ATHROW) {
                                modifier.removeAll(getInstructionsBetween(
                                        insn,
                                        before,
                                        true,
                                        false
                                ));
                            }
                        }
                    } else if (method.instructions.get(index - 4).getOpcode() == GOTO) {
                        AbstractInsnNode gotoInsn = method.instructions.get(index - 4);
                        AbstractInsnNode ifInsn = method.instructions.get(index + 6);
                        AbstractInsnNode ifEnd = method.instructions.get(index + 8);
                        modifier.removeAll(getInstructionsBetween(gotoInsn, ifInsn, true, true));
                        modifier.removeAll(getInstructionsBetween(ifEnd, ((JumpInsnNode) ifEnd).label, true, false));
                    }
                }
            }

            modifier.apply(method);
        }
    }
}
