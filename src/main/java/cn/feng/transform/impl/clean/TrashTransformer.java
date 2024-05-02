package cn.feng.transform.impl.clean;

import cn.feng.analysis.rewriter.CodeAnalyzer;
import cn.feng.analysis.rewriter.CodeRewriter;
import cn.feng.analysis.rewriter.ICRReferenceHandler;
import cn.feng.analysis.rewriter.value.CodeReferenceValue;
import cn.feng.transform.Transformer;
import cn.feng.util.asm.Access;
import cn.feng.util.asm.InstructionModifier;
import cn.feng.util.Util;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;

/**
 * @Author: ChengFeng
 * @CreateTime: 2024-05-02
 */
public class TrashTransformer extends Transformer implements ICRReferenceHandler {
    @Override
    public void transform(ClassNode node) {
        node.methods.forEach(it -> simulateAndRewrite(node, it));
    }

    private void simulateAndRewrite(ClassNode cn, MethodNode m) {
        CodeAnalyzer a = new CodeAnalyzer(new CodeRewriter(this, Access.isStatic(m.access), m.maxLocals, m.desc));
        try {
            a.analyze(cn.name, m);
        } catch (AnalyzerException e) {
            Util.info("Failed stack analysis in " + cn.name + "." + m.name + ":" + e.getMessage());
            return;
        }

        InstructionModifier im = new InstructionModifier();
        int size = m.instructions.size();
        Frame<CodeReferenceValue>[] frames = a.getFrames();
        for (int i = 0; i < m.instructions.size(); i++) {
            AbstractInsnNode ain = m.instructions.get(i);
            Frame<CodeReferenceValue> frame = frames[i];
            if (frame == null) {
                continue;
            }
            switch (ain.getOpcode()) {
                case POP:
                    CodeReferenceValue top = getStackFromTop(frame, 0);
                    if (removeIfNotRequired(im, top))
                        im.remove(ain);
                    break;
                case POP2:
                    CodeReferenceValue top2 = getStackFromTop(frame, 0);
                    if (top2.getSize() > 1) {
                        if (removeIfNotRequired(im, top2))
                            im.remove(ain);
                    } else {
                        CodeReferenceValue lower = getStackFromTop(frame, 1);
                        boolean first = removeIfNotRequired(im, top2);
                        boolean second = removeIfNotRequired(im, lower);
                        if (first != second) {
                            // one of them was removed, change to single pop
                            im.replace(ain, new InsnNode(POP));
                        } else if (first && second) {
                            im.remove(ain);
                        }
                    }
                    break;
                case SWAP:
                    CodeReferenceValue first = getStackFromTop(frame, 0);
                    CodeReferenceValue second = getStackFromTop(frame, 1);
                    if (!first.isRequiredInCode() && !second.isRequiredInCode()) {
                        // both are movable
                        im.removeAll(first.getInstructions());
                        im.removeAll(second.getInstructions());
                        // swap instructions and remove swap
                        for (AbstractInsnNode insn : first.getInstructions())
                            im.append(ain, insn);
                        for (AbstractInsnNode insn : second.getInstructions())
                            im.append(ain, insn);
                        im.remove(ain);
                    }
                    break;
            }
        }
        im.apply(m);
        if (m.instructions.size() < size) {
            Util.info(cn.name + " " + m.name);
        }
    }

    private CodeReferenceValue getStackFromTop(Frame<CodeReferenceValue> frame, int i) {
        return frame.getStack(frame.getStackSize() - 1 - i);
    }

    private boolean removeIfNotRequired(InstructionModifier im, CodeReferenceValue val) {
        if (!val.isRequiredInCode()) {
            im.removeAll(val.getInstructions());
            return true;
        }
        return false;
    }

    public String toString(Frame<CodeReferenceValue> f) {
        StringBuilder stringBuilder = new StringBuilder(" LOCALS: (");
        for (int i = 0; i < f.getLocals(); ++i) {
            stringBuilder.append(f.getLocal(i));
            stringBuilder.append('|');
        }
        stringBuilder.append(") STACK: (");
        for (int i = 0; i < f.getStackSize(); ++i) {
            CodeReferenceValue combined = f.getStack(i).combine();
            stringBuilder
                    .append(combined.getStackValueOrNull() != null ? combined.getStackValueOrNull() : combined.toString());
            stringBuilder.append('|');
        }
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    @Override
    public Object getFieldValueOrNull(BasicValue v, String owner, String name, String desc) {
        return null;
    }

    @Override
    public Object getMethodReturnOrNull(BasicValue v, String owner, String name, String desc,
                                        List<? extends CodeReferenceValue> values) {
        return null;
    }
}
