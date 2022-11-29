package kz.talipovsn.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int LARGE_FONT = 20; // Размер шрифта для режима крупного шрифта
    private final int SMALL_FONT = 16; // Размер шрифта для режима обычного шрифта
    private int fontSize = SMALL_FONT; // Выбранный размер шрифта

    MySQLite db = new MySQLite(this); // Класс работы с нашей базой данных

    EditText editText; // Компонент для задания строки поиска
    TextView textView; // Компонент для вывода ответа

    static final String FILTER = "FILTER"; // Имя параметра для сохранения при переворачивании экрана
    String filter = ""; // Фильтр поиска

    SharedPreferences sPref; // Класс для работы с настройками программы
    static final String CONFIG_FILE_NAME = "Config"; // Имя файла настроек приложения
    static final String FONT_SIZE = "FontSize"; // Имя параметра для сохранения размера шрифта в настройках приложения

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Сохранение данных при перевороте экрана
        savedInstanceState.putString(FILTER, filter);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Активация меню
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Доступ к компонентам
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

        textView.setKeyListener(null); // Запрет на изменение данных с клавиатуры

        // Чтение сохраненной настройки размера шрифта из параметров приложения
        sPref = getSharedPreferences(CONFIG_FILE_NAME, MODE_PRIVATE);
        fontSize = sPref.getInt(FONT_SIZE, SMALL_FONT);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // Установка начально размера шрифта
        textView.requestFocus(); // Передача фокуса на комонент чтобы закрылось окно ввода у "editText"

        // Восстановление фильтра после переворота экрана
        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(FILTER));
        }

        textView.setText(R.string.Загрузка_данных);

        // Обработчик изменения текста в компоненте "editText"
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Сделаем поиск данных в другом потоке
                new Thread(new Runnable() {
                    public void run() {
                        filter = editText.getText().toString().trim();
                        final String data = db.getData(filter);
                        // Сделаем вывод результата синхронно с основным потоком
                        textView.post(new Runnable() {
                            public void run() {
                                textView.setText(data);
                            }
                        });
                    }
                }).start();

            }

        });

        // Инициализация начального поиска (показать все записи)
        editText.post(new Runnable() {
            public void run() {
                editText.setText(filter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Установка правильного отображения пункта выбора крупного шрифта
        menu.findItem(R.id.large_font).setChecked(fontSize == LARGE_FONT);
        return true;
    }

    // Обработчик выбора пунктов меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Написать автору
        if (id == R.id.email) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.myemail)});
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Добавьте_еще_номер));
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.Предлагаю_такой_номер));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.Посылка_письма)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, R.string.Нет_установленного_почтового_клиента, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        // Установка/снятие крупного шрифта
        if (id == R.id.large_font) {
            item.setChecked(!item.isChecked());
            int size = item.isChecked() ? LARGE_FONT : SMALL_FONT;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            fontSize = size;
            return true;
        }
        // Выход
        if (id == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Метод при закрытии окна
    @Override
    protected void onStop() {
        super.onStop();
        // Сохранение размера шрифта в настройках программы
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(FONT_SIZE, fontSize);
        ed.apply();
    }
}
