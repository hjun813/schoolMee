package com.antigravity.domain.student.service;

import com.antigravity.domain.student.dto.StudentResponse;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 조회 전용 트랜잭션: DB 락 없이 성능 향상
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * 전체 학생 목록을 조회한다.
     * Fetch Join으로 school 정보를 함께 로드해 N+1 문제를 방지한다.
     */
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(StudentResponse::from)
                .toList();
    }
}
