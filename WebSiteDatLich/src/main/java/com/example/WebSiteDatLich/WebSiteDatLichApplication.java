package com.example.WebSiteDatLich;

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

	public static void main(String[] args) {
		SpringApplication.run(WebSiteDatLichApplication.class, args);
	}

	@PostConstruct
	public void initFirebase() {
		try {
			System.out.println("Initializing Firebase...");

			// Đảm bảo rằng tệp JSON chứa khóa dịch vụ của Firebase nằm ở đúng vị trí
			FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://ungdungdatlichkham-default-rtdb.firebaseio.com/")
					.setStorageBucket("ungdungdatlichkham.appspot.com")
					.build();

			// Khởi tạo FirebaseApp
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
				System.out.println("Firebase connected successfully!");
			} else {
				System.out.println("Firebase already initialized.");
			}
		} catch (IOException e) {
			System.err.println("Error initializing Firebase: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
