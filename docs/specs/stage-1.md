# Этап 1. Создание команды и просмотр команды

## Цель этапа
Пользователь может создать команду, при этом владелец автоматически становится её участником, а также просматривать список своих команд и детали конкретной команды. Этап даёт работающий кусок: сущность Team, репозиторий, сервис, контроллер, тесты.

## Область изменений
- Новая сущность не требуется: `com.site.webapp.models.Team` уже существует.
- Нужны новый репозиторий, сервис, контроллер, DTO, исключения и новая Flyway-миграция для таблиц `teams` и `team_members`.
- Существующий код не рефакторится.
- Перед реализацией необходимо проверить существующие сигнатуры `User`, `CustomUserDetails`, `TaskController`, `LoggingController` и `GlobalExceptionHandler`, чтобы не предполагать наличие методов, которых нет в проекте.

## 1. Классы

### 1.1. `com.site.webapp.models.Team` (существует)
Сущность команды. Поля:
- `Long id` — идентификатор (`@Id`, `@GeneratedValue(IDENTITY)`)
- `String name` — название (`@NotBlank`, `@Size(min=2, max=100)`, `nullable = false`)
- `Long ownerId` — владелец команды (`nullable = false`, `updatable = false`)
- `Set<User> members` — участники (`@ManyToMany`, таблица `team_members`, `FetchType.LAZY`)
- `LocalDateTime createdAt` — дата создания (`@CreationTimestamp`, `updatable = false`)
- `Long version` — `@Version` для оптимистичной блокировки

Методы: `addMember(User)`, `removeMember(User)`, `isMember(Long userId)`, `equals`/`hashCode` по `id`, `toString`.

### 1.2. `com.site.webapp.repo.TeamRepository` (новый)
`public interface TeamRepository extends JpaRepository<Team, Long>`

Методы:
- `List<Team> findByOwnerId(Long ownerId)` — команды, где пользователь владелец
- `List<Team> findByMembersContaining(User user)` — команды, где пользователь участник
- `Optional<Team> findByIdAndOwnerId(Long id, Long ownerId)` — команда по ID и владельцу
- `boolean existsByName(String name)` — проверка уникальности названия (только если бизнес-требование подтверждено, см. раздел 6)

### 1.3. `com.site.webapp.service.TeamService` (новый)
`@Service`, `@Transactional`

Методы:
- `Team createTeam(Team team, CustomUserDetails userDetails)` — создаёт команду, устанавливает `ownerId` = `userDetails.getId()`, добавляет владельца в `members`, сохраняет
- `Team getTeamById(Long id)` — возвращает команду по ID с загруженными `members` (через `EntityGraph` или `JOIN FETCH`), иначе `TeamNotFoundException`
- `List<Team> getUserTeams(Long userId)` — команды, где пользователь участник
- `boolean isMember(Long teamId, Long userId)` — проверка участия пользователя в команде

### 1.4. `com.site.webapp.controllers.TeamController` (новый)
`@Controller`, наследуется от `LoggingController`. Стиль MVC + Thymeleaf (как `TaskController`, `RegistrationController`).

### 1.5. `com.site.webapp.dto.TeamCreateDto` (новый)
DTO для формы создания команды:
- `String name` — `@NotBlank(message = "Название команды обязательно")`, `@Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")`
- Геттеры, сеттеры, конструкторы пишутся вручную (без Lombok)

### 1.6. Исключения (новые)
- `com.site.webapp.exception.team.TeamNotFoundException` — команда не найдена. Должен возвращать HTTP 404.
- `com.site.webapp.exception.team.TeamAccessDeniedException` — пользователь не является участником команды. Должен возвращать HTTP 403.

### 1.7. База данных (Flyway)
Новая миграция (не редактировать старые), версия определяется после проверки существующих миграций в `src/main/resources/db/migration/` (например, `V3__create_teams_table.sql`, если V3 свободен):
- таблица `teams` (id, name, owner_id, created_at, version)
- внешний ключ `teams.owner_id` → `users.id` (гарантирует существование владельца)
- таблица `team_members` (team_id, user_id, внешние ключи на `teams(id)` и `users(id)`, первичный ключ `(team_id, user_id)`)
- индексы по `owner_id`, `team_id`, `user_id`
- старые миграции не изменяются

## 2. Эндпоинты

Приложение использует серверную отрисовку (Thymeleaf). Контроллер возвращает имена представлений, при ошибках валидации форма показывается повторно с сообщениями, при успешном действии — редирект (302).

| # | Метод | Путь | Описание | Параметры / тело | Успех | Ошибка |
|---|-------|------|----------|------------------|-------|--------|
| 1 | `GET` | `/teams` | Список команд текущего пользователя | `@AuthenticationPrincipal CustomUserDetails currentUser` | 200, представление `teams` | — |
| 2 | `GET` | `/teams/new` | Форма создания команды | — | 200, представление `team-create` с `teamForm` | — |
| 3 | `POST` | `/teams` | Создать команду | `@Valid @ModelAttribute("teamForm") TeamCreateDto`, `BindingResult`, `currentUser` | 302 редирект на `/teams/{id}` | 200, представление `team-create` с ошибками валидации |
| 4 | `GET` | `/teams/{id}` | Детали команды | `@PathVariable Long id`, `currentUser` | 200, представление `team-details` с командой и участниками | 404 если команда не найдена; 403 если текущий пользователь не участник |

## 3. Коды ответов

| Код | Значение | Когда возвращается |
|-----|----------|-------------------|
| 200 OK | Страница отображена | `GET /teams`, `GET /teams/new`, `GET /teams/{id}`, `POST /teams` с ошибками валидации |
| 302 Found | Редирект | Успешное создание команды (`POST /teams`) |
| 403 Forbidden | Доступ запрещён | Попытка просмотра команды, где пользователь не участник (`TeamAccessDeniedException`) |
| 404 Not Found | Не найдено | Команда с указанным `id` не существует (`TeamNotFoundException`) |

HTTP 400 не используется: ошибки валидации формы `TeamCreateDto` обрабатываются контроллером и возвращают 200 с повторной отрисовкой формы.

Обработка исключений:
- `TeamNotFoundException` → HTTP 404 (через `@ResponseStatus(HttpStatus.NOT_FOUND)` на классе исключения или через `@ExceptionHandler` в `GlobalExceptionHandler`, возвращающий статус 404 без редиректа)
- `TeamAccessDeniedException` → HTTP 403 (через `@ResponseStatus(HttpStatus.FORBIDDEN)` на классе исключения или через `@ExceptionHandler` в `GlobalExceptionHandler`, возвращающий статус 403 без редиректа)
- Существующая обработка `EntityNotFoundException`, `BusinessRuleViolationException`, `EntityAlreadyExistsException`, `ObjectOptimisticLockingFailureException`, `FileStorageException`, `EmailSendException` и общего `Exception` в `GlobalExceptionHandler` сохраняется без изменений.

## 4. Загрузка связанных сущностей (LazyInitializationException)

Поле `members` у `Team` имеет `FetchType.LAZY`. Для страницы `team-details` участники должны быть доступны в Thymeleaf после завершения транзакции сервиса.

Решение:
- В `TeamRepository` добавить метод с `@EntityGraph(attributePaths = "members")` или `JOIN FETCH members` для загрузки участников вместе с командой.
- Метод `getTeamById` в `TeamService` использует этот метод репозитория, чтобы `members` был инициализирован до возврата в контроллер.
- Альтернативно — загрузить `members` явно в сервисе перед возвратом результата.

## 5. Получение ID текущего пользователя

Класс `com.site.webapp.security.CustomUserDetails` имеет метод `getId()` (возвращает `Long`), который задаётся из `user.getId()` в конструкторе `CustomUserDetails(User user)`.

В контроллере и сервисе ID текущего пользователя получается как `currentUser.getId()` из параметра `@AuthenticationPrincipal CustomUserDetails currentUser`.

## 6. Получение владельца команды

Владелец команды определяется по полю `ownerId` (тип `Long`). Для отображения на странице команды:
- Сервис или контроллер получает `User` владельца через `userRepository.findById(ownerId)`.
- `UserRepository` наследует `JpaRepository<User, Long>`, поэтому метод `findById(Long id)` доступен по умолчанию.
- Внешний ключ `teams.owner_id` → `users.id` гарантирует, что пользователь-владелец существует в таблице `users`.
- Не дублировать сущность `User` и не создавать ненужных полей в `Team` для хранения имени владельца.
- Поведение при отсутствии владельца не придумывается (внешний ключ исключает такую ситуацию на уровне БД).

## 7. Уникальность названий команд

UNIQUE-ограничение на `teams.name` не вводится без подтверждённого бизнес-требования. Решение: уникальность названий не гарантируется на уровне БД. Метод `existsByName` в `TeamRepository` оставлен как опциональный и может быть удалён, если бизнес-требование на уникальность не подтвердится.

## 8. Критерии приёмки

### AC-1. Создание команды авторизованным пользователем
- **Given:** авторизованный пользователь открыт на странице `/teams/new`
- **When:** вводит корректное название команды (от 2 до 100 символов) и отправляет форму
- **Then:** команда создаётся, возвращается редирект (302) на `/teams/{id}`, на странице отображается созданная команда

### AC-2. Владелец автоматически становится участником
- **Given:** пользователь создал команду
- **Then:** `ownerId` команды равен id создателя (`currentUser.getId()`), создатель входит в множество `members` команды

### AC-3. Валидация названия команды
- **Given:** пользователь отправляет форму с пустым названием, названием из 1 символа или более 100 символов
- **Then:** форма показывается повторно (HTTP 200), отображаются ошибки валидации с русскими сообщениями, команда не создаётся

### AC-4. Просмотр списка своих команд
- **Given:** пользователь является участником одной или нескольких команд
- **When:** открывает `/teams`
- **Then:** отображается список команд, где пользователь участник (HTTP 200)

### AC-5. Просмотр деталей команды участником
- **Given:** пользователь является участником команды с ID `X`
- **When:** открывает `/teams/X`
- **Then:** отображаются название команды, владелец, список участников (HTTP 200)

### AC-6. Запрет доступа не-участнику
- **Given:** пользователь не является участником команды с ID `X`
- **When:** открывает `/teams/X`
- **Then:** возвращается HTTP 403, отображается сообщение об ошибке

### AC-7. Команда не найдена
- **Given:** запрашивается несуществующий ID команды
- **When:** открывает `/teams/{id}`
- **Then:** возвращается HTTP 404, отображается сообщение "Команда не найдена"

### AC-8. Тесты
- **Given:** реализован этап 1
- **Then:** написаны тесты для `TeamRepository`, `TeamService`, `TeamController`; команда `./mvnw test` проходит успешно
- Тесты репозитория проверяют поиск команд по владельцу и участнику, а также загрузку `members` через `EntityGraph`/`JOIN FETCH`.
- Тесты сервиса проверяют автоматическое добавление владельца в `members` при создании команды.
- Тесты контроллера проверяют HTTP 200 для `GET /teams`, `GET /teams/new`, `GET /teams/{id}` (участник), `POST /teams` с ошибками валидации; HTTP 302 при успешном создании; HTTP 403 для не-участника; HTTP 404 для несуществующей команды.
- Тесты проверяют, что команда не создаётся при ошибках валидации формы.

## 9. Последовательность реализации (для следующего этапа кода)
1. Написать тесты (репозиторий, сервис, контроллер)
2. Создать Flyway-миграцию (версия определяется после проверки существующих миграций)
3. Создать `TeamRepository` (с `EntityGraph`/`JOIN FETCH` для `members`)
4. Создать `TeamService`
5. Создать `TeamCreateDto`
6. Создать `TeamController` и шаблоны Thymeleaf
7. Запустить `./mvnw test`, исправить ошибки
8. Коммиты разделить: `test: ...` и `feat: ...`

## Оставшиеся вопросы (требуют решения перед реализацией)

1. **Способ возврата HTTP 404/403:** Нужно решить, использовать ли `@ResponseStatus` на классах исключений `TeamNotFoundException` и `TeamAccessDeniedException`, или добавить новые `@ExceptionHandler` методы в существующий `GlobalExceptionHandler`. Оба варианта не ломают существующую обработку, но требуют выбора.

2. **Проверка сигнатур:** Перед реализацией необходимо подтвердить, что `CustomUserDetails.getId()` и `UserRepository.findById()` доступны в текущей версии кода (на момент анализа они есть, но могут быть изменены).
