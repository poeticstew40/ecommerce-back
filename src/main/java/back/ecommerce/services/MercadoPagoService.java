package back.ecommerce.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

import back.ecommerce.entities.PedidosEntity;

@Service
public class MercadoPagoService {

    // ⚠️ IMPORTANTE: Poné tu Access Token de prueba aquí (o en application.properties)
    // Lo conseguís en: https://www.mercadopago.com.ar/developers/panel
    private final String ACCESS_TOKEN = "APP_USR-7983789130208261-112018-62869b26284c43ecd8786f7518853570-3005390485"; 

    public String crearPreferencia(PedidosEntity pedido) {
        // 1. Inicializar SDK
        MercadoPagoConfig.setAccessToken(ACCESS_TOKEN);

        // 2. Crear lista de items para MP
        List<PreferenceItemRequest> items = new ArrayList<>();

        // Recorremos los items de tu pedido y los convertimos a items de MP
        pedido.getItemsPedido().forEach(item -> {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(item.getProducto().getNombre())
                    .quantity(item.getCantidad())
                    .unitPrice(BigDecimal.valueOf(item.getPrecioUnitario()))
                    .currencyId("ARS") // O la moneda que uses
                    .build();
            items.add(itemRequest);
        });

        // 3. Configurar URLs de retorno (a dónde vuelve el usuario después de pagar)
        // Podés poner la URL de tu frontend local o producción
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:5173/compra-exitosa") // Cambiar por URL real del front
                .failure("http://localhost:5173/compra-fallida")
                .pending("http://localhost:5173/compra-pendiente")
                .build();

        // 4. Armar la solicitud completa
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                //.autoReturn("approved") // Volver automático si se aprueba
                .externalReference(String.valueOf(pedido.getId())) // Guardamos el ID de tu pedido para identificarlo después
                .build();

        // 5. Crear la preferencia en MP y obtener el Link
        // En tu método crearPreferencia...
        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();

        } catch (com.mercadopago.exceptions.MPApiException e) {
            // 👇 ESTO ES LO QUE NECESITAMOS VER
            System.err.println("❌ ERROR MP: " + e.getApiResponse().getContent());
            throw new RuntimeException("Error de MP", e);
        } catch (Exception e) {
            throw new RuntimeException("Error general", e);
        }
    }
}