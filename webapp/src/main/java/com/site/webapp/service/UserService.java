package com.site.webapp.service;

import com.site.webapp.config.EncoderConfig;
import com.site.webapp.models.Role;
import com.site.webapp.models.User;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.UserRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,RoleRepository roleRepository,PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(email);

        if (user == null){
            throw new UsernameNotFoundException("User not found" + email);
        }
        return user;
    }

    public User findUserById(Long userId){
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
//        return userFromDb.orElse(null);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public List<User> allUsers(){
        return userRepository.findAll();
    }
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    public boolean saveUser(User user){
        User userFromDB = userRepository.findByEmail(user.getEmail());
        if (userFromDB != null){
            return false;
        }
        Role employeeRole = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Роль EMPLOYEE не найдена"));
        user.setRoles(Collections.singleton(employeeRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }
    public boolean deleteUser(Long userId){
        if (userRepository.findById(userId).isPresent()){
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public void updateUserRoles(Long userId, List<Long> roleIds) {
        User user = findUserById(userId);
        if(user != null){
            Set<Role> newRoles = new HashSet<>();
            if (roleIds != null && !roleIds.isEmpty()){
                for(Long roleId : roleIds){
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if(role != null){
                        newRoles.add(role);
                    }
                }
            }
            user.setRoles(newRoles);
            userRepository.save(user);
        }
    }
}
