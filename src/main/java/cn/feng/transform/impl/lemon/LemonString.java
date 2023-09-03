package cn.feng.transform.impl.lemon;

import cn.feng.hierarchy.Hierarchy;
import cn.feng.transform.Transformer;
import org.objectweb.asm.tree.*;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class LemonString extends Transformer {
    @Override
    public void transform(ClassNode node, Hierarchy hierarchy) {
        decrypt(node);
        node.methods.removeIf(it -> isAccess(it.access, ACC_PRIVATE | ACC_STATIC) && it.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;"));
    }

    private void decrypt(ClassNode node) {
        for (MethodNode method : node.methods) {
            boolean found;
            do {
                found = false;
                for (AbstractInsnNode insn : method.instructions) {
                    boolean moveToNextLabel = false;
                    if (!shouldDecrypt(insn)) {
                        moveToNextLabel = shouldDecryptNextLabel(insn);
                        if (!moveToNextLabel) continue;
                    }

                    String encrypted = getString(insn);
                    AbstractInsnNode three = find(method, insn, 3);
                    MethodInsnNode methodInsn = (MethodInsnNode) (moveToNextLabel ? three : insn.getNext().getNext());
                    int index = getNumber(insn.getNext()).intValue();
                    int key = index ^ methodInsn.name.hashCode();

                    if (moveToNextLabel) method.instructions.remove(three);
                    method.instructions.remove(insn.getNext().getNext());
                    method.instructions.remove(insn.getNext());
                    if (moveToNextLabel) method.instructions.insertBefore(insn, new LabelNode());
                    method.instructions.set(insn, new LdcInsnNode(decrypt(encrypted, key)));
                    found = true;
                }
            } while (found);
        }
    }

    private boolean shouldDecrypt(AbstractInsnNode stringInsn) {
        return isString(stringInsn) && isInteger(stringInsn.getNext()) && stringInsn.getNext().getNext() instanceof MethodInsnNode methodInsn && methodInsn.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;");
    }

    private boolean shouldDecryptNextLabel(AbstractInsnNode stringInsn) {
        return isString(stringInsn) && isInteger(stringInsn.getNext()) && stringInsn.getNext().getNext() instanceof LabelNode &&
                stringInsn.getNext().getNext().getNext() instanceof MethodInsnNode methodInsn && methodInsn.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;");
    }

    private String decrypt(String str, int key) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ key);
        }

        return new String(chars);
    }
}
