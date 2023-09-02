package cn.feng.transform.impl.lemon;

import cn.feng.transform.Transformer;
import cn.feng.wrapper.ClassWrapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class LemonNumber extends Transformer {
    @Override
    public void transform(ClassWrapper wrapper, ClassNode node) {
        for (MethodNode method : node.methods) {
            boolean found;
            do {
                found = false;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == INEG && insn.getNext() != null && insn.getNext().getOpcode() == INEG) {
                        method.instructions.remove(insn.getNext());
                        method.instructions.remove(insn);
                        found = true;
                    }
                }
            } while (found);
        }
    }
}
