package cn.feng.transform;

import cn.feng.wrapper.ClassWrapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public abstract class ComposedTransformer extends Transformer {
    protected abstract List<Transformer> getTransformers();

    @Override
    public void transform(ClassWrapper wrapper, ClassNode node) {
        for (Transformer transformer : getTransformers()) {
            transformer.transform(wrapper, node);
        }
    }
}
