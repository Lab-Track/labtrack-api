package com.labtrack.labtrack.security;

import com.labtrack.labtrack.model.Technician;
import com.labtrack.labtrack.repository.TechnicianRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TechnicianUserDetailsService implements UserDetailsService {

    private final TechnicianRepository technicianRepository;

    public TechnicianUserDetailsService(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        Technician technician = technicianRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Technician not found: " + login));

        return User.withUsername(technician.getLogin())
                .password(technician.getPasswordHash())
                .authorities("ROLE_TECHNICIAN")
                .build();
    }
}
