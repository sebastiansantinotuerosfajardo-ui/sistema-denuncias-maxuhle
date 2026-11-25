package com.maxuhle.denuncias.config;

import com.maxuhle.denuncias.model.Usuario;
import com.maxuhle.denuncias.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios de prueba locales (admin/operador) la primera vez que arranca la aplicación.
 * No corre en producción (perfil prod) ni al sembrar datos de demostración (perfil seed): en la nube
 * los usuarios los crea DemoDataSeeder con contraseñas tomadas de variables de entorno.
 */
@Component
@Profile("!prod & !seed")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "admin123", "ADMIN");
        crearSiNoExiste("operador", "operador123", "OPERADOR");
    }

    private void crearSiNoExiste(String username, String passwordPlano, String rol) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(passwordPlano));
            u.setRol(rol);
            usuarioRepository.save(u);
        }
    }
}
