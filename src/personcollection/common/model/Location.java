package personcollection.common.model;

import java.io.Serializable;

/**
 * Местоположение объекта Person.
 * Используется для хранения координат места и его названия.
 */
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    private final float x;
    private final long y;
    private final Long z;
    private final String name;

    /**
     * Создаёт объект местоположения.
     *
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param name название места
     */
    public Location(float x, long y, Long z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        validate();
    }

    /**
     * Проверяет корректность полей местоположения.
     *
     * @throws IllegalArgumentException если данные объекта некорректны
     */
    public void validate() {
        if (z == null) {
            throw new IllegalArgumentException("location.z не может быть null.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("location.name не может быть пустым.");
        }
        if (name.length() > 968) {
            throw new IllegalArgumentException("location.name слишком длинное.");
        }
    }

    /**
     * Возвращает координату X.
     *
     * @return значение X
     */
    public float getX() {
        return x;
    }

    /**
     * Возвращает координату Y.
     *
     * @return значение Y
     */
    public long getY() {
        return y;
    }

    /**
     * Возвращает координату Z.
     *
     * @return значение Z
     */
    public Long getZ() {
        return z;
    }

    /**
     * Возвращает название местоположения.
     *
     * @return название места
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строковое представление местоположения
     */
    @Override
    public String toString() {
        return "Location{x=" + x + ", y=" + y + ", z=" + z + ", name='" + name + "'}";
    }
}
