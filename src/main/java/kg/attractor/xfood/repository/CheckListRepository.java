package kg.attractor.xfood.repository;

import jakarta.transaction.Transactional;
import kg.attractor.xfood.enums.Status;
import kg.attractor.xfood.model.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {

    Optional<CheckList> findByUuidLink(String uuid);

    @Query(value = """
            SELECT c
            FROM CheckList c
                    JOIN Opportunity o ON c.opportunity.id = o.id
                     JOIN User u ON o.user.id = u.id
            WHERE u.email = :#{#username} AND CAST(c.status as text) = :#{#status.getStatus()}
            """)
    List<CheckList> findCheckListByExpertEmailAndStatus(String username, Status status);

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
		    WHERE CAST(c.status as text) = :#{#status.getStatus()}
		    """)
    List<CheckList> findByStatus(Status status);

    @Query(value = """
		    SELECT c
		    FROM CheckList c
		            JOIN Opportunity o ON c.opportunity.id = o.id
		             JOIN User u ON o.user.id = u.id
		    WHERE CAST(c.status as text) = :#{#status.getStatus()}
		    """)
    List<CheckList> findCheckListByStatus(Status status);

    @Modifying
    @Transactional
    @Query(value = """
			insert into check_lists(status, work_schedule_id, opportunity_id)
 			values(CAST(CAST(:#{#status} as text) as Status),:#{#workSchedule},:#{#opportunity}) ;
	""", nativeQuery = true)
    int saveChecklist(Long opportunity, Long workSchedule, String status);

    CheckList findCheckListByWorkSchedule_IdAndAndOpportunity_Id(Long workScheduleId, Long opportunityId);

    @Query(value = """
            SELECT c
            FROM CheckList c
            where c.uuidLink = ?1
            and CAST(c.status as text) = :#{#status.getStatus()}
            """)
    Optional<CheckList> findByUuidLinkAndStatus(String uuidLink, Status status);
}
