package ch.hearc.cafheg.business.allocations;

public class ParentAllocationRequest {

    private String enfantResidence;
    private Boolean parent1ActiviteLucrative;
    private String parent1Residence;
    private Boolean parent2ActiviteLucrative;
    private String parent2Residence;
    private Boolean parentsEnsemble;
    private Number parent1Salaire;
    private Number parent2Salaire;

    // ✅ Constructeur vide (obligatoire pour Spring)
    public ParentAllocationRequest() {
    }

    // ✅ Constructeur complet (utile pour les tests unitaires)
    public ParentAllocationRequest(String enfantResidence, Boolean parent1ActiviteLucrative,
                                   String parent1Residence, Boolean parent2ActiviteLucrative,
                                   String parent2Residence, Boolean parentsEnsemble,
                                   Number parent1Salaire, Number parent2Salaire) {
        this.enfantResidence = enfantResidence;
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent1Residence = parent1Residence;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parent2Residence = parent2Residence;
        this.parentsEnsemble = parentsEnsemble;
        this.parent1Salaire = parent1Salaire;
        this.parent2Salaire = parent2Salaire;
    }

    // ✅ Getters
    public String getEnfantResidence() {
        return enfantResidence;
    }

    public Boolean getParent1ActiviteLucrative() {
        return parent1ActiviteLucrative;
    }

    public String getParent1Residence() {
        return parent1Residence;
    }

    public Boolean getParent2ActiviteLucrative() {
        return parent2ActiviteLucrative;
    }

    public String getParent2Residence() {
        return parent2Residence;
    }

    public Boolean getParentsEnsemble() {
        return parentsEnsemble;
    }

    public Number getParent1Salaire() {
        return parent1Salaire;
    }

    public Number getParent2Salaire() {
        return parent2Salaire;
    }

    // ✅ Setters
    public void setEnfantResidence(String enfantResidence) {
        this.enfantResidence = enfantResidence;
    }

    public void setParent1ActiviteLucrative(Boolean parent1ActiviteLucrative) {
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
    }

    public void setParent1Residence(String parent1Residence) {
        this.parent1Residence = parent1Residence;
    }

    public void setParent2ActiviteLucrative(Boolean parent2ActiviteLucrative) {
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
    }

    public void setParent2Residence(String parent2Residence) {
        this.parent2Residence = parent2Residence;
    }

    public void setParentsEnsemble(Boolean parentsEnsemble) {
        this.parentsEnsemble = parentsEnsemble;
    }

    public void setParent1Salaire(Number parent1Salaire) {
        this.parent1Salaire = parent1Salaire;
    }

    public void setParent2Salaire(Number parent2Salaire) {
        this.parent2Salaire = parent2Salaire;
    }
}
