package personcollection.server;

import personcollection.common.network.Request;
import personcollection.common.network.Response;
import personcollection.common.network.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * UDP-сервер, принимающий запросы клиента, обрабатывающий их и отправляющий ответы.
 * Для чтения, обработки и отправки используются разные пулы потоков.
 */
public class UdpServer {
    private static final int BUFFER_SIZE = 65507;

    private final CommandProcessor processor;
    private final int port;
    private volatile boolean running = true;

    /**
     * Создаёт объект UDP-сервера.
     *
     * @param manager менеджер коллекции
     * @param databaseManager менеджер базы данных
     * @param port порт сервера
     */
    public UdpServer(CollectionManager manager, DatabaseManager databaseManager, int port) {
        this.processor = new CommandProcessor(manager, databaseManager);
        this.port = port;
    }

    /**
     * Запускает основной цикл сервера.
     *
     * @throws IOException если произошла ошибка при работе с каналом или селектором
     */
    public void run() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        Selector selector = Selector.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_READ);

        // Требования лабораторной №7:
        // чтение запросов — CachedThreadPool;
        // обработка запросов — FixedThreadPool;
        // отправка ответов — ForkJoinPool.
        ExecutorService readPool = Executors.newCachedThreadPool();
        ExecutorService processingPool = Executors.newFixedThreadPool(4);
        ForkJoinPool sendPool = new ForkJoinPool();

        BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        System.out.println("Сервер запущен на порту " + port + ".");
        System.out.println("Локальные команды сервера: help, save, exit");

        try {
            while (running) {
                processServerConsole(console);
                selector.select(200);

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isReadable()) {
                        readPool.submit(() -> readRequest(channel, processingPool, sendPool));
                    }
                }
            }
        } finally {
            readPool.shutdown();
            await(readPool);

            processingPool.shutdown();
            await(processingPool);

            sendPool.shutdown();
            await(sendPool);

            channel.close();
            selector.close();
        }
    }

    /**
     * Обрабатывает локальные команды сервера.
     */
    private void processServerConsole(BufferedReader console) throws IOException {
        while (console.ready()) {
            String line = console.readLine();
            if (line == null) {
                return;
            }

            String command = line.trim();
            if (command.isEmpty()) {
                return;
            }

            if ("help".equals(command)) {
                System.out.println("help : показать локальные команды сервера");
                System.out.println("save : данные сохраняются в PostgreSQL автоматически");
                System.out.println("exit : завершить сервер");
            } else if ("save".equals(command)) {
                System.out.println("Данные сохраняются в PostgreSQL автоматически.");
            } else if ("exit".equals(command)) {
                running = false;
            } else {
                System.out.println("Неизвестная локальная команда сервера.");
            }
        }
    }

    /**
     * Считывает один UDP-запрос в потоке CachedThreadPool и передаёт его на обработку.
     */
    private void readRequest(DatagramChannel channel,
                             ExecutorService processingPool,
                             ForkJoinPool sendPool) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress client = channel.receive(buffer);
            if (client == null) {
                return;
            }

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Object object = Serializer.fromBytes(data);
            if (!(object instanceof Request)) {
                sendPool.submit(() -> sendSafely(
                        channel, client, Response.error("Некорректный формат запроса.")));
                return;
            }

            Request request = (Request) object;

            processingPool.submit(() -> {
                Response response = processor.process(request);
                sendPool.submit(() -> sendSafely(channel, client, response));
            });
        } catch (Exception e) {
            System.out.println("Ошибка чтения запроса: " + e.getMessage());
        }
    }

    /**
     * Безопасно отправляет ответ клиенту из ForkJoinPool.
     */
    private void sendSafely(DatagramChannel channel, SocketAddress client, Response response) {
        try {
            byte[] bytes = Serializer.toBytes(response);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            synchronized (channel) {
                channel.send(buffer, client);
            }
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }

    /**
     * Ожидает завершения задач пула перед остановкой сервера.
     */
    private void await(ExecutorService pool) {
        try {
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
