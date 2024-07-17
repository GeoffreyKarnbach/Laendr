package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationColumnDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationSummaryDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SortDirectionDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationSummary;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReputationSummaryMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReputationSummaryServiceImpl implements ReputationSummaryService {

    private final ReputationLenderRepository reputationLenderRepository;
    private final ReputationLocationRepository reputationLocationRepository;
    private final ReputationRenterRepository reputationRenterRepository;
    private final ReputationSummaryMapper reputationSummaryMapper;

    @Override
    public PageableDto<ReputationSummaryDto> getLenderReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection) {
        return getReputations(search, page, pageSize, sortColumn, sortDirection, reputationLenderRepository::summarizeAllByLenderNameContaining);
    }

    @Override
    public PageableDto<ReputationSummaryDto> getLocationReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection) {
        return getReputations(search, page, pageSize, sortColumn, sortDirection, reputationLocationRepository::summarizeAllByLocationNameContaining);
    }

    @Override
    public PageableDto<ReputationSummaryDto> getRenterReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection) {
        return getReputations(search, page, pageSize, sortColumn, sortDirection, reputationRenterRepository::summarizeAllByRenterNameContaining);
    }

    private PageableDto<ReputationSummaryDto> getReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection,
                                                             BiFunction<String, PageRequest, Page<ReputationSummary>> function) {
        var pageRequest = PageRequest.of(page, pageSize, Sort.by(sortDirection.getDirection(), sortColumn.getAttribute()));
        var results = function.apply(search, pageRequest);
        var summaries = results.stream().map(reputationSummaryMapper::entityToDto).toList();

        return new PageableDto<>(results.getTotalElements(), results.getTotalPages(), results.getNumberOfElements(), summaries);
    }

}
