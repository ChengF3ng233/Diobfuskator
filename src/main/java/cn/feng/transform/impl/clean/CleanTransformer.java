package cn.feng.transform.impl.clean;

import cn.feng.hierarchy.Hierarchy;
import cn.feng.transform.Transformer;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class CleanTransformer extends Transformer {
    @Override
    public void transform(ClassNode node, Hierarchy hierarchy) {
        node.visibleAnnotations = filterAnnotations(node.visibleAnnotations);
        node.invisibleAnnotations = filterAnnotations(node.invisibleAnnotations);

        node.methods.forEach(methodNode -> {
            methodNode.visibleAnnotations = filterAnnotations(methodNode.visibleAnnotations);
            methodNode.invisibleAnnotations = filterAnnotations(methodNode.invisibleAnnotations);

            for (AbstractInsnNode insn : methodNode.instructions) {
                if (insn.getOpcode() == NOP) methodNode.instructions.remove(insn);
            }

            methodNode.tryCatchBlocks.removeIf(tbce -> tbce.type == null || tbce.type.isBlank() || tbce.type.isEmpty());
            methodNode.tryCatchBlocks.removeIf(tbce -> tbce.start.equals(tbce.end) || tbce.start.equals(tbce.handler) || tbce.end.equals(tbce.handler));

            List<TryCatchBlockNode> toRemove = new ArrayList<>();
            methodNode.tryCatchBlocks.forEach(tbce -> {
                LabelNode start = tbce.start;
                LabelNode handler = tbce.handler;
                LabelNode end = tbce.end;

                if (methodNode.instructions.indexOf(start) == -1 || methodNode.instructions.indexOf(handler) == -1 || methodNode.instructions.indexOf(end) == -1)
                    toRemove.add(tbce);
                else if (end.getNext() != null && end.getNext().getNext() != null && end.getNext().getOpcode() == ACONST_NULL && end.getNext().getNext().getOpcode() == ATHROW)
                    toRemove.add(tbce);
                else if (methodNode.instructions.indexOf(start) >= methodNode.instructions.indexOf(handler) || methodNode.instructions.indexOf(start) >= methodNode.instructions.indexOf(end) || methodNode.instructions.indexOf(handler) <= methodNode.instructions.indexOf(end))
                    toRemove.add(tbce);
            });

            methodNode.tryCatchBlocks.removeAll(toRemove);
        });

        node.fields.forEach(fieldNode -> {
            fieldNode.visibleAnnotations = filterAnnotations(fieldNode.visibleAnnotations);
            fieldNode.invisibleAnnotations = filterAnnotations(fieldNode.invisibleAnnotations);
        });

        node.signature = null;
        node.methods.forEach(methodNode -> methodNode.signature = null);
        node.fields.forEach(fieldNode -> fieldNode.signature = null);
    }

    private List<AnnotationNode> filterAnnotations(List<AnnotationNode> nodes) {
        if (nodes == null) {
            return null;
        }

        return nodes.stream()
                .filter(node -> node.desc.startsWith("L"))
                .filter(node -> node.desc.endsWith(";"))
                .filter(node -> node.desc.length() >= 3)
                .filter(node -> !node.desc.contains("\n"))
                .filter(node -> !node.desc.contains(" "))
                .filter(node -> !node.desc.contains("\u0000"))
                .collect(Collectors.toList());
    }
}
