package com.soufyan.userservice.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import com.soufyan.userservice.model.*;

@Repository
public interface UserRepository extends CrudRepository<User,Long>, PagingAndSortingRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);
}
