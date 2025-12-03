package com.samson.restbackend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samson.restbackend.util.UserRole;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    },
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "user_id", updatable = false, nullable = false, unique = true, columnDefinition = "BIGINT UNSIGNED")
    private UUID user_id;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    @ToString.Include
    private String username;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    @ToString.Include
    private String email;

    @Column(name = "hashed_password", nullable = false, length = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private OffsetDateTime created_at;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private OffsetDateTime updated_at;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer version;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole userRole;
}