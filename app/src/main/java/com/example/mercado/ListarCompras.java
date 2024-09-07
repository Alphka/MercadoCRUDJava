package com.example.mercado;

import static com.example.mercado.Helpers.formatDate;
import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ListarCompras extends AppCompatActivity {
	SQLiteDatabase database;

	LinearLayout lista;

	final ColumnInfo[] columns = new ColumnInfo[]{
		new ColumnInfo("id", "ID"),
		new ColumnInfo("data", "Data da compra"),
		new ColumnInfo("valor", "Valor total da compra"),
		new ColumnInfo("data_pagamento", "Data final para o pagamento"),
		new ColumnInfo("cliente.nome", "Nome do cliente")
	};

	final String keysQuery = String.join(", ", Arrays.stream(columns)
		.map(columnInfo -> columnInfo.key.contains(".") ? columnInfo.key : String.format("compra.%s", columnInfo.key))
		.toArray(String[]::new)
	);

	final int columnsSize = columns.length,
	columIdIndex = Arrays.asList(Arrays.stream(columns)
		.map(columnInfo -> columnInfo.key)
		.toArray(String[]::new)
	).indexOf("id");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_listar_compras);

		lista = findViewById(R.id.lista);

		final SearchView searchInput = findViewById(R.id.searchInput);

		searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
			@Override
			public boolean onQueryTextSubmit(final String query){
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText){
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
	@SuppressLint("DefaultLocale")
	private void renderList(final String searchQuery){
		if(lista.getChildCount() > 0){
			lista.removeAllViews();
		}

		final boolean hasSearch = !Objects.isNull(searchQuery);

		final Cursor cursor = database.rawQuery(
			"select " + keysQuery + ", " +
			"case when compra.data_pagamento is null then true else false end as em_pendencia, " +
			"case when (" +
				"compra.data_pagamento is null and " +
				"date('now') > date(" +
					"strftime('%Y-%m-', compra.data) || printf('%02d', cliente.dia_pagamento)," +
					"'+1 month'" +
				")" +
			") then true else false end as em_debito " +
			"from compra " +
			"join cliente on cliente.id = compra.id_cliente " +
			(hasSearch ? (
				"left join compra_produto on compra.id = compra_produto.id_compra " +
				"left join produto on compra_produto.id_produto = produto.id " +
				"where cliente.nome like ? or produto.descricao like ? or produto.unidade like ? " +
				"group by compra.id "
			) : "") +
			"order by cliente.id asc",
			hasSearch ? new String[]{ searchQuery, searchQuery, searchQuery } : null
		);

		while(cursor.moveToNext()){
			final String compraId = cursor.getString(columIdIndex);

			final LinearLayout container = new LinearLayout(this);

			container.setOrientation(LinearLayout.VERTICAL);
			container.setPadding(0, 0, 0, 60);

			final boolean emDebito = cursor.getInt(columnsSize + 1) == 1,
			pagamentoPendente = emDebito || cursor.getInt(columnsSize) == 1;

			if(pagamentoPendente){
				final TextView debtTitle = new TextView(this);

				debtTitle.setText(emDebito ? "Em débito" : "Pendente");
				debtTitle.setTypeface(null, Typeface.BOLD);

				if(emDebito) debtTitle.setTextColor(Color.RED);

				container.addView(debtTitle);
			}

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

					columnValue.setText(columnInfo.key.equals("valor")
						? formatPrice(cursor.getFloat(index))
							: columnInfo.key.contains("data")
								? formatDate(value)
								: getInputString(value)
					);
				}catch(final Exception error){
					throw new RuntimeException(error);
				}

				boolean childrenAdded = false;

				if(index == columnsSize - 1){
					try(final Cursor productsCursor = database.rawQuery(
						"select produto.descricao, produto.unidade, compra_produto.quantidade " +
						"from produto " +
						"join compra_produto on compra_produto.id_produto = produto.id " +
						"where compra_produto.id_compra = ?",
						new String[]{ compraId }
					)){
						final ArrayList<String> productsList = new ArrayList<>();

						while(productsCursor.moveToNext()){
							productsList.add(String.format(
								"%dx %s (%s)",
								productsCursor.getInt(2),
								productsCursor.getString(0),
								productsCursor.getString(1)
							));
						}

						if(productsList.size() > 0){
							final LinearLayout productRowContainer = new LinearLayout(this);

							productRowContainer.setOrientation(LinearLayout.HORIZONTAL);
							productRowContainer.setPadding(0, 0, 0, 4);

							final TextView productsRowTitle = new TextView(this),
							productsRowValue = new TextView(this);

							productsRowTitle.setTypeface(null, Typeface.BOLD);
							productsRowTitle.setText("Produtos: ");

							productsRowValue.setText(String.join(", ", productsList));

							rowContainer.addView(columnTitle);
							rowContainer.addView(columnValue);
							productRowContainer.addView(productsRowTitle);
							productRowContainer.addView(productsRowValue);
							container.addView(rowContainer);
							container.addView(productRowContainer);

							childrenAdded = true;
						}
					}
				}

				if(!childrenAdded){
					rowContainer.addView(columnTitle);
					rowContainer.addView(columnValue);
					container.addView(rowContainer);
				}
			}

			lista.addView(container);
		}

		cursor.close();
	}
}
