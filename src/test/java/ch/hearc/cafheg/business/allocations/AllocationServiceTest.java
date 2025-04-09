package ch.hearc.cafheg.business.allocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AllocationServiceTest {

  private AllocationService allocationService;

  private AllocataireMapper allocataireMapper;
  private AllocationMapper allocationMapper;

  @BeforeEach
  void setUp() {
    allocataireMapper = Mockito.mock(AllocataireMapper.class);
    allocationMapper = Mockito.mock(AllocationMapper.class);

    allocationService = new AllocationService(allocataireMapper, allocationMapper);
  }

  @Test
  void findAllAllocataires_GivenEmptyAllocataires_ShouldBeEmpty() {
    Mockito.when(allocataireMapper.findAll("Geiser")).thenReturn(Collections.emptyList());
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertThat(all).isEmpty();
  }

  @Test
  void findAllAllocataires_Given2Geiser_ShouldBe2() {
    Mockito.when(allocataireMapper.findAll("Geiser"))
        .thenReturn(Arrays.asList(new Allocataire(new NoAVS("1000-2000"), "Geiser", "Arnaud"),
            new Allocataire(new NoAVS("1000-2001"), "Geiser", "Aurélie")));
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getNoAVS()).isEqualTo(new NoAVS("1000-2000")),
        () -> assertThat(all.get(0).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(0).getPrenom()).isEqualTo("Arnaud"),
        () -> assertThat(all.get(1).getNoAVS()).isEqualTo(new NoAVS("1000-2001")),
        () -> assertThat(all.get(1).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(1).getPrenom()).isEqualTo("Aurélie"));
  }

  @Test
  void findAllocationsActuelles() {
    Mockito.when(allocationMapper.findAll())
        .thenReturn(Arrays.asList(new Allocation(new Montant(new BigDecimal(1000)), Canton.NE,
            LocalDate.now(), null), new Allocation(new Montant(new BigDecimal(2000)), Canton.FR,
            LocalDate.now(), null)));
    List<Allocation> all = allocationService.findAllocationsActuelles();
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getMontant()).isEqualTo(new Montant(new BigDecimal(1000))),
        () -> assertThat(all.get(0).getCanton()).isEqualTo(Canton.NE),
        () -> assertThat(all.get(0).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(0).getFin()).isNull(),
        () -> assertThat(all.get(1).getMontant()).isEqualTo(new Montant(new BigDecimal(2000))),
        () -> assertThat(all.get(1).getCanton()).isEqualTo(Canton.FR),
        () -> assertThat(all.get(1).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(1).getFin()).isNull());
  }

  @Test
  void getParentDroitAllocation_parent1SeulActif_shouldReturnParent1() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(false);
    demande.setParent1Salaire(BigDecimal.valueOf(4000));
    demande.setParent2Salaire(BigDecimal.valueOf(2000));
    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent1", result);
  }

  @Test
  void getParentDroitAllocation_parent2SeulActif_shouldReturnParent2() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(false);
    demande.setParent2ActiviteLucrative(true);
    demande.setParent1Salaire(BigDecimal.valueOf(4000));
    demande.setParent2Salaire(BigDecimal.valueOf(2000));
    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent2", result);
  }

  @Test
  void getParentDroitAllocation_deuxActifs_parent1SalairePlusHaut_shouldReturnParent1() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setParent1Salaire(BigDecimal.valueOf(5000));
    demande.setParent2Salaire(BigDecimal.valueOf(3000));
    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent1", result);
  }

  @Test
  void getParentDroitAllocation_deuxActifs_parent2SalairePlusHaut_shouldReturnParent2() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setParent1Salaire(BigDecimal.valueOf(3000));
    demande.setParent2Salaire(BigDecimal.valueOf(5000));
    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent2", result);
  }

  @Test
  void getParentDroitAllocation_deuxActifs_salaireEgal_shouldReturnParent2ParDefaut() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setParent1Salaire(BigDecimal.valueOf(4000));
    demande.setParent2Salaire(BigDecimal.valueOf(4000));
    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent2", result); // Comportement actuel : retourne P2 si égalité
  }

  @Test
  void deuxActifs_enfantChezParent1_shouldReturnParent1() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setEnfantResidence("NE");
    demande.setParent1Residence("NE");
    demande.setParent2Residence("BE");
    demande.setParent1Salaire(BigDecimal.valueOf(4000));
    demande.setParent2Salaire(BigDecimal.valueOf(6000)); // pour vérifier que le salaire ne compte pas encore

    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent1", result);
  }

  @Test
  void deuxActifs_enfantHabiteAilleurs_parent2TravailleDansSonCanton_shouldReturnParent2() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setEnfantResidence("FR");             // Fribourg
    demande.setParent1Residence("NE");            // Neuchâtel
    demande.setParent2Residence("FR");            // Parent2 travaille dans le bon canton
    demande.setParent1Salaire(BigDecimal.valueOf(7000));
    demande.setParent2Salaire(BigDecimal.valueOf(3000));

    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent2", result);
  }

  @Test
  void deuxActifs_pasDeLienAvecEnfant_salairePlusHautParent1_shouldReturnParent1() {
    DemandeAllocation demande = new DemandeAllocation();
    demande.setParent1ActiviteLucrative(true);
    demande.setParent2ActiviteLucrative(true);
    demande.setEnfantResidence("FR");
    demande.setParent1Residence("GE");
    demande.setParent2Residence("NE");
    demande.setParent1Salaire(BigDecimal.valueOf(8000));
    demande.setParent2Salaire(BigDecimal.valueOf(4000));

    String result = allocationService.getParentDroitAllocation(demande);
    assertEquals("Parent1", result);
  }
}