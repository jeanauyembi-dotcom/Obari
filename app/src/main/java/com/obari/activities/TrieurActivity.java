package com.obari.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.obari.R;
import com.obari.utils.Constants;
import com.obari.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Écran Trieur (remplace Screen_Trieur d'App Inventor).
 *
 * Fonctionnalités :
 *   - Affichage du solde du portefeuille
 *   - Prise de photo via l'appareil photo (Camera + FileProvider)
 *   - Validation du tri → envoi dans Firebase (CloudDB remplacé par Firebase)
 *   - Informations du tri en cours (labels)
 */
public class TrieurActivity extends AppCompatActivity {

    // Vues
    private TextView tvSolde;
    private TextView tvInfoTri1;
    private TextView tvInfoTri2;
    private TextView tvInfoTri3;
    private ImageView ivPhoto;
    private Button btnValiderTri;

    // Données
    private Uri photoUri;
    private String cheminPhoto;

    private SessionManager sessionManager;
    private DatabaseReference usersRef;
    private DatabaseReference trisRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trieur);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.titre_trieur);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.DB_USERS);
        trisRef  = FirebaseDatabase.getInstance().getReference(Constants.DB_TRIS);

        initViews();
        chargerSolde();
        setupBoutons();
    }

    // ─── Initialisation ────────────────────────────────────────────────────────

    private void initViews() {
        tvSolde      = findViewById(R.id.tv_solde);
        tvInfoTri1   = findViewById(R.id.tv_info_tri1);
        tvInfoTri2   = findViewById(R.id.tv_info_tri2);
        tvInfoTri3   = findViewById(R.id.tv_info_tri3);
        ivPhoto      = findViewById(R.id.iv_photo);
        btnValiderTri = findViewById(R.id.btn_valider_tri);

        // Clic sur la photo → ouvrir l'appareil photo
        ivPhoto.setOnClickListener(v -> ouvrirCamera());
    }

    private void setupBoutons() {
        btnValiderTri.setOnClickListener(v -> validerTri());

        // Long click sur le bouton → option supplémentaire (ex: historique)
        btnValiderTri.setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.msg_historique_tri, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    // ─── Solde Portefeuille ────────────────────────────────────────────────────

    private void chargerSolde() {
        String cleUser = sessionManager.getTelephone().replace("+", "");
        usersRef.child(cleUser).child("solde").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double solde = 0.0;
                if (snapshot.exists()) {
                    Object val = snapshot.getValue();
                    if (val instanceof Double) solde = (Double) val;
                    else if (val instanceof Long) solde = ((Long) val).doubleValue();
                }
                sessionManager.saveSolde(solde);
                tvSolde.setText(getString(R.string.label_solde, solde));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvSolde.setText(getString(R.string.label_solde, sessionManager.getSolde()));
            }
        });
    }

    // ─── Appareil Photo ────────────────────────────────────────────────────────

    private void ouvrirCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
            return;
        }
        lancerCamera();
    }

    private void lancerCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.error_camera_indisponible, Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = creerFichierPhoto();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_creation_fichier, Toast.LENGTH_SHORT).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, Constants.REQUEST_IMAGE_CAPTURE);
    }

    private File creerFichierPhoto() throws IOException {
        String horodatage = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String nomFichier = "OBARI_" + horodatage;
        File dossier = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photo   = File.createTempFile(nomFichier, ".jpg", dossier);
        cheminPhoto  = photo.getAbsolutePath();
        return photo;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ivPhoto.setImageURI(photoUri);
            tvInfoTri1.setText(getString(R.string.info_photo_prise));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lancerCamera();
        } else {
            Toast.makeText(this, R.string.error_permission_camera, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Validation du Tri ─────────────────────────────────────────────────────

    private void validerTri() {
        if (photoUri == null) {
            Toast.makeText(this, R.string.error_pas_de_photo, Toast.LENGTH_SHORT).show();
            return;
        }

        // Enregistrer le tri dans Firebase
        String cleUser   = sessionManager.getTelephone().replace("+", "");
        String horodatage = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        trisRef.child(cleUser).push().setValue(
                new TriageRecord(sessionManager.getNom(), horodatage, cheminPhoto)
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, R.string.tri_valide, Toast.LENGTH_SHORT).show();
            // Réinitialiser l'interface
            ivPhoto.setImageResource(R.drawable.ic_camera_placeholder);
            tvInfoTri1.setText(R.string.info_tri_en_attente);
            photoUri    = null;
            cheminPhoto = null;
        }).addOnFailureListener(e ->
            Toast.makeText(this, getString(R.string.error_firebase, e.getMessage()),
                    Toast.LENGTH_LONG).show()
        );
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Modèle interne pour un enregistrement de tri ─────────────────────────

    /** Classe de données pour enregistrer un tri dans Firebase. */
    public static class TriageRecord {
        public String trieur;
        public String date;
        public String cheminPhoto;

        public TriageRecord() {}

        public TriageRecord(String trieur, String date, String cheminPhoto) {
            this.trieur      = trieur;
            this.date        = date;
            this.cheminPhoto = cheminPhoto;
        }
    }
}
