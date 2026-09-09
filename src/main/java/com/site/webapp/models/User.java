package com.site.webapp.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Size(min=2,max = 50, message = "Имя должно содержать не менее 2 символов")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min=2,max = 50, message = "Фамилия должна содержать не менее 2 символов")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Введите корректный email")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min=4, message = "Пароль должен содержать не менее 4 символов")
    @Column(nullable = false)
    private String password;

    @Transient
    private String passwordConfirm;

    @Column(length = 255)
    private String originalAvatarFileName;

    @Column(length = 512, unique = true)
    private String avatarS3Key;

    @Column(nullable = false)
    private boolean deleted = false;

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
    private Set<Task> favouriteTasks = new HashSet<>();

    // Constructors

    public User() {
    }

    public User(String firstName, String lastName, String email, String password,String originalAvatarFileName,String avatarS3Key) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.originalAvatarFileName = originalAvatarFileName;
        this.avatarS3Key = avatarS3Key;
    }

    // Logic
    public boolean isTaskFavorite(Long taskId) {
        return favouriteTasks.stream().anyMatch(task -> task.getId().equals(taskId));
    }

    // Getters and Setters

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

    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();

        return full.isEmpty() ? email : full;
    }


    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getOriginalAvatarFileName() {
        return originalAvatarFileName;
    }
    public void setOriginalAvatarFileName(String originalAvatarFileName) {
        this.originalAvatarFileName = originalAvatarFileName;
    }

    public String getAvatarS3Key() {
        return avatarS3Key;
    }
    public void setAvatarS3Key(String avatarS3Key) {
        this.avatarS3Key = avatarS3Key;
    }

    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Task> getFavouriteTasks() {
        return favouriteTasks;
    }
    public void setFavouriteTasks(Set<Task> favouriteTasks) {
        this.favouriteTasks = favouriteTasks;
    }

    // UserDetails Overrides
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
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return !deleted;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Base Method Overrides

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
