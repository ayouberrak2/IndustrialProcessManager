CREATE DATABASE IF NOT EXISTS ocpprocess
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ocpprocess;

-- =========================================================
-- Table pour le login
-- =========================================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    atelier_id INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_utilisateur_username (username),
    UNIQUE KEY uk_utilisateur_email (email)
);

-- =========================================================
-- Tables du diagramme UML
-- =========================================================

CREATE TABLE IF NOT EXISTS atelier (
    id INT NOT NULL AUTO_INCREMENT,
    code_atelier VARCHAR(50) NOT NULL,
    nom_atelier VARCHAR(100) NOT NULL,
    est_actif BOOLEAN NOT NULL DEFAULT TRUE,
    chef_atelier_id INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_atelier_code (code_atelier),
    UNIQUE KEY uk_atelier_chef (chef_atelier_id)
);

CREATE TABLE IF NOT EXISTS equipement (
    id INT NOT NULL AUTO_INCREMENT,
    atelier_id INT NOT NULL,
    tag_industriel VARCHAR(100) NOT NULL,
    type_equipement VARCHAR(100) NOT NULL,
    nom_equipement VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_equipement_tag (tag_industriel),
    CONSTRAINT fk_equipement_atelier
        FOREIGN KEY (atelier_id)
        REFERENCES atelier(id)
);

CREATE TABLE IF NOT EXISTS operation_process (
    id INT NOT NULL AUTO_INCREMENT,
    num_ordre_fab VARCHAR(100) NOT NULL,
    type_operation VARCHAR(100) NOT NULL,
    statut_operation VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NULL,
    operateur VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- Association entre OperationProcess et Equipement
-- C'est la table qui remplace AssociationClass3 du schema.
CREATE TABLE IF NOT EXISTS operation_equipement (
    id INT NOT NULL AUTO_INCREMENT,
    operation_process_id INT NOT NULL,
    equipement_id INT NOT NULL,
    duree_estimee INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_operation_equipement_operation
        FOREIGN KEY (operation_process_id)
        REFERENCES operation_process(id),
    CONSTRAINT fk_operation_equipement_equipement
        FOREIGN KEY (equipement_id)
        REFERENCES equipement(id)
);

CREATE TABLE IF NOT EXISTS article_matiere (
    id INT NOT NULL AUTO_INCREMENT,
    nom_article VARCHAR(100) NOT NULL,
    categorie VARCHAR(100) NOT NULL,
    unite_standard VARCHAR(50) NOT NULL,
    densite_standard FLOAT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS flux_matiere (
    id INT NOT NULL AUTO_INCREMENT,
    operation_process_id INT NOT NULL,
    article_matiere_id INT NOT NULL,
    type_flux VARCHAR(100) NOT NULL,
    mesure_capteur FLOAT NOT NULL,
    mesure_diametre FLOAT NOT NULL,
    date_mesure DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_flux_operation
        FOREIGN KEY (operation_process_id)
        REFERENCES operation_process(id),
    CONSTRAINT fk_flux_article
        FOREIGN KEY (article_matiere_id)
        REFERENCES article_matiere(id)
);

CREATE TABLE IF NOT EXISTS bilan_massique (
    id INT NOT NULL AUTO_INCREMENT,
    operation_process_id INT NOT NULL,
    total_entrees_t FLOAT NOT NULL,
    total_sorties_t FLOAT NOT NULL,
    ecart_pertes_t FLOAT NOT NULL,
    rendement_val FLOAT NOT NULL,
    date_calcul DATE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bilan_operation (operation_process_id),
    CONSTRAINT fk_bilan_operation
        FOREIGN KEY (operation_process_id)
        REFERENCES operation_process(id)
);

CREATE TABLE IF NOT EXISTS lot_production (
    id INT NOT NULL AUTO_INCREMENT,
    operation_process_id INT NOT NULL,
    article_matiere_id INT NOT NULL,
    date DATE NOT NULL,
    statut_qualite VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lot_operation
        FOREIGN KEY (operation_process_id)
        REFERENCES operation_process(id),
    CONSTRAINT fk_lot_article
        FOREIGN KEY (article_matiere_id)
        REFERENCES article_matiere(id)
);

CREATE TABLE IF NOT EXISTS analyse_laboratoire (
    id INT NOT NULL AUTO_INCREMENT,
    lot_production_id INT NOT NULL,
    taux_p2o5 FLOAT NOT NULL,
    taux_cadmium_ppm FLOAT NOT NULL,
    solides_suspendu FLOAT NOT NULL,
    date_analyse DATE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_analyse_lot (lot_production_id),
    CONSTRAINT fk_analyse_lot
        FOREIGN KEY (lot_production_id)
        REFERENCES lot_production(id)
);

-- =========================================================
-- Quelques donnees simples pour tester
-- =========================================================

INSERT IGNORE INTO atelier (id, code_atelier, nom_atelier, est_actif)
VALUES (1, 'ATL-01', 'Atelier Principal', TRUE);

-- Si la base existe deja, cette ligne ajoute la colonne atelier_id.
ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS atelier_id INT NULL;

-- Si la base existe deja, cette ligne ajoute le chef de l'atelier.
ALTER TABLE atelier
    ADD COLUMN IF NOT EXISTS chef_atelier_id INT NULL;

UPDATE atelier a
JOIN (
    SELECT chef_atelier_id, MIN(id) AS keep_id
    FROM atelier
    WHERE chef_atelier_id IS NOT NULL
    GROUP BY chef_atelier_id
) keeper ON a.chef_atelier_id = keeper.chef_atelier_id
SET a.chef_atelier_id = NULL
WHERE a.id <> keeper.keep_id;

-- Mot de passe : admin123
-- Hash SHA-256 de admin123
INSERT INTO utilisateur (username, password_hash, role, email, atelier_id)
VALUES (
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'ADMIN',
    'admin@ocp.local',
    NULL
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    email = VALUES(email),
    atelier_id = VALUES(atelier_id);

-- Autres roles disponibles dans l'application :
-- chef / chef123
-- labo / labo123
INSERT INTO utilisateur (username, password_hash, role, email, atelier_id)
VALUES (
    'chef',
    'fa0990ab6f2ecfd562611cedad67152e8c1117f91c22d15094d1e242314243af',
    'CHEF_ATELIER',
    'chef@ocp.local',
    1
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    email = VALUES(email),
    atelier_id = VALUES(atelier_id);

INSERT INTO utilisateur (username, password_hash, role, email, atelier_id)
VALUES (
    'labo',
    '7ef60dabca460d6a6374930fabe1280052ad358b81b3ab396f2931c04628fd75',
    'TECHNICIEN_LABO',
    'labo@ocp.local',
    1
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    email = VALUES(email),
    atelier_id = VALUES(atelier_id);

UPDATE atelier
SET chef_atelier_id = (
    SELECT id FROM utilisateur
    WHERE username = 'chef'
    LIMIT 1
)
WHERE id = 1;

INSERT IGNORE INTO equipement (id, atelier_id, tag_industriel, type_equipement, nom_equipement)
VALUES (1, 1, 'EQ-001', 'Capteur', 'Capteur debit');

INSERT IGNORE INTO article_matiere (id, nom_article, categorie, unite_standard, densite_standard)
VALUES (1, 'Phosphate', 'Matiere premiere', 'Tonne', 1.50);

INSERT IGNORE INTO operation_process (
    id,
    num_ordre_fab,
    type_operation,
    statut_operation,
    date_debut,
    date_fin,
    operateur
)
VALUES (
    1,
    'OF-001',
    'Production',
    'EN_COURS',
    '2026-06-16',
    NULL,
    'Operateur 1'
);

INSERT IGNORE INTO operation_equipement (
    id,
    operation_process_id,
    equipement_id,
    duree_estimee
)
VALUES (1, 1, 1, 120);

INSERT IGNORE INTO flux_matiere (
    id,
    operation_process_id,
    article_matiere_id,
    type_flux,
    mesure_capteur,
    mesure_diametre,
    date_mesure
)
VALUES (1, 1, 1, 'ENTREE', 100.5, 30.2, '2026-06-16');

INSERT IGNORE INTO bilan_massique (
    id,
    operation_process_id,
    total_entrees_t,
    total_sorties_t,
    ecart_pertes_t,
    rendement_val,
    date_calcul
)
VALUES (1, 1, 100.5, 95.0, 5.5, 94.5, '2026-06-16');

INSERT IGNORE INTO lot_production (
    id,
    operation_process_id,
    article_matiere_id,
    date,
    statut_qualite
)
VALUES (1, 1, 1, '2026-06-16', 'CONFORME');

INSERT IGNORE INTO analyse_laboratoire (
    id,
    lot_production_id,
    taux_p2o5,
    taux_cadmium_ppm,
    solides_suspendu,
    date_analyse
)
VALUES (1, 1, 28.4, 12.2, 3.1, '2026-06-16');
