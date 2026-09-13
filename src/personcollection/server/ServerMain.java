package personcollection.server;

import personcollection.common.model.Person;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Точка входа в серверное приложение.
 * Выполняет загрузку коллекции из PostgreSQL и запуск UDP-сервера.
 */
public class ServerMain {

    /**
     * Запускает серверное приложение.
     *
     * @param args аргументы командной строки: логин БД, порт сервера и пароль БД
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Использование: java personcollection.server.ServerMain <db-login> <port> <db-password>");
            return;
        }

        String dbUser = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Порт должен быть целым числом.");
            return;
        }
        String dbPassword = args[2];

        DatabaseManager databaseManager = new DatabaseManager("pg", 5432, "studs", dbUser, dbPassword);
        CollectionManager manager = new CollectionManager("studs");

        try {
            databaseManager.initializeSchema();
            LinkedList<Person> loaded = databaseManager.loadPeople();
            manager.replaceAll(loaded);
            System.out.println("Коллекция загружена из PostgreSQL. Элементов: " + loaded.size());
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к базе данных: " + e.getMessage());
            return;
        }

        try {
            new UdpServer(manager, databaseManager, port).run();
        } catch (IOException e) {
            System.out.println("Не удалось запустить сервер: " + e.getMessage());
        }
    }
}
