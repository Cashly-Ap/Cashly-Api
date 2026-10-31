package com.cashly.cashly_api.platform.auth.authorization.infrastructure.persistence.adapters;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class JdbcAccessRepository implements AccessRepository {
    private final JdbcTemplate jdbc;

    public JdbcAccessRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static UUID uuid(byte[] bytes) {
        var buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    private Role mapRole(ResultSet rs, int row) throws SQLException {
        return new Role(uuid(rs.getBytes("id")), rs.getString("name"),
                rs.getString("description"), rs.getBoolean("active"),
                rs.getLong("version"), rs.getString("system_key"), Map.of());
    }

    private Role withPermissions(Role role,
            Map<BusinessModule, Set<PermissionAction>> permissions) {
        return new Role(role.id(), role.name(), role.description(),
                role.active(), role.version(), role.systemKey(), permissions);
    }

    private void addGrant(ResultSet row,
            Map<BusinessModule, Set<PermissionAction>> grants)
            throws SQLException {
        grants.computeIfAbsent(BusinessModule.valueOf(row.getString("module")),
                key -> EnumSet.noneOf(PermissionAction.class))
                .add(PermissionAction.valueOf(row.getString("action")));
    }

    @Override
    public Optional<Role> role(UUID id) {
        var role = jdbc.query("select * from auth_roles where id=?",
                this::mapRole, bytes(id)).stream().findFirst();
        if (role.isEmpty()) {
            return Optional.empty();
        }
        Map<BusinessModule, Set<PermissionAction>> grants = new EnumMap<>(
                BusinessModule.class);
        jdbc.query(
                "select module, action from auth_role_permissions where role_id=?",
                (RowCallbackHandler) row -> addGrant(row, grants), bytes(id));
        return Optional.of(withPermissions(role.get(), grants));
    }

    @Override
    public List<Role> roles() {
        return roles(false);
    }

    @Override
    public List<Role> editableRoles() {
        return roles(true);
    }

    private List<Role> roles(boolean editableOnly) {
        var roles = jdbc.query("select * from auth_roles"
                + (editableOnly ? " where system_key IS NULL" : "")
                + " order by name", this::mapRole);
        Map<UUID, Map<BusinessModule, Set<PermissionAction>>> allGrants = new HashMap<>();
        jdbc.query("select role_id,module,action from auth_role_permissions",
                (RowCallbackHandler) row -> {
                    var grants = allGrants.computeIfAbsent(
                            uuid(row.getBytes("role_id")),
                            key -> new EnumMap<>(BusinessModule.class));
                    addGrant(row, grants);
                });
        return roles.stream().map(role -> withPermissions(role,
                allGrants.getOrDefault(role.id(), Map.of()))).toList();
    }

    @Override
    public void saveRole(Role role, boolean create) {
        if (create) {
            jdbc.update(
                    "insert into auth_roles(id,name,description,active,version,system_key) values(?,?,?,?,0,?)",
                    bytes(role.id()), role.name(), role.description(),
                    role.active(), role.systemKey());
        } else {
            int changed = jdbc.update(
                    """
                            update auth_roles set name=?,description=?,active=?,version=version+1 where id=? and version=?
                            """,
                    role.name(), role.description(), role.active(),
                    bytes(role.id()), role.version());
            if (changed != 1) {
                conflict();
            }
        }
        jdbc.update("delete from auth_role_permissions where role_id=?",
                bytes(role.id()));
        role.permissions().forEach((module,
                actions) -> actions.forEach(action -> jdbc.update(
                        "insert into auth_role_permissions(role_id,module,action) values(?,?,?)",
                        bytes(role.id()), module.name(), action.name())));
    }

    private Department mapDepartment(ResultSet rs, int row)
            throws SQLException {
        return new Department(uuid(rs.getBytes("id")), rs.getString("name"),
                rs.getString("description"), rs.getBoolean("active"),
                rs.getLong("version"));
    }

    @Override
    public Optional<Department> department(UUID id) {
        return jdbc.query("select * from auth_departments where id=?",
                this::mapDepartment, bytes(id)).stream().findFirst();
    }

    @Override
    public List<Department> departments() {
        return jdbc.query("select * from auth_departments order by name",
                this::mapDepartment);
    }

    @Override
    public void saveDepartment(Department department, boolean create) {
        if (create) {
            jdbc.update(
                    "insert into auth_departments(id,name,description,active,version) values(?,?,?,?,0)",
                    bytes(department.id()), department.name(),
                    department.description(), department.active());
        } else {
            int changed = jdbc.update(
                    """
                            update auth_departments set name=?,description=?,active=?,version=version+1 where id=? and version=?
                            """,
                    department.name(), department.description(),
                    department.active(), bytes(department.id()),
                    department.version());
            if (changed != 1) {
                conflict();
            }
        }
    }

    @Override
    public void deleteRole(UUID id) {
        jdbc.update("delete from auth_roles where id=?", bytes(id));
    }

    @Override
    public void deleteDepartment(UUID id) {
        jdbc.update("delete from auth_departments where id=?", bytes(id));
    }

    @Override
    public boolean roleReferenced(UUID id) {
        return jdbc.queryForObject("select count(*) from users where role_id=?",
                Long.class, bytes(id)) > 0;
    }

    @Override
    public boolean departmentReferenced(UUID id) {
        return jdbc.queryForObject(
                "select count(*) from users where department_id=?", Long.class,
                bytes(id)) > 0;
    }

    @Override
    public void createAdministrationLockIfAbsent() {
        jdbc.update(
                "insert ignore into auth_administration_lock(id) values(1)");
    }

    @Override
    public void lockAdministration() {
        jdbc.queryForObject(
                "select id from auth_administration_lock where id=1 for update",
                Integer.class);
    }

    @Override
    public long activeSystemAdministrators() {
        return jdbc.queryForObject(
                """
                        select count(*) from users u join auth_roles r on r.id=u.role_id
                        where u.active=true and r.active=true and r.system_key='SYSTEM_ADMIN'
                        """,
                Long.class);
    }

    @Override
    public Optional<Assignment> assignment(UUID userId) {
        return jdbc.query(
                "select role_id,department_id,active from users where id=?",
                (rs, row) -> new Assignment(uuid(rs.getBytes(1)),
                        uuid(rs.getBytes(2)), rs.getBoolean(3)),
                bytes(userId)).stream().findFirst();
    }

    private static void conflict() {
        throw new AccessConflictException("Record changed; reload and retry");
    }
}
