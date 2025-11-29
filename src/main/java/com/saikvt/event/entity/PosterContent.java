package com.saikvt.event.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "poster_content")
public class PosterContent {
    @Id
    @Column(name = "content_id")
    private String contentId;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "poster_media_url")
    private String posterMediaUrl;

    @Column(name = "content_text")
    private String contentText;

    @ManyToOne
    @JoinColumn(name = "stall_id")
    private Stall stall;

    public PosterContent() {}

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getPosterMediaUrl() {
        return posterMediaUrl;
    }

    public void setPosterMediaUrl(String posterMediaUrl) {
        this.posterMediaUrl = posterMediaUrl;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Stall getStall() {
        return stall;
    }

    public void setStall(Stall stall) {
        this.stall = stall;
    }
}