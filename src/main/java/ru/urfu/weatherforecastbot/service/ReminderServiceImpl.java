package ru.urfu.weatherforecastbot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.urfu.weatherforecastbot.bot.Bot;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.WeatherForecastBot;
import ru.urfu.weatherforecastbot.database.ReminderRepository;
import ru.urfu.weatherforecastbot.model.Reminder;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ReminderServiceImpl implements ReminderService {

    /**
     * Сообщение исключения о неверной позиции при удалении напоминания
     */
    private static final String WRONG_REMINDER_POSITION_EXCEPTION_MESSAGE = "Wrong reminder position provided!";
    /**
     * Форматировщик даты и времени
     */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
    /**
     * Бот
     */
    private final Bot bot;
    /**
     * Обработчик запросов на получение прогнозов погоды
     */
    private final WeatherForecastRequestHandler weatherForecastRequestHandler;
    /**
     * Репозиторий для хранения напоминаний
     */
    private final ReminderRepository reminderRepository;
    /**
     * ExecutorService для планирования отправления напоминаний
     */
    private final ScheduledExecutorService executorService;
    /**
     * Запланированные задачи на отправку напоминаний<br>
     * В качестве ключа используется ID напоминания, в качестве значения - Future с задачей отправления напоминания
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    /**
     * Создает экземпляр ReminderServiceImpl, используя в качестве executorService
     * {@code Executors.newSingleThreadScheduledExecutor()} и forecastFormatter {@link WeatherForecastServiceImpl}
     *
     * @param bot                бот
     * @param weatherService     сервис для получения прогнозов погоды
     * @param reminderRepository репозиторий напоминаний
     */
    @Autowired
    public ReminderServiceImpl(WeatherForecastBot bot, WeatherForecastService weatherService,
                               ReminderRepository reminderRepository) {
        this.bot = bot;
        this.weatherForecastRequestHandler =
                new WeatherForecastRequestHandlerImpl(weatherService, new WeatherForecastFormatterImpl());
        this.reminderRepository = reminderRepository;
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Создает экземпляр ReminderServiceImpl, используя в качестве executorService переданный экземпляр
     * {@code ScheduledExecutorService}
     *
     * @param bot                бот
     * @param weatherService     сервис для получения прогнозов погоды
     * @param forecastFormatter  форматировщик прогноза погоды
     * @param reminderRepository репозиторий напоминаний
     * @param executorService    executorService
     */
    public ReminderServiceImpl(WeatherForecastBot bot, WeatherForecastService weatherService,
                               WeatherForecastFormatter forecastFormatter, ReminderRepository reminderRepository,
                               ScheduledExecutorService executorService) {
        this.bot = bot;
        this.weatherForecastRequestHandler = new WeatherForecastRequestHandlerImpl(weatherService, forecastFormatter);
        this.reminderRepository = reminderRepository;
        this.executorService = executorService;
    }

    @Override
    public void addReminder(long chatId, String placeName, String time) throws DateTimeParseException {
        LocalTime parsedTime = LocalTime.parse(time, dateTimeFormatter);
        Reminder reminder = new Reminder();

        reminder.setChatId(chatId);
        reminder.setPlaceName(placeName);
        reminder.setTime(parsedTime);

        reminder = reminderRepository.save(reminder);
        scheduleReminder(reminder);
    }

    @Override
    public void deleteReminderByRelativePosition(long chatId, int position) throws IllegalArgumentException {
        List<Reminder> reminders = reminderRepository.findAllByChatId(chatId);
        if (position <= 0 || position < reminders.size() || position > reminders.size()) {
            throw new IllegalArgumentException(WRONG_REMINDER_POSITION_EXCEPTION_MESSAGE);
        }
        Reminder reminderToDelete = reminders.get(position - 1);
        cancelReminderById(reminderToDelete.getId());
        reminderRepository.deleteById(reminderToDelete.getId());
    }

    /**
     * Создает задачу на отправку напоминания в {@link ReminderServiceImpl#executorService executorService}
     * и помещает Future задачи в {@link ReminderServiceImpl#scheduledTasks scheduledTasks}
     *
     * @param reminder напоминание
     */
    private void scheduleReminder(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime nextRun = now
                .withHour(reminder.getTime().getHour())
                .withMinute(reminder.getTime().getMinute())
                .withSecond(0);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        long delay = Duration.between(now, nextRun).getSeconds();
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                () -> sendReminder(reminder),
                delay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        scheduledTasks.put(reminder.getId(), future);
    }

    /**
     * Отправляет напоминание, используя {@link ReminderServiceImpl#bot бота}
     *
     * @param reminder напоминание
     */
    private void sendReminder(Reminder reminder) {
        BotMessage message = new BotMessage();
        message.setText(
                weatherForecastRequestHandler.handleForecasts(reminder.getPlaceName(), ForecastTimePeriod.TODAY));
        bot.sendMessage(reminder.getChatId(), message);
    }

    /**
     * Вызывается после инициализации бина и инъекции зависимостей
     */
    @PostConstruct
    private void postConstruct() {
        recoverReminders();
    }

    /**
     * Восстанавливает напоминания после повторного запуска бота
     */
    private void recoverReminders() {
        List<Reminder> reminders = reminderRepository.findAll();
        for (Reminder reminder : reminders) {
            scheduleReminder(reminder);
        }
    }

    /**
     * Отменяет выполнение задачи на отправку напоминания
     *
     * @param id ID напоминания
     */
    private void cancelReminderById(long id) {
        scheduledTasks.get(id).cancel(false);
    }

}
