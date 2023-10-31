package com.api.rentchart.repository;

import com.api.rentchart.entities.User;
import com.api.rentchart.exceptions.EtAuthException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

@Repository
public class UserRepository {

    private static final String SQL_CREATE = "INSERT INTO RENT_CAR.USER(EMAIL, USERNAME, PASSWORD) VALUES (?, ?, ?)";
    private static final String SQL_COUNT_BY_EMAIL = "SELECT COUNT(*) FROM RENT_CAR.USER WHERE EMAIL = ?";
    private static final String SQL_COUNT_BY_USERNAME = "SELECT COUNT(*) FROM RENT_CAR.USER WHERE USERNAME = ?";
    private static final String SQL_FIND_BY_ID = "SELECT ID, EMAIL, USERNAME, PASSWORD " +
            "FROM RENT_CAR.USER WHERE ID = ?";
    private static final String SQL_FIND_BY_EMAIL = "SELECT ID, EMAIL, USERNAME, PASSWORD " +
            "FROM RENT_CAR.USER WHERE EMAIL = ?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public UUID create(String email, String username, String password) throws EtAuthException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email);
                ps.setString(2, username);
                ps.setString(3, hashedPassword);
                return ps;
            }, keyHolder);
            return (UUID) keyHolder.getKeys().get("ID");
        }catch(Exception e){
            throw new EtAuthException("Detalles Invalidos. Fallido a crear cuenta");
        }
    }

    public User findByEmailAndPassword(String email, String password) throws EtAuthException {
        try{
            User user = jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, new Object[]{email}, userRowMapper);
            if(!BCrypt.checkpw(password, user.getPassword()))
                throw new EtAuthException("Password o email invalidos");
            return user;
        }catch(EmptyResultDataAccessException e){
            throw new EtAuthException("Password o email invalidos");
        }
    }

    public Integer getCountByEmail(String email) {
        return jdbcTemplate.queryForObject(SQL_COUNT_BY_EMAIL, new Object[]{email}, Integer.class);
    }

    public Integer getCountByUsername(String username) {
        return jdbcTemplate.queryForObject(SQL_COUNT_BY_USERNAME, new Object[]{username}, Integer.class);
    }

    public User findById(UUID id) {
        return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{id}, userRowMapper);
    }

    private RowMapper<User> userRowMapper = ((rs, rowNum) -> {
        return new User((java.util.UUID) rs.getObject("ID"),
                rs.getString("EMAIL"),
                rs.getString("USERNAME"),
                rs.getString("PASSWORD"));
    });

}
