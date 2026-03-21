package com.example.jira_mini.service.Auth;

import com.example.jira_mini.entity.User;
import com.example.jira_mini.exception.EmailAlreadyExistsException;
import com.example.jira_mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void register(String email, String password, String fullName) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException("Email " + email + " đã được sử dụng. Vui lòng chọn email khác!");
    }
    String passwordHash = passwordEncoder.encode(password);
    User user = User.builder().email(email).passwordHash(passwordHash).fullName(fullName).build();
    userRepository.save(user);
  }
}
