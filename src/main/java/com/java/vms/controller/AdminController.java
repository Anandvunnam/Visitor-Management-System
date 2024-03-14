package com.java.vms.controller;

import com.java.vms.model.FlatDTO;
import com.java.vms.model.FlatStatus;
import com.java.vms.model.UserDTO;
import com.java.vms.service.FlatService;
import com.java.vms.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private FlatService flatService;

    @PostMapping("/user")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUser(@RequestBody @Valid final UserDTO userDTO) {
        final Long createdId = userService.create(userDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/changeUserStatus/{id}")
    public ResponseEntity<Long> changeUserStatus(@PathVariable(name = "id") final Long id) {
        userService.markUserStatus(id);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/modifyUserDetails")
    public ResponseEntity<Void> updateUser(@RequestBody @Valid final UserDTO userDTO) {
        userService.update(userDTO);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/flat")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createFlat(@RequestBody @Valid final FlatDTO flatDTO) {
        final Long createdId = flatService.create(flatDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/changeFlatStatus")
    public ResponseEntity<FlatStatus> markFlatStatus(@RequestParam(name = "num") @Valid final String flatNum,
                                                     @RequestParam(name = "st") @Valid final boolean status){
        FlatStatus flatStatus = flatService.changeFlatStatusToNotAvailable(flatNum, status);
        // TODO : 1. Need to change way of handling when only one rq param is received.
        return ResponseEntity.ok(flatStatus);
    }

}
