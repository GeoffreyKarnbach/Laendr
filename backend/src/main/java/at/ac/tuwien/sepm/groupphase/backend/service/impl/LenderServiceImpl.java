package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LenderMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LenderService;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.LenderValidator;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LenderServiceImpl implements LenderService {

    private final ApplicationUserRepository userRepository;
    private final LenderRepository lenderRepository;
    private final ReputationLenderRepository reputationRepository;
    private final ReputationService reputationService;
    private final UserService userService;
    private final LenderMapper lenderMapper;
    private final LenderValidator lenderValidator;

    @Override
    public LenderViewDto getById(long id) {

        Optional<Lender> lender = lenderRepository.findById(id);
        if (lender.isPresent()) {
            var lenderDto = lenderMapper.entityToDto(lender.get());
            var requestingUser = UserUtil.getActiveUser();
            lenderDto.setCallerIsThisLender(
                requestingUser != null && lender.get().getOwner().getEmail().equals(UserUtil.getActiveUser().getEmail())
            );
            return lenderDto;
        } else {
            throw new NotFoundException("Vermieter mit id " + id + " nicht gefunden");
        }
    }

    @Override
    public String addLenderRole(long userId) {
        var userEntity = userRepository.findById(userId);
        if (userEntity.isEmpty()) {
            throw new NotFoundException("Nutzer mit id " + userId + " existiert nicht");
        }
        lenderValidator.validateAddLenderRole(userEntity.get());

        lenderRepository.save(Lender.builder()
            .id(userEntity.get().getId())
            .reputation(reputationRepository.save(reputationService.newLenderReputationEntity()))
            .build());

        var activeUser = UserUtil.getActiveUser();
        if (activeUser != null && activeUser.getEmail().equals(userEntity.get().getEmail())) {
            return userService.regenerateToken(activeUser.getEmail());
        }
        return null;
    }
}
