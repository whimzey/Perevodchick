package com.example.me.perevodchick;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private String afterTranslationString;
    private String toTranslateString;
    private Spinner fromlang;
    private Spinner tolang;
    private EditText toTranslateEditText;
    private TextView afterTranslationTextView;
    private Button setToFavorite;
    private Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Realm.init(this);//база данных, в которой храним данные истории и избранного

        realm = Realm.getDefaultInstance();

        fromlang = (Spinner) findViewById(R.id.fromlang);
        tolang = (Spinner) findViewById(R.id.tolang);
        setToFavorite = (Button) findViewById(R.id.addtofavoritebutton);
        toTranslateEditText = (EditText) findViewById(R.id.editText);
        afterTranslationTextView = (TextView) findViewById(R.id.textView);

        try {
            //в теле адаптера вызываем метод, который возвращает список языков
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, new ArrayList<>(Translator.getLang().keySet())) {

                public View getView(int position, View convertView, ViewGroup parent) {
                    //настраиваем стиль спиннера
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.cardview_dark_background));
                    ((TextView) v).setTextSize(18);
                    ((TextView) v).setGravity(Gravity.CENTER);
                    return v;
                }

            };

            adapter.setDropDownViewResource(R.layout.my_spinner_style);

            //присваиваем выпадающим спискам адаптер со списком языков
            fromlang.setAdapter(adapter);
            tolang.setAdapter(adapter);


        } catch (Exception e) {
            //ловим ошибку отсутствия интернета, которую пробрасываем в Translator, и выводим ее на экран
            //приложение при этом не закрывается, просто ничего не происходит
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Кажется, у вас закончился интернет :(\nПожалуйста, включите его, и перезапустите приложение", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }
        //нижнее навигационное меню
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //обработчик кнопки добавления в избранное
    public void setToFavorite(View view) {

        //если что-то переведено, передаем данные методу, который добавляет их в базу данных
        if (!"".equals(toTranslateString)) {
            //передаем строки "на перевод" и "переведено", а также объект БД
            Translator.setToFavorite(toTranslateString, afterTranslationString, realm);
            //меняем значок избранного на только что проведенном переводе
            setToFavorite.setBackgroundResource(R.drawable.ic_star_black_24dp);
        }

    }

    //обработчик кнопки получения перевода
    public void getTranslation(View view) {
        //меняем значок избранного, так как получаем уже новый перевод
        if (!"".equals(afterTranslationString)) {
            setToFavorite.setBackgroundResource(R.drawable.ic_star_border_black_24dp);
        }
        //забираем строку
        toTranslateString = toTranslateEditText.getText().toString();
        //проверяем на пустое поле перевода
        if (!"".equals(toTranslateString)) {
            try {

                //передаем языки и текст для перевода методу, который возвращает перевод
                afterTranslationString = Translator.getTranslate(
                        fromlang.getSelectedItem().toString(), tolang.getSelectedItem().toString(), toTranslateString, realm);
                //устанавливаем перевод в поле вывода
                afterTranslationTextView.setText(afterTranslationString);

            } catch (Exception e) {
                //если интернет закончился во время процесса перевода, выдаем всплывающее окно об ошибке
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Кажется, у вас закончился интернет :(\nПожалуйста, включите его, и перезапустите приложение", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }


    //здесь указываем, куда бежать навигационному меню при нажатии на ту или иную кнопку
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //остаемся дома
                    return true;
                case R.id.navigation_history:
                    //переходим на вкладку истории
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                    //добавляем плавную анимацию
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;
                case R.id.navigation_favorite:
                    //переходим на вкладку избранного
                    startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;
            }
            return false;
        }

    };

    //инвертировать направление перевода
    public void invertLang(View view) {

        int fromlanfitempos = fromlang.getSelectedItemPosition();
        fromlang.setSelection(tolang.getSelectedItemPosition());
        tolang.setSelection(fromlanfitempos);

    }
    //EXTERMINATE!EXTERMINATE!
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


}
