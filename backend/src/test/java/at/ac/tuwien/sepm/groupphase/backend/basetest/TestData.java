package at.ac.tuwien.sepm.groupphase.backend.basetest;

import java.util.ArrayList;
import java.util.List;

public interface TestData {

    String BASE_URI = "/api/v1";

    String LOGIN_BASE_URI = BASE_URI + "/authentication";

    String LENDER_USER = "lender@email.com";
    List<String> LENDER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
            add("ROLE_RENTER");
            add("ROLE_LENDER");
        }
    };

    String RENTER_USER = "renter@email.com";
    List<String> RENTER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
            add("ROLE_RENTER");
        }
    };

    String ADMIN_USER = "admin@email.com";
    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };
    String DEFAULT_USER = "admin@email.com";
    List<String> USER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
        }
    };

    String LENDER2_USER = "renter2@email.com";
    List<String> LENDER2_ROLES = new ArrayList<>() {
        {
            add("ROLE_LENDER");
            add("ROLE_USER");
        }
    };

}
