package com.obari.models;

public class User {
    private String nom;
    private String telephone;
    private String password;
    private String role;
    private double solde;

    public User() {}

    public User(String nom, String telephone, String password, String role) {
        this.nom = nom; this.telephone = telephone;
        this.password = password; this.role = role;
    }

    public String getNom()       { return nom; }
    public void setNom(String v) { nom = v; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String v) { telephone = v; }
    public String getPassword()  { return password; }
    public void setPassword(String v) { password = v; }
    public String getRole()      { return role; }
    public void setRole(String v){ role = v; }
    public double getSolde()     { return solde; }
    public void setSolde(double v){ solde = v; }
}
