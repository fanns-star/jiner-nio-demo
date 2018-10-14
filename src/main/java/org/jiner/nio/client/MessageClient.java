package org.jiner.nio.client;

import org.jiner.nio.MessageUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by fs on 2018/10/13.
 */
public class MessageClient {

    private InetSocketAddress address;

    public MessageClient(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

    public void start() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        //设置非阻塞模式
        socketChannel.configureBlocking(false);
        //建立连接
        socketChannel.connect(address);

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true){
            int readyChannels = selector.select();
            if (readyChannels == 0){
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey selectionKey = keyIterator.next();
                //处理connect事件
                if (selectionKey.isConnectable()){
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    //如果已完成三次握手，但是还没完成连接，则调用finishConnect()完成连接
                    if (!channel.isConnected() && channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    //注册读事件
                    channel.register(selectionKey.selector(), SelectionKey.OP_READ);
                    //新起一个线程用于从控制台接受消息并发送
                    new Thread(new MessageProducer(socketChannel)).start();
                }

                if (selectionKey.isReadable()){
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    String msg = MessageUtils.readMessage(channel);
                    //如果服务端关闭连接，客户端退出
                    if (msg == null){
                        System.exit(1);
                    }

                    System.out.println("receive message:"+msg);
                }

                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MessageClient messageClient = new MessageClient("127.0.0.1", 8090);
        messageClient.start();
    }
}
