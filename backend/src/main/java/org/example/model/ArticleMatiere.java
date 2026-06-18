package org.example.model;

public class ArticleMatiere {

    private int id;
    private String nomArticle;
    private String categorie;
    private String uniteStandard;
    private float densiteStandard;

    public ArticleMatiere() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getUniteStandard() {
        return uniteStandard;
    }

    public void setUniteStandard(String uniteStandard) {
        this.uniteStandard = uniteStandard;
    }

    public float getDensiteStandard() {
        return densiteStandard;
    }

    public void setDensiteStandard(float densiteStandard) {
        this.densiteStandard = densiteStandard;
    }
}
