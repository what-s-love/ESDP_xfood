package kg.attractor.xfood.repository;

import jakarta.transaction.Transactional;
import kg.attractor.xfood.enums.Status;
import kg.attractor.xfood.model.CheckList;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long>, JpaSpecificationExecutor<CheckList> {
    List<CheckList> findCheckListByExpertEmailAndStatus(String email, Status status);

    Optional<CheckList> findByUuidLink(String uuid);

    @Query(value = """
            SELECT c
            FROM CheckList c
            where c.uuidLink = ?1
            and CAST(c.status as text) = :#{#status.getStatus()}
            """)
    Optional<CheckList> findByIdAndStatus(String checkListId, Status status);

    @Query(value = """
            SELECT c
            FROM CheckList c
            where c.id = ?1
            and CAST(c.status as text) = :#{#status.getStatus()}
            """)
    Optional<CheckList> findByIdAndStatus(Long checkListId, Status status);

    @Query(value = """
            SELECT c
            FROM CheckList c
            WHERE CAST(c.status as text) = :#{#status.getStatus()}
            """)
    List<CheckList> findByStatus(Status status);

   @Query(value = """
            SELECT c
            FROM CheckList c
            WHERE CAST(c.status as text) = :#{#status.getStatus()}
            """)
    List<CheckList> findCheckListByStatus(Status status);

    @Query(value = """
            SELECT *
            FROM check_lists
            WHERE deleted = TRUE
            """, nativeQuery = true)
    List<CheckList> findDeletedChecklists();

    @Modifying
    @Transactional
   @Query(value = """
            		insert into check_lists(status, work_schedule_id, expert_id, uuid_link, type_id)
            			values(CAST(CAST(:#{#status} as text) as Status),:#{#workSchedule}, :#{#expertId}, :#{#uuid_link}, :#{#checkTypeId}) ;
            """, nativeQuery = true)
    int saveCheckList(Long workSchedule, String status, Long expertId, String uuid_link, Long checkTypeId);



    @Query(value = """
            SELECT c
            FROM CheckList c
            where c.uuidLink = ?1
            and CAST(c.status as text) = :#{#status.getStatus()}
            """)
    Optional<CheckList> findByUuidLinkAndStatus(String uuidLink, Status status);

    CheckList findByCheckListsCriteriaId(Long id);

    Optional<CheckList> findCheckListByWorkSchedule_IdAndExpert_Id(Long workScheduleId, Long expertId);

    boolean existsByWorkSchedule_IdAndExpert_Id(Long id, Long expertId);

    void deleteByUuidLinkAndStatusIsNot(String uuid, Status status);

    void deleteByUuidLink(String uuid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE check_lists SET deleted = false WHERE uuid_link = ?1", nativeQuery = true)
    void restore(String uuid);

    @Query(nativeQuery = true, value = "SELECT * FROM check_lists WHERE uuid_link = ?1 and deleted = true")
    Optional<CheckList> findDeleted(String checkListId);

    @Query("select c from CheckList  c where c.endTime between :startDate and :endDate ")
    List<CheckList> findAllByEndTimeBetween(LocalDate startDate, LocalDate endDate);
}
