package cn.feng.wrapper;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class FieldWrapper {
    private final FieldNode field;
    private final ClassNode owner;

    public FieldWrapper(FieldNode field, ClassNode owner) {
        this.field = field;
        this.owner = owner;
    }

    public FieldNode getField() {
        return field;
    }

    public ClassNode getOwner() {
        return owner;
    }
}
