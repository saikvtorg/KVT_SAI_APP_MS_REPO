package com.saikvt.event.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "module")
public class Module {
    @Id
    @Column(name = "module_id")
    private String moduleId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "assigned_team_id")
    private String assignedTeamId;

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @JsonBackReference
    private Exhibition exhibition;

    // Expose exhibitionId in JSON without duplicating DB mapping. Marked not insertable/updatable to avoid conflicts with the relationship.
    @JsonProperty("exhibitionId")
    @Column(name = "exhibition_id", insertable = false, updatable = false)
    private String exhibitionId;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stall> stalls;

    public Module() {}

    public Module(String moduleId, String name, String description, String assignedTeamId) {
        this.moduleId = moduleId;
        this.name = name;
        this.description = description;
        this.assignedTeamId = assignedTeamId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(String assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
    }

    public Exhibition getExhibition() {
        return exhibition;
    }

    public void setExhibition(Exhibition exhibition) {
        this.exhibition = exhibition;
    }

    public String getExhibitionId() {
        // prefer explicit exhibitionId column value if present, otherwise derive from relation
        if (exhibitionId != null && !exhibitionId.isEmpty()) return exhibitionId;
        if (exhibition != null) return exhibition.getExhibitionId();
        return null;
    }

    // no setter for exhibitionId (read-only)

    public List<Stall> getStalls() {
        return stalls;
    }

    public void setStalls(List<Stall> stalls) {
        this.stalls = stalls;
    }
}