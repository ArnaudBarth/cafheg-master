package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.business.allocations.DemandeAllocation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.List;

public class AllocationService {

  private static final String PARENT_1 = "Parent1";
  private static final String PARENT_2 = "Parent2";

  private final AllocataireMapper allocataireMapper;
  private final AllocationMapper allocationMapper;

  public AllocationService(
      AllocataireMapper allocataireMapper,
      AllocationMapper allocationMapper) {
    this.allocataireMapper = allocataireMapper;
    this.allocationMapper = allocationMapper;
  }

  public List<Allocataire> findAllAllocataires(String likeNom) {
    System.out.println("Rechercher tous les allocataires");
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

  public String getParentDroitAllocation(DemandeAllocation d) {
    if (d.isParent1ActiviteLucrative() && !d.isParent2ActiviteLucrative()) {
      return PARENT_1;
    }
    if (d.isParent2ActiviteLucrative() && !d.isParent1ActiviteLucrative()) {
      return PARENT_2;
    }
    // Si les deux parents sont actifs
    if (d.isParent1ActiviteLucrative() && d.isParent2ActiviteLucrative()) {
      if (d.getEnfantResidence() != null) {
        if (d.getEnfantResidence().equalsIgnoreCase(d.getParent1Residence())) {
          return PARENT_1;
        }
        if (d.getEnfantResidence().equalsIgnoreCase(d.getParent2Residence())) {
          return PARENT_2;
        }
      }
    }
    return d.getParent1Salaire().compareTo(d.getParent2Salaire()) > 0 ? PARENT_1 : PARENT_2;
  }
}
