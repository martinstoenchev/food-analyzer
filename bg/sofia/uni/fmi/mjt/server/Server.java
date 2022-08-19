package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.command.Command;
import bg.sofia.uni.fmi.mjt.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.server.exceptions.BadInputParameterException;
import bg.sofia.uni.fmi.mjt.server.exceptions.FoodAnalyzerException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.server.food.Food;
import bg.sofia.uni.fmi.mjt.server.food.FoodByName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String TRY_AGAIN_MESSAGE = " Please, try again";

    private static final int SERVER_PORT = 9999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private Food[] foods;
    private final CommandExecutor commandExecutor;

    public Server(CommandExecutor commandExecutor) {
        this(SERVER_PORT, commandExecutor);
    }

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();

                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput;

                            try {
                                clientInput = getClientInput(clientChannel);
                            } catch (IOException e) {
                                continue;
                            }

                            if (clientInput == null) {
                                continue;
                            }

                            try {
                                String output = commandExecutor.execute(CommandCreator.newCommand(clientInput));
                                writeClientOutput(clientChannel, output);
                            } catch (FoodAnalyzerException e) {
                                writeClientOutput(clientChannel, e.getMessage());
                            } catch (IllegalStateException e) {
                                writeClientOutput(clientChannel, TRY_AGAIN_MESSAGE);
                            }
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start server", e);
        }

    }

    public void stop() {
        isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = socketChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        if (output.length() > BUFFER_SIZE) {
            output = reduceResults(output);
        }

        buffer.clear();
        buffer.put(output.getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        clientChannel.write(buffer);
    }

    private String reduceResults(String output) {
        String[] lines = output.split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            if (sb.length() + line.length() + System.lineSeparator().length() < BUFFER_SIZE) {
                sb.append(line).append(System.lineSeparator());
            } else {
                break;
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newCachedThreadPool();
        HttpClient httpClient = HttpClient.newBuilder().executor(executorService).build();

        Path pathOfCacheFile = Path.of("cache.txt");
        Path pathOfLogFile = Path.of("log.txt");

        Server server = new Server(new CommandExecutor(pathOfCacheFile, pathOfLogFile, httpClient));
        server.start();

    }

}
