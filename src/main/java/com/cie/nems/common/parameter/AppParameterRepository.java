package com.cie.nems.common.parameter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.String;

public interface AppParameterRepository extends JpaRepository<AppParameter, String> {
}
