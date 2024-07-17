package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Profile("!test")
@Slf4j
@RequiredArgsConstructor
public class ReputationServiceScheduling {

    // Minimum age since last update before a reputation is considered for scheduled time
    // decay update.
    private static final long DECAY_UPDATE_MIN_AGE = 7; // in days

    private final ReputationService reputationService;

    @PostConstruct
    private void notifyScheduled() {
        log.info("Scheduled time decaying of reputations is enabled");
    }

    /**
     * Scheduled function to apply time decay to reputations that haven't been updated in
     * the last DECAY_UPDATE_MIN_AGE days.
     * Runs at midnight every sunday.
     */
    @Scheduled(cron = "59 59 23 * * 0")
    protected void scheduledUpdateTimeDecay() {
        reputationService.updateTimeDecayBefore(LocalDateTime.now().minus(DECAY_UPDATE_MIN_AGE, ChronoUnit.DAYS));
    }

}
