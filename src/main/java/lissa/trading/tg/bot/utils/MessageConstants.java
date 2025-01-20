package lissa.trading.tg.bot.utils;

import lombok.Data;

@Data
public class MessageConstants {
    public static final String BRAND_INFO_MESSAGE = """
            <b>Название компании:</b> %s
            <b>Тикеры компании:</b> %s
            <b>Описание компании:</b> %s
            <b>Сектор:</b> %s
            <b>Страна:</b> %s
            <a href="%s">Сайт компании</a>
            """;

    public static final String IDEAS_MESSAGE = """
            <b>Название идеи:</b> %s
            <b>Брокер:</b> %s
            <b>Точность прогнозов брокера:</b> %s
            <b>Тикеры:</b> %s
            <b>Начальная цена:</b> %s
            <b>Актуальная цена:</b> %s
            <b>Доходность:</b> %s
            <b>Целевая доходность:</b> %s
            <b>Дата начала:</b> %s
            <b>Дата окончания:</b> %s
            <a href="%s">Ссылка на идею</a>
            """;

    public static final String PULSE_NEWS_MESSAGE = """
            <b>Новость по тикерам:</b> %s
            <b>Дата публикации:</b> %s
            <b>Создатель:</b> %s
            <b>Содержание:</b> %s
            <a href="%s">Ссылка на новость</a>
            """;

    public static final String NEWS_MESSAGE = """
            <b>Название:</b> %s
            <b>Дата публикации:</b> %s
            <a href="%s">Ссылка на новость</a>
            """;

    public static final String CHOOSE_TYPE_MESSAGE = """
            Выберите желаемое:
            news - Новости
            ideas - Идеи для инвестиций
            brandInfo - Информацию о компаниях
            /pulse - Обновить тикеры
            """;

    public static final String PRINT_TICKERS_MESSAGE = "Напишите тикеры компаний " +
            "о которых желаете получить информацию\n" +
            "В формате: YDEX,SBER,VTBR";

    public static final String HELP_MESSAGE = """
            Этот бот позволяет вам управлять вашими избранными акциями и получать уведомления, когда их цены изменяются более чем на 3% за последний час.

            Вот список доступных команд:

            /start - Начать взаимодействие с ботом
            /token - Обновить ваш Tinkoff токен
            /info - Получить информацию о вашем аккаунте
            /favourites - Просмотреть ваши избранные акции
            /refresh - Обновить данные и получить актуальную информацию
            /pulse - Получить информацию об акциях из Tinkoff Pulse
            /news -  Получить новости по акциям из разных ресурсов
            /addFavourites - Добавить любимую акцию по тикеру
            /removeFavorites - Удалить любимую акцию по тикеру
            /cancel - Отменить текущую операцию
            /help - Показать это сообщение помощи
            """;

    public static final String UNKNOWN_COMMAND_MESSAGE = "Извините, я не понимаю это сообщение. " +
            "Пожалуйста, используйте команду или введите /help для списка доступных команд.";

    public static final String USER_INFO_MESSAGE = """
            Информация о пользователе:
            -------------------------
            ID: %s
            Никнейм: %s
            """;

    public static final String ADD_TICKERS_MESSAGE = "Напишите тикеры компаний" +
            "которые желаете добавить в избранное\n" +
            "в формате: YDEX,SBER,VTBR";

    public static final String REMOVE_TICKERS_MESSAGE = "Напишите тикеры компаний" +
            "которые желаете удалить из избранного\n" +
            "в формате: YDEX,SBER,VTBR";

    public static final String FAVORITES_OPERATION_SUCCESS = "Успешно, для просмотра используйте \"/favourites\"";


    public static final String FAVORITES_OPERATION_FAIL = "Произошла внутренняя ошибка сервиса." +
            " Повторите запрос позже";

    public static final String TICKERS_LIST_PATTERN = "^[A-Za-z0-9]+(,[A-Za-z0-9]+)*$";

    public static final String INVALID_TICKERS_MESSAGE = "Некорректный формат. Убедитесь, " +
            "что тикеры указаны через запятую без пробелов или других символов.";

    public static final String SENT_REQUEST_MESSAGE = "Запрос отправлен, ожидайте...";

    public static final String INVALID_TOKEN_MESSAGE = "Невалидный токен, вместо тинькофф инвестиций" +
            " будет использоваться Московская биржа";
}