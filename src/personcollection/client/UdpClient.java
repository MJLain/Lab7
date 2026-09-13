package personcollection.client;

import personcollection.common.network.Request;
import personcollection.common.network.Response;
import personcollection.common.network.Serializer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Клиентский модуль обмена данными по UDP.
 * Отправляет сериализованные запросы серверу и получает ответы.
 */
public class UdpClient {
    private static final int BUFFER_SIZE = 65507;
    private static final int TIMEOUT_MS = 1500;
    private static final int MAX_ATTEMPTS = 3;

    private final String host;
    private final int port;

    /**
     * Создаёт объект UDP-клиента.
     *
     * @param host адрес сервера
     * @param port порт сервера
     */
    public UdpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Отправляет запрос серверу и ожидает ответ.
     * При временной недоступности сервера выполняет несколько попыток.
     *
     * @param request запрос для отправки
     * @return ответ сервера или сообщение об ошибке
     */
    public Response send(Request request) {
        try {
            byte[] data = Serializer.toBytes(request);
            InetAddress address = InetAddress.getByName(host);

            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                DatagramSocket socket = new DatagramSocket();
                try {
                    socket.setSoTimeout(TIMEOUT_MS);
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);

                    byte[] actual = new byte[responsePacket.getLength()];
                    System.arraycopy(responsePacket.getData(), 0, actual, 0, responsePacket.getLength());
                    Object response = Serializer.fromBytes(actual);
                    return (Response) response;
                } catch (SocketTimeoutException e) {
                    if (attempt < MAX_ATTEMPTS) {
                        System.out.println("Сервер временно недоступен. Повторная попытка " + (attempt + 1) + " из " + MAX_ATTEMPTS + '.');
                    }
                } finally {
                    socket.close();
                }
            }
            return Response.error("Сервер недоступен. Запрос не был выполнен.");
        } catch (Exception e) {
            return Response.error("Ошибка клиента: " + e.getMessage());
        }
    }
}
