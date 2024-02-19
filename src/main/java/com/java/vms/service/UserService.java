package com.java.vms.service;

import com.java.vms.domain.Address;
import com.java.vms.domain.Flat;
import com.java.vms.domain.User;
import com.java.vms.model.UserDTO;
import com.java.vms.repos.AddressRepository;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final FlatRepository flatRepository;

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

    public Long create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        return userRepository.save(user).getId();
    }

    public void update(final Long id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    public void delete(final Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setUserStatus(user.getUserStatus());
        userDTO.setRole(user.getRole());
        userDTO.setAddress(user.getAddress() == null ? null : user.getAddress().getId());
        userDTO.setFlat(user.getFlat() == null ? null : user.getFlat().getId());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setUserStatus(userDTO.getUserStatus());
        user.setRole(userDTO.getRole());
        final Address address = userDTO.getAddress() == null ? null : addressRepository.findById(userDTO.getAddress())
                .orElseThrow(() -> new NotFoundException("address not found"));
        user.setAddress(address);
        final Flat flat = userDTO.getFlat() == null ? null : flatRepository.findById(userDTO.getFlat())
                .orElseThrow(() -> new NotFoundException("flat not found"));
        user.setFlat(flat);
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
