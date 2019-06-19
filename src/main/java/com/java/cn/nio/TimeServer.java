package com.java.cn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class TimeServer {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    public TimeServer(int port) {
        try {
            //初始化选择器selector
            selector = Selector.open();

            //初始化serversocketchannel
            serverSocketChannel = ServerSocketChannel.open();
            //非阻塞
            serverSocketChannel.configureBlocking(false);
            //绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(port));

            //注册到选择器
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        //轮询
        while (true) {
            try {
                int wait = selector.select();
                if (wait == 0) continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    process(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void process(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ );
        } else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            if (client.read(buffer) > 0) {
                buffer.flip();
                String req = new String(buffer.array(), 0, buffer.remaining());
                System.out.println(req);
                String content = "QUERY TIME ORDER".equals(req) ? "Now is " + new Date() : "BAD ORDER";
                content += "\n";
                doWrite(client, content);
            } else {
                key.cancel();
                if (key.channel() != null)
                    key.channel().close();
            }
        } else if (key.isWritable()) {
            SocketChannel client = (SocketChannel) key.channel();
            doWrite(client, "hello");
        }
    }

    private void doWrite(SocketChannel client, String content) throws IOException {
        client.write(ByteBuffer.wrap(content.getBytes()));
    }

    public static void main(String[] args) {
        new TimeServer(8080).listen();
    }
}
