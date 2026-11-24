package com.noviaralabs.ledger.service;

import com.noviaralabs.ledger.dto.Responses.UserDto;
import com.noviaralabs.ledger.entity.AppUser;
import com.noviaralabs.ledger.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public UserDto login(String mobile) {
        AppUser u = users.findByMobileNumber(clean(mobile))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(u);
    }

    @Transactional
    public UserDto register(String mobile, String name) {
        String m = clean(mobile);
        if (users.findByMobileNumber(m).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mobile number already registered");
        }
        AppUser u = new AppUser();
        u.setMobileNumber(m);
        u.setName(name.trim());
        return toDto(users.save(u));
    }

    private static String clean(String s) { return s.replaceAll("\\s+", ""); }

    private static UserDto toDto(AppUser u) { return new UserDto(u.getId(), u.getName(), u.getMobileNumber()); }
}
