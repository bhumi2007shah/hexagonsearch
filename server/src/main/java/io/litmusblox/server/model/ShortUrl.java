package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "SHORT_URL")
public class ShortUrl implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URL", nullable = false, unique = false)
    private String url;

    @Column(name = "HASH", nullable = false, unique = true)
    private String hash;

    @Column(name = "SHORT_URL", nullable = false, unique = true)
    private String shortUrl;

    @JsonIgnore
    @CreatedDate
    @Column(name="CREATED_ON")
    private Date createdOn;

    @Override
    public String toString() {
        return "Link{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", hash='" + hash + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }

    public ShortUrl(String url) {
        this.url = url;
    }

    @PrePersist
    void setCreatedOn(){
        this.createdOn = new Date();
    }

}
