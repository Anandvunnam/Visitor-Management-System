package com.java.vms.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class VisitDTO {

    private Long id;

    @NotNull
    private VisitStatus visitStatus;

    @NotNull
    private LocalDateTime inTime;

    @NotNull
    private LocalDateTime outTime;

    @NotNull
    @Size(max = 255)
    private String visitorImgUrl;

    @NotNull
    @Size(max = 255)
    private String purpose;

    @NotNull
    private Long user;

    @NotNull
    private Long flat;

    @NotNull
    private Long visitor;

}
