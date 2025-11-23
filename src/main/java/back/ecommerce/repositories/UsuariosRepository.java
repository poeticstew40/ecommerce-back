package back.ecommerce.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.UsuariosEntity;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Long>{

    Optional<UsuariosEntity> findByEmail(String email);

    Optional<UsuariosEntity> findByVerificationCode(String verificationCode);
    
}
