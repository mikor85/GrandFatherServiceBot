package mi.corvik.dao;

import mi.corvik.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {

    AppUser findAppUserByTelegramUserId(Long id);
}