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
    private static final String SPORT_MAPPING_SUPPORT_FILE_PATH = "D:/Crawler/support/sport_mapping_support.xls";

    /**
     * 爬取比赛映射 - 电竞
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

            // 平博电竞
            int rowIndex = 1;
            HSSFSheet pbSheet = workbook.getSheetAt(0);
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
            HSSFSheet rgSheet = workbook.getSheetAt(1);
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
            HSSFSheet tfSheet = workbook.getSheetAt(2);
            // LOL
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = tfESport(tfSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

            // IM电竞
            rowIndex = 1;
            HSSFSheet imSheet = workbook.getSheetAt(3);
            // LOL
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_LOL, rowIndex, style);
            // DOTA2
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_DOTA2, rowIndex, style);
            // CSGO
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_CSGO, rowIndex, style);
            // 王者荣耀
            rowIndex = imESport(imSheet, Constant.ESPORTS_TYPE_KPL, rowIndex, style);

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

    /* ====================== 平博电竞 - start  ====================== */
    private int pbESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("平博电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        List<List<Object>> allLeagues = new ArrayList<>();

        // 今天
        String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
        Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_PB));
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
        Map<String, Object> map2 = HttpClientUtils.get(url2, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_PB));
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

                String leagueId = Dictionary.ESPORT_PB_LEAGUE_MAPPING.get(type).get(leagueName);

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
                            matchHomeTeamName = replaceKill(homeTeamName);
                            matchGuestTeamName = replaceKill(guestTeamName);
                        } else {
                            matchHomeTeamName = homeTeamName;
                            matchGuestTeamName = guestTeamName;
                        }

                        if (leagueId == null) {
                            // 联赛未录入
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 1, leagueName, style);
                            setExcelCellValue(homeRow, 2, matchHomeTeamName, style);
                            startRowIndex++;

                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 1, leagueName, style);
                            setExcelCellValue(guestRow, 2, matchGuestTeamName, style);
                            startRowIndex++;
                        } else {
                            // 联赛已录入
                            String homeTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchHomeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchGuestTeamName.toUpperCase());

                            if (homeTeamId == null) {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, matchHomeTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, matchHomeTeamName, null);
                                setExcelCellValue(homeRow, 3, homeTeamId, null);
                                startRowIndex++;
                            }

                            if (guestTeamId == null) {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, matchGuestTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, matchGuestTeamName, null);
                                setExcelCellValue(guestRow, 3, guestTeamId, null);
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
     * 判断是不是（击杀数）盘
     * @param teamName
     * @return
     */
    private boolean isKill(String teamName) {
        return teamName.contains(PBConstant.TEAM_NAME_KILL_SUFFIX) || teamName.contains(PBConstant.TEAM_NAME_KILL_SUFFIX_EN);
    }

    /**
     * 替换击杀数后缀
     * @param teamName
     * @return
     */
    private String replaceKill(String teamName) {
        if (teamName.contains(PBConstant.TEAM_NAME_KILL_SUFFIX)) {
            return teamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX, "").trim();
        } else if (teamName.contains(PBConstant.TEAM_NAME_KILL_SUFFIX_EN)) {
            return teamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX_EN, "").trim();
        }
        return teamName.toUpperCase();
    }
    /* ====================== 平博电竞 - end  ====================== */

    /* ====================== RG电竞 - start  ====================== */
    private int rgESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("RG电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

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
                String leagueId = Dictionary.ESPORT_RG_LEAGUE_MAPPING.get(type).get(leagueName);

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
                    setExcelCellValue(homeRow, 1, leagueName, style);
                    setExcelCellValue(homeRow, 2, homeTeamName, style);
                    startRowIndex++;

                    HSSFRow guestRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(guestRow, 1, leagueName, style);
                    setExcelCellValue(guestRow, 2, guestTeamName, style);
                    startRowIndex++;
                } else {
                    // 联赛已录入
                    String homeTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                    String guestTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                    if (homeTeamId == null) {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueId, null);
                        setExcelCellValue(homeRow, 1, leagueName, null);
                        setExcelCellValue(homeRow, 2, homeTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueId, null);
                        setExcelCellValue(homeRow, 1, leagueName, null);
                        setExcelCellValue(homeRow, 2, homeTeamName, null);
                        setExcelCellValue(homeRow, 3, homeTeamId, null);
                        startRowIndex++;
                    }

                    if (guestTeamId == null) {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueId, null);
                        setExcelCellValue(guestRow, 1, leagueName, null);
                        setExcelCellValue(guestRow, 2, guestTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueId, null);
                        setExcelCellValue(guestRow, 1, leagueName, null);
                        setExcelCellValue(guestRow, 2, guestTeamName, null);
                        setExcelCellValue(guestRow, 3, guestTeamId, null);
                        startRowIndex++;
                    }
                }
            }
        }

        return startRowIndex+1;
    }
    /* ====================== RG电竞 - end  ====================== */

    /* ====================== TF电竞 - start  ====================== */
    // 认证token
    private final static String AUTHORIZATION = "Token c4b789e82ce341ac985e44b6b4da5042";

    private int tfESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("TF电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

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
                String leagueId = Dictionary.ESPORT_TF_LEAGUE_MAPPING.get(type).get(leagueName);

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
                    setExcelCellValue(homeRow, 1, leagueName, style);
                    setExcelCellValue(homeRow, 2, homeTeamName, style);
                    startRowIndex++;

                    HSSFRow guestRow = sheet.createRow(startRowIndex);
                    setExcelCellValue(guestRow, 1, leagueName, style);
                    setExcelCellValue(guestRow, 2, guestTeamName, style);
                    startRowIndex++;
                } else {
                    // 联赛已录入
                    String homeTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                    String guestTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                    if (homeTeamId == null) {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueId, null);
                        setExcelCellValue(homeRow, 1, leagueName, null);
                        setExcelCellValue(homeRow, 2, homeTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 0, leagueId, null);
                        setExcelCellValue(homeRow, 1, leagueName, null);
                        setExcelCellValue(homeRow, 2, homeTeamName, null);
                        setExcelCellValue(homeRow, 3, homeTeamId, null);
                        startRowIndex++;
                    }

                    if (guestTeamId == null) {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueId, null);
                        setExcelCellValue(guestRow, 1, leagueName, null);
                        setExcelCellValue(guestRow, 2, guestTeamName, style);
                        startRowIndex++;
                    } else {
                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 0, leagueId, null);
                        setExcelCellValue(guestRow, 1, leagueName, null);
                        setExcelCellValue(guestRow, 2, guestTeamName, null);
                        setExcelCellValue(guestRow, 3, guestTeamId, null);
                        startRowIndex++;
                    }
                }
            }
        }

        return startRowIndex+1;
    }
    /* ====================== TF电竞 - end  ====================== */

    /* ====================== IM电竞 - start  ====================== */
    private int imESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("IM电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

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
        Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL_V1, body, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_IM));
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
                            String leagueId = Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(type).get(leagueName);

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
                                        setExcelCellValue(homeRow, 1, leagueName, style);
                                        setExcelCellValue(homeRow, 2, homeTeamName, style);
                                        startRowIndex++;

                                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(guestRow, 1, leagueName, style);
                                        setExcelCellValue(guestRow, 2, guestTeamName, style);
                                        startRowIndex++;
                                    } else {
                                        // 联赛已录入
                                        String homeTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                                        String guestTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                                        if (homeTeamId == null) {
                                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(homeRow, 0, leagueId, null);
                                            setExcelCellValue(homeRow, 1, leagueName, null);
                                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                                            startRowIndex++;
                                        } else {
                                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(homeRow, 0, leagueId, null);
                                            setExcelCellValue(homeRow, 1, leagueName, null);
                                            setExcelCellValue(homeRow, 2, homeTeamName, null);
                                            setExcelCellValue(homeRow, 3, homeTeamId, null);
                                            startRowIndex++;
                                        }

                                        if (guestTeamId == null) {
                                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(guestRow, 0, leagueId, null);
                                            setExcelCellValue(guestRow, 1, leagueName, null);
                                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                                            startRowIndex++;
                                        } else {
                                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(guestRow, 0, leagueId, null);
                                            setExcelCellValue(guestRow, 1, leagueName, null);
                                            setExcelCellValue(guestRow, 2, guestTeamName, null);
                                            setExcelCellValue(guestRow, 3, guestTeamId, null);
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
    /* ====================== IM电竞 - end  ====================== */

    /* ====================== 泛亚电竞 - start  ====================== */
    private int fyESport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("泛亚电竞_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
        Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
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
                        String leagueId = Dictionary.ESPORT_FY_LEAGUE_MAPPING.get(type).get(leagueName);


                        // 主客队信息
                        String homeTeamName = ((String) match.get("HomeName"));
                        String guestTeamName = ((String) match.get("AwayName"));
                        homeTeamName = homeTeamName.trim();
                        guestTeamName = guestTeamName.trim();

                        if (leagueId == null) {
                            // 联赛未录入
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 1, leagueName, style);
                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                            startRowIndex++;

                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 1, leagueName, style);
                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                            startRowIndex++;
                        } else {
                            String homeTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                            if (homeTeamId == null) {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, null);
                                setExcelCellValue(homeRow, 3, homeTeamId, null);
                                startRowIndex++;
                            }

                            if (guestTeamId == null) {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, null);
                                setExcelCellValue(guestRow, 3, guestTeamId, null);
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
    /* ====================== 泛亚电竞 - end  ====================== */

    /**
     * 爬取比赛映射 - 体育
     */
    public void mappingSupport4Sport() {
        OutputStream out = null;
        File excelFile = new File(SPORT_MAPPING_SUPPORT_FILE_PATH);
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            // 打开工作表
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            HSSFCellStyle style = workbook.createCellStyle();
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
            style.setFillForegroundColor(HSSFColor.RED.index);    //填红色

            // 平博体育
            int rowIndex = 1;
            HSSFSheet pbSheet = workbook.getSheetAt(0);
            // 足球
            rowIndex = pbSport(pbSheet, Constant.SPORTS_TYPE_SOCCER, rowIndex, style);
            // 篮球
            rowIndex = pbSport(pbSheet, Constant.SPORTS_TYPE_BASKETBALL, rowIndex, style);

            // 188体育
            rowIndex = 1;
            HSSFSheet ybbSheet = workbook.getSheetAt(1);
            // 足球
            rowIndex = ybbSport(ybbSheet, Constant.SPORTS_TYPE_SOCCER, rowIndex, style);
            // 篮球
            rowIndex = ybbSport(ybbSheet, Constant.SPORTS_TYPE_BASKETBALL, rowIndex, style);

            // im体育
            rowIndex = 1;
            HSSFSheet imSheet = workbook.getSheetAt(2);
            // 足球
            rowIndex = imSport(imSheet, Constant.SPORTS_TYPE_SOCCER, rowIndex, style);
            // 篮球
            rowIndex = imSport(imSheet, Constant.SPORTS_TYPE_BASKETBALL, rowIndex, style);

            // BTI体育
            rowIndex = 1;
            HSSFSheet btiSheet = workbook.getSheetAt(3);
            // 足球
            rowIndex = btiSport(btiSheet, Constant.SPORTS_TYPE_SOCCER, rowIndex, style);
            // 篮球
            rowIndex = btiSport(btiSheet, Constant.SPORTS_TYPE_BASKETBALL, rowIndex, style);

            out = new FileOutputStream(SPORT_MAPPING_SUPPORT_FILE_PATH);
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

    /* ====================== 平博体育 - start  ====================== */
    private int pbSport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("平博体育_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        List<List<Object>> allLeagues = new ArrayList<>();

        String sp = null;
        if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_BASKETBALL)) {
            sp = PBConstant.SP_BASKETBALL;
        } else if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_SOCCER)) {
            sp = PBConstant.SP_SOCCER;
        }
        if (sp == null) {
            return startRowIndex+1;
        }
        // 今天
        String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, sp, "", System.currentTimeMillis());
        Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_PB));
        if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
            List<Object> n = (List<Object>) map.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                List<Object> sports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(sports)) {
                    // 联赛列表
                    List<List<Object>> leagues = (List<List<Object>>) sports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        allLeagues.addAll(leagues);
                    }
                }
            }
        }
        // 早盘
        String zpUrl = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_ZP, sp, TimeUtils.getDate(), System.currentTimeMillis());
        Map<String, Object> zpMap = HttpClientUtils.get(zpUrl, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_PB));
        if (zpMap != null && zpMap.get("n") != null && !CollectionUtils.isEmpty((List<Object>) zpMap.get("n"))) {
            List<Object> n = (List<Object>) zpMap.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                List<Object> sports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(sports)) {
                    // 联赛列表
                    List<List<Object>> leagues = (List<List<Object>>) sports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        allLeagues.addAll(leagues);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(allLeagues)) {
            // 遍历联赛列表
            for (List<Object> league : allLeagues) {
                // 联赛名
                String leagueName = ((String) league.get(1)).trim();

                // 忽略角球和红黄牌
                if (leagueName.contains(PBConstant.LEAGUE_NAME_IGNORE_JQ) || leagueName.contains(PBConstant.LEAGUE_NAME_IGNORE_HHP)) {
                    continue;
                }

                String leagueId = Dictionary.SPORT_PB_LEAGUE_MAPPING.get(type).get(leagueName);

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

                        if (leagueId == null) {
                            // 联赛未录入
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 1, leagueName, style);
                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                            startRowIndex++;

                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 1, leagueName, style);
                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                            startRowIndex++;
                        } else {
                            // 联赛已录入
                            String homeTeamId = Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                            if (homeTeamId == null) {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, null);
                                setExcelCellValue(homeRow, 3, homeTeamId, null);
                                startRowIndex++;
                            }

                            if (guestTeamId == null) {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, null);
                                setExcelCellValue(guestRow, 3, guestTeamId, null);
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
    /* ====================== 平博体育 - end  ====================== */

    /* ====================== IM体育 - start  ====================== */
    private int imSport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("IM体育_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        Integer sportId = null;
        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_SOCCER;
        } else if (Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_BASKETBALL;
        }

        if (sportId != null) {
            List<Map<String, Object>> allSel = new ArrayList<>();

            // 今日
            JSONObject todayBody = getBaseBody(sportId, IMConstant.MARKET_TODAY, null, null);
            Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_SPORT_BASE_URL, todayBody, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_IM));
            if (map != null && map.get("sel") != null) {
                List<Map<String, Object>> sels = (List<Map<String, Object>>) map.get("sel");
                if (!CollectionUtils.isEmpty(sels)) {
                    allSel.addAll(sels);
                }
            }

            // 早盘
            String zpDate = TimeUtils.getNextDay(TimeUtils.TIME_FORMAT_3);
            JSONObject zpBody = getBaseBody(sportId, IMConstant.MARKET_ZP, zpDate, zpDate);
            Map<String, Object> zpMap = HttpClientUtils.post(IMConstant.IM_SPORT_BASE_URL, zpBody, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_IM));
            if (zpMap != null && zpMap.get("sel") != null) {
                List<Map<String, Object>> sels = (List<Map<String, Object>>) zpMap.get("sel");
                if (!CollectionUtils.isEmpty(sels)) {
                    allSel.addAll(sels);
                }
            }

            if (!CollectionUtils.isEmpty(allSel)) {
                for (Map<String, Object> sel : allSel) {
                    // 联赛名
                    String leagueName = ((String) sel.get("cn")).trim();

                    // 忽略电竞足球
                    if (leagueName.startsWith(IMConstant.LEAGUE_NAME_IGNORE_DZZQ)) {
                        continue;
                    }

                    // 赛事信息获取
                    String leagueId = Dictionary.SPORT_IM_LEAGUE_MAPPING.get(type).get(leagueName);

                    // 队伍名
                    String homeTeamName = ((String) sel.get("htn")).trim();
                    String guestTeamName = ((String) sel.get("atn")).trim();

                    if (leagueId == null) {
                        // 联赛未录入
                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(homeRow, 1, leagueName, style);
                        setExcelCellValue(homeRow, 2, homeTeamName, style);
                        startRowIndex++;

                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                        setExcelCellValue(guestRow, 1, leagueName, style);
                        setExcelCellValue(guestRow, 2, guestTeamName, style);
                        startRowIndex++;
                    } else {
                        // 联赛已录入
                        String homeTeamId = Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                        String guestTeamId = Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                        if (homeTeamId == null) {
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 0, leagueId, null);
                            setExcelCellValue(homeRow, 1, leagueName, null);
                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                            startRowIndex++;
                        } else {
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 0, leagueId, null);
                            setExcelCellValue(homeRow, 1, leagueName, null);
                            setExcelCellValue(homeRow, 2, homeTeamName, null);
                            setExcelCellValue(homeRow, 3, homeTeamId, null);
                            startRowIndex++;
                        }

                        if (guestTeamId == null) {
                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 0, leagueId, null);
                            setExcelCellValue(guestRow, 1, leagueName, null);
                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                            startRowIndex++;
                        } else {
                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 0, leagueId, null);
                            setExcelCellValue(guestRow, 1, leagueName, null);
                            setExcelCellValue(guestRow, 2, guestTeamName, null);
                            setExcelCellValue(guestRow, 3, guestTeamId, null);
                            startRowIndex++;
                        }
                    }

                    startRowIndex++;
                }
            }
        }

        return startRowIndex + 1;
    }

    /**
     * 获取请求Body
     */
    private JSONObject getBaseBody(Integer sportId, Integer market, String dateFrom, String dateTo) {
        JSONObject body = new JSONObject();
        body.put("BetTypeIds", new Integer[] {1, 2, 3});
        body.put("DateFrom", null);        body.put("DateTo", null);
        if (!StringUtils.isEmpty(dateFrom) && !StringUtils.isEmpty(dateTo)) {
            body.put("DateFrom", dateFrom);
            body.put("DateTo", dateTo);
        }
        body.put("IsCombo", false);
        body.put("Market", market);
        body.put("MatchDay", 0);
        body.put("OddsType", 3);
        body.put("PeriodIds", new Integer[] {1, 2});
        body.put("Season", 0);
        body.put("SortType", 1);
        body.put("SportId", sportId);
        return body;
    }
    /* ====================== Im体育 - end  ====================== */

    /* ====================== 188体育 - start  ====================== */
    private int ybbSport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("188体育_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        List<Map<String, Object>> allLagues = new ArrayList<>();

        // 今日
        String url = String.format(YBBConstant.YBB_BASE_URL, System.currentTimeMillis());
        Map<String, Object> map = HttpClientUtils.postFrom(url, getFormData(true, type), Map.class);
        if (map != null && map.get("mod") != null) {
            Map<String, Object> mod = (Map<String, Object>) map.get("mod");
            if (!CollectionUtils.isEmpty(mod)) {
                List<Map<String, Object>> d = (List<Map<String, Object>>) mod.get("d");
                if (!CollectionUtils.isEmpty(d)) {
                    Map<String, Object> cMap = d.get(0);
                    if (!CollectionUtils.isEmpty(cMap)) {
                        // 联赛列表
                        List<Map<String, Object>> leagues = (List<Map<String, Object>>) cMap.get("c");
                        if (!CollectionUtils.isEmpty(leagues)) {
                            allLagues.addAll(leagues);
                        }
                    }
                }
            }
        }

        // 早盘
        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            String zpUrl = String.format(YBBConstant.YBB_BASE_URL, System.currentTimeMillis());
            Map<String, Object> zpMap = HttpClientUtils.postFrom(zpUrl, getFormData(false, type), Map.class);
            if (zpMap != null && zpMap.get("mod") != null) {
                Map<String, Object> mod = (Map<String, Object>) zpMap.get("mod");
                if (!CollectionUtils.isEmpty(mod)) {
                    List<Map<String, Object>> d = (List<Map<String, Object>>) mod.get("d");
                    if (!CollectionUtils.isEmpty(d)) {
                        Map<String, Object> cMap = d.get(0);
                        if (!CollectionUtils.isEmpty(cMap)) {
                            // 联赛列表
                            List<Map<String, Object>> leagues = (List<Map<String, Object>>) cMap.get("c");
                            if (!CollectionUtils.isEmpty(leagues)) {
                                allLagues.addAll(leagues);
                            }
                        }
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(allLagues)) {
            for (Map<String, Object> league : allLagues) {
                // 联赛名
                String leagueName = (String) league.get("n");

                // 忽略电子足球和特别投注
                if (leagueName.startsWith(YBBConstant.LEAGUE_NAME_IGNORE_DZZQ) || leagueName.endsWith(YBBConstant.LEAGUE_NAME_IGNORE_TBTZ)) {
                    continue;
                }

                String leagueId = Dictionary.SPORT_YB_LEAGUE_MAPPING.get(type).get(leagueName);

                List<Map<String, Object>> games = (List<Map<String, Object>>) league.get("e");
                if (!CollectionUtils.isEmpty(games)) {
                    for (Map<String, Object> game : games) {
                        // 判断是不是角球/罚牌数等，需要跳过
                        Map<String, Object> cei = (Map<String, Object>) game.get("cei");
                        String n = (String) cei.get("n");
                        if (!"".equals(n)) {
                            continue;
                        }

                        // 队伍信息
                        List<String> teamInfo = (List<String>) game.get("i");
                        if (CollectionUtils.isEmpty(teamInfo) || teamInfo.size() < 2) {
                            continue;
                        }
                        // home team name
                        String homeTeamName = (String) teamInfo.get(0);
                        homeTeamName = homeTeamName.trim();
                        // guest team name
                        String guestTeamName = (String) teamInfo.get(1);
                        guestTeamName = guestTeamName.trim();

                        if (leagueId == null) {
                            // 联赛未录入
                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(homeRow, 1, leagueName, style);
                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                            startRowIndex++;

                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                            setExcelCellValue(guestRow, 1, leagueName, style);
                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                            startRowIndex++;
                        } else {
                            // 联赛已录入
                            String homeTeamId = Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                            String guestTeamId = Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                            if (homeTeamId == null) {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow homeRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(homeRow, 0, leagueId, null);
                                setExcelCellValue(homeRow, 1, leagueName, null);
                                setExcelCellValue(homeRow, 2, homeTeamName, null);
                                setExcelCellValue(homeRow, 3, homeTeamId, null);
                                startRowIndex++;
                            }

                            if (guestTeamId == null) {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, style);
                                startRowIndex++;
                            } else {
                                HSSFRow guestRow = sheet.createRow(startRowIndex);
                                setExcelCellValue(guestRow, 0, leagueId, null);
                                setExcelCellValue(guestRow, 1, leagueName, null);
                                setExcelCellValue(guestRow, 2, guestTeamName, null);
                                setExcelCellValue(guestRow, 3, guestTeamId, null);
                                startRowIndex++;
                            }
                        }
                    }
                }

                startRowIndex++;
            }
        }

        return startRowIndex + 1;
    }

    /**
     * 请求参数
     */
    private Map<String, Object> getFormData(boolean isToday, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("IsFirstLoad", true);
        params.put("VersionL", -1);
        params.put("VersionU", 0);
        params.put("VersionS", -1);
        params.put("VersionF", -1);
        params.put("VersionH", 0);
        params.put("VersionT", -1);
        params.put("IsEventMenu", false);
        params.put("SportID", 1);
        params.put("CompetitionID", -1);
        params.put("oIsInplayAll", false);
        params.put("oIsFirstLoad", true);
        params.put("oSortBy", 1);
        params.put("oOddsType", 0);
        params.put("oPageNo", 0);
        params.put("LiveCenterEventId", 0);
        params.put("LiveCenterSportId", 0);

        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            // 足球
            if (isToday) {
                params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
                params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under?q=&country=CN&currency=RMB&tzoff=-240&reg=China&rc=CN&allowRacing=false");
            } else {
                params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/tomorrow/full-time-asian-handicap-and-over-under");
                params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
            }
        } else {
            // 篮球
            params.put("reqUrl", "/zh-cn/sports/basketball/competition/full-time-asian-handicap-and-over-under");
            params.put("hisUrl", "/zh-cn/sports/basketball/competition/full-time-asian-handicap-and-over-under?q=&country=CN&currency=RMB&tzoff=-240&reg=China&rc=CN&allowRacing=false");
        }

        return params;
    }
    /* ====================== 188体育 - end  ====================== */

    /* ====================== BTI体育 - start  ====================== */
    private int btiSport(HSSFSheet sheet, String type, int startRowIndex, HSSFCellStyle style) {
        log.info("BTI体育_" + type);

        // 设置类型
        HSSFRow typeRow = sheet.createRow(startRowIndex);
        setExcelCellValue(typeRow, 0, type, style);
        startRowIndex += 4;

        Integer branchId = null;
        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            branchId = BTIConstant.BRANCH_ID_SOCCER;
        } else if (Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(type)) {
            branchId = BTIConstant.BRANCH_ID_BASKETBALL;
        }

        if (branchId != null) {
            List<String> urls = new ArrayList<>();
            urls.add(BTIConstant.BTI_TODAY_URL);
            urls.add(BTIConstant.BTI_EARLY_URL);

            for (String baseUrl : urls) {
                String url = String.format(baseUrl, branchId);
                List list = HttpClientUtils.get(url, List.class, getRequestHeaders(), null, false);

                if (!CollectionUtils.isEmpty(list)) {
                    // 【1】是联赛列表
                    List<List<Object>> leagueList = (List<List<Object>>) list.get(1);
                    if (!CollectionUtils.isEmpty(leagueList)) {
                        // 联赛信息
                        Map<Integer, String> leagueMap = new HashMap<>();
                        for (List<Object> leagueInfo : leagueList) {
                            if (!CollectionUtils.isEmpty(leagueInfo)) {
                                Integer leagueId = (Integer) leagueInfo.get(0);
                                String leagueName = (String) leagueInfo.get(1);
                                leagueName = leagueName.trim();

                                if (leagueName.startsWith(BTIConstant.LEAGUE_NAME_IGNORE_FIFA)) {
                                    continue;
                                }

                                leagueMap.put(leagueId, leagueName);
                            }
                        }

                        if (!CollectionUtils.isEmpty(leagueMap)) {
                            // 【2】是比赛列表
                            List<List<Object>> gameList = (List<List<Object>>) list.get(2);
                            if (!CollectionUtils.isEmpty(gameList)) {
                                for (List<Object> gameInfo : gameList) {
                                    // 【14】联赛ID（盘口内的）
                                    Integer dishLeagueId = (Integer) gameInfo.get(9);
                                    if (!leagueMap.containsKey(dishLeagueId)) {
                                        continue;
                                    }
                                    String leagueName = leagueMap.get(dishLeagueId);

                                    // 赛事信息获取
                                    String leagueId = Dictionary.SPORT_BTI_LEAGUE_MAPPING.get(type).get(leagueName);

                                    // 【1】主队名
                                    String homeTeamName = (String) gameInfo.get(1);
                                    homeTeamName = homeTeamName.trim();
                                    // 【2】客队名
                                    String guestTeamName = (String) gameInfo.get(2);
                                    guestTeamName = guestTeamName.trim();

                                    if (leagueId == null) {
                                        // 联赛未录入
                                        HSSFRow homeRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(homeRow, 1, leagueName, style);
                                        setExcelCellValue(homeRow, 2, homeTeamName, style);
                                        startRowIndex++;

                                        HSSFRow guestRow = sheet.createRow(startRowIndex);
                                        setExcelCellValue(guestRow, 1, leagueName, style);
                                        setExcelCellValue(guestRow, 2, guestTeamName, style);
                                        startRowIndex++;
                                    } else {
                                        // 联赛已录入
                                        String homeTeamId = Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                                        String guestTeamId = Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());

                                        if (homeTeamId == null) {
                                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(homeRow, 0, leagueId, null);
                                            setExcelCellValue(homeRow, 1, leagueName, null);
                                            setExcelCellValue(homeRow, 2, homeTeamName, style);
                                            startRowIndex++;
                                        } else {
                                            HSSFRow homeRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(homeRow, 0, leagueId, null);
                                            setExcelCellValue(homeRow, 1, leagueName, null);
                                            setExcelCellValue(homeRow, 2, homeTeamName, null);
                                            setExcelCellValue(homeRow, 3, homeTeamId, null);
                                            startRowIndex++;
                                        }

                                        if (guestTeamId == null) {
                                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(guestRow, 0, leagueId, null);
                                            setExcelCellValue(guestRow, 1, leagueName, null);
                                            setExcelCellValue(guestRow, 2, guestTeamName, style);
                                            startRowIndex++;
                                        } else {
                                            HSSFRow guestRow = sheet.createRow(startRowIndex);
                                            setExcelCellValue(guestRow, 0, leagueId, null);
                                            setExcelCellValue(guestRow, 1, leagueName, null);
                                            setExcelCellValue(guestRow, 2, guestTeamName, null);
                                            setExcelCellValue(guestRow, 3, guestTeamId, null);
                                            startRowIndex++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                startRowIndex++;
            }
        }

        return startRowIndex + 1;
    }

    /**
     * 获取请求头
     * @return
     */
    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("RequestTarget", "AJAXService");
        return headers;
    }
    /* ====================== BTI体育 - end  ====================== */

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
