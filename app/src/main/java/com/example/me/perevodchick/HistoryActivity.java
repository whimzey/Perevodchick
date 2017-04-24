package com.example.me.perevodchick;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


public class HistoryActivity extends AppCompatActivity {
    private  Realm realm;

    List<History> values;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);
        //наша база
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        //копируем из базы список объектов истории переводов
        values = realm.copyFromRealm(realm.where(History.class).findAll());
        //делаем так, чтобы данные выводились в порядке "последний зашел-первый вышел"
        Collections.reverse(values);

        //если список истории пуст, кнопка "очистить историю" скрывается
        if (values.size() == 0) {
            (findViewById(R.id.clearhistorybutton)).setVisibility(View.INVISIBLE);
        }

        //инициализуем адаптер списком объектов истории
        ListOfHistoryAdapter  adapter = new ListOfHistoryAdapter(this, values);

        ((ListView) findViewById(R.id.listoftrans)).setAdapter(adapter);

        //навигационное меню
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    //навигационное меню
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    startActivity(new Intent(HistoryActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;
                case R.id.navigation_history:

                    return true;
                case R.id.navigation_favorite:
                    startActivity(new Intent(HistoryActivity.this, FavoriteActivity.class));
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;
            }
            return false;
        }

    };

    //обработчик кнопки "очистить историю"
    public void clearHistory(View view) {

        //находим все объекты истории
        final RealmResults<History> results = realm.where(History.class).findAll();

//выполнем транзакцию по удалению истории
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });

       //перезагружаем активити (знаю, можно красивее сделать, но не успела отладить красивый способ через адаптер)
        Intent i = new Intent( this , this.getClass() );
        finish();
        this.startActivity(i);
    }


}

