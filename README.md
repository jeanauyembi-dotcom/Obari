# Obari App — Android Native

Application Android reconstruite proprement à partir du projet App Inventor original.

## Structure du projet

```
ObariApp/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/obari/
│       │   ├── activities/
│       │   │   ├── LoginActivity.java       ← Connexion / Inscription (Screen1)
│       │   │   ├── ClientActivity.java      ← Catalogue produits (Screen_client)
│       │   │   ├── FabricantActivity.java   ← Gestion produits (Screen_fabricant)
│       │   │   ├── TrieurActivity.java      ← Tri + caméra + portefeuille (Screen_Trieur)
│       │   │   └── ProduitAdapter.java      ← Adaptateur RecyclerView
│       │   ├── models/
│       │   │   ├── User.java
│       │   │   └── Produit.java
│       │   └── utils/
│       │       ├── Constants.java
│       │       └── SessionManager.java
│       └── res/
│           ├── layout/
│           │   ├── activity_login.xml
│           │   ├── activity_client.xml
│           │   ├── activity_fabricant.xml
│           │   ├── activity_trieur.xml
│           │   └── item_produit.xml
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           ├── drawable/    ← icônes vectorielles
│           └── xml/
│               └── file_paths.xml
├── build.gradle
└── settings.gradle
```

## Fonctionnalités

| Écran         | Rôle     | Fonctionnalités                                      |
|---------------|----------|------------------------------------------------------|
| LoginActivity | Tous     | Inscription + connexion, sélection de rôle           |
| ClientActivity| Client   | Catalogue produits en grille, recherche temps réel   |
| FabricantActivity | Fab. | Ajout produit, liste de ses produits publiés         |
| TrieurActivity| Trieur   | Portefeuille, caméra, validation de tri, Firebase    |

## Configuration Firebase (obligatoire)

1. Créer un projet sur [Firebase Console](https://console.firebase.google.com/)
2. Activer **Realtime Database** (règles de test : `".read": true, ".write": true`)
3. Télécharger `google-services.json` et le placer dans `/app/`
4. Ajouter dans le `build.gradle` racine :
   ```groovy
   plugins {
       id 'com.google.gms.google-services' version '4.4.1' apply false
   }
   ```

## Compilation

```bash
# Depuis la racine du projet
./gradlew assembleDebug
# L'APK sera dans : app/build/outputs/apk/debug/app-debug.apk
```

## Notes de migration

- **CloudDB** (Redis) → remplacé par **Firebase Realtime Database**
- **App Inventor runtime** (Kawa/YAIL) → **Java natif** pur
- **TinyDB** → `SharedPreferences` (via `SessionManager`)
- **Camera** App Inventor → `MediaStore.ACTION_IMAGE_CAPTURE` + `FileProvider`
- Les assets images (`.jpg`) de l'ancien projet peuvent être placés dans `res/drawable/`
  ou chargés depuis Firebase Storage si hébergés en ligne.
