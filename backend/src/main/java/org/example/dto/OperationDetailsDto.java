package org.example.dto;

import java.util.ArrayList;
import java.util.List;

public class OperationDetailsDto {

    private OperationProcessDto operation;
    private List<FluxMatiereDto> flux = new ArrayList<>();
    private BilanMassiqueDto bilanMassique;
    private List<LotProductionDto> lots = new ArrayList<>();
    private List<AnalyseLaboratoireDto> analyses = new ArrayList<>();

    public OperationDetailsDto() {
    }

    public OperationProcessDto getOperation() {
        return operation;
    }

    public void setOperation(OperationProcessDto operation) {
        this.operation = operation;
    }

    public List<FluxMatiereDto> getFlux() {
        return flux;
    }

    public void setFlux(List<FluxMatiereDto> flux) {
        this.flux = flux;
    }

    public BilanMassiqueDto getBilanMassique() {
        return bilanMassique;
    }

    public void setBilanMassique(BilanMassiqueDto bilanMassique) {
        this.bilanMassique = bilanMassique;
    }

    public List<LotProductionDto> getLots() {
        return lots;
    }

    public void setLots(List<LotProductionDto> lots) {
        this.lots = lots;
    }

    public List<AnalyseLaboratoireDto> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<AnalyseLaboratoireDto> analyses) {
        this.analyses = analyses;
    }
}
