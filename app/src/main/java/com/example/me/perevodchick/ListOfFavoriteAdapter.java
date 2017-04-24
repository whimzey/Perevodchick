package com.example.me.perevodchick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.List;


//адаптер для отображения избранного
class ListOfFavoriteAdapter extends ArrayAdapter<String> {
    private final List<Favorite> data;
    private final Context context;

     ListOfFavoriteAdapter(Context context, List<Favorite> data) {
        super(context, R.layout.list_view);
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        // возвращаем количество элементов списка
        return data.size();
    }

    @Override
    public String getItem(int position) {
        // получение одного элемента по индексу
        return data.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // заполнение элементов списка
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // задаем вид элемента списка, который мы создали выше
         view = inflater.inflate(R.layout.list_view, parent, false);

        // проставляем данные для элементов
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView translation = (TextView) view.findViewById(R.id.translation);
        TextView lang = (TextView) view.findViewById(R.id.lang);


        // получаем элемент списка
        Favorite objectItem = data.get(position);

        // устанавливаем значения компонентам одного элемента списка
        title.setText(objectItem.getName());
        translation.setText(objectItem.getValue());
        lang.setText(objectItem.getLang());

        } else {
            view = convertView;
        }

        return view;
    }
}
