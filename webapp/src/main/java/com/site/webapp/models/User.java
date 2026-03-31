package com.site.webapp.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Size(min=2,max = 50, message = "Имя должно содержать не менее 2 символов")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min=2,max = 50, message = "Фамилия должна содержать не менее 2 символов")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(length = 255)
    private String originalAvatarFileName;

    @Column(length = 255, unique = true)
    private String storedAvatarFileName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Введите корректный email")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min=4, message = "Пароль должен содержать не менее 4 символов")
    @Column(nullable = false)
    private String password;

    @Transient
    private String passwordConfirm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_favourite_tasks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Task> favouriteTasks = new HashSet<>();

    public Set<Task> getFavouriteTasks() {
        return favouriteTasks;
    }
    public void setFavouriteTasks(Set<Task> favouriteTasks) {
        this.favouriteTasks = favouriteTasks;
    }

    public boolean isTaskFavorite(Long taskId) {
        return favouriteTasks.stream().anyMatch(task -> task.getId().equals(taskId));
    }

    public User() {
    }
    public User(String firstName, String lastName, String email, String password,String originalAvatarFileName,String storedAvatarFileName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.originalAvatarFileName = originalAvatarFileName;
        this.storedAvatarFileName = storedAvatarFileName;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getOriginalAvatarFileName() {
        return originalAvatarFileName;
    }

    public void setOriginalAvatarFileName(String originalAvatarFileName) {
        this.originalAvatarFileName = originalAvatarFileName;
    }

    public String getStoredAvatarFileName() {
        return storedAvatarFileName;
    }

    public void setStoredAvatarFileName(String storedAvatarFileName) {
        this.storedAvatarFileName = storedAvatarFileName;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    @Nonnull
    public String getUsername() {
        return getEmail();
    }

    @Override
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {this.password = password;}

    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }

}
