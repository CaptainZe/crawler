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
import java.util.HashMap;
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

    /*      体育      */

    /**
     * 解析并缓存体育映射表
     */
    public String sportsMapping() {
        File excelFile = new File(Constant.SPORT_MAPPING_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            // 初始化体育字典表
            initSportDictionary();

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
                String type = getCellValue(row.getCell(0));
                String id = getCellValue(row.getCell(1));
                String pbName = getCellValue(row.getCell(2));
                String ybName = getCellValue(row.getCell(3));
                String sbName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));
                String btiName = getCellValue(row.getCell(6));

                if (!StringUtils.isEmpty(pbName)) {
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.SPORT_PB_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(ybName)) {
                    List<String> ybNames = splitCellValue(ybName);
                    for (String name : ybNames) {
                        Dictionary.SPORT_YB_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(sbName)) {
                    List<String> sbNames = splitCellValue(sbName);
                    for (String name : sbNames) {
                        Dictionary.SPORT_SB_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.SPORT_IM_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(btiName)) {
                    List<String> btiNames = splitCellValue(btiName);
                    for (String name : btiNames) {
                        Dictionary.SPORT_BTI_LEAGUE_MAPPING.get(type).put(name, id);
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

                if (!StringUtils.isEmpty(pbName)) {
                    Dictionary.SPORT_PB_DISH_MAPPING.get(leagueType).put(pbName, id);
                }
                if (!StringUtils.isEmpty(ybName)) {
                    Dictionary.SPORT_YB_DISH_MAPPING.get(leagueType).put(ybName, id);
                }
                if (!StringUtils.isEmpty(sbName)) {
                    Dictionary.SPORT_SB_DISH_MAPPING.get(leagueType).put(sbName, id);
                }
                if (!StringUtils.isEmpty(imName)) {
                    Dictionary.SPORT_IM_DISH_MAPPING.get(leagueType).put(imName, id);
                }
                if (!StringUtils.isEmpty(btiName)) {
                    Dictionary.SPORT_BTI_DISH_MAPPING.get(leagueType).put(btiName, id);
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
     * 体育字典初始化
     */
    private void initSportDictionary() {
        // 联赛
        Dictionary.SPORT_PB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_PB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_YB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_YB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_SB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_SB_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_IM_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_IM_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_BTI_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_BTI_LEAGUE_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        
        // 盘口
        Dictionary.SPORT_PB_DISH_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_PB_DISH_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_YB_DISH_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_YB_DISH_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_SB_DISH_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_SB_DISH_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_IM_DISH_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_IM_DISH_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
        Dictionary.SPORT_BTI_DISH_MAPPING.put(Constant.SPORTS_TYPE_SOCCER, new HashMap<>());
        Dictionary.SPORT_BTI_DISH_MAPPING.put(Constant.SPORTS_TYPE_BASKETBALL, new HashMap<>());
    }

    /*      电竞      */

    /**
     * 解析并缓存电竞映射表
     */
    public String esportsMapping() {
        File excelFile = new File(Constant.ESPORT_MAPPING_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            // 初始化电竞字典表
            initEsportDictionary();

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
                String type = getCellValue(row.getCell(0));
                String id = getCellValue(row.getCell(1));
                String pbName = getCellValue(row.getCell(2));
                String rgName = getCellValue(row.getCell(3));
                String tfName = getCellValue(row.getCell(4));
                String imName = getCellValue(row.getCell(5));
                String fyName = getCellValue(row.getCell(6));

                if (!StringUtils.isEmpty(pbName)) {
                    List<String> pbNames = splitCellValue(pbName);
                    for (String name : pbNames) {
                        Dictionary.ESPORT_PB_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(rgName)) {
                    List<String> rgNames = splitCellValue(rgName);
                    for (String name : rgNames) {
                        Dictionary.ESPORT_RG_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(tfName)) {
                    List<String> tfNames = splitCellValue(tfName);
                    for (String name : tfNames) {
                        Dictionary.ESPORT_TF_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(imName)) {
                    List<String> imNames = splitCellValue(imName);
                    for (String name : imNames) {
                        Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(type).put(name, id);
                    }
                }
                if (!StringUtils.isEmpty(fyName)) {
                    List<String> fyNames = splitCellValue(fyName);
                    for (String name : fyNames) {
                        Dictionary.ESPORT_FY_LEAGUE_MAPPING.get(type).put(name, id);
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
                String fyName = getCellValue(row.getCell(6));

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
                if (!Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.containsKey(leagueId)) {
                    Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.put(leagueId, new LinkedHashMap<>());
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
                if (!StringUtils.isEmpty(fyName)) {
                    List<String> fyNames = splitCellValue(fyName);
                    for (String name : fyNames) {
                        Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).put(name.toUpperCase(), id);
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
                String fyName = getCellValue(row.getCell(8));

                if (!StringUtils.isEmpty(pbName)) {
                    Dictionary.ESPORT_PB_DISH_MAPPING.get(leagueType).put(pbName, id);
                }
                if (!StringUtils.isEmpty(rgName)) {
                    Dictionary.ESPORT_RG_DISH_MAPPING.get(leagueType).put(rgName, id);
                }
                if (!StringUtils.isEmpty(tfName)) {
                    Dictionary.ESPORT_TF_DISH_MAPPING.get(leagueType).put(tfName, id);
                }
                if (!StringUtils.isEmpty(imName)) {
                    Dictionary.ESPORT_IM_DISH_MAPPING.get(leagueType).put(imName, id);
                }
                if (!StringUtils.isEmpty(imDisplayName)) {
                    Dictionary.ESPORT_IM_DISH_DISPLAY_MAPPING.get(leagueType).put(imName, imDisplayName);
                }
                if (!StringUtils.isEmpty(fyName)) {
                    Dictionary.ESPORT_FY_DISH_MAPPING.get(leagueType).put(fyName, id);
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
     * 电竞字典初始化
     */
    private void initEsportDictionary() {
        // 联赛
        Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_PB_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_RG_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_TF_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_IM_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_FY_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_FY_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_FY_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_FY_LEAGUE_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());

        // 盘口
        Dictionary.ESPORT_PB_DISH_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_PB_DISH_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_PB_DISH_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_PB_DISH_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_RG_DISH_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_RG_DISH_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_RG_DISH_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_RG_DISH_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_TF_DISH_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_TF_DISH_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_TF_DISH_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_TF_DISH_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
        Dictionary.ESPORT_FY_DISH_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_FY_DISH_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_FY_DISH_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_FY_DISH_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());

        Dictionary.ESPORT_IM_DISH_DISPLAY_MAPPING.put(Constant.ESPORTS_TYPE_LOL, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_DISPLAY_MAPPING.put(Constant.ESPORTS_TYPE_DOTA2, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_DISPLAY_MAPPING.put(Constant.ESPORTS_TYPE_CSGO, new HashMap<>());
        Dictionary.ESPORT_IM_DISH_DISPLAY_MAPPING.put(Constant.ESPORTS_TYPE_KPL, new HashMap<>());
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
