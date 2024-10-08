package kg.attractor.xfood.repository;

import kg.attractor.xfood.model.CheckType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckTypeRepository extends JpaRepository<CheckType, Long> {
    @Query("select c from CheckType c order by c.name")
    List<CheckType> findByOrderByNameAsc();

    Optional<CheckType> findByName(String name);
    boolean existsByName(String name);
}
