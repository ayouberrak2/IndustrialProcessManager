package org.example.dto;

public class LotProductionDto {

    private int id;
    private Integer operationProcessId;
    private String numOrdreFab;
    private Integer articleMatiereId;
    private String nomArticle;
    private String date;
    private String statutQualite;

    public LotProductionDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOperationProcessId() {
        return operationProcessId;
    }

    public void setOperationProcessId(Integer operationProcessId) {
        this.operationProcessId = operationProcessId;
    }

    public String getNumOrdreFab() {
        return numOrdreFab;
    }

    public void setNumOrdreFab(String numOrdreFab) {
        this.numOrdreFab = numOrdreFab;
    }

    public Integer getArticleMatiereId() {
        return articleMatiereId;
    }

    public void setArticleMatiereId(Integer articleMatiereId) {
        this.articleMatiereId = articleMatiereId;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatutQualite() {
        return statutQualite;
    }

    public void setStatutQualite(String statutQualite) {
        this.statutQualite = statutQualite;
    }
}
