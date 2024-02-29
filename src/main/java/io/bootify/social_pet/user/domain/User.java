package io.bootify.social_pet.user.domain;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.util.WebAdvice;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String numeroTlf;

    @Column(nullable = false)
    private String contrasea;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Follower",
            joinColumns = @JoinColumn(name = "followerUsersId"),
            inverseJoinColumns = @JoinColumn(name = "followedUsersId")
    )
    private Set<User> followerUsers;

    @ManyToMany(mappedBy = "followerUsers",fetch = FetchType.EAGER)
    private Set<User> followedUsers;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Photo> userPhotos;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
