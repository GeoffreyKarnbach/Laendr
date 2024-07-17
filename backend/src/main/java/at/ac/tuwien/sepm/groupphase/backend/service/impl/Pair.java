package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Pair<T, U> {

    @Getter @Setter
    @NonNull
    private T first;
    @Getter @Setter
    @NonNull
    private U second;

}
