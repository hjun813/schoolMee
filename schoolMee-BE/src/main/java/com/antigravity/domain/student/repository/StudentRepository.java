package com.antigravity.domain.student.repository;

import com.antigravity.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // N+1 방지: school을 fetch join으로 한 번에 로드
    @Query("SELECT s FROM Student s JOIN FETCH s.school WHERE s.school.id = :schoolId")
    List<Student> findAllBySchoolIdWithSchool(@Param("schoolId") Long schoolId);

    List<Student> findAll();
}
