package com.example.WebSiteDatLich;

import com.example.WebSiteDatLich.service.DataTestImportService;
import groovy.lang.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@SpringBootApplication
public class WebSiteDatLichApplication {
	@Autowired

	public static void main(String[] args) {
		SpringApplication.run(WebSiteDatLichApplication.class, args);
	}

	@PostConstruct
	public void initFirebase() {
		try {
			// Đảm bảo rằng tệp JSON chứa khóa dịch vụ của Firebase nằm ở đúng vị trí
			FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://ungdungdatlichkham-default-rtdb.firebaseio.com/") // Thay thế bằng URL Firebase của bạn
					.setStorageBucket("ungdungdatlichkham.appspot.com") // Thay thế bằng tên bucket Firebase Storage của bạn
					.build();

			// Khởi tạo FirebaseApp
			if (FirebaseApp.getApps().isEmpty()) { // Chỉ khởi tạo nếu Firebase chưa được khởi tạo
				FirebaseApp.initializeApp(options);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
