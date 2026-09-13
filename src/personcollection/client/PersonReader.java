package personcollection.client;

import personcollection.common.model.Coordinates;
import personcollection.common.model.EyeColor;
import personcollection.common.model.HairColor;
import personcollection.common.model.Location;
import personcollection.common.model.Person;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Считывает объект {@link Person} из консоли или из скрипта.
 * Выполняет базовую валидацию пользовательского ввода.
 */
public class PersonReader {

    /**
     * Считывает все поля объекта Person и создаёт новый экземпляр.
     *
     * @param scanner источник входных данных
     * @param interactive {@code true}, если нужно показывать приглашения к вводу
     * @return созданный объект Person
     */
    public Person readPerson(Scanner scanner, boolean interactive) {
        String name = readNonEmpty(scanner, interactive, "Введите name: ");
        long coordX = readLong(scanner, interactive, "Введите coordinates.x: ");
        int coordY = readInt(scanner, interactive, "Введите coordinates.y: ");
        Double height = readNullableDouble(scanner, interactive, "Введите height (пустая строка = null): ");
        LocalDate birthday = readNullableDate(scanner, interactive, "Введите birthday YYYY-MM-DD (пустая строка = null): ");
        EyeColor eyeColor = readNullableEyeColor(scanner, interactive, "Введите eyeColor (пустая строка = null): ");
        HairColor hairColor = readNullableHairColor(scanner, interactive, "Введите hairColor (пустая строка = null): ");
        float locX = readFloat(scanner, interactive, "Введите location.x: ");
        long locY = readLong(scanner, interactive, "Введите location.y: ");
        long locZ = readLong(scanner, interactive, "Введите location.z: ");
        String locName = readNonEmpty(scanner, interactive, "Введите location.name: ");

        return new Person(
                name,
                new Coordinates(coordX, coordY),
                height,
                birthday,
                eyeColor,
                hairColor,
                new Location(locX, locY, Long.valueOf(locZ), locName)
        );
    }

    /**
     * Считывает одну строку из входного потока.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return считанная строка без внешних пробелов
     */
    private String readLine(Scanner scanner, boolean interactive, String prompt) {
        if (interactive) {
            System.out.print(prompt);
        }
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Недостаточно данных.");
        }
        return scanner.nextLine().trim();
    }

    /**
     * Считывает непустую строку.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return непустая строка
     */
    private String readNonEmpty(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            String value = readLine(scanner, interactive, prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Поле не может быть пустым.");
        }
    }

    /**
     * Считывает значение типа long.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return введённое значение long
     */
    private long readLong(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            try {
                return Long.parseLong(readLine(scanner, interactive, prompt));
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести long.");
            }
        }
    }

    /**
     * Считывает значение типа int.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return введённое значение int
     */
    private int readInt(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(scanner, interactive, prompt));
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести int.");
            }
        }
    }

    /**
     * Считывает значение типа float.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return введённое значение float
     */
    private float readFloat(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            try {
                return Float.parseFloat(readLine(scanner, interactive, prompt));
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести float.");
            }
        }
    }

    /**
     * Считывает необязательное значение роста.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return значение роста или {@code null}
     */
    private Double readNullableDouble(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            String value = readLine(scanner, interactive, prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                double parsed = Double.parseDouble(value);
                if (parsed <= 0) {
                    System.out.println("Число должно быть больше 0.");
                    continue;
                }
                return Double.valueOf(parsed);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести число.");
            }
        }
    }

    /**
     * Считывает необязательную дату рождения.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return дата рождения или {@code null}
     */
    private LocalDate readNullableDate(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            String value = readLine(scanner, interactive, prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
                System.out.println("Нужно ввести дату в формате YYYY-MM-DD.");
            }
        }
    }

    /**
     * Считывает необязательное значение цвета глаз.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return цвет глаз или {@code null}
     */
    private EyeColor readNullableEyeColor(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            String value = readLine(scanner, interactive, prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return EyeColor.valueOf(value.toUpperCase());
            } catch (Exception e) {
                System.out.println("Допустимые значения: BLUE, ORANGE, WHITE, BROWN.");
            }
        }
    }

    /**
     * Считывает необязательное значение цвета волос.
     *
     * @param scanner источник входных данных
     * @param interactive нужно ли выводить приглашение
     * @param prompt текст приглашения
     * @return цвет волос или {@code null}
     */
    private HairColor readNullableHairColor(Scanner scanner, boolean interactive, String prompt) {
        while (true) {
            String value = readLine(scanner, interactive, prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return HairColor.valueOf(value.toUpperCase());
            } catch (Exception e) {
                System.out.println("Допустимые значения: BLACK, YELLOW, BROWN.");
            }
        }
    }
}
