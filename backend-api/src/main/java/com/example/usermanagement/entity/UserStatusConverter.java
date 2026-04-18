package com.example.usermanagement.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 用户状态枚举转换器
 * yongHu_zhuangTai_meiJu_zhuanHuan_qi
 */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<User.UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(User.UserStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public User.UserStatus convertToEntityAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return User.UserStatus.ACTIVE;
        }
        return User.UserStatus.fromValue(value.toLowerCase());
    }
}
