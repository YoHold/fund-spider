package com.yochiu.fund.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yochiu.fund.entity.StockBoard;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * @Author: yochiu
 * @Description: 核心题材
 * @Date: 2020/10/13
 */
public class StockBoardSpider {

    private static final String STOCK_BOARD_API = "http://f10.eastmoney.com/CoreConception/CoreConceptionAjax";

    private static Set<String> FILTER_BOARD = Sets.newHashSet("融资融券", "富时概念", "深股通", "AH股",
            "标普概念", "基金重仓", "深成500", "MSCI中国", "证金持股", "沪股通", "HS300_", "创业板综", "山东板块", "雄安新区", "上证50_", "国企改革", "长江三角",
            "中证500", "MSCI大盘", "广东板块", "MSCI中盘", "QFII重仓", "创业成份", "机构重仓", "江苏板块", "工业4.0", "四川板块", "上证180_",
            "深证100R", "上证180", "上证380", "股权激励", "次新股", "央视50_", "北京板块", "浙江板块", "深圳特区", "转债标的");

    /**
     * 获取股票所属板块跟业务介绍
     * 解析数据结构如下:
     * {
     *     "hxtc":[
     *         {
     *             "zqnm":"--",
     *             "zqdm":"002013.SZ",
     *             "zqjc":"--",
     *             "jyscbm":"--",
     *             "gjc":"所属板块",
     *             "yd":"1",
     *             "ydnr":"MSCI中盘 标普概念 大飞机 富时概念 国企改革 航母概念 航天航空 湖北板块 湖北自贸 军工 融资融券 深成500 深股通 无人机 证金持股 中证500"
     *         },
     *         {
     *             "zqnm":"--",
     *             "zqdm":"002013.SZ",
     *             "zqjc":"--",
     *             "jyscbm":"--",
     *             "gjc":"经营范围",
     *             "yd":"2",
     *             "ydnr":"主营:为各类飞行器、发动机配套的机载机电系统及设备的研制、生产、销售和服务,并为航天、兵器、船舶、电子信息等防务领域提供相应配套产品及服务。兼营:车船载系统、各类精冲制品及精密冲压模具、工业自动化与控制设备、机电设备及系统、电动车、制冷系统、信息系统及产品的研发、生产、销售及服务。经营本企业自产产品及技术的出口业务;经营本企业生产所需的原辅材料、仪器仪表、机械设备、零配件及技术的进口业务(国家限定公司经营和国家禁止进出口的商品及技术除外);经营进料加工和“三来一补”业务。"
     *         },
     *         {
     *             "zqnm":"--",
     *             "zqdm":"002013.SZ",
     *             "zqjc":"--",
     *             "jyscbm":"--",
     *             "gjc":"航空机电系统",
     *             "yd":"3",
     *             "ydnr":"公司是航空工业旗下航空机电系统的专业化整合和产业化发展平台,承担航空机电产品的市场开拓、设计研发、生产制造、售后服务、维修保障的全价值链管理,为航空装备提供大专业配套系统产品,在国内航空机电领域处于主导地位。"
     *         }
     *     ]
     * }
     * @param symbol
     * @return
     */
    public static StockBoard getStockBoard(String symbol) {
        Map<String, String> params = Maps.newHashMap();
        params.put("code", SymbolUtil.getStockId(symbol));
        String content = HttpUtil.get(STOCK_BOARD_API, 3000, 3000, "UTF-8", params);
        if (StringUtils.isBlank(content)) {
            return null;
        }

        StockBoard stockBoard = new StockBoard();
        JSONObject dataJson = JSONObject.parseObject(content);
        JSONArray resJson;
        if ((resJson = dataJson.getJSONArray("hxtc")) != null) {
            int size = resJson.size();
            if (size > 0) {
                JSONObject boardJson = resJson.getJSONObject(0);
                stockBoard.setBoardDesc(filterBoard(boardJson.getString("ydnr")));
            }
            if (size >= 3) {
                JSONObject businessDescJson = resJson.getJSONObject(2);
                stockBoard.setBusinessDesc(businessDescJson.getString("ydnr"));
            }
        }

        return stockBoard;
    }

    private static String filterBoard(String boardDesc) {
        if (StringUtils.isEmpty(boardDesc)) {
            return "";
        }
        StringBuilder boardNewDesc = new StringBuilder();
        for (String board : boardDesc.trim().split(" ")) {
            if (!FILTER_BOARD.contains(board)) {
                boardNewDesc.append(board).append(" ");
            }
        }
        return boardNewDesc.toString().trim();
    }

}
