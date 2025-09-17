package back.ecommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.UsuariosEntity;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Long>{

}
