package com.java.vms.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.java.vms.controller.AdminController;
import com.java.vms.domain.Address;
import com.java.vms.domain.Flat;
import com.java.vms.domain.User;
import com.java.vms.model.*;
import com.java.vms.repos.AddressRepository;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.util.NotFoundException;
import com.java.vms.util.RedisCacheUtil;
import java.util.Objects;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

/***
 *
 * @project Visitor-Management-System
 * @author anvunnam on 19-03-2025.
 ***/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser
public class AdminControllerIT {

     private UserDTO userDTO = UserDTO.builder()
             .name("TestUser")
             .email("testing@email.com")
             .password("testPassword")
             .phone(1234567890L)
             .flatNum(null)
             .line1("Naruto Street")
             .city("Kyoto")
             .state("Kansai")
             .country("Japan")
             .role(Role.RESIDENT)
             .pincode(505327)
             .build();

     private FlatDTO flatDTO = FlatDTO.builder()
             .flatNum("TestFlat")
             .flatStatus(FlatStatus.AVAILABLE)
             .build();

     @Autowired
     private AdminController adminController;

     @Autowired
     private UserRepository userRepository;

     @Autowired
     private FlatRepository flatRepository;

     @Autowired
     private AddressRepository addressRepository;
     
     @Autowired
     private RedisCacheUtil redisCacheUtil;

     private final Address address =
             Address.builder()
                  .line1(userDTO.getLine1())
                  .line2(userDTO.getLine2())
                  .city(userDTO.getCity())
                  .state(userDTO.getState())
                  .country(userDTO.getCountry())
                  .pincode(userDTO.getPincode())
                  .build();

     private final Flat flat =
             Flat.builder()
                     .flatNum(flatDTO.getFlatNum())
                     .flatStatus(flatDTO.getFlatStatus())
                     .build();

     private final User user =
                  User.builder()
                          .name(userDTO.getName())
                          .email(userDTO.getEmail())
                          .password(userDTO.getPassword())
                          .phone(userDTO.getPhone())
                          .role(userDTO.getRole())
                          .userStatus(UserStatus.ACTIVE)
                          .flat(flat)
                          .address(address)
                          .build();
     
     private void setUp(){
         addressRepository.save(address);
         flatRepository.save(flat);
         userRepository.save(user);
     }

     @AfterEach
     public void tearDown(){
          userRepository.deleteAll();
          flatRepository.deleteAll();
     }

     @Test
     public void testCreateUserIT() throws BadRequestException {
          userDTO.setUserStatus(UserStatus.INACTIVE);

          ResponseEntity<Void> responseEntity = adminController.createUser(userDTO);

          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
          assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

          Long createdUserId = Long.valueOf(Objects
                  .requireNonNull(responseEntity.getHeaders().getLocation())
                  .getPath()
                  .split("/")[3]);

          User user = userRepository.findById(createdUserId).get();
          assertThat(user).isNotNull();
          assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
     }

     @Test
     public void testCreateUserWhenUserAlreadyExitsIT(){
          setUp();
          assertThrows(BadRequestException.class, () -> {
               adminController.createUser(userDTO);
          });
     }

     @Test
     public void testChangeUserStatusIt(){
          setUp();
          User user = userRepository.findUserByEmail(userDTO.getEmail()).get();
          ResponseEntity<Void> responseEntity = adminController.changeUserStatus(user.getId());

          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
          User updatedUser = userRepository.findUserByEmail(userDTO.getEmail()).get();
          assertThat(userDTO.getUserStatus()).isNotEqualTo(updatedUser.getUserStatus());
     }

     @Test
     public void testUpdateUser(){
          setUp();
          userDTO.setName("TestUser-2");
          ResponseEntity<Void> responseEntity = adminController.updateUser(userDTO);
          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
          assertThat(responseEntity.hasBody()).isEqualTo(false);
          User userUpdated = userRepository.findUserByEmail(userDTO.getEmail()).get();
          assertThat(userDTO.getName()).isEqualTo(userUpdated.getName());

     }

     @Test
     public void testUpdateUserWhenUserNotFound(){
          userDTO.setName("TestUser-2");
          assertThrows( NotFoundException.class, () -> {
               adminController.updateUser(userDTO);
          });
     }

     @Test
     public void testCreateFlat() throws BadRequestException {
          ResponseEntity<Void> responseEntity = adminController.createFlat(flatDTO);

          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
          assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

          Long createdFlatId = Long.valueOf(responseEntity.getHeaders().getLocation().getPath().split("/")[3]);

          Flat savedFlat = flatRepository.findById(createdFlatId).get();

          assertThat(savedFlat).isNotNull();

          Flat cachedRedisFlat = (Flat) redisCacheUtil.getValueFromRedisCache(savedFlat.getFlatNum());

          assertThat(cachedRedisFlat).isNotNull();
     }

     @Test
     public void testCreateFlatWhenFlatAlreadyExits(){
          setUp();
          assertThrows(BadRequestException.class, () -> {
               adminController.createFlat(flatDTO);
          });
     }

     @Test
     public void testMarkFlatStatusToNotAvailable(){
          setUp();

          ResponseEntity<Void> responseEntity = adminController.changeFlatStatus(flatDTO.getFlatNum(), false);

          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
          assertThat(responseEntity.hasBody()).isEqualTo(false);

          Flat updatedFlat = flatRepository.findByFlatNum(flatDTO.getFlatNum()).get();

          assertThat(updatedFlat.getFlatStatus()).isEqualTo(FlatStatus.NOTAVAILABLE);

          Flat cachedRedisFlat = (Flat) redisCacheUtil.getValueFromRedisCache(updatedFlat.getFlatNum());

          assertThat(cachedRedisFlat).isNotNull();
          assertThat(cachedRedisFlat.getFlatStatus()).isEqualTo(FlatStatus.NOTAVAILABLE);

     }

     @Test
     public void testMarkFlatStatusToAvailable(){

          flatDTO.setFlatStatus(FlatStatus.NOTAVAILABLE);
          setUp();

          ResponseEntity<Void> responseEntity = adminController.changeFlatStatus(flatDTO.getFlatNum(), true);

          assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
          assertThat(responseEntity.hasBody()).isEqualTo(false);

          Flat updatedFlat = flatRepository.findByFlatNum(flatDTO.getFlatNum()).get();

          assertThat(updatedFlat.getFlatStatus()).isEqualTo(FlatStatus.AVAILABLE);

          Flat cachedRedisFlat = (Flat) redisCacheUtil.getValueFromRedisCache(updatedFlat.getFlatNum());

          assertThat(cachedRedisFlat).isNotNull();
          assertThat(cachedRedisFlat.getFlatStatus()).isEqualTo(FlatStatus.AVAILABLE);

     }
}
