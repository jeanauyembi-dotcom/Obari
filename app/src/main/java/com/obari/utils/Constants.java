package com.obari.utils;

/**
 * Constantes globales de l'application Obari.
 */
public final class Constants {

    private Constants() {}

    // Rôles utilisateur
    public static final String KEY_ROLE     = "user_role";
    public static final String ROLE_CLIENT    = "Client";
    public static final String ROLE_FABRICANT = "Fabricant";
    public static final String ROLE_TRIEUR    = "Trieur";

    // SharedPreferences
    public static final String PREF_NAME      = "ObariPrefs";
    public static final String PREF_TELEPHONE = "telephone";
    public static final String PREF_NOM       = "nom";
    public static final String PREF_ROLE      = "role";
    public static final String PREF_SOLDE     = "solde";

    // ─── Nœuds Firebase Realtime Database ─────────────────────────────────────

    /** Produits publiés par les fabricants */
    public static final String DB_PRODUITS  = "produits";

    /** Utilisateurs (téléphone → objet User) */
    public static final String DB_USERS     = "utilisateurs";

    /** Enregistrements de tri */
    public static final String DB_TRIS      = "tris";

    /**
     * Nœud de configuration distante.
     * Structure Firebase :
     *   config/
     *     banniere_titre: "Soldes d'été !"
     *     banniere_message: "Jusqu'à -50% sur tous les produits"
     *     banniere_active: true
     *     marketplace_titre: "Marketplace Obari"
     *     maintenance: false
     *     version_min: "1.0"
     */
    public static final String DB_CONFIG    = "config";

    // ─── Clés de configuration distante ───────────────────────────────────────

    public static final String CFG_BANNIERE_TITRE   = "banniere_titre";
    public static final String CFG_BANNIERE_MSG     = "banniere_message";
    public static final String CFG_BANNIERE_ACTIVE  = "banniere_active";
    public static final String CFG_MARKETPLACE_TITRE = "marketplace_titre";
    public static final String CFG_MAINTENANCE      = "maintenance";

    // ─── Catégories de produits ────────────────────────────────────────────────

    public static final String[] CATEGORIES = {
        "Tous", "Alimentaire", "Textile", "Électronique",
        "Maison", "Beauté", "Agriculture", "Autre"
    };

    // ─── Codes caméra ─────────────────────────────────────────────────────────

    public static final int REQUEST_IMAGE_CAPTURE   = 101;
    public static final int REQUEST_CAMERA_PERMISSION = 201;

    // ─── Seuil stock faible ────────────────────────────────────────────────────

    public static final int SEUIL_STOCK_FAIBLE = 5;
}
