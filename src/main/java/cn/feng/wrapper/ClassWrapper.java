package cn.feng.wrapper;

import cn.feng.Diobfuskator;
import cn.feng.util.Util;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class ClassWrapper {
    private final byte[] data;
    private final ClassNode node;
    private final List<MethodWrapper> methods;
    private final List<FieldWrapper> fields;
    private final int flags;
    private String name;
    public ClassWrapper(ClassNode node, byte[] data, int flags) {
        this.node = node;
        this.data = data;
        this.flags = flags;
        this.name = node.name;
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();

        for (MethodNode method : node.methods) {
            methods.add(new MethodWrapper(method, node));
        }

        for (FieldNode field : node.fields) {
            fields.add(new FieldWrapper(field, node));
        }
    }

    public String getName() {
        return name;
    }

    public boolean noTransform() {
        return Modifier.isInterface(node.access);
    }

    public ClassNode getNode() {
        return node;
    }

    public List<MethodWrapper> getMethods() {
        return methods;
    }

    public List<FieldWrapper> getFields() {
        return fields;
    }

    public byte[] toByteArray() {
        try {
            ClassWriter writer = new ClassWriter(flags);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Exception e) {
            Util.info("ERROR: Wrote [" + node.name + "] with original bytes.");
            return data;
        }
    }
}
