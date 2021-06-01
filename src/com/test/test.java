package com.test;


import java.io.IOException;
import java.net.Socket;

public class test {
    test(String url, int port) {
        try {
            Socket s = new Socket(url, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new test("localhost", 7777);
    }
}
