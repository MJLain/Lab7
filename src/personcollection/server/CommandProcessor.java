package personcollection.server;

import personcollection.common.model.Person;
import personcollection.common.network.CommandType;
import personcollection.common.network.Request;
import personcollection.common.network.Response;

import java.sql.SQLException;
import java.util.List;

/**
 * Обрабатывает запросы клиента и вызывает соответствующие операции менеджеров.
 */
public class CommandProcessor {
    private final CollectionManager manager;
    private final DatabaseManager databaseManager;

    /**
     * Создаёт обработчик команд.
     *
     * @param manager менеджер коллекции
     * @param databaseManager менеджер базы данных
     */
    public CommandProcessor(CollectionManager manager, DatabaseManager databaseManager) {
        this.manager = manager;
        this.databaseManager = databaseManager;
    }

    /**
     * Выполняет обработку клиентского запроса.
     *
     * @param request запрос клиента
     * @return результат выполнения команды
     */
    public Response process(Request request) {
        try {
            CommandType type = request.getType();

            if (type == CommandType.REGISTER) {
                return databaseManager.register(request.getUsername(), request.getPassword())
                        ? Response.ok("Пользователь зарегистрирован.")
                        : Response.error("Пользователь с таким логином уже существует.");
            }

            if (type == CommandType.LOGIN) {
                return databaseManager.authenticate(request.getUsername(), request.getPassword())
                        ? Response.ok("Авторизация успешна.")
                        : Response.error("Неверный логин или пароль.");
            }

            if (!databaseManager.authenticate(request.getUsername(), request.getPassword())) {
                return Response.error("Необходимо авторизоваться.");
            }

            manager.remember(type.name().toLowerCase());

            if (type == CommandType.HELP) {
                return Response.ok(manager.getHelp());
            }
            if (type == CommandType.INFO) {
                return Response.ok(manager.getInfo());
            }
            if (type == CommandType.SHOW) {
                List<Person> people = manager.getSortedByLocation();
                return people.isEmpty()
                        ? Response.ok("Коллекция пуста.")
                        : Response.withPeople("Элементы коллекции (отсортированы по location):", people);
            }
            if (type == CommandType.ADD) {
                synchronized (manager) {
                    databaseManager.insertPerson(request.getPerson(), request.getUsername());
                    manager.add(request.getPerson());
                }
                return Response.ok("Элемент добавлен.");
            }
            if (type == CommandType.UPDATE) {
                synchronized (manager) {
                    if (databaseManager.updatePerson(
                            request.getId().intValue(), request.getPerson(), request.getUsername())) {
                        manager.update(request.getId().intValue(), request.getPerson());
                        return Response.ok("Элемент обновлен.");
                    }
                }
                return Response.error("Элемент не найден или принадлежит другому пользователю.");
            }
            if (type == CommandType.REMOVE_BY_ID) {
                synchronized (manager) {
                    if (databaseManager.deleteById(request.getId().intValue(), request.getUsername())) {
                        manager.removeById(request.getId().intValue());
                        return Response.ok("Элемент удален.");
                    }
                }
                return Response.error("Элемент не найден или принадлежит другому пользователю.");
            }
            if (type == CommandType.CLEAR) {
                synchronized (manager) {
                    int removed = databaseManager.deleteAllByOwner(request.getUsername());
                    manager.clear(request.getUsername());
                    return Response.ok("Удалено ваших элементов: " + removed);
                }
            }
            if (type == CommandType.SHUFFLE) {
                manager.shuffle();
                return Response.ok("Коллекция перемешана.");
            }
            if (type == CommandType.SORT) {
                manager.sort();
                return Response.ok("Коллекция отсортирована.");
            }
            if (type == CommandType.HISTORY) {
                return Response.ok(manager.getHistory());
            }
            if (type == CommandType.REMOVE_ALL_BY_HEIGHT) {
                synchronized (manager) {
                    int removed = databaseManager.deleteByHeightAndOwner(
                            request.getHeight().doubleValue(), request.getUsername());
                    manager.removeAllByHeight(request.getHeight().doubleValue(), request.getUsername());
                    return Response.ok("Удалено элементов: " + removed);
                }
            }
            if (type == CommandType.SUM_OF_HEIGHT) {
                return Response.ok("Сумма height: " + manager.sumOfHeight());
            }
            if (type == CommandType.PRINT_FIELD_ASCENDING_HAIR_COLOR) {
                return Response.withHairColors(
                        "Значения hairColor по возрастанию:", manager.getHairColorsAscending());
            }

            return Response.error("Неизвестная команда.");
        } catch (IllegalArgumentException e) {
            return Response.error("Ошибка: " + e.getMessage());
        } catch (SQLException e) {
            return Response.error("Ошибка базы данных: " + e.getMessage());
        }
    }
}
