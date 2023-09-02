package cn.feng.transform.composed;

import cn.feng.transform.ComposedTransformer;
import cn.feng.transform.Transformer;
import cn.feng.transform.impl.lemon.LemonFlow;
import cn.feng.transform.impl.lemon.LemonNumber;
import cn.feng.transform.impl.lemon.LemonString;

import java.util.List;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class LemonTransformer extends ComposedTransformer {
    @Override
    protected List<Transformer> getTransformers() {
        return List.of(new LemonFlow(), new LemonNumber(), new LemonString());
    }
}
