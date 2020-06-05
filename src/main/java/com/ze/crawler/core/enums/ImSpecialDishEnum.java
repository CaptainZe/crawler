package com.ze.crawler.core.enums;

import lombok.Getter;

/**
 * IM盘口特殊盘枚举WinHandicapA
 */
@Getter
public enum ImSpecialDishEnum {
    // 让分盘
    WINHANDICAPA_15("WinHandicapA-15", "SERIES_RFP", "-1.5", "1.5"),
    WINHANDICAPB_15("WinHandicapB-15", "SERIES_RFP", "1.5", "-1.5"),
    WINHANDICAPA_25("WinHandicapA-25", "SERIES_RFP", "-2.5", "2.5"),
    WINHANDICAPB_25("WinHandicapB-25", "SERIES_RFP", "2.5", "-2.5"),
    ;

    /**
     * im盘口中可获获取到的值
     */
    private String originalValue;
    /**
     * 自定义匹配值
     */
    private String customValue;
    /**
     * 主队item
     */
    private String homeTeamItem;
    /**
     * 客队item
     */
    private String guestTeamItem;
    ImSpecialDishEnum(String originalValue, String customValue, String homeTeamItem, String guestTeamItem) {
        this.originalValue = originalValue;
        this.customValue = customValue;
        this.homeTeamItem = homeTeamItem;
        this.guestTeamItem = guestTeamItem;
    }

    /**
     * 根据盘口原始值获取枚举
     * @param value
     * @return
     */
    public static ImSpecialDishEnum getImSpecialDishByOriginalValue(String value) {
        for (ImSpecialDishEnum imSpecialDishEnum : ImSpecialDishEnum.values()) {
            if (imSpecialDishEnum.originalValue.equalsIgnoreCase(value)) {
                return imSpecialDishEnum;
            }
        }
        return null;
    }
}
