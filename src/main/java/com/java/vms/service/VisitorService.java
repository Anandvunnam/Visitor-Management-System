package com.java.vms.service;

import com.java.vms.domain.Address;
import com.java.vms.domain.Visitor;
import com.java.vms.model.VisitorDTO;
import com.java.vms.repos.AddressRepository;
import com.java.vms.repos.VisitorRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final AddressRepository addressRepository;

    public VisitorService(final VisitorRepository visitorRepository,
            final AddressRepository addressRepository) {
        this.visitorRepository = visitorRepository;
        this.addressRepository = addressRepository;
    }

    public List<VisitorDTO> findAll() {
        final List<Visitor> visitors = visitorRepository.findAll(Sort.by("id"));
        return visitors.stream()
                .map(visitor -> mapToDTO(visitor, new VisitorDTO()))
                .toList();
    }

    public VisitorDTO get(final Long id) {
        return visitorRepository.findById(id)
                .map(visitor -> mapToDTO(visitor, new VisitorDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final VisitorDTO visitorDTO) {
        final Visitor visitor = new Visitor();
        mapToEntity(visitorDTO, visitor);
        return visitorRepository.save(visitor).getId();
    }

    public void update(final Long id, final VisitorDTO visitorDTO) {
        final Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(visitorDTO, visitor);
        visitorRepository.save(visitor);
    }

    public void delete(final Long id) {
        visitorRepository.deleteById(id);
    }

    private VisitorDTO mapToDTO(final Visitor visitor, final VisitorDTO visitorDTO) {
        visitorDTO.setId(visitor.getId());
        visitorDTO.setName(visitor.getName());
        visitorDTO.setPhone(visitor.getPhone());
        visitorDTO.setNumOfGuests(visitor.getNumOfGuests());
        visitorDTO.setUnqId(visitor.getUnqId());
        visitorDTO.setAddress(visitor.getAddress() == null ? null : visitor.getAddress().getId());
        return visitorDTO;
    }

    private Visitor mapToEntity(final VisitorDTO visitorDTO, final Visitor visitor) {
        visitor.setName(visitorDTO.getName());
        visitor.setPhone(visitorDTO.getPhone());
        visitor.setNumOfGuests(visitorDTO.getNumOfGuests());
        visitor.setUnqId(visitorDTO.getUnqId());
        final Address address = visitorDTO.getAddress() == null ? null : addressRepository.findById(visitorDTO.getAddress())
                .orElseThrow(() -> new NotFoundException("address not found"));
        visitor.setAddress(address);
        return visitor;
    }

    public boolean phoneExists(final String phone) {
        return visitorRepository.existsByPhoneIgnoreCase(phone);
    }

    public boolean unqIdExists(final String unqId) {
        return visitorRepository.existsByUnqIdIgnoreCase(unqId);
    }

}
