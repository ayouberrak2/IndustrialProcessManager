package org.example.service;

import java.util.List;

import org.example.model.Atelier;
import org.example.repository.AtelierRepository;

public class AtelierService {

    private final AtelierRepository atelierRepository;

    public AtelierService(AtelierRepository atelierRepository) {
        this.atelierRepository = atelierRepository;
    }

    public List<Atelier> getAllAteliers() {
        return atelierRepository.findAll();
    }

    public boolean createAtelier(Atelier atelier) {
        if (isBlank(atelier.getCodeAtelier())
                || isBlank(atelier.getNomAtelier())
                || !hasChef(atelier.getChefAtelierId())) {
            return false;
        }

        if (!atelierRepository.chefExists(atelier.getChefAtelierId())) {
            return false;
        }

        if (atelierRepository.chefAlreadyUsed(atelier.getChefAtelierId(), 0)) {
            return false;
        }

        return atelierRepository.create(atelier);
    }

    public boolean updateAtelier(int id, Atelier atelier) {
        if (isBlank(atelier.getCodeAtelier())
                || isBlank(atelier.getNomAtelier())
                || !hasChef(atelier.getChefAtelierId())) {
            return false;
        }

        if (!atelierRepository.chefExists(atelier.getChefAtelierId())) {
            return false;
        }

        if (atelierRepository.chefAlreadyUsed(atelier.getChefAtelierId(), id)) {
            return false;
        }

        atelier.setId(id);
        return atelierRepository.update(atelier);
    }

    public boolean deleteAtelier(int id) {
        return atelierRepository.delete(id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean hasChef(Integer chefAtelierId) {
        return chefAtelierId != null && chefAtelierId > 0;
    }
}
