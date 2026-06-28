package org.example.model;

public class Equipement {

    private int id;
    private String tagIndustriel;
    private String typeEquipement;
    private String nomEquipement;
    private String statutEquipement = "OPERATIONNEL";

    public Equipement() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTagIndustriel() {
        return tagIndustriel;
    }

    public void setTagIndustriel(String tagIndustriel) {
        this.tagIndustriel = tagIndustriel;
    }

    public String getTypeEquipement() {
        return typeEquipement;
    }

    public void setTypeEquipement(String typeEquipement) {
        this.typeEquipement = typeEquipement;
    }

    public String getNomEquipement() {
        return nomEquipement;
    }

    public void setNomEquipement(String nomEquipement) {
        this.nomEquipement = nomEquipement;
    }

    public String getStatutEquipement() {
        return statutEquipement;
    }

    public void setStatutEquipement(String statutEquipement) {
        this.statutEquipement = statutEquipement;
    }
}
