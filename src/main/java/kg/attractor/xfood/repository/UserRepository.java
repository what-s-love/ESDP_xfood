package kg.attractor.xfood.repository;

import kg.attractor.xfood.enums.Role;
import kg.attractor.xfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> getByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(
            """
                    SELECT u from User u
                    where CAST(u.role as text) = ?1
                    """
    )
    List<User> findByRole(String role);

}
