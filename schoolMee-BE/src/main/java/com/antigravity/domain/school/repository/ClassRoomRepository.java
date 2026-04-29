package com.antigravity.domain.school.repository;

import com.antigravity.domain.school.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    List<ClassRoom> findAllBySchoolId(Long schoolId);
}
