package com.student.student_api.repository;

import com.student.student_api.model.Student;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StudentRepo {

    private HashMap<Long, Student> studentMap;

    public StudentRepo() {
        studentMap = new HashMap<>();
    }

    public Student save(Student student) {
        studentMap.put(student.getId(), student);
        return student;
    }

    public List<Student> findAll()
    {
        return new ArrayList<>(studentMap.values());
    }

    public Student findById(Long id) {
        return studentMap.get(id);
    }

    public void deleteById(Long id) {
        studentMap.remove(id);
    }

    public Student update(Long id, Student student) {
        return studentMap.put(id, student);
    }

}
