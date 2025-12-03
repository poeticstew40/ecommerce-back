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
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import back.ecommerce.entities.PedidosEntity;
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
                // Comentado para evitar error "back_url.success invalid" en localhost o URLs sin HTTPS
                // .autoReturn("approved") 
                .externalReference(String.valueOf(pedido.getId()))
                .notificationUrl(backendUrl + "/api/pagos/webhook")
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();
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

    private void enviarEmailComprador(PedidosEntity pedido, Long mpPaymentId) {
        String email = pedido.getUsuario().getEmail();
        String asunto = "Comprobante de Compra - Pedido #" + pedido.getId();
        
        String htmlContent = generarTicketHtml(pedido, mpPaymentId);
        emailService.enviarCorreo(email, asunto, htmlContent);
    }

    private String generarTicketHtml(PedidosEntity pedido, Long mpPaymentId) {
        StringBuilder sb = new StringBuilder();
        
        // Estilos CSS inline
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

        // Info del Pedido
        sb.append("<div style='padding: 20px;'>");
        sb.append("<p>Hola <strong>").append(pedido.getUsuario().getNombre()).append("</strong>,</p>");
        sb.append("<p>Tu pedido ha sido confirmado. Aquí tienes los detalles:</p>");
        
        sb.append("<table style='width: 100%; margin-bottom: 20px;'>");
        sb.append("<tr><td><strong>N° Pedido:</strong> #").append(pedido.getId()).append("</td>");
        sb.append("<td><strong>Ref. Pago MP:</strong> ").append(mpPaymentId).append("</td></tr>");
        
        // Formato de fecha seguro
        String fecha = pedido.getFechaPedido() != null ? 
                       pedido.getFechaPedido().format(DateTimeFormatter.ISO_LOCAL_DATE) : "Hoy";
                       
        sb.append("<tr><td><strong>Fecha:</strong> ").append(fecha).append("</td>");
        sb.append("<td><strong>Estado:</strong> <span style='color:green; font-weight:bold;'>PAGADO</span></td></tr>");
        sb.append("</table>");

        // Tabla de Productos
        sb.append("<table style='").append(estiloTabla).append("'>");
        sb.append("<tr style='").append(estiloHeader).append("'>");
        sb.append("<th style='").append(estiloCelda).append("'>Producto</th>");
        sb.append("<th style='").append(estiloCelda).append("'>Cant.</th>");
        sb.append("<th style='").append(estiloCelda).append("'>Precio Unit.</th>");
        sb.append("<th style='").append(estiloCelda).append("'>Subtotal</th>");
        sb.append("</tr>");

        pedido.getItemsPedido().forEach(item -> {
            sb.append("<tr>");
            sb.append("<td style='").append(estiloCelda).append("'>").append(item.getProducto().getNombre()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>").append(item.getCantidad()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>$").append(item.getPrecioUnitario()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>$").append(item.getPrecioUnitario() * item.getCantidad()).append("</td>");
            sb.append("</tr>");
        });

        // Envío
        if (pedido.getCostoEnvio() != null && pedido.getCostoEnvio() > 0) {
            sb.append("<tr>");
            sb.append("<td style='").append(estiloCelda).append("'>Envío (").append(pedido.getMetodoEnvio()).append(")</td>");
            sb.append("<td style='").append(estiloCelda).append("'>1</td>");
            sb.append("<td style='").append(estiloCelda).append("'>$").append(pedido.getCostoEnvio()).append("</td>");
            sb.append("<td style='").append(estiloCelda).append("'>$").append(pedido.getCostoEnvio()).append("</td>");
            sb.append("</tr>");
        }

        // Total
        sb.append("</table>");
        sb.append("<h2 style='text-align: right; color: ").append(colorPrimario).append(";'>TOTAL: $").append(pedido.getTotal()).append("</h2>");

        // Footer
        sb.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 2px solid #eee; font-size: 12px; color: #777;'>");
        sb.append("<p>Dirección de entrega: ").append(pedido.getDireccionEnvio()).append("</p>");
        sb.append("<p>Si tienes dudas, contacta a la tienda.</p>");
        sb.append("</div>");
        
        sb.append("</div></body></html>");
        
        return sb.toString();
    }

    private void enviarEmailVendedor(PedidosEntity pedido) {
        String emailVendedor = pedido.getTienda().getVendedor().getEmail();
        String asunto = "¡Nueva Venta! Pedido #" + pedido.getId();

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("<html><body style='font-family: Arial, sans-serif;'>");
        mensaje.append("<div style='padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>");
        mensaje.append("<h2 style='color: #27ae60;'>¡Nueva Venta Confirmada! 🎉</h2>");
        mensaje.append("<p>El comprador <strong>").append(pedido.getUsuario().getNombre()).append("</strong> ha pagado el pedido #").append(pedido.getId()).append(".</p>");
        
        mensaje.append("<h3>Resumen:</h3>");
        mensaje.append("<ul>");
        pedido.getItemsPedido().forEach(item -> {
            mensaje.append("<li>").append(item.getCantidad()).append("x ").append(item.getProducto().getNombre()).append("</li>");
        });
        mensaje.append("</ul>");
        
        mensaje.append("<h3 style='color: #333;'>Total: $").append(pedido.getTotal()).append("</h3>");
        mensaje.append("<hr>");
        mensaje.append("<p><strong>Envío a:</strong> ").append(pedido.getDireccionEnvio()).append("</p>");
        mensaje.append("<p style='font-weight: bold;'>¡A preparar el paquete!</p>");
        mensaje.append("</div></body></html>");

        emailService.enviarCorreo(emailVendedor, asunto, mensaje.toString());
    }
}