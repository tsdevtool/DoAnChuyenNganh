package com.example.WebSiteDatLich.service;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.model.Doctor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataImportService {
    private final FirebaseDatabase firebaseDatabase;

    public DataImportService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public void importTestData() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors");
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");
        DatabaseReference positionsRef = firebaseDatabase.getReference("positions");
        // Tạo dữ liệu mẫu cho bảng User
        User user1 = new User();
        user1.setUser_id("-O941D_Req5OyTUY5uIn");
        user1.setName("Dr. John Doe");
        user1.setEmail("johndoe@example.com");
        user1.setPhone("0123456789");
        user1.setAvatar("https://storage.googleapis.com/download/storage/v1/b/ungdungdatlichkham.appspot.com/o/efb3a7c9-3dac-47c1-8480-a47ede602287_bai3.jpg?generation=1728627971762516&alt=media");
        user1.setAddress("123 Main Street, City");
        user1.setDate_of_birth("1980-01-01");

        User user2 = new User();
        user2.setUser_id("-O8uFNsL0nSOhsXB1c_X");
        user2.setName("Dr. Jane Smith");
        user2.setEmail("janesmith@example.com");
        user2.setPhone("0987654321");
        user2.setAvatar("https://storage.googleapis.com/download/storage/v1/b/ungdungdatlichkham.appspot.com/o/e39735c1-9fb9-43dc-8021-a548bbb8409b_bacsi1.jpg?generation=1728808807873997&alt=media");
        user2.setAddress("456 Another St, City");
        user2.setDate_of_birth("1975-05-10");
        // Thêm dữ liệu User vào Firebase
        usersRef.child(user1.getUser_id()).setValueAsync(user1);
        usersRef.child(user2.getUser_id()).setValueAsync(user2);
        // Tạo dữ liệu mẫu cho bảng Doctor
        Doctor doctor1 = new Doctor();
        doctor1.setDoctor_id(1);
        doctor1.setUser_id(user1.getUser_id()); // Liên kết với User
        doctor1.setDepartment_id(101); // Giả sử có một phòng ban với ID 101
        doctor1.setPosition_id(201); // Giả sử có một chức vụ với ID 201
        doctor1.setInformation("Chuyên khoa tim mạch với hơn 20 năm kinh nghiệm.");
        doctor1.setExperience("Trưởng khoa tim mạch tại bệnh viện XYZ.");
        doctor1.setEducation("Tốt nghiệp Đại học Y Hà Nội.");
        Doctor doctor2 = new Doctor();
        doctor2.setDoctor_id(2);
        doctor2.setUser_id(user2.getUser_id()); // Liên kết với User
        doctor2.setDepartment_id(102); // Giả sử có một phòng ban với ID 102
        doctor2.setPosition_id(202); // Giả sử có một chức vụ với ID 202
        doctor2.setInformation("Chuyên khoa nội tiết với hơn 15 năm kinh nghiệm.");
        doctor2.setExperience("Bác sĩ nội tiết tại bệnh viện ABC.");
        doctor2.setEducation("Tốt nghiệp Đại học Y Dược TPHCM.");
        // Thêm dữ liệu Doctor vào Firebase
        doctorsRef.child(doctor1.getDoctor_id().toString()).setValueAsync(doctor1);
        doctorsRef.child(doctor2.getDoctor_id().toString()).setValueAsync(doctor2);


    }
}
