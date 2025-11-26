package back.ecommerce.services;

import java.math.BigDecimal;
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

@Service
@RequiredArgsConstructor
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

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/compra-exitosa")
                .failure(frontendUrl + "/compra-fallida")
                .pending(frontendUrl + "/compra-pendiente")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(String.valueOf(pedido.getId()))
                .notificationUrl(backendUrl + "/api/pagos/webhook")
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();
        } catch (MPApiException e) {
            System.err.println("ERROR MP: " + e.getApiResponse().getContent());
            throw new RuntimeException("Error de MP", e);
        } catch (Exception e) {
            throw new RuntimeException("Error general", e);
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
                    
                    if (pedido.getUsuario() != null && pedido.getUsuario().getEmail() != null) {
                        String emailUsuario = pedido.getUsuario().getEmail();
                        String asunto = "Pago Confirmado! Pedido #" + pedido.getId();
                        
                        BigDecimal total = BigDecimal.valueOf(pedido.getTotal());
                        BigDecimal envio = BigDecimal.valueOf(pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : 0.0);
                        BigDecimal subtotal = total.subtract(envio);

                        String mensaje = "Hola " + pedido.getUsuario().getNombre() + ",\n\n" +
                                "Tu pago ha sido procesado exitosamente.\n" +
                                "--------------------------------------\n" +
                                "Subtotal Productos: $" + subtotal + "\n" +
                                "Costo de Envío:     $" + envio + "\n" +
                                "--------------------------------------\n" +
                                "TOTAL ABONADO:      $" + total + "\n" +
                                "--------------------------------------\n\n" +
                                "Tienda: " + (pedido.getTienda() != null ? pedido.getTienda().getNombreFantasia() : "E-commerce") + "\n" +
                                "Gracias por tu compra!";

                        emailService.enviarCorreo(emailUsuario, asunto, mensaje);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando notificación: " + e.getMessage());
        }
    } 
}