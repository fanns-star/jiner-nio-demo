package org.jiner.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by fs on 2018/10/13.
 */
public class MessageUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String readMessage(SocketChannel channel) throws IOException {
        return readMessage(channel, "UTF-8");
    }

    public static String readMessage(SocketChannel channel, String charsetName) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        byte[] bytes = new byte[0];
        int readBytes = channel.read(byteBuffer);

        //读出的字节数为-1表示连接已断开
        if (readBytes == -1){
            channel.close();
            return null;
        }
        while (readBytes > 0){
            byteBuffer.flip();
            int start = bytes.length;
            bytes = Arrays.copyOf(bytes, start+byteBuffer.limit());
            byteBuffer.get(bytes, start, byteBuffer.limit());
            byteBuffer.clear();
            readBytes = channel.read(byteBuffer);
        }

        return new String(bytes, charsetName);
    }

    public static void sendMessage(String msg, SocketChannel socketChannel) throws IOException {
        if (msg == null){
            return;
        }

        byte[] msgBytes = msg.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(msgBytes.length);
        byteBuffer.put(msgBytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

}
