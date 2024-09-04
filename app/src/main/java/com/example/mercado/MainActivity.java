package com.example.mercado;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SQLiteDatabase database = openOrCreateDatabase("Mercado", Context.MODE_PRIVATE, null);

		database.execSQL("create table if not exists cliente (id integer primary key autoincrement, nome text not null, email text, telefone text, dia_pagamento int not null, logradouro text, complemento text, numero int, bairro text, cidade text, estado text, cep char(8))");
		database.execSQL("create table if not exists compra (id integer primary key autoincrement, data date not null, valor float default 0, data_pagamento date, id_cliente int not null, foreign key (id_cliente) references cliente(id))");
		database.execSQL("create table if not exists produto (id integer primary key autoincrement, descricao text not null, unidade text not null, preco float not null)");
		database.execSQL("create table if not exists compra_produto (id integer primary key autoincrement, quantidade int, id_compra int not null, id_produto int not null, foreign key (id_compra) references compra(id), foreign key (id_produto) references produto(id))");

		setContentView(R.layout.activity_main);

		final Button cadastroClientes = findViewById(R.id.cadastro_clientes),
		listarClientes = findViewById(R.id.listar_clientes),
		cadastroProdutos = findViewById(R.id.cadastro_produtos),
		listarProdutos = findViewById(R.id.listar_produtos),
		cadastroCompras = findViewById(R.id.cadastro_compras),
		listarCompras = findViewById(R.id.listar_compras);

		cadastroClientes.setOnClickListener(view -> startActivity(new Intent(this, CadastroClientes.class)));
		listarClientes.setOnClickListener(view -> startActivity(new Intent(this, ListarClientes.class)));
		cadastroProdutos.setOnClickListener(view -> startActivity(new Intent(this, CadastroProdutos.class)));
		listarProdutos.setOnClickListener(view -> startActivity(new Intent(this, ListarProdutos.class)));
		cadastroCompras.setOnClickListener(view -> startActivity(new Intent(this, CadastroCompras.class)));
		listarCompras.setOnClickListener(view -> startActivity(new Intent(this, ListarCompras.class)));
	}
}
