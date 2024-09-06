package com.example.mercado;

import static com.example.mercado.Helpers.DATE_FORMAT;
import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;
import static com.example.mercado.Helpers.isValidDate;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class CadastroCompras extends AppCompatActivity {
	private static final String TAG = "compra";

	private SQLiteDatabase database;

	private boolean isEditar = false;

	private String compraId,
	clienteId,
	clienteNome;

	private int clienteDiaPagamento;

	private float compraValor;

	private TextView textNomeCliente,
	textDataPagamento;

	private EditText inputId,
	inputData,
	inputIdCliente;

	private Button buscarCompra,
	buscarCliente,
	excluirCompra,
	salvar,
	resetar,
	editarProdutos;

	private LinearLayout listaProdutos;

	private CharSequence salvarDefaultText;

	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_cadastro_compras);

		inputId = findViewById(R.id.inputId);
		inputData = findViewById(R.id.inputData);
		inputIdCliente = findViewById(R.id.inputIdCliente);

		textNomeCliente = findViewById(R.id.nomeCliente);
		textDataPagamento = findViewById(R.id.dataPagamento);

		buscarCompra = findViewById(R.id.buscarCompra);
		buscarCliente = findViewById(R.id.buscarCliente);
		excluirCompra = findViewById(R.id.excluirCompra);
		salvar = findViewById(R.id.salvar);
		resetar = findViewById(R.id.resetar);
		editarProdutos = findViewById(R.id.editarProdutos);

		listaProdutos = findViewById(R.id.listaProdutos);

		salvarDefaultText = salvar.getText();

		salvar.setOnClickListener(view -> {
			final String data = inputData.getText().toString().trim();

			try{
				validarDataCompra(data);
			}catch(final Exception error){
				inputData.setError(error.getMessage());
				return;
			}

			final ContentValues compraInfo = new ContentValues();
			final String[] dataParts = data.split("-", 3);

			compraInfo.put("data", String.format(
				"%04d-%02d-%02d",
				Integer.parseInt(dataParts[2]),
				Integer.parseInt(dataParts[1]),
				Integer.parseInt(dataParts[0])
			));

			if(isEditar){
				database.update(
					"compra",
					compraInfo,
					"id = ?",
					new String[]{ compraId }
				);

				Toast.makeText(this, "Compra editada com sucesso!", Toast.LENGTH_SHORT).show();

				adicionarDadosCliente(false);
			}else{
				compraInfo.put("id_cliente", clienteId);

				final int _compraId = (int) database.insert("compra", null, compraInfo);

				Toast.makeText(this, "Compra cadastrada com sucesso!", Toast.LENGTH_SHORT).show();

				compraId = String.valueOf(_compraId);

				newEditForm();
			}
		});

		buscarCompra.setOnClickListener(view -> {
			final String idText = inputId.getText().toString().trim();

			if(idText.isEmpty()){
				inputId.setError("ID inválido");
				return;
			}

			try{
				compraId = String.valueOf(Integer.parseInt(idText));
			}catch(final Exception error){
				Log.e(TAG, Objects.requireNonNull(error.getMessage()));
				return;
			}

			final Cursor cursor = database.rawQuery(
				"select data, id_cliente, cliente.nome as nome_cliente, dia_pagamento " +
				"from compra " +
				"join cliente on cliente.id = id_cliente " +
				"where compra.id = ?",
				new String[]{ compraId }
			);

			if(!cursor.moveToNext()){
				Toast.makeText(this, "Compra não encontrada!", Toast.LENGTH_SHORT).show();
				cursor.close();
				return;
			}

			final String data = cursor.getString(0);

			inputData.setText(Objects.isNull(data) ? "" : String.join("/", data.split("-")));
			inputIdCliente.setText(getInputString(cursor.getString(1)));

			clienteNome = cursor.getString(2);
			clienteDiaPagamento = cursor.getInt(3);

			newEditForm();

			cursor.close();
		});

		buscarCliente.setOnClickListener(view -> {
			final String data = inputData.getText().toString().trim();

			try{
				validarDataCompra(data);
			}catch(final Exception error){
				inputData.setError(error.getMessage());
				return;
			}

			final String idCliente = inputIdCliente.getText().toString().trim();

			if(idCliente.isEmpty()){
				inputIdCliente.setError("ID inválido");
				return;
			}

			try{
				clienteId = String.valueOf(Integer.parseInt(idCliente));
			}catch(final Exception error){
				Log.e(TAG, Objects.requireNonNull(error.getMessage()));
				return;
			}

			final Cursor cursor = database.query(
				"cliente",
				new String[]{ "nome", "dia_pagamento" },
				"id = ?",
				new String[]{ idCliente },
				null,
				null,
				null,
				"1"
			);

			if(!cursor.moveToNext()){
				Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
				cursor.close();
				return;
			}

			inputIdCliente.setEnabled(false);
			buscarCliente.setEnabled(false);
			salvar.setEnabled(true);

			clienteNome = cursor.getString(0);
			clienteDiaPagamento = cursor.getInt(1);

			adicionarDadosCliente();

			cursor.close();
		});

		excluirCompra.setOnClickListener(view -> {
			database.delete("compra", "id = ?", new String[]{ compraId });
			finish();
		});

		resetar.setOnClickListener(view -> resetForm());

		editarProdutos.setOnClickListener(view -> {
			final Intent intent = new Intent(this, CRUDProduto.class);
			intent.putExtra("compra_id", compraId);
			intent.putExtra("compra_valor", compraValor);
			startActivity(intent);
		});
	}
	private void validarDataCompra(final String data) throws Exception {
		if(Objects.isNull(data) || data.isEmpty()) throw new Exception("A data da compra é obrigatória");
		if(!isValidDate(data)) throw new Exception("Data inválida");
	}
	private void adicionarDadosCliente(){
		adicionarDadosCliente(true);
	}
	private void adicionarDadosCliente(boolean atualizarCliente){
		if(atualizarCliente) textNomeCliente.setText(clienteNome);

		final String dataCompra = inputData.getText().toString().trim();
		final Calendar calendar = Calendar.getInstance();
		final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

		try{
			calendar.setTime(dateFormat.parse(dataCompra));
			calendar.set(Calendar.DAY_OF_MONTH, clienteDiaPagamento);
			calendar.add(Calendar.MONTH, 1);
		}catch(final Exception error){
			throw new RuntimeException(error);
		}

		textDataPagamento.setText(dateFormat.format(calendar.getTime()));
	}
	private void newEditForm(){
		inputId.setText(compraId);
		inputId.setEnabled(false);
		buscarCompra.setEnabled(false);
		buscarCliente.setEnabled(false);
		inputIdCliente.setEnabled(false);
		excluirCompra.setVisibility(View.VISIBLE);
		editarProdutos.setVisibility(View.VISIBLE);

		salvar.setEnabled(true);
		salvar.setText("Editar");
		isEditar = true;

		adicionarDadosCliente();

		listaProdutos.removeAllViews();

		final Cursor produtosCursor = database.rawQuery(
			"select produto.id, produto.descricao, compra_produto.quantidade, produto.preco " +
			"from produto " +
			"join compra_produto on compra_produto.id_produto = produto.id " +
			"where compra_produto.id_compra = ? " +
			"order by produto.id asc",
			new String[]{ compraId }
		);

		boolean hasProducts = false;

		compraValor = 0;

		while(produtosCursor.moveToNext()){
			hasProducts = true;

			final String id = produtosCursor.getString(0);
			final String descricao = produtosCursor.getString(1);
			final int quantidade = produtosCursor.getInt(2);
			final float preco = produtosCursor.getFloat(3);

			compraValor += preco * quantidade;

			addProductRow(id, descricao, quantidade, preco);
		}

		produtosCursor.close();

		if(hasProducts){
			final TextView title = new TextView(this);

			title.setText("Produtos adicionados a essa compra:");
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			title.setTypeface(null, Typeface.BOLD);
			title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			title.setPadding(0, 0, 0, 20);

			listaProdutos.addView(title, 0);

			excluirCompra.setBackgroundColor(0xFFE3DCE4);
			excluirCompra.setEnabled(false);
		}else{
			excluirCompra.setBackgroundColor(0xFFCC0000);
			excluirCompra.setEnabled(true);
		}
	}
	private void addProductRow(
		final String id,
		final String name,
		final int quantity,
		final float price
	){
		final LinearLayout container = new LinearLayout(this),
		idRow = new LinearLayout(this),
		productRow = new LinearLayout(this),
		quantityRow = new LinearLayout(this),
		valueRow = new LinearLayout(this);

		final TextView idTitle = new TextView(this),
		idText = new TextView(this),
		productNameTitle = new TextView(this),
		quantityTitle = new TextView(this),
		productNameText = new TextView(this),
		quantityText = new TextView(this),
		valueTitle = new TextView(this),
		valueText = new TextView(this);

		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(20, 0, 20, 40);

		idTitle.setText("ID: ");
		idTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		idTitle.setTypeface(null, Typeface.BOLD);

		idText.setText(String.valueOf(id));
		idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		productNameTitle.setText("Produto: ");
		productNameTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		productNameText.setText(name);
		productNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		quantityTitle.setText("Quantidade: ");
		quantityTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		quantityText.setText(String.valueOf(quantity));
		quantityText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		valueTitle.setText("Preço: ");
		valueTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		valueText.setText(formatPrice(quantity * price));
		valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		idRow.addView(idTitle);
		idRow.addView(idText);
		productRow.addView(productNameTitle);
		productRow.addView(productNameText);
		quantityRow.addView(quantityTitle);
		quantityRow.addView(quantityText);
		valueRow.addView(valueTitle);
		valueRow.addView(valueText);

		container.addView(idRow);
		container.addView(productRow);
		container.addView(quantityRow);
		container.addView(valueRow);
		listaProdutos.addView(container);
	}
	private void resetForm(){
		isEditar = false;

		textDataPagamento.setText("");
		textNomeCliente.setText("");
		editarProdutos.setVisibility(View.INVISIBLE);
		excluirCompra.setVisibility(View.INVISIBLE);
		excluirCompra.setEnabled(true);
		buscarCompra.setEnabled(true);
		inputIdCliente.setEnabled(true);
		buscarCliente.setEnabled(true);
		inputId.setEnabled(true);
		inputId.setText("");
		inputData.setText("");
		inputIdCliente.setText("");
		salvar.setText(salvarDefaultText);
		listaProdutos.removeAllViews();
	}
}
