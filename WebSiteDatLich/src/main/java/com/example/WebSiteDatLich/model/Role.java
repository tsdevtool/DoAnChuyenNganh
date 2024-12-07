package com.example.WebSiteDatLich.model;



public enum Role {
    ADMIN(1),
    DOCTOR(2),
    USER(3);

    private final int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Role fromId(int id) {
        for (Role role : Role.values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        return USER; // Giá trị mặc định nếu không tìm thấy
    }
}
