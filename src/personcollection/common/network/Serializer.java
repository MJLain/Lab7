package personcollection.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Утилитный класс для сериализации и десериализации объектов.
 */
public final class Serializer {

    /**
     * Закрытый конструктор утилитного класса.
     */
    private Serializer() {
    }

    /**
     * Преобразует объект в массив байтов.
     *
     * @param object сериализуемый объект
     * @return массив байтов
     * @throws IOException если произошла ошибка сериализации
     */
    public static byte[] toBytes(Object object) throws IOException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteArray);
        objectStream.writeObject(object);
        objectStream.flush();
        return byteArray.toByteArray();
    }

    /**
     * Восстанавливает объект из массива байтов.
     *
     * @param bytes массив байтов
     * @return десериализованный объект
     * @throws IOException если произошла ошибка чтения
     * @throws ClassNotFoundException если не найден класс объекта
     */
    public static Object fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return objectStream.readObject();
    }
}
