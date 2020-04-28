package com.ze.crawler.core.service.mapping;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;

/**
 * 映射
 */
@Service
public class MappingService {

    // 完成返回值
    private static final String DONE = "done";

    /**
     * 解析并缓存电竞映射表
     */
    public String esportsMapping() {
        File excelFile = new File(Constant.ESPORT_MAPPING_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            // 1. sheet[0] -- 联赛映射表
            HSSFSheet leagueMappingSheet = workbook.getSheetAt(0);
            for (int rowIndex=1; rowIndex <= leagueMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 类型、ID、平博电竞、RG电竞、TF电竞、IM电竞
                HSSFRow row = leagueMappingSheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                if (row.getCell(0) == null) {
                    // 分隔线
                    continue;
                }

                // 各列信息
                String id = getCellValue(row.getCell(1));
                String pbName = getCellValue(row.getCell(2));
                String rgName = getCellValue(row.getCell(3));
                String tfName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));

                if (!StringUtils.isEmpty(pbName)) {
                    Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(pbName, id);
                }
                if (!StringUtils.isEmpty(rgName)) {
                    Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(rgName, id);
                }
                if (!StringUtils.isEmpty(tfName)) {
                    Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(tfName, id);
                }
                if (!StringUtils.isEmpty(imName)) {
                    Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(imName, id);
                }
            }

            // 2. sheet[1] -- 队伍映射表
            HSSFSheet teamMappingSheet = workbook.getSheetAt(1);
            for (int rowIndex=1; rowIndex <= teamMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 联赛ID、ID、平博电竞、RG电竞、TF电竞、IM电竞
                HSSFRow row = teamMappingSheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                if (row.getCell(0) == null) {
                    // 分隔线
                    continue;
                }

                // 各列信息
                String leagueId = getCellValue(row.getCell(0));
                String id = getCellValue(row.getCell(1));
                String pbName = getCellValue(row.getCell(2));
                String rgName = getCellValue(row.getCell(3));
                String tfName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));

                if (!Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }

                if (!StringUtils.isEmpty(pbName)) {
                    Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).put(pbName.toUpperCase(), id);
                }
                if (!StringUtils.isEmpty(rgName)) {
                    Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).put(rgName.toUpperCase(), id);
                }
                if (!StringUtils.isEmpty(tfName)) {
                    Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).put(tfName.toUpperCase(), id);
                }
                if (!StringUtils.isEmpty(imName)) {
                    Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).put(imName.toUpperCase(), id);
                }
            }

            // 3. sheet[2] -- 盘口映射表
            HSSFSheet dishMappingSheet = workbook.getSheetAt(2);
            for (int rowIndex=1; rowIndex <= dishMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 赛事类型、盘口类型、ID、平博电竞、RG电竞、TF电竞、IM电竞（匹配名）、IM电竞（显示名）
                HSSFRow row = dishMappingSheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                if (row.getCell(0) == null) {
                    // 分隔线
                    continue;
                }

                String leagueType = getCellValue(row.getCell(0));
                String dishType = getCellValue(row.getCell(1));
                String id = getCellValue(row.getCell(2));
                String pbName = getCellValue(row.getCell(3));
                String rgName = getCellValue(row.getCell(4));
                String tfName = getCellValue(row.getCell(5));
                String imName = getCellValue(row.getCell(6));
                String imDisplayName = getCellValue(row.getCell(7));
                
                if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(leagueType)) {
                    // LOL
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.ESPORT_LOL_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(rgName)) {
                        Dictionary.ESPORT_LOL_RG_DISH_MAPPING.put(rgName, id);
                    }
                    if (!StringUtils.isEmpty(tfName)) {
                        Dictionary.ESPORT_LOL_TF_DISH_MAPPING.put(tfName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.ESPORT_LOL_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(imDisplayName)) {
                        Dictionary.ESPORT_LOL_IM_DISH_DISPLAY_MAPPING.put(imName, imDisplayName);
                    }
                } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(leagueType)) {
                    // DOTA2
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.ESPORT_DOTA2_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(rgName)) {
                        Dictionary.ESPORT_DOTA2_RG_DISH_MAPPING.put(rgName, id);
                    }
                    if (!StringUtils.isEmpty(tfName)) {
                        Dictionary.ESPORT_DOTA2_TF_DISH_MAPPING.put(tfName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.ESPORT_DOTA2_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(imDisplayName)) {
                        Dictionary.ESPORT_DOTA2_IM_DISH_DISPLAY_MAPPING.put(imName, imDisplayName);
                    }
                } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(leagueType)) {
                    // CSGO
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.ESPORT_CSGO_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(rgName)) {
                        Dictionary.ESPORT_CSGO_RG_DISH_MAPPING.put(rgName, id);
                    }
                    if (!StringUtils.isEmpty(tfName)) {
                        Dictionary.ESPORT_CSGO_TF_DISH_MAPPING.put(tfName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.ESPORT_CSGO_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(imDisplayName)) {
                        Dictionary.ESPORT_CSGO_IM_DISH_DISPLAY_MAPPING.put(imName, imDisplayName);
                    }
                }

                // 盘口类型映射
                Dictionary.ESPORT_DISH_TYPE_MAPPING.put(id, dishType);
            }

            return DONE;
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * 获取单元格的值
     */
    private String getCellValue(HSSFCell cell) {
        if (cell == null) {
            return "";
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
