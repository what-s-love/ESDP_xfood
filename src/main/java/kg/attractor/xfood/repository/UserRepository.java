package kg.attractor.xfood.repository;

import kg.attractor.xfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long> {
}
