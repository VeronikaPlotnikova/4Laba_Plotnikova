# Лабораторная работа №4. Взаимодействие с сервером
- _Выполнила:_ Плотниква Вероника
- _Язык программирования:_ Java

## Описание проекта
Это Android-приложение подключается к интернет-ресурсу, получает информацию о текущих музыкальных треках, отображает данные в интерфейсе и сохраняет их в локальную базу данных для последующего просмотра.

## Основные функции приложения
- Проверка подключения к интернету: При запуске приложение проверяет наличие интернет-соединения. Если оно недоступно, приложение работает в оффлайн-режиме, и пользователь получает уведомление.
- Асинхронный запрос данных: Если подключение доступно, приложение каждые 20 секунд запрашивает информацию о текущем музыкальном треке с сайта, используя библиотеку Jsoup.
- Сохранение данных в локальную базу: Полученные данные (исполнитель, название трека, время) сохраняются в базе данных.
- Отображение данных: Вся информация из базы данных выводится на экран в ListView, с обновлением через SimpleCursorAdapter.

## Основные компоненты проекта
MainActivity - Основной экран приложения
Этот класс выполняет проверку сети, запускает асинхронную задачу для получения данных и отображает список треков на экране.

``` java
// Проверка подключения к интернету
private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = 
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
}

// Загрузка данных из БД и отображение в ListView
private void loadTracksFromDatabase() {
    Cursor cursor = dbHelper.getAllTracks();
    String[] fromColumns = {DatabaseHelper.COLUMN_ARTIST, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_TIMESTAMP};
    int[] toViews = {R.id.artistTextView, R.id.titleTextView, R.id.timestampTextView};
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        this, R.layout.track_list_item, cursor, fromColumns, toViews, 0);
    trackListView.setAdapter(adapter);
}
```
FetchTrackTask - Асинхронная задача
Этот класс отвечает за выполнение запроса к серверу и обработку полученных данных.

Использование Jsoup для парсинга HTML страницы.
Сохранение данных в базу с проверкой уникальности.

``` java
// Асинхронный запрос на получение данных о треках
@Override
protected String doInBackground(Void... voids) {
    try {
        Document doc = Jsoup.connect("https://www.loveradio.ru/player/history")
                            .userAgent("Mozilla/5.0").get();
        Elements trackElements = doc.select("li[data-v-f7ae7722]");
        
        for (Element trackElement : trackElements) {
            String artist = trackElement.select(".playlist-item-card__artist-song").text();
            String title = trackElement.select(".playlist-item-card__artist-song").text();
            String timestamp = trackElement.select(".playlist-item-card__artist-time").text();

            if (!artist.isEmpty() && !title.isEmpty() && !timestamp.isEmpty()) {
                dbHelper.insertTrack(artist, title, timestamp); // Проверка уникальности
            }
        }
    } catch (IOException e) {
        return "Ошибка подключения: " + e.getMessage();
    }
}
```
Основные функции:
Проверка подключения к интернету:

``` java
private boolean isNetworkAvailable() {
    // проверка доступности сети
}
Запрос информации о треках с сервера каждые 20 секунд:

private Runnable fetchTrackRunnable = new Runnable() {
    @Override
    public void run() {
        new FetchTrackTask(dbHelper, statusTextView).execute();
        handler.postDelayed(this, 20000); // повторы каждые 20 секунд
    }
};
```
Отображение данных в UI: Данные из базы данных выводятся в ListView с помощью SimpleCursorAdapter.

