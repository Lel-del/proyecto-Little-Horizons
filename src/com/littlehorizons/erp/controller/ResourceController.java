package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceCategoryDAO;
import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.dao.ResourceLoanDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.util.AppLogger;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic controller for the library and laboratory resource module.
 */
public class ResourceController {

    private static final List<String> VALID_STATUSES =
            Arrays.asList("Disponible", "Prestado", "En Mantenimiento", "Dado de Baja");

    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ResourceCategoryDAO categoryDAO = new ResourceCategoryDAO();
    private final ResourceLoanDAO loanDAO = new ResourceLoanDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    public int registerResource(Resource resource) {
        validateResource(resource);
        if (resourceDAO.existsCode(resource.getResourceCode())) {
            throw new IllegalArgumentException("Ya existe un recurso con el código " + resource.getResourceCode());
        }
        int id = resourceDAO.insert(resource);
        AppLogger.getInstance().logInfo("ResourceController.registerResource", "Recurso registrado ID " + id);
        return id;
    }

    public boolean updateResource(Resource resource) {
        validateResource(resource);
        if (resource.getResourceId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un recurso válido para actualizar.");
        }
        return resourceDAO.update(resource);
    }

    public boolean changeResourceStatus(int resourceId, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado inválido. Use: " + VALID_STATUSES);
        }
        return resourceDAO.updateStatus(resourceId, status);
    }

    public boolean deleteResource(int resourceId) {
        if (loanDAO.hasActiveLoanForResource(resourceId)) {
            throw new IllegalStateException("No se puede eliminar un recurso con préstamos activos.");
        }
        return resourceDAO.delete(resourceId);
    }

    public Resource findResource(int resourceId) {
        return resourceDAO.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el recurso con ID " + resourceId));
    }

    public List<Resource> listResources() {
        return resourceDAO.findAll();
    }

    public List<Resource> listResourcesByStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado inválido. Use: " + VALID_STATUSES);
        }
        return resourceDAO.findByStatus(status);
    }

    public List<Resource> listResourcesByCategory(int categoryId) {
        return resourceDAO.findByCategory(categoryId);
    }

    public List<ResourceCategory> listCategories() {
        return categoryDAO.findAll();
    }

    public int registerCategory(ResourceCategory category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        return categoryDAO.insert(category);
    }

    /**
     * Registers a loan to a student, validating availability of the resource.
     */
    public int loanToStudent(int resourceId, int studentId, LocalDate loanDate,
                             LocalDate expectedReturnDate, String observations) {
        Resource resource = findResource(resourceId);
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        validateLoanDates(loanDate, expectedReturnDate);
        validateResourceAvailable(resource);
        ResourceLoan loan = new ResourceLoan();
        loan.setResourceId(resourceId);
        loan.setStudentId(studentId);
        loan.setLoanDate(loanDate);
        loan.setExpectedReturnDate(expectedReturnDate);
        loan.setLoanStatus("Activo");
        loan.setObservations(observations);
        int id = loanDAO.insert(loan, ResourceLoanDAO.BorrowerType.ESTUDIANTE);
        resourceDAO.updateStatus(resourceId, "Prestado");
        AppLogger.getInstance().logInfo("ResourceController.loanToStudent",
                "Préstamo " + id + " del recurso " + resourceId + " al estudiante " + studentId);
        return id;
    }

    /**
     * Registers a loan to a teacher, validating availability of the resource.
     */
    public int loanToTeacher(int resourceId, int teacherId, LocalDate loanDate,
                             LocalDate expectedReturnDate, String observations) {
        Resource resource = findResource(resourceId);
        teacherDAO.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor con ID " + teacherId));
        validateLoanDates(loanDate, expectedReturnDate);
        validateResourceAvailable(resource);
        ResourceLoan loan = new ResourceLoan();
        loan.setResourceId(resourceId);
        loan.setTeacherId(teacherId);
        loan.setLoanDate(loanDate);
        loan.setExpectedReturnDate(expectedReturnDate);
        loan.setLoanStatus("Activo");
        loan.setObservations(observations);
        int id = loanDAO.insert(loan, ResourceLoanDAO.BorrowerType.PROFESOR);
        resourceDAO.updateStatus(resourceId, "Prestado");
        AppLogger.getInstance().logInfo("ResourceController.loanToTeacher",
                "Préstamo " + id + " del recurso " + resourceId + " al profesor " + teacherId);
        return id;
    }

    public boolean registerReturn(ResourceLoan loan, LocalDate returnDate) {
        if (loan == null) {
            throw new IllegalArgumentException("Debe seleccionar un préstamo válido.");
        }
        if ("Devuelto".equalsIgnoreCase(loan.getLoanStatus())) {
            throw new IllegalStateException("El préstamo seleccionado ya fue devuelto.");
        }
        ResourceLoanDAO.BorrowerType type = "Profesor".equalsIgnoreCase(loan.getBorrowerType())
                ? ResourceLoanDAO.BorrowerType.PROFESOR
                : ResourceLoanDAO.BorrowerType.ESTUDIANTE;
        boolean updated = loanDAO.registerReturn(loan.getLoanId(), type,
                returnDate != null ? returnDate : LocalDate.now());
        if (updated) {
            resourceDAO.updateStatus(loan.getResourceId(), "Disponible");
            AppLogger.getInstance().logInfo("ResourceController.registerReturn",
                    "Devolución registrada para préstamo " + loan.getLoanId());
        }
        return updated;
    }

    public List<ResourceLoan> listAllLoans() {
        return loanDAO.findAll();
    }

    public List<ResourceLoan> listActiveLoans() {
        return loanDAO.findActive();
    }

    public List<ResourceLoan> listLoansByStudent(int studentId) {
        return loanDAO.findByStudent(studentId);
    }

    public List<ResourceLoan> listLoansByTeacher(int teacherId) {
        return loanDAO.findByTeacher(teacherId);
    }

    /**
     * Overdue loans and marks them as Vencido.
     */
    public List<ResourceLoan> generateOverdueReport(LocalDate referenceDate) {
        LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now();
        List<ResourceLoan> overdue = loanDAO.findOverdue(reference);
        for (ResourceLoan loan : overdue) {
            ResourceLoanDAO.BorrowerType type = "Profesor".equalsIgnoreCase(loan.getBorrowerType())
                    ? ResourceLoanDAO.BorrowerType.PROFESOR
                    : ResourceLoanDAO.BorrowerType.ESTUDIANTE;
            if (!"Vencido".equalsIgnoreCase(loan.getLoanStatus())) {
                loanDAO.markOverdue(loan.getLoanId(), type);
            }
        }
        return overdue;
    }

    private void validateResourceAvailable(Resource resource) {
        if (!"Disponible".equalsIgnoreCase(resource.getResourceStatus())) {
            throw new IllegalStateException("El recurso no está disponible. Estado actual: " + resource.getResourceStatus());
        }
        if (loanDAO.hasActiveLoanForResource(resource.getResourceId())) {
            throw new IllegalStateException("El recurso ya tiene un préstamo activo registrado.");
        }
    }

    private void validateLoanDates(LocalDate loanDate, LocalDate expectedReturnDate) {
        if (loanDate == null || expectedReturnDate == null) {
            throw new IllegalArgumentException("Las fechas de préstamo y devolución son obligatorias.");
        }
        if (loanDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de préstamo no puede ser futura.");
        }
        if (!expectedReturnDate.isAfter(loanDate)) {
            throw new IllegalArgumentException("La fecha de devolución esperada debe ser posterior al préstamo.");
        }
    }

    private void validateResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Los datos del recurso son obligatorios.");
        }
        if (resource.getResourceCode() == null || resource.getResourceCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del recurso es obligatorio.");
        }
        if (resource.getResourceName() == null || resource.getResourceName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio.");
        }
        if (resource.getResourceStatus() == null || !VALID_STATUSES.contains(resource.getResourceStatus())) {
            throw new IllegalArgumentException("Estado del recurso inválido. Use: " + VALID_STATUSES);
        }
        boolean categoryExists = categoryDAO.findAll().stream()
                .anyMatch(c -> c.getCategoryId() == resource.getCategoryId());
        if (!categoryExists) {
            throw new IllegalArgumentException("La categoría del recurso no existe.");
        }
    }
}
