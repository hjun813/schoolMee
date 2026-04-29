package com.antigravity.domain.student.repository;

import com.antigravity.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s JOIN FETCH s.classRoom cr JOIN FETCH cr.school WHERE cr.school.id = :schoolId")
    List<Student> findAllBySchoolIdWithSchool(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s JOIN FETCH s.classRoom cr JOIN FETCH cr.school WHERE s.id = :studentId")
    Optional<Student> findByIdWithSchool(@Param("studentId") Long studentId);

    List<Student> findAllByClassRoomId(Long classRoomId);

    long countByClassRoomSchoolId(Long schoolId);

    Optional<Student> findByFaceKey(String faceKey);

    @Query("SELECT s.faceKey FROM Student s WHERE s.faceKey IS NOT NULL")
    List<String> findAllFaceKeys();
}
