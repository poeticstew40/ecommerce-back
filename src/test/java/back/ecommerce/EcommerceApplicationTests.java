package back.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=true",
    
    "jwt.secret.key=test-secret-key-123456",
    "cloudinary.cloud_name=test-cloud",
    "cloudinary.api_key=12345",
    "cloudinary.api_secret=test-secret",
    "mp.access.token=TEST-1234567890",
    "app.backend.url=http://localhost:8080",
    "app.frontend.url=http://localhost:5173",
    "resend.api.key=re_123456",
    "resend.email.from=test@test.com"
})
class EcommerceApplicationTests {

	@Test
	void contextLoads() {
	}

}
