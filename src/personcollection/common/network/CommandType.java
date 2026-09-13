package personcollection.common.network;

import java.io.Serializable;

/**
 * Перечисление команд, которые клиент может отправлять серверу.
 */
public enum CommandType implements Serializable {
    REGISTER,
    LOGIN,
    HELP,
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    SHUFFLE,
    SORT,
    HISTORY,
    REMOVE_ALL_BY_HEIGHT,
    SUM_OF_HEIGHT,
    PRINT_FIELD_ASCENDING_HAIR_COLOR
}
