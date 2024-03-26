package com.java.vms.service;

import com.java.vms.model.PreApproveDTO;
import com.java.vms.model.VisitorDTO;
import com.java.vms.repos.FlatRepository;
import com.java.vms.repos.UserRepository;
import com.java.vms.repos.VisitRepository;
import com.java.vms.repos.VisitorRepository;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResidentService {


    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final FlatRepository flatRepository;
    private final VisitorRepository visitorRepository;

    private final VisitService visitService;

    private final  VisitorService visitorService;

    private Logger LOGGER = LoggerFactory.getLogger(ResidentService.class);

    public ResidentService(final VisitRepository visitRepository, final UserRepository userRepository,
                           final FlatRepository flatRepository, final VisitorRepository visitorRepository,
                           final VisitService visitService,final VisitorService visitorService) {
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.flatRepository = flatRepository;
        this.visitorRepository = visitorRepository;
        this.visitService = visitService;
        this.visitorService = visitorService;
    }

    @SneakyThrows
    public void createPreApprovedVisitReq(final PreApproveDTO preApproveDTO, final Long userId){
        Long visitorId = visitorService.create(preApproveDTO);
        LOGGER.info("Visitor created with id: " + visitorId);
        Long visitId = visitService.create(preApproveDTO, visitorId, userId);
        LOGGER.info("Visit request created with id: " + visitId);
    }
}
