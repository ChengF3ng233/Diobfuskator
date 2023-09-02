package cn.feng.transform;

import cn.feng.util.ASMHelper;
import cn.feng.wrapper.ClassWrapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public abstract class Transformer extends ASMHelper {
    public abstract void transform(ClassWrapper wrapper, ClassNode node);
}
