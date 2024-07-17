package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;

import java.util.List;

public interface PlzService {

    /**
     * Finds the top 10 plzs that start with the given string.
     *
     * @param plzQuery plz string to search for
     * @return found plzs
     */
    List<PlzDto> findPlzSuggestions(String plzQuery);

}
