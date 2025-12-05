package back.ecommerce.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public TiendaResponse create(TiendaRequest request, MultipartFile logoFile, List<MultipartFile> bannerFiles) {
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
        if (logoFile != null && !logoFile.isEmpty()) {
            urlLogo = cloudinaryService.uploadFile(logoFile);
        }
        
        List<String> banners = new ArrayList<>();
        if (bannerFiles != null && !bannerFiles.isEmpty()) {
            for (MultipartFile file : bannerFiles) {
                if (!file.isEmpty()) {
                    banners.add(cloudinaryService.uploadFile(file));
                }
            }
        }
        
        if (request.getBanners() != null) {
            banners.addAll(request.getBanners());
        }

        var entity = new TiendaEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setLogo(urlLogo);
        entity.setBanners(banners);
        entity.setVendedor(vendedor);
        entity.setCostoEnvio(request.getCostoEnvio() != null ? request.getCostoEnvio() : 0.0);

        var tiendaGuardada = tiendaRepository.save(entity);

        if (vendedor.getRol() != Rol.VENDEDOR) {
            vendedor.setRol(Rol.VENDEDOR);
            usuariosRepository.save(vendedor);
        }

        String linkTienda = frontendUrl + "/tienda/" + tiendaGuardada.getNombreUrl();
        String html = """
            <html>
            <body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>
                <div style='max-width: 600px; margin: 0 auto; background: white; padding: 0; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>
                    <div style='background-color: #4F46E5; padding: 20px; text-align: center;'>
                        <h1 style='color: white; margin: 0;'>¡Tu Tienda está Lista! 🚀</h1>
                    </div>
                    <div style='padding: 30px;'>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Felicitaciones, ya creamos tu espacio <strong>%s</strong> en nuestra plataforma.</p>
                        <p>Ahora podés empezar a cargar productos y vender.</p>
                        <div style='text-align: center; margin: 30px 0;'>
                            <a href='%s' style='background-color: #4F46E5; color: white; padding: 15px 30px; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 16px;'>Ver mi Tienda</a>
                        </div>
                        <p style='text-align: center; color: #666;'>Tu URL pública es: <br> <a href='%s'>%s</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(vendedor.getNombre(), tiendaGuardada.getNombreFantasia(), linkTienda, linkTienda, linkTienda);

        emailService.enviarCorreo(vendedor.getEmail(), "Tu tienda está lista!", html);
         
        return convertirEntidadAResponse(tiendaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public TiendaResponse readByNombreUrl(String nombreUrl) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        
        entity.getBanners().size(); 
        
        return convertirEntidadAResponse(entity);
    }

    @Override
    @Transactional(readOnly = true) 
    public TiendaResponse readByVendedorDni(Long dni) {
        var entity = tiendaRepository.findByVendedorDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("El vendedor no tiene una tienda asociada"));

        entity.getBanners().size(); 

        return convertirEntidadAResponse(entity);
    }

    @Override
    public TiendaResponse update(String nombreUrl, TiendaRequest request, MultipartFile logoFile, List<MultipartFile> bannerFiles) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        validarDueño(entity);

        if (request.getNombreFantasia() != null && !request.getNombreFantasia().isBlank()) {
            entity.setNombreFantasia(request.getNombreFantasia());
        }
        if (request.getDescripcion() != null) {
            entity.setDescripcion(request.getDescripcion());
        }
        
        if (request.getNombreUrl() != null && !request.getNombreUrl().isBlank() 
                && !request.getNombreUrl().equals(entity.getNombreUrl())) {
            
            if (tiendaRepository.findByNombreUrl(request.getNombreUrl()).isPresent()) {
                throw new IllegalArgumentException("La URL '" + request.getNombreUrl() + "' ya está en uso por otra tienda.");
            }
            entity.setNombreUrl(request.getNombreUrl());
        }
        
        if (logoFile != null && !logoFile.isEmpty()) {
            String nuevaUrl = cloudinaryService.uploadFile(logoFile);
            entity.setLogo(nuevaUrl);
        }

        List<String> bannersFinales = (request.getBanners() != null) 
                                      ? new ArrayList<>(request.getBanners()) 
                                      : new ArrayList<>(entity.getBanners());

        if (bannerFiles != null && !bannerFiles.isEmpty()) {
            for (MultipartFile file : bannerFiles) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file);
                    bannersFinales.add(url);
                }
            }
        }

        if (request.getCostoEnvio() != null) {
            entity.setCostoEnvio(request.getCostoEnvio());
        }
        
        entity.setBanners(bannersFinales);

        return convertirEntidadAResponse(tiendaRepository.save(entity));
    }

    @Override
    public void delete(String nombreUrl) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        validarDueño(entity);
        tiendaRepository.delete(entity);
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

    @Override
    @Transactional(readOnly = true)
    public List<TiendaResponse> readAll() {
        List<TiendaResponse> tiendas = tiendaRepository.findAll().stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());

        tiendas.sort((t1, t2) -> t2.getCantidadProductos().compareTo(t1.getCantidadProductos()));

        return tiendas;
    }

    private TiendaResponse convertirEntidadAResponse(TiendaEntity entity) {
        int cantidad = (entity.getProductos() != null) ? entity.getProductos().size() : 0;

        return TiendaResponse.builder()
                .id(entity.getId())
                .nombreUrl(entity.getNombreUrl())
                .nombreFantasia(entity.getNombreFantasia())
                .logo(entity.getLogo())
                .descripcion(entity.getDescripcion())
                .vendedorDni(entity.getVendedor() != null ? entity.getVendedor().getDni() : null)
                .vendedorNombre(entity.getVendedor() != null ? entity.getVendedor().getNombre() : null)
                .banners(entity.getBanners())
                .costoEnvio(entity.getCostoEnvio())
                .cantidadProductos(cantidad)
                .build();
    }

}