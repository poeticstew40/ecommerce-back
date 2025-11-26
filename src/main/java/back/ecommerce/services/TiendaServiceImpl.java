package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.entities.TiendaEntity;
import back.ecommerce.repositories.TiendaRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TiendaServiceImpl implements TiendaService {

    private final TiendaRepository tiendaRepository;
    private final UsuariosRepository usuariosRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public TiendaResponse create(TiendaRequest request) {
        var vendedor = usuariosRepository.findById(request.getVendedorDni())
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con DNI: " + request.getVendedorDni()));

        if (!vendedor.isEmailVerificado()) {
             throw new IllegalStateException("Debes verificar tu email antes de crear una tienda. Revisá tu correo para activarla.");
        }

        if (tiendaRepository.findByNombreUrl(request.getNombreUrl()).isPresent()) {
            throw new IllegalArgumentException("La URL de tienda '" + request.getNombreUrl() + "' ya está en uso.");
        }

        var entity = new TiendaEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setVendedor(vendedor);

        var tiendaGuardada = tiendaRepository.save(entity);

        String asunto = "Tu tienda '" + tiendaGuardada.getNombreFantasia() + "' está lista!";
        String mensaje = "Hola " + vendedor.getNombre() + ",\n\n" +
                         "Felicitaciones, ya creamos tu espacio en nuestra plataforma.\n" +
                         "Podés acceder a tu panel de administración y empezar a cargar productos.\n\n" +
                         "Tu URL pública es: " + frontendUrl + "/tienda/" + tiendaGuardada.getNombreUrl() + "\n\n" +
                         "Éxitos,\nEl equipo de Ecommerce.";
        
        emailService.enviarCorreo(vendedor.getEmail(), asunto, mensaje);
        
        return convertirEntidadAResponse(tiendaGuardada);
    }

    @Override
    public TiendaResponse readByNombreUrl(String nombreUrl) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        return convertirEntidadAResponse(entity);
    }

    private TiendaResponse convertirEntidadAResponse(TiendaEntity entity) {
        return TiendaResponse.builder()
                .id(entity.getId())
                .nombreUrl(entity.getNombreUrl())
                .nombreFantasia(entity.getNombreFantasia())
                .logo(entity.getLogo())
                .descripcion(entity.getDescripcion())
                .vendedorDni(entity.getVendedor() != null ? entity.getVendedor().getDni() : null)
                .vendedorNombre(entity.getVendedor() != null ? entity.getVendedor().getNombre() : null)
                .build();
    }
}