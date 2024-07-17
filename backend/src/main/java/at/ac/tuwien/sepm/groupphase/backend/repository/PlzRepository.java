package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlzRepository extends JpaRepository<Plz, String> {

    /**
     * Finds the top 10 plzs that start with the given string.
     *
     * @param plz plz string to search for
     * @return found plzs
     */
    List<Plz> findTop10ByPlzStartsWith(String plz);

}
