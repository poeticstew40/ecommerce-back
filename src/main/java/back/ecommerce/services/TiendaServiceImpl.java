package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.entities.Rol;
import back.ecommerce.entities.TiendaEntity;
import back.ecommerce.entities.UsuariosEntity;
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
    private final CloudinaryService cloudinaryService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public TiendaResponse create(TiendaRequest request, MultipartFile file) {
        var vendedor = usuariosRepository.findById(request.getVendedorDni())
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con DNI: " + request.getVendedorDni()));

        UsuariosEntity usuarioLogueado = (UsuariosEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!vendedor.getEmail().equals(usuarioLogueado.getEmail())) {
             throw new IllegalArgumentException("ACCESO DENEGADO: El email del vendedor no coincide con el usuario logueado.");
        }

        if (!vendedor.getDni().equals(usuarioLogueado.getDni())) {
             throw new IllegalArgumentException("ACCESO DENEGADO: El DNI del vendedor no coincide con el usuario logueado.");
        }

        if (!vendedor.isEmailVerificado()) {
             throw new IllegalStateException("Debes verificar tu email antes de crear una tienda.");
        }

        if (tiendaRepository.findByNombreUrl(request.getNombreUrl()).isPresent()) {
            throw new IllegalArgumentException("La URL de tienda '" + request.getNombreUrl() + "' ya está en uso.");
        }

        String urlLogo = null;
        if (file != null && !file.isEmpty()) {
            urlLogo = cloudinaryService.uploadFile(file);
        }

        var entity = new TiendaEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setLogo(urlLogo);
        entity.setVendedor(vendedor);

        var tiendaGuardada = tiendaRepository.save(entity);

        if (vendedor.getRol() != Rol.VENDEDOR) {
            vendedor.setRol(Rol.VENDEDOR);
            usuariosRepository.save(vendedor);
        }

        String asunto = "Tu tienda '" + tiendaGuardada.getNombreFantasia() + "' está lista!";
        String mensaje = "Hola " + vendedor.getNombre() + ",\n\n" +
                         "Felicitaciones, ya creamos tu espacio en nuestra plataforma.\n" +
                         "Tu URL pública es: " + frontendUrl + "/tienda/" + tiendaGuardada.getNombreUrl();
        
        emailService.enviarCorreo(vendedor.getEmail(), asunto, mensaje);
        
        return convertirEntidadAResponse(tiendaGuardada);
    }

    @Override
    public TiendaResponse readByNombreUrl(String nombreUrl) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        return convertirEntidadAResponse(entity);
    }

    @Override
    public TiendaResponse update(String nombreUrl, TiendaRequest request, MultipartFile file) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));

        validarDueño(entity);

        if (request.getNombreFantasia() != null && !request.getNombreFantasia().isBlank()) {
            entity.setNombreFantasia(request.getNombreFantasia());
        }
        if (request.getDescripcion() != null) {
            entity.setDescripcion(request.getDescripcion());
        }
        
        if (file != null && !file.isEmpty()) {
            String nuevaUrl = cloudinaryService.uploadFile(file);
            entity.setLogo(nuevaUrl);
        }

        return convertirEntidadAResponse(tiendaRepository.save(entity));
    }

    private void validarDueño(TiendaEntity tienda) {
        UsuariosEntity usuarioLogueado = (UsuariosEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (!tienda.getVendedor().getEmail().equals(usuarioLogueado.getEmail())) {
            throw new IllegalArgumentException("ACCESO DENEGADO: No eres el dueño de esta tienda (Email incorrecto).");
        }
        if (!tienda.getVendedor().getDni().equals(usuarioLogueado.getDni())) {
            throw new IllegalArgumentException("ACCESO DENEGADO: No eres el dueño de esta tienda (DNI incorrecto).");
        }
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