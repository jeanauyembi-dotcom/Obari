package com.obari.activities;

import android.content.Context;
import android.graphics.Paint;
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
 * Adaptateur RecyclerView produits pour la marketplace (grille 2 colonnes).
 * Affiche : image, badge promo, badge stock faible, nom, prix barré, bouton +.
 */
public class ProduitAdapter extends RecyclerView.Adapter<ProduitAdapter.ViewHolder> {

    public interface OnProduitClickListener {
        void onClick(Produit produit);
    }
    public interface OnAddToCartListener {
        void onAdd(Produit produit);
    }

    private final Context context;
    private final List<Produit> produits;
    private final OnProduitClickListener clickListener;
    private OnAddToCartListener cartListener;

    public ProduitAdapter(Context context, List<Produit> produits,
                          OnProduitClickListener clickListener) {
        this.context = context;
        this.produits = produits;
        this.clickListener = clickListener;
    }

    public void setCartListener(OnAddToCartListener l) { this.cartListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Produit p = produits.get(position);

        h.tvNom.setText(p.getNom());

        // Prix avec promo
        if (p.getPromo() > 0) {
            h.tvPrix.setText(String.format("%.0f FCFA", p.getPrixPromo()));
            h.tvPrixBarre.setVisibility(View.VISIBLE);
            h.tvPrixBarre.setText(String.format("%.0f FCFA", p.getPrix()));
            h.tvPrixBarre.setPaintFlags(h.tvPrixBarre.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvPromoBadge.setVisibility(View.VISIBLE);
            h.tvPromoBadge.setText(context.getString(R.string.label_promo, p.getPromo()));
        } else {
            h.tvPrix.setText(String.format("%.0f FCFA", p.getPrix()));
            h.tvPrixBarre.setVisibility(View.GONE);
            h.tvPromoBadge.setVisibility(View.GONE);
        }

        // Badge stock faible
        if (p.isStockFaible()) {
            h.tvStockBadge.setVisibility(View.VISIBLE);
            h.tvStockBadge.setText("Stock: " + p.getStock());
        } else if (p.getStock() == 0) {
            h.tvStockBadge.setVisibility(View.VISIBLE);
            h.tvStockBadge.setText("Rupture");
        } else {
            h.tvStockBadge.setVisibility(View.GONE);
        }

        // Image placeholder (Glide peut être ajouté ici)
        h.ivProduit.setImageResource(R.drawable.ic_produit_placeholder);

        // Bouton désactivé si rupture
        h.btnAddCart.setEnabled(p.getStock() > 0);
        h.btnAddCart.setAlpha(p.getStock() > 0 ? 1f : 0.4f);

        h.itemView.setOnClickListener(v -> { if (clickListener != null) clickListener.onClick(p); });
        h.btnAddCart.setOnClickListener(v -> { if (cartListener != null) cartListener.onAdd(p); });
    }

    @Override
    public int getItemCount() { return produits.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduit;
        TextView tvNom, tvPrix, tvPrixBarre, tvPromoBadge, tvStockBadge;
        ImageButton btnAddCart;

        ViewHolder(View v) {
            super(v);
            ivProduit    = v.findViewById(R.id.iv_produit);
            tvNom        = v.findViewById(R.id.tv_nom_produit);
            tvPrix       = v.findViewById(R.id.tv_prix_produit);
            tvPrixBarre  = v.findViewById(R.id.tv_prix_barre);
            tvPromoBadge = v.findViewById(R.id.tv_promo_badge);
            tvStockBadge = v.findViewById(R.id.tv_stock_badge);
            btnAddCart   = v.findViewById(R.id.btn_add_cart);
        }
    }
}
