package personcollection.server;

import personcollection.common.model.Coordinates;
import personcollection.common.model.EyeColor;
import personcollection.common.model.HairColor;
import personcollection.common.model.Location;
import personcollection.common.model.Person;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.LinkedList;

/**
 * Выполняет работу с PostgreSQL через JDBC.
 */
public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;

    /**
     * Создаёт менеджер базы данных.
     *
     * @param host адрес PostgreSQL
     * @param port порт PostgreSQL
     * @param database имя базы данных
     * @param user имя пользователя БД
     * @param password пароль пользователя БД
     */
    public DatabaseManager(String host, int port, String database, String user, String password) {
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.user = user;
        this.password = password;
    }

    /**
     * Открывает JDBC-соединение с базой данных.
     *
     * @return соединение с PostgreSQL
     * @throws SQLException если соединение установить не удалось
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Создаёт sequence и таблицы, необходимые программе.
     *
     * @throws SQLException если запрос к БД завершился ошибкой
     */
    public void initializeSchema() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS person_collection_users (" +
                            "username VARCHAR(64) PRIMARY KEY, " +
                            "password_hash VARCHAR(32) NOT NULL" +
                            ")");

            statement.executeUpdate(
                    "CREATE SEQUENCE IF NOT EXISTS person_collection_id_seq " +
                            "START WITH 1 INCREMENT BY 1");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS person_collection_people (" +
                            "id INTEGER PRIMARY KEY DEFAULT nextval('person_collection_id_seq'), " +
                            "name TEXT NOT NULL CHECK (btrim(name) <> ''), " +
                            "coord_x BIGINT NOT NULL, " +
                            "coord_y INTEGER NOT NULL, " +
                            "creation_date TIMESTAMP NOT NULL, " +
                            "height DOUBLE PRECISION CHECK (height IS NULL OR height > 0), " +
                            "birthday DATE, " +
                            "eye_color VARCHAR(16), " +
                            "hair_color VARCHAR(16), " +
                            "loc_x REAL NOT NULL, " +
                            "loc_y BIGINT NOT NULL, " +
                            "loc_z BIGINT NOT NULL, " +
                            "loc_name VARCHAR(968) NOT NULL CHECK (btrim(loc_name) <> ''), " +
                            "owner_username VARCHAR(64) NOT NULL " +
                            "REFERENCES person_collection_users(username)" +
                            ")");

            statement.executeUpdate(
                    "ALTER SEQUENCE person_collection_id_seq " +
                            "OWNED BY person_collection_people.id");
        }
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param username логин
     * @param rawPassword пароль в открытом виде
     * @return true, если пользователь был создан
     * @throws SQLException если запрос к БД завершился ошибкой
     */
    public boolean register(String username, String rawPassword) throws SQLException {
        validateCredentials(username, rawPassword);
        String sql = "INSERT INTO person_collection_users(username, password_hash) " +
                "VALUES (?, ?) ON CONFLICT (username) DO NOTHING";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashPassword(rawPassword));
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Проверяет логин и пароль пользователя.
     *
     * @param username логин
     * @param rawPassword пароль в открытом виде
     * @return true, если учётные данные верны
     * @throws SQLException если запрос к БД завершился ошибкой
     */
    public boolean authenticate(String username, String rawPassword) throws SQLException {
        if (username == null || rawPassword == null) {
            return false;
        }

        String sql = "SELECT username FROM person_collection_users " +
                "WHERE username = ? AND password_hash = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashPassword(rawPassword));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Загружает коллекцию из БД при запуске сервера.
     * Дальнейшие команды чтения работают уже с коллекцией в памяти.
     *
     * @return загруженная коллекция
     * @throws SQLException если запрос к БД завершился ошибкой
     */
    public LinkedList<Person> loadPeople() throws SQLException {
        String sql = "SELECT id, name, coord_x, coord_y, creation_date, height, birthday, " +
                "eye_color, hair_color, loc_x, loc_y, loc_z, loc_name, owner_username " +
                "FROM person_collection_people ORDER BY id";

        LinkedList<Person> result = new LinkedList<Person>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(readPerson(resultSet));
            }
        }

        return result;
    }

    /**
     * Добавляет Person в БД. id создаётся sequence базы данных.
     * Поля id, creationDate и owner устанавливаются объекту только после успешного INSERT.
     *
     * @param person добавляемый объект
     * @param owner логин владельца
     * @throws SQLException если INSERT завершился ошибкой
     */
    public void insertPerson(Person person, String owner) throws SQLException {
        person.validate();
        java.util.Date creationDate = new java.util.Date();

        String sql = "INSERT INTO person_collection_people(" +
                "name, coord_x, coord_y, creation_date, height, birthday, eye_color, hair_color, " +
                "loc_x, loc_y, loc_z, loc_name, owner_username) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"})) {

            bindPersonForInsert(statement, person, creationDate, owner);
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Объект не был добавлен в базу данных.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("База данных не вернула id добавленного объекта.");
                }

                person.setId(Integer.valueOf(generatedKeys.getInt(1)));
                person.setCreationDate(creationDate);
                person.setOwner(owner);
                person.validate();
            }
        }
    }

    /**
     * Обновляет объект, только если он принадлежит указанному пользователю.
     */
    public boolean updatePerson(int id, Person person, String owner) throws SQLException {
        person.validate();

        String sql = "UPDATE person_collection_people SET " +
                "name = ?, coord_x = ?, coord_y = ?, height = ?, birthday = ?, " +
                "eye_color = ?, hair_color = ?, loc_x = ?, loc_y = ?, loc_z = ?, loc_name = ? " +
                "WHERE id = ? AND owner_username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, person.getName());
            statement.setLong(index++, person.getCoordinates().getX());
            statement.setInt(index++, person.getCoordinates().getY());
            setNullableDouble(statement, index++, person.getHeight());
            setNullableDate(statement, index++, person.getBirthday());
            setNullableString(statement, index++, enumName(person.getEyeColor()));
            setNullableString(statement, index++, enumName(person.getHairColor()));
            statement.setFloat(index++, person.getLocation().getX());
            statement.setLong(index++, person.getLocation().getY());
            statement.setLong(index++, person.getLocation().getZ().longValue());
            statement.setString(index++, person.getLocation().getName());
            statement.setInt(index++, id);
            statement.setString(index, owner);

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Удаляет объект, только если он принадлежит указанному пользователю.
     */
    public boolean deleteById(int id, String owner) throws SQLException {
        String sql = "DELETE FROM person_collection_people WHERE id = ? AND owner_username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, owner);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Удаляет все объекты указанного пользователя.
     */
    public int deleteAllByOwner(String owner) throws SQLException {
        String sql = "DELETE FROM person_collection_people WHERE owner_username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            return statement.executeUpdate();
        }
    }

    /**
     * Удаляет объекты с указанным height, принадлежащие указанному пользователю.
     */
    public int deleteByHeightAndOwner(double height, String owner) throws SQLException {
        String sql = "DELETE FROM person_collection_people " +
                "WHERE height = ? AND owner_username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, height);
            statement.setString(2, owner);
            return statement.executeUpdate();
        }
    }

    /**
     * Создаёт Person по текущей строке ResultSet.
     */
    private Person readPerson(ResultSet resultSet) throws SQLException {
        Number heightValue = (Number) resultSet.getObject("height");
        Double height = heightValue == null ? null : Double.valueOf(heightValue.doubleValue());

        Date birthdaySql = resultSet.getDate("birthday");
        LocalDate birthday = birthdaySql == null ? null : birthdaySql.toLocalDate();

        String eyeColor = resultSet.getString("eye_color");
        String hairColor = resultSet.getString("hair_color");

        Person person = new Person(
                resultSet.getString("name"),
                new Coordinates(resultSet.getLong("coord_x"), resultSet.getInt("coord_y")),
                height,
                birthday,
                eyeColor == null ? null : EyeColor.valueOf(eyeColor),
                hairColor == null ? null : HairColor.valueOf(hairColor),
                new Location(
                        resultSet.getFloat("loc_x"),
                        resultSet.getLong("loc_y"),
                        Long.valueOf(resultSet.getLong("loc_z")),
                        resultSet.getString("loc_name")
                )
        );

        person.setId(Integer.valueOf(resultSet.getInt("id")));
        person.setCreationDate(new java.util.Date(resultSet.getTimestamp("creation_date").getTime()));
        person.setOwner(resultSet.getString("owner_username"));
        person.validate();
        return person;
    }

    /**
     * Подставляет параметры INSERT через PreparedStatement.
     */
    private void bindPersonForInsert(PreparedStatement statement, Person person,
                                     java.util.Date creationDate, String owner) throws SQLException {
        int index = 1;
        statement.setString(index++, person.getName());
        statement.setLong(index++, person.getCoordinates().getX());
        statement.setInt(index++, person.getCoordinates().getY());
        statement.setTimestamp(index++, new Timestamp(creationDate.getTime()));
        setNullableDouble(statement, index++, person.getHeight());
        setNullableDate(statement, index++, person.getBirthday());
        setNullableString(statement, index++, enumName(person.getEyeColor()));
        setNullableString(statement, index++, enumName(person.getHairColor()));
        statement.setFloat(index++, person.getLocation().getX());
        statement.setLong(index++, person.getLocation().getY());
        statement.setLong(index++, person.getLocation().getZ().longValue());
        statement.setString(index++, person.getLocation().getName());
        statement.setString(index, owner);
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value.doubleValue());
        }
    }

    private void setNullableDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(value));
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    /**
     * Хэширует пароль алгоритмом MD2.
     */
    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD2");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : hash) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Алгоритм MD2 недоступен.", e);
        }
    }

    private void validateCredentials(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }
        if (username.length() > 64) {
            throw new IllegalArgumentException("Логин не должен быть длиннее 64 символов.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }
    }
}
