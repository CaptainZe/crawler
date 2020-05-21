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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 映射
 */
@SuppressWarnings("all")
@Service
public class MappingService {

    // 完成返回值
    private static final String DONE = "done";

    /**
     * 解析并缓存体育映射表
     */
    public String sportsMapping() {
        File excelFile = new File(Constant.SPORT_MAPPING_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            // 1. sheet[0] -- 联赛映射表
            HSSFSheet leagueMappingSheet = workbook.getSheetAt(0);
            for (int rowIndex=1; rowIndex <= leagueMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 类型、ID、平博体育、188体育(yabo)、沙巴体育、IM体育、BTI体育
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
                String ybName = getCellValue(row.getCell(3));
                String sbName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));
                String btiName = getCellValue(row.getCell(6));

                if (!StringUtils.isEmpty(pbName)) {
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.SPORT_PB_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(ybName)) {
                    List<String> ybNames = splitCellValue(ybName);
                    for (String name : ybNames) {
                        Dictionary.SPORT_YB_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(sbName)) {
                    List<String> sbNames = splitCellValue(sbName);
                    for (String name : sbNames) {
                        Dictionary.SPORT_SB_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.SPORT_IM_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(btiName)) {
                    List<String> btiNames = splitCellValue(btiName);
                    for (String name : btiNames) {
                        Dictionary.SPORT_BTI_LEAGUE_MAPPING.put(name, id);
                    }
                }
            }

            // 2. sheet[1] -- 队伍映射表
            HSSFSheet teamMappingSheet = workbook.getSheetAt(1);
            for (int rowIndex=1; rowIndex <= teamMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 联赛ID、ID、平博体育、188体育(yabo)、沙巴体育、IM体育、BTI体育
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
                String ybName = getCellValue(row.getCell(3));
                String sbName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));
                String btiName = getCellValue(row.getCell(6));

                if (!Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.SPORT_SB_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.SPORT_SB_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }
                if (!Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
                }

                if (!StringUtils.isEmpty(pbName)) {
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(ybName)) {
                    List<String> ybNames = splitCellValue(ybName);
                    for (String name : ybNames) {
                        Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(sbName)) {
                    List<String> sbNames = splitCellValue(sbName);
                    for (String name : sbNames) {
                        Dictionary.SPORT_SB_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(btiName)) {
                    List<String> btiNames = splitCellValue(btiName);
                    for (String name : btiNames) {
                        Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
            }

            // 3. sheet[2] -- 盘口映射表
            HSSFSheet dishMappingSheet = workbook.getSheetAt(2);
            for (int rowIndex=1; rowIndex <= dishMappingSheet.getLastRowNum(); rowIndex++) {
                // 列顺序: 赛事类型、盘口类型、ID、平博体育、188体育(yabo)、沙巴体育、IM体育、BTI体育
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
                String ybName = getCellValue(row.getCell(4));
                String sbName = getCellValue(row.getCell(5));
                String imName = getCellValue(row.getCell(6));
                String btiName = getCellValue(row.getCell(7));

                if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(leagueType)) {
                    // 足球
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.SPORT_SOCCER_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(ybName)) {
                        Dictionary.SPORT_SOCCER_YB_DISH_MAPPING.put(ybName, id);
                    }
                    if (!StringUtils.isEmpty(sbName)) {
                        Dictionary.SPORT_SOCCER_SB_DISH_MAPPING.put(sbName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.SPORT_SOCCER_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(btiName)) {
                        Dictionary.SPORT_SOCCER_BTI_DISH_MAPPING.put(btiName, id);
                    }
                } else if (Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(leagueType)) {
                    // 足球
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.SPORT_BASKETBALL_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(ybName)) {
                        Dictionary.SPORT_BASKETBALL_YB_DISH_MAPPING.put(ybName, id);
                    }
                    if (!StringUtils.isEmpty(sbName)) {
                        Dictionary.SPORT_BASKETBALL_SB_DISH_MAPPING.put(sbName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.SPORT_BASKETBALL_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(btiName)) {
                        Dictionary.SPORT_BASKETBALL_BTI_DISH_MAPPING.put(btiName, id);
                    }
                }

                // 盘口类型映射
                Dictionary.SPORT_DISH_TYPE_MAPPING.put(id, dishType);
            }

            return DONE;
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

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
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(rgName)) {
                    List<String> rgNames = splitCellValue(rgName);
                    for (String name : rgNames) {
                        Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(tfName)) {
                    List<String> tfNames = splitCellValue(tfName);
                    for (String name : tfNames) {
                        Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(name, id);
                    }
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
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(rgName)) {
                    List<String> rgNames = splitCellValue(rgName);
                    for (String name : rgNames) {
                        Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(tfName)) {
                    List<String> tfNames = splitCellValue(tfName);
                    for (String name : tfNames) {
                        Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
                    }
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
                } else if (Constant.ESPORTS_TYPE_KPL.equalsIgnoreCase(leagueType)) {
                    // 王者荣耀
                    if (!StringUtils.isEmpty(pbName)) {
                        Dictionary.ESPORT_KPL_PB_DISH_MAPPING.put(pbName, id);
                    }
                    if (!StringUtils.isEmpty(rgName)) {
                        Dictionary.ESPORT_KPL_RG_DISH_MAPPING.put(rgName, id);
                    }
                    if (!StringUtils.isEmpty(tfName)) {
                        Dictionary.ESPORT_KPL_TF_DISH_MAPPING.put(tfName, id);
                    }
                    if (!StringUtils.isEmpty(imName)) {
                        Dictionary.ESPORT_KPL_IM_DISH_MAPPING.put(imName, id);
                    }
                    if (!StringUtils.isEmpty(imDisplayName)) {
                        Dictionary.ESPORT_KPL_IM_DISH_DISPLAY_MAPPING.put(imName, imDisplayName);
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

    /**
     * 有的映射可能存在多个名字，通过#进行分隔
     * @param value
     * @return
     */
    private List<String> splitCellValue(String value) {
        String[] array = value.split("#");
        return Arrays.asList(array);
    }
}
