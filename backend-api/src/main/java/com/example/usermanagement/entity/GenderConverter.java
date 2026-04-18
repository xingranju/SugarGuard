package com.example.usermanagement.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 性别枚举转换器
 * xingBie_meiJu_zhuanHuan_qi
 */
@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<User.Gender, String> {

    @Override
    public String convertToDatabaseColumn(User.Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender.getValue();
    }

    @Override
    public User.Gender convertToEntityAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return User.Gender.OTHER;
        }
        return User.Gender.fromValue(value.toLowerCase());
    }
}
