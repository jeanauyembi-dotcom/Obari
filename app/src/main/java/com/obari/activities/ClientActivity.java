package com.obari.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.obari.R;
import com.obari.models.Produit;
import com.obari.utils.Constants;
import com.obari.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Marketplace client — grille produits avec filtre catégorie,
 * badges promo/stock et configuration distante Firebase.
 */
public class ClientActivity extends AppCompatActivity {

    private EditText etRecherche;
    private RecyclerView recyclerProduits;
    private LinearLayout llCategories;
    private TextView tvCountProduits;
    private TextView tvGreeting;

    private List<Produit> tousLesProduits = new ArrayList<>();
    private List<Produit> produitsFiltres = new ArrayList<>();
    private ProduitAdapter adapter;

    private String categorieSelectionnee = "Tous";

    private DatabaseReference produitsRef;
    private DatabaseReference configRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        sessionManager = new SessionManager(this);
        produitsRef    = FirebaseDatabase.getInstance().getReference(Constants.DB_PRODUITS);
        configRef      = FirebaseDatabase.getInstance().getReference(Constants.DB_CONFIG);

        initViews();
        setupRecyclerView();
        setupRecherche();
        setupCategoriesBar();
        chargerConfig();
        chargerProduits();
    }

    // ─── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
        etRecherche      = findViewById(R.id.et_recherche);
        recyclerProduits = findViewById(R.id.recycler_produits);
        llCategories     = findViewById(R.id.ll_categories);
        tvCountProduits  = findViewById(R.id.tv_count_produits);
        tvGreeting       = findViewById(R.id.tv_greeting);

        String nom = sessionManager.getNom();
        if (nom != null && !nom.isEmpty()) {
            tvGreeting.setText("Bonjour, " + nom + " 👋");
        }

        ImageButton btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> deconnecter());
        }
    }

    private void setupRecyclerView() {
        adapter = new ProduitAdapter(this, produitsFiltres, produit -> {
            Toast.makeText(this, produit.getNom(), Toast.LENGTH_SHORT).show();
        });
        adapter.setCartListener(produit -> {
            Toast.makeText(this,
                    getString(R.string.msg_panier_ajoute, produit.getNom()),
                    Toast.LENGTH_SHORT).show();
        });
        recyclerProduits.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProduits.setAdapter(adapter);
    }

    private void setupRecherche() {
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { appliquerFiltres(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoriesBar() {
        for (String cat : Constants.CATEGORIES) {
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(10);
            chip.setLayoutParams(lp);
            chip.setText(cat);
            chip.setTextSize(13);
            chip.setPadding(28, 14, 28, 14);
            chip.setBackground(getDrawable(R.drawable.bg_chip_categorie));
            chip.setSelected(cat.equals("Tous"));
            chip.setTextColor(cat.equals("Tous")
                    ? getColor(R.color.text_on_primary)
                    : getColor(R.color.text_secondary));

            chip.setOnClickListener(v -> {
                categorieSelectionnee = cat;
                // Mettre à jour visuellement tous les chips
                for (int i = 0; i < llCategories.getChildCount(); i++) {
                    View c = llCategories.getChildAt(i);
                    c.setSelected(false);
                    ((TextView) c).setTextColor(getColor(R.color.text_secondary));
                }
                chip.setSelected(true);
                chip.setTextColor(getColor(R.color.text_on_primary));
                appliquerFiltres();
            });

            llCategories.addView(chip);
        }
    }

    // ─── Config distante Firebase ──────────────────────────────────────────────

    private void chargerConfig() {
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Titre marketplace personnalisable
                if (snapshot.hasChild(Constants.CFG_MARKETPLACE_TITRE)) {
                    String titre = snapshot.child(Constants.CFG_MARKETPLACE_TITRE)
                            .getValue(String.class);
                }
                // Bannière promo dynamique (afficher un Toast ou une Card)
                boolean banniereActive = Boolean.TRUE.equals(
                        snapshot.child(Constants.CFG_BANNIERE_ACTIVE).getValue(Boolean.class));
                if (banniereActive) {
                    String msg = snapshot.child(Constants.CFG_BANNIERE_MSG).getValue(String.class);
                    if (msg != null && !msg.isEmpty()) {
                        Toast.makeText(ClientActivity.this, "🎉 " + msg, Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ─── Données produits ──────────────────────────────────────────────────────

    private void chargerProduits() {
        produitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tousLesProduits.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Produit p = child.getValue(Produit.class);
                    if (p != null && p.isActif()) {
                        p.setId(child.getKey());
                        tousLesProduits.add(p);
                    }
                }
                appliquerFiltres();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClientActivity.this,
                        getString(R.string.error_firebase, error.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void appliquerFiltres() {
        String query = etRecherche.getText().toString().toLowerCase().trim();
        produitsFiltres.clear();

        for (Produit p : tousLesProduits) {
            boolean matchCat = categorieSelectionnee.equals("Tous")
                    || categorieSelectionnee.equals(p.getCategorie());
            boolean matchSearch = query.isEmpty()
                    || (p.getNom() != null && p.getNom().toLowerCase().contains(query));
            if (matchCat && matchSearch) {
                produitsFiltres.add(p);
            }
        }

        adapter.notifyDataSetChanged();
        tvCountProduits.setText(produitsFiltres.size() + " produit(s)");
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void deconnecter() {
        sessionManager.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
