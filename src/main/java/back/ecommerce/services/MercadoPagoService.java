package back.ecommerce.services;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException; // Importar Excepción MP
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.PedidosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    @Value("${mp.access.token}")
    private String accessToken;

    @Value("${app.backend.url}")
    private String backendUrl;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final PedidosRepository pedidosRepository;
    private final EmailService emailService;

    public String crearPreferencia(PedidosEntity pedido) {

        String urlFrontLimpia = this.frontendUrl.trim(); 
        String urlBackLimpia = this.backendUrl.trim();

        log.info("DEBUG URL FRONTEND: '{}'", this.frontendUrl);
        log.info("DEBUG URL BACKEND: '{}'", this.backendUrl);

        MercadoPagoConfig.setAccessToken(accessToken);

        List<PreferenceItemRequest> items = new ArrayList<>();
        
        pedido.getItemsPedido().forEach(item -> {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(item.getProducto().getNombre())
                    .quantity(item.getCantidad())
                    .unitPrice(BigDecimal.valueOf(item.getPrecioUnitario()))
                    .currencyId("ARS")
                    .build();
            items.add(itemRequest);
        });

        // Agregar costo de envío como ítem si existe
        if (pedido.getCostoEnvio() != null && pedido.getCostoEnvio() > 0) {
            PreferenceItemRequest itemEnvio = PreferenceItemRequest.builder()
                    .title("Costo de Envío")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(pedido.getCostoEnvio()))
                    .currencyId("ARS")
                    .build();
            items.add(itemEnvio);
        }

        String urlRetorno = this.frontendUrl;

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(urlRetorno + "/compra-exitosa")
                .failure(urlRetorno + "/compra-fallida")
                .pending(urlRetorno + "/compra-pendiente")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                //.autoReturn("approved") 
                .externalReference(String.valueOf(pedido.getId()))
                .notificationUrl(backendUrl + "/api/pagos/webhook")
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();
        } catch (MPApiException e) {
            // Captura el error específico de la API de Mercado Pago con el detalle
            log.error("❌ ERROR API MP: Status: {} | Body: {}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Mercado Pago rechazó los datos: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            log.error("Error creando preferencia MP: {}", e.getMessage());
            throw new RuntimeException("Error creando preferencia MP", e);
        }
    }
    
    public void procesarNotificacion(Long paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);

            if ("approved".equals(payment.getStatus())) {
                String externalReference = payment.getExternalReference();
                Long pedidoId = Long.parseLong(externalReference);

                PedidosEntity pedido = pedidosRepository.findById(pedidoId)
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

                if (!"PAGADO".equals(pedido.getEstado())) {
                    pedido.setEstado("PAGADO");
                    pedidosRepository.save(pedido);
                    
                    // Enviar correos
                    if (pedido.getUsuario() != null) {
                        enviarEmailComprador(pedido, payment.getId());
                    }

                    if (pedido.getTienda() != null && pedido.getTienda().getVendedor() != null) {
                        enviarEmailVendedor(pedido);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error procesando notificación de pago: {}", e.getMessage());
        }
    } 

    // ========================================================================
    // CORREO PARA EL COMPRADOR (TICKET DE COMPRA)
    // ========================================================================
    private void enviarEmailComprador(PedidosEntity pedido, Long mpPaymentId) {
        String email = pedido.getUsuario().getEmail();
        String asunto = "Comprobante de Compra - Pedido #" + pedido.getId();
        
        String htmlContent = generarTicketHtml(pedido, mpPaymentId);
        emailService.enviarCorreo(email, asunto, htmlContent);
    }

    private String generarTicketHtml(PedidosEntity pedido, Long mpPaymentId) {
        StringBuilder sb = new StringBuilder();
        
        String colorPrimario = "#4F46E5";
        String estiloTabla = "width: 100%; border-collapse: collapse; margin-top: 20px;";
        String estiloCelda = "padding: 12px; border-bottom: 1px solid #ddd; text-align: left;";
        String estiloHeader = "background-color: #f8f9fa; font-weight: bold;";

        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
        
        // Encabezado
        sb.append("<div style='text-align: center; padding: 20px; background-color: ").append(colorPrimario).append("; color: white;'>");
        sb.append("<h1>¡Gracias por tu compra!</h1>");
        sb.append("<p style='font-size: 18px;'>").append(pedido.getTienda().getNombreFantasia()).append("</p>");
        sb.append("</div>");

        // Info General
        sb.append("<div style='padding: 20px;'>");
        sb.append("<p>Hola <strong>").append(pedido.getUsuario().getNombre()).append("</strong>,</p>");
        sb.append("<p>Tu pedido ha sido confirmado y el pago acreditado.</p>");
        
        sb.append("<table style='width: 100%; margin-bottom: 20px;'>");
        sb.append("<tr><td><strong>N° Pedido:</strong> #").append(pedido.getId()).append("</td>");
        sb.append("<td><strong>Pago MP:</strong> ").append(mpPaymentId).append("</td></tr>");
        
        String fecha = pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DateTimeFormatter.ISO_LOCAL_DATE) : "Hoy";
        sb.append("<tr><td><strong>Fecha:</strong> ").append(fecha).append("</td>");
        sb.append("<td><strong>Estado:</strong> <span style='color:green; font-weight:bold;'>PAGADO</span></td></tr>");
        sb.append("</table>");

        // Tabla de Productos
        sb.append("<h3>Detalle de productos</h3>");
        sb.append("<table style='").append(estiloTabla).append("'>");
        sb.append("<tr style='").append(estiloHeader).append("'>");
        sb.append("<th style='").append(estiloCelda).append("'>Producto</th>");
        sb.append("<th style='").append(estiloCelda).append("'>Cant.</th>");
        sb.append("<th style='").append(estiloCelda).append("'>Subtotal</th>");
        sb.append("</tr>");

        pedido.getItemsPedido().forEach(item -> {
            sb.append("<tr>");
            sb.append("<td style='").append(estiloCelda).append("'>").append(item.getProducto().getNombre()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>").append(item.getCantidad()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>$").append(item.getPrecioUnitario() * item.getCantidad()).append("</td>");
            sb.append("</tr>");
        });

        // Sección de Totales y Envío
        sb.append("</table>");
        
        sb.append("<div style='text-align: right; margin-top: 15px;'>");
        if (pedido.getCostoEnvio() != null && pedido.getCostoEnvio() > 0) {
            sb.append("<p>Subtotal Productos: $").append(pedido.getTotal() - pedido.getCostoEnvio()).append("</p>");
            sb.append("<p>Envío: $").append(pedido.getCostoEnvio()).append("</p>");
        } else {
            sb.append("<p>Envío: Gratis / Retiro</p>");
        }
        sb.append("<h2 style='color: ").append(colorPrimario).append(";'>TOTAL: $").append(pedido.getTotal()).append("</h2>");
        sb.append("</div>");

        // Información de Entrega
        sb.append("<div style='margin-top: 30px; background-color: #f9fafb; padding: 15px; border-radius: 8px;'>");
        sb.append("<h3 style='margin-top: 0;'>Información de Entrega</h3>");
        sb.append("<p><strong>Método:</strong> ").append(pedido.getMetodoEnvio() != null ? pedido.getMetodoEnvio() : "A convenir").append("</p>");
        
        if ("Envío a Domicilio".equalsIgnoreCase(pedido.getMetodoEnvio())) {
            sb.append("<p><strong>Dirección de envío:</strong> ").append(pedido.getDireccionEnvio()).append("</p>");
            sb.append("<p><em>El vendedor preparará tu paquete y lo enviará a esta dirección.</em></p>");
        } else {
            sb.append("<p><strong>Retiro en Tienda:</strong> Debes pasar a buscar tu pedido por el local.</p>");
            sb.append("<p><em>Ponte en contacto con el vendedor para coordinar el horario.</em></p>");
        }
        sb.append("</div>");
        
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // ========================================================================
    // CORREO PARA EL VENDEDOR (NUEVA VENTA) - ¡MEJORADO!
    // ========================================================================
    private void enviarEmailVendedor(PedidosEntity pedido) {
        String emailVendedor = pedido.getTienda().getVendedor().getEmail();
        String asunto = "¡Nueva Venta! Pedido #" + pedido.getId();
        UsuariosEntity comprador = pedido.getUsuario();

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
        
        // Header Verde
        mensaje.append("<div style='padding: 20px; background-color: #27ae60; color: white; text-align: center; border-radius: 8px 8px 0 0;'>");
        mensaje.append("<h1 style='margin:0;'>¡Felicitaciones! Nueva Venta 🎉</h1>");
        mensaje.append("<p style='margin:5px 0;'>Pedido #").append(pedido.getId()).append("</p>");
        mensaje.append("</div>");

        mensaje.append("<div style='padding: 20px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 8px 8px;'>");

        // 1. Datos del Comprador
        mensaje.append("<h3 style='border-bottom: 2px solid #eee; padding-bottom: 10px; color: #2c3e50;'>👤 Datos del Cliente</h3>");
        mensaje.append("<ul style='list-style: none; padding: 0;'>");
        mensaje.append("<li style='margin-bottom: 8px;'><strong>Nombre:</strong> ").append(comprador.getNombre()).append(" ").append(comprador.getApellido()).append("</li>");
        mensaje.append("<li style='margin-bottom: 8px;'><strong>DNI:</strong> ").append(comprador.getDni()).append("</li>");
        mensaje.append("<li style='margin-bottom: 8px;'><strong>Email:</strong> <a href='mailto:").append(comprador.getEmail()).append("'>").append(comprador.getEmail()).append("</a></li>");
        mensaje.append("</ul>");

        // 2. Detalle de Entrega (Lógica condicional)
        mensaje.append("<h3 style='border-bottom: 2px solid #eee; padding-bottom: 10px; margin-top: 25px; color: #2c3e50;'>🚚 Método de Entrega</h3>");
        
        String metodo = pedido.getMetodoEnvio() != null ? pedido.getMetodoEnvio() : "A convenir";
        boolean esEnvio = "Envío a Domicilio".equalsIgnoreCase(metodo);

        mensaje.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px;'>");
        mensaje.append("<p style='font-size: 1.1em; font-weight: bold; margin-top: 0;'>").append(metodo).append("</p>");

        if (esEnvio) {
            mensaje.append("<p><strong>Dirección de Entrega:</strong><br>").append(pedido.getDireccionEnvio()).append("</p>");
            if (pedido.getCostoEnvio() != null && pedido.getCostoEnvio() > 0) {
                mensaje.append("<p><strong>Costo de envío cobrado:</strong> $").append(pedido.getCostoEnvio()).append("</p>");
            } else {
                mensaje.append("<p><strong>Costo de envío:</strong> Gratis / Incluido</p>");
            }
            mensaje.append("<p style='color: #d35400;'>⚠ Debes preparar el paquete para envío.</p>");
        } else {
            mensaje.append("<p>El cliente pasará a retirar el pedido por tu tienda.</p>");
            mensaje.append("<p><strong>Costo de envío:</strong> $0.00</p>");
            mensaje.append("<p style='color: #27ae60;'>✓ Ten el pedido listo para entregar.</p>");
        }
        mensaje.append("</div>");

        // 3. Resumen de Productos
        mensaje.append("<h3 style='border-bottom: 2px solid #eee; padding-bottom: 10px; margin-top: 25px; color: #2c3e50;'>🛒 Productos Vendidos</h3>");
        mensaje.append("<table style='width: 100%; border-collapse: collapse;'>");
        mensaje.append("<tr style='background-color: #f1f1f1;'><th style='padding: 8px; text-align: left;'>Cant.</th><th style='padding: 8px; text-align: left;'>Producto</th><th style='padding: 8px; text-align: right;'>Precio</th></tr>");
        
        pedido.getItemsPedido().forEach(item -> {
            mensaje.append("<tr>");
            mensaje.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(item.getCantidad()).append("</td>");
            mensaje.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(item.getProducto().getNombre()).append("</td>");
            mensaje.append("<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: right;'>$").append(item.getPrecioUnitario() * item.getCantidad()).append("</td>");
            mensaje.append("</tr>");
        });
        mensaje.append("</table>");

        mensaje.append("<h2 style='text-align: right; color: #27ae60; margin-top: 20px;'>Total Pagado: $").append(pedido.getTotal()).append("</h2>");
        
        mensaje.append("</div>");
        mensaje.append("</body></html>");

        emailService.enviarCorreo(emailVendedor, asunto, mensaje.toString());
    }
}