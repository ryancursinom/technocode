package com.aula.tiktoktech;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Componentes de Tela
    TextView txtVazio;
    FloatingActionButton fabNovaFoto;
    private Uri fotoUri;


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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar componentes de tela
        txtVazio = findViewById(R.id.txtVazio);
        fabNovaFoto = findViewById(R.id.fabNovaFoto);

        // Configurar botão de nova foto
        fabNovaFoto.setOnClickListener(v -> {
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

                        String urlImagem = (String) resultData.get("url");

                        fabNovaFoto.setEnabled(true);
                        txtVazio.setText(urlImagem);
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

    // Metodo para tirar foto
    private void tirarFoto() {
        File arquivo = new File(getExternalFilesDir(null), "foto_" + System.currentTimeMillis() + ".jpg");
        fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", arquivo);

        fabNovaFoto.setEnabled(false);
        camera.launch(fotoUri);
    }


}
