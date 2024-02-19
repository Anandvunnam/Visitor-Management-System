package com.java.vms.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    private Long phone;

    @NotNull
    private UserStatus userStatus;

    @NotNull
    private Role role;

    @NotNull
    private Long address;

    private Long flat;

}
