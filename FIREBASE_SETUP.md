# Подключение Firebase (чат) — запуск на ЭМУЛЯТОРЕ

Этот документ описывает, что нужно сделать руками, чтобы заработал **чат**
между пользователями (как в Telegram) на Firebase Firestore.

Код уже подготовлен. Здесь — только ручные шаги (создание проекта Firebase и ключи).

---

## Часть 1. Адрес бэкенда (для эмулятора менять НЕ надо)

В `RetrofitClient.kt` адрес `http://10.0.2.2:8080/` — это **правильный адрес для эмулятора**.
`10.0.2.2` — специальный алиас внутри эмулятора, который указывает на `localhost`
твоего ПК, где запущен Spring-бэкенд. Ничего менять не нужно — запускай как обычно.

> На будущее: если когда-нибудь запустишь на реальном телефоне — подставь в BASE_URL
> локальный IP ПК (например 192.168.1.42), добавь на бэкенде `server.address=0.0.0.0`
> и открой порт 8080. Для эмулятора это не требуется.

### ВАЖНО для Firebase на эмуляторе
Firebase требует Google Play Services. Эмулятор должен быть с образом, где есть **Google Play**:
- в Android Studio → Device Manager → Create Device,
- при выборе системного образа бери строку с иконкой **Play Store** в столбце "Play Store"
  (например "Pixel 7 / API 34 (Google Play)"), а не просто (Google APIs).
- без Google Play вход в Firebase Auth / Firestore может не работать.

---

## Часть 2. Создание проекта Firebase

1. Зайди на https://console.firebase.google.com и создай проект (например `kursach-mobilka`).
2. В проекте включи:
   - **Build → Firestore Database** → Create database → начни в режиме *production*
     (правила мы зальём свои, см. ниже).
   - **Build → Authentication → Sign-in method** → включи провайдер **Anonymous**
     НЕ нужен; нам нужен вход по **Custom token** — он работает без отдельного включения
     провайдера, достаточно service account (см. часть 3).

### 2.1 Android-приложение в Firebase
1. В проекте Firebase нажми «Добавить приложение» → Android.
2. Укажи **package name**: `com.example.demoapp` (точно как в `applicationId`).
3. Скачай файл **`google-services.json`**.
4. Положи его в папку:
   ```
   app/google-services.json
   ```
   (рядом с `app/build.gradle.kts`). Плагин google-services уже подключён в проекте.

### 2.2 Правила Firestore
1. Firebase Console → Firestore Database → вкладка **Rules**.
2. Вставь содержимое файла `firestore.rules` из этого репозитория и нажми **Publish**.

---

## Часть 3. Сервисный ключ для бэкенда (Firebase Admin SDK)

Бэкенд подписывает Firebase **custom token**, чтобы uid в Firebase совпадал с id
пользователя из нашей БД. Для этого нужен сервисный ключ.

1. Firebase Console → ⚙️ **Project settings** → вкладка **Service accounts**.
2. Нажми **Generate new private key** → скачается JSON-файл.
3. Переименуй его в `firebase-service-account.json` и положи в бэкенд:
   ```
   src/main/resources/firebase-service-account.json
   ```
4. ВАЖНО: этот файл — **секрет**. Он уже добавлен в `.gitignore`, его НЕЛЬЗЯ коммитить.

При старте бэкенда в логах должно появиться:
`Firebase Admin SDK успешно инициализирован`.
Если ключа нет — увидишь предупреждение, и выдача токена работать не будет.

---

## Часть 4. Как всё работает вместе

1. Пользователь логинится на нашем Spring-бэкенде → получает JWT (как и раньше).
2. При входе в раздел «Чаты» приложение запрашивает у бэкенда Firebase custom token:
   `GET /firebase/token` (с нашим JWT в заголовке).
3. Бэкенд через Admin SDK выдаёт токен с `uid = "uid_<id>"`.
4. Приложение вызывает `signInWithCustomToken(...)` → входит в Firebase Auth.
5. Дальше чат полностью на Firestore:
   - `chats/{chatId}` — документ диалога (участники, последнее сообщение),
   - `chats/{chatId}/messages/{id}` — сообщения, обновляются в реальном времени.
6. Чтобы начать переписку: открой профиль пользователя → кнопка **«Написать сообщение»**.
   Приложение спросит у бэкенда uid собеседника (`GET /profile/lookup/{email}`),
   создаст диалог и откроет переписку.

---

## Чек-лист
- [ ] BASE_URL заменён на IP ПК, на бэкенде `server.address=0.0.0.0`, порт 8080 открыт
- [ ] `app/google-services.json` добавлен
- [ ] `src/main/resources/firebase-service-account.json` добавлен в бэкенд (НЕ коммитить)
- [ ] Правила `firestore.rules` опубликованы в Firebase Console
- [ ] Бэкенд в логах пишет «Firebase Admin SDK успешно инициализирован»
