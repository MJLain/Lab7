package personcollection.common.network;

import personcollection.common.model.HairColor;
import personcollection.common.model.Person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Объект ответа сервера.
 * Содержит текст результата выполнения команды и дополнительные данные.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final List<Person> people;
    private final List<HairColor> hairColors;

    /**
     * Создаёт объект ответа.
     *
     * @param success признак успешного выполнения команды
     * @param message текст ответа
     * @param people список объектов Person
     * @param hairColors список цветов волос
     */
    private Response(boolean success, String message, List<Person> people, List<HairColor> hairColors) {
        this.success = success;
        this.message = message;
        this.people = people == null ? new ArrayList<Person>() : people;
        this.hairColors = hairColors == null ? new ArrayList<HairColor>() : hairColors;
    }

    /**
     * Создаёт успешный ответ без дополнительных данных.
     *
     * @param message текст ответа
     * @return объект ответа
     */
    public static Response ok(String message) {
        return new Response(true, message, null, null);
    }

    /**
     * Создаёт ответ с ошибкой.
     *
     * @param message текст ошибки
     * @return объект ответа
     */
    public static Response error(String message) {
        return new Response(false, message, null, null);
    }

    /**
     * Создаёт успешный ответ со списком элементов коллекции.
     *
     * @param message текст ответа
     * @param people список элементов
     * @return объект ответа
     */
    public static Response withPeople(String message, List<Person> people) {
        return new Response(true, message, people, null);
    }

    /**
     * Создаёт успешный ответ со списком цветов волос.
     *
     * @param message текст ответа
     * @param colors список цветов волос
     * @return объект ответа
     */
    public static Response withHairColors(String message, List<HairColor> colors) {
        return new Response(true, message, null, colors);
    }

    /**
     * Возвращает признак успешного выполнения команды.
     *
     * @return {@code true}, если команда выполнена успешно
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Возвращает текст ответа.
     *
     * @return текст ответа
     */
    public String getMessage() {
        return message;
    }

    /**
     * Возвращает список объектов Person.
     *
     * @return список элементов коллекции
     */
    public List<Person> getPeople() {
        return people;
    }

    /**
     * Возвращает список цветов волос.
     *
     * @return список цветов волос
     */
    public List<HairColor> getHairColors() {
        return hairColors;
    }
}
