package edu.uptc.swii.loginservice.repository;

import edu.uptc.swii.loginservice.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, String> {}