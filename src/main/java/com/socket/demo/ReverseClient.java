package com.socket.demo;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

// Socket client for test
public class ReverseClient {

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8888;

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();

            InputStream inputStream = Files.newInputStream(new File("/Users/mars/Developer/Git/GitHub/actuatordemo/src/main/resources/testfiles/multichunk.png").toPath());
            int n;
            byte[] buffer = new byte[10027];
            while ((n = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                output.flush();
                Thread.sleep(1000);
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}