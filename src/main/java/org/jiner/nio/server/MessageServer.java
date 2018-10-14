package org.jiner.nio.server;

import org.jiner.nio.MessageUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fs on 2018/10/13.
 */
public class MessageServer {

    private int port;

    private final List<SocketChannel> clients = new ArrayList<>();

    public MessageServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //使用selector必须将channel设为非阻塞模式
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(this.port));

        Selector selector = Selector.open();
        //注册accept事件
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            //如果selector里没有已就绪的通道，select()方法将一直阻塞到有就绪的通道为止
            // 另外有select(long timeout)会阻塞timeout毫秒，如果还没有就绪通道就返回0
            //selectNow()方法不管有没有就绪通道会立即返回
            int readyChannel = selector.select(10);
            if (readyChannel == 0){
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey selectionKey = keyIterator.next();
                //处理accept事件
                if (selectionKey.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //跟ServerSocketChannel一样要设为非阻塞模式
                    socketChannel.configureBlocking(false);
                    //向selector注册read事件
                    socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
                    clients.add(socketChannel);
                    System.out.println("one client connect, total clients:"+clients.size());
                }

                //处理read事件
                if (selectionKey.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    String msg = MessageUtils.readMessage(socketChannel);
                    //读出来的消息为null说明客户端已关闭链接，将服务端的channel关闭，并从client列表中移除
                    if (msg == null){
                        socketChannel.close();
                        clients.remove(socketChannel);
                    }

                    System.out.println("received:"+msg);
                    forwardMessage(msg, socketChannel);
                }

                keyIterator.remove();
            }
        }

    }

    //转发消息给其他客户端
    public void forwardMessage(String msg, SocketChannel socketChannel) throws IOException {
        int count = 0;
        for (SocketChannel client : clients){
            //不给发送者发消息
            if (client == socketChannel){
                continue;
            }

            MessageUtils.sendMessage(msg, client);
            count++;
        }

        System.out.println("send message:{"+msg+"}  to "+count+" clients");
    }

    public static void main(String[] args) throws IOException {
        MessageServer messageServer = new MessageServer(8090);
        messageServer.start();
    }
}
