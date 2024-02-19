package com.java.vms.service;

import com.java.vms.domain.Flat;
import com.java.vms.domain.User;
import com.java.vms.domain.Visit;
import com.java.vms.domain.Visitor;
import com.java.vms.model.VisitDTO;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.repos.VisitRepository;
import com.java.vms.repos.VisitorRepository;
import com.java.vms.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final FlatRepository flatRepository;
    private final VisitorRepository visitorRepository;

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

    public Long create(final VisitDTO visitDTO) {
        final Visit visit = new Visit();
        mapToEntity(visitDTO, visit);
        return visitRepository.save(visit).getId();
    }

    public void update(final Long id, final VisitDTO visitDTO) {
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
        visitDTO.setUser(visit.getUser() == null ? null : visit.getUser().getId());
        visitDTO.setFlat(visit.getFlat() == null ? null : visit.getFlat().getId());
        visitDTO.setVisitor(visit.getVisitor() == null ? null : visit.getVisitor().getId());
        return visitDTO;
    }

    private Visit mapToEntity(final VisitDTO visitDTO, final Visit visit) {
        visit.setVisitStatus(visitDTO.getVisitStatus());
        visit.setInTime(visitDTO.getInTime());
        visit.setOutTime(visitDTO.getOutTime());
        visit.setVisitorImgUrl(visitDTO.getVisitorImgUrl());
        visit.setPurpose(visitDTO.getPurpose());
        final User user = visitDTO.getUser() == null ? null : userRepository.findById(visitDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        visit.setUser(user);
        final Flat flat = visitDTO.getFlat() == null ? null : flatRepository.findById(visitDTO.getFlat())
                .orElseThrow(() -> new NotFoundException("flat not found"));
        visit.setFlat(flat);
        final Visitor visitor = visitDTO.getVisitor() == null ? null : visitorRepository.findById(visitDTO.getVisitor())
                .orElseThrow(() -> new NotFoundException("visitor not found"));
        visit.setVisitor(visitor);
        return visit;
    }

}
