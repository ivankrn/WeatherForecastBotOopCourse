# Проект для курса “ООП и разработка на Java”

**Идея проекта:** разработать Telegram - бота для просмотра прогноза погоды. С помощью данного бота пользователи могли бы узнавать актуальную информацию о прогнозе погоды для определенного города или места.

Для того, чтобы бот мог начать работу, создайте файл конфигурации application.yml в каталоге */src/main/resources* и добавьте следующие настройки:

```
bot:
  name: <название бота>
  token: <токен бота>
```

По умолчанию, бот использует H2 in-memory базу данных. Если вам нужна более детальная конфигурация, добавьте в файл
конфигурации application.yml необходимые изменения. Пример конфигурации БД представлен ниже:

```
spring:
  datasource:
    url: jdbc:h2:mem:mydb
    username: sa
    password: password
    driverClassName: org.h2.Driver
  jpa:
    spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

В приведенном выше примере устанавливается url, имя пользователя и пароль, а также драйвер для подключения к базе 
данных.

## Задача 1

Реализовать основной функционал проекта. Бот должен будет уметь:
Получать прогноз погоды по определенному местоположению, используя интеграцию c WeatherAPI (или, возможно, другой сервис)

Отправлять пользователю прогноз погоды в определенном населенном пункте
_Например, при вводе команды /info Екатеринбург пользователю бы присылалось сообщение с прогнозом погоды на сегодня, включая температуру и прочее._

Отправлять пользователю прогноз погоды в определенном населенном пункте на неделю вперёд
_Например, при вводе команды /info_week Екатеринбург пользователю бы присылалось сообщение с прогнозом погоды на неделю вперед._

Показывать справку по командам бота
_Например, при вводе команды /help пользователь мог бы узнать, какие команды поддерживаются ботом и что они делают._

Обеспечить возможность работы с несколькими пользователями одновременно

_Пример:_  
User: _/start_  
Bot: Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:  
* _/start_ - запустить бота
* _/help_ - меню помощи
* _/info <название населенного пункта>_ - вывести прогноз погоды для <населенного пункта>
* _/info_week <название населенного пункта>_ - вывести прогноз погоды для <название населенного пункта> на неделю вперёд

User: _/info Екатеринбург_  
Bot: Прогноз погоды в городе Екатеринбург на сегодня…  
User: _/info_week Екатеринбург_  
Bot: Прогноз погоды в городе Екатеринбург на неделю…

## Задача 2

Перейти на использование конечного автомата, чтобы пользователи могли взаимодействовать с ботом в форме диалога (не только через команды).
_Например, бот бы спрашивал пользователя про необходимое действие, после чего спрашивал бы про место, и в итоге запрашивал бы временной промежуток для прогноза погоды._

Помимо этого, добавить кнопки для упрощения работы с ботом.
_Например, на экране пользователя отображались бы кнопки с возможными действиями, благодаря чему вместо ввода команд, пользователь мог бы просто нажать по определенной кнопке и выбрать требуемое действие._

_Пример:_  
User: _/start_  
Bot: Здравствуйте!  
| Узнать прогноз | Помощь | Отмена |  
User: *нажимает на узнать прогноз*  
Bot: Введите название населенного пункта:  
| Отмена |
User: Екатеринбург  
Bot: Выберите временной период для просмотра:  
| Сегодня | Завтра | 5 дней |  
User: *нажимает на сегодня*  
Bot: Прогноз погоды на сегодня в городе Екатеринбург…


## Задача 3

Реализовать функционал уведомлений по сводке погоды. Пользователи смогут настроить отправку уведомлений в указанное время по прогнозу погоды в определенном месте.

Реализовать возможность просмотра созданных подписок на уведомления. Пользователи смогут смотреть, какие уведомления они настроили.

Реализовать возможность изменения созданной подписки на уведомления. Пользователь сможет изменять место прогноза погоды или время для уведомлений.

Реализовать возможность удаления созданной подписки на уведомления. Так пользователи смогут удалять ненужные и неактуальные подписки.

_Пример:_  
User: _/subscribe Екатеринбург 08:00_  
Bot: Напоминание создано. Буду присылать прогноз погоды в 08:00.  
User: _/show_subscriptions_  
Bot: 1) Екатеринбург, 08:00  
_на следующий день в 08:00 приходит напоминание_  
Bot: Прогноз погоды в городе Екатеринбург…  
User: _/edit_subscription 1 Екатеринбург 10:00_  
Bot: Напоминание изменено. Буду присылать прогноз погоды в 10:00.   
_на следующий день в 10:00 приходит напоминание_  
Bot: Прогноз погоды в городе Екатеринбург…  
User: _/del_subscription 1_  
Bot: Напоминание удалено. Больше не буду присылать прогноз погоды.
