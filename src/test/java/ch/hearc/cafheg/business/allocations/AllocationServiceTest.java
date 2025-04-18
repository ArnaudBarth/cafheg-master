package ch.hearc.cafheg.business.allocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class AllocationServiceTest {

  private AllocationService allocationService;

  private AllocataireMapper allocataireMapper;
  private AllocationMapper allocationMapper;
  private VersementMapper versementMapper;

  @BeforeEach
  void setUp() {
    allocataireMapper = Mockito.mock(AllocataireMapper.class);
    allocationMapper = Mockito.mock(AllocationMapper.class);
    versementMapper = Mockito.mock(VersementMapper.class);

    allocationService = new AllocationService(allocataireMapper, allocationMapper, versementMapper);
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
  void testParent1ALSeulement() {
    ParentAllocationRequest request = new ParentAllocationRequest(
            "", true, "", false, "", false, BigDecimal.valueOf(5000), BigDecimal.valueOf(0)
    );

    AllocationService service = new AllocationService(null, null, null);
    String result = service.getParentDroitAllocation(request);
    assertEquals("Parent1", result);
  }

  @Test
  void testParent2ALSeulement() {
    ParentAllocationRequest request = new ParentAllocationRequest(
            "", false, "", true, "", false, BigDecimal.valueOf(0), BigDecimal.valueOf(4000)
    );

    AllocationService service = new AllocationService(null, null, null);
    String result = service.getParentDroitAllocation(request);
    assertEquals("Parent2", result);
  }

  @Test
  void testDeuxParentsAL_SalaireParent1PlusGrand() {
    ParentAllocationRequest request = new ParentAllocationRequest(
            "", true, "", true, "", true, BigDecimal.valueOf(8000), BigDecimal.valueOf(4000)
    );

    AllocationService service = new AllocationService(null, null, null);
    String result = service.getParentDroitAllocation(request);
    assertEquals("Parent1", result);
  }

  @Test
  void testDeuxParentsAL_SalaireParent2PlusGrand() {
    ParentAllocationRequest request = new ParentAllocationRequest(
            "", true, "", true, "", false, BigDecimal.valueOf(2000), BigDecimal.valueOf(4000)
    );

    AllocationService service = new AllocationService(null, null, null);
    String result = service.getParentDroitAllocation(request);
    assertEquals("Parent2", result);
  }

}