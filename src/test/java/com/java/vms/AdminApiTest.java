package com.java.vms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.vms.domain.Address;
import com.java.vms.domain.User;
import com.java.vms.model.Role;
import com.java.vms.model.UserDTO;
import com.java.vms.model.UserStatus;
import com.java.vms.repos.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = ("classpath:application-test.properties"))
@AutoConfigureMockMvc
@EnableAutoConfiguration
public class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;
    private UserDTO userDTO;
    private User user;
    private Address address;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        address = Address.builder().id(1L).line1("Tulasi Nagar")
                .line2("Lingampet Road").city("jagtial").state("Telangana").country("India").pincode(505327).dateCreated(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();

        user = new User(1L, "Anand", "test@yopmail.com",
                9381026991L, UserStatus.ACTIVE, Role.ADMIN, address, null, OffsetDateTime.now(),OffsetDateTime.now());

        userDTO = UserDTO.builder().id(1L).name("Anand").email("test@yopmail.com").phone(9381026991L).role(Role.ADMIN)
                .line1("Tulasi Nagar").line2("Lingampet Road").city("jagtial").state("Telangana").country("India")
                .pincode(505327).build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(userDTO);
        //Integer initialRelationSize = repository.findAll().size();
        mockMvc.perform(post("/admin/user").contentType("application/json").content(jsonData)).
                andDo(print()).andExpect(status().isCreated());
        //Integer finalRelationSize = repository.findAll().size();
        User actual = userRepository.findUserByEmail("test@yopmail.com").get();
        assertThat(actual).isEqualTo(user);
    }

}
