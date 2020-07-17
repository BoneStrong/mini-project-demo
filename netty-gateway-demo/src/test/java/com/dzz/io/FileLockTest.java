package com.dzz.io;

import org.junit.Test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * @author zoufeng
 * @date 2020-7-17
 */
public class FileLockTest {

    @Test
    public void testLock() throws IOException, InterruptedException {
        File file = new File("D:\\temp\\filelock.txt");

        //�����ļ�����
        RandomAccessFile fis = new RandomAccessFile(file, "rw");
        FileChannel channel = fis.getChannel();
        FileLock lock = null;
        try {
            lock = channel.tryLock();
            System.out.println("���Ƿ���" + lock.isShared());
            System.out.println("��ȡ����ʼ���ļ�");
        } catch (Exception e) {
            System.out.println("�������߳����ڲ������ļ�����ǰ�߳�����1000����");
            sleep(1000);
        }
        readFile(fis);

        new Thread(() -> {
            while (true) {
                try {
                    readFile(fis);
                    System.out.println(Thread.currentThread().getName() + "��ȡ�ɹ�������1s");
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        System.out.println(Thread.currentThread().getName() + "��ȡʧ�ܣ�����1s");
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, "�߳�2").start();

        TimeUnit.SECONDS.sleep(10);

        System.out.println("������ϣ��ͷ���");
        if (lock != null)
            lock.release();
        System.out.println("���ͷ����");

        Thread.sleep(Integer.MAX_VALUE);

    }

    private void readFile(RandomAccessFile fis) throws IOException {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while ((fis.read(buf)) != -1) {
            sb.append(new String(buf, "utf-8"));
            buf = new byte[1024];
        }
        System.out.println(Thread.currentThread().getName() + " : " + sb);
    }
}
