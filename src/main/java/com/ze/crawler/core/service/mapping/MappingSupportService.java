package com.ze.crawler.core.service.mapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.Dictionary;
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
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

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
            // 王者荣耀
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

            // 平博电竞
            rowIndex = 1;
            HSSFSheet pbSheet = workbook.getSheetAt(1);
            // LOL
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = pbESport(pbSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

            // RG电竞
            rowIndex = 1;
            HSSFSheet rgSheet = workbook.getSheetAt(2);
            // LOL
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = rgESport(rgSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

            // TF电竞
            rowIndex = 1;
            HSSFSheet tfSheet = workbook.getSheetAt(3);
            // LOL
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

            // 泛亚电竞
            rowIndex = 1;
            HSSFSheet fySheet = workbook.getSheetAt(4);
            // LOL
            rowIndex = fyESport(fySheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = fyESport(fySheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = fyESport(fySheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = fyESport(fySheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

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

        Integer sportId = null;
        if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_LOL;
        } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_DOTA2;
        } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_CSGO;
        } else if (Constant.ESPORTS_TYPE_KPL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_KPL;
        }

        if (sportId == null) {
            return startRowIndex+1;
        }

        JSONObject body = getBaseBody(sportId);
        Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL_V1, body, Map.class);
        if (map != null && map.get("Sport") != null) {
            List<Map<String, Object>> sports = (List<Map<String, Object>>) map.get("Sport");
            if (!CollectionUtils.isEmpty(sports)) {
                for (Map<String, Object> sport : sports) {
                    Integer returnSportId = (Integer) sport.get("SportId");
                    if (returnSportId != sportId) {
                        continue;
                    }

                    List<Map<String, Object>> leagues = (List<Map<String, Object>>) sport.get("LG");
                    if (!CollectionUtils.isEmpty(leagues)) {
                        for (Map<String, Object> league : leagues) {
                            // 联赛名
                            String leagueName = (String) league.get("BaseLGName");
                            leagueName = leagueName.trim();
                            String leagueId = Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(leagueName);

                            List<Map<String, Object>> games = (List<Map<String, Object>>) league.get("ParentMatch");
                            if (!CollectionUtils.isEmpty(games)) {
                                for (Map<String, Object> game : games) {
                                    // 主队
                                    String homeTeamName = (String) game.get("PHTName");
                                    homeTeamName = homeTeamName.trim();
                                    // 客队
                                    String guestTeamName = (String) game.get("PATName");
                                    guestTeamName = guestTeamName.trim();
                                    if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                                        continue;
                                    }

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
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_KPL)) {
                    if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_KPL)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 暂不需要
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
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_KPL)) {
                    if (!gameName.equals(RGConstant.GAME_NAME_KPL)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 暂不需要
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
        } else if (Constant.ESPORTS_TYPE_KPL.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_KPL;
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
     * 泛亚
     */
    public int fyESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("泛亚电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 2;

        Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
        Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.USE_PROXY);
        if (!CollectionUtils.isEmpty(map)) {
            Map<String, Object> info = (Map<String, Object>) map.get("info");
            if (!CollectionUtils.isEmpty(info) && info.containsKey("Match")) {
                List<Map<String, Object>> matchList = (List<Map<String, Object>>) info.get("Match");
                if (!CollectionUtils.isEmpty(matchList)) {
                    for (Map<String, Object> match : matchList) {
                        // 比赛名
                        String gameName = (String) match.get("GameName");
                        // 根据type过滤想要的联赛
                        if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                            if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_LOL)) {
                                continue;
                            }
                        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                            if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_DOTA2)) {
                                continue;
                            }
                        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                            if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_CSGO)) {
                                continue;
                            }
                        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_KPL)) {
                            if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_KPL)) {
                                continue;
                            }
                        } else {
                            // 其余赛事, 暂不需要
                            continue;
                        }

                        // 联赛名
                        String leagueName = (String) match.get("LeagueName");
                        leagueName = leagueName.trim();
                        String leagueId = Dictionary.ESPORT_FY_LEAGUE_MAPPING.get(leagueName);


                        // 主客队信息
                        String homeTeamName = ((String) match.get("HomeName"));
                        String guestTeamName = ((String) match.get("AwayName"));
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
                            String homeTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

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
            }
        }

        return startRowIndex+1;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(String path) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("ghost", "60e1601dc3964090ac33e4d55ffe0bbe");
        headers.put("origin", "https://jingjib.aabv.top");
        headers.put("path", path);
        headers.put("referer", "https://jingjib.aabv.top/index.html?v=1.2.22");
        headers.put("x-forwarded-host", "jingjib.aabv.top");

        return headers;
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
    private JSONObject getBaseBody(Integer sportId) {
        JSONObject body = new JSONObject();
        body.put("BettingChannel", 1);
        body.put("EventMarket", -99);
        body.put("Language", "chs");
        body.put("Token", null);
        body.put("BaseLGIds", Collections.singletonList(-99));
        body.put("SportId", sportId);
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
