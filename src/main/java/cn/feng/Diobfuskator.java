package cn.feng;

import cn.feng.transform.Transformer;
import cn.feng.transform.composed.CleanTransformer;
import cn.feng.util.Util;
import me.coley.cafedude.InvalidClassException;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author ChengFeng
 * @since 2023/9/2
 **/
public class Diobfuskator extends Util {
    public final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private final Builder config;
    public Map<ClassNode, byte[]> classes = new ConcurrentHashMap<>();

    private Diobfuskator(Builder builder) {
        config = builder;
    }

    private void execute() {
        long start = System.currentTimeMillis();
        info("Running transformers: ");
        config.transformers.forEach(it -> info("-" + it.getClass().getSimpleName()));
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(config.output));
             JarFile jar = new JarFile(config.input.toFile())) {
            Map<String, byte[]> write = new ConcurrentHashMap<>();


            load(zos, jar, write);
            transform(write);
            write(write, zos);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        info("Took " + (System.currentTimeMillis() - start) / 1000f + "s, bypassed " + Data.html + " html crashers, fixed " + Data.fix + " invalid classes.");
    }

    private void load(ZipOutputStream zos, JarFile jar, Map<String, byte[]> write) throws IOException, InterruptedException {
        info("Loading...");
        Enumeration<JarEntry> entries = jar.entries();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            String name = entry.getName();
            byte[] data = IOUtils.toByteArray(jar.getInputStream(entry));

            if (name.startsWith("<html>")) {
                Data.html++;
                continue;
            }

            if (!isClass(name, data)) {
                zos.putNextEntry(entry);
                zos.write(data);
                zos.closeEntry();
                continue;
            }

            executor.execute(() -> {
                ClassNode node;
                try {
                    node = loadClass(data, config.READER_FLAGS);
                } catch (InvalidClassException e) {
                    info("ERROR: [" + name + "] not loaded.");
                    write.put(name, data);
                    return;
                }

                classes.put(node, data);
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        executor.close();

        info("Loaded " + classes.size() + " classes.");
    }

    private void transform(Map<String, byte[]> write) throws InterruptedException {
        info("Transforming...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (Map.Entry<ClassNode, byte[]> entry : classes.entrySet()) {
            ClassNode node = entry.getKey();
            executor.execute(() -> {
                if (!noTransform(node)) {
                    for (Transformer transformer : config.transformers) {
                        transformer.transform(node);
                    }
                }

                write.put(node.name + ".class", getClassBytes(node, config.WRITER_FLAGS, entry.getValue()));
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        executor.close();
        info("Transformed.");
    }

    private void write(Map<String, byte[]> write, ZipOutputStream zos) throws IOException {
        info("Writing...");
        for (Map.Entry<String, byte[]> entry : write.entrySet()) {
            zos.putNextEntry(new ZipEntry(entry.getKey()));
            zos.write(entry.getValue());
            zos.closeEntry();
        }
        info("Wrote.");
    }

    public static class Builder {
        private final List<Transformer> transformers;
        private Path input;
        private Path output;
        private int READER_FLAGS;
        private int WRITER_FLAGS;

        public Builder() {
            transformers = new ArrayList<>();
            READER_FLAGS = 0;
            WRITER_FLAGS = 0;
        }

        public Builder clean() {
            this.transformers.add(new CleanTransformer());
            return this;
        }

        public Builder input(Path input) {
            this.input = input;
            return this;
        }

        public Builder output(Path output) {
            this.output = output;
            return this;
        }

        public Builder readerFlags(int flags) {
            this.READER_FLAGS = flags;
            return this;
        }

        public Builder writerFlags(int flags) {
            this.WRITER_FLAGS = flags;
            return this;
        }

        public Builder transformers(Transformer... transformers) {
            Collections.addAll(this.transformers, transformers);
            return this;
        }

        public void execute() {
            new Diobfuskator(this).execute();
        }
    }
}
