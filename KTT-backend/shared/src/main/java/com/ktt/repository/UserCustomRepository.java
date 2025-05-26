package com.ktt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCustomRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int updateNoOfAttempt(String login, String companyCode) {
        String sql = "Update users set number_of_attempts = number_of_attempts + 1 where login = :login and company_code = :companyCode and is_mail_verified = true";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("login", login);
        param.addValue("companyCode", companyCode);
        return namedParameterJdbcTemplate.update(sql, param);
    }

    public int updateAccountStatus(String login, String companyCode) {
        String sql = "Update users set number_of_attempts = 0 , account_status = 'Active' where login = :login and company_code = :companyCode";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("login", login);
        param.addValue("companyCode", companyCode);
        return namedParameterJdbcTemplate.update(sql, param);
    }
}
