package com.printclub.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人统计返回
 *
 * @author A
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {

    /** 累计打印次数 */
    private Integer totalPrints;

    /** 参与项目数 */
    private Integer totalProjects;

    /** 作品数 */
    private Integer totalArtworks;
}
