package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Invoice;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for generated electronic invoices.
 */
public class InvoiceDAO {

    public int insert(Invoice invoice) {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_txt, ruta_archivo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceNumber());
            ps.setInt(2, invoice.getPaymentId());
            ps.setDate(3, Date.valueOf(invoice.getIssueDate()));
            ps.setString(4, invoice.getCufe());
            ps.setString(5, invoice.getFileName());
            ps.setString(6, invoice.getFilePath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    invoice.setInvoiceId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la factura");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la factura electrónica", ex);
        }
    }

    public Optional<Invoice> findByPayment(int paymentId) {
        String sql = "SELECT * FROM facturas WHERE id_pago = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar la factura del pago", ex);
        }
    }

    public List<Invoice> findAll() {
        String sql = "SELECT * FROM facturas ORDER BY fecha_emision DESC";
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoices.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las facturas", ex);
        }
        return invoices;
    }

    /**
     * Generates the next consecutive invoice number based on the current maximum.
     */
    public String nextInvoiceNumber() {
        String sql = "SELECT COUNT(*) + 1 AS siguiente FROM facturas";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = rs.next() ? rs.getInt("siguiente") : 1;
            return String.format("FAC-%06d", next);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el consecutivo de factura", ex);
        }
    }

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setInvoiceId(rs.getInt("id_factura"));
        i.setInvoiceNumber(rs.getString("numero_factura"));
        i.setPaymentId(rs.getInt("id_pago"));
        Date date = rs.getDate("fecha_emision");
        i.setIssueDate(date != null ? date.toLocalDate() : null);
        i.setCufe(rs.getString("cufe"));
        i.setFileName(rs.getString("nombre_archivo_txt"));
        i.setFilePath(rs.getString("ruta_archivo"));
        return i;
    }
}
