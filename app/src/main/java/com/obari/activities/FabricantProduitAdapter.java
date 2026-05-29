package com.obari.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.obari.R;
import com.obari.models.Produit;

import java.util.List;

/**
 * Adaptateur liste produits fabricant (layout horizontal avec edit/delete).
 */
public class FabricantProduitAdapter extends RecyclerView.Adapter<FabricantProduitAdapter.VH> {

    public interface OnEditListener  { void onEdit(Produit p); }
    public interface OnDeleteListener { void onDelete(Produit p); }

    private final Context ctx;
    private final List<Produit> produits;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    public FabricantProduitAdapter(Context ctx, List<Produit> produits,
                                   OnEditListener el, OnDeleteListener dl) {
        this.ctx = ctx; this.produits = produits;
        this.editListener = el; this.deleteListener = dl;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_produit_fabricant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Produit p = produits.get(position);
        h.tvNom.setText(p.getNom());
        h.tvPrix.setText(String.format("%.0f FCFA", p.getPrix()));
        h.tvStock.setText("Stock : " + p.getStock());
        h.ivProduit.setImageResource(R.drawable.ic_produit_placeholder);

        if (p.getPromo() > 0) {
            h.tvPromo.setVisibility(View.VISIBLE);
            h.tvPromo.setText("-" + p.getPromo() + "%");
        } else {
            h.tvPromo.setVisibility(View.GONE);
        }

        h.btnEdit.setOnClickListener(v -> { if (editListener != null) editListener.onEdit(p); });
        h.btnDelete.setOnClickListener(v -> { if (deleteListener != null) deleteListener.onDelete(p); });
    }

    @Override public int getItemCount() { return produits.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProduit;
        TextView tvNom, tvPrix, tvStock, tvPromo;
        ImageButton btnEdit, btnDelete;

        VH(View v) {
            super(v);
            ivProduit = v.findViewById(R.id.iv_produit);
            tvNom     = v.findViewById(R.id.tv_nom_produit);
            tvPrix    = v.findViewById(R.id.tv_prix_produit);
            tvStock   = v.findViewById(R.id.tv_stock);
            tvPromo   = v.findViewById(R.id.tv_promo);
            btnEdit   = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
