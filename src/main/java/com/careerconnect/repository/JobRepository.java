package com.careerconnect.repository;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiter(User recruiter);

    List<Job> findByRecruiterId(Long recruiterId);
    Page<Job> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Query("""
           SELECT j FROM Job j
           WHERE (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%')))
             AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
             AND (:skills IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :skills, '%')))
           ORDER BY j.createdDate DESC
           """)
    Page<Job> searchJobsPage(@Param("title") String title,
                              @Param("location") String location,
                              @Param("skills") String skills,
                              Pageable pageable);

    @Query("""
           SELECT j FROM Job j
           WHERE (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%')))
             AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
             AND (:skills IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :skills, '%')))
           ORDER BY j.createdDate DESC
           """)
    List<Job> searchJobs(@Param("title") String title,
                          @Param("location") String location,
                          @Param("skills") String skills);
}
