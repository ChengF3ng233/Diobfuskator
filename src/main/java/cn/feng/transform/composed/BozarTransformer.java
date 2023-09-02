package cn.feng.transform.composed;

import cn.feng.transform.ComposedTransformer;
import cn.feng.transform.Transformer;
import cn.feng.transform.impl.bozar.BozarConstantFlow;
import cn.feng.transform.impl.bozar.BozarHeavyFlow;
import cn.feng.transform.impl.bozar.BozarLightFlow;
import cn.feng.transform.impl.bozar.BozarString;
import cn.feng.transform.impl.clean.CleanTransformer;
import cn.feng.transform.impl.misc.UniversalNumber;

import java.util.List;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class BozarTransformer extends ComposedTransformer {
    @Override
    protected List<Transformer> getTransformers() {
        return List.of(new BozarConstantFlow(), new UniversalNumber(), new BozarString(), new BozarLightFlow(), new BozarHeavyFlow(false), new BozarHeavyFlow(true), new CleanTransformer());
    }
}
