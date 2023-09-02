import cn.feng.Diobfuskator;
import cn.feng.transform.composed.LemonTransformer;
import org.objectweb.asm.ClassReader;

import java.nio.file.Path;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class Loader {
    public static void main(String[] args) {
        new Diobfuskator.Builder()
                .input(Path.of("work", "input.jar"))
                .output(Path.of("work", "deobf.jar"))
                .transformers(
                        new LemonTransformer()
                )
                .readerFlags(ClassReader.SKIP_FRAMES)
                .clean()
                .execute();
    }
}
