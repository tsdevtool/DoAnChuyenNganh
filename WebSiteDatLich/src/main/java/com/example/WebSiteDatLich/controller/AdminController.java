package com.example.WebSiteDatLich.controller;
import com.example.WebSiteDatLich.model.Diagnose;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
public class AdminController {
    @Autowired
    private  final AdminService adminService;
    public  AdminController(AdminService adminService){
        this.adminService= adminService;
    }
    @GetMapping("/doctoradmin")
    public String showDoctorAdmin(Model model) {
        try {
            CompletableFuture<List<Doctor>> doctorsFuture = adminService.getAllDoctorsAsync();
            List<Doctor> doctors = doctorsFuture.join();

            if (doctors.isEmpty()) {
                System.out.println("No doctors found."); // Log kiểm tra
            } else {
                System.out.println("Doctors loaded: " + doctors.size());
            }

            model.addAttribute("doctors", doctors);
            return "DoctorAdmin/doctoradmin-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/doctoradmin/add")
    public String showAddDoctorForm(Model model) {
        try {
            CompletableFuture<List<User>> usersFuture = adminService.getUsersWithRoleDoctorNotInDoctors();
            CompletableFuture<Map<String, Object>> departmentsFuture = adminService.getDepartments();
            CompletableFuture<Map<String, String>> positionsFuture = adminService.getPositions();

            List<User> users = usersFuture.join();
            Map<String, Object> departments = departmentsFuture.join();
            Map<String, String> positions = positionsFuture.join();

            model.addAttribute("users", users);
            model.addAttribute("departments", departments);
            model.addAttribute("positions", positions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu: " + e.getMessage());
        }

        return "DoctorAdmin/doctoradmin-add";
    }

    @PostMapping("/doctoradmin/add")
    public String addDoctor(@RequestParam("user_id") String userId,
                            @RequestParam("department_id") String departmentId, // String
                            @RequestParam("position_id") Integer positionId,
                            @RequestParam("education") String education,
                            @RequestParam("experience") String experience,
                            @RequestParam("information") String information,
                            RedirectAttributes redirectAttributes) {
        try {
            CompletableFuture<Boolean> result = adminService.addNewDoctor(userId, departmentId, positionId, education, experience, information);

            if (result.join()) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu thông tin bác sĩ!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm bác sĩ: " + e.getMessage());
        }

        return "redirect:/doctoradmin";
    }
    /*@GetMapping("/doctoradmin/detail/{id}")
    public String showDoctorDetail(@PathVariable("id") String doctorId, Model model) {
        try {
            CompletableFuture<Doctor> doctorFuture = adminService.getDoctorById(doctorId);
            CompletableFuture<Map<Integer, String>> departmentsFuture = adminService.getDepartmentNames();
            CompletableFuture<Map<Integer, String>> positionsFuture = adminService.getPositionNames();

            CompletableFuture.allOf(doctorFuture, departmentsFuture, positionsFuture).join();

            Doctor doctor = doctorFuture.get();
            Map<Integer, String> departments = departmentsFuture.get();
            Map<Integer, String> positions = positionsFuture.get();

            model.addAttribute("doctor", doctor);
            model.addAttribute("departments", departments);
            model.addAttribute("positions", positions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lấy thông tin chi tiết bác sĩ: " + e.getMessage());
            return "error-page";
        }

        return "DoctorAdmin/doctoradmin-detail";
    }*/
    @GetMapping("/doctoradmin/detail/{id}")
    public String showDoctorDetail(@PathVariable("id") String doctorId, Model model) {
        try {
            Map<String, Object> doctorDetails = adminService.getDoctorWithDetailsById(doctorId).join();

            model.addAttribute("doctor", doctorDetails.get("doctor"));
            model.addAttribute("userName", doctorDetails.get("userName"));
            model.addAttribute("avatarUrl", doctorDetails.get("avatarUrl"));
            model.addAttribute("departmentName", doctorDetails.get("departmentName"));
            model.addAttribute("positionName", doctorDetails.get("positionName"));

            return "DoctorAdmin/doctoradmin-detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi lấy thông tin chi tiết bác sĩ: " + e.getMessage());
            return "error-page";
        }
    }


    //cập nhập thông tin bác sĩ
    @GetMapping("/doctoradmin/edit/{id}")
    public String editDoctor(@PathVariable("id") String doctorId, Model model) {
        // Lấy thông tin bác sĩ
        CompletableFuture<Doctor> doctorFuture = adminService.getDoctorById(doctorId);
        CompletableFuture<Map<String, Object>> departmentsFuture = adminService.getDepartments();
        CompletableFuture<Map<String, String>> positionsFuture = adminService.getPositions();

        CompletableFuture.allOf(doctorFuture, departmentsFuture, positionsFuture).join();

        try {
            Doctor doctor = doctorFuture.get();
            Map<String, Object> departments = departmentsFuture.get();
            Map<String, String> positions = positionsFuture.get();

            model.addAttribute("doctor", doctor);
            model.addAttribute("departments", departments);
            model.addAttribute("positions", positions);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải dữ liệu cho form chỉnh sửa bác sĩ.", e);
        }

        return "DoctorAdmin/doctoradmin-edit";
    }


    @PostMapping("/doctoradmin/update")
    public String updateDoctor(@ModelAttribute Doctor doctor) {
        adminService.updateDoctor(
                doctor.getDoctor_id(),
                doctor.getDepartment_id(),
                doctor.getPosition_id(),
                doctor.getEducation(),
                doctor.getExperience(),
                doctor.getInformation()
        ).thenAccept(success -> {
            if (!success) {
                throw new RuntimeException("Cập nhật thất bại.");
            }
        }).join();
        return "redirect:/doctoradmin";
    }




    //Hiển thị danh sách Chuyên khoa
    @GetMapping("/departmentadmin")
    public String showDepartments(Model model) {
        List<Map<String, Object>> departments = adminService.getAllDepartments().join();
        model.addAttribute("departments", departments);
        return "DepartmentAdmin/index-list";
    }
    // Hiển thị form thêm chuyên khoa
    @GetMapping("/departmentadmin/add")
    public String showAddDepartmentForm(Model model) {
        model.addAttribute("department", new HashMap<>());
        return "DepartmentAdmin/index-add";
    }

    // Xử lý thêm chuyên khoa mới
    @PostMapping("/departmentadmin/add")
    public String addDepartment(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            // Upload hình ảnh và lấy URL
            String imageUrl = adminService.uploadImageToFirebaseStorage(image);

            // Lưu thông tin chuyên khoa vào Firebase
            adminService.addNewDepartment(name, imageUrl).join();

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chuyên khoa thành công!");
            return "redirect:/departmentadmin";
        } catch (Exception e) {
            // Thêm thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm chuyên khoa thất bại: " + e.getMessage());
            return "redirect:/departmentadmin/add";
        }
    }


    //Cập nhập chuyên khoa
    @GetMapping("/departmentadmin/edit/{id}")
    public String showEditDepartmentForm(@PathVariable("id") String id, Model model) {
        CompletableFuture<List<Map<String, Object>>> departmentsFuture = adminService.getAllDepartments();
        List<Map<String, Object>> departments = departmentsFuture.join();

        Map<String, Object> department = departments.stream()
                .filter(dept -> dept.get("department_id").toString().equals(id))
                .findFirst()
                .orElse(null);

        model.addAttribute("department", department);
        return "DepartmentAdmin/index-edit";
    }



    // Xử lý cập nhật chuyên khoa
    @PostMapping("/departmentadmin/edit")
    public String updateDepartment(
            @RequestParam("department_id") String departmentId,
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = null;

            // Nếu có hình ảnh mới, upload và lấy URL ảnh
            if (image != null && !image.isEmpty()) {
                imageUrl = adminService.uploadImageToFirebaseStorage(image, departmentId);
            }

            // Cập nhật thông tin chuyên khoa
            adminService.updateDepartment(departmentId, name, imageUrl).join();

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chuyên khoa thành công!");
            return "redirect:/departmentadmin";
        } catch (Exception e) {
            // Thêm thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
            return "redirect:/departmentadmin";
        }
    }


    // Xử lý yêu cầu xóa chuyên khoa
    @PostMapping("/departmentadmin/delete")
    public String deleteDepartment(@RequestParam String departmentId, Model model) {
        CompletableFuture<Boolean> deleteFuture = adminService.deleteDepartment(departmentId);
        try {
            deleteFuture.join(); // Chờ kết quả xóa
            return "redirect:/departmentadmin"; // Trở lại danh sách chuyên khoa sau khi xóa thành công
        } catch (Exception e) {
            model.addAttribute("error", "Không thể xóa chuyên khoa: " + e.getMessage());
            return "DepartmentAdmin/index-list";
        }
    }

    //Quản lý người dùng User
    @GetMapping("/useradmin")
    public String showUserList(Model model) {
        List<Map<String, Object>> users = adminService.getAllUsers().join();
        model.addAttribute("users", users);
        return "UserAdmin/user-list";
    }
    /*@PostMapping("/useradmin/updateRole")
    @ResponseBody
    public ResponseEntity<String> updateUserRole(@RequestBody Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            int roleId = (int) payload.get("roleId");

            // Gọi service để cập nhật role
            adminService.updateUserRole(userId, roleId);

            // Trả về phản hồi thành công
            return ResponseEntity.ok("Cập nhật quyền thành công!");
        } catch (Exception e) {
            // Xử lý lỗi và trả về thông báo lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cập nhật thất bại: " + e.getMessage());
        }
    }*/


    @PostMapping("/useradmin/updateRole")
    @ResponseBody
    public ResponseEntity<String> updateUserRole(@RequestBody Map<String, Object> payload) {
        try {
            // Log dữ liệu payload
            System.out.println("Payload nhận được: " + payload);

            String userId = (String) payload.get("userId");
            int roleId = (int) payload.get("roleId");

            // Gọi service để cập nhật role_id
            adminService.updateUserRole(userId, roleId).join();

            return ResponseEntity.ok("Cập nhật quyền thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi chi tiết
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cập nhật thất bại: " + e.getMessage());
        }
    }


    //Hiển thị form thêm user
    // Hiển thị form thêm người dùng
    @GetMapping("/useradmin/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new HashMap<>());
        return "UserAdmin/user-add";
    }

    //Xử lý thêm người dùng mới

    @PostMapping("/useradmin/addUser")
    public String addUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("role") int roleId,
            @RequestParam("gender") boolean sex,
            @RequestParam("avatar") MultipartFile avatar,
            @RequestParam("date_of_birth") String dateOfBirth,
            @RequestParam("password") String password, // Thêm tham số password
            RedirectAttributes redirectAttributes) {
        try {
            String avatarUrl = null;
            if (avatar != null && !avatar.isEmpty()) {
                avatarUrl = adminService.uploadImageToFirebaseStorage(avatar);
            }

            adminService.addUserToFirebase(name, email, phone, address, roleId, sex, avatarUrl, dateOfBirth, password).join();

            redirectAttributes.addFlashAttribute("successMessage", "Thêm người dùng thành công!");
            return "redirect:/useradmin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm người dùng thất bại: " + e.getMessage());
            return "redirect:/useradmin/add";
        }
    }
    @GetMapping("/useradmin/detail/{id}")
    public String showUserDetail(@PathVariable("id") String userId, Model model) {
        System.out.println("Bắt đầu lấy thông tin người dùng với ID: " + userId); // Log bắt đầu

        try {
            // Lấy thông tin chi tiết từ Firebase
            Map<String, Object> userDetails = adminService.getUserWithDetailsById(userId).join();

            System.out.println("Lấy thông tin người dùng thành công: " + userDetails); // Log dữ liệu lấy được

            model.addAttribute("user", userDetails.get("user"));
            return "UserAdmin/user-detail"; // Trả về trang user-detail.html
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi lấy thông tin chi tiết người dùng: " + e.getMessage());
            return "error-page"; // Trả về trang lỗi nếu có lỗi
        }
    }




    //Tìm kiếm user
    @GetMapping("/useradmin/search")
    @ResponseBody
    public List<Map<String, Object>> searchUsers(@RequestParam("keyword") String keyword) {
        // Lấy danh sách tất cả người dùng từ Firebase
        List<Map<String, Object>> allUsers = adminService.getAllUsers().join();

        // Lọc người dùng theo từ khóa (khớp với tên, email, hoặc số điện thoại)
        return allUsers.stream()
                .filter(user -> user.get("name").toString().toLowerCase().contains(keyword.toLowerCase())
                        || user.get("email").toString().toLowerCase().contains(keyword.toLowerCase())
                        || user.get("phone").toString().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    //Hiển thị danh sách chức vụ
    @GetMapping("/positionadmin")
    public String showPositions(Model model) {
        Map<String, String> positions = adminService.getPositions().join();
        List<Map<String, String>> positionList = positions.entrySet().stream()
                .map(entry -> Map.of("id", entry.getKey(), "name", entry.getValue()))
                .collect(Collectors.toList());
        model.addAttribute("positions", positionList);
        return "PositionAdmin/position-list"; // Tên file HTML hiển thị danh sách
    }
    // Hiển thị form thêm chức vụ
    @GetMapping("/positionadmin/add")
    public String showAddPositionForm() {
        return "PositionAdmin/position-add";
    }

    // Xử lý thêm chức vụ
    @PostMapping("/positionadmin/add")
    public String createPosition(@RequestParam("name") String name, RedirectAttributes redirectAttributes) {
        try {
            adminService.addPositionWithNumericId(name).join(); // Gọi Service để thêm mới
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chức vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm chức vụ thất bại: " + e.getMessage());
        }
        return "redirect:/positionadmin";
    }
    // Hiển thị form sửa chức vụ
    @GetMapping("/positionadmin/edit/{id}")
    public String showEditPositionForm(@PathVariable("id") String id, Model model) {
        Map<String, String> positions = adminService.getPositions().join();
        model.addAttribute("position", Map.of("id", id, "name", positions.get(id)));
        return "PositionAdmin/position-edit";
    }
    @PostMapping("/positionadmin/edit")
    public String updatePosition(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.updatePosition(id, name).join();
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chức vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật chức vụ thất bại: " + e.getMessage());
        }
        return "redirect:/positionadmin";
    }



    // Xóa chức vụ
    @PostMapping("/positionadmin/delete/{id}")
    public String deletePosition(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deletePosition(id).join();
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chức vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/positionadmin";
    }


    //Hiển thị danh sách chẩn đoán
    @GetMapping("/diagnoseadmin")
    public String listDiagnoses(Model model) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> diagnoses = adminService.getAllDiagnosesWithDepartmentNames().get();
        model.addAttribute("diagnoses", diagnoses);
        return "DiagnoseAdmin/diagnose-list";
    }

    @GetMapping("/diagnoseadmin/add")
    public String addDiagnoseForm(Model model) throws ExecutionException, InterruptedException {
        // Lấy danh sách các khoa từ Firebase
        model.addAttribute("departments", adminService.getAllDepartments().get());
        model.addAttribute("diagnose", new Diagnose());
        return "DiagnoseAdmin/diagnose-add";
    }


    @PostMapping("/diagnoseadmin/add")
    public String addDiagnose(@RequestParam String name, @RequestParam String department_id) throws ExecutionException, InterruptedException {
        adminService.addDiagnose(name, department_id).get();
        return "redirect:/diagnoseadmin";
    }
    @GetMapping("/diagnoseadmin/edit/{id}")
    public String editDiagnoseForm(@PathVariable String id, Model model) throws ExecutionException, InterruptedException {
        // Lấy thông tin chẩn đoán theo ID
        Diagnose diagnose = adminService.getDiagnoseById(id).get();
        // Lấy danh sách chuyên khoa
        List<Map<String, Object>> departments = adminService.getAllDepartments().get();
        // Gắn thông tin vào model
        model.addAttribute("diagnose", diagnose);
        model.addAttribute("departments", departments);
        return "DiagnoseAdmin/diagnose-edit";
    }

    @PostMapping("/diagnoseadmin/edit")
    public String editDiagnose(@ModelAttribute Diagnose diagnose) throws ExecutionException, InterruptedException {
        adminService.updateDiagnose(diagnose).get();
        return "redirect:/diagnoseadmin";
    }

    @PostMapping("/diagnoseadmin/delete/{id}")
    public String deleteDiagnose(@PathVariable String id) throws ExecutionException, InterruptedException {
        adminService.deleteDiagnose(id).get();
        return "redirect:/diagnoseadmin";
    }

}
