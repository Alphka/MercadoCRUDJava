package com.example.mercado;

import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Objects;

public class ListarProdutos extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SQLiteDatabase database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_listar_produtos);

		final LinearLayout lista = findViewById(R.id.lista);

		final ColumnInfo[] columns = new ColumnInfo[]{
			new ColumnInfo("id", "ID"),
			new ColumnInfo("descricao", "Descrição"),
			new ColumnInfo("unidade", "Unidade"),
			new ColumnInfo("preco", "Preço")
		};

		final Cursor cursor = database.rawQuery(
			"select " + String.join(", ", Arrays.stream(columns).map(columnInfo -> columnInfo.key).toArray(String[]::new)) + " " +
			"from produto " +
			"order by id asc",
			null
		);

		final int columnsSize = columns.length;

		while(cursor.moveToNext()){
			final LinearLayout container = new LinearLayout(this);

			container.setOrientation(LinearLayout.VERTICAL);
			container.setPadding(0, 0, 0, 60);

			for(int index = 0; index < columnsSize; index++){
				final ColumnInfo columnInfo = columns[index];

				final LinearLayout rowContainer = new LinearLayout(this);

				rowContainer.setOrientation(LinearLayout.HORIZONTAL);
				rowContainer.setPadding(0, 0, 0, 4);

				final TextView columnTitle = new TextView(this),
				columnValue = new TextView(this);

				columnTitle.setTypeface(null, Typeface.BOLD);
				columnTitle.setText(String.format("%s: ", columnInfo.name));

				try{
					final String value = cursor.getString(index);

					if(Objects.isNull(value) || value.isEmpty()) continue;

					columnValue.setText(columnInfo.key.equals("preco")
						? formatPrice(cursor.getFloat(index))
						: getInputString(value)
					);
				}catch(final Exception error){
					throw new RuntimeException(error);
				}

				rowContainer.addView(columnTitle);
				rowContainer.addView(columnValue);
				container.addView(rowContainer);
			}

			lista.addView(container);
		}

		cursor.close();
	}
}
