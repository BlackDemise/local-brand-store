package wandererpi.lbs.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import wandererpi.lbs.entity.User;

import java.util.Optional;

@Component
public class AppAuditAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null 
                || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof User user) {
            return Optional.ofNullable(user.getId());
        }
        
        return Optional.empty();
    }
}
