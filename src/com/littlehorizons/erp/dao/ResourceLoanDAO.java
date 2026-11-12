package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.ResourceLoan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for resource loans, covering both student and teacher loan tables.
 */
public class ResourceLoanDAO {

    public enum BorrowerType { ESTUDIANTE, PROFESOR }

    private static final String STUDENT_SELECT =
            "SELECT pe.*, r.nombre_recurso, " +
            "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_prestatario, " +
            "'Estudiante' AS tipo_prestatario FROM prestamos_estudiante pe " +
            "JOIN recursos r ON r.id_recurso = pe.id_recurso " +
            "JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante ";

    private static final String TEACHER_SELECT =
            "SELECT pp.*, r.nombre_recurso, " +
            "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_prestatario, " +
            "'Profesor' AS tipo_prestatario FROM prestamos_profesor pp " +
            "JOIN recursos r ON r.id_recurso = pp.id_recurso " +
            "JOIN profesores p ON p.id_profesor = pp.id_profesor ";

    public int insert(ResourceLoan loan, BorrowerType borrowerType) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return insert(loan, borrowerType, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el préstamo de recurso", ex);
        }
    }

    /**
     * Inserts a loan using an externally managed connection (transaction-aware).
     */
    public int insert(ResourceLoan loan, BorrowerType borrowerType, Connection conn) {
        String table = borrowerType == BorrowerType.ESTUDIANTE ? "prestamos_estudiante" : "prestamos_profesor";
        String borrowerColumn = borrowerType == BorrowerType.ESTUDIANTE ? "id_estudiante" : "id_profesor";
        String sql = "INSERT INTO " + table + " (id_recurso, " + borrowerColumn + ", fecha_prestamo, " +
                "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getResourceId());
            ps.setInt(2, borrowerType == BorrowerType.ESTUDIANTE ? loan.getStudentId() : loan.getTeacherId());
            ps.setDate(3, Date.valueOf(loan.getLoanDate()));
            ps.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
            ps.setString(5, loan.getLoanStatus());
            ps.setString(6, loan.getObservations());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    loan.setLoanId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del préstamo");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el préstamo de recurso", ex);
        }
    }

    public boolean registerReturn(int loanId, BorrowerType borrowerType, java.time.LocalDate returnDate) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return registerReturn(loanId, borrowerType, returnDate, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la devolución del recurso", ex);
        }
    }

    /**
     * Registers a return using an externally managed connection.
     */
    public boolean registerReturn(int loanId, BorrowerType borrowerType, java.time.LocalDate returnDate,
                                  Connection conn) {
        String table = borrowerType == BorrowerType.ESTUDIANTE ? "prestamos_estudiante" : "prestamos_profesor";
        String sql = "UPDATE " + table + " SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto' " +
                "WHERE id_prestamo = ? AND estado_prestamo <> 'Devuelto'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setInt(2, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la devolución del recurso", ex);
        }
    }

    public boolean markOverdue(int loanId, BorrowerType borrowerType) {
        String table = borrowerType == BorrowerType.ESTUDIANTE ? "prestamos_estudiante" : "prestamos_profesor";
        String sql = "UPDATE " + table + " SET estado_prestamo = 'Vencido' WHERE id_prestamo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al marcar el préstamo como vencido", ex);
        }
    }

    public List<ResourceLoan> findAll() {
        List<ResourceLoan> loans = new ArrayList<>();
        loans.addAll(queryAll(STUDENT_SELECT));
        loans.addAll(queryAll(TEACHER_SELECT));
        return loans;
    }

    public List<ResourceLoan> findActive() {
        List<ResourceLoan> loans = new ArrayList<>();
        loans.addAll(queryAll(STUDENT_SELECT + "WHERE pe.estado_prestamo IN ('Activo','Vencido')"));
        loans.addAll(queryAll(TEACHER_SELECT + "WHERE pp.estado_prestamo IN ('Activo','Vencido')"));
        return loans;
    }

    public List<ResourceLoan> findOverdue(java.time.LocalDate referenceDate) {
        List<ResourceLoan> loans = new ArrayList<>();
        loans.addAll(queryOverdue(STUDENT_SELECT +
                "WHERE pe.estado_prestamo IN ('Activo','Vencido') AND pe.fecha_devolucion_esperada < ?", referenceDate));
        loans.addAll(queryOverdue(TEACHER_SELECT +
                "WHERE pp.estado_prestamo IN ('Activo','Vencido') AND pp.fecha_devolucion_esperada < ?", referenceDate));
        return loans;
    }

    public List<ResourceLoan> findByStudent(int studentId) {
        return queryById(STUDENT_SELECT + "WHERE pe.id_estudiante = ? ORDER BY pe.fecha_prestamo DESC", studentId);
    }

    public List<ResourceLoan> findByTeacher(int teacherId) {
        return queryById(TEACHER_SELECT + "WHERE pp.id_profesor = ? ORDER BY pp.fecha_prestamo DESC", teacherId);
    }

    public boolean hasActiveLoanForResource(int resourceId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return hasActiveLoanForResource(resourceId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar préstamos activos del recurso", ex);
        }
    }

    /**
     * Checks active loans using an externally managed connection.
     */
    public boolean hasActiveLoanForResource(int resourceId, Connection conn) {
        String sql = "SELECT (SELECT COUNT(*) FROM prestamos_estudiante WHERE id_recurso = ? AND estado_prestamo IN ('Activo','Vencido')) + " +
                "(SELECT COUNT(*) FROM prestamos_profesor WHERE id_recurso = ? AND estado_prestamo IN ('Activo','Vencido')) AS total";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar préstamos activos del recurso", ex);
        }
    }

    private List<ResourceLoan> queryAll(String sql) {
        List<ResourceLoan> loans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                loans.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los préstamos", ex);
        }
        return loans;
    }

    private List<ResourceLoan> queryOverdue(String sql, java.time.LocalDate referenceDate) {
        List<ResourceLoan> loans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(referenceDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los préstamos vencidos", ex);
        }
        return loans;
    }

    private List<ResourceLoan> queryById(String sql, int id) {
        List<ResourceLoan> loans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar los préstamos del prestatario", ex);
        }
        return loans;
    }

    private ResourceLoan map(ResultSet rs) throws SQLException {
        ResourceLoan l = new ResourceLoan();
        l.setLoanId(rs.getInt("id_prestamo"));
        l.setResourceId(rs.getInt("id_recurso"));
        String type = rs.getString("tipo_prestatario");
        l.setBorrowerType(type);
        if ("Estudiante".equalsIgnoreCase(type)) {
            l.setStudentId(rs.getInt("id_estudiante"));
        } else {
            l.setTeacherId(rs.getInt("id_profesor"));
        }
        Date loanDate = rs.getDate("fecha_prestamo");
        l.setLoanDate(loanDate != null ? loanDate.toLocalDate() : null);
        Date expected = rs.getDate("fecha_devolucion_esperada");
        l.setExpectedReturnDate(expected != null ? expected.toLocalDate() : null);
        Date actual = rs.getDate("fecha_devolucion_real");
        l.setActualReturnDate(actual != null ? actual.toLocalDate() : null);
        l.setLoanStatus(rs.getString("estado_prestamo"));
        l.setObservations(rs.getString("observaciones"));
        l.setResourceName(rs.getString("nombre_recurso"));
        l.setBorrowerName(rs.getString("nombre_prestatario"));
        return l;
    }
}
