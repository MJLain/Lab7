package personcollection.server;

import personcollection.common.model.HairColor;
import personcollection.common.model.Location;
import personcollection.common.model.Person;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Управляет коллекцией объектов Person на сервере.
 * Отвечает за изменение, сортировку, удаление и получение информации о коллекции.
 */
public class CollectionManager {
    private final LinkedList<Person> people = new LinkedList<Person>();
    private final Date initDate = new Date();
    private final String fileName;
    private final ArrayDeque<String> history = new ArrayDeque<String>();

    private static final Comparator<Location> LOCATION_COMPARATOR = Comparator
            .comparingDouble(Location::getX)
            .thenComparingLong(Location::getY)
            .thenComparingLong(location -> location.getZ().longValue())
            .thenComparing(Location::getName);

    private static final Comparator<Person> PERSON_LOCATION_COMPARATOR = Comparator
            .comparing(Person::getLocation, LOCATION_COMPARATOR)
            .thenComparing(Person::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * Создаёт менеджер коллекции.
     *
     * @param fileName имя базы данных, с которой связана коллекция
     */
    public CollectionManager(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Полностью заменяет текущую коллекцию загруженными данными.
     *
     * @param loaded список загруженных элементов
     */
    public synchronized void replaceAll(List<Person> loaded) {
        people.clear();
        people.addAll(loaded);
    }

    /**
     * Сохраняет команду в истории последних запросов.
     *
     * @param command имя команды
     */
    public synchronized void remember(String command) {
        if (history.size() == 8) {
            history.removeFirst();
        }
        history.addLast(command);
    }

    /**
     * Добавляет новый объект в коллекцию.
     * id и дата создания к этому моменту уже назначены базой данных.
     *
     * @param person добавляемый объект
     */
    public synchronized void add(Person person) {
        person.validate();
        people.add(person);
    }

    /**
     * Обновляет элемент коллекции по идентификатору.
     *
     * @param id идентификатор изменяемого элемента
     * @param person новый объект Person
     * @return {@code true}, если элемент найден и обновлён
     */
    public synchronized boolean update(int id, Person person) {
        for (int i = 0; i < people.size(); i++) {
            Person current = people.get(i);
            if (current.getId() != null && current.getId().intValue() == id) {
                person.setId(current.getId());
                person.setCreationDate(current.getCreationDate());
                person.setOwner(current.getOwner());
                person.validate();
                people.set(i, person);
                return true;
            }
        }
        return false;
    }

    /**
     * Удаляет элемент по идентификатору.
     *
     * @param id идентификатор удаляемого элемента
     * @return {@code true}, если элемент был удалён
     */
    public synchronized boolean removeById(int id) {
        return people.removeIf(person -> person.getId() != null && person.getId().intValue() == id);
    }

    /**
     * Полностью очищает коллекцию.
     */
    public synchronized void clear() {
        people.clear();
    }

    /**
     * Удаляет из памяти только элементы указанного пользователя.
     *
     * @param owner логин владельца
     */
    public synchronized void clear(String owner) {
        people.removeIf(person -> owner.equals(person.getOwner()));
    }

    /**
     * Перемешивает элементы коллекции в случайном порядке.
     */
    public synchronized void shuffle() {
        Collections.shuffle(people);
    }

    /**
     * Сортирует коллекцию в естественном порядке объектов Person.
     */
    public synchronized void sort() {
        List<Person> sorted = people.stream().sorted().collect(Collectors.toList());
        people.clear();
        people.addAll(sorted);
    }

    /**
     * Удаляет все элементы с указанным значением height.
     *
     * @param height значение роста
     * @return количество удалённых элементов
     */
    public synchronized int removeAllByHeight(double height) {
        int before = people.size();
        people.removeIf(person -> person.getHeight() != null && Double.compare(person.getHeight().doubleValue(), height) == 0);
        return before - people.size();
    }

    /**
     * Удаляет из памяти элементы с указанным height, принадлежащие пользователю.
     *
     * @param height значение роста
     * @param owner логин владельца
     * @return количество удалённых элементов
     */
    public synchronized int removeAllByHeight(double height, String owner) {
        int before = people.size();
        people.removeIf(person -> owner.equals(person.getOwner()) &&
                person.getHeight() != null && Double.compare(person.getHeight().doubleValue(), height) == 0);
        return before - people.size();
    }

    /**
     * Вычисляет сумму всех ненулевых значений height.
     *
     * @return сумма значений роста
     */
    public synchronized double sumOfHeight() {
        return people.stream()
                .map(Person::getHeight)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /**
     * Возвращает список цветов волос по возрастанию.
     *
     * @return отсортированный список hairColor
     */
    public synchronized List<HairColor> getHairColorsAscending() {
        return people.stream()
                .map(Person::getHairColor)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список элементов, отсортированный по местоположению.
     *
     * @return отсортированный список Person
     */
    public synchronized List<Person> getSortedByLocation() {
        return people.stream().sorted(PERSON_LOCATION_COMPARATOR).collect(Collectors.toList());
    }

    /**
     * Возвращает копию текущей коллекции.
     *
     * @return снимок коллекции
     */
    public synchronized List<Person> snapshot() {
        return new ArrayList<Person>(people);
    }

    /**
     * Формирует строку с информацией о коллекции.
     *
     * @return строка с информацией о коллекции
     */
    public synchronized String getInfo() {
        return "Тип коллекции: java.util.LinkedList" + System.lineSeparator() +
                "Дата инициализации: " + initDate + System.lineSeparator() +
                "Количество элементов: " + people.size() + System.lineSeparator() +
                "База данных: " + fileName;
    }

    /**
     * Возвращает историю последних команд.
     *
     * @return история команд
     */
    public synchronized String getHistory() {
        if (history.isEmpty()) {
            return "История пуста.";
        }
        return history.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Возвращает справку по доступным командам клиента.
     *
     * @return текст справки
     */
    public synchronized String getHelp() {
        return "help : вывести справку по доступным командам" + System.lineSeparator() +
                "info : вывести информацию о коллекции" + System.lineSeparator() +
                "show : вывести все элементы коллекции" + System.lineSeparator() +
                "add : добавить новый элемент в коллекцию" + System.lineSeparator() +
                "update id : обновить элемент по id" + System.lineSeparator() +
                "remove_by_id id : удалить элемент по id" + System.lineSeparator() +
                "clear : очистить коллекцию" + System.lineSeparator() +
                "shuffle : перемешать коллекцию" + System.lineSeparator() +
                "sort : отсортировать коллекцию" + System.lineSeparator() +
                "history : вывести последние 8 команд" + System.lineSeparator() +
                "remove_all_by_height height : удалить элементы с указанным height" + System.lineSeparator() +
                "sum_of_height : вывести сумму значений height" + System.lineSeparator() +
                "print_field_ascending_hair_color : вывести hairColor по возрастанию" + System.lineSeparator() +
                "execute_script file_name : выполнить команды из файла" + System.lineSeparator() +
                "exit : завершить клиент";
    }
}
