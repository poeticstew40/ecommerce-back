package back.ecommerce.controllers;

import java.util.Map;

import org.springframework.http.MediaType; // 👈 Importante: Agrega este import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import back.ecommerce.services.CloudinaryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/storage")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StorageController {

    private final CloudinaryService cloudinaryService;

    // ✅ CAMBIO CLAVE: Agregar consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}