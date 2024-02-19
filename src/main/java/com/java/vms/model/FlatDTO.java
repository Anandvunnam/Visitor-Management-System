package com.java.vms.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FlatDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String flatNum;

    @NotNull
    private FlatStatus flatStatus;

}
