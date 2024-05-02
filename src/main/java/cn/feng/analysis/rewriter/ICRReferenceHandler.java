package cn.feng.analysis.rewriter;

import java.util.List;

import cn.feng.analysis.rewriter.value.CodeReferenceValue;
import org.objectweb.asm.tree.analysis.BasicValue;


/**
 * Same as {@link cn.feng.analysis.stack.IConstantReferenceHandler} for CodeRewriter
 */
public interface ICRReferenceHandler {

  Object getFieldValueOrNull(BasicValue v, String owner, String name, String desc);

  Object getMethodReturnOrNull(BasicValue v, String owner, String name, String desc,
                               List<? extends CodeReferenceValue> values);

}
