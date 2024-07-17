package at.ac.tuwien.sepm.groupphase.backend.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class UserUtil {

    public static UserInfo getActiveUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return new UserInfo(auth.getPrincipal().toString(), auth.getAuthorities());
        }

        return null;
    }

    @AllArgsConstructor
    @Getter
    public static class UserInfo {

        private String email;
        private Collection<? extends GrantedAuthority> roles;

    }

}
