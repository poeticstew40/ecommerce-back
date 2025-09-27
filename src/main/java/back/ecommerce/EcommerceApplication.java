package back.ecommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import back.ecommerce.services.PedidosService;

@SpringBootApplication
public class EcommerceApplication implements CommandLineRunner{

	@Autowired
	private PedidosService pedidosService;
	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
		System.out.println("E-commerce application started successfully.");
	}

    @Override
    public void run(String... args) throws Exception {

		this.pedidosService.delete(1L);
    }

}
