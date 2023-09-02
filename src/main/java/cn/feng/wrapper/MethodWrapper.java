package cn.feng.wrapper;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class MethodWrapper {
    private final MethodNode method;
    private final ClassNode owner;

    public MethodWrapper(MethodNode method, ClassNode owner) {
        this.method = method;
        this.owner = owner;
    }

    public boolean noTransform() {
        return !hasInstructions() || Modifier.isNative(method.access) || Modifier.isInterface(owner.access) || method.name.equals("valueOf");
    }

    public boolean hasInstructions() {
        return method.instructions.size() > 0;
    }

    public MethodNode getMethod() {
        return method;
    }

    public ClassNode getOwner() {
        return owner;
    }
}
