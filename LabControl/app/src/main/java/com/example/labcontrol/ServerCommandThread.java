package com.example.labcontrol;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class ServerCommandThread extends Thread {
    MainActivity parent;
    String ip;
    String command;

    ServerCommandThread(MainActivity p, String i, String c) {
        parent = p;
        ip = i;
        command = c;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, parent.SERVER_PORT), 5000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write((command).getBytes());
            out.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            boolean seenNull = false;
            StringBuilder response = new StringBuilder();

            while ((bytesRead = in.read(buffer)) != -1) {
                String part = new String(buffer, 0, bytesRead);
                response.append(part);

                // Flushing to separate stages of a function
                int flushIndex = response.indexOf("\0");
                if (flushIndex != -1 && !seenNull) {
                    String startMsg = response.substring(0, flushIndex);
                    parent.showMessage(ip + ":\n" + startMsg.trim());
                    seenNull = true;

                    response.delete(0, flushIndex + 1);
                }
            }

            if (response.length() > 0) {
                parent.showMessage(ip + ":\n" + response.toString().trim());
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            parent.showMessage("❌ " + ip + ": Connection failed - " + e.getMessage());
        }
    }
}
