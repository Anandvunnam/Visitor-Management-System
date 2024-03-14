package com.java.vms.service;

import com.java.vms.domain.Flat;
import com.java.vms.model.FlatDTO;
import com.java.vms.model.FlatStatus;
import com.java.vms.repos.FlatRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class FlatService {

    private final FlatRepository flatRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(FlatService.class);

    public FlatService(final FlatRepository flatRepository) {
        this.flatRepository = flatRepository;
    }

    public List<FlatDTO> findAll() {
        final List<Flat> flats = flatRepository.findAll(Sort.by("id"));
        return flats.stream()
                .map(flat -> mapToDTO(flat, new FlatDTO()))
                .toList();
    }

    public FlatDTO get(final Long id) {
        return flatRepository.findById(id)
                .map(flat -> mapToDTO(flat, new FlatDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final FlatDTO flatDTO) {
        final Flat flat = new Flat();
        mapToEntity(flatDTO, flat);
        flat.setFlatStatus(FlatStatus.AVAILABLE);
        LOGGER.info("FLAT created with num: " + flatDTO.getFlatNum());
        return flatRepository.save(flat).getId();
    }

    public Flat update(final FlatDTO flatDTO) throws NotFoundException {
        final Flat flat = flatRepository.findByFlatNum(flatDTO.getFlatNum())
                .orElseThrow(() -> new NotFoundException("Flat not found for num: " + flatDTO.getFlatNum()));
        mapToEntity(flatDTO, flat);
        flatRepository.save(flat);
        return flat;
    }

    private FlatDTO mapToDTO(final Flat flat, final FlatDTO flatDTO) {
//        flatDTO.setId(flat.getId());
        flatDTO.setFlatNum(flat.getFlatNum());
        flatDTO.setFlatStatus(flat.getFlatStatus());
        return flatDTO;
    }

    private Flat mapToEntity(final FlatDTO flatDTO, final Flat flat) {
        flat.setFlatNum(flatDTO.getFlatNum());
        if(flatDTO.getFlatStatus() != null){
            flat.setFlatStatus(flatDTO.getFlatStatus());
        }
        else{
            flat.setFlatStatus(FlatStatus.AVAILABLE);
        }
        return flat;
    }

    public boolean flatNumExists(final String flatNum) {
        return flatRepository.existsByFlatNumIgnoreCase(flatNum);
    }

    public FlatStatus changeFlatStatusToNotAvailable(String flatNum, boolean status) {
        final Flat flat = flatRepository.findByFlatNum(flatNum)
                .orElseThrow(() -> new NotFoundException("Flat not found for num: " + flatNum));
        if(status)
            flat.setFlatStatus(FlatStatus.AVAILABLE);
        else
            flat.setFlatStatus(FlatStatus.NOTAVAILABLE);
        LOGGER.info("FLAT status changed for flat num: " + flatNum);
        return flatRepository.save(flat).getFlatStatus();
    }
}
