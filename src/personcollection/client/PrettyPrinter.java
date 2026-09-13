package personcollection.client;

import personcollection.common.model.HairColor;
import personcollection.common.model.Person;

import java.util.List;

/**
 * Вспомогательный класс для форматирования данных,
 * полученных от сервера, в удобный для чтения текст.
 */
public final class PrettyPrinter {

    /**
     * Закрытый конструктор утилитного класса.
     */
    private PrettyPrinter() {
    }

    /**
     * Форматирует список объектов Person для вывода в консоль.
     *
     * @param people список элементов коллекции
     * @return строка для вывода
     */
    public static String formatPeople(List<Person> people) {
        if (people == null || people.isEmpty()) {
            return "Коллекция пуста.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Элементы коллекции: ").append(people.size()).append("\n");

        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            sb.append("\n");
            sb.append(i + 1).append(") id=").append(person.getId())
                    .append(", name=").append(person.getName()).append("\n");
            sb.append("   coordinates: ").append(person.getCoordinates()).append("\n");
            sb.append("   owner: ").append(value(person.getOwner())).append("\n");
            sb.append("   creationDate: ").append(value(person.getCreationDate())).append("\n");
            sb.append("   height: ").append(value(person.getHeight())).append("\n");
            sb.append("   birthday: ").append(value(person.getBirthday())).append("\n");
            sb.append("   eyeColor: ").append(value(person.getEyeColor())).append("\n");
            sb.append("   hairColor: ").append(value(person.getHairColor())).append("\n");
            sb.append("   location: ").append(value(person.getLocation())).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Форматирует список цветов волос.
     *
     * @param hairColors список цветов волос
     * @return строка для вывода
     */
    public static String formatHairColors(List<HairColor> hairColors) {
        if (hairColors == null || hairColors.isEmpty()) {
            return "Список пуст.";
        }

        StringBuilder sb = new StringBuilder("Цвета волос:\n");
        for (HairColor hairColor : hairColors) {
            sb.append("- ").append(hairColor).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Преобразует объект в строку с обработкой {@code null}.
     *
     * @param object объект для преобразования
     * @return строковое представление объекта или символ "-"
     */
    private static String value(Object object) {
        return object == null ? "-" : object.toString();
    }
}
