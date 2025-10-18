package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;
import back.ecommerce.entities.CategoriasEntity;
import back.ecommerce.repositories.CategoriasRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class CategoriasServiceImpl implements CategoriasService{

    private final CategoriasRepository categoriasRepository;

    //create categoria
    @Override
    public CategoriasResponse create(CategoriasRequest categoria) {
        var entity = new CategoriasEntity();
        BeanUtils.copyProperties(categoria, entity);

        var categoriaCreated = categoriasRepository.save(entity);

        var response = new CategoriasResponse();
        BeanUtils.copyProperties(categoriaCreated, response);

        return response;
    }

    //get categoria by id
    @Override
    public CategoriasResponse readById(Long id) {
        final var entityResponse = this.categoriasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + id));

        var response = new CategoriasResponse();
        BeanUtils.copyProperties(entityResponse, response);

        return response;
    }

    @Override // Anotación que indica que estás implementando un método de la interfaz
    public List<CategoriasResponse> readAll() {
        // 1. Llama al repositorio para traer todas las entidades de la DB.
        List<CategoriasEntity> entityFromDB = this.categoriasRepository.findAll();

        // 2. Usa un Stream para convertir cada entidad a su DTO correspondiente.
        return entityFromDB.stream()
            .map(entidad -> {
                CategoriasResponse response = new CategoriasResponse();
                BeanUtils.copyProperties(entidad, response);
                return response;
            })
            .collect(Collectors.toList()); // 3. Junta todos los DTOs en una nueva lista.
    }

    @Override
    public CategoriasResponse update(Long id, CategoriasRequest categoria) {
        final var entityFromDB = this.categoriasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + id));

        if (categoria.getNombre() != null && 
            !categoria.getNombre().isBlank()) {
            entityFromDB.setNombre(categoria.getNombre());
        }
        var categoriaActualizada = this.categoriasRepository.save(entityFromDB);

        final var response = new CategoriasResponse();

        BeanUtils.copyProperties(categoriaActualizada, response);

        return response;
    }

    @Override
    public void delete(Long id) {
        var categoria = this.categoriasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + id));

        log.info("Eliminando categoria: {}", categoria.getNombre());

        this.categoriasRepository.delete(categoria);
    }

}
