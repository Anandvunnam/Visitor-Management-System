package com.java.vms.service;

import com.java.vms.domain.Address;
import com.java.vms.domain.Flat;
import com.java.vms.domain.User;
import com.java.vms.model.UserDTO;
import com.java.vms.model.UserStatus;
import com.java.vms.repos.AddressRepository;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final FlatRepository flatRepository;

    private Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserService(final UserRepository userRepository,
            final AddressRepository addressRepository, final FlatRepository flatRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.flatRepository = flatRepository;
    }

    public List<UserDTO> findAll() {
        final List<User> users = userRepository.findAll(Sort.by("id"));
        return users.stream()
                .map(user -> mapToDTO(user, new UserDTO()))
                .toList();
    }

    public UserDTO get(final Long id) {
        return userRepository.findById(id)
                .map(user -> mapToDTO(user, new UserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Long create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        user.setUserStatus(UserStatus.ACTIVE);
        LOGGER.info("New user created");
        return userRepository.save(user).getId();
    }

    public void update(final UserDTO userDTO) {
        final User user = userRepository.findUserByEmail(userDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: " + userDTO.getEmail()));
        mapToEntity(userDTO, user);
        LOGGER.info("User " + user.getEmail() + " details updated.");
        userRepository.save(user);
    }

    public void markUserStatus(final Long id) throws NotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found for ID: " + id));
        if(user.getUserStatus() == UserStatus.ACTIVE){
            user.setUserStatus(UserStatus.INACTIVE);
        }
        else{
            user.setUserStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
        LOGGER.info("User " + user.getName() + " status updated");
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setUserStatus(user.getUserStatus());
        userDTO.setRole(user.getRole());
        Address address = user.getAddress() == null ? null : user.getAddress();
        userDTO.setLine1(address.getLine1());
        userDTO.setLine2(address.getLine2());
        userDTO.setCity(address.getCity());
        userDTO.setState(address.getState());
        userDTO.setCountry(address.getCountry());
        userDTO.setPincode(address.getPincode());
        userDTO.setFlatNum(user.getFlat() == null ? null : user.getFlat().getFlatNum());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
//      No need to set user status
        if(userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        if(userDTO.getLine1() != null || userDTO.getLine2() != null ||
            userDTO.getCity() != null || userDTO.getState() != null ||
            userDTO.getCountry() != null || userDTO.getPincode() != null) {
            Address address = Address.builder().line1(userDTO.getLine1())
                    .line2(userDTO.getLine2())
                    .city(userDTO.getCity())
                    .state(userDTO.getState())
                    .country(userDTO.getCountry())
                    .pincode(userDTO.getPincode()).build();
            addressRepository.save(address);
            user.setAddress(address);
        }
        if(userDTO.getFlatNum() == null) {
            final Flat flat = userDTO.getFlatNum() == null ? null : flatRepository.findByFlatNum(userDTO.getFlatNum())
                    .orElseThrow(() -> new NotFoundException("flat not found"));
            user.setFlat(flat);
        }
        return user;
    }

    public boolean nameExists(final String name) {
        return userRepository.existsByNameIgnoreCase(name);
    }

    public boolean emailExists(final String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean phoneExists(final Long phone) {
        return userRepository.existsByPhone(phone);
    }

}
