package com.java.vms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.vms.config.TestSecurityConfig;
import com.java.vms.model.*;
import com.java.vms.service.FlatService;
import com.java.vms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("Test")
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
@TestPropertySource(locations = ("classpath:application-test.properties"))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public class AdminControllerTest {

    private UserDTO userDTO;
    private FlatDTO flat;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private FlatService flatService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    ArgumentCaptor<FlatDTO> flatDTOArgumentCaptor;
    @Captor
    ArgumentCaptor<String> flatNumCaptor;
    @Captor
    ArgumentCaptor<Boolean> flatStatusBooleanCaptor;

    @MockitoBean
    @Qualifier("testSecurityFilterChain")
    private SecurityFilterChain securityFilterChain;

    @BeforeEach
    public void setUp(){

        userDTO = UserDTO.builder()
                .name("Anand")
                .email("test@yopmail.com")
                .phone(9381026991L)
                .role(Role.ADMIN)
                .password("pass")
                .line1("Tulasi Nagar")
                .line2("Lingampet Road")
                .city("jagtial")
                .state("Telangana")
                .country("India")
                .pincode(505327)
                .build();

        flat = FlatDTO.builder()
                .flatNum("T-101")
                .flatStatus(FlatStatus.AVAILABLE)
                .build();
    }

    /*@Test
    public void testCreateUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(userDTO);
        //Integer initialRelationSize = repository.findAll().size();
        mockMvc.perform(post("/admin/user").contentType("application/json").content(jsonData)).
                andDo(print()).andExpect(status().isCreated());
        //Integer finalRelationSize = repository.findAll().size();
        User actual = userRepository.findUserByEmail("test@yopmail.com").get();
        assertThat(actual).isEqualTo(user);
    } */

    @Test
    public void testCreateUser() throws Exception {

        given(userService.create(any(UserDTO.class)))
                .willReturn(1L);

        mockMvc.perform(post("/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        verify(userService, times(1)).create(any(UserDTO.class));
    }

    @Test
    public void testCreateUserWhenUserAlreadyExists() throws Exception {

        given(userService.create(any(UserDTO.class)))
                .willThrow(SQLIntegrityConstraintViolationException.class);

            mockMvc.perform(post("/admin/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().is5xxServerError());

        verify(userService, times(1)).create(any(UserDTO.class));
    }

    @Test
    public void testCreateUserWithoutContent() throws Exception {

        given(userService.create(any(UserDTO.class)))
                .willThrow(HandlerMethodValidationException.class);


        mockMvc.perform(post("/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /*@Test
    public void testAddFlat() throws Exception {
        FlatDTO flatDTO = FlatDTO.builder().flatNum("T-101").build();
        ObjectMapper mapper = new ObjectMapper();
        String flatJsonData = mapper.writeValueAsString(flatDTO);
        int initialSize = flatRepository.findAll().size();
        mockMvc.perform(post("/admin/flat").contentType("application/json").content(flatJsonData))
                .andDo(print()).andExpect(status().isCreated());
        int finalSize =flatRepository.findAll().size();
        assertThat(finalSize - initialSize).isEqualTo(1);
    }*/

    @Test
    public void testAddFlat() throws Exception {
        when(flatService.create(any(FlatDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/admin/flat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(flat))
                        )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        verify(flatService, times(1)).create(any(FlatDTO.class));
    }

    /* @Test
    public void testAddFlatWithFlatStatus() throws Exception {
        FlatDTO flatDTO = FlatDTO.builder().flatNum("T-101").flatStatus(FlatStatus.NOTAVAILABLE).build();
        ObjectMapper mapper = new ObjectMapper();
        String flatJsonData = mapper.writeValueAsString(flatDTO);
        int initialSize = flatRepository.findAll().size();
        MvcResult result = mockMvc.perform(post("/admin/flat").contentType("application/json").content(flatJsonData))
                .andDo(print()).andExpect(status().isCreated()).andReturn();
        int finalSize =flatRepository.findAll().size();
        assertThat(finalSize - initialSize).isEqualTo(1);
        Flat flat1 = flatRepository.findById(Long.valueOf(result.getResponse().getContentAsString())).get();
        assertThat(flat1.getFlatStatus()).isEqualTo(FlatStatus.AVAILABLE);
    } */

    @Test
    public void testAddFlatWithFlatStatus() throws Exception {
        flat.setFlatStatus(FlatStatus.NOTAVAILABLE);

        mockMvc.perform(
                post("/admin/flat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flat)))
                .andExpect(status().isCreated())
                .andReturn();

        verify(flatService).create(flatDTOArgumentCaptor.capture());

        assertThat(flat.getFlatStatus()).isEqualTo(flatDTOArgumentCaptor.getValue().getFlatStatus());

    }

    /*@Test
    public void testChangeFlatStatusToUnAvailable() throws Exception {
        //flatRepository.save(flat);
        MvcResult result = mockMvc.perform(put("/admin/changeFlatStatus").contentType("application/json")
                        .param("num",flat.getFlatNum()).param("st",String.valueOf(false)))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString().replace("\"","")).isEqualTo(FlatStatus.NOTAVAILABLE.toString());
    }*/

    @Test
    public void testChangeFlatStatusToUnAvailable() throws Exception {

        mockMvc.perform(put("/admin/changeFlatStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("num",flat.getFlatNum())
                        .param("st",String.valueOf(false))
                        )
                        .andExpect(status().isOk());

        verify(flatService, times(1)).changeFlatStatusToNotAvailable(flatNumCaptor.capture(), flatStatusBooleanCaptor.capture());

        assertThat(flat.getFlatNum()).isEqualTo(flatNumCaptor.getValue());
        assertThat(false).isEqualTo(flatStatusBooleanCaptor.getValue());
    }

    /*@Test
    public void testChangeFlatStatusToAvailable() throws Exception {
        flat.setFlatStatus(FlatStatus.NOTAVAILABLE);
//        flatRepository.save(flat);
        MvcResult result = mockMvc.perform(
                put("/admin/changeFlatStatus")
                        .contentType("application/json")
                        .param("num",flat.getFlatNum())
                        .param("st",String.valueOf(true))
                )
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse()
                .getContentAsString()
                .replace("\"",""))
                .isEqualTo(FlatStatus.AVAILABLE.toString());
    }*/

    @Test
    public void testChangeFlatStatusToAvailable() throws Exception {

        mockMvc.perform(put("/admin/changeFlatStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("num",flat.getFlatNum())
                        .param("st",String.valueOf(true))
                )
                .andExpect(status().isOk());

        verify(flatService, times(1)).changeFlatStatusToNotAvailable(flatNumCaptor.capture(), flatStatusBooleanCaptor.capture());

        assertThat(flat.getFlatNum()).isEqualTo(flatNumCaptor.getValue());
        assertThat(true).isEqualTo(flatStatusBooleanCaptor.getValue());
    }
}
