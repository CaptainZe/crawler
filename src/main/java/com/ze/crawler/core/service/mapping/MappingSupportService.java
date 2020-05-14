package com.ze.crawler.core.service.mapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.*;
import com.ze.crawler.core.model.RgESportsResultItemModel;
import com.ze.crawler.core.model.RgESportsResultModel;
import com.ze.crawler.core.model.RgESportsResultTeamModel;
import com.ze.crawler.core.model.TfESportsResultModel;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 映射辅助类
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class MappingSupportService {

    private static final String ESPORT_MAPPING_SUPPORT_FILE_PATH = "D:/Crawler/support/esport_mapping_support.xls";

    // 认证token
    private final static String AUTHORIZATION = "Token c4b789e82ce341ac985e44b6b4da5042";

    /**
     * 爬取比赛映射
     */
    public void mappingSupport() {
        OutputStream out = null;
        File excelFile = new File(ESPORT_MAPPING_SUPPORT_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            HSSFCellStyle style = workbook.createCellStyle();
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
            style.setFillForegroundColor(HSSFColor.RED.index);    //填红色

            // IM电竞
            int rowIndex = 1;
            HSSFSheet imSheet = workbook.getSheetAt(0);
            // LOL
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);

            // 平博电竞
            rowIndex = 1;
            HSSFSheet pbSheet = workbook.getSheetAt(1);
            // LOL
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);

            // RG电竞
            rowIndex = 1;
            HSSFSheet rgSheet = workbook.getSheetAt(2);
            // LOL
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);

            // TF电竞
            rowIndex = 1;
            HSSFSheet tfSheet = workbook.getSheetAt(3);
            // LOL
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);

            out = new FileOutputStream(ESPORT_MAPPING_SUPPORT_FILE_PATH);
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if(out != null){
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * IM
     */
    public int imESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("IM电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 2;

        JSONObject body = getBaseBody(null, null);
        Map<String, Object> map = HttpClientUtils.post(ImConstant.IM_BASE_URL, body, Map.class);
        if (map != null && map.get("d") != null) {
            List<List<Object>> d = (List<List<Object>>) map.get("d");
            if (!CollectionUtils.isEmpty(d)) {
                Integer sportId = null;
                if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
                    sportId = ImConstant.SPORT_ID_LOL;
                } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
                    sportId = ImConstant.SPORT_ID_DOTA2;
                } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
                    sportId = ImConstant.SPORT_ID_CSGO;
                }

                if (sportId != null) {
                    for (List<Object> league : d) {
                        // [8]
                        Integer gameType = (Integer) league.get(8);
                        if (!sportId.equals(gameType)) {
                            continue;
                        }

                        // [1][5]都有联赛信息,目前取[5]
                        List<String> leagueNames = (List<String>) league.get(5);
                        if (CollectionUtils.isEmpty(leagueNames) && leagueNames.size() < 2) {
                            continue;
                        }

                        // 联赛名
                        String leagueName = leagueNames.get(1).trim();
                        String leagueId = Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(leagueName);

                        // [10]比赛列表
                        List<List<Object>> games = (List<List<Object>>) league.get(10);
                        if (!CollectionUtils.isEmpty(games)) {
                            for (List<Object> game : games) {
                                // [5]主队    [6]客队
                                List<String> homeTeamNames = (List<String>) game.get(5);
                                if (CollectionUtils.isEmpty(homeTeamNames) && homeTeamNames.size() < 2) {
                                    continue;
                                }
                                // 主队名
                                String homeTeamName = homeTeamNames.get(2);
                                homeTeamName = homeTeamName.trim();

                                List<String> guestTeamNames = (List<String>) game.get(6);
                                if (CollectionUtils.isEmpty(guestTeamNames) && guestTeamNames.size() < 2) {
                                    continue;
                                }
                                // 客队名
                                String guestTeamName = guestTeamNames.get(2);
                                guestTeamName = guestTeamName.trim();

                                if (leagueId == null) {
                                    // 联赛未录入
                                    HSSFRow homeRow = sheet.createRow(startRowIndex);
                                    setExcelCellValue(homeRow, 0, leagueName, style);
                                    setExcelCellValue(homeRow, 1, homeTeamName, style);
                                    startRowIndex++;

                                    HSSFRow guestRow = sheet.createRow(startRowIndex);
                                    setExcelCellValue(guestRow, 0, leagueName, style);
                                    setExcelCellValue(guestRow, 1, guestTeamName, style);
                                    startRowIndex++;
                                } else {
                                    // 联赛已录入
                                    String homeTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                                    String guestTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                                    String leagueCellValue = leagueName + "(" + leagueId + ")";
                                    if (homeTeamId == null) {
                                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                                        setExcelCellValue(homeRow, 1, homeTeamName, style);
                                        startRowIndex++;
                                    } else {
                                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                                        setExcelCellValue(homeRow, 1, homeTeamName, null);
                                        startRowIndex++;
                                    }

                                    if (guestTeamId == null) {
                                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                                        setExcelCellValue(guestRow, 1, guestTeamName, style);
                                        startRowIndex++;
                                    } else {
                                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                                        setExcelCellValue(guestRow, 1, guestTeamName, null);
                                        startRowIndex++;
                                    }
                                }
                            }
                        }

                        startRowIndex++;
                    }
                }
            }
        }

        return startRowIndex+1;
    }

    /**
     * 平博
     */
    public int pbESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("平博电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 2;

        List<List<Object>> allLeagues = new ArrayList<>();

        // 今天
        String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
        Map<String, Object> map = HttpClientUtils.get(url, Map.class);
        if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
            List<Object> n = (List<Object>) map.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                // eSports 电竞盘列表 (1是E Sports 2是联赛列表)
                List<Object> eSports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(eSports)) {
                    // 联赛列表 (这个是全部电竞的列表, 包含LOL,DOTA2,CSGO等, 需要根据type过滤想要的)
                    List<List<Object>> leagues = (List<List<Object>>) eSports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        allLeagues.addAll(leagues);
                    }
                }
            }
        }

        // 早盘
        String url2 = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_ZP, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
        Map<String, Object> map2 = HttpClientUtils.get(url2, Map.class);
        if (map2 != null && map2.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map2.get("n"))) {
            List<Object> n = (List<Object>) map2.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                // eSports 电竞盘列表 (1是E Sports 2是联赛列表)
                List<Object> eSports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(eSports)) {
                    // 联赛列表 (这个是全部电竞的列表, 包含LOL,DOTA2,CSGO等, 需要根据type过滤想要的)
                    List<List<Object>> leagues = (List<List<Object>>) eSports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        allLeagues.addAll(leagues);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(allLeagues)) {
            // 遍历联赛列表
            for (List<Object> league : allLeagues) {
                // 联赛名, 比如: 英雄联盟 - 中国LPL
                String leagueName = ((String) league.get(1)).trim();

                // 根据type过滤想要的联赛
                if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                    if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_LOL)
                            && !leagueName.startsWith(PBConstant.LEAGUE_PREFIX_LOL_EN)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                    if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_DOTA2)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                    if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_CSGO)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 比如王者荣耀, 暂不需要
                    continue;
                }

                String leagueId = Dictionary.ESPORT_PB_LEAGUE_MAPPING.get(leagueName);

                // 具体比赛列表
                List<List<Object>> games = (List<List<Object>>) league.get(2);
                if (!CollectionUtils.isEmpty(games)) {
                    for (List<Object> game : games) {
                        // home team name
                        String homeTeamName = (String) game.get(1);
                        homeTeamName = homeTeamName.trim();
                        // guest team name
                        String guestTeamName = (String) game.get(2);
                        guestTeamName = guestTeamName.trim();

                        String matchHomeTeamName = null;
                        String matchGuestTeamName = null;
                        if (isKill(homeTeamName)) {
                            matchHomeTeamName = homeTeamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX, "").trim();
                            matchGuestTeamName = guestTeamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX, "").trim();
                        } else {
                            matchHomeTeamName = homeTeamName;
                            matchGuestTeamName = guestTeamName;
                        }

                        if (leagueId == null) {
                            // 联赛未录入
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 0, leagueName, style);
                            setExcelCellValue(homeRow, 1, matchHomeTeamName, style);
                            startRowIndex++;

                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 0, leagueName, style);
                            setExcelCellValue(guestRow, 1, matchGuestTeamName, style);
                            startRowIndex++;
                        } else {
                            // 联赛已录入
                            String homeTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchHomeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchGuestTeamName.toUpperCase());

                            String leagueCellValue = leagueName + "(" + leagueId + ")";
                            if (homeTeamId == null) {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueCellValue, null);
                                setExcelCellValue(homeRow, 1, matchHomeTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueCellValue, null);
                                setExcelCellValue(homeRow, 1, matchHomeTeamName, null);
                                startRowIndex++;
                            }

                            if (guestTeamId == null) {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueCellValue, null);
                                setExcelCellValue(guestRow, 1, matchGuestTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueCellValue, null);
                                setExcelCellValue(guestRow, 1, matchGuestTeamName, null);
                                startRowIndex++;
                            }
                        }
                    }
                }

                startRowIndex++;
            }
        }

        return startRowIndex+1;
    }

    /**
     * RG
     */
    public int rgESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("RG电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 2;

        List<RgESportsResultItemModel> allResult = new ArrayList<>();

        // 今日
        for (int page = 1; page <= RGConstant.MAX_PAGE; page++) {
            String url = String.format(RGConstant.RG_BASE_URL, page, RGConstant.MATCH_TYPE_TODAY);
            RgESportsResultModel rgESportsResultModel = HttpClientUtils.get(url, RgESportsResultModel.class);
            if (rgESportsResultModel != null && !CollectionUtils.isEmpty(rgESportsResultModel.getResult())) {
                allResult.addAll(rgESportsResultModel.getResult());
            }
        }
        // 赛前。 包含未来几天的数据，只找第二天的
        for (int page=1; page <= RGConstant.MAX_PAGE; page++) {
            String url = String.format(RGConstant.RG_BASE_URL, page, RGConstant.MATCH_TYPE_ZP);
            RgESportsResultModel rgESportsResultModel = HttpClientUtils.get(url, RgESportsResultModel.class);
            if (rgESportsResultModel != null && !CollectionUtils.isEmpty(rgESportsResultModel.getResult())) {
                allResult.addAll(rgESportsResultModel.getResult());
            }
        }

        if (!CollectionUtils.isEmpty(allResult)) {
            for (RgESportsResultItemModel item : allResult) {
                // 赛事名
                String gameName = item.getGameName();

                // 根据type过滤想要的联赛
                if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                    if (!gameName.equals(RGConstant.GAME_NAME_LOL)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                    if (!gameName.equals(RGConstant.GAME_NAME_DOTA2)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                    if (!gameName.equals(RGConstant.GAME_NAME_CSGO)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 比如王者荣耀, 暂不需要
                    continue;
                }

                // 赛事信息获取
                String leagueName = item.getTournamentName().trim();
                String leagueId = Dictionary.ESPORT_RG_LEAGUE_MAPPING.get(leagueName);

                // 队伍信息
                String homeTeamName = null;
                String guestTeamName = null;
                if (!CollectionUtils.isEmpty(item.getTeam())) {
                    // 主队
                    RgESportsResultTeamModel homeTeam = item.getTeam().get(1);
                    homeTeamName = homeTeam.getTeamName();

                    // 客队
                    RgESportsResultTeamModel guestTeam = item.getTeam().get(0);
                    guestTeamName = guestTeam.getTeamName();
                }
                if (homeTeamName==null || guestTeamName==null) {
                    continue;
                }
                homeTeamName = homeTeamName.trim();
                guestTeamName = guestTeamName.trim();

                if (leagueId == null) {
                    // 联赛未录入
                    HSSFRow homeRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(homeRow, 0, leagueName, style);
                    setExcelCellValue(homeRow, 1, homeTeamName, style);
                    startRowIndex++;

                    HSSFRow guestRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(guestRow, 0, leagueName, style);
                    setExcelCellValue(guestRow, 1, guestTeamName, style);
                    startRowIndex++;
                } else {
                    // 联赛已录入
                    String homeTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                    String guestTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                    String leagueCellValue = leagueName + "(" + leagueId + ")";
                    if (homeTeamId == null) {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                        setExcelCellValue(homeRow, 1, homeTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                        setExcelCellValue(homeRow, 1, homeTeamName, null);
                        startRowIndex++;
                    }

                    if (guestTeamId == null) {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                        setExcelCellValue(guestRow, 1, guestTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                        setExcelCellValue(guestRow, 1, guestTeamName, null);
                        startRowIndex++;
                    }
                }
            }
        }

        return startRowIndex+1;
    }

    /**
     * TF
     */
    public int tfESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("TF电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 2;

        List list = new ArrayList();

        Integer gameId = null;
        if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_LOL;
        } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_DOTA2;
        } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_CSGO;
        }

        if (gameId != null) {
            // 今日
            String url = String.format(TFConstant.TF_TODAY_URL, gameId);
            List tfESportsResultModels = HttpClientUtils.get(url, List.class, AUTHORIZATION);
            if (!CollectionUtils.isEmpty(tfESportsResultModels)) {
                list.addAll(tfESportsResultModels);
            }

            // 早盘
            String url2 = String.format(TFConstant.TF_ZP_URL, gameId, TimeUtils.getNextDay());
            List tfESportsResultModels2 = HttpClientUtils.get(url2, List.class, AUTHORIZATION);
            if (!CollectionUtils.isEmpty(tfESportsResultModels2)) {
                list.addAll(tfESportsResultModels2);
            }
        }

        if (!CollectionUtils.isEmpty(list)) {
            for (Object object : list) {
                TfESportsResultModel tfESportsResultModel = JSON.parseObject(JSON.toJSONString(object), TfESportsResultModel.class);

                // 联赛名
                String leagueName = tfESportsResultModel.getCompetitionName().trim();
                String leagueId = Dictionary.ESPORT_TF_LEAGUE_MAPPING.get(leagueName);

                // 主客队信息
                String homeTeamName = null;
                String guestTeamName = null;
                if (tfESportsResultModel.getHome() != null) {
                    homeTeamName = tfESportsResultModel.getHome().getTeamName();
                }
                if (tfESportsResultModel.getAway() != null) {
                    guestTeamName = tfESportsResultModel.getAway().getTeamName();
                }
                if (homeTeamName==null || guestTeamName==null) {
                    continue;
                }

                homeTeamName = homeTeamName.trim();
                guestTeamName = guestTeamName.trim();

                if (leagueId == null) {
                    // 联赛未录入
                    HSSFRow homeRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(homeRow, 0, leagueName, style);
                    setExcelCellValue(homeRow, 1, homeTeamName, style);
                    startRowIndex++;

                    HSSFRow guestRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(guestRow, 0, leagueName, style);
                    setExcelCellValue(guestRow, 1, guestTeamName, style);
                    startRowIndex++;
                } else {
                    // 联赛已录入
                    String homeTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                    String guestTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                    String leagueCellValue = leagueName + "(" + leagueId + ")";
                    if (homeTeamId == null) {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                        setExcelCellValue(homeRow, 1, homeTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueCellValue, null);
                        setExcelCellValue(homeRow, 1, homeTeamName, null);
                        startRowIndex++;
                    }

                    if (guestTeamId == null) {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                        setExcelCellValue(guestRow, 1, guestTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueCellValue, null);
                        setExcelCellValue(guestRow, 1, guestTeamName, null);
                        startRowIndex++;
                    }
                }
            }
        }

        return startRowIndex+1;
    }

    /**
     * 判断是不是（击杀数）盘
     * @param teamName
     * @return
     */
    private boolean isKill(String teamName) {
        return teamName.endsWith(PBConstant.TEAM_NAME_KILL_SUFFIX);
    }

    /**
     * 获取请求body
     */
    private JSONObject getBaseBody(Integer sportId, String parentMatchNo) {
        JSONObject body = new JSONObject();
        body.put("WalletMode", 2);
        body.put("WalletBalanceDisplayEnabled", true);
        body.put("WalletBalanceRefreshInterval", 60);
        body.put("OddsType", 2);
        body.put("Language", 1);
        body.put("ShowStatistics", 0);
        body.put("ExtraFilter", "48");
        body.put("CompanyId", "1582");
        body.put("AcceptAnyOdds", false);
        body.put("AcceptHigherOdds", true);
        body.put("SeasonId", 0);
        body.put("VIPSpread", 1);
        body.put("Playsite", false);
        body.put("SportId", -1);    // 请求具体比赛时,需要替换 ("SportId": "45")
        if (sportId != null) {
            body.put("SportId", sportId);
        }
        body.put("Market", 0);
        body.put("OddsPageCode", 0);
        body.put("ShowStatsLeftFloatMenu", false);
        body.put("ShowMatchResults", true);
        body.put("ShowTeamLeftFloatMenu", true);
        body.put("ShowTermsLeftFloatMenu", true);
        body.put("ShowAnnouncementLeftFloatMenu", true);
        body.put("ShowCMS", false);
        body.put("showAnnouncement", true);
        body.put("TranslationCode", "REAL");
        body.put("IsMultipleCurrency", false);
        body.put("QueryToken", "");
        body.put("LiveStream", 1);
        body.put("MobileMerchantHomeUrl", null);
        body.put("IndexMatchesReloadIntervalSeconds", 20);
        body.put("LiveBallsReloadIntervalSeconds", 7);
        body.put("SmvReloadIntervalSeconds", 10);
        body.put("ParlayViewReloadIntervalSeconds", 10);
        body.put("NoSponsorAdsInVideo", false);
        body.put("FilterSportId", -1);
        body.put("PageSportIds", Collections.singletonList(sportId));
        body.put("PageMarket", 3);
        body.put("ViewType", 2);
        body.put("MemberCode", "");
        if (parentMatchNo != null) {
            body.put("ParentMatchNo", null);    // 请求具体比赛时,需要替换 ("ParentMatchNo": "9793752")
        }
        body.put("ParentMatchNo", parentMatchNo);
        body.put("MatchIdList", null);
        return body;
    }

    /**
     * 设置单元格值
     * @param row
     * @param column
     * @param value
     * @param useStyle
     */
    private void setExcelCellValue(HSSFRow row, int column, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(column);
        cell.setCellType(CellType.STRING);
        if (style != null) {
            cell.setCellStyle(style);
        }
        cell.setCellValue(value);
    }
}
