package com.example.mercado;

import static com.example.mercado.Helpers.formatPrice;
import static com.example.mercado.Helpers.getInputString;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class CRUDProduto extends AppCompatActivity {
	private static final String TAG = "compra_produto";

	private SQLiteDatabase database;

	private String compraId,
	produtoId,
	nomeProduto,
	unidadeProduto,
	valorCompraDefaultText;

	private float compraValor,
	precoProduto;

	private TextView valorCompraText,
	nomeProdutoText,
	unidadeProdutoText,
	precoProdutoText;

	private EditText inputProdutoId,
	inputQuantidade;

	private Button buscarProduto,
	adicionar,
	remover,
	resetar,
	editar;

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);
		final Bundle extras = Objects.requireNonNull(getIntent().getExtras());

		compraId = extras.getString("compra_id");
		compraValor = extras.getFloat("compra_valor");

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_crud_produto);

		final TextView idCompra = findViewById(R.id.idCompra);
		valorCompraText = findViewById(R.id.valorCompra);

		idCompra.setText(idCompra.getText().toString() + compraId);

		valorCompraDefaultText = valorCompraText.getText().toString();
		valorCompraText.setText(valorCompraDefaultText + formatPrice(compraValor));

		inputProdutoId = findViewById(R.id.inputProdutoId);
		inputQuantidade = findViewById(R.id.inputQuantidade);

		nomeProdutoText = findViewById(R.id.nomeProduto);
		unidadeProdutoText = findViewById(R.id.unidadeProduto);
		precoProdutoText = findViewById(R.id.precoProduto);

		buscarProduto = findViewById(R.id.buscarProduto);
		adicionar = findViewById(R.id.adicionar);
		remover = findViewById(R.id.remover);
		resetar = findViewById(R.id.resetar);
		editar = findViewById(R.id.editar);

		buscarProduto.setOnClickListener(view -> {
			final String idText = inputProdutoId.getText().toString().trim();

			if(idText.isEmpty()){
				inputProdutoId.setError("ID inválido");
				return;
			}

			try{
				produtoId = String.valueOf(Integer.parseInt(idText));
			}catch(Exception error){
				Log.e(TAG, Objects.requireNonNull(error.getMessage()));
				return;
			}

			final Cursor cursor = database.rawQuery(
				"select descricao, unidade, preco from produto where id = ? limit 1",
				new String[]{ produtoId }
			);

			if(!cursor.moveToNext()){
				Toast.makeText(this, "Produto não encontrado!", Toast.LENGTH_SHORT).show();
				cursor.close();
				return;
			}

			adicionar.setVisibility(View.VISIBLE);
			remover.setVisibility(View.VISIBLE);
			editar.setVisibility(View.VISIBLE);

			inputProdutoId.setEnabled(false);
			buscarProduto.setEnabled(false);

			inputProdutoId.setText(idText);

			nomeProduto = cursor.getString(0);
			unidadeProduto = cursor.getString(1);
			precoProduto = cursor.getFloat(2);

			nomeProdutoText.setText(getInputString(nomeProduto));
			unidadeProdutoText.setText(getInputString(unidadeProduto));
			precoProdutoText.setText(formatPrice(precoProduto));

			cursor.close();

			final Cursor compraCursor = database.rawQuery(
				"select quantidade from compra_produto where id_compra = ? and id_produto = ? limit 1",
				new String[]{ compraId, produtoId }
			);

			if(compraCursor.moveToNext()){
				inputQuantidade.setText(getInputString(compraCursor.getString(0)));
			}

			compraCursor.close();
		});

		adicionar.setOnClickListener(view -> {
			int quantidade;

			try{
				 quantidade = getQuantity();
			}catch(Exception error){
				inputQuantidade.setError(error.getMessage());
				return;
			}

			if(hasProduct()){
				Toast.makeText(this, "Este produto já foi adicionado a compra de ID " + compraId, Toast.LENGTH_SHORT).show();
				return;
			}

			final ContentValues compraProdutoInfo = new ContentValues();

			compraProdutoInfo.put("quantidade", quantidade);
			compraProdutoInfo.put("id_compra", compraId);
			compraProdutoInfo.put("id_produto", produtoId);

			database.insert("compra_produto", null, compraProdutoInfo);

			Toast.makeText(this, "Produto adicionado com sucesso!", Toast.LENGTH_SHORT).show();

			updatePurchasePrice();
		});

		editar.setOnClickListener(view -> {
			int quantidade;

			try{
				 quantidade = getQuantity();
			}catch(Exception error){
				inputQuantidade.setError(error.getMessage());
				return;
			}

			if(!hasProduct()){
				Toast.makeText(this, String.format("Essa compra não possui o produto \"%s\" adicionado", nomeProduto), Toast.LENGTH_SHORT).show();
				return;
			}

			final ContentValues compraProdutoInfo = new ContentValues();

			compraProdutoInfo.put("quantidade", quantidade);

			database.update(
				"compra_produto",
				compraProdutoInfo,
				"id_compra = ? and id_produto = ?",
				new String[]{ compraId, produtoId }
			);

			Toast.makeText(this, "O produto foi atualizado com sucesso!", Toast.LENGTH_SHORT).show();

			updatePurchasePrice();
		});

		remover.setOnClickListener(view -> {
			database.delete(
				"compra_produto",
				"id_compra = ? and id_produto = ?",
				new String[]{ compraId, produtoId }
			);

			Toast.makeText(this, "O produto foi removido com sucesso!", Toast.LENGTH_SHORT).show();

			updatePurchasePrice();
		});

		resetar.setOnClickListener(view -> {
			adicionar.setVisibility(View.INVISIBLE);
			remover.setVisibility(View.INVISIBLE);
			editar.setVisibility(View.INVISIBLE);
			buscarProduto.setEnabled(true);
			inputProdutoId.setEnabled(true);
			inputProdutoId.setText("");
			inputQuantidade.setText("");
			nomeProdutoText.setText("");
			unidadeProdutoText.setText("");
			precoProdutoText.setText("");
		});
	}
	private int getQuantity() throws Exception {
		final String quantidade = inputQuantidade.getText().toString().trim();

		int quantidadeInt;

		try{
			quantidadeInt = Integer.parseInt(quantidade);

			if(quantidadeInt == 0) throw new Exception("A quantidade deve ser maior que 0");
		}catch(Exception error){
			throw new Exception("Valor inválido para a quantidade de produtos");
		}

		return quantidadeInt;
	}
	private boolean hasProduct(){
		try(final Cursor compraCursor = database.rawQuery(
		"select 1 from compra " +
			"join compra_produto on compra_produto.id_compra = compra.id " +
			"where compra.id = ? and id_produto = ? " +
			"limit 1",
			new String[]{ compraId, produtoId }
		)){
			return compraCursor.moveToNext();
		}
	}
	@SuppressLint("SetTextI18n")
	private float updatePurchasePrice(){
		final Cursor priceCursor = database.rawQuery(
			"select sum(produto.preco * compra_produto.quantidade) " +
			"from produto " +
			"join compra_produto on compra_produto.id_produto = produto.id " +
			"where compra_produto.id_compra = ?",
			new String[]{ compraId }
		);

		float price = priceCursor.moveToNext() ? priceCursor.getFloat(0) : 0;

		priceCursor.close();

		database.execSQL(
		"update compra " +
			"set valor = ?" +
			"where compra.id = ?",
			new String[]{ String.valueOf(price), compraId }
		);

		valorCompraText.setText(valorCompraDefaultText + formatPrice(price));

		return price;
	}
}
