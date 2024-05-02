package cn.feng.transform;

import cn.feng.util.asm.ASMHelper;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public abstract class Transformer extends ASMHelper {
    public abstract void transform(ClassNode node);
}
