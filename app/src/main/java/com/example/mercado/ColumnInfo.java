package com.example.mercado;

import java.util.Objects;

public class ColumnInfo {
	String key, name;

	ColumnInfo(String key, String name){
		this.key = Objects.requireNonNull(key);
		this.name = Objects.requireNonNull(name);
	}
}
