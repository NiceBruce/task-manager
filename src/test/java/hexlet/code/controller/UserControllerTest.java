package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LoginDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtilsForApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtilsForApp.asJson;
import static hexlet.code.utils.TestUtilsForApp.TEST_USERNAME;
import static hexlet.code.utils.TestUtilsForApp.TEST_USERNAME_2;
import static hexlet.code.utils.TestUtilsForApp.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
public class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtilsForApp utils;

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {
        assertEquals(0, userRepository.count());
        utils.regDefaultUser().andExpect(status().isCreated());
        assertEquals(1, userRepository.count());
    }

    @Test
    public void getUserById() throws Exception {
        utils.regDefaultUser();
        final User expectedUser = userRepository.findAll().get(0);
        final var response = utils.perform(
                get(USER_CONTROLLER_PATH + ID, expectedUser.getId()),
                expectedUser.getEmail()
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final User user = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertEquals(expectedUser.getId(), user.getId());
        assertEquals(expectedUser.getEmail(), user.getEmail());
        assertEquals(expectedUser.getFirstName(), user.getFirstName());
        assertEquals(expectedUser.getLastName(), user.getLastName());
    }

    @Test
    public void getAllUsers() throws Exception {
        utils.regDefaultUser();
        final var response = utils.perform(get(USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<User> users = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(users).hasSize(1);
    }

    @Test
    public void twiceRegTheSameUserFail() throws Exception {
        utils.regDefaultUser().andExpect(status().isCreated());
        utils.regDefaultUser().andExpect(status().is(422));

        assertEquals(1, userRepository.count());
    }

    @Test
    public void login() throws Exception {
        utils.regDefaultUser();
        final LoginDto loginDto = new LoginDto(
                utils.getTestRegistrationDto().getEmail(),
                utils.getTestRegistrationDto().getPassword()
        );
        final var loginRequest = post(LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);
        utils.perform(loginRequest).andExpect(status().isOk());
    }

    @Test
    public void loginFail() throws Exception {
        final LoginDto loginDto = new LoginDto(
                utils.getTestRegistrationDto().getEmail(),
                utils.getTestRegistrationDto().getPassword()
        );
        final var loginRequest = post(LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);
        utils.perform(loginRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUser() throws Exception {
        utils.regDefaultUser();

        final Long userId = userRepository.findByEmail(TEST_USERNAME).get().getId();

        final var userDto = new UserDto("new name", "new last name", TEST_USERNAME_2, "new pwd");

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(userDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_USERNAME).andExpect(status().isOk());

        assertTrue(userRepository.existsById(userId));
        assertNull(userRepository.findByEmail(TEST_USERNAME).orElse(null));
        assertNotNull(userRepository.findByEmail(TEST_USERNAME_2).orElse(null));
    }

    @Test
    public void deleteUser() throws Exception {
        utils.regDefaultUser();

        final Long userId = userRepository.findByEmail(TEST_USERNAME).get().getId();
        MockHttpServletRequestBuilder request = delete(USER_CONTROLLER_PATH + ID, userId);

        utils.perform(request, TEST_USERNAME)
                .andExpect(status().isOk());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void deleteUserFails() throws Exception {
        utils.regDefaultUser();
        utils.regUser(new UserDto(
                "fname",
                "lname",
                TEST_USERNAME_2,
                "pwd"
        ));

        final Long userId = userRepository.findByEmail(TEST_USERNAME).get().getId();

        utils.perform(delete(USER_CONTROLLER_PATH + ID, userId), TEST_USERNAME_2)
                .andExpect(status().isForbidden());

        assertEquals(2, userRepository.count());
    }
}
