# Limits
Настройка ограничений (лимитов) в системе приёма платежей

В задаче предполагается наличие некоторой системы приема платежей.
При попадании в систему платеж передается на обработку.
Платежи осуществляются на сумму за некоторую услугу в пользу клиента с лицевым счетом.
 
Задача:
 
В эту систему нужно добавить подсистему ограничений (лимитов) на прием платежей для борьбы с мошенничеством.
В решении должна быть возможность определять, например, такие лимиты для платежей:

1. Не более 5000 руб. днем с 9:00 утра до 23:00 за одну услугу(*)
2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*)
3. Не более 2000 руб. в сутки по одинаковым платежам(**)
4. Не более 3000 руб. в течение одного часа за одну услугу(*)
5. Не более 20 одинаковых платежей(**) в сутки
6. Не более 40 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*)
7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента

(\*) услуга - это, например, пополнение счета мобильного телефона (лицевой счет клиента) провайдера МТС

(\*\*) одинаковые платежи - платежи с одинаковыми счетом клиента и услугой

(***) сумма указана для совокупности платежей, т.е., например, клиент может сделать 1 платеж на 4000 руб. или 10 платежей по 400 руб., в сумме дающих 4000 руб.
 
Лимиты должны быть настраиваемые.
 
В случае, если какой-то из лимитов превышен, необходимо переводить "подозрительный" платеж в статус "требует подтверждения".
Платеж, прошедший ограничения, должен быть переведен в статус "готов к проведению".

В решении для тестов нужно реализовать примитивную систему приема платежей, в которую в тестах должны поступать платежи для проверки работоспособности лимитов из списка выше.

В тестах должны быть продемонстрированы варианты, когда задано несколько лимитов из списка выше, в т.ч. несколько лимитов, настроенных, для одной услуги.

Для тестов системе должны быть известны несколько (3-4) клиентов и несколько (3-4) услуг.

В качестве источника настроенных лимитов, которые система использует для проверки платежей, а также известных пользователей и услуг,
система должна использовать простые локальные объекты, базы данных использовать не нужно.
 
Требования к решению:
- Задачу необходимо решить на языке Java
- Можно считать, что платежи приходят в систему в одном потоке
- Требуется реализовать задачу с тестами (предпочтительно использование JUnit)
