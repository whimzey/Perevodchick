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


public class FavoriteActivity extends AppCompatActivity {
    private  Realm realm;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_list);

        //наша база
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        List<Favorite> values = realm.copyFromRealm(realm.where(Favorite.class).findAll());
        Collections.reverse(values);//выводим последний добавленный первым в списке

        //если список  пуст, кнопка "очистить избранное" скрывается
        if (values.size() == 0) {
            (findViewById(R.id.clearfavbutton)).setVisibility(View.INVISIBLE);
        }


        ListView listView = (ListView) findViewById(R.id.listoffavourite);
        listView.setAdapter(new ListOfFavoriteAdapter(this, values));

        //назигационное меню
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
//кнопка "очистить избранное"

    public void clearFavorite(View view) {

        //находим все объекты избранного
        final RealmResults<Favorite> results = realm.where(Favorite.class).findAll();

        //выполнем транзакцию по удалению избранного
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

    //навигационное меню
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;

                case R.id.navigation_history:
                    startActivity(new Intent(FavoriteActivity.this, HistoryActivity.class));
                    overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    return true;

                case R.id.navigation_favorite:
                    return true;
            }
            return false;
        }

    };
}


