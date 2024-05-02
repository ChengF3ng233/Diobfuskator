package cn.feng.transform.composed;

import cn.feng.transform.ComposedTransformer;
import cn.feng.transform.Transformer;
import cn.feng.transform.impl.clean.AttributeTransformer;
import cn.feng.transform.impl.clean.TrashTransformer;
import cn.feng.transform.impl.clean.VariableTransformer;
import org.objectweb.asm.tree.*;

import java.util.List;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class CleanTransformer extends ComposedTransformer {
    @Override
    protected List<Transformer> getTransformers() {
        return List.of(new AttributeTransformer(), new TrashTransformer(), new VariableTransformer());
    }
}
