package com.obari.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
 * Espace Fabricant — liste produits (stats, badge stock/promo),
 * dialog d'ajout avec catégorie + stock + promo.
 * Toutes les valeurs stock/promo sont modifiables à distance via Firebase.
 */
public class FabricantActivity extends AppCompatActivity {

    private TextView tvFabricantNom;
    private TextView tvNbProduits;
    private TextView tvTotalStock;
    private TextView tvPromosActives;
    private RecyclerView recyclerMesProduits;
    private LinearLayout llEmptyState;
    private ExtendedFloatingActionButton fabAjouter;

    private List<Produit> mesProduits = new ArrayList<>();
    private FabricantProduitAdapter adapter;

    private DatabaseReference produitsRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabricant);

        sessionManager = new SessionManager(this);
        produitsRef    = FirebaseDatabase.getInstance().getReference(Constants.DB_PRODUITS);

        initViews();
        setupRecyclerView();
        chargerMesProduits();
    }

    private void initViews() {
        tvFabricantNom   = findViewById(R.id.tv_fabricant_nom);
        tvNbProduits     = findViewById(R.id.tv_nb_produits);
        tvTotalStock     = findViewById(R.id.tv_total_stock);
        tvPromosActives  = findViewById(R.id.tv_promos_actives);
        recyclerMesProduits = findViewById(R.id.recycler_mes_produits);
        llEmptyState     = findViewById(R.id.ll_empty_state);
        fabAjouter       = findViewById(R.id.fab_ajouter);

        String nom = sessionManager.getNom();
        if (nom != null && !nom.isEmpty()) tvFabricantNom.setText(nom);

        fabAjouter.setOnClickListener(v -> ouvrirDialogAjout(null));

        ImageButton btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> deconnecter());
    }

    private void setupRecyclerView() {
        adapter = new FabricantProduitAdapter(this, mesProduits,
                produit -> ouvrirDialogAjout(produit),   // Modifier
                produit -> confirmerSupprimer(produit)   // Supprimer
        );
        recyclerMesProduits.setLayoutManager(new LinearLayoutManager(this));
        recyclerMesProduits.setAdapter(adapter);
    }

    // ─── Dialog Ajout / Modification ──────────────────────────────────────────

    private void ouvrirDialogAjout(Produit produitAModifier) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_ajouter_produit, null);

        TextInputEditText etNom         = dialogView.findViewById(R.id.et_nom_produit);
        TextInputEditText etDesc        = dialogView.findViewById(R.id.et_description_produit);
        TextInputEditText etPrix        = dialogView.findViewById(R.id.et_prix_produit);
        TextInputEditText etStock       = dialogView.findViewById(R.id.et_stock_produit);
        TextInputEditText etPromo       = dialogView.findViewById(R.id.et_promo_produit);
        AutoCompleteTextView etCat      = dialogView.findViewById(R.id.et_categorie);

        // Remplir le spinner catégories
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.CATEGORIES);
        etCat.setAdapter(catAdapter);

        // Mode modification : pré-remplir
        if (produitAModifier != null) {
            etNom.setText(produitAModifier.getNom());
            etDesc.setText(produitAModifier.getDescription());
            etPrix.setText(String.valueOf((int) produitAModifier.getPrix()));
            etStock.setText(String.valueOf(produitAModifier.getStock()));
            etPromo.setText(String.valueOf(produitAModifier.getPromo()));
            etCat.setText(produitAModifier.getCategorie(), false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ObariDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btn_annuler).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_publier).setOnClickListener(v -> {
            String nom   = etNom.getText() != null ? etNom.getText().toString().trim() : "";
            String desc  = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            String prixS = etPrix.getText() != null ? etPrix.getText().toString().trim() : "";
            String stkS  = etStock.getText() != null ? etStock.getText().toString().trim() : "0";
            String promoS = etPromo.getText() != null ? etPromo.getText().toString().trim() : "0";
            String cat   = etCat.getText() != null ? etCat.getText().toString().trim() : "Autre";

            if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prixS)) {
                Toast.makeText(this, R.string.error_champs_vides, Toast.LENGTH_SHORT).show();
                return;
            }

            double prix;
            int stock, promo;
            try {
                prix  = Double.parseDouble(prixS);
                stock = TextUtils.isEmpty(stkS) ? 0 : Integer.parseInt(stkS);
                promo = TextUtils.isEmpty(promoS) ? 0 : Integer.parseInt(promoS);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_prix_invalide, Toast.LENGTH_SHORT).show();
                return;
            }

            if (produitAModifier != null) {
                // Mise à jour
                produitAModifier.setNom(nom);
                produitAModifier.setDescription(desc);
                produitAModifier.setPrix(prix);
                produitAModifier.setStock(stock);
                produitAModifier.setPromo(promo);
                produitAModifier.setCategorie(cat);
                produitsRef.child(produitAModifier.getId()).setValue(produitAModifier)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Produit mis à jour !", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            } else {
                // Nouveau produit
                Produit p = new Produit(nom, desc, prix,
                        sessionManager.getTelephone(), cat, stock, promo);
                DatabaseReference newRef = produitsRef.push();
                p.setId(newRef.getKey());
                newRef.setValue(p)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, R.string.produit_ajoute, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        getString(R.string.error_firebase, e.getMessage()),
                                        Toast.LENGTH_LONG).show());
            }
        });

        dialog.show();
    }

    // ─── Suppression ──────────────────────────────────────────────────────────

    private void confirmerSupprimer(Produit produit) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_supprimer)
                .setMessage(produit.getNom())
                .setPositiveButton("Supprimer", (d, w) ->
                        produitsRef.child(produit.getId()).removeValue()
                                .addOnSuccessListener(a ->
                                        Toast.makeText(this,
                                                R.string.produit_supprime, Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Annuler", null)
                .show();
    }

    // ─── Chargement Firebase ───────────────────────────────────────────────────

    private void chargerMesProduits() {
        String telephone = sessionManager.getTelephone();
        produitsRef.orderByChild("fabricantId")
                .equalTo(telephone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mesProduits.clear();
                        int totalStock = 0;
                        int promos = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Produit p = child.getValue(Produit.class);
                            if (p != null) {
                                p.setId(child.getKey());
                                mesProduits.add(p);
                                totalStock += p.getStock();
                                if (p.getPromo() > 0) promos++;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        actualiserStats(mesProduits.size(), totalStock, promos);
                        llEmptyState.setVisibility(
                                mesProduits.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FabricantActivity.this,
                                getString(R.string.error_firebase, error.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void actualiserStats(int nb, int stock, int promos) {
        tvNbProduits.setText(String.valueOf(nb));
        tvTotalStock.setText(String.valueOf(stock));
        tvPromosActives.setText(String.valueOf(promos));
    }

    private void deconnecter() {
        sessionManager.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
