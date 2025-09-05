package pe.crediactiva.util;

import pe.crediactiva.config.AppConfig;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades para manejo de fechas en CrediActiva.
 * Incluye lógica específica del negocio como exclusión de domingos.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class DateUtils {
    
    private static final ZoneId ZONA_HORARIA = ZoneId.of(AppConfig.getTimezone());
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Obtiene la fecha actual en la zona horaria de la aplicación.
     * 
     * @return fecha actual
     */
    public static LocalDate hoy() {
        return LocalDate.now(ZONA_HORARIA);
    }
    
    /**
     * Obtiene la fecha y hora actual en la zona horaria de la aplicación.
     * 
     * @return fecha y hora actual
     */
    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_HORARIA);
    }
    
    /**
     * Ajusta una fecha para que no caiga en domingo.
     * Si la fecha es domingo, la mueve al lunes siguiente.
     * 
     * @param fecha fecha a ajustar
     * @return fecha ajustada (nunca domingo)
     */
    public static LocalDate ajustarSinDomingo(LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        
        // Si es domingo (DayOfWeek.SUNDAY = 7), mover al lunes
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return fecha.plusDays(1);
        }
        
        return fecha;
    }
    
    /**
     * Agrega meses a una fecha y ajusta para que no caiga en domingo.
     * 
     * @param fechaBase fecha base
     * @param meses meses a agregar
     * @return fecha ajustada sin domingos
     */
    public static LocalDate agregarMesesSinDomingo(LocalDate fechaBase, int meses) {
        if (fechaBase == null) {
            return null;
        }
        
        LocalDate fechaResultado = fechaBase.plusMonths(meses);
        return ajustarSinDomingo(fechaResultado);
    }
    
    /**
     * Calcula los días de diferencia entre dos fechas.
     * 
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return días de diferencia (positivo si fechaFin es posterior)
     */
    public static long diasEntre(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }
    
    /**
     * Calcula los días de atraso desde una fecha de vencimiento.
     * 
     * @param fechaVencimiento fecha de vencimiento
     * @return días de atraso (0 si no está vencido, positivo si está vencido)
     */
    public static int calcularDiasAtraso(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            return 0;
        }
        
        long dias = diasEntre(fechaVencimiento, hoy());
        return Math.max(0, (int) dias);
    }
    
    /**
     * Verifica si una fecha está vencida.
     * 
     * @param fechaVencimiento fecha de vencimiento
     * @return true si está vencida
     */
    public static boolean estaVencida(LocalDate fechaVencimiento) {
        return fechaVencimiento != null && fechaVencimiento.isBefore(hoy());
    }
    
    /**
     * Verifica si una fecha está dentro del plazo (hoy o futuro).
     * 
     * @param fechaVencimiento fecha de vencimiento
     * @return true si está dentro del plazo
     */
    public static boolean estaDentroPlazo(LocalDate fechaVencimiento) {
        return fechaVencimiento != null && !fechaVencimiento.isBefore(hoy());
    }
    
    /**
     * Formatea una fecha para mostrar en la interfaz.
     * 
     * @param fecha fecha a formatear
     * @return fecha formateada o cadena vacía si es null
     */
    public static String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }
    
    /**
     * Formatea una fecha y hora para mostrar en la interfaz.
     * 
     * @param fechaHora fecha y hora a formatear
     * @return fecha y hora formateada o cadena vacía si es null
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(FORMATO_FECHA_HORA) : "";
    }
    
    /**
     * Parsea una fecha desde un string.
     * 
     * @param fechaStr fecha como string (formato dd/MM/yyyy)
     * @return fecha parseada o null si no se puede parsear
     */
    public static LocalDate parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(fechaStr.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parsea una fecha y hora desde un string.
     * 
     * @param fechaHoraStr fecha y hora como string (formato dd/MM/yyyy HH:mm:ss)
     * @return fecha y hora parseada o null si no se puede parsear
     */
    public static LocalDateTime parsearFechaHora(String fechaHoraStr) {
        if (fechaHoraStr == null || fechaHoraStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(fechaHoraStr.trim(), FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Obtiene el primer día del mes actual.
     * 
     * @return primer día del mes
     */
    public static LocalDate primerDiaDelMes() {
        return hoy().withDayOfMonth(1);
    }
    
    /**
     * Obtiene el último día del mes actual.
     * 
     * @return último día del mes
     */
    public static LocalDate ultimoDiaDelMes() {
        LocalDate hoy = hoy();
        return hoy.withDayOfMonth(hoy.lengthOfMonth());
    }
    
    /**
     * Obtiene el primer día del mes de una fecha específica.
     * 
     * @param fecha fecha de referencia
     * @return primer día del mes
     */
    public static LocalDate primerDiaDelMes(LocalDate fecha) {
        return fecha != null ? fecha.withDayOfMonth(1) : null;
    }
    
    /**
     * Obtiene el último día del mes de una fecha específica.
     * 
     * @param fecha fecha de referencia
     * @return último día del mes
     */
    public static LocalDate ultimoDiaDelMes(LocalDate fecha) {
        return fecha != null ? fecha.withDayOfMonth(fecha.lengthOfMonth()) : null;
    }
    
    /**
     * Verifica si una fecha está en el rango especificado (inclusivo).
     * 
     * @param fecha fecha a verificar
     * @param fechaInicio inicio del rango
     * @param fechaFin fin del rango
     * @return true si está en el rango
     */
    public static boolean estaEnRango(LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fecha == null || fechaInicio == null || fechaFin == null) {
            return false;
        }
        
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
    
    /**
     * Convierte LocalDate a java.sql.Date para uso con JDBC.
     * 
     * @param localDate fecha local
     * @return java.sql.Date o null si la entrada es null
     */
    public static java.sql.Date toSqlDate(LocalDate localDate) {
        return localDate != null ? java.sql.Date.valueOf(localDate) : null;
    }
    
    /**
     * Convierte LocalDateTime a java.sql.Timestamp para uso con JDBC.
     * 
     * @param localDateTime fecha y hora local
     * @return java.sql.Timestamp o null si la entrada es null
     */
    public static java.sql.Timestamp toSqlTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? java.sql.Timestamp.valueOf(localDateTime) : null;
    }
    
    /**
     * Convierte java.sql.Date a LocalDate.
     * 
     * @param sqlDate fecha SQL
     * @return LocalDate o null si la entrada es null
     */
    public static LocalDate fromSqlDate(java.sql.Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }
    
    /**
     * Convierte java.sql.Timestamp a LocalDateTime.
     * 
     * @param sqlTimestamp timestamp SQL
     * @return LocalDateTime o null si la entrada es null
     */
    public static LocalDateTime fromSqlTimestamp(java.sql.Timestamp sqlTimestamp) {
        return sqlTimestamp != null ? sqlTimestamp.toLocalDateTime() : null;
    }
    
    /**
     * Obtiene el nombre del día de la semana en español.
     * 
     * @param fecha fecha
     * @return nombre del día
     */
    public static String getNombreDia(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
    
    /**
     * Obtiene el nombre del mes en español.
     * 
     * @param fecha fecha
     * @return nombre del mes
     */
    public static String getNombreMes(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        
        return switch (fecha.getMonth()) {
            case JANUARY -> "Enero";
            case FEBRUARY -> "Febrero";
            case MARCH -> "Marzo";
            case APRIL -> "Abril";
            case MAY -> "Mayo";
            case JUNE -> "Junio";
            case JULY -> "Julio";
            case AUGUST -> "Agosto";
            case SEPTEMBER -> "Septiembre";
            case OCTOBER -> "Octubre";
            case NOVEMBER -> "Noviembre";
            case DECEMBER -> "Diciembre";
        };
    }
}


