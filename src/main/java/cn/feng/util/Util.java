package cn.feng.util;

import cn.feng.Data;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class Util {
    public static boolean isClass(String fileName, byte[] bytes) {
        return bytes.length >= 4 && String
                .format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]).equals("CAFEBABE") && (
                fileName.endsWith(".class") || fileName.endsWith(".class/"));
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode) throws InvalidClassException {
        return loadClass(bytes, readerMode, true);
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode, boolean fix) throws InvalidClassException {
        ClassNode classNode;
        try {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, readerMode);
        } catch (Exception e) {
            classNode = fix ? loadClass(fixClass(bytes), readerMode, false) : null;
        }

        return classNode;
    }

    public static boolean noTransform(ClassNode node) {
        return Modifier.isInterface(node.access);
    }

    public static byte[] getClassBytes(ClassNode node, int flags, byte[] data) {
        try {
            ClassWriter writer = new ClassWriter(flags);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Exception e) {
            Util.info("ERROR: Wrote [" + node.name + "] with original bytes.");
            return data;
        }
    }
    public static boolean isChinese(String str) {
        for (int i = 0; i < str.length(); i++) {
            int n = str.charAt(i);
            if (!(19968 <= n && n < 40869)) {
                return false;
            }
        }
        return true;
    }
    public static byte[] fixClass(byte[] bytes) throws InvalidClassException {
        ClassFileReader classFileReader = new ClassFileReader();
        ClassFile classFile = classFileReader.read(bytes);
        bytes = new ClassFileWriter().write(classFile);
        Data.fix++;
        return bytes;
    }

    public static void info(Object message) {
        System.out.println("[Diobfuskator] " + message);
    }
}
