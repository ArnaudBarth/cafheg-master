package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;

public class AllocationService {
  private static final String PARENT_1 = "Parent1";
  private static final String PARENT_2 = "Parent2";
  private final AllocataireMapper allocataireMapper;
  private final AllocationMapper allocationMapper;
  private final VersementMapper versementMapper;
  private static final Logger logger = LoggerFactory.getLogger(AllocationService.class);

  public AllocationService(AllocataireMapper allocataireMapper, AllocationMapper allocationMapper,
                           VersementMapper versementMapper) {
    this.allocataireMapper = allocataireMapper;
    this.allocationMapper = allocationMapper;
    this.versementMapper = versementMapper;
  }

  public List<Allocataire> findAllAllocataires(String likeNom) {
    logger.info("Rechercher tous les allocataires contenant '{}'", likeNom);
    try {
      List<Allocataire> result = allocataireMapper.findAll(likeNom);
      logger.debug("Nombre d'allocataire trouvés : {}", result.size());
      return result;
    } catch (Exception e) {
      logger.error("Erreur lors de la recherche des allocataires", e);
      throw e;
    }
  }

  public List<Allocation> findAllocationsActuelles() {
    logger.info("Rechercher toutes les allocations actuelles");
    try {
      List<Allocation> result = allocationMapper.findAll();
      logger.debug("Nombre d'allocations trouvées : {}", result.size());
      return result;
    } catch (Exception e) {
      logger.error("Erreur lors de la récupération des allocations", e);
      throw e;
    }
  }

  public String getParentDroitAllocation(ParentAllocationRequest request) {
    logger.info("Déterminer quel parent a le droit aux allocations");
    try {
      Boolean p1AL = Boolean.TRUE.equals(request.getParent1ActiviteLucrative());
      Boolean p2AL = Boolean.TRUE.equals(request.getParent2ActiviteLucrative());
      Number salaireP1 = request.getParent1Salaire() != null ? request.getParent1Salaire() : BigDecimal.ZERO;
      Number salaireP2 = request.getParent2Salaire() != null ? request.getParent2Salaire() : BigDecimal.ZERO;

      logger.debug("Salaires : P1={}, P2={}", salaireP1, salaireP2);

      if (p1AL && !p2AL) {
        logger.debug("Parent 1 seul actif");
        return PARENT_1;
      }

      if (p2AL && !p1AL) {
        logger.debug("Parent 2 seul actif");
        return PARENT_2;
      }

      String parentChoisi = salaireP1.doubleValue() > salaireP2.doubleValue() ? PARENT_1 : PARENT_2;
      logger.debug("Comparaison des salaires, parent choisi : {}", parentChoisi);
      return parentChoisi;
    } catch (Exception e) {
      logger.error("Erreur lors de la détermination du parent ayant droit", e);
      throw e;
    }
  }

  public void supprimerAllocataireSiAucunVersement(Long allocataireId) {
    try {
      if (versementMapper.hasVersementsForAllocataire(allocataireId)) {
        logger.error("Impossible de supprimer : l’allocataire {} a déjà reçu des versements.", allocataireId);
        throw new IllegalStateException("Suppression impossible : des versements existent pour cet allocataire.");
      }

      logger.info("Aucun versement trouvé. Suppression de l’allocataire {} en cours...", allocataireId);
      allocataireMapper.supprimerAllocataireParId(allocataireId);
      logger.info("Allocataire {} supprimé avec succès.", allocataireId);
    } catch (Exception e) {
      logger.error("Erreur lors de la suppression de l'allocataire {}", allocataireId, e);
      throw e;
    }
  }

  public void modifierAllocataire(Long numero, String nouveauNom, String nouveauPrenom) {
    try {
      Allocataire actuel = allocataireMapper.findByNumero(numero);

      if (actuel == null) {
        logger.warn("Tentative de modification : allocataire {} introuvable", numero);
        throw new IllegalArgumentException("Allocataire introuvable");
      }

      if (actuel.getNom().equals(nouveauNom) && actuel.getPrenom().equals(nouveauPrenom)) {
        logger.info("Aucune modification pour l’allocataire {} (nom et prénom identiques)", numero);
        return;
      }

      allocataireMapper.modifierNomPrenom(numero, nouveauNom, nouveauPrenom);
      logger.info("Allocataire {} modifié : nom='{}' → '{}', prénom='{}' → '{}'",
              numero, actuel.getNom(), nouveauNom, actuel.getPrenom(), nouveauPrenom);
    } catch (Exception e) {
      logger.error("Erreur lors de la modification de l'allocataire {}", numero, e);
      throw e;
    }
  }
}