package com.staybook.api;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.staybook.api.entity.User;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.entity.Role;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("johnpassword")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        userRepository.save(user);

        var result = userRepository.findByEmail("johndoe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("johndoe@example.com");
    }
}
