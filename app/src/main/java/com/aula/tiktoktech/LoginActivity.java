package com.aula.tiktoktech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "sessao_usuario";
    public static final String KEY_USERNAME = "username";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!getIntent().getBooleanExtra("trocar_usuario", false) && !prefs.getString(KEY_USERNAME, "").isEmpty()) {
            abrirTelaInicial(); return;
        }

        setContentView(R.layout.activity_login);
        EditText nome = findViewById(R.id.edtUsername);

        ((Button) findViewById(R.id.btnEntrar)).setOnClickListener(v -> {
            String valor = nome.getText().toString().trim();

            if (valor.isEmpty()) {
                nome.setError(getString(R.string.erro_nome_obrigatorio));
                return;
            }

            prefs.edit().putString(KEY_USERNAME, valor).apply();

            Toast.makeText(this, getString(R.string.bem_vindo, valor), Toast.LENGTH_SHORT).show();
            abrirTelaInicial();
        });
    }
    private void abrirTelaInicial() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
