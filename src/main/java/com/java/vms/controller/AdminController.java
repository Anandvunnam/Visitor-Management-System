package com.java.vms.controller;

import com.java.vms.model.FlatDTO;
import com.java.vms.model.UserDTO;
import com.java.vms.model.VisitDTO;
import com.java.vms.service.FlatService;
import com.java.vms.service.UserService;
import com.java.vms.service.VisitService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private FlatService flatService;

    @PostMapping("/user")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Void> createUser
            (@RequestBody @Valid final UserDTO userDTO) throws BadRequestException {
        final Long createdId;
        try {
            createdId = userService.create(userDTO);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BadRequestException("User details already exists!");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/admin/user/"+ createdId);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping("/changeUserStatus/{id}")
    public ResponseEntity<Void> changeUserStatus
            (@PathVariable(name = "id") final Long id)
    {
        userService.markUserStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modifyUserDetails")
    public ResponseEntity<Void> updateUser
            (@RequestBody @Valid final UserDTO userDTO)
    {
        userService.update(userDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/flat")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Void> createFlat
            (@RequestBody @Valid final FlatDTO flatDTO)
            throws BadRequestException
    {
        final Long createdId;
        try{
            createdId = flatService.create(flatDTO);
        } catch (SQLIntegrityConstraintViolationException e){
            throw new BadRequestException("Flat is already present!");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/admin/flat/" + createdId);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping("/changeFlatStatus")
    public ResponseEntity<Void> changeFlatStatus
            (@RequestParam(name = "num") @Valid final String flatNum,
             @RequestParam(name = "st") @Valid final boolean status)
    {
        flatService.changeFlatStatus(flatNum, status);
        // TODO : 1. Need to change way of handling when only one rq param is received.
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/uploadUserData")
    public ResponseEntity<List<String>> uploadUserData
            (@RequestParam MultipartFile file)
    {
        return ResponseEntity.ok(userService.createUsersFromFile(file));
    }

    @GetMapping("/generate-visit-report")
    public ResponseEntity<byte[]> generateVisitReport
            (@RequestParam String fromDate,
            @RequestParam String toDate)
            throws BadRequestException
    {
        //TODO: Move Business logic to Service Layer
        LocalDateTime lclFromDate;
        LocalDateTime lclToDate;

        try {
            lclFromDate = LocalDate.parse(fromDate).atStartOfDay();
            lclToDate = LocalDate.parse(toDate).atStartOfDay();
        }
        catch (DateTimeParseException e){
            throw new BadRequestException("Invalid to/from dates");
        }
        byte[] response = visitService.getAllVisitRequestsBetweenDates(lclFromDate, lclToDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setContentDispositionFormData("filename","VisitReport_" + fromDate +"_" + toDate + ".csv");

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/listAllVisits")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<VisitDTO>> getAllVisitRequests
            (@RequestParam(name = "pgSize") Integer pageSize,
            @RequestParam(name = "pgNum") Integer pageNumber)
    {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return ResponseEntity.ok(visitService.findAll(pageable));
    }

}
