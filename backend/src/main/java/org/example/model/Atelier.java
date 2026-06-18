package org.example.model;

public class Atelier {

    private int id;
    private String codeAtelier;
    private String nomAtelier;
    private boolean estActif;
    private Integer chefAtelierId;
    private String chefAtelierName;
    private String chefAtelierEmail;
    private int nombreUsers;
    private int nombreTechniciens;

    public Atelier() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodeAtelier() {
        return codeAtelier;
    }

    public void setCodeAtelier(String codeAtelier) {
        this.codeAtelier = codeAtelier;
    }

    public String getNomAtelier() {
        return nomAtelier;
    }

    public void setNomAtelier(String nomAtelier) {
        this.nomAtelier = nomAtelier;
    }

    public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }

    public Integer getChefAtelierId() {
        return chefAtelierId;
    }

    public void setChefAtelierId(Integer chefAtelierId) {
        this.chefAtelierId = chefAtelierId;
    }

    public String getChefAtelierName() {
        return chefAtelierName;
    }

    public void setChefAtelierName(String chefAtelierName) {
        this.chefAtelierName = chefAtelierName;
    }

    public String getChefAtelierEmail() {
        return chefAtelierEmail;
    }

    public void setChefAtelierEmail(String chefAtelierEmail) {
        this.chefAtelierEmail = chefAtelierEmail;
    }

    public int getNombreUsers() {
        return nombreUsers;
    }

    public void setNombreUsers(int nombreUsers) {
        this.nombreUsers = nombreUsers;
    }

    public int getNombreTechniciens() {
        return nombreTechniciens;
    }

    public void setNombreTechniciens(int nombreTechniciens) {
        this.nombreTechniciens = nombreTechniciens;
    }
}
