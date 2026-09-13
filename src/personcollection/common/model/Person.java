package personcollection.common.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * Класс, описывающий человека в коллекции.
 * Объекты этого класса хранятся на сервере и передаются между клиентом и сервером.
 */
public class Person implements Comparable<Person>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private final String name;
    private final Coordinates coordinates;
    private Date creationDate;
    private final Double height;
    private final LocalDate birthday;
    private final EyeColor eyeColor;
    private final HairColor hairColor;
    private final Location location;
    private String owner;

    /**
     * Создаёт новый объект Person без автоматически генерируемых полей.
     *
     * @param name имя человека
     * @param coordinates координаты
     * @param height рост
     * @param birthday дата рождения
     * @param eyeColor цвет глаз
     * @param hairColor цвет волос
     * @param location местоположение
     */
    public Person(String name, Coordinates coordinates, Double height, LocalDate birthday,
                  EyeColor eyeColor, HairColor hairColor, Location location) {
        this.name = name;
        this.coordinates = coordinates;
        this.height = height;
        this.birthday = birthday;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.location = location;
        validate();
    }

    /**
     * Проверяет корректность данных объекта.
     *
     * @throws IllegalArgumentException если данные объекта некорректны
     */
    public void validate() {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("id должен быть больше 0.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name не может быть пустым.");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates не может быть null.");
        }
        if (height != null && height <= 0) {
            throw new IllegalArgumentException("height должен быть больше 0.");
        }
        if (location == null) {
            throw new IllegalArgumentException("location не может быть null.");
        }
        if (id != null && creationDate == null) {
            throw new IllegalArgumentException("creationDate не может быть null, если id уже задан.");
        }
        location.validate();
    }

    /**
     * Возвращает идентификатор объекта.
     *
     * @return id объекта
     */
    public Integer getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор объекта.
     *
     * @param id идентификатор
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Возвращает имя человека.
     *
     * @return имя
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает координаты человека.
     *
     * @return объект координат
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Возвращает дату создания объекта.
     *
     * @return дата создания
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Устанавливает дату создания объекта.
     *
     * @param creationDate дата создания
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Возвращает рост человека.
     *
     * @return рост или {@code null}
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Возвращает дату рождения человека.
     *
     * @return дата рождения или {@code null}
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * Возвращает цвет глаз.
     *
     * @return цвет глаз или {@code null}
     */
    public EyeColor getEyeColor() {
        return eyeColor;
    }

    /**
     * Возвращает цвет волос.
     *
     * @return цвет волос или {@code null}
     */
    public HairColor getHairColor() {
        return hairColor;
    }

    /**
     * Возвращает местоположение человека.
     *
     * @return объект местоположения
     */
    public Location getLocation() {
        return location;
    }


    /**
     * Возвращает логин пользователя, создавшего объект.
     *
     * @return логин владельца
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Устанавливает владельца объекта.
     *
     * @param owner логин владельца
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Сравнивает два объекта Person по идентификатору.
     *
     * @param other другой объект Person
     * @return отрицательное число, ноль или положительное число в зависимости от результата сравнения
     */
    @Override
    public int compareTo(Person other) {
        if (id == null && other.id == null) {
            return 0;
        }
        if (id == null) {
            return -1;
        }
        if (other.id == null) {
            return 1;
        }
        return id.compareTo(other.id);
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строковое представление Person
     */
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", height=" + height +
                ", birthday=" + birthday +
                ", eyeColor=" + eyeColor +
                ", hairColor=" + hairColor +
                ", location=" + location +
                ", owner='" + owner + '\'' +
                '}';
    }
}
