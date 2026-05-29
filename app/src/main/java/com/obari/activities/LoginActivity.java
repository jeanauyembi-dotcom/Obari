package com.obari.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.obari.R;
import com.obari.models.User;
import com.obari.utils.Constants;
import com.obari.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextInputLayout tilNom;
    private TextInputLayout tilRole;
    private TextInputEditText etNom;
    private TextInputEditText etTelephone;
    private TextInputEditText etPassword;
    private AutoCompleteTextView spinnerRole;
    private Button btnAction;

    private boolean modeInscription = false;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usersRef       = FirebaseDatabase.getInstance().getReference(Constants.DB_USERS);
        sessionManager = new SessionManager(this);

        // Si déjà connecté → rediriger
        if (sessionManager.isLoggedIn()) {
            naviguerVersRole(sessionManager.getRole());
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        tabLayout    = findViewById(R.id.tab_layout);
        tilNom       = findViewById(R.id.til_nom);
        tilRole      = findViewById(R.id.til_role);
        etNom        = findViewById(R.id.et_nom);
        etTelephone  = findViewById(R.id.et_telephone);
        etPassword   = findViewById(R.id.et_password);
        spinnerRole  = findViewById(R.id.spinner_role);
        btnAction    = findViewById(R.id.btn_action);

        // Spinner rôles
        ArrayAdapter<String> rolesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{Constants.ROLE_CLIENT, Constants.ROLE_FABRICANT, Constants.ROLE_TRIEUR});
        spinnerRole.setAdapter(rolesAdapter);
        spinnerRole.setText(Constants.ROLE_CLIENT, false);

        // Onglets Connexion / Inscription
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                modeInscription = tab.getPosition() == 1;
                mettreAJourUI();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnAction.setOnClickListener(v -> {
            if (modeInscription) inscrire(); else connecter();
        });
    }

    private void mettreAJourUI() {
        int vis = modeInscription ? android.view.View.VISIBLE : android.view.View.GONE;
        tilNom.setVisibility(vis);
        tilRole.setVisibility(vis);
        btnAction.setText(modeInscription ? R.string.btn_inscrire : R.string.btn_connecter);
    }

    private void connecter() {
        String tel = getTexte(etTelephone);
        String pwd = getTexte(etPassword);
        if (tel.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, R.string.error_champs_vides, Toast.LENGTH_SHORT).show();
            return;
        }
        String cle = tel.replace("+", "");
        usersRef.child(cle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    Toast.makeText(LoginActivity.this, R.string.error_compte_introuvable, Toast.LENGTH_SHORT).show();
                    return;
                }
                User u = snap.getValue(User.class);
                if (u != null && pwd.equals(u.getPassword())) {
                    sessionManager.saveSession(u.getNom(), tel, u.getRole(), 0);
                    naviguerVersRole(u.getRole());
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Mot de passe incorrect.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(LoginActivity.this,
                        getString(R.string.error_firebase, err.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void inscrire() {
        String nom  = getTexte(etNom);
        String tel  = getTexte(etTelephone);
        String pwd  = getTexte(etPassword);
        String role = spinnerRole.getText().toString().trim();
        if (nom.isEmpty() || tel.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, R.string.error_champs_vides, Toast.LENGTH_SHORT).show();
            return;
        }
        String cle = tel.replace("+", "");
        User u = new User(nom, tel, pwd, role);
        usersRef.child(cle).setValue(u)
                .addOnSuccessListener(a -> {
                    sessionManager.saveSession(nom, tel, role, 0);
                    Toast.makeText(this, R.string.inscription_reussie, Toast.LENGTH_SHORT).show();
                    naviguerVersRole(role);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.error_firebase, e.getMessage()),
                                Toast.LENGTH_LONG).show());
    }

    private void naviguerVersRole(String role) {
        Class<?> dest;
        if (Constants.ROLE_FABRICANT.equals(role))      dest = FabricantActivity.class;
        else if (Constants.ROLE_TRIEUR.equals(role))    dest = TrieurActivity.class;
        else                                             dest = ClientActivity.class;
        startActivity(new Intent(this, dest));
    }

    private String getTexte(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
