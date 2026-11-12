# Этап 2. Статистика на главной странице `/`

## Цель этапа
Отображать на главной странице `/` персональную статистику по задачам текущего пользователя: количество завершённых, среднее время выполнения, самую быструю задачу, завершённые за неделю и месяц, процент выполнено в срок.

## Текущее состояние реализации (факт)

Код этапа 2 уже реализован, тесты написаны, миграция V5 применена. Ниже приведено описание фактического состояния для согласования.

### Реализованные классы

| Класс | Путь | Статус |
|-------|------|--------|
| `Task` | `src/main/java/com/site/webapp/models/Task.java` | Уже изменён: добавлено поле `completedAt`, индекс `idx_tasks_completed_at` |
| `TaskRepository` | `src/main/java/com/site/webapp/repo/TaskRepository.java` | Уже изменён: добавлены методы статистики с `@Query(nativeQuery = true)` |
| `TaskService` | `src/main/java/com/site/webapp/service/TaskService.java` | Уже изменён: добавлен `getStatistics()`, логика `completedAt` в `updateTask()` |
| `TaskStatisticsDto` | `src/main/java/com/site/webapp/dto/TaskStatisticsDto.java` | Уже создан |
| `MainController` | `src/main/java/com/site/webapp/controllers/MainController.java` | Уже изменён: инжектит `TaskService`, вычисляет статистику |
| `home.html` | `src/main/resources/templates/home.html` | Уже изменён: отображает статистику |
| `V5__add_completed_at_to_tasks.sql` | `src/main/resources/db/migration/V5__add_completed_at_to_tasks.sql` | Уже создана |

### Реализованные тесты

| Тест | Путь | Статус |
|------|------|--------|
| `TaskRepositoryTest` | `src/test/java/com/site/webapp/repo/TaskRepositoryTest.java` | Уже создан, покрывает Stage 2 методы |
| `TaskServiceTest` | `src/test/java/com/site/webapp/service/TaskServiceTest.java` | Уже создан, покрывает статистику и переходы статусов |
| `MainControllerTest` | `src/test/java/com/site/webapp/controllers/MainControllerTest.java` | Уже создан, покрывает контроллер |

## Поля модели `Task` (фактические)

| Поле | Тип | Ограничения | Комментарий |
|------|-----|-------------|-------------|
| `id` | Long | `@Id`, `@GeneratedValue(SEQUENCE)` | |
| `title` | String | `@NotBlank`, `nullable = false` | |
| `priority` | Priority | `@NotNull`, `nullable = false` | |
| `status` | TaskStatus | `@NotNull`, `nullable = false`, `ENUM` | NEW, IN_PROGRESS, TESTING, REVIEW, COMPLETED, CANCELLED, ARCHIVED |
| `artistId` | Long | nullable | |
| `ownerId` | Long | `@NotNull`, `nullable = false`, `updatable = false` | Владелец задачи, используется для персональной статистики |
| `deadline` | LocalDateTime | **Требуется nullable** (см. правила ниже) | В текущем коде `@NotNull`, `@Future` — требуется изменение |
| `comment` | String | `@Size(max=500)` | |
| `createdAt` | LocalDateTime | `@CreationTimestamp`, `nullable = false`, `updatable = false` | Устанавливается Hibernate при вставке, не менять |
| `updatedAt` | LocalDateTime | `@UpdateTimestamp` | Обновляется при любом изменении |
| `completedAt` | LocalDateTime | `nullable = true` | Дата фактического завершения, заполняется сервисом |
| `deletedAt` | LocalDateTime | nullable | |
| `deleted` | boolean | `nullable = false`, default `false` | Мягкое удаление |
| `version` | Long | `@Version` | Оптимистичная блокировка |

## Правила completedAt (обязательные, уже реализованы)

1. При переходе из любого статуса в `COMPLETED`, если `completedAt == null`, установить `LocalDateTime.now()`.
2. При переходе из `COMPLETED` в любой другой статус обязательно установить `completedAt = null`.
3. При редактировании задачи, которая уже находится в `COMPLETED`, не менять `completedAt`.
4. При повторном завершении задачи после возврата из `COMPLETED` установить новое время завершения.

Реализовано в `TaskService.updateTask()` (строки 127-133):
```java
Task.TaskStatus newStatus = task.getStatus();
if (newStatus == Task.TaskStatus.COMPLETED && oldStatus != Task.TaskStatus.COMPLETED && task.getCompletedAt() == null) {
    task.setCompletedAt(LocalDateTime.now());
} else if (newStatus != Task.TaskStatus.COMPLETED && oldStatus == Task.TaskStatus.COMPLETED) {
    task.setCompletedAt(null);
}
```

Места изменения статуса задачи в проекте:
1. `TaskService.createTask()` — статус `NEW`, `completedAt` не устанавливается (остаётся `null`).
2. `TaskService.updateTask()` — при изменении статуса (см. логику выше).
`TaskPlanService` и `TaskPlanScheduler` только читают задачи и не меняют статус.

## Правила deadline (окончательные)

1. `deadline` может быть `NULL` (задача может существовать без дедлайна).
2. Убрать `@NotNull` с поля `deadline` в `Task.java`.
3. Убрать `@Future` с поля `deadline` в `Task.java` (проверка "не в прошлом" переносится на уровень DTO/Service).
4. Новый дедлайн нельзя устанавливать в прошлом — проверка на уровне DTO/Service.
5. Просроченную задачу разрешено редактировать без изменения её дедлайна.
6. При обновлении проверять, изменился ли `deadline` относительно сохранённого значения. Если не изменился, старый просроченный `deadline` разрешено сохранить.
7. Если текущая БД имеет `NOT NULL` для `deadline`, создать новую миграцию `V6__make_deadline_nullable.sql`. Старые миграции не редактировать.
8. `@CreationTimestamp` на `createdAt` оставить без изменений.

## Правила статистики (уже реализованы)

1. Учитывать только задачи текущего пользователя (фильтрация по `ownerId`).
2. Исключать `deleted = true`.
3. Для процента своевременности учитывать только завершённые задачи с заполненными `deadline` и `completedAt`.
4. Использовать `completedAt` для временных интервалов.
5. Неделя начинается в понедельник, часовой пояс `Europe/Moscow` (жестко задан через `ZoneId.of("Europe/Moscow")`).
6. Интервалы недели и месяца полуоткрытые: `[start, end)`.

## Формулы расчёта (фактические)

### Среднее время выполнения
```sql
SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600), 0)
FROM tasks
WHERE owner_id = :ownerId
  AND status = 'COMPLETED'
  AND deleted = false
  AND completed_at IS NOT NULL
```
Результат в часах. В сервисе округляется: `Math.round(avgHours * 100.0) / 100.0`. Если таких задач нет — 0.

### Самая быстрая задача
```sql
SELECT * FROM tasks
WHERE owner_id = :ownerId
  AND status = 'COMPLETED'
  AND deleted = false
  AND completed_at IS NOT NULL
ORDER BY (completed_at - created_at) ASC
LIMIT 1
```
Не загружает все задачи в Java. Если таких задач нет — `null`.

### Процент выполнено в срок
```
onTimePercentage = (COUNT задач со статусом COMPLETED, deleted = false, completed_at IS NOT NULL, deadline IS NOT NULL, completed_at <= deadline, owner_id = текущий пользователь)
                   / (COUNT задач со статусом COMPLETED, deleted = false, completed_at IS NOT NULL, deadline IS NOT NULL, owner_id = текущий пользователь) * 100
```
Если знаменатель = 0 — процент = 0. Округление: `Math.round((double) onTimeCount / withDeadlineCount * 100.0 * 100.0) / 100.0`.

### Завершённые за неделю
Задачи с `completed_at` в полуоткрытом интервале `[начало текущего понедельника 00:00 Europe/Moscow, начало следующего понедельника 00:00 Europe/Moscow)`.

### Завершённые за месяц
Задачи с `completed_at` в полуоткрытом интервале `[1-е число текущего месяца 00:00 Europe/Moscow, 1-е число следующего месяца 00:00 Europe/Moscow)`.

## Обработка существующих задач с completed_at = NULL

После миграции V5 задачи со статусом `COMPLETED`, созданные до добавления поля, будут иметь `completed_at = NULL`:
- **totalCompleted** — учитываются (статус COMPLETED)
- **averageCompletionHours** — исключаются (нет completed_at)
- **fastestCompletedTask** — исключаются (нет completed_at)
- **completedThisWeek / completedThisMonth** — исключаются (нет completed_at для определения периода)
- **onTimePercentage** — исключаются из числителя и знаменателя (нет completed_at и/или deadline для сравнения)

## Персональность статистики

Статистика считается только для задач, где `owner_id = id текущего пользователя`. Задачи, доступные пользователю через членство в команде (Team), но не им созданные, не включаются в статистику.

## Влияние @CreationTimestamp на тестирование

Поле `createdAt` помечено `@CreationTimestamp` и `@Column(updatable = false)`. Hibernate устанавливает его автоматически при вставке. В тестах используется `task.setCreatedAt(...)` для переопределения значения — это требует обязательной проверки на следующем этапе (не меняя production-код), так как поведение `@CreationTimestamp` при прямом вызове сеттера в тестах может зависеть от конфигурации Hibernate и порядка валидации. При реальном использовании через контроллер `createdAt` устанавливается Hibernate и не может быть переопределен пользователем.

## Критерии приёмки (AC-1–AC-12)

### AC-1. Персональная статистика
- **Given:** пользователь A имеет свои задачи, пользователь B — свои
- **When:** пользователь A открывает `/`
- **Then:** статистика отображается только для задач пользователя A (фильтрация по `ownerId`)

### AC-2. Общее количество завершённых задач
- **Given:** у пользователя есть задачи со статусом `COMPLETED` и другие статусы
- **When:** открыта главная страница `/`
- **Then:** отображается корректное количество задач со статусом `COMPLETED` и `deleted = false` для этого пользователя

### AC-3. Среднее время выполнения
- **Given:** есть завершённые задачи с заполненным `completedAt`
- **When:** открыта главная страница `/`
- **Then:** отображается среднее время выполнения в часах (с точностью до 2 знаков после запятой)

### AC-4. Самая быстро выполненная задача
- **Given:** есть завершённые задачи с разным временем выполнения
- **When:** открыта главная страница `/`
- **Then:** отображается задача с минимальным `completedAt - createdAt` (или сообщение, если задач нет)

### AC-5. Завершённые за неделю и месяц
- **Given:** есть задачи, завершённые в текущую неделю и месяц
- **When:** открыта главная страница `/`
- **Then:** отображается количество завершённых задач за текущую неделю (пн–вс, полуоткрытый интервал) и календарный месяц (полуоткрытый интервал)

### AC-6. Процент выполнено в срок
- **Given:** есть задачи со статусом `COMPLETED`, некоторые завершены до дедлайна, некоторые после
- **When:** открыта главная страница `/`
- **Then:** отображается процент задач, где `completedAt <= deadline` (от 0 до 100, 0 если нет подходящих задач)

### AC-7. Задачи без завершения не влияют на статистику
- **Given:** есть задачи со статусом не `COMPLETED`
- **When:** открыта главная страница `/`
- **Then:** они не учитываются в подсчёте завершённых, среднем времени, fastest task

### AC-8. Мягко удалённые задачи не учитываются
- **Given:** есть мягко удалённые задачи (`deleted = true`)
- **When:** открыта главная страница `/`
- **Then:** они не учитываются ни в одной метрике

### AC-9. Переход статуса COMPLETED → не-COMPLETED очищает completedAt
- **Given:** задача со статусом `COMPLETED` и заполненным `completedAt`
- **When:** статус изменён на `IN_PROGRESS` (или другой не-COMPLETED)
- **Then:** `completedAt` становится `null`

### AC-10. Переход статуса не-COMPLETED → COMPLETED заполняет completedAt
- **Given:** задача со статусом `IN_PROGRESS` и `completedAt = null`
- **When:** статус изменён на `COMPLETED`
- **Then:** `completedAt` устанавливается в текущее время

### AC-11. Повторное редактирование завершённой задачи не меняет completedAt
- **Given:** задача со статусом `COMPLETED` и заполненным `completedAt`
- **When:** задача редактируется (меняется название, приоритет и т.д.), статус остаётся `COMPLETED`
- **Then:** `completedAt` не меняется

### AC-12. Тесты
- **Given:** реализован этап 2
- **Then:** написаны тесты для `TaskRepository`, `TaskService`, `MainController`; команда `./mvnw test` проходит успешно

## Дополнительные критерии приёмки для deadline

### AC-13. Создание задачи с будущим дедлайном
- **Given:** пользователь создаёт задачу с дедлайном в будущем
- **When:** задача сохраняется
- **Then:** задача создаётся успешно, дедлайн сохраняется

### AC-14. Создание задачи без дедлайна
- **Given:** пользователь создаёт задачу без дедлайна (`deadline = null`)
- **When:** задача сохраняется
- **Then:** задача создаётся успешно, `deadline` остаётся `null`

### AC-15. Запрет нового или изменённого дедлайна в прошлом
- **Given:** пользователь создаёт задачу или редактирует существующую, устанавливая дедлайн в прошлом (отличный от сохранённого значения)
- **When:** задача сохраняется
- **Then:** ошибка валидации, задача не сохраняется. Если дедлайн совпадает с сохранённым значением (даже если он уже просрочен), задача сохраняется успешно

### AC-16. Редактирование просроченной задачи без изменения дедлайна
- **Given:** задача с просроченным дедлайном (в прошлом)
- **When:** пользователь редактирует задачу, не меняя дедлайн
- **Then:** задача сохраняется успешно, старый просроченный дедлайн остаётся

### AC-17. Очистка существующего дедлайна при редактировании
- **Given:** задача с заполненным дедлайном
- **When:** пользователь редактирует задачу, устанавливая `deadline = null`
- **Then:** задача сохраняется успешно, `deadline` становится `null`

## Граничные случаи для тестирования

1. Нет задач вообще — статистика показывает нули, fastest task = null
2. Нет завершённых задач — среднее время = 0, fastest task = null, процент в срок = 0
3. Одна завершённая задача — среднее время = время этой задачи, fastest task = эта задача
4. Задача завершена точно в дедлайн — считается выполненной в срок
5. Задача завершена на 1 секунду после дедлайна — не считается выполненной в срок
6. Мягко удалённые задачи (`deleted = true`) — не учитываются в статистике
7. Задачи с `completedAt = NULL` (после миграции) — исключаются из временных метрик
8. Знаменатель процента в срок = 0 — процент = 0
9. Повторное редактирование завершённой задачи — completedAt не меняется
10. Переход COMPLETED → IN_PROGRESS — completedAt очищается
11. Переход IN_PROGRESS → COMPLETED — completedAt заполняется

## Последовательность реализации (уже выполнена)

1. ✅ Написать тесты (репозиторий, сервис, контроллер)
2. ✅ Создать Flyway-миграцию `V5__add_completed_at_to_tasks.sql` с полем `completed_at`
3. ✅ Изменить `Task` — добавить поле `completedAt` и индекс
4. ✅ Изменить `TaskRepository` — добавить методы запросов с фильтрацией по `ownerId`, включая `@Query` с `nativeQuery = true`
5. ✅ Изменить `TaskService` — добавить расчёт статистики, логику `completedAt` в `createTask` и `updateTask`
6. ✅ Создать `TaskStatisticsDto`
7. ✅ Изменить `MainController` — передать статистику в модель
8. ✅ Обновить `home.html` — отобразить статистику
9. ✅ Запустить `./mvnw test`, исправить ошибки
10. ⏳ Коммиты разделить: `test: ...` и `feat: ...` (не выполнено, требует разрешения)

## Изменения в спецификации (по сравнению с предыдущей версией)

1. Удалены открытые вопросы — все разрешены пользователем.
2. Раздел "Правила deadline" полностью переработан с окончательными правилами (8 пунктов).
3. Добавлены критерии приёмки AC-13…AC-16 для deadline (создание с будущим дедлайном, без дедлайна, запрет прошлого, редактирование просроченной без изменения).
4. Зафиксировано, что nullable-изменение БД для `deadline` выполняется новой миграцией `V6__make_deadline_nullable.sql` только после проверки текущей схемы.
5. Подтверждено: `@CreationTimestamp` на `createdAt` оставляется без изменений.
6. Подтверждено: правила `completedAt` оставлены без изменений.
7. Удален раздел "Найденные противоречия" и "Вопросы, требующие решения" — все закрыто.
