package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 13; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !
    static final String DATABASE_NAME = "software"; // Имя базы данных

    static final String TABLE_NAME = "software_types"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием ПО
    static final String NAME_LC = "name_lc"; // // Поле с наименованием ПО в нижнем регистре
    static final String TYPE = "type"; // Поле с типом ПО
    static final String FUNCTION = "function"; // Поле с функцией ПО
    static final String PRICE = "price";
    static final String RELEASE_DATE = "release_date"; // Поле с датой выпуска ПО
     // Поле с последней версией ПО

    static final String ASSETS_FILE_NAME = "software.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SOFTWARE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + NAME_LC + " TEXT,"
                + TYPE + " TEXT,"
                + FUNCTION + " TEXT,"
                + PRICE + " TEXT,"
                + RELEASE_DATE + " TEXT" + ")";
        db.execSQL(CREATE_SOFTWARE_TABLE);
        System.out.println(CREATE_SOFTWARE_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String type, String function, String releaseDate, String lastVersion) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(NAME_LC, name.toLowerCase());
        values.put(TYPE, type);
        values.put(FUNCTION, function);
        values.put(PRICE, releaseDate);
        values.put(RELEASE_DATE, lastVersion);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    System.out.println(st.toString());
                    String name = st.nextToken().trim(); // Извлекаем из строки название ПО без пробелов на концах
                    String type = st.nextToken().trim(); // Извлекаем из строки тип ПО без пробелов на концах
                    String function = st.nextToken().trim();
                    String  price = st.nextToken().trim();
                    String releaseDate = st.nextToken().trim();
                    addData(db, name, type, function, price, releaseDate); // Добавляем название и телефон в базу данных
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter) {

        String selectQuery; // Переменная для SQL-запроса

        if (filter.equals("")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME;
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'" +
                    " OR " + TYPE + " LIKE '%" + filter + "%'" +
                    " OR " + FUNCTION + " LIKE '%" + filter + "%'" +
                    " OR " +  PRICE + " LIKE '%" + filter + "%'" +
                    " OR " + RELEASE_DATE + " LIKE '%" + filter + "%'" + ") ORDER BY " + NAME;
        }
        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int a = cursor.getColumnIndex(NAME);
                int b = cursor.getColumnIndex(TYPE);
                int c = cursor.getColumnIndex(FUNCTION);
                int d = cursor.getColumnIndex(PRICE);
                int e = cursor.getColumnIndex(RELEASE_DATE);

                String name = cursor.getString(a); // Чтение названия организации
                String type = cursor.getString(b);
                String function = cursor.getString(c);
                String price = cursor.getString(d);
                String releaseDate = cursor.getString(e);

                data.append(String.valueOf(++num) + ") " + name + ": " + type + ", "
                        + function  + ", " +  price + ", " + releaseDate + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}