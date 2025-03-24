package com.java.vms.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.vms.model.*;
import com.java.vms.service.FlatService;
import com.java.vms.service.UserService;
import com.java.vms.service.VisitService;
import java.sql.SQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    private UserDTO userDTO;
    private FlatDTO flat;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private FlatService flatService;
    @MockitoBean
    private VisitService visitService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Captor
    ArgumentCaptor<UserDTO> userDTOArgumentCaptor;
    @Captor
    ArgumentCaptor<FlatDTO> flatDTOArgumentCaptor;
    @Captor
    ArgumentCaptor<String> flatNumCaptor;
    @Captor
    ArgumentCaptor<Boolean> flatStatusBooleanCaptor;

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

    @Test
    public void testCreateUser() throws Exception {

        given(userService.create(any(UserDTO.class)))
                .willReturn(1L);

        mockMvc.perform(post("/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        verify(userService, times(1)).create(userDTOArgumentCaptor.capture());

        assertThat(userDTOArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(userDTO);
    }

    @Test
    public void testCreateUserWhenUserAlreadyExists() throws Exception {

        given(userService.create(any(UserDTO.class)))
                .willThrow(SQLIntegrityConstraintViolationException.class);

        mockMvc.perform(post("/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).create(userDTOArgumentCaptor.capture());

        assertThat(userDTOArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(userDTO);
    }

    @Test
    public void testCreateUserWithoutContent() throws Exception {
        mockMvc.perform(post("/admin/user")
             .contentType(MediaType.APPLICATION_JSON)
             .content("{}"))
             .andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.exception")
             .value(MethodArgumentNotValidException.class.getSimpleName()));

        verify(userService, times(0)).create(any(UserDTO.class));
    }

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

        verify(flatService, times(1)).create(flatDTOArgumentCaptor.capture());

        assertThat(flatDTOArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(flat);
    }

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

    @Test
    public void testChangeFlatStatusToUnAvailable() throws Exception {

        mockMvc.perform(put("/admin/changeFlatStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("num",flat.getFlatNum())
                        .param("st",String.valueOf(false))
                        )
                        .andExpect(status().isNoContent());

        verify(flatService, times(1)).changeFlatStatus(flatNumCaptor.capture(), flatStatusBooleanCaptor.capture());

        assertThat(flat.getFlatNum()).isEqualTo(flatNumCaptor.getValue());
        assertThat(false).isEqualTo(flatStatusBooleanCaptor.getValue());
    }

    @Test
    public void testChangeFlatStatusToAvailable() throws Exception {

        mockMvc.perform(put("/admin/changeFlatStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("num",flat.getFlatNum())
                        .param("st",String.valueOf(true))
                )
                .andExpect(status().isNoContent());

        verify(flatService, times(1)).changeFlatStatus(flatNumCaptor.capture(), flatStatusBooleanCaptor.capture());

        assertThat(flat.getFlatNum()).isEqualTo(flatNumCaptor.getValue());
        assertThat(true).isEqualTo(flatStatusBooleanCaptor.getValue());
    }
}
