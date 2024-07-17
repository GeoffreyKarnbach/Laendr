package at.ac.tuwien.sepm.groupphase.backend.service;

public interface UserDeletionService {

    /**
     * Deletes all personal information from the given user and its associated entities.
     *
     * @param id ID of the user to delete
     */
    void deleteUser(long id);

}
