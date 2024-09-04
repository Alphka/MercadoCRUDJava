package com.example.mercado;

import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ListarCompras extends AppCompatActivity {
	private static final String TAG = "lista_compras";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SQLiteDatabase database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_listar_compras);

		final LinearLayout lista = findViewById(R.id.lista);

		/* final String[] columns = {
			"ID",
			"Data da compra",
			"Valor total da compra",
			"Data final para o pagamento",
			"Nome do cliente",
			"Logradouro",
		}; */

		final ColumnInfo[] columns = new ColumnInfo[]{
			new ColumnInfo("id", "ID"),
			new ColumnInfo("data", "Data da compra"),
			new ColumnInfo("valor", "Valor total da compra"),
			new ColumnInfo("data_pagamento", "Data final para o pagamento"),
			new ColumnInfo("cliente.nome", "Nome do cliente")
		};

		final Cursor cursor = database.rawQuery(
		"select " +
			"compra.id, " +
			"compra.data, " +
			"compra.valor, " +
			"compra.data_pagamento, " +
			"cliente.nome, " +
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
			"order by cliente.id asc",
			null
		);

		final int columnsSize = columns.length;

		while(cursor.moveToNext()){
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
							: columnInfo.key.equals("data") || columnInfo.key.equals("data_pagamento")
								? formatDate(value)
								: getInputString(value)
					);
				}catch(Exception error){
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
	@SuppressLint("DefaultLocale")
	@NonNull
	private String formatDate(@NonNull final String date){
		try{
			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			dateFormat.setLenient(false);
			return DateFormat.getDateInstance(DateFormat.SHORT, new Locale("pt", "BR"))
				.format(Objects.requireNonNull(dateFormat.parse(date)));
		}catch(ParseException error){
			Log.e(TAG, Objects.requireNonNull(error.getMessage()));
			return "";
		}
	}
}

