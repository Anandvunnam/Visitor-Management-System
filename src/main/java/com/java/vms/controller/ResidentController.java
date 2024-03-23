package com.java.vms.controller;

import com.java.vms.model.VisitDTO;
import com.java.vms.service.VisitService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resident")
public class ResidentController {

    @Autowired
    private VisitService visitService;

    @PutMapping("/approveReq/{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Void> approveReq(@PathVariable(name = "id") @Valid Long visitId) throws BadRequestException {
        visitService.approveVisitReq(visitId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rejectReq/{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Void> rejectReq(@PathVariable(name = "id") @Valid Long visitId) throws BadRequestException {
        visitService.rejectVisitReq(visitId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listAllVisitReqs")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<VisitDTO>> getAllVisitReqsByStatus(@RequestParam(name = "status") String status,
                                                                  @RequestParam(name = "name") String userName,
                                                                  @RequestParam(name = "phone") Long phone) throws BadRequestException{
        return ResponseEntity.ok().body(visitService.listAllVisitReqsByStatus(status, userName, phone, true));
    }
}
