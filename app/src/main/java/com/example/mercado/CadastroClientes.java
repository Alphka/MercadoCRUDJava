package com.example.mercado;

import static com.example.mercado.Helpers.formatCep;
import static com.example.mercado.Helpers.formatPhoneNumber;
import static com.example.mercado.Helpers.getInputString;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Objects;

public class CadastroClientes extends AppCompatActivity {
	private static final String TAG = "cliente";

	private boolean isEditar = false;

	private String userId;

	private EditText inputId,
	inputNome,
	inputEmail,
	inputTelefone,
	inputDiaPagamento,
	inputLogradouro,
	inputNumero,
	inputComplemento,
	inputBairro,
	inputCidade,
	inputEstado,
	inputCep;

	private Button buscarCliente,
	excluirCliente,
	salvar,
	resetar;

	private CharSequence salvarDefaultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SQLiteDatabase database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		setContentView(R.layout.activity_cadastro_clientes);

		inputId = findViewById(R.id.inputId);
		inputNome = findViewById(R.id.inputNome);
		inputEmail = findViewById(R.id.inputEmail);
		inputTelefone = findViewById(R.id.inputTelefone);
		inputDiaPagamento = findViewById(R.id.inputDiaPagamento);
		inputLogradouro = findViewById(R.id.inputLogradouro);
		inputNumero = findViewById(R.id.inputNumero);
		inputComplemento = findViewById(R.id.inputComplemento);
		inputBairro = findViewById(R.id.inputBairro);
		inputCidade = findViewById(R.id.inputCidade);
		inputEstado = findViewById(R.id.inputEstado);
		inputCep = findViewById(R.id.inputCep);

		buscarCliente = findViewById(R.id.buscarCliente);
		excluirCliente = findViewById(R.id.excluirCliente);
		salvar = findViewById(R.id.salvar);
		resetar = findViewById(R.id.resetar);

		salvarDefaultText = salvar.getText();

		salvar.setOnClickListener(view -> {
			final String nome = inputNome.getText().toString().trim(),
			email = inputEmail.getText().toString().trim(),
			telefone = inputTelefone.getText().toString().trim().replaceAll("\\D", ""),
			diaPagamento = inputDiaPagamento.getText().toString().trim(),
			logradouro = inputLogradouro.getText().toString().trim(),
			numero = inputNumero.getText().toString().trim(),
			complemento = inputComplemento.getText().toString().trim(),
			bairro = inputBairro.getText().toString().trim(),
			cidade = inputCidade.getText().toString().trim(),
			estado = inputEstado.getText().toString().trim(),
			cep = inputCep.getText().toString().trim().replaceAll("\\D", "");

			if(nome.length() < 3){
				inputNome.setError("O nome deve ter no mínimo 3 e no máximo 255 caracteres.");
				return;
			}

			if(!email.isEmpty() && !email.matches("^[a-zA-Z\\d.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z\\d](?:[a-zA-Z\\d-]{0,61}[a-zA-Z\\d])?(?:\\.[a-zA-Z\\d](?:[a-zA-Z\\d-]{0,61}[a-zA-Z\\d])?)*$")){
				inputEmail.setError("E-mail inválido");
				return;
			}

			if(!telefone.isEmpty() && (telefone.length() > 15 || !telefone.matches("^55?\\d{1,2}9\\d{8}$"))){
				inputTelefone.setError("Telefone inválido");
				return;
			}

			int diaPagamentoInt = 0;

			try{
				diaPagamentoInt = Integer.parseInt(diaPagamento);
				if(diaPagamentoInt == 0 || diaPagamentoInt > 31){
					inputDiaPagamento.setError("O dia de pagamento deve ser maior que zero e menor ou igual a 31");
					return;
				}
			}catch(Exception error){
				inputDiaPagamento.setError("Valor inválido para o dia de pagamento");
			}

			int numeroInt = 0;

			if(!numero.isEmpty()){
				try{
					numeroInt = Integer.parseInt(numero);
					if(numeroInt <= 0){
						inputNumero.setError("O número da casa deve ser um número inteiro maior que zero");
						return;
					}
				}catch(Exception error){
					inputNumero.setError("Valor inválido para o número da casa");
					return;
				}
			}

			if(!estado.isEmpty() && estado.length() != 2){
				inputEstado.setError("O campo de estado deve ter 2 caracteres");
				return;
			}

			if(!cep.isEmpty() && !cep.matches("^\\d{8}$")){
				inputCep.setError("CEP inválido");
				return;
			}

			final Object[] clientInfo = {
				nome,
				email.isEmpty() ? null : email,
				telefone.isEmpty() ? null : telefone,
				diaPagamentoInt,
				logradouro.isEmpty() ? null : logradouro,
				numero.isEmpty() ? null : numeroInt,
				complemento.isEmpty() ? null : complemento,
				bairro.isEmpty() ? null : bairro,
				cidade.isEmpty() ? null : cidade,
				estado.isEmpty() ? null : estado,
				cep.isEmpty() ? null : cep
			};

			if(isEditar){
				final int clientInfoLength = clientInfo.length;
				final Object[] editClientInfo = Arrays.copyOf(clientInfo, clientInfoLength + 1);

				editClientInfo[clientInfoLength] = userId;

				database.execSQL("update cliente set nome = ?, email = ?, telefone = ?, dia_pagamento = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, cep = ? where id = ?", editClientInfo);
			}else{
				database.execSQL("insert into cliente (nome, email, telefone, dia_pagamento, logradouro, numero, complemento, bairro, cidade, estado, cep) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", clientInfo);
			}

			Toast.makeText(
				this,
				String.format("Cliente %s com sucesso!", isEditar ? "editado" : "cadastrado"),
				Toast.LENGTH_SHORT
			).show();

			resetForm();
		});

		buscarCliente.setOnClickListener(view -> {
			final String idText = inputId.getText().toString().trim();

			if(idText.isEmpty()){
				inputId.setError("ID inválido");
				return;
			}

			try{
				userId = String.valueOf(Integer.parseInt(idText));
			}catch(Exception error){
				Log.e(TAG, Objects.requireNonNull(error.getMessage()));
				return;
			}

			final Cursor cursor = database.rawQuery(
				"select nome, email, telefone, dia_pagamento, logradouro, numero, complemento, bairro, cidade, estado, cep, " +
					"case when exists (" +
						"select 1 " +
						"from compra " +
						"where compra.id_cliente = cliente.id " +
						"limit 1" +
					") then true else false end as possui_compra " +
					"from cliente " +
					"where id = ? " +
					"limit 1",
				new String[]{ userId }
			);

			if(!cursor.moveToNext()){
				Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
				cursor.close();
				return;
			}

			inputId.setEnabled(false);
			buscarCliente.setEnabled(false);
			excluirCliente.setVisibility(View.VISIBLE);

			inputId.setText(idText);
			inputNome.setText(getInputString(cursor.getString(0)));
			inputEmail.setText(getInputString(cursor.getString(1)));
			inputTelefone.setText(formatPhoneNumber(cursor.getString(2)));
			inputDiaPagamento.setText(getInputString(cursor.getString(3)));
			inputLogradouro.setText(getInputString(cursor.getString(4)));
			inputNumero.setText(getInputString(cursor.getString(5)));
			inputComplemento.setText(getInputString(cursor.getString(6)));
			inputBairro.setText(getInputString(cursor.getString(7)));
			inputCidade.setText(getInputString(cursor.getString(8)));
			inputEstado.setText(getInputString(cursor.getString(9)));
			inputCep.setText(formatCep(cursor.getString(10)));
			salvar.setText("Editar");
			isEditar = true;

			// A exclusão de um cliente que possui compras não é possível,
			// apenas por exclusão lógica, ou se as compras do cliente forem excluídas anteriormente
			final boolean possuiCompra = cursor.getInt(11) == 1;

			if(possuiCompra){
				excluirCliente.setBackgroundColor(0xFFE3DCE4);
				excluirCliente.setEnabled(false);
			}else{
				excluirCliente.setBackgroundColor(0xFFCC0000);
				excluirCliente.setEnabled(true);
			}

			cursor.close();
		});

		excluirCliente.setOnClickListener(view -> {
			database.delete("cliente", "id = ?", new String[]{ userId });
			finish();
		});

		resetar.setOnClickListener(view -> resetForm());
	}
	private void resetForm(){
		isEditar = false;

		excluirCliente.setVisibility(View.INVISIBLE);
		buscarCliente.setEnabled(true);
		inputId.setEnabled(true);
		inputId.setText("");
		inputNome.setText("");
		inputEmail.setText("");
		inputTelefone.setText("");
		inputDiaPagamento.setText("");
		inputLogradouro.setText("");
		inputNumero.setText("");
		inputComplemento.setText("");
		inputBairro.setText("");
		inputCidade.setText("");
		inputEstado.setText("");
		inputCep.setText("");
		salvar.setText(salvarDefaultText);
	}
}
