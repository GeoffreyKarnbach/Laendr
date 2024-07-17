package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationColumnDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationSummaryDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SortDirectionDto;

public interface ReputationSummaryService {

    /**
     * Returns reputation summaries for all lenders with name matching the given query string.
     *
     * @param search The query string
     * @param page page to request
     * @param pageSize size of page to request
     * @param sortColumn column to sort by
     * @param sortDirection direction to sort in
     * @return reputation summaries for matching lenders as pageable
     */
    PageableDto<ReputationSummaryDto> getLenderReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection);

    /**
     * Returns reputation summaries for all locations with name matching the given query string.
     *
     * @param search The query string
     * @param page page to request
     * @param pageSize size of page to request
     * @param sortColumn column to sort by
     * @param sortDirection direction to sort in
     * @return reputation summaries for matching locations as pageable
     */
    PageableDto<ReputationSummaryDto> getLocationReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection);

    /**
     * Returns reputation summaries for all renters with name matching the given query string.
     *
     * @param search The query string
     * @param page page to request
     * @param pageSize size of page to request
     * @param sortColumn column to sort by
     * @param sortDirection direction to sort in
     * @return reputation summaries for matching renters as pageable
     */
    PageableDto<ReputationSummaryDto> getRenterReputations(String search, int page, int pageSize, ReputationColumnDto sortColumn, SortDirectionDto sortDirection);

}
