package org.jiner.nio.client;

import org.jiner.nio.MessageUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Created by fs on 2018/10/13.
 */
public class MessageProducer implements Runnable {

    private SocketChannel socketChannel;

    public MessageProducer(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        while (true){
            //从控制台输入
            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();
            try {
                MessageUtils.sendMessage(msg, socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
