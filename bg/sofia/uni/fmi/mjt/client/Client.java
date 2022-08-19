package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private static final String INVALID_INPUT = "invalid input";

    private static final int SERVER_PORT = 9999;
    private static final int BUFFER_SIZE = 2048;
    private static final String SERVER_HOST = "localhost";

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server");

            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                if (command == null || command.trim().isEmpty() || System.lineSeparator().equals(command)) {
                    command = INVALID_INPUT;
                }

                buffer.clear();
                buffer.put(command.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println(reply);

                if (command.equals("disconnect")) {
                    break;
                }
            }


        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }

    }

}
