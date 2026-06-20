package com.campuslink.dto;

import lombok.Data;

/**
 * 编辑队伍请求。
 */
@Data
public class TeamUpdateDTO {

    private String name;

    private Long competitionId;

    private String intro;

    private Integer totalSize;

    private String college;

    /** RECRUITING / FULL / CLOSED */
    private String status;
}
