package back.ecommerce.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value; // Importar Value
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

    // ✅ CAMBIO: Inyección de propiedades
    @Value("${mp.access.token}")
    private String accessToken;

    @Value("${app.backend.url}")
    private String backendUrl;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final PedidosRepository pedidosRepository;

    public String crearPreferencia(PedidosEntity pedido) {
        // Usamos el token inyectado
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

        // Usamos la URL del frontend inyectada
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
                // Usamos la URL del backend inyectada
                .notificationUrl(backendUrl + "/api/pagos/webhook")
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();

        } catch (MPApiException e) {
            System.err.println("❌ ERROR MP: " + e.getApiResponse().getContent());
            throw new RuntimeException("Error de MP", e);
        } catch (Exception e) {
            throw new RuntimeException("Error general", e);
        }
    }
    
    public void procesarNotificacion(Long paymentId) {
        try {
            // Aseguramos el token también aquí
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
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando notificación: " + e.getMessage());
        }
    } 
}