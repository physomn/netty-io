package com.java.cn.nio;

import java.io.*;
import java.net.Socket;

public class TimeClient {

    public static void main(String[] args) {
        try {
            Socket client = new Socket("localhost", 8080);
            PrintStream printStream = new PrintStream(client.getOutputStream());
            printStream.print("QUERY TIME ORDER");
            printStream.flush();

            InputStream inputStream = client.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            System.out.println(line);

            reader.close();
            inputStream.close();
            printStream.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
