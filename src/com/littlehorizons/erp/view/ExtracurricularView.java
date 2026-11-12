package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for extracurricular clubs and events.
 */
public class ExtracurricularView {

    private final InputValidator input;
    private final ExtracurricularController controller = new ExtracurricularController();

    public ExtracurricularView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("ACTIVIDADES EXTRACURRICULARES");
            System.out.println("1. Registrar club");
            System.out.println("2. Listar clubes");
            System.out.println("3. Inscribir estudiante en club");
            System.out.println("4. Retirar estudiante de club");
            System.out.println("5. Consultar membresías de un club");
            System.out.println("6. Registrar evento");
            System.out.println("7. Listar eventos");
            System.out.println("8. Registrar participante en evento");
            System.out.println("9. Actualizar posición en competencia");
            System.out.println("10. Consultar participantes de un evento");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 10);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("ExtracurricularView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("ExtracurricularView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerClub();
                break;
            case 2:
                listClubs();
                break;
            case 3:
                enrollInClub();
                break;
            case 4:
                withdrawFromClub();
                break;
            case 5:
                listMemberships();
                break;
            case 6:
                registerEvent();
                break;
            case 7:
                listEvents();
                break;
            case 8:
                registerParticipant();
                break;
            case 9:
                updatePosition();
                break;
            case 10:
                listParticipants();
                break;
            default:
                break;
        }
    }

    private void registerClub() {
        ConsolePrinter.printSectionHeader("NUEVO CLUB");
        Club club = new Club();
        club.setClubName(input.readNonEmptyString("Nombre del club: "));
        club.setDescription(input.readOptionalString("Descripción: "));
        if (input.readBooleanYN("¿Desea asignar un profesor encargado?")) {
            club.setSupervisorTeacherId(input.readPositiveInt("ID del profesor encargado: "));
        }
        club.setMaxCapacity(input.readInt("Capacidad máxima: ", 1, 200));
        club.setMeetingSchedule(input.readOptionalString("Horario de reunión (ej: Martes 3-5 PM): "));
        club.setActive(input.readBooleanYN("¿Club activo?"));
        int id = controller.registerClub(club);
        ConsolePrinter.printSuccess("Club registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listClubs() {
        ConsolePrinter.printSectionHeader("CATÁLOGO DE CLUBES");
        List<Club> clubs = controller.listClubs();
        if (clubs.isEmpty()) {
            ConsolePrinter.printWarning("No hay clubes registrados.");
        } else {
            clubs.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void enrollInClub() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        ConsolePrinter.printInfo("Clubes disponibles:");
        controller.listClubs().forEach(System.out::println);
        int clubId = input.readPositiveInt("ID del club: ");
        LocalDate joinDate = input.readDate("Fecha de ingreso", false);
        int id = controller.enrollStudentInClub(studentId, clubId, joinDate);
        ConsolePrinter.printSuccess("Membresía registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void withdrawFromClub() {
        int membershipId = input.readPositiveInt("ID de la membresía: ");
        LocalDate date = input.readDate("Fecha de retiro", false);
        String reason = input.readOptionalString("Motivo del retiro (opcional): ");
        controller.withdrawStudentFromClub(membershipId, date, reason);
        ConsolePrinter.printSuccess("Retiro registrado correctamente.");
        input.waitForEnter();
    }

    private void listMemberships() {
        int clubId = input.readPositiveInt("ID del club: ");
        List<ClubMembership> memberships = controller.listMembershipsByClub(clubId);
        ConsolePrinter.printSectionHeader("MEMBRESÍAS DEL CLUB");
        if (memberships.isEmpty()) {
            ConsolePrinter.printWarning("El club no tiene membresías registradas.");
        } else {
            memberships.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void registerEvent() {
        ConsolePrinter.printSectionHeader("NUEVO EVENTO");
        SchoolEvent event = new SchoolEvent();
        event.setEventName(input.readNonEmptyString("Nombre del evento: "));
        event.setDescription(input.readOptionalString("Descripción: "));
        event.setEventType(input.readChoice("Tipo de evento",
                Arrays.asList("Académico", "Cultural", "Deportivo", "Social", "Científico")));
        event.setEventDate(input.readDate("Fecha del evento", true));
        event.setLocation(input.readOptionalString("Lugar: "));
        event.setHasCompetition(input.readBooleanYN("¿Tiene competencia?"));
        if (input.readBooleanYN("¿Desea asignar un profesor organizador?")) {
            event.setOrganizerTeacherId(input.readPositiveInt("ID del profesor organizador: "));
        }
        event.setActive(true);
        int id = controller.registerEvent(event);
        ConsolePrinter.printSuccess("Evento registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listEvents() {
        ConsolePrinter.printSectionHeader("CALENDARIO DE EVENTOS");
        List<SchoolEvent> events = controller.listEvents();
        if (events.isEmpty()) {
            ConsolePrinter.printWarning("No hay eventos registrados.");
        } else {
            events.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void registerParticipant() {
        int eventId = input.readPositiveInt("ID del evento: ");
        SchoolEvent event = controller.findEvent(eventId);
        int studentId = input.readPositiveInt("ID del estudiante: ");
        String role = input.readChoice("Rol de participación",
                Arrays.asList("Participante", "Competidor", "Organizador", "Asistente"));
        String position = null;
        if (event.isHasCompetition()
                && input.readBooleanYN("¿Desea registrar posición obtenida?")) {
            position = input.readChoice("Posición",
                    Arrays.asList("1er Lugar", "2do Lugar", "3er Lugar", "Participante"));
        }
        String observations = input.readOptionalString("Observaciones (opcional): ");
        int id = controller.registerEventParticipant(eventId, studentId, role, position, observations);
        ConsolePrinter.printSuccess("Participación registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void updatePosition() {
        int participationId = input.readPositiveInt("ID de la participación: ");
        String position = input.readChoice("Nueva posición",
                Arrays.asList("1er Lugar", "2do Lugar", "3er Lugar", "Participante"));
        controller.updateParticipantPosition(participationId, position);
        ConsolePrinter.printSuccess("Posición actualizada correctamente.");
        input.waitForEnter();
    }

    private void listParticipants() {
        int eventId = input.readPositiveInt("ID del evento: ");
        List<EventParticipant> participants = controller.listParticipantsByEvent(eventId);
        ConsolePrinter.printSectionHeader("PARTICIPANTES DEL EVENTO");
        if (participants.isEmpty()) {
            ConsolePrinter.printWarning("El evento no tiene participantes registrados.");
        } else {
            participants.forEach(System.out::println);
        }
        input.waitForEnter();
    }
}
