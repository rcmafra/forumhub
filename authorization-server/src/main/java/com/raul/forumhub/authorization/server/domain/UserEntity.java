package com.raul.forumhub.authorization.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;


@Entity(name = "users")
@Table(schema = "hub", name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    public boolean getIsAccountNonExpired() {
        return isAccountNonExpired;
    }

    public boolean getIsAccountNonLocked() {
        return isAccountNonLocked;
    }

    public boolean getIsCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

}
