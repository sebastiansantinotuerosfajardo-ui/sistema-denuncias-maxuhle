package com.maxuhle.denuncias.config;

import com.maxuhle.denuncias.model.Denuncia;
import com.maxuhle.denuncias.model.Seguimiento;
import com.maxuhle.denuncias.model.Usuario;
import com.maxuhle.denuncias.repository.DenunciaRepository;
import com.maxuhle.denuncias.repository.SeguimientoRepository;
import com.maxuhle.denuncias.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Datos de demostración (SOLO FICTICIOS: el sistema trata con menores de edad).
 *
 * Solo corre con el perfil "seed" (SPRING_PROFILES_ACTIVE=prod,seed) y no hace nada si ya existen denuncias,
 * por lo que se puede dejar activo por error sin duplicar datos. Las contraseñas de los usuarios demo
 * NO están en el código: se leen de SEED_ADMIN_PASSWORD y SEED_OPERADOR_PASSWORD.
 *
 * Genera 5 usuarios, 120 denuncias de los últimos 6 meses y unos 400 seguimientos coherentes.
 * Es determinista (semilla fija): cada ejecución sobre una base vacía produce el mismo conjunto.
 */
@Component
@Profile("seed")
public class DemoDataSeeder implements CommandLineRunner {

    private static final int TOTAL_DENUNCIAS = 120;
    private static final int DIAS_ATRAS = 180;

    private final UsuarioRepository usuarioRepository;
    private final DenunciaRepository denunciaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate tx;

    public DemoDataSeeder(UsuarioRepository usuarioRepository, DenunciaRepository denunciaRepository,
                          SeguimientoRepository seguimientoRepository, PasswordEncoder passwordEncoder,
                          TransactionTemplate tx) {
        this.usuarioRepository = usuarioRepository;
        this.denunciaRepository = denunciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.tx = tx;
    }

    // ---------- Datos ficticios ----------
    private static final String[] NOMBRES = {"Camila", "Valeria", "Sofía", "Lucía", "Fernanda", "Daniela", "Andrea",
            "Mariana", "Rosa", "Milagros", "Jimena", "Alejandra", "Luis", "Diego", "Mateo", "Sebastián", "Gabriel",
            "Joaquín", "Renato", "Bruno", "Adrián", "Thiago", "Jhon", "Piero", "Marco", "Anthony", "Fabrizio", "Kevin"};
    private static final String[] APELLIDOS = {"Quispe", "Mamani", "Huamán", "Flores", "Rodríguez", "Chávez", "Condori",
            "Ccori", "Ramos", "Vargas", "Gutiérrez", "Paredes", "Cáceres", "Valdivia", "Zeballos", "Medina", "Salas",
            "Choque", "Apaza", "Torres", "Delgado", "Cornejo", "Núñez", "Ponce", "Linares", "Béjar", "Ccama", "Arce"};
    private static final String[] GRADOS = {"1.° de primaria", "2.° de primaria", "3.° de primaria", "4.° de primaria",
            "5.° de primaria", "6.° de primaria", "1.° de secundaria", "2.° de secundaria", "3.° de secundaria",
            "4.° de secundaria", "5.° de secundaria"};
    private static final String[] LUGARES = {"el patio", "el aula", "el pasillo", "los servicios higiénicos", "el comedor",
            "la salida del colegio", "el paradero escolar", "el chat del salón", "el laboratorio", "la cancha deportiva"};

    private static final String[] DESC_ACOSO = {
            "Un grupo de compañeros apodan y se burlan del afectado todos los días en %s, y ya no quiere venir a clases.",
            "El afectado es excluido de forma constante de los trabajos grupales y recibe burlas en %s.",
            "Desde hace tres semanas le esconden sus útiles y lo molestan repetidamente en %s.",
            "El afectado cuenta que lo siguen y lo amenazan con dejarlo solo si no les da su refrigerio en %s.",
            "Circulan mensajes y memes ofensivos sobre el afectado en el grupo del salón, y las burlas continúan en %s."};
    private static final String[] DESC_CONVIVENCIA = {
            "Discusión entre dos estudiantes en %s por un trabajo grupal; piden mediación de un tutor.",
            "Los estudiantes del grado no respetan los turnos de uso de %s y generan conflictos frecuentes.",
            "Un estudiante interrumpe constantemente las clases y falta el respeto a sus compañeros en %s.",
            "Hubo un malentendido entre compañeras en %s que terminó en gritos; solicitan conversar con la psicóloga.",
            "Se reporta desorden y uso inadecuado del lenguaje entre un grupo de estudiantes en %s."};
    private static final String[] DESC_PSICOLOGICO = {
            "El afectado refiere que una persona del entorno escolar lo humilla frente a otros en %s y se siente muy triste.",
            "Se observa que el afectado llora con frecuencia y dice que le dicen palabras hirientes en %s.",
            "El afectado manifiesta temor de asistir al colegio por comentarios que lo hacen sentir menospreciado en %s.",
            "Una compañera comenta que el afectado recibe insultos y comparaciones constantes en %s."};
    private static final String[] DESC_FISICO = {
            "Se reporta un empujón fuerte entre estudiantes en %s; el afectado tiene un moretón leve en el brazo.",
            "Durante el recreo hubo un forcejeo en %s y el afectado salió con un rasguño; fue atendido en tópico.",
            "El afectado indica que lo golpearon con una mochila en %s; no requirió atención médica."};

    private static final String[] OBS_NOTA_INICIAL = {
            "Se revisó el caso con la coordinación de tutoría y se programó una entrevista.",
            "Se comunicó al tutor del aula para observar la situación esta semana.",
            "Se solicitó información adicional a los docentes del grado."};
    private static final String[] OBS_ATENCION = {
            "Entrevista con el estudiante afectado; se brindó acompañamiento emocional.",
            "Reunión con los padres de familia para informar las acciones del colegio.",
            "Conversación con los estudiantes involucrados y firma de compromisos de convivencia.",
            "El tutor realizó seguimiento en aula y reportó avances parciales.",
            "Sesión con la psicóloga del colegio; se acordó una segunda cita."};
    private static final String[] OBS_DERIVADA = {
            "Se derivó al servicio de psicología externa con autorización de los padres.",
            "Se coordinó con la DEMUNA para orientación a la familia."};
    private static final String[] OBS_CIERRE = {
            "Caso resuelto: los estudiantes mantienen una convivencia adecuada y no hubo nuevos reportes.",
            "Se cierra por cumplimiento de los compromisos y conformidad de las familias.",
            "Se cierra tras dos semanas sin incidencias; se mantiene la observación del tutor.",
            "Cierre con acta firmada por las partes y verificación de mejora en el clima del aula."};

    @Override
    public void run(String... args) {
        tx.executeWithoutResult(status -> sembrar());
    }

    private void sembrar() {
        if (denunciaRepository.count() > 0) {
            System.out.println("[seed] Ya existen denuncias: no se siembra nada.");
            return;
        }
        String passAdmin = leerPassword("SEED_ADMIN_PASSWORD");
        String passOperador = leerPassword("SEED_OPERADOR_PASSWORD");

        Usuario admin = crearUsuario("admin", passAdmin, "ADMIN");
        List<Usuario> operadores = List.of(
                crearUsuario("op.psicologia", passOperador, "OPERADOR"),
                crearUsuario("op.convivencia", passOperador, "OPERADOR"),
                crearUsuario("op.tutoria", passOperador, "OPERADOR"),
                crearUsuario("op.secretaria", passOperador, "OPERADOR"));
        List<Usuario> todos = new ArrayList<>();
        todos.add(admin);
        todos.addAll(operadores);

        Random rnd = new Random(2026);
        LocalDateTime ahora = LocalDateTime.now();
        int totalSeguimientos = 0;

        for (int i = 0; i < TOTAL_DENUNCIAS; i++) {
            String tipo = elegirTipo(rnd);
            Denuncia d = new Denuncia();
            d.setTipoDenuncia(tipo);
            d.setDescripcion(descripcion(tipo, rnd));
            d.setNombreDenunciante(rnd.nextInt(100) < 25 ? null : nombreCompleto(rnd));
            d.setNombreAfectado(nombreCompleto(rnd));
            d = denunciaRepository.save(d); // @PrePersist fija protocolo, estado y fecha = ahora

            LocalDateTime registro = ahora.minusDays(rnd.nextInt(DIAS_ATRAS)).minusHours(rnd.nextInt(8))
                    .withMinute(rnd.nextInt(60)).withSecond(0).withNano(0);
            String estadoInicial = d.getEstado();
            // Un tercio queda en el estado inicial, un tercio en atención y un tercio resuelto/cerrado
            int grupo = i % 3;
            if (grupo == 2 && ahora.minusDays(10).isBefore(registro)) {
                registro = ahora.minusDays(10 + rnd.nextInt(20)); // lo resuelto necesita tiempo para transcurrir
            }
            d.setFechaRegistro(registro);

            LocalDateTime cursor = registro;
            Usuario quienRegistro = operadores.get(rnd.nextInt(operadores.size()));
            guardarSeguimiento(d, null, estadoInicial, "Denuncia registrada", quienRegistro, cursor);
            totalSeguimientos++;

            String actual = estadoInicial;
            if (grupo == 0) {
                // Se queda en el estado inicial; una nota de revisión sin cambio de estado
                cursor = siguiente(cursor, ahora, rnd);
                guardarSeguimiento(d, actual, actual, pick(OBS_NOTA_INICIAL, rnd), todos.get(rnd.nextInt(todos.size())), cursor);
                totalSeguimientos++;
            } else {
                // EN_ATENCION (dos o tres observaciones)
                int atenciones = 2 + rnd.nextInt(2);
                for (int a = 0; a < atenciones; a++) {
                    cursor = siguiente(cursor, ahora, rnd);
                    guardarSeguimiento(d, actual, "EN_ATENCION", pick(OBS_ATENCION, rnd), admin, cursor);
                    actual = "EN_ATENCION";
                    totalSeguimientos++;
                }
                if (grupo == 2) {
                    if (atenciones == 2 && rnd.nextInt(100) < 40) {
                        cursor = siguiente(cursor, ahora, rnd);
                        guardarSeguimiento(d, actual, "DERIVADA", pick(OBS_DERIVADA, rnd), admin, cursor);
                        actual = "DERIVADA";
                        totalSeguimientos++;
                    }
                    cursor = siguiente(cursor, ahora, rnd);
                    guardarSeguimiento(d, actual, "CERRADA", pick(OBS_CIERRE, rnd), admin, cursor);
                    actual = "CERRADA";
                    totalSeguimientos++;
                }
            }
            d.setEstado(actual);
            denunciaRepository.save(d);
        }
        System.out.println("[seed] Datos de demostración creados: " + TOTAL_DENUNCIAS + " denuncias y "
                + totalSeguimientos + " seguimientos.");
    }

    // ---------- Auxiliares ----------

    private String leerPassword(String variable) {
        String v = System.getenv(variable);
        if (v == null || v.length() < 8) {
            throw new IllegalStateException("Define la variable de entorno " + variable
                    + " (mínimo 8 caracteres) para sembrar los usuarios de demostración.");
        }
        return v;
    }

    private Usuario crearUsuario(String username, String passwordPlano, String rol) {
        return usuarioRepository.findByUsername(username).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(passwordPlano));
            u.setRol(rol);
            return usuarioRepository.save(u);
        });
    }

    /** Proporciones: más acoso escolar y convivencia, menos maltrato físico. */
    private String elegirTipo(Random rnd) {
        int n = rnd.nextInt(100);
        if (n < 35) return "ACOSO_ESCOLAR";
        if (n < 70) return "CONVIVENCIA";
        if (n < 90) return "MALTRATO_PSICOLOGICO";
        return "MALTRATO_FISICO";
    }

    private String descripcion(String tipo, Random rnd) {
        String[] pool = switch (tipo) {
            case "ACOSO_ESCOLAR" -> DESC_ACOSO;
            case "MALTRATO_PSICOLOGICO" -> DESC_PSICOLOGICO;
            case "MALTRATO_FISICO" -> DESC_FISICO;
            default -> DESC_CONVIVENCIA;
        };
        String texto = String.format(pick(pool, rnd), pick(LUGARES, rnd));
        return texto + " Grado del afectado: " + pick(GRADOS, rnd) + ".";
    }

    private String nombreCompleto(Random rnd) {
        return pick(NOMBRES, rnd) + " " + pick(APELLIDOS, rnd) + " " + pick(APELLIDOS, rnd);
    }

    private static String pick(String[] arr, Random rnd) {
        return arr[rnd.nextInt(arr.length)];
    }

    /** Avanza entre 1 y 6 días desde el cursor, sin pasar de "ahora". */
    private LocalDateTime siguiente(LocalDateTime cursor, LocalDateTime ahora, Random rnd) {
        LocalDateTime n = cursor.plusDays(1 + rnd.nextInt(6)).plusHours(rnd.nextInt(5));
        return n.isAfter(ahora) ? ahora.minusMinutes(5 + rnd.nextInt(50)) : n;
    }

    private void guardarSeguimiento(Denuncia d, String anterior, String nuevo, String obs, Usuario usuario,
                                    LocalDateTime fecha) {
        Seguimiento s = new Seguimiento();
        s.setDenuncia(d);
        s.setUsuario(usuario);
        s.setEstadoAnterior(anterior);
        s.setEstadoNuevo(nuevo);
        s.setObservacion(obs);
        s = seguimientoRepository.save(s); // @PrePersist pone fecha = ahora
        s.setFecha(fecha);                 // entidad gestionada: la fecha histórica se escribe al confirmar la transacción
    }
}
