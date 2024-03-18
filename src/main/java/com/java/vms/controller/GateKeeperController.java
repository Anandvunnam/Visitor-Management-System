package com.java.vms.controller;


import com.java.vms.model.VisitDTO;
import com.java.vms.model.VisitorDTO;
import com.java.vms.service.UserService;
import com.java.vms.service.VisitService;
import com.java.vms.service.VisitorService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gt")
public class GateKeeperController {
    @Autowired
    private VisitService visitService;

    @Autowired
    private VisitorService visitorService;

    @PostMapping("/createVisitor")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createVisitor(@RequestBody @Valid final VisitorDTO visitorDTO) {
        final Long createdId = visitorService.create(visitorDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PostMapping("/createVisit")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createVisit(@RequestBody @Valid final VisitDTO visitDTO) throws BadRequestException {
        final Long createdId = visitService.create(visitDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/markEntry/{visitId}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Void> markEntry(@PathVariable @Valid final Long visitId) throws BadRequestException {
        visitService.markVisitorEntry(visitId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/markExit/{visitId}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Void> markExit(@PathVariable @Valid final Long visitId) throws BadRequestException {
        visitService.markVisitorExit(visitId);
        return ResponseEntity.ok().build();
    }
}
