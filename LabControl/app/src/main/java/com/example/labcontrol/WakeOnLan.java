package com.example.labcontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WakeOnLan {
    public static final Map<String, String> ipToMacMap =
            Stream.of(new String[][] {
                            { "192.168.88.2",  "50:81:40:2B:91:8D" },   // PRPC01
                            { "192.168.88.3",  "50:81:40:2B:7C:78" },   // PRPC02
                            { "192.168.88.4",  "50:81:40:2B:78:DD" },   // PRPC03
                            { "192.168.88.5",  "50:81:40:2B:7B:3D" },   // PRPC04
                            { "192.168.88.6",  "50:81:40:2B:79:91" },   // PRPC05
                            { "192.168.88.7",  "C8:5A:CF:0F:76:3D" },   // PRPC06
                            { "192.168.88.8",  "C8:5A:CF:0D:71:24" },   // PRPC07
                            { "192.168.88.9",  "C8:5A:CF:0F:B3:FF" },   // PRPC08
                            { "192.168.88.10", "C8:5A:CF:0E:2C:C4" },   // PRPC09
                            { "192.168.88.11", "C8:5A:CF:0F:7C:D0" },   // PRPC10
                            { "192.168.88.12", "C8:5A:CF:0D:71:3A" },   // PRPC11
                            { "192.168.88.13", "C8:5A:CF:0F:EE:01" },   // PRPC12
                            { "192.168.88.14", "C8:5A:CF:0E:1D:88" },   // PRPC13
                            { "192.168.88.15", "C8:5A:CF:0F:F0:1E" },   // PRPC14
                            { "192.168.88.16", "50:81:40:2B:7D:A4" },   // PRPC15
                            { "192.168.88.17", "C8:5A:CF:0E:2C:78" },   // PRPC16
                            { "192.168.88.18", "50:81:40:2B:87:F4" },   // PRPC17
                            { "192.168.88.19", "C8:5A:CF:0F:EC:11" },   // PRPC18
                            { "192.168.88.20", "C8:5A:CF:0F:7C:1F" },   // PRPC19
                            { "192.168.88.21", "C8:5A:CF:0D:71:2C" },   // PRPC20
                            { "192.168.88.22", "C8:5A:CF:0D:70:95" },   // PRPC21
                            { "192.168.88.23", "50:81:40:2B:5F:D0" },   // PRPC22
                            { "192.168.88.24", "50:81:40:2B:7A:0B" },   // PRPC23
                            { "192.168.88.25", "50:81:40:2B:8F:D3" },   // PRPC24
                            { "192.168.88.26", "50:81:40:2B:72:E0" },   // PRPC25
                            { "192.168.88.27", "50:81:40:2B:7A:74" },   // PRPC26
                            { "192.168.88.28", "C8:5A:CF:0F:7C:D4" }    // PRPC27DESK
                    })
                    .collect(Collectors.toMap(data -> data[0], data -> data[1],
                            (e1, e2) -> e1, LinkedHashMap::new));

    public static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address format: " + macStr);
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address: " + macStr);
        }
        return bytes;
    }

    public static void sendWakeOnLan(String broadcastIp, String macStr) throws IOException {
        byte[] macBytes = getMacBytes(macStr);

        byte[] packetData = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            packetData[i] = (byte) 0xFF;
        }
        for (int i = 6; i < packetData.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, packetData, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(broadcastIp);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, address, 9);
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.send(packet);
        socket.close();
    }
}
