package com.dzz.io;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zoufeng
 * @date 2019/6/5
 */
public class MmpTest {

    private FileChannel fileChannel;

    private MappedByteBuffer writeBuffer;

    private AtomicInteger readPos = new AtomicInteger(0);

    private AtomicInteger writePos = new AtomicInteger(0);

    private String filePath = "D:\\temp\\temp.txt";

    private int fileSize = 1024 * 1024;


    @Before
    public void init() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            //create file
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");) {
                randomAccessFile.setLength(fileSize);
            }
        }

        System.out.println("init file ok !");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();
        writeBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
    }

    @Test
    public void testReadAndWrite() throws InterruptedException {
        new Thread(this::write).start();
        new Thread(this::read).start();
        Thread.sleep(1000 * 50);
    }


    private void write() {
        ByteBuffer slice = writeBuffer.slice();
        for (int i = 0; i < fileSize - 10; ) {
            slice.position(i);
            String random = RandomStringUtils.randomAlphabetic(10);
            byte[] bytes = random.getBytes();
            slice.put(bytes);
            writePos.addAndGet(bytes.length);
            i += bytes.length;
        }
    }

    private void read() {
        MappedByteBuffer slice = (MappedByteBuffer) writeBuffer.slice();
        slice.position(0);
        while (readPos.get() < 5000) {
            if (readPos.get() < writePos.get()) {
                slice.position(readPos.get());
                byte[] bytes = new byte[10];
                slice.get(bytes, 0, 10);
                System.out.println(new String(bytes));
                readPos.addAndGet(10);
            }

        }
    }


    /**
     * mmpbuffer测试
     *
     * @throws Exception 文件异常
     */
    @Test

    public void test1() throws Exception {
        String dir = "D:\\temp";
        RandomAccessFile memoryMappedFile;
        int size = 1024 * 1024;
        try {
            memoryMappedFile = new RandomAccessFile(dir + "/tmps.txt", "rw");
            MappedByteBuffer mappedByteBuffer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
            for (int i = 0; i < 100; ) {
                mappedByteBuffer.position(i);
                int index = RandomUtils.nextInt(1, 10);
                String random = RandomStringUtils.randomAlphabetic(index);
                byte[] bytes = random.getBytes();
                mappedByteBuffer.put(bytes);
                i = i + bytes.length;
            }
            memoryMappedFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
