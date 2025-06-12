package com.example.labcontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WakeOnLan {

    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int WOL_PORT = 9;

    public static void sendWolPacket(String mac) {
        EXECUTOR.execute(() -> {
            try {
                wake(mac);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void wake(final String mac) throws IOException {
        wake(mac, WOL_PORT);
    }

    public static void wake(final String mac, final int port) throws IOException {
        final byte[] address = parseMac(mac);

        final byte[] payload = buildPayload(address);

        final SocketAddress destination = new InetSocketAddress(BROADCAST_ADDRESS, port);
        final DatagramPacket packet = new DatagramPacket(payload, payload.length, destination);

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(packet);
        }
    }

    private static byte[] parseMac(String mac) {
        String[] hex = mac.split("[:-]");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }

        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }

    private static byte[] buildPayload(final byte[] address) {
        final ByteBuffer packet = ByteBuffer.wrap(new byte[6 + (16 * address.length)]);
        for (int i = 0; i < 6; i++) {
            packet.put((byte) 0xFF);
        }

        for (int i = 0; i < 16; i++) {
            packet.put(address);
        }

        return packet.array();
    }

    private WakeOnLan() {}
}
