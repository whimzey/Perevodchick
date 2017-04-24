package com.example.me.perevodchick;

import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import xdroid.toaster.Toaster;

//класс, в котором происходит обмен данными с Яндексом
class Translator {

    private static final String key = "trnsl.1.1.20170415T181448Z.a05fd52cf8c63077.794667fb803f21806c5026638b5cc0e5bfd17efc";
    private static Map<String, String> map;
    private static String lang;

    //метод загрузки языков
    static Map<String, String> getLang() {

        AsyncTask task = new AsyncTask<Object, Object, Map<String, String>>() {

            @Override
            protected void onPostExecute(Map<String, String> result) {

                super.onPostExecute(result);
            }

            @Override
            protected Map<String, String> doInBackground(Object... strings) {

                try {
                    HttpUrl.Builder urlBuilder = HttpUrl.parse("https://translate.yandex.net/api/v1.5/tr.json/getLangs").newBuilder();
                    urlBuilder.addQueryParameter("key", key);
                    urlBuilder.addQueryParameter("ui", "ru");

                    //формируем объект: методом getresponse получаем ответ от Яндекса, парсим его в объект json по ключевому слову langs
                    JsonObject obj = (JsonObject) (new JsonParser()).parse(getResponse(urlBuilder)).getAsJsonObject().get("langs");

                    Map<String, String> map = new TreeMap<>();
                    //создаем карту со значениями языков, при помощи парсинга объекта
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        map.put(entry.getValue().toString().replaceAll("\"", ""), entry.getKey());
                    }
                    return map;
                } catch (Exception e) {
                    return null;
                }
            }
        };
        task.execute();

        try {
            map = (Map<String, String>) task.get();
        } catch (InterruptedException | ExecutionException e) {
            Toaster.toastLong("Ой-ёй! Кажется, пропало подключение к интернету при вызове языков");
            task.cancel(true);
            return null;
        }
        return map;
    }
    //метод получения перевода
    static String getTranslate(String fromlang, String tolang, final String textToTranslate, Realm mainrealm) {

        //в метод передаем языки отдельно, а тут уже есть готовая карта соответствий, по которой формируем направление перевода
        lang = map.get(fromlang) + "-" + map.get(tolang);

        AsyncTask task = new AsyncTask<Object, Object, String>() {
            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);
            }

            @Override
            protected String doInBackground(Object... strings) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse("https://translate.yandex.net/api/v1.5/tr.json/translate").newBuilder();
                urlBuilder.addQueryParameter("key", key);
                urlBuilder.addQueryParameter("text", escapeString(textToTranslate));//экранируем спецсимволы
                urlBuilder.addQueryParameter("lang", lang);

                //забираем из ответа только контент text
                return ((JsonObject) new JsonParser().parse(getResponse(urlBuilder))).get("text").getAsString();
            }
        };
        task.execute();

        String translation;

        try {
            //убираем экранирование и возвращаем перевод в главный поток
            translation = StringEscapeUtils.unescapeJava(String.valueOf(task.get()));
            //отправляем перевод в историю
            setToHistory(textToTranslate, translation, lang, mainrealm);

        } catch (InterruptedException | ExecutionException e) {
            Toaster.toastLong("Ой-ёй! Кажется, пропало подключение к интернету при получении перевода");
            task.cancel(true);
            return null;
        }

        return translation;

    }

    //получаем ответ сервера в виде строки
    private static String getResponse(HttpUrl.Builder url) {
        String response = "";

        try {
            response = new OkHttpClient().newCall(new Request.Builder()
                    .url(url.build().toString())
                    .build()).execute().body().string();

        } catch (IOException e) {
            Toaster.toastLong("В приложении произошла ошибка, возможно, стоит его перезапустить");
        }

        return response;
    }

    //отправляем в историю
    private static void setToHistory(String from, String to, String lang, Realm realm) {

        //проверяем историю на содержание такого же перевода, если он есть, не повторяем его
        if ((realm.where(History.class).
                contains("name", from).findAll()).size() == 0) {

            realm.beginTransaction();
            History historyItem = realm.createObject(History.class);

            historyItem.setName(from);
            historyItem.setValue(to);
            historyItem.setLang(lang);

            realm.commitTransaction();

        }

    }
    //отправляем в избранное
    static void setToFavorite(String from, String to, Realm realm) {

        //проверяем избранное на дублирование элементов. повторно не добавляем
        if ((realm.where(Favorite.class).
                contains("name", from).findAll()).size() == 0) {
            realm.beginTransaction();

            Favorite favItem = realm.createObject(Favorite.class);

            favItem.setName(from);
            favItem.setValue(to);
            favItem.setLang(lang);

            realm.commitTransaction();
        }

    }

    //экранируем символы
    private static String escapeString(String toTranslateString) {
        //разбиваем строку
        char[] text = toTranslateString.toCharArray();
        StringBuilder answer = new StringBuilder();
        //проверяем посимвольно. если спецсимвол есть, он автоматически заэкранируется
        for (char aText : text) {
            if (Character.isLetter(aText)) {
                answer.append(aText);
            } else {
                answer.append(StringEscapeUtils.escapeJava(String.valueOf(aText)));
            }
        }
        return answer.toString();
    }
}