package com.java.vms.service;

import com.java.vms.domain.Flat;
import com.java.vms.domain.User;
import com.java.vms.domain.Visit;
import com.java.vms.domain.Visitor;
import com.java.vms.model.VisitDTO;
import com.java.vms.model.VisitStatus;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.repos.VisitRepository;
import com.java.vms.repos.VisitorRepository;
import com.java.vms.util.NotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final FlatRepository flatRepository;
    private final VisitorRepository visitorRepository;

    private Logger LOGGER = LoggerFactory.getLogger(VisitService.class);

    public VisitService(final VisitRepository visitRepository, final UserRepository userRepository,
            final FlatRepository flatRepository, final VisitorRepository visitorRepository) {
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.flatRepository = flatRepository;
        this.visitorRepository = visitorRepository;
    }

    public List<VisitDTO> findAll() {
        final List<Visit> visits = visitRepository.findAll(Sort.by("id"));
        return visits.stream()
                .map(visit -> mapToDTO(visit, new VisitDTO()))
                .toList();
    }

    public VisitDTO get(final Long id) {
        return visitRepository.findById(id)
                .map(visit -> mapToDTO(visit, new VisitDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final VisitDTO visitDTO) throws BadRequestException {
        final Visit visit = new Visit();
        mapToEntity(visitDTO, visit);
        LOGGER.info("Visit created for visitor with Id: " + visitDTO.getVisitor());
        return visitRepository.save(visit).getId();
    }

    public void update(final Long id, final VisitDTO visitDTO) throws BadRequestException {
        final Visit visit = visitRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(visitDTO, visit);
        visitRepository.save(visit);
    }
    

    private VisitDTO mapToDTO(final Visit visit, final VisitDTO visitDTO) {
        visitDTO.setId(visit.getId());
        visitDTO.setVisitStatus(visit.getVisitStatus());
        visitDTO.setInTime(visit.getInTime());
        visitDTO.setOutTime(visit.getOutTime());
        visitDTO.setVisitorImgUrl(visit.getVisitorImgUrl());
        visitDTO.setPurpose(visit.getPurpose());
        visitDTO.setNumOfGuests(visit.getNumOfGuests());
        visitDTO.setUserName(visit.getUser() == null ? null : visit.getUser().getName());
        visitDTO.setUserPhoneNumber(visit.getUser() == null ? null : visit.getUser().getPhone());
        visitDTO.setFlatNum(visit.getFlat() == null ? null : visit.getFlat().getFlatNum());
        visitDTO.setVisitor(visit.getVisitor() == null ? null : visit.getVisitor().getId());
        return visitDTO;
    }

    private Visit mapToEntity(final VisitDTO visitDTO, final Visit visit) throws BadRequestException {
        visit.setVisitStatus(VisitStatus.PENDING);
        visit.setInTime(visitDTO.getInTime());
        visit.setOutTime(visitDTO.getOutTime());
        visit.setVisitorImgUrl(visitDTO.getVisitorImgUrl());
        visit.setPurpose(visitDTO.getPurpose());
        visit.setNumOfGuests(visitDTO.getNumOfGuests());
        final User user = visitDTO.getUserName() == null && visitDTO.getUserPhoneNumber() == null ?
                null : userRepository.findUserByNameAndPhone(visitDTO.getUserName(), visitDTO.getUserPhoneNumber())
                .orElseThrow(() -> new NotFoundException("user not found for resident: " + visitDTO.getUserName()));

        final Flat flat = visitDTO.getFlatNum() == null ? null : flatRepository.findByFlatNum(visitDTO.getFlatNum())
                .orElseThrow(() -> new NotFoundException("flat not found for flat num: " + visitDTO.getFlatNum()));

        final Visitor visitor = visitDTO.getVisitor() == null ? null : visitorRepository.findById(visitDTO.getVisitor())
                .orElseThrow(() -> new NotFoundException("visitor not found for id: " + visitDTO.getVisitor()));
        if (user == null || flat == null || visitor == null){
            throw new BadRequestException("Invalid request received. Please provide valid details.");
        }
        visit.setFlat(flat);
        visit.setUser(user);
        visit.setVisitor(visitor);
        return visit;
    }

    public void markVisitorEntry(Long visitId) throws BadRequestException {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new BadRequestException("No visit request found for id: " + visitId));
        if(visit.getVisitStatus() == VisitStatus.APPROVED){
            visit.setInTime(LocalDateTime.now());
            LOGGER.info("Marked entry for visitor with id: " + visit.getVisitor().getId());
            visitRepository.save(visit);
        }
        else if(visit.getVisitStatus() == VisitStatus.REJECTED){
            LOGGER.warn("Visit request reject for id: " + visit.getId());
        }
        else{
            throw new BadRequestException("Visit request not yet approved.");
        }
    }

    public void markVisitorExit(Long visitId) throws BadRequestException {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new BadRequestException("No visit request found for id: " + visitId));
        if(visit.getInTime() != null && visit.getOutTime() == null){
            visit.setOutTime(LocalDateTime.now());
            visit.setVisitStatus(VisitStatus.COMPLETED);
            LOGGER.info("Marked exit for visitor with id: " + visit.getVisitor().getId());
            visitRepository.save(visit);
        }
        else{
            throw new BadRequestException("Visitor entry not found.");
        }
    }

    public void approveVisitReq(Long visitId) throws BadRequestException {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new BadRequestException("No visit request found for id: " + visitId));
        if(visit.getVisitStatus() != VisitStatus.PENDING){
            LOGGER.info("Visit req with id " + visit.getId() + " is not pending");
            throw new BadRequestException("Visit req with id " + visit.getId() + " is not pending");
        }
        visit.setVisitStatus(VisitStatus.APPROVED);
        LOGGER.info("Approved visit request with id: " + visit.getId());
        visitRepository.save(visit);
    }

    public void rejectVisitReq(Long visitId) throws BadRequestException {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new BadRequestException("No visit request found for id: " + visitId));
        if(visit.getVisitStatus() != VisitStatus.PENDING){
            LOGGER.info("Visit req with id " + visit.getId() + " is not pending");
            throw new BadRequestException("Visit req with id " + visit.getId() + " is not pending");
        }
        visit.setVisitStatus(VisitStatus.REJECTED);
        LOGGER.info("Rejected visit request with id: " + visit.getId());
        visitRepository.save(visit);
    }

    public List<VisitDTO> listAllVisitReqsByStatus(String status, String userName, Long userPhone, boolean isDurationEnabled) throws BadRequestException {
        final User user = userRepository.findUserByNameAndPhone(userName, userPhone)
                .orElseThrow(() -> new BadRequestException("User not found  with given details."));
        VisitStatus vStatus = VisitStatus.APPROVED.name().toLowerCase().equals(status.toLowerCase())? VisitStatus.APPROVED : VisitStatus.REJECTED;
        List<Visit> visits = visitRepository.findVisitByVisitStatusAndUser(vStatus, user);
        if (visits.isEmpty()){
            throw new NotFoundException("No visit requests found with status: " + status);
        }
        List<VisitDTO> visitDTOs = new ArrayList<>();
        VisitDTO visitDTO = null;
        for(Visit visit: visits){
            if(isDurationEnabled){
                Duration duration = Duration.between(LocalDateTime.now(), visit.getDateCreated()).abs();
                LOGGER.error("DURATION: " + duration.toDays());
                if(duration.toDays() > 30L){
                    LOGGER.info("Ignored visit request with id: " + visit.getId());
                    continue;
                }
            }
            visitDTO = new VisitDTO();
            visitDTOs.add(mapToDTO(visit, visitDTO));
        }
        LOGGER.info("List of visits [" + visitDTOs.size() + "] found for user: " + userName + " with status: " + status);
        return visitDTOs;
    }
}
