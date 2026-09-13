package personcollection.client;

import personcollection.common.model.HairColor;
import personcollection.common.model.Person;
import personcollection.common.network.CommandType;
import personcollection.common.network.Request;
import personcollection.common.network.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * Главный класс клиентского приложения.
 * Отвечает за чтение команд пользователя, формирование запросов и вывод ответов сервера.
 */
public class ClientMain {
    private final UdpClient udpClient;
    private final PersonReader personReader = new PersonReader();
    private final ArrayDeque<String> scripts = new ArrayDeque<String>();
    private boolean running = true;
    private String username;
    private String password;

    /**
     * Создаёт объект клиентского приложения.
     *
     * @param udpClient объект для отправки запросов серверу по UDP\
     */
    public ClientMain(UdpClient udpClient) {
        this.udpClient = udpClient;
    }

    /**
     * Точка входа в клиентское приложение.
     *
     * @param args аргументы командной строки: host и port
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Использование: java personcollection.client.ClientMain <host> <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Порт должен быть целым числом.");
            return;
        }

        ClientMain app = new ClientMain(new UdpClient(args[0], port));
        app.run();
    }

    /**
     * Запускает основной интерактивный цикл клиента.
     * Считывает команды из консоли до завершения работы приложения.
     */
    private void run() {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        if (!authorize(scanner)) {
            return;
        }
        System.out.println("Введите help для списка команд.");
        while (running) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                executeLine(line, scanner, true);
            }
        }
    }

    /**
     * Выполняет регистрацию или авторизацию пользователя перед работой с коллекцией.
     *
     * @param scanner источник входных данных
     * @return {@code true}, если пользователь успешно авторизован
     */
    private boolean authorize(Scanner scanner) {
        while (true) {
            System.out.print("Введите login или register: ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String action = scanner.nextLine().trim().toLowerCase();
            if (!"login".equals(action) && !"register".equals(action)) {
                System.out.println("Введите login для входа или register для регистрации.");
                continue;
            }

            System.out.print("Логин: ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String enteredUsername = scanner.nextLine().trim();

            System.out.print("Пароль: ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String enteredPassword = scanner.nextLine();

            CommandType type = "register".equals(action) ? CommandType.REGISTER : CommandType.LOGIN;
            Response response = udpClient.send(Request.auth(type, enteredUsername, enteredPassword));
            printResponse(response);
            if (response != null && response.isSuccess()) {
                username = enteredUsername;
                password = enteredPassword;
                return true;
            }
        }
    }

    /**
     * Обрабатывает одну введённую строку команды.
     *
     * @param line строка с командой
     * @param scanner источник входных данных
     * @param interactive {@code true}, если команда вводится из консоли; {@code false}, если из скрипта
     */
    private void executeLine(String line, Scanner scanner, boolean interactive) {
        String[] parts = line.split("\\s+", 2);
        String command = parts[0];
        String arg = parts.length > 1 ? parts[1].trim() : "";

        try {
            if ("exit".equals(command)) {
                running = false;
                return;
            }
            if ("execute_script".equals(command)) {
                executeScript(arg);
                return;
            }
            if ("save".equals(command)) {
                System.out.println("Команда save недоступна в клиентском приложении.");
                return;
            }

            Request request = buildRequest(command, arg, scanner, interactive);
            Response response = udpClient.send(request.withCredentials(username, password));
            printResponse(response);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Формирует объект запроса на основе имени команды и её аргумента.
     *
     * @param command имя команды
     * @param arg строковый аргумент команды
     * @param scanner источник входных данных
     * @param interactive режим ввода
     * @return готовый объект запроса
     * @throws IllegalArgumentException если команда неизвестна или аргумент некорректен
     */
    private Request buildRequest(String command, String arg, Scanner scanner, boolean interactive) {
        if ("help".equals(command)) {
            return Request.simple(CommandType.HELP);
        }
        if ("info".equals(command)) {
            return Request.simple(CommandType.INFO);
        }
        if ("show".equals(command)) {
            return Request.simple(CommandType.SHOW);
        }
        if ("add".equals(command)) {
            return Request.withPerson(CommandType.ADD, personReader.readPerson(scanner, interactive));
        }
        if ("update".equals(command)) {
            return Request.forUpdate(parseId(arg), personReader.readPerson(scanner, interactive));
        }
        if ("remove_by_id".equals(command)) {
            return Request.withId(CommandType.REMOVE_BY_ID, parseId(arg));
        }
        if ("clear".equals(command)) {
            return Request.simple(CommandType.CLEAR);
        }
        if ("shuffle".equals(command)) {
            return Request.simple(CommandType.SHUFFLE);
        }
        if ("sort".equals(command)) {
            return Request.simple(CommandType.SORT);
        }
        if ("history".equals(command)) {
            return Request.simple(CommandType.HISTORY);
        }
        if ("remove_all_by_height".equals(command)) {
            return Request.withHeight(CommandType.REMOVE_ALL_BY_HEIGHT, parseHeight(arg));
        }
        if ("sum_of_height".equals(command)) {
            return Request.simple(CommandType.SUM_OF_HEIGHT);
        }
        if ("print_field_ascending_hair_color".equals(command)) {
            return Request.simple(CommandType.PRINT_FIELD_ASCENDING_HAIR_COLOR);
        }
        throw new IllegalArgumentException("Неизвестная команда. Введите help.");
    }

    /**
     * Выполняет команды из файла-скрипта.
     * Защищается от рекурсивного вызова одного и того же скрипта.
     *
     * @param fileName имя файла скрипта
     * @throws IllegalArgumentException если имя файла не указано
     */
    private void executeScript(String fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Не указано имя файла.");
        }
        File file = new File(fileName);
        String path = file.getAbsolutePath();
        if (scripts.contains(path)) {
            System.out.println("Обнаружена рекурсия скриптов.");
            return;
        }

        scripts.addLast(path);
        try {
            Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name());
            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    System.out.println(">>> " + line);
                    executeLine(line, scanner, false);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Файл скрипта не найден.");
        } finally {
            scripts.removeLastOccurrence(path);
        }
    }

    /**
     * Преобразует строку в положительный идентификатор.
     *
     * @param arg строковое значение id
     * @return целочисленный идентификатор
     * @throws IllegalArgumentException если id отсутствует или задан неверно
     */
    private int parseId(String arg) {
        if (arg.isEmpty()) {
            throw new IllegalArgumentException("Не указан id.");
        }
        try {
            int id = Integer.parseInt(arg);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть целым числом больше 0.");
        }
    }

    /**
     * Преобразует строку в значение height.
     *
     * @param arg строковое значение height
     * @return значение роста
     * @throws IllegalArgumentException если аргумент отсутствует или задан неверно
     */
    private double parseHeight(String arg) {
        if (arg.isEmpty()) {
            throw new IllegalArgumentException("Не указан height.");
        }
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("height должен быть числом.");
        }
    }

    /**
     * Выводит ответ, полученный от сервера.
     *
     * @param response объект ответа сервера
     */
    private void printResponse(Response response) {
        if (response == null) {
            System.out.println("Пустой ответ от сервера.");
            return;
        }
        if (response.getMessage() != null && !response.getMessage().isEmpty()) {
            System.out.println(response.getMessage());
        }
        if (!response.getPeople().isEmpty()) {
            System.out.println(PrettyPrinter.formatPeople(response.getPeople()));
        }
        if (!response.getHairColors().isEmpty()) {
            System.out.println(PrettyPrinter.formatHairColors(response.getHairColors()));
        }
    }
}
