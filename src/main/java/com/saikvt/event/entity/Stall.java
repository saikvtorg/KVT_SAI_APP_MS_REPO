package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "stall")
public class Stall {
    @Id
    @Column(name = "stall_id")
    private String stallId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "stall_number")
    private String stallNumber;

    @Column(name = "layout")
    private String layout;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "stall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosterContent> posterContent;

    public Stall() {}

    public String getStallId() {
        return stallId;
    }

    public void setStallId(String stallId) {
        this.stallId = stallId;
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

    public String getStallNumber() {
        return stallNumber;
    }

    public void setStallNumber(String stallNumber) {
        this.stallNumber = stallNumber;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<PosterContent> getPosterContent() {
        return posterContent;
    }

    public void setPosterContent(List<PosterContent> posterContent) {
        this.posterContent = posterContent;
    }
}
