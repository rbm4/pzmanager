package com.apocalipsebr.zomboid.server.manager.domain.entity;
import jakarta.persistence.*;



@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String author;

    @Column(name = "answered_id")
    private Long answeredID;

    @Column(nullable = false)
    private Boolean viewed = false;

    public Ticket() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getAnsweredID() {
        return answeredID;
    }

    public void setAnsweredID(Long answeredID) {
        this.answeredID = answeredID;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }
}