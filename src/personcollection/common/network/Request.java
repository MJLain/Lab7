package personcollection.common.network;

import personcollection.common.model.Person;

import java.io.Serializable;

/**
 * Объект запроса, который клиент отправляет серверу.
 * Содержит тип команды и её аргументы.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandType type;
    private final Integer id;
    private final Double height;
    private final Person person;
    private String username;
    private String password;

    /**
     * Создаёт объект запроса.
     *
     * @param type тип команды
     * @param id идентификатор объекта
     * @param height значение роста
     * @param person объект Person
     */
    private Request(CommandType type, Integer id, Double height, Person person) {
        this.type = type;
        this.id = id;
        this.height = height;
        this.person = person;
    }

    /**
     * Создаёт запрос без аргументов.
     *
     * @param type тип команды
     * @return объект запроса
     */
    public static Request simple(CommandType type) {
        return new Request(type, null, null, null);
    }

    /**
     * Создаёт запрос с аргументом id.
     *
     * @param type тип команды
     * @param id идентификатор объекта
     * @return объект запроса
     */
    public static Request withId(CommandType type, int id) {
        return new Request(type, Integer.valueOf(id), null, null);
    }

    /**
     * Создаёт запрос с аргументом height.
     *
     * @param type тип команды
     * @param height значение роста
     * @return объект запроса
     */
    public static Request withHeight(CommandType type, double height) {
        return new Request(type, null, Double.valueOf(height), null);
    }

    /**
     * Создаёт запрос с объектом Person.
     *
     * @param type тип команды
     * @param person объект Person
     * @return объект запроса
     */
    public static Request withPerson(CommandType type, Person person) {
        return new Request(type, null, null, person);
    }

    /**
     * Создаёт запрос для команды update.
     *
     * @param id идентификатор изменяемого элемента
     * @param person новый объект Person
     * @return объект запроса
     */
    public static Request forUpdate(int id, Person person) {
        return new Request(CommandType.UPDATE, Integer.valueOf(id), null, person);
    }

    /**
     * Создаёт запрос регистрации или авторизации.
     *
     * @param type REGISTER или LOGIN
     * @param username логин пользователя
     * @param password пароль пользователя
     * @return объект запроса
     */
    public static Request auth(CommandType type, String username, String password) {
        Request request = new Request(type, null, null, null);
        request.username = username;
        request.password = password;
        return request;
    }

    /**
     * Добавляет логин и пароль к запросу.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @return этот же объект запроса
     */
    public Request withCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public CommandType getType() {
        return type;
    }

    public Integer getId() {
        return id;
    }

    public Double getHeight() {
        return height;
    }

    public Person getPerson() {
        return person;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
