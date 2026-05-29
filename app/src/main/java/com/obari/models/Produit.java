package com.obari.models;

/**
 * Modèle produit étendu avec stock, promo, catégorie, imageUrl.
 * Compatible Firebase Realtime Database (constructeur vide requis).
 */
public class Produit {

    private String id;
    private String nom;
    private String description;
    private double prix;
    private String imageUrl;
    private String fabricantId;
    private String categorie;   // Alimentaire, Textile, Électronique, etc.
    private int stock;          // Contrôlable à distance via Firebase
    private int promo;          // % de remise, 0 = pas de promo (contrôlable à distance)
    private boolean actif;      // Masquer un produit sans le supprimer (contrôlable à distance)

    // Constructeur vide requis par Firebase
    public Produit() { this.actif = true; }

    public Produit(String nom, String description, double prix,
                   String fabricantId, String categorie, int stock, int promo) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.fabricantId = fabricantId;
        this.categorie = categorie;
        this.stock = stock;
        this.promo = promo;
        this.actif = true;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFabricantId() { return fabricantId; }
    public void setFabricantId(String fabricantId) { this.fabricantId = fabricantId; }

    public String getCategorie() { return categorie != null ? categorie : "Général"; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getPromo() { return promo; }
    public void setPromo(int promo) { this.promo = promo; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    /** Prix après remise. */
    public double getPrixPromo() {
        if (promo > 0 && promo <= 100) {
            return prix * (1 - promo / 100.0);
        }
        return prix;
    }

    /** Vrai si le stock est faible (entre 1 et 5). */
    public boolean isStockFaible() {
        return stock > 0 && stock <= 5;
    }
}
