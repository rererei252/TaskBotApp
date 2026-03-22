package com.example.taskbot.task.repository;

import com.example.taskbot.task.entity.Task;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select t
            from Task t
            where t.user.id = :userId
              and t.deletedAt is null
              and t.startAt < :to
              and t.dueAt >= :from
            order by t.startAt asc, t.id asc
            """)
    List<Task> findMonthlyTasks(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            select t
            from Task t
            where t.id = :taskId
              and t.user.id = :userId
              and t.deletedAt is null
            """)
    Optional<Task> findOwnedTask(
            @Param("taskId") Long taskId,
            @Param("userId") Long userId
    );
}
