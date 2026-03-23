package com.student.student_api.service;

import com.student.student_api.model.Student;
import com.student.student_api.repository.StudentRepo;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StudentServiceImpl implements StudentService{

    private final StudentRepo studentRepo;
    private final AtomicLong idcCounter = new AtomicLong(1);
    public StudentServiceImpl(StudentRepo studentRepo) {
        this.studentRepo = studentRepo;
    }
    @Override
    public Student createStudent(Student student) {
        student.setId(idcCounter.getAndIncrement());
        return studentRepo.save(student);
    }

    @Override
    public Student updateStudent(Long id, Student student) {
        return studentRepo.update(id, student);
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepo.deleteById(id);
    }

    @Override
    public Student getStudentById(Long id) {
        return studentRepo.findById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }
}
