package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.mapper.PlzMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PlzService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlzServiceImpl implements PlzService {

    private final PlzRepository plzRepository;

    private final PlzMapper plzMapper;

    @Override
    public List<PlzDto> findPlzSuggestions(String plzQuery) {
        return plzRepository.findTop10ByPlzStartsWith(plzQuery).stream().map(plzMapper::entityToDto).toList();
    }
}
