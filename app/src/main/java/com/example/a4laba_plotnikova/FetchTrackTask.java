package com.example.a4laba_plotnikova;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;

public class FetchTrackTask extends AsyncTask<Void, Void, String> {
    private DatabaseHelper dbHelper;
    private TextView statusTextView;

    public FetchTrackTask(DatabaseHelper dbHelper, TextView statusTextView) {
        this.dbHelper = dbHelper;
        this.statusTextView = statusTextView;
    }

    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder resultBuilder = new StringBuilder();

        try {
            // Отключаем проверку SSL-сертификатов
            disableSSLVerification();

            // Подключаемся к сайту
            Document doc = Jsoup.connect("https://www.loveradio.ru/player/history")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36")
                    .timeout(10000) // Устанавливаем таймаут в 10 секунд
                    .get();

            // Получаем элементы с треками
            Elements trackElements = doc.select("li[data-v-f7ae7722]");

            if (trackElements.isEmpty()) {
                return "Нет треков для парсинга.";
            }

            // Проходим по каждому элементу и извлекаем информацию
            for (Element trackElement : trackElements) {
                String artist = trackElement.select(".playlist-item-card__artist-song").text();
                String title = trackElement.select(".playlist-item-card__artist-song").text();
                String timestamp = trackElement.select(".playlist-item-card__artist-time").text();

                // Проверяем, что все данные присутствуют
                if (!artist.isEmpty() && !title.isEmpty() && !timestamp.isEmpty()) {
                    // Вставляем в базу данных
                    dbHelper.insertTrack(artist, title, timestamp);
                    resultBuilder.append(artist).append(" - ").append(title).append(" (").append(timestamp).append(")\n");
                }
            }

            return resultBuilder.toString();

        } catch (IOException e) {
            // Логирование ошибки подключения
            Log.e("FetchTrackTask", "Ошибка подключения: " + e.getMessage());
            return "Ошибка подключения: " + e.getMessage();
        } catch (Exception e) {
            // Логирование прочих ошибок
            Log.e("FetchTrackTask", "Ошибка обработки данных: " + e.getMessage());
            return "Ошибка обработки данных: " + e.getMessage();
        }
    }

    // Метод для отключения проверки SSL-сертификатов
    private void disableSSLVerification() {
        try {
            // Создаем менеджер для доверенных сертификатов
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Инициализируем SSLContext с доверенным менеджером
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            Log.e("FetchTrackTask", "Ошибка при отключении SSL-проверки: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            statusTextView.setText("Треки из истории:\n" + result);
        } else {
            statusTextView.setText("Не удалось получить данные о треках.");
        }
    }
}