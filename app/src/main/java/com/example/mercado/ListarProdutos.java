package com.example.mercado;

import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Objects;

public class ListarProdutos extends AppCompatActivity {
	SQLiteDatabase database;

	LinearLayout lista;

	final ColumnInfo[] columns = new ColumnInfo[]{
		new ColumnInfo("id", "ID"),
		new ColumnInfo("descricao", "Descrição"),
		new ColumnInfo("unidade", "Unidade"),
		new ColumnInfo("preco", "Preço")
	};

	final String keysQuery = String.join(", ", Arrays.stream(columns).map(columnInfo -> columnInfo.key).toArray(String[]::new));

	final int columnsSize = columns.length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_listar_produtos);

		lista = findViewById(R.id.lista);

		final SearchView searchInput = findViewById(R.id.searchInput);

		searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				if(newText.isEmpty()){
					renderList(null);
					return true;
				}

				renderList(String.format("%%%s%%", newText));
				return true;
			}
		});

		renderList(null);
	}
	private void renderList(final String searchQuery){
		if(lista.getChildCount() > 0){
			lista.removeAllViews();
		}

		final boolean hasSearch = !Objects.isNull(searchQuery);

		final Cursor cursor = database.rawQuery(
			"select " + keysQuery + " " +
			"from produto " +
			(hasSearch ? ("where produto.descricao like ? or produto.unidade like ? ") : "") +
			"order by id asc",
			hasSearch ? new String[]{ searchQuery, searchQuery } : null
		);

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
