package ch.hearc.cafheg.business.versements;

import static java.util.stream.Collectors.toMap;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.infrastructure.pdf.PDFExporter;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersementService {
  private final VersementMapper versementMapper;
  private final AllocataireMapper allocataireMapper;
  private final PDFExporter pdfExporter;
  private static final Logger logger = LoggerFactory.getLogger(VersementService.class);

  public VersementService(
      VersementMapper versementMapper,
      AllocataireMapper allocataireMapper,
      PDFExporter pdfExporter) {
    this.versementMapper = versementMapper;
    this.allocataireMapper = allocataireMapper;
    this.pdfExporter = pdfExporter;
  }

  public byte[] exportPDFVersements(long allocataireId) {
    logger.info("Exporter le PDF des versements pour l'allocataire {}", allocataireId);
    try {
      List<VersementParentParMois> versementParentEnfantParMois = versementMapper
              .findVersementParentEnfantParMois();

      Map<LocalDate, Montant> montantParMois = versementParentEnfantParMois.stream()
              .filter(v -> v.getParentId() == allocataireId)
              .collect(toMap(VersementParentParMois::getMois,
                      v -> new Montant(v.getMontant().getValue()),
                      (v1, v2) -> new Montant(v1.value.add(v2.value))));

      if (montantParMois.isEmpty()) {
        logger.warn("Aucun versement trouvé pour l'allocataire {}", allocataireId);
      }

      Allocataire allocataire = allocataireMapper.findById(allocataireId);
      if (allocataire == null) {
        logger.error("Allocataire {} introuvable", allocataireId);
        throw new IllegalArgumentException("Allocataire introuvable");
      }

      return pdfExporter.generatePDFVversement(allocataire, montantParMois);
    } catch (Exception e) {
      logger.error("Erreur lors de l'export PDF des versements pour l'allocataire {}", allocataireId, e);
      throw e;
    }

  }

  public Montant findSommeAllocationNaissanceParAnnee(int year) {
    logger.info("Rechercher la somme des allocations de naissances pour l'année {}", year);
    try {
      List<VersementAllocationNaissance> versements = versementMapper
              .findAllVersementAllocationNaissance();
      return VersementAllocationNaissance.sommeParAnnee(versements, year);
    } catch (Exception e) {
      logger.error("Erreur lors du calcul des allocations naissance pour l'année {}", year, e);
      throw e;
    }
  }

  public Montant findSommeAllocationParAnnee(int year) {
    logger.info("Rechercher la somme des allocations  {}", year);
    try {
      List<VersementAllocation> versements = versementMapper
              .findAllVersementAllocation();
      return VersementAllocation.sommeParAnnee(versements, year);
    }catch (Exception e) {
      logger.error("Erreur lors du calcul des allocations pour l'année {}", year, e);
      throw e;
    }
  }

  public byte[] exportPDFAllocataire(long allocataireId) {
    logger.info("Exporter les PDF pour l'allocataire: {}", allocataireId);
    try {
      List<VersementParentEnfant> versements = versementMapper.findVersementParentEnfant();

      Map<Long, Montant> montantsParEnfant = versements.stream()
              .filter(v -> v.getParentId() == allocataireId)
              .collect(Collectors.toMap(VersementParentEnfant::getEnfantId,
                      VersementParentEnfant::getMontant, (v1, v2) -> v1));

      if (montantsParEnfant.isEmpty()) {
        logger.warn("Aucun versement trouvé pour l'allocataire {}", allocataireId);
      }

      Allocataire allocataire = allocataireMapper.findById(allocataireId);
      if (allocataire == null) {
        logger.error("Allocataire {} introuvable", allocataireId);
        throw new IllegalArgumentException("Allocataire introuvable");
      }

      return pdfExporter.generatePDFAllocataire(allocataire, montantsParEnfant);
    } catch (Exception e) {
      logger.error("Erreur lors de l'export PDF de l'allocataire {}", allocataireId, e);
      throw e;
    }
  }
}