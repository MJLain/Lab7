package personcollection.common.model;

import java.io.Serializable;

/**
 * Координаты объекта Person.
 * Используются как часть модели человека и передаются по сети в сериализованном виде.
 */
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long x;
    private final int y;

    /**
     * Создаёт объект координат.
     *
     * @param x координата X
     * @param y координата Y
     */
    public Coordinates(long x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает координату X.
     *
     * @return значение X
     */
    public long getX() {
        return x;
    }

    /**
     * Возвращает координату Y.
     *
     * @return значение Y
     */
    public int getY() {
        return y;
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строковое представление координат
     */
    @Override
    public String toString() {
        return "Coordinates{x=" + x + ", y=" + y + '}';
    }
}
