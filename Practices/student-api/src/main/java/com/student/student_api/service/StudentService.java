package com.student.student_api.service;

import com.student.student_api.model.Student;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

public interface StudentService {
    public Student createStudent(Student student);
    public Student updateStudent(Long id, Student student);
    public void deleteStudent(Long id);
    public Student getStudentById(Long id);
    public List<Student> getAllStudents();

}
