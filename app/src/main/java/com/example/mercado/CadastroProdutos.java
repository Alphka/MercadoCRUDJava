package com.example.mercado;

import static com.example.mercado.Helpers.getInputString;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Objects;

public class CadastroProdutos extends AppCompatActivity {
	private static final String TAG = "produto";

	boolean isEditar = false;

	String productId;

	EditText inputId,
	inputDescricao,
	inputUnidade,
	inputPreco;

	Button buscarProduto,
	excluirProduto,
	salvar,
	resetar;

	CharSequence salvarDefaultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SQLiteDatabase database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_cadastro_produtos);

		inputId = findViewById(R.id.inputId);
		inputDescricao = findViewById(R.id.inputDescricao);
		inputUnidade = findViewById(R.id.inputUnidade);
		inputPreco = findViewById(R.id.inputPreco);

		buscarProduto = findViewById(R.id.buscarProduto);
		excluirProduto = findViewById(R.id.excluirProduto);
		salvar = findViewById(R.id.salvar);
		resetar = findViewById(R.id.resetar);

		salvarDefaultText = salvar.getText();

		salvar.setOnClickListener(view -> {
			String descricao = inputDescricao.getText().toString().trim(),
			unidade = inputUnidade.getText().toString().trim(),
			preco = inputPreco.getText().toString().trim();

			if(descricao.length() < 3){
				inputDescricao.setError("A descrição deve ter no mínimo 3 e no máximo 255 caracteres.");
				return;
			}

			if(unidade.length() < 4){
				inputUnidade.setError("A unidade deve ter no mínimo 3 e no máximo 255 caracteres");
				return;
			}

			float precoFloat = 0f;

			try{
				precoFloat = Float.parseFloat(preco);
				if(precoFloat == 0){
					inputPreco.setError("O preço deve ser maior que zero");
					return;
				}
			}catch(final Exception error){
				inputPreco.setError("Valor inválido para o preço");
			}

			final Object[] productInfo = {
				descricao,
				unidade,
				precoFloat
			};

			if(isEditar){
				int productInfoLength = productInfo.length;
				Object[] editProductInfo = Arrays.copyOf(productInfo, productInfoLength + 1);

				editProductInfo[productInfoLength] = productId;

				database.execSQL("update produto set descricao = ?, unidade = ?, preco = ? where id = ?", editProductInfo);
			}else{
				database.execSQL("insert into produto (descricao, unidade, preco) values (?, ?, ?)", productInfo);
			}

			Toast.makeText(
				this,
				String.format("Produto %s com sucesso!", isEditar ? "editado" : "cadastrado"),
				Toast.LENGTH_SHORT
			).show();

			resetForm();
		});

		buscarProduto.setOnClickListener(view -> {
			final String idText = inputId.getText().toString().trim();

			if(idText.isEmpty()){
				inputId.setError("ID inválido");
				return;
			}

			try{
				productId = String.valueOf(Integer.parseInt(idText));
			}catch(final Exception error){
				Log.e(TAG, Objects.requireNonNull(error.getMessage()));
				return;
			}

			final Cursor cursor = database.rawQuery(
				"select descricao, unidade, preco, " +
				"case when exists (" +
					"select 1 " +
					"from compra " +
					"where compra.id_produto = produto.id " +
					"limit 1" +
				") then true else false end as possui_venda " +
				"from produto " +
				"where produto.id = ? " +
				"limit 1",
				new String[]{ productId }
			);

			if(!cursor.moveToNext()){
				Toast.makeText(this, "Produto não encontrado!", Toast.LENGTH_SHORT).show();
				cursor.close();
				return;
			}

			inputId.setEnabled(false);
			buscarProduto.setEnabled(false);
			excluirProduto.setVisibility(View.VISIBLE);

			inputId.setText(idText);
			inputDescricao.setText(getInputString(cursor.getString(0)));
			inputUnidade.setText(getInputString(cursor.getString(1)));
			inputPreco.setText(getInputString(cursor.getString(2)));
			salvar.setText("Editar");
			isEditar = true;

			final boolean possuiVenda = cursor.getInt(3) == 1;

			if(possuiVenda){
				excluirProduto.setBackgroundColor(0xFFE3DCE4);
				excluirProduto.setEnabled(false);
			}else{
				excluirProduto.setBackgroundColor(0xFFCC0000);
				excluirProduto.setEnabled(true);
			}

			cursor.close();
		});

		excluirProduto.setOnClickListener(view -> {
			database.delete("produto", "id = ?", new String[]{ productId });
			finish();
		});

		resetar.setOnClickListener(view -> resetForm());
	}
	private void resetForm(){
		isEditar = false;

		excluirProduto.setVisibility(View.INVISIBLE);
		buscarProduto.setEnabled(true);
		inputId.setEnabled(true);
		inputId.setText("");
		inputDescricao.setText("");
		inputUnidade.setText("");
		inputPreco.setText("");
		salvar.setText(salvarDefaultText);
	}
}
