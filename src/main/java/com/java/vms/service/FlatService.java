package com.java.vms.service;

import com.java.vms.domain.Flat;
import com.java.vms.model.FlatDTO;
import com.java.vms.repos.FlatRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class FlatService {

    private final FlatRepository flatRepository;

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
        return flatRepository.save(flat).getId();
    }

    public void update(final Long id, final FlatDTO flatDTO) {
        final Flat flat = flatRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(flatDTO, flat);
        flatRepository.save(flat);
    }

    public void delete(final Long id) {
        flatRepository.deleteById(id);
    }

    private FlatDTO mapToDTO(final Flat flat, final FlatDTO flatDTO) {
        flatDTO.setId(flat.getId());
        flatDTO.setFlatNum(flat.getFlatNum());
        flatDTO.setFlatStatus(flat.getFlatStatus());
        return flatDTO;
    }

    private Flat mapToEntity(final FlatDTO flatDTO, final Flat flat) {
        flat.setFlatNum(flatDTO.getFlatNum());
        flat.setFlatStatus(flatDTO.getFlatStatus());
        return flat;
    }

    public boolean flatNumExists(final String flatNum) {
        return flatRepository.existsByFlatNumIgnoreCase(flatNum);
    }

}
