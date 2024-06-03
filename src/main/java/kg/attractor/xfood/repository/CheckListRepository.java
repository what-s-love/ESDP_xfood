package kg.attractor.xfood.repository;

import kg.attractor.xfood.model.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface CheckListRepository extends JpaRepository<CheckList, Integer> {
}
