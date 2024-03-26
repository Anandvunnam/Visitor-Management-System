package com.java.vms.service;

import com.java.vms.domain.Address;
import com.java.vms.domain.Visitor;
import com.java.vms.model.PreApproveDTO;
import com.java.vms.model.VisitorDTO;
import com.java.vms.repos.AddressRepository;
import com.java.vms.repos.VisitorRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final AddressRepository addressRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(VisitorService.class);

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
        if(unqIdExists(visitorDTO.getUnqId())){
            LOGGER.info("Visitor already exists with unq ID: " + visitorDTO.getUnqId());
            return visitorRepository.findVisitorByUnqId(visitorDTO.getUnqId()).get().getId();
        }
        mapToEntity(visitorDTO, visitor);
        LOGGER.info("Visitor created with unq id: " + visitorDTO.getUnqId());
        return visitorRepository.save(visitor).getId();
    }

    public Long create(final PreApproveDTO preApproveDTO) {
        VisitorDTO visitorDTO = new VisitorDTO();
        visitorDTO = mapPreApprovedDTOToVisitorDTO(preApproveDTO, visitorDTO);
        return create(visitorDTO);
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
        visitorDTO.setUnqId(visitor.getUnqId());
        Address address = visitor.getAddress() == null ? null : visitor.getAddress();
        visitorDTO.setLine1(address.getLine1());
        visitorDTO.setLine2(address.getLine2());
        visitorDTO.setCity(address.getCity());
        visitorDTO.setState(address.getState());
        visitorDTO.setCountry(address.getCountry());
        visitorDTO.setPincode(address.getPincode());
        //visitorDTO.setAddress(visitor.getAddress() == null ? null : visitor.getAddress().getId());
        return visitorDTO;
    }

    private Visitor mapToEntity(final VisitorDTO visitorDTO, final Visitor visitor) {
        visitor.setName(visitorDTO.getName());
        visitor.setPhone(visitorDTO.getPhone());
        visitor.setUnqId(visitorDTO.getUnqId());
        if(visitorDTO.getLine1() != null || visitorDTO.getLine2() != null ||
                visitorDTO.getCity() != null || visitorDTO.getState() != null ||
                visitorDTO.getCountry() != null || visitorDTO.getPincode() != null) {
                final Address address = Address.builder().line1(visitorDTO.getLine1())
                    .line2(visitorDTO.getLine2())
                    .city(visitorDTO.getCity())
                    .state(visitorDTO.getState())
                    .country(visitorDTO.getCountry())
                    .pincode(visitorDTO.getPincode()).build();
            addressRepository.save(address);
            visitor.setAddress(address);
        }
        return visitor;
    }

    private VisitorDTO mapPreApprovedDTOToVisitorDTO(final PreApproveDTO preApproveDTO, VisitorDTO visitorDTO){
        visitorDTO.setName(preApproveDTO.getName());
        visitorDTO.setPhone(preApproveDTO.getPhone());
        visitorDTO.setUnqId(preApproveDTO.getUnqId());
        visitorDTO.setLine1(preApproveDTO.getLine1());
        visitorDTO.setLine2(preApproveDTO.getLine2());
        visitorDTO.setCity(preApproveDTO.getCity());
        visitorDTO.setState(preApproveDTO.getState());
        visitorDTO.setCountry(preApproveDTO.getCountry());
        visitorDTO.setPincode(preApproveDTO.getPincode());
        return visitorDTO;
    }

    public boolean phoneExists(final String phone) {
        return visitorRepository.existsByPhoneIgnoreCase(phone);
    }

    public boolean unqIdExists(final String unqId) {
        return visitorRepository.existsByUnqIdIgnoreCase(unqId);
    }

}
