package com.example.mercado;

import static com.example.mercado.Helpers.formatCep;
import static com.example.mercado.Helpers.formatPhoneNumber;
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

import java.util.Arrays;
import java.util.Objects;

public class ListarClientes extends AppCompatActivity {
	SQLiteDatabase database;

	LinearLayout lista;

	final ColumnInfo[] columns = new ColumnInfo[]{
		new ColumnInfo("id", "ID"),
		new ColumnInfo("nome", "Nome"),
		new ColumnInfo("email", "Email"),
		new ColumnInfo("telefone", "Telefone"),
		new ColumnInfo("dia_pagamento", "Dia de Pagamento"),
		new ColumnInfo("logradouro", "Logradouro"),
		new ColumnInfo("numero", "Número"),
		new ColumnInfo("complemento", "Complemento"),
		new ColumnInfo("bairro", "Bairro"),
		new ColumnInfo("cidade", "Cidade"),
		new ColumnInfo("estado", "Estado"),
		new ColumnInfo("cep", "CEP")
	};

	final String keysQuery = String.join(", ", Arrays.stream(columns)
		.map(columnInfo -> String.format("cliente.%s", columnInfo.key))
		.toArray(String[]::new)
	);

	final int columnsSize = columns.length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_listar_clientes);

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
			"(" +
				"select sum(compra.valor) " +
				"from compra " +
				"where compra.id_cliente = cliente.id and compra.data_pagamento is null" +
			") as pagamento, " +
			"case when exists (" +
				"select 1 " +
				"from compra " +
				"where compra.id_cliente = cliente.id and compra.data_pagamento is null" +
			") then true else false end as em_pendencia, " +
			"case when exists (" +
				"select 1 " +
				"from compra " +
				"where compra.id_cliente = cliente.id " +
					"and compra.data_pagamento is null " +
					"and date('now') > date(" +
						"strftime('%Y-%m-', compra.data) || printf('%02d', cliente.dia_pagamento)," +
						"'+1 month'" +
					")" +
			") then true else false end as em_debito " +
			"from cliente " +
			(hasSearch ? ("where cliente.nome like ? ") : "") +
			"order by cliente.id asc",
			hasSearch ? new String[]{ searchQuery } : null
		);

		while(cursor.moveToNext()){
			final LinearLayout container = new LinearLayout(this);

			container.setOrientation(LinearLayout.VERTICAL);
			container.setPadding(0, 0, 0, 60);

			final boolean emDebito = cursor.getInt(columnsSize + 2) == 1,
			pagamentoPendente = emDebito || cursor.getInt(columnsSize + 1) == 1;

			if(pagamentoPendente){
				final float pagamento = cursor.getFloat(columnsSize);
				final TextView debtTitle = new TextView(this);

				debtTitle.setText(String.format("%s: %s", emDebito ? "Em débito" : "Pendente", formatPrice(pagamento)));
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

					columnValue.setText(columnInfo.key.equals("telefone")
						? formatPhoneNumber(value)
						: columnInfo.key.equals("cep")
							? formatCep(value)
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
