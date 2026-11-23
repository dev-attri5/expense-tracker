package com.expense;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ExpenseDAO expenseDAO;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return user;
    };

    public UserDAO(JdbcTemplate jdbcTemplate, ExpenseDAO expenseDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.expenseDAO = expenseDAO;
    }

    private String getTableName() {
        String db = expenseDAO.getTargetDatabase();
        if (db != null && !db.trim().isEmpty()
                && !"sys".equalsIgnoreCase(db)
                && !"information_schema".equalsIgnoreCase(db)
                && !"performance_schema".equalsIgnoreCase(db)
                && !"mysql".equalsIgnoreCase(db)) {
            return "`" + db + "`.`users`";
        }
        return "`users`";
    }

    /**
     * Checks if a username already exists.
     */
    public boolean existsByUsername(String username) {
        expenseDAO.ensureTableExists();
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE `username` = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    /**
     * Finds a user by their exact username.
     */
    public User findByUsername(String username) {
        expenseDAO.ensureTableExists();
        String sql = "SELECT `id`, `username`, `password`, `created_at` FROM " + getTableName() + " WHERE `username` = ?";
        List<User> list = jdbcTemplate.query(sql, rowMapper, username);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Finds a user by their primary key ID.
     */
    public User findById(Long id) {
        expenseDAO.ensureTableExists();
        String sql = "SELECT `id`, `username`, `password`, `created_at` FROM " + getTableName() + " WHERE `id` = ?";
        List<User> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Registers a new user with a pre-hashed password.
     */
    public int createUser(String username, String encodedPassword) {
        expenseDAO.ensureTableExists();
        String sql = "INSERT INTO " + getTableName() + " (`username`, `password`) VALUES (?, ?)";
        return jdbcTemplate.update(sql, username, encodedPassword);
    }
}
