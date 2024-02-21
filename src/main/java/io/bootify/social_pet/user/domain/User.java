package io.bootify.social_pet.user.domain;

import io.bootify.social_pet.photo.domain.Photo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String numeroTlf;

    @Column(nullable = false)
    private String contraseA;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @ManyToMany
    @JoinTable(
            name = "Follower",
            joinColumns = @JoinColumn(name = "followerUsersId"),
            inverseJoinColumns = @JoinColumn(name = "followedUsersId")
    )
    private Set<User> followerUsers;

    @ManyToMany(mappedBy = "followerUsers")
    private Set<User> followedUsers;

    @OneToMany(mappedBy = "user")
    private Set<Photo> userPhotos;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
