package com.gmoon.spreadsheet.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

@Setter
@Getter
public class ResourceVO {

    private String language;

    private SortedMap<String, String> dataMap = new TreeMap<>();

    public static ResourceVO newInstance(String language) {
        ResourceVO vo = new ResourceVO();
        vo.setLanguage(language);
        return vo;
    }

    public static boolean verify(String language) {
        return Arrays.asList("ko", "en", "ja", "zh_CN", "zh_TW").stream()
                .anyMatch(str -> str.equals(language));
    }
}
