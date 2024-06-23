package com.java.vms.rest;

import com.java.vms.model.FlatDTO;
import com.java.vms.model.FlatStatus;
import com.java.vms.service.FlatService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/flats", produces = MediaType.APPLICATION_JSON_VALUE)
public class FlatResource {

    private final FlatService flatService;

    public FlatResource(final FlatService flatService) {
        this.flatService = flatService;
    }

    @GetMapping
    public ResponseEntity<List<FlatDTO>> getAllFlats() {
        return ResponseEntity.ok(flatService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlatDTO> getFlat(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(flatService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createFlat(@RequestBody @Valid final FlatDTO flatDTO) throws SQLIntegrityConstraintViolationException {
        flatDTO.setFlatStatus(flatDTO.getFlatStatus() != null? flatDTO.getFlatStatus() :FlatStatus.AVAILABLE);
        final Long createdId = flatService.create(flatDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateFlat(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final FlatDTO flatDTO) {
        flatService.update(flatDTO);
        return ResponseEntity.ok(id);
    }

//    Delete Method not required as this is not a restful project.
//    @DeleteMapping("/{id}")
//    @ApiResponse(responseCode = "204")
//    public ResponseEntity<Void> deleteFlat(@PathVariable(name = "id") final Long id) {
//        flatService.delete(id);
//        return ResponseEntity.noContent().build();
//    }

}
