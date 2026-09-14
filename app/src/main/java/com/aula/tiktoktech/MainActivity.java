package com.aula.tiktoktech;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.aula.tiktoktech.model.Post;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Componentes de Tela
    TextView txtVazio;
    FloatingActionButton fabNovaFoto;
    private Uri fotoUri;
    private final List<Post> posts = new ArrayList<>();
    private PostAdapter adapter;
    private ListenerRegistration feedListener;


    // Configurar Câmera
    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok != null && ok) {
                    salvarNuvem();
                } else {
                    fabNovaFoto.setEnabled(true);
                    txtVazio.setText(R.string.msg_captura_cancelada);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar componentes de tela
        txtVazio = findViewById(R.id.txtVazio);
        fabNovaFoto = findViewById(R.id.fabNovaFoto);
        RecyclerView recyclerPosts = findViewById(R.id.recyclerPosts);
        adapter = new PostAdapter(posts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(adapter);
        observarFeed();

        // Configurar botão de nova foto
        fabNovaFoto.setOnClickListener(v -> {
            verificarLogin();
            tirarFoto();
        });

        // Configurar Cloudinary
        try {
            Map<String, String> config = new HashMap<>();

            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);

            MediaManager.init(this, config);

        } catch (Exception e) {
            Log.i("app_minhaselfie", "Erro ao configurar Cloudinary: " + e.getMessage());
        }
    }

    private void observarFeed() {
        feedListener = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("criadoEm", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(
                                this,
                                getString(R.string.msg_erro_feed, error.getMessage()),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    posts.clear();
                    if (snapshot != null) for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            posts.add(post);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    txtVazio.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override protected void onDestroy() { if (feedListener != null) feedListener.remove(); super.onDestroy(); }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_trocar_usuario) {
            startActivity(new Intent(this, LoginActivity.class).putExtra("trocar_usuario", true));
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_logout) {
            getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Metodo para salvar foto no Cloudinary
    private void salvarNuvem() {

        fabNovaFoto.setEnabled(false);
        txtVazio.setText("Enviando foto para a nuvem...");


        MediaManager.get()
                .upload(fotoUri)
                .option("folder", getString(R.string.cloudinary_folder))
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String urlImagem = (String) resultData.get("secure_url");
                        pedirLegenda(urlImagem);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        fabNovaFoto.setEnabled(true);
                        txtVazio.setText(getString(R.string.msg_erro_upload, error.getDescription()));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }

                }).dispatch();
    }

    private void pedirLegenda(String urlImagem) {
        EditText entrada = new EditText(this);
        entrada.setHint(R.string.hint_legenda);
        entrada.setSingleLine(false);

        int margem = (int) (24 * getResources().getDisplayMetrics().density);
        entrada.setPadding(margem, 8, margem, 8);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.titulo_nova_publicacao)
                .setView(entrada)
                .setNegativeButton(R.string.acao_cancelar, (d, w) -> liberarCamera())
                .setPositiveButton(R.string.acao_publicar, (d, w) -> {

                    String legenda = entrada.getText().toString().trim();

                    FirebaseFirestore.getInstance().collection("posts")
                            .add(new Post(urlImagem, legenda))
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    String erro = task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Erro desconhecido";
                                    Toast.makeText(
                                            MainActivity.this,
                                            MainActivity.this.getString(R.string.msg_erro_salvar, erro),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                                liberarCamera();
                            });
                }).setOnCancelListener(d -> liberarCamera()).show();
    }

    private void liberarCamera() { fabNovaFoto.setEnabled(true); }

    // Metodo para tirar foto
    private void tirarFoto() {
        if (getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_USERNAME, "").isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        File arquivo = new File(getExternalFilesDir(null), "foto_" + System.currentTimeMillis() + ".jpg");
        fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);

        fabNovaFoto.setEnabled(false);
        camera.launch(fotoUri);
    }

    // Verificar Login
    private void verificarLogin() {
        if (getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_USERNAME, "").isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


}
