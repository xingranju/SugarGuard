# -*- coding: utf-8 -*-
"""
糖知 APP (SugarGuard AI) —— 软件工程综合实践项目计划书生成脚本
输出：
  1. 鞠星冉-软件工程综合实践项目计划书.docx  —— 主文档
  2. Sprint0-甘特图.png ~ Sprint3-甘特图.png  —— 4 张甘特图（内嵌至 docx）
  3. Sprint1-甘特图.drawio ~ Sprint3-甘特图.drawio  —— 可编辑甘特图
"""
from __future__ import annotations

import os
from datetime import date, timedelta

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib import rcParams

from docx import Document
from docx.shared import Pt, Cm, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml, OxmlElement

OUT_DIR = r"E:\code\Anroid\MyApplication\综合实践\说明"
IMG_DIR = r"E:\code\Anroid\MyApplication\图"
os.makedirs(OUT_DIR, exist_ok=True)

# ==================== matplotlib 中文字体设置 ====================
rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
rcParams["axes.unicode_minus"] = False

# ==================== 项目核心数据 ====================
MEMBERS = [
    ("组长 / Product Owner / Scrum Master", "鞠星冉",
     "需求定义与团队协调；AI 服务（FastAPI + ViT + DeepSeek/RAG）开发；配置与变更管理；TAPD 项目管理；答辩汇报"),
    ("开发成员 / Dev Team", "王梓名",
     "Android 前端开发（Kotlin + Jetpack Compose）；拍照识别与相机模块；与后端/AI 服务对接"),
    ("开发成员 / Dev Team", "吴汉东",
     "Android 数据层开发（Room 本地数据库 + Repository）；数据同步与离线缓存；饮食日记与数据分析模块"),
    ("开发成员 / Dev Team", "陶星佑",
     "Android UI/UX 开发（Compose 页面 + Material3 主题）；原型走查；界面优化与创新页面设计"),
    ("开发成员 / Dev Team", "王文博",
     "后端 API 开发（Spring Boot + MySQL + JWT 认证）；云服务器部署与运维；Jenkins CI/CD 流水线"),
]

# 产品 Backlog（结合"改 bug + 部署 + 数据库补全 + 优化 + 创新"五大目标)
BACKLOG = [
    ("P0", "US-01 现有项目调通与 Bug 修复", "修复现有项目全部编译/运行/逻辑 Bug，保证 Android + 后端 + AI 服务三端联调通畅", "13"),
    ("P0", "US-02 云服务器部署与上线", "完成后端 API、AI 服务、MySQL 的云服务器（腾讯云/阿里云）容器化部署，支持公网访问", "13"),
    ("P0", "US-03 账号体系与健康档案", "用户注册/登录/JWT 鉴权，5 步健康建档向导，个人中心管理健康信息", "8"),
    ("P1", "US-04 饮品/食物数据库扩充", "饮品库扩至 100+ 条（含奶茶、咖啡、果汁、碳酸等），食物库扩至 200+ 条（含常见主食、水果、蔬菜、快餐等），补全营养成分、GI、图片", "13"),
    ("P1", "US-05 健康知识库补全", "补全健康知识 RAG 库（控糖科普、运动建议、糖尿病饮食指南等），接入 AI 问答", "8"),
    ("P1", "US-06 拍照识别优化（双通道）", "优化 ML Kit 本地识别 + ViT 远程识别的准确率；增加识别结果置信度、二次确认、历史记录", "13"),
    ("P1", "US-07 饮食日记与日历视图", "按日查看/添加/删除餐食记录，支持 7/30 日切换，每日糖分热量汇总", "8"),
    ("P1", "US-08 多维数据分析", "按日/周/月/半年分析糖摄入与健康指标趋势，柱/折线图可视化", "8"),
    ("P2", "US-09 AI 个性化健康问答", "DeepSeek + RAG 多轮问答；基于用户健康档案生成个性化建议", "13"),
    ("P2", "US-10 智能饮品推荐", "基于用户偏好、历史摄入与健康目标的混合推荐，提供低糖替代方案", "8"),
    ("P2", "US-11 推送通知系统", "Firebase/厂商推送：糖摄入超标提醒、补水提醒、健康报告推送", "5"),
    ("P3", "US-12【创新】AI 营养教练", "基于周/月健康数据，AI 生成个性化营养计划书与周报；支持语音播报", "13"),
    ("P3", "US-13【创新】控糖打卡社区", "每日控糖打卡、社区排行榜、徽章成就系统，激励行为改变", "13"),
    ("P3", "US-14【创新】家庭共管模式", "支持家庭成员（如家长管理儿童）健康数据互通与预警", "8"),
    ("P3", "US-15【创新】智能预警 & 报告", "异常指标（血糖、摄入量）自动预警；每周生成可导出 PDF 健康报告", "8"),
]

# 迭代总览
ITERATIONS = [
    {
        "key": "Sprint 0",
        "name": "筹备迭代 · 需求分析与环境搭建",
        "range": (date(2026, 4, 16), date(2026, 4, 19)),
        "goal": "完成需求澄清、TAPD 平台搭建、Tgit 代码仓库建立、云服务器准备、现有项目 Bug 清单梳理，为正式开发扫清障碍。",
        "focus": "需求分析 / 业务建模 / 环境配置",
    },
    {
        "key": "Sprint 1",
        "name": "基础调通 · Bug 修复 · 云部署",
        "range": (date(2026, 4, 20), date(2026, 4, 28)),
        "goal": "修复现有项目全部阻塞性 Bug，打通 Android + 后端 + AI 服务三端联调；完成云服务器首次部署；饮品/食物数据库初步扩充。",
        "focus": "软件编码 / 软件测试 / 软件部署",
    },
    {
        "key": "Sprint 2",
        "name": "数据库扩展 · 核心功能优化",
        "range": (date(2026, 4, 29), date(2026, 5, 8)),
        "goal": "完成饮品 100+/食物 200+ 数据库扩充与健康 RAG 知识库补全；优化拍照识别、饮食日记、数据分析、AI 问答、智能推荐五大核心功能。",
        "focus": "软件设计 / 软件编码 / 代码审查",
    },
    {
        "key": "Sprint 3",
        "name": "创新功能 · 全面测试 · 答辩准备",
        "range": (date(2026, 5, 9), date(2026, 5, 18)),
        "goal": "开发 AI 营养教练、控糖打卡社区、家庭共管模式、智能预警等 2-3 项创新功能；完成系统测试、Jenkins CI/CD、答辩材料与演示视频。",
        "focus": "软件测试 / 软件部署 / 答辩准备",
    },
]

# 各迭代 Sprint Backlog —— (任务名, 负责人, 起始日, 结束日, 类别)
# 类别用于甘特图着色：dev=开发 / test=测试 / deploy=部署 / design=设计 / pm=项目管理
SPRINT0_TASKS = [
    ("现场听讲 + 需求宣贯 + 组队分工确认", "全体", date(2026, 4, 16), date(2026, 4, 16), "pm"),
    ("现有项目全量代码走查 + Bug 清单汇总", "全体", date(2026, 4, 16), date(2026, 4, 18), "design"),
    ("TAPD 项目创建 + 迭代/需求/缺陷初始化", "鞠星冉", date(2026, 4, 17), date(2026, 4, 17), "pm"),
    ("Tgit 代码仓库建立 + 分支策略 + .gitignore", "鞠星冉", date(2026, 4, 17), date(2026, 4, 18), "deploy"),
    ("业务流程图 & 用户旅程图（原型梳理）", "鞠星冉", date(2026, 4, 17), date(2026, 4, 19), "design"),
    ("Android 端编译环境对齐（SDK/Gradle/Room）", "王梓名", date(2026, 4, 17), date(2026, 4, 18), "dev"),
    ("后端 Spring Boot + MySQL 环境配置核对", "王文博", date(2026, 4, 18), date(2026, 4, 19), "dev"),
    ("FastAPI + ViT + DeepSeek Key 本地跑通", "鞠星冉", date(2026, 4, 18), date(2026, 4, 19), "dev"),
    ("云服务器（4C8G / CentOS）申请与 SSH 配置", "王梓名", date(2026, 4, 18), date(2026, 4, 19), "deploy"),
    ("产品 Backlog 评审 & Sprint 1 任务拆分", "全体", date(2026, 4, 19), date(2026, 4, 19), "pm"),
]

SPRINT1_TASKS = [
    ("Android 启动崩溃 & Compose 主题 Bug 修复", "王梓名、陶星佑", date(2026, 4, 20), date(2026, 4, 22), "dev"),
    ("Room 数据库迁移异常 & DAO 查询 Bug 修复", "吴汉东", date(2026, 4, 20), date(2026, 4, 22), "dev"),
    ("后端 JWT 过期 & CORS 跨域 Bug 修复", "王文博", date(2026, 4, 20), date(2026, 4, 22), "dev"),
    ("AI 服务端口冲突 & ViT 模型加载 Bug 修复", "鞠星冉", date(2026, 4, 20), date(2026, 4, 22), "dev"),
    ("三端联调：Android → 后端 → AI 全链路", "全体", date(2026, 4, 23), date(2026, 4, 24), "test"),
    ("MySQL 云端部署 + 数据迁移", "王文博", date(2026, 4, 23), date(2026, 4, 24), "deploy"),
    ("后端 API Docker 化 + docker-compose 编排", "王文博", date(2026, 4, 24), date(2026, 4, 25), "deploy"),
    ("AI 服务 Docker 化 + GPU/CPU 容错配置", "鞠星冉", date(2026, 4, 24), date(2026, 4, 25), "deploy"),
    ("Android Release 打包 + 安装包回归测试", "王梓名", date(2026, 4, 25), date(2026, 4, 26), "test"),
    ("饮品库扩充至 60 条（一期）", "陶星佑", date(2026, 4, 23), date(2026, 4, 25), "dev"),
    ("食物库扩充至 120 条（一期）", "吴汉东", date(2026, 4, 23), date(2026, 4, 25), "dev"),
    ("单元测试（UserDao/MealDao/AuthService）", "吴汉东、王文博", date(2026, 4, 26), date(2026, 4, 27), "test"),
    ("Postman API 测试脚本 + SonarQube 扫码", "鞠星冉", date(2026, 4, 26), date(2026, 4, 27), "test"),
    ("Sprint 1 评审 + 燃尽图更新 + 总结会", "全体", date(2026, 4, 28), date(2026, 4, 28), "pm"),
]

SPRINT2_TASKS = [
    ("饮品库扩充至 100+（二期，补全营养/图片）", "陶星佑", date(2026, 4, 29), date(2026, 5, 1), "dev"),
    ("食物库扩充至 200+（二期，补全 GI/份量）", "吴汉东", date(2026, 4, 29), date(2026, 5, 1), "dev"),
    ("健康 RAG 知识库文档收集与切分", "鞠星冉", date(2026, 4, 29), date(2026, 5, 1), "dev"),
    ("RAG 向量库（FAISS/Milvus）构建与接入", "鞠星冉", date(2026, 5, 2), date(2026, 5, 4), "dev"),
    ("ML Kit 本地识别优化（置信度、二次确认）", "王梓名", date(2026, 4, 29), date(2026, 5, 2), "dev"),
    ("ViT 远程识别接口性能调优", "鞠星冉", date(2026, 5, 2), date(2026, 5, 4), "dev"),
    ("饮食日记日历视图（7/30 日切换）重构", "陶星佑", date(2026, 5, 2), date(2026, 5, 4), "dev"),
    ("数据分析多维图表（柱/折线/饼图）重构", "吴汉东", date(2026, 5, 2), date(2026, 5, 4), "dev"),
    ("AI 问答多轮对话 + 个性化上下文", "王梓名、鞠星冉", date(2026, 5, 5), date(2026, 5, 6), "dev"),
    ("智能饮品推荐（协同过滤 + 规则混合）", "吴汉东、鞠星冉", date(2026, 5, 5), date(2026, 5, 6), "dev"),
    ("Material3 主题 v2 + 无障碍优化", "陶星佑", date(2026, 5, 5), date(2026, 5, 6), "dev"),
    ("代码审查（SonarQube 阻断高危告警归零）", "全体", date(2026, 5, 6), date(2026, 5, 7), "test"),
    ("集成测试（核心 5 功能端到端回归）", "王梓名、吴汉东", date(2026, 5, 7), date(2026, 5, 8), "test"),
    ("Sprint 2 评审 + 燃尽图更新 + 总结会", "全体", date(2026, 5, 8), date(2026, 5, 8), "pm"),
]

SPRINT3_TASKS = [
    ("【创新】AI 营养教练：周/月报生成器", "鞠星冉", date(2026, 5, 9), date(2026, 5, 11), "dev"),
    ("【创新】AI 营养教练：语音播报 + UI", "王梓名、陶星佑", date(2026, 5, 9), date(2026, 5, 11), "dev"),
    ("【创新】控糖打卡 & 徽章系统（后端）", "王文博", date(2026, 5, 9), date(2026, 5, 11), "dev"),
    ("【创新】控糖打卡 & 社区排行榜（前端）", "吴汉东、陶星佑", date(2026, 5, 10), date(2026, 5, 12), "dev"),
    ("【创新】家庭共管 & 智能预警推送", "王文博、王梓名", date(2026, 5, 11), date(2026, 5, 13), "dev"),
    ("【创新】周报 PDF 导出与分享", "王梓名", date(2026, 5, 12), date(2026, 5, 13), "dev"),
    ("Jenkins + TAPD DevOps + Tgit 流水线打通", "王文博、鞠星冉", date(2026, 5, 12), date(2026, 5, 14), "deploy"),
    ("JMeter 压测 + 性能调优", "鞠星冉", date(2026, 5, 13), date(2026, 5, 14), "test"),
    ("全量验收测试（TAPD 测试用例）", "全体", date(2026, 5, 14), date(2026, 5, 15), "test"),
    ("UI 走查 + 视觉验收 + 文案润色", "陶星佑", date(2026, 5, 14), date(2026, 5, 15), "test"),
    ("云端正式发布（v1.0.0）+ Tag + Release Note", "王文博", date(2026, 5, 15), date(2026, 5, 15), "deploy"),
    ("答辩 PPT 撰写 + 演示视频录制", "鞠星冉、陶星佑", date(2026, 5, 15), date(2026, 5, 17), "pm"),
    ("实习报告整理 + 过程文档归档", "鞠星冉", date(2026, 5, 16), date(2026, 5, 17), "pm"),
    ("答辩预演 + Q&A 准备", "全体", date(2026, 5, 18), date(2026, 5, 18), "pm"),
    ("正式答辩", "全体", date(2026, 5, 19), date(2026, 5, 19), "pm"),
]

SPRINT_BY_KEY = {
    "Sprint 0": SPRINT0_TASKS,
    "Sprint 1": SPRINT1_TASKS,
    "Sprint 2": SPRINT2_TASKS,
    "Sprint 3": SPRINT3_TASKS,
}

CATEGORY_COLORS = {
    "dev":    "#4E79A7",
    "test":   "#F28E2B",
    "deploy": "#59A14F",
    "design": "#B07AA1",
    "pm":     "#E15759",
}
CATEGORY_LABELS = {
    "dev":    "开发",
    "test":   "测试/审查",
    "deploy": "部署/运维",
    "design": "设计/建模",
    "pm":     "项目管理/评审",
}

WEEKDAY_CN = ["一", "二", "三", "四", "五", "六", "日"]


# ==================== 甘特图生成 ====================
def draw_gantt(sprint_key: str, sprint_name: str,
               start: date, end: date, tasks: list,
               out_png: str):
    total_days = (end - start).days + 1
    n = len(tasks)
    fig_w = max(14, total_days * 0.7 + 7)
    fig_h = max(5, n * 0.45 + 2.2)
    fig, ax = plt.subplots(figsize=(fig_w, fig_h))

    # 周末背景
    for i in range(total_days):
        d = start + timedelta(days=i)
        if d.weekday() >= 5:
            ax.axvspan(i - 0.5, i + 0.5, color="#F5F5F5", zorder=0)

    # 任务条
    for idx, (name, owner, ts, te, cat) in enumerate(tasks):
        y = n - idx - 1
        s_off = (ts - start).days
        dur = (te - ts).days + 1
        color = CATEGORY_COLORS.get(cat, "#4E79A7")
        ax.barh(y, dur, left=s_off - 0.4, height=0.6,
                color=color, edgecolor="#333", linewidth=0.6, zorder=3)
        ax.text(s_off + dur - 0.4 + 0.1, y, f" {owner}",
                va="center", ha="left", fontsize=8, color="#333", zorder=4)

    # Y 轴标签
    ax.set_yticks(range(n))
    ax.set_yticklabels([t[0] for t in reversed(tasks)], fontsize=9)

    # X 轴
    ax.set_xticks(range(total_days))
    xlabels = []
    for i in range(total_days):
        d = start + timedelta(days=i)
        xlabels.append(f"{d.month}/{d.day}\n{WEEKDAY_CN[d.weekday()]}")
    ax.set_xticklabels(xlabels, fontsize=8)
    ax.set_xlim(-0.6, total_days - 0.4)
    ax.set_ylim(-0.8, n - 0.2)

    # 网格
    ax.grid(axis="x", linestyle=":", color="#CCC", zorder=1)
    ax.set_axisbelow(True)

    # 标题
    title = f"{sprint_key} 甘特图 —— {sprint_name}\n周期：{start.isoformat()} ~ {end.isoformat()}（共 {total_days} 天）"
    ax.set_title(title, fontsize=13, fontweight="bold", pad=12)

    # 图例
    legend_handles = [mpatches.Patch(color=CATEGORY_COLORS[k], label=CATEGORY_LABELS[k])
                      for k in ["dev", "test", "deploy", "design", "pm"]]
    ax.legend(handles=legend_handles, loc="upper right",
              bbox_to_anchor=(1.0, -0.08), ncol=5, fontsize=9, frameon=False)

    for spine in ["top", "right"]:
        ax.spines[spine].set_visible(False)

    plt.tight_layout()
    plt.savefig(out_png, dpi=150, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    return out_png


# ==================== drawio 甘特图生成（参考实习1样式） ====================
def build_drawio_gantt(sprint_key: str, sprint_name: str,
                       start: date, end: date, tasks: list,
                       out_path: str):
    """生成简洁版 drawio 甘特图，参考 E:\\code\\软件过程与项目管理\\实习1\\Sprint1-甘特图.drawio"""
    total_days = (end - start).days + 1
    # 尺寸
    x_id, w_id = 0, 40
    x_task, w_task = 40, 260
    x_owner, w_owner = 300, 70
    x_days, w_days = 370, 40
    x_cal = 410
    cell_w = 55
    header_h = 45  # 日期+星期两行
    row_h = 26
    y_hdr_date = 30
    y_hdr_week = 45
    y_body = 60

    cells = []

    def add_rect(cid, x, y, w, h, value, style):
        cells.append(
            f'        <mxCell id="{cid}" parent="1" style="{style}" value="{value}" vertex="1">\n'
            f'          <mxGeometry height="{h}" width="{w}" x="{x}" y="{y}" as="geometry" />\n'
            f'        </mxCell>'
        )

    header_style = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#4472C4;fontColor=#FFFFFF;"
                    "fontStyle=1;fontSize=10;fontFamily=Microsoft YaHei;strokeColor=#2F5496;")
    date_style = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#4472C4;fontColor=#FFFFFF;"
                  "fontStyle=1;fontSize=9;fontFamily=Microsoft YaHei;strokeColor=#2F5496;")
    week_style = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#D6E4F0;"
                  "fontSize=8;fontFamily=Microsoft YaHei;strokeColor=#B4C7E7;")
    weekend_style = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFE8E8;"
                     "fontSize=8;fontFamily=Microsoft YaHei;strokeColor=#E4A0A0;")
    cell_row_style = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;fontSize=9;"
                      "fontFamily=Microsoft YaHei;strokeColor=#CCCCCC;align=left;spacingLeft=4;")
    cell_row_center = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;fontSize=9;"
                       "fontFamily=Microsoft YaHei;strokeColor=#CCCCCC;")
    weekend_cell = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FAFAFA;"
                    "fontSize=8;fontFamily=Microsoft YaHei;strokeColor=#E5E5E5;")
    bar_style_base = ("rounded=1;whiteSpace=wrap;html=1;fontColor=#FFFFFF;"
                      "fontSize=9;fontFamily=Microsoft YaHei;fontStyle=1;strokeColor=#333333;")
    cat_fill = {
        "dev":    "#4E79A7",
        "test":   "#F28E2B",
        "deploy": "#59A14F",
        "design": "#B07AA1",
        "pm":     "#E15759",
    }

    # 表头
    add_rect("hdr_id",    x_id,    y_hdr_date, w_id,    header_h, "序号",     header_style)
    add_rect("hdr_task",  x_task,  y_hdr_date, w_task,  header_h, "任务名称", header_style)
    add_rect("hdr_owner", x_owner, y_hdr_date, w_owner, header_h, "负责人",   header_style)
    add_rect("hdr_days",  x_days,  y_hdr_date, w_days,  header_h, "天数",     header_style)

    # 日期表头（两行：日期 + 星期）
    for i in range(total_days):
        d = start + timedelta(days=i)
        is_weekend = d.weekday() >= 5
        x = x_cal + i * cell_w
        add_rect(f"dh{i}", x, y_hdr_date, cell_w, 15, f"{d.month}/{d.day}", date_style)
        add_rect(f"dw{i}", x, y_hdr_week, cell_w, 15, WEEKDAY_CN[d.weekday()],
                 weekend_style if is_weekend else week_style)

    # 任务行 + 背景网格
    for idx, (name, owner, ts, te, cat) in enumerate(tasks):
        y = y_body + idx * row_h
        add_rect(f"r{idx}_id",    x_id,    y, w_id,    row_h, str(idx + 1), cell_row_center)
        # 任务名称单元格
        task_val = name.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
        add_rect(f"r{idx}_task",  x_task,  y, w_task,  row_h, task_val,     cell_row_style)
        add_rect(f"r{idx}_owner", x_owner, y, w_owner, row_h, owner,        cell_row_center)
        dur = (te - ts).days + 1
        add_rect(f"r{idx}_days",  x_days,  y, w_days,  row_h, str(dur),     cell_row_center)
        # 日历背景网格（周末浅色）
        for i in range(total_days):
            d = start + timedelta(days=i)
            x = x_cal + i * cell_w
            style = weekend_cell if d.weekday() >= 5 else cell_row_center
            add_rect(f"r{idx}_bg{i}", x, y, cell_w, row_h, "", style)
        # 条
        s_off = (ts - start).days
        bar_x = x_cal + s_off * cell_w + 2
        bar_w = dur * cell_w - 4
        bar_style = bar_style_base + f"fillColor={cat_fill.get(cat, '#4E79A7')};"
        add_rect(f"r{idx}_bar", bar_x, y + 3, bar_w, row_h - 6, owner, bar_style)

    # 图例
    lg_y = y_body + len(tasks) * row_h + 20
    legend_items = [
        ("dev", "开发"),
        ("test", "测试/审查"),
        ("deploy", "部署/运维"),
        ("design", "设计/建模"),
        ("pm", "项目管理/评审"),
    ]
    lg_x = x_cal
    for i, (k, label) in enumerate(legend_items):
        style = bar_style_base + f"fillColor={cat_fill[k]};"
        add_rect(f"lg{i}", lg_x + i * 130, lg_y, 120, 22, label, style)

    # 标题
    title_style = ("text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;"
                   "fontSize=14;fontFamily=Microsoft YaHei;fontStyle=1;")
    title = f"{sprint_key} 甘特图 —— {sprint_name}  （{start.isoformat()} ~ {end.isoformat()}，共 {total_days} 天）"
    add_rect("title", 0, 0, x_cal + total_days * cell_w, 28, title, title_style)

    page_w = max(1200, x_cal + total_days * cell_w + 60)
    page_h = lg_y + 80

    body = "\n".join(cells)
    xml = f"""<mxfile host="Electron" agent="SugarGuard-plan-gen" version="29.0">
  <diagram name="{sprint_key} 甘特图" id="gantt-{sprint_key.replace(' ', '').lower()}">
    <mxGraphModel dx="900" dy="600" grid="0" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{page_w}" pageHeight="{page_h}" background="#ffffff" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
{body}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
"""
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(xml)
    return out_path


# ==================== docx 构造辅助 ====================
doc = Document()

# 全局 Normal 样式
_normal = doc.styles["Normal"]
_normal.font.name = "宋体"
_normal.font.size = Pt(11)
_normal.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
_normal.paragraph_format.line_spacing = 1.5

for sec in doc.sections:
    sec.top_margin = Cm(2.5)
    sec.bottom_margin = Cm(2.5)
    sec.left_margin = Cm(2.8)
    sec.right_margin = Cm(2.8)


def set_run_font(run, name="宋体", size=11, bold=False, color=None):
    run.font.name = name
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = RGBColor(*color)
    run.element.rPr.rFonts.set(qn("w:eastAsia"), name)


def add_heading(text, level=1, center=False):
    p = doc.add_paragraph()
    if center:
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    sizes = {0: 22, 1: 16, 2: 14, 3: 12, 4: 11}
    set_run_font(run, name="黑体", size=sizes.get(level, 12), bold=True,
                 color=(0x1F, 0x3A, 0x5F))
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(4)
    return p


def add_para(text, indent=True, size=11, bold=False):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold)
    if indent:
        p.paragraph_format.first_line_indent = Cm(0.74)
    return p


def add_bullet(text, size=11):
    p = doc.add_paragraph(style=None)
    run = p.add_run("• " + text)
    set_run_font(run, size=size)
    p.paragraph_format.left_indent = Cm(0.5)
    return p


def set_cell_shade(cell, color):
    cell._tc.get_or_add_tcPr().append(
        parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color}"/>')
    )


def set_cell_borders(cell):
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = OxmlElement("w:tcBorders")
    for edge in ("top", "left", "bottom", "right"):
        b = OxmlElement(f"w:{edge}")
        b.set(qn("w:val"), "single")
        b.set(qn("w:sz"), "4")
        b.set(qn("w:color"), "888888")
        tcBorders.append(b)
    tcPr.append(tcBorders)


def make_table(headers, rows, col_widths=None, head_color="1F3A5F",
               zebra=True, font_size=10, align_cols=None):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    # 表头
    for i, h in enumerate(headers):
        c = table.rows[0].cells[i]
        c.text = ""
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(h)
        set_run_font(run, name="黑体", size=font_size, bold=True,
                     color=(0xFF, 0xFF, 0xFF))
        set_cell_shade(c, head_color)
        set_cell_borders(c)
    # 数据行
    for r, row in enumerate(rows):
        for j, v in enumerate(row):
            c = table.rows[r + 1].cells[j]
            c.text = ""
            p = c.paragraphs[0]
            if align_cols and j in align_cols:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            run = p.add_run(str(v))
            set_run_font(run, size=font_size)
            if zebra and r % 2 == 1:
                set_cell_shade(c, "F5F7FA")
            set_cell_borders(c)
    # 列宽
    if col_widths:
        for i, w in enumerate(col_widths):
            for row in table.rows:
                row.cells[i].width = Cm(w)
    doc.add_paragraph()
    return table


def add_image(path, width_cm=16):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run()
    run.add_picture(path, width=Cm(width_cm))


def add_caption(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    set_run_font(run, size=10, bold=True, color=(0x55, 0x55, 0x55))
    p.paragraph_format.space_after = Pt(10)


def page_break():
    doc.add_page_break()


# ========== 先生成 4 张甘特图 PNG + 3 个 drawio ==========
png_map = {}
for it in ITERATIONS:
    key = it["key"]
    png_path = os.path.join(OUT_DIR, f"{key.replace(' ', '')}-甘特图.png")
    draw_gantt(key, it["name"], it["range"][0], it["range"][1],
               SPRINT_BY_KEY[key], png_path)
    png_map[key] = png_path

# 4 个迭代全部生成 drawio（含 Sprint 0）
for key in ["Sprint 0", "Sprint 1", "Sprint 2", "Sprint 3"]:
    it = next(x for x in ITERATIONS if x["key"] == key)
    drawio_path = os.path.join(OUT_DIR, f"{key.replace(' ', '')}-甘特图.drawio")
    build_drawio_gantt(key, it["name"], it["range"][0], it["range"][1],
                       SPRINT_BY_KEY[key], drawio_path)

# ==================== 文档正文 ====================
# 封面
cover = doc.add_paragraph()
cover.alignment = WD_ALIGN_PARAGRAPH.CENTER
cover.paragraph_format.space_before = Pt(120)
r = cover.add_run("软件工程综合实践")
set_run_font(r, "黑体", 26, bold=True, color=(0x1F, 0x3A, 0x5F))
cover2 = doc.add_paragraph()
cover2.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = cover2.add_run("项目开发计划书")
set_run_font(r, "黑体", 28, bold=True, color=(0x26, 0xA6, 0x9A))

doc.add_paragraph()
sub = doc.add_paragraph()
sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = sub.add_run("—— SugarGuard AI · 糖知 —— \n青少年控糖智能健康管理移动应用")
set_run_font(r, "黑体", 16, bold=True, color=(0x33, 0x33, 0x33))

for _ in range(6):
    doc.add_paragraph()

info_rows = [
    ("项目名称", "SugarGuard AI（糖知）"),
    ("项目类型", "Android 移动应用 + Spring Boot 后端 + Python AI 服务"),
    ("项目组长", "鞠星冉"),
    ("组员", "鞠星冉、王梓名、吴汉东、陶星佑、王文博（共 5 人）"),
    ("课程名称", "软件工程综合实践（3.0 学分 / 3 周）"),
    ("实习周期", "2026-04-16 ~ 2026-05-19（含答辩）"),
    ("开发方法", "Scrum 敏捷（3 正式迭代 + 1 筹备迭代）"),
    ("协作平台", "TAPD + 腾讯工蜂（Tgit）+ Jenkins + SonarQube"),
    ("文档版本", "V1.2（TAPD 全模块完善版）"),
    ("编制日期", "2026-04-18"),
]
make_table(["项目信息", "内容"], info_rows,
           col_widths=[4, 12], head_color="1F3A5F",
           zebra=True, align_cols=[0])

page_break()

# 目录占位
add_heading("目 录", level=0, center=True)
toc_items = [
    "文档修订历史",
    "一、项目概述",
    "二、项目团队与角色分工",
    "三、课程目标对照（支撑毕业要求）",
    "四、业务建模",
    "五、需求分析与产品订单（Product Backlog）",
    "六、软件体系结构与设计概要",
    "七、迭代计划总览",
    "八、Sprint 0 —— 筹备迭代",
    "九、Sprint 1 —— 基础调通 · Bug 修复 · 云部署",
    "十、Sprint 2 —— 数据库扩展 · 核心功能优化",
    "十一、Sprint 3 —— 创新功能 · 全面测试 · 答辩准备",
    "十二、配置与变更管理",
    "十三、软件测试计划",
    "十四、软件部署计划（Jenkins + TAPD DevOps + Tgit）",
    "十五、代码质量与审查管理",
    "十六、项目风险识别与应对",
    "十七、里程碑与交付物清单",
    "十八、答辩准备计划",
]
for i, t in enumerate(toc_items):
    p = doc.add_paragraph()
    run = p.add_run(t)
    set_run_font(run, size=12)
    p.paragraph_format.left_indent = Cm(0.5)

page_break()

# ===== 修订历史 =====
add_heading("文档修订历史", level=1)
make_table(
    ["版本", "修订日期", "修订内容", "修订人"],
    [
        ["V0.1", "2026-04-14", "文档结构初稿，整理产品 Backlog 与迭代规划", "鞠星冉"],
        ["V1.0", "2026-04-16", "完成整体项目计划书初稿（含 4 次迭代甘特图）", "鞠星冉"],
        ["V1.1", "2026-04-17", "完善业务建模（业务流程图+用户旅程图）、系统架构图、Sprint 0 甘特图；补充 TAPD 用户画像与验收测试用例", "鞠星冉"],
        ["V1.2", "2026-04-18", "完善各迭代 TAPD 平台操作细化：新增任务、里程碑、发布计划、看板、Wiki、甘特图、腾讯工蜂、测试用例、测试协同、流水线、发布评审、项目仪表盘、统计等模块的具体操作，全面覆盖 TAPD 平台 5 大类 20+ 模块", "鞠星冉"],
    ],
    col_widths=[2, 3, 9, 2], align_cols=[0, 1, 3],
)

# ===== 一、项目概述 =====
add_heading("一、项目概述", level=1)

add_heading("1.1 项目背景", level=2)
add_para(
    "我国青少年（15-25 岁）饮食高糖化现象日益突出，奶茶、果茶、碳酸饮料等高糖饮品消费量逐年增加，"
    "导致肥胖、血糖异常、龋齿等健康问题低龄化。现有大多数健康类 App 面向中老年糖尿病患者，"
    "对青少年友好度、趣味性和个性化不足。"
)
add_para(
    "SugarGuard AI（糖知）是面向 15-25 岁青少年人群的智能控糖健康管理 Android 应用，"
    "本次综合实践以团队已有的 Android 前端 + Spring Boot 后端 + Python AI 服务三端原型代码为开发起点，"
    "通过 3 周 3 轮迭代，完成『调通 Bug → 云端部署 → 数据库补全 → 核心功能优化 → 创新功能开发』的系统化升级。"
)

add_heading("1.2 项目目标", level=2)
objectives = [
    ("功能目标", "围绕识别、记录、分析、建议四大闭环，提供食物/饮品拍照识别、饮食日记、多维数据分析、AI 问答、智能推荐等完整功能。"),
    ("质量目标", "通过单元测试、集成测试、SonarQube 代码审查、JMeter 压测等手段，保证阻断级 Bug 清零，核心功能可用率 ≥ 95%。"),
    ("交付目标", "将后端与 AI 服务以 Docker 化方式部署至云服务器，Android 端可通过公网直接访问，提供可安装的 Release APK。"),
    ("创新目标", "在原型基础上开发『AI 营养教练』『控糖打卡社区』『家庭共管』『智能预警 + 周报 PDF』等 2-3 项创新特性。"),
    ("过程目标", "按敏捷方法规范使用 TAPD + Tgit + Jenkins + SonarQube 完成全流程管理，过程文档与 TAPD 平台成果保持一致。"),
]
make_table(["目标维度", "内容"], objectives, col_widths=[3, 13], align_cols=[0])

add_heading("1.3 技术栈", level=2)
tech = [
    ["端/层", "技术选型", "理由"],
    ["Android 前端", "Kotlin 1.9 + Jetpack Compose + Material3 + Retrofit + Room + ML Kit + CameraX", "Google 官方推荐栈，声明式 UI 开发效率高，Material3 原生支持动态主题"],
    ["后端", "Spring Boot 3 + Spring Security + JWT + Spring Data JPA + MySQL 8", "主流 Java 企业级栈，生态成熟，团队熟悉度高"],
    ["AI 服务", "Python 3.11 + FastAPI + ViT（视觉 Transformer）+ DeepSeek API + FAISS 向量库 + LangChain", "FastAPI 轻量高性能；ViT 支持细粒度食物识别；DeepSeek + RAG 提供专业控糖问答"],
    ["数据存储", "MySQL 8（云端）+ Room/SQLite（本地缓存）+ FAISS 向量库", "混合存储：云端保证多端同步，本地保证离线可用"],
    ["DevOps", "Tgit（代码）+ TAPD（敏捷协作）+ Jenkins（CI/CD）+ SonarQube（代码审查）+ Docker/docker-compose（部署）", "符合指导书 3.3/3.5 条要求，一站式敏捷研发"],
    ["测试", "JUnit 5 / Mockito / MockK（单元）+ Postman（API）+ Espresso（UI）+ JMeter（压测）", "覆盖单元/API/UI/性能四层测试"],
]
make_table(tech[0], tech[1:], col_widths=[3, 6, 7], align_cols=[0])

# ===== 二、团队 =====
add_heading("二、项目团队与角色分工", level=1)
add_para(
    "团队共 5 人，采用 Scrum 敏捷方法，按『Product Owner + Scrum Master + Dev Team』角色组织。"
    "考虑到团队规模与成员熟悉度，由组长鞠星冉同时兼任 Product Owner 和 Scrum Master，负责需求与流程协调；"
    "其余 4 名同学担任开发成员，按前端、数据、UI、后端四条线分工协作，关键接口联调采用『结对开发』。"
)
make_table(
    ["角色", "姓名", "主要职责"],
    [[m[0], m[1], m[2]] for m in MEMBERS],
    col_widths=[4, 2, 10], align_cols=[0, 1],
)

add_heading("2.1 结对 & 协作矩阵", level=2)
pair_rows = [
    ["Android 前端 ↔ 后端 API", "王梓名 ↔ 王文博", "登录/餐食/分析接口联调"],
    ["Android 数据层 ↔ 后端 API", "吴汉东 ↔ 王文博", "数据同步、分页、搜索接口"],
    ["Android 前端 ↔ AI 服务", "王梓名 ↔ 鞠星冉", "拍照识别 / AI 问答接口"],
    ["Android UI ↔ 产品", "陶星佑 ↔ 鞠星冉", "原型走查、交互细节、无障碍"],
    ["DevOps ↔ 全员", "王文博 + 鞠星冉", "Jenkins 流水线、云服务器、SonarQube"],
]
make_table(["协作线", "成员", "协作内容"], pair_rows,
           col_widths=[5, 4, 7], align_cols=[0, 1])

# ===== 三、课程目标对照 =====
add_heading("三、课程目标对照（支撑毕业要求）", level=1)
add_para(
    "本计划严格对照《软件工程综合实践——实习指导书（2026 版）》第二章 2.2 节所列的 10 个实习目标，"
    "将每个目标落实到具体任务、交付物与 TAPD 平台成果，确保考核全覆盖。"
)
goal_rows = [
    ["目标 1", "4-1  查找文献资料", "业务调研、竞品分析、RAG 知识库文献收集（Sprint 0/2）"],
    ["目标 2", "5-1  熟练使用敏捷方法 + TAPD 等工具", "TAPD 需求/迭代/缺陷/报表全覆盖；过程文档与平台一致"],
    ["目标 3", "5-2  选用恰当技术栈", "Kotlin/Compose + Spring Boot + FastAPI + Docker 技术栈评估与实施"],
    ["目标 4", "2-3  问题分析与建模", "业务流程图、用户故事、Product Backlog、数据库 ER 模型"],
    ["目标 5", "3-3  系统分析评价与优化", "体系结构设计、模块划分、性能调优（JMeter 压测 + SonarQube）"],
    ["目标 6", "5-3  编码/测试/交付/部署", "Tgit 代码提交、JUnit/Postman/Espresso/JMeter 测试、Jenkins + Docker 部署"],
    ["目标 7", "11-2 项目/配置/变更管理", "TAPD 迭代 + 变更记录；Tgit 分支策略 + Tag 管理"],
    ["目标 8", "9-2  团队协作沟通", "每日站会、Sprint 评审、结对开发、跨线联调"],
    ["目标 9", "9-3  团队角色承担", "组长负责制 + 明确分工 + 角色轮值（评审主持）"],
    ["目标 10", "10-1 生命周期文档规范", "业务建模/需求/设计/编码/测试/部署/变更各阶段文档齐全"],
]
make_table(["目标编号", "对应毕业要求", "落实方式与交付物"],
           goal_rows, col_widths=[2, 4, 10], align_cols=[0, 1])

# ===== 四、业务建模 =====
add_heading("四、业务建模", level=1)
add_para(
    "依据指导书 3.5（1）要求，提供至少一个高层业务流程图。下图以用户『识别 → 记录 → 分析 → 建议』四步控糖闭环为主线，"
    "串联拍照识别、饮食日记、数据分析、AI 问答、智能推荐五大核心场景，并映射到 Android 端、后端、AI 服务三端。"
)

add_heading("4.1 高层业务流程图", level=2)
add_para(
    "业务主流程：用户打开 App → 登录/注册 → 完成健康建档 → 进入首页查看糖摄入概览 → "
    "【识别】拍照/选图 → ML Kit 本地识别 或 ViT 远程识别 → 识别结果二次确认 → "
    "【记录】写入饮食日记（本地 Room + 云端 MySQL 双写）→ "
    "【分析】多维图表展示日/周/月趋势 → "
    "【建议】AI 问答（DeepSeek + RAG）+ 智能饮品推荐 → "
    "【闭环】超标预警推送 → 用户行为修正 → 成就徽章激励。"
)
add_para(
    "下图为高层业务流程图（跨泳道视图），涵盖用户、Android 客户端、Spring Boot 后端、"
    "Python AI 服务、MySQL 数据库五个层次，以 8 个阶段（注册→建档→识别→记录→分析→问答→推荐预警→闭环激励）"
    "清晰展示了数据从用户操作到数据库持久化再到 AI 反馈的完整流向。"
)
add_image(os.path.join(IMG_DIR, "业务流程图.png"), width_cm=16.5)
add_caption("图 4-1 糖知 APP 高层业务流程图")

bp_headers = ["阶段", "用户动作", "前端处理", "后端/AI 服务", "数据写入/查询"]
bp_rows = [
    ["1 登录", "输入账号密码", "LoginScreen 表单校验", "POST /api/auth/login 返回 JWT", "users 表校验"],
    ["2 建档", "5 步向导填写", "RegisterScreen 分步引导", "POST /api/profile/init", "user_health_profile 新建"],
    ["3 识别", "拍照 / 选图", "CameraX + 压缩上传", "ML Kit 本地 or POST /ai/recognize（ViT）", "food/drink 库匹配"],
    ["4 记录", "确认添加餐食", "DiaryScreen 表单", "POST /api/meal/record", "meal_records 双写（Room+MySQL）"],
    ["5 分析", "查看图表", "AnalysisScreen 图表组件", "GET /api/meal/stats?period=", "聚合 SQL + 缓存"],
    ["6 建议", "提问 / 查看推荐", "ChatScreen / RecommendScreen", "POST /ai/chat（DeepSeek+RAG）", "conversations + RAG 向量库"],
    ["7 预警", "接收推送", "通知栏点击跳转", "定时任务扫描超标 → 推送", "notifications 表"],
]
make_table(bp_headers, bp_rows, col_widths=[1.5, 3, 3.5, 4, 4],
           align_cols=[0], font_size=9)

add_heading("4.2 用户旅程图", level=2)
add_para(
    "以核心用户画像『林小茶——重度奶茶爱好者（20 岁大三学生）』为典型用户，绘制从认知阶段到习惯养成的"
    "完整用户旅程图（6 个阶段：认知→注册建档→首次使用→日常使用→深度使用→习惯养成），"
    "每个阶段包含用户行为、触点/页面、用户想法、情绪曲线、痛点/机会 5 个维度分析。"
)
add_image(os.path.join(IMG_DIR, "用户旅程图.png"), width_cm=16.5)
add_caption("图 4-2 典型用户旅程图（林小茶——重度奶茶爱好者）")
add_para(
    "用户画像完整定义（7 个 Persona）已录入 TAPD『文档』模块，包括林小茶（重度奶茶爱好者）、"
    "陈浩然（家族病史被动用户）、赵雅琪（健身数据控）、周小宇（家长引导青少年）、"
    "孙明轩（高压研究生）、陆思远（体检异常觉醒者）、唐可欣（外卖隐性糖盲区），"
    "涵盖不同年龄段、使用动机和技术熟练度，确保产品需求覆盖多样化用户群体。"
)

# ===== 五、需求分析 =====
add_heading("五、需求分析与产品订单（Product Backlog）", level=1)
add_para(
    "采用用户故事（User Story）+ INVEST 原则组织产品订单，共 15 个核心用户故事、总计约 155 故事点。"
    "按 MoSCoW 方法划分优先级 P0/P1/P2/P3，其中 P0 与 P1 为必须交付，P2 与 P3 视实际进度取舍。"
    "原型与界面设计参见 TAPD『文档』板块（原型链接由陶星佑维护）。"
)
add_para(
    "下表为按五大开发目标整合的高层 Product Backlog 总览（15 条），"
    "详细的用户故事按模块 A-H 共拆分为 48 条、110 故事点，已录入 TAPD「需求」模块。"
)
make_table(
    ["优先级", "编号 / 标题", "用户故事", "故事点"],
    [[p[0], p[1], p[2], p[3]] for p in BACKLOG],
    col_widths=[1.5, 4.5, 9, 1.5], align_cols=[0, 3], font_size=9,
)

# ===== 六、软件设计概要 =====
add_heading("六、软件体系结构与设计概要", level=1)
add_para(
    "系统整体采用经典的『客户端 - 服务端 - AI 服务』三端分层架构，符合指导书 3.5（3）对体系结构与构件级设计的要求。"
    "详细设计（ER 图、类图、时序图）在 Sprint 1 的『软件设计阶段』由各端负责人补充到 TAPD『文档』板块。"
)

add_heading("6.1 体系结构总览", level=2)
add_para(
    "下图为系统架构图，包含 Android 客户端（UI/ViewModel/Repository/Notification 四层）、"
    "Spring Boot 后端（Controller/Service/Security/JPA 四层）、"
    "Python AI 服务（FastAPI/ViT/LangChain-DeepSeek/FAISS 三层）、"
    "MySQL 数据库（12 张核心表）以及 DevOps 基础设施（Tgit/TAPD/Jenkins/SonarQube/Docker）的完整组件映射。"
)
add_image(os.path.join(IMG_DIR, "系统架构图.png"), width_cm=16.5)
add_caption("图 6-1 糖知 APP 系统架构图")
arch_rows = [
    ["表现层", "Android Jetpack Compose UI + MVVM + StateFlow", "陶星佑 / 王梓名", "呈现用户界面、响应交互"],
    ["业务层（Android）", "ViewModel + UseCase + Repository（本地 Room + 远程 Retrofit）", "吴汉东 / 王梓名", "协调数据与 UI，处理离线场景"],
    ["服务层（后端）", "Spring Boot Controller → Service → JPA Repository", "王文博", "对外 REST API，认证鉴权"],
    ["AI 服务层", "FastAPI Router → Service → ViT Model / DeepSeek / FAISS", "鞠星冉", "图像识别、问答、RAG 检索"],
    ["数据层", "MySQL（主）+ Room/SQLite（本地缓存）+ FAISS（向量）+ 对象存储（图片）", "王文博 / 吴汉东", "结构化 + 非结构化存储"],
    ["基础设施", "Docker + docker-compose + Nginx 反代 + Jenkins CI/CD + SonarQube", "王文博 / 鞠星冉", "持续集成、部署、监控"],
]
make_table(["层次", "主要技术/组件", "负责人", "主要职责"], arch_rows,
           col_widths=[3, 6, 3, 4], align_cols=[0, 2])

add_heading("6.2 核心模块划分（构件级）", level=2)
mod_rows = [
    ["M1 账号与健康档案", "LoginScreen / RegisterScreen / HealthProfileScreen + AuthService + UserController", "US-03"],
    ["M2 识别模块", "RecognitionScreen + CameraUseCase + MLKitService + AiRecognizeApi + ViTRouter", "US-06"],
    ["M3 饮食日记", "DiaryScreen + MealRepository + MealController + MealService", "US-07"],
    ["M4 数据分析", "AnalysisScreen + StatsRepository + StatsController（聚合 SQL）", "US-08"],
    ["M5 AI 问答", "ChatScreen + ChatApi + ChatRouter + RAGService（DeepSeek + FAISS）", "US-09"],
    ["M6 智能推荐", "RecommendScreen + RecommendService（CF + 规则混合）+ DrinkRepo", "US-10"],
    ["M7 通知预警", "NotificationService + FCM/厂商通道 + 定时扫描任务（Spring @Scheduled）", "US-11, US-15"],
    ["M8 创新：AI 营养教练 / 打卡社区 / 家庭共管", "CoachScreen / CommunityScreen / FamilyScreen + 对应 Service", "US-12/13/14/15"],
]
make_table(["模块", "核心构件", "关联 User Story"], mod_rows,
           col_widths=[3, 10, 3], align_cols=[0, 2])

add_heading("6.3 数据库设计要点", level=2)
add_para(
    "系统采用 MySQL 作为主持久化存储，以「用户」实体为核心，通过关系网络连接 12 个业务实体。"
    "Android Room 端保留 8 张镜像表用于离线缓存与首次启动种子数据。"
    "下图为数据库 ER 图（实体-关系模型），展示了完整的实体、属性及关系："
)
add_image(os.path.join(IMG_DIR, "数据库ER图.png"), width_cm=16.5)
add_caption("图 6-2 数据库 ER 关系图")

db_er_rows = [
    ["用户（User）", "用户id(PK)/密码/手机号/头像", "核心实体，所有业务数据的归属主体"],
    ["健康档案（HealthProfile）", "身高/体重/BMI/性别/年龄/每日糖限制/每日热量限制/体重目标", "1:1 关联用户，存储个人健康基线"],
    ["每日健康记录（DailyHealthRecord）", "记录id(PK)/记录日期/糖摄入/饮水量/运动/睡眠/心情/血压/血糖", "1:N 关联用户，按日记录多维健康数据"],
    ["餐食记录（MealRecord）", "记录id(PK)/餐次类型/份量/含糖量/热量/食物图片/备注", "1:N 关联用户，同时关联饮品和食物营养表"],
    ["饮品（Drink）", "饮品id(PK)/饮品名称/品牌/分类/含糖量/热量/健康评分/图片URL", "饮品基础数据表，支持搜索和推荐"],
    ["食物营养（FoodNutrition）", "食物id(PK)/名称/分类/GI值/含糖量/热量/蛋白质/脂肪/份量", "食物基础数据表，供识别结果匹配"],
    ["饮品偏好（DrinkPreference）", "偏好id(PK)/偏好评分/喜欢标记", "N:1 关联用户和饮品，记录用户口味偏好"],
    ["对话历史（ConversationHistory）", "会话id/角色(user/assistant)/内容/接收", "1:N 关联用户，记录 AI 问答全部对话"],
    ["推荐历史（RecommendHistory）", "推荐类型/内容/评分/查看状态/触发时间", "1:N 关联用户，记录个性化推荐结果"],
    ["通知（Notification）", "通知id(PK)/类型/标题/正文/是否已读/创建时间", "1:N 关联用户，承载系统推送消息"],
    ["通知设置（NotificationSettings）", "糖分预警/饮水提醒/提醒时间列表/免打扰时段", "1:1 关联用户，控制通知偏好"],
    ["健康报告（HealthReport）", "报告id(PK)/报告周期/JSON内容/生成时间", "1:N 关联用户，存储AI生成的周/月报"],
    ["健康知识库（KnowledgeBase）", "知识id(PK)/类别/内容/可搜索/标签/分类", "RAG 知识文档库，供 AI 检索增强"],
]
make_table(["实体", "主要属性", "说明"], db_er_rows,
           col_widths=[3.5, 7, 5.5], align_cols=[0], font_size=9)

# ===== 七、迭代计划总览 =====
add_heading("七、迭代计划总览", level=1)
add_para(
    "依据指导书 3.4『建议一轮迭代 7-10 天』的要求，结合课程时间（2026-04-16 ~ 2026-05-19，约 34 天），"
    "将项目划分为 1 个筹备迭代（Sprint 0）+ 3 个正式迭代（Sprint 1/2/3）+ 1 次答辩，总体时间线如下。"
)
overview = [
    [it["key"], it["name"], f'{it["range"][0].isoformat()} ~ {it["range"][1].isoformat()}',
     f'{(it["range"][1] - it["range"][0]).days + 1} 天', it["focus"], it["goal"]]
    for it in ITERATIONS
]
overview.append(["答辩", "正式答辩 + 软件演示", "2026-05-19", "1 天",
                 "汇报展示", "15 分钟：10 分钟汇报 + 5 分钟问答"])
make_table(["阶段", "名称", "起止日期", "天数", "聚焦工作流", "核心目标"],
           overview, col_widths=[1.6, 3.4, 3, 1.2, 2.2, 4.6],
           align_cols=[0, 2, 3, 4], font_size=9)

# ===== 八/九/十/十一 逐迭代详情 =====
section_titles = {
    "Sprint 0": "八、Sprint 0 —— 筹备迭代",
    "Sprint 1": "九、Sprint 1 —— 基础调通 · Bug 修复 · 云部署",
    "Sprint 2": "十、Sprint 2 —— 数据库扩展 · 核心功能优化",
    "Sprint 3": "十一、Sprint 3 —— 创新功能 · 全面测试 · 答辩准备",
}

for it in ITERATIONS:
    key = it["key"]
    add_heading(section_titles[key], level=1)
    add_heading("迭代信息", level=2)
    info = [
        ["迭代名称", it["name"]],
        ["起止日期", f'{it["range"][0].isoformat()} ~ {it["range"][1].isoformat()}（共 {(it["range"][1]-it["range"][0]).days+1} 天）'],
        ["聚焦工作流", it["focus"]],
        ["迭代目标", it["goal"]],
    ]
    make_table(["项目", "内容"], info, col_widths=[3, 13], align_cols=[0])

    add_heading("冲刺订单（Sprint Backlog）", level=2)
    sb_rows = []
    tasks = SPRINT_BY_KEY[key]
    # Sprint 0 当前状态：任务1已完成(√)、任务3进行中、其余待开始
    sprint0_statuses = {0: "√", 2: "√", 3: "√", 4: "√"}
    for i, (name, owner, ts, te, cat) in enumerate(tasks):
        if key == "Sprint 0":
            status = sprint0_statuses.get(i, "待开始")
        else:
            status = "待开始"
        sb_rows.append([
            str(i + 1), name, owner,
            f'{ts.month}/{ts.day} — {te.month}/{te.day}',
            str((te - ts).days + 1),
            CATEGORY_LABELS[cat],
            status,
        ])
    make_table(["序号", "任务", "负责人", "起止日期", "天数", "类别", "状态"],
               sb_rows, col_widths=[1, 6, 2, 2.6, 1, 1.5, 1.4],
               align_cols=[0, 2, 3, 4, 5, 6], font_size=9)

    # ===== TAPD 细化活动表 =====
    add_heading("TAPD 平台操作细化", level=2)
    if key == "Sprint 0":
        tapd_rows = [
            ["4/16 上午", "TAPD「项目管理」", "创建「糖知SugarGuard」项目；邀请5名成员加入（鞠/王梓/吴/陶/王文）；配置角色：鞠星冉=PO+SM，其余=Dev Team", "鞠星冉", "5人全部加入项目"],
            ["4/16 上午", "TAPD「迭代」", "创建4个迭代：Sprint 0(4/16-4/19)、Sprint 1(4/20-4/28)、Sprint 2(4/29-5/8)、Sprint 3(5/9-5/18)，各设目标描述", "鞠星冉", "4个迭代已创建且可切换"],
            ["4/16 上午", "TAPD「里程碑」", "创建6个项目里程碑：M1(4/19 Sprint0完成)、M2(4/28 Sprint1完成)、M3(5/8 Sprint2完成)、M4(5/15 v1.0.0发布)、M5(5/18 答辩材料冻结)、M6(5/19 正式答辩)，各设目标描述与交付物", "鞠星冉", "6个里程碑已创建"],
            ["4/16 上午", "TAPD「发布计划」", "创建4个发布版本：v0.1.0-alpha(Sprint1末4/28)、v0.2.0-beta(Sprint2末5/8)、v1.0.0(Sprint3末5/15)、答辩版本(5/18)，各版本含预期交付功能清单与验收标准", "鞠星冉", "发布计划4条已录入"],
            ["4/16 上午", "TAPD「看板」", "配置项目看板：创建5列(待开始→开发中→代码审查→测试中→已完成)；配置WIP限制(开发中≤8,审查中≤3)；关联迭代筛选器；设置卡片显示字段(优先级/故事点/负责人)", "鞠星冉", "看板5列+WIP限制配置完成"],
            ["4/16 下午", "TAPD「需求」", "录入模块A用户管理9条(US-A01~A09,共18点)、模块B食物识别6条(US-B01~B06,共16点)，设优先级+故事点+所属迭代", "鞠星冉", "需求15条，P0标红"],
            ["4/16-4/18", "TAPD「缺陷」", "代码走查Bug录入：每条含标题[模块]、严重程度(致命/严重/一般/轻微)、优先级(紧急/高/中/低)、重现步骤、实际/预期结果、截图", "鞠星冉/吴汉东", "缺陷≥20条（P0≥5,P1≥8,P2≥7）"],
            ["4/17 上午", "TAPD「需求」", "录入模块C饮食日记9条(US-C01~C09,共19点)、模块D健康分析6条(US-D01~D06,共18点)", "鞠星冉", "需求累计30条"],
            ["4/17 上午", "TAPD「需求」", "录入模块E AI助手7条(US-E01~E07,共17点)、模块F健康记录5条(US-F01~F05,共10点)", "鞠星冉", "需求累计42条"],
            ["4/17 下午", "TAPD「需求」", "录入模块G成就系统2条(US-G01~G02,共5点)、模块H通知设置4条(US-H01~H04,共7点)；所有需求关联到迭代1或2", "鞠星冉", "需求总计48条/110点"],
            ["4/17", "TAPD「文档」", "上传7个Persona卡片（林小茶/陈浩然/赵雅琪/周小宇/孙明轩/陆思远/唐可欣），含年龄/职业/标签/核心需求/动机", "陶星佑", "文档「用户画像」1份(7个角色)"],
            ["4/17", "TAPD「文档」", "上传竞品分析表（薄荷健康/糖护士/营养师/MyFitnessPal 4款APP对比）", "陶星佑", "文档「竞品分析」1份"],
            ["4/17", "TAPD「文档」", "上传技术架构图PNG（三端架构）、系统架构图PNG、业务流程图PNG", "鞠星冉", "文档「架构设计」3份图"],
            ["4/17", "TAPD「Wiki」", "创建项目Wiki首页：包含项目简介、技术栈总览、快速开始指南（本地运行/远程部署）、API文档导航、编码规范链接、会议纪要索引页；后续各Sprint持续补充", "鞠星冉", "Wiki首页+≥3个子页面"],
            ["4/17", "TAPD「任务」", "为Sprint 0的10项冲刺订单在TAPD「任务」模块创建对应子任务：每个任务含标题/负责人/预估工时(h)/起止日期/关联需求号，状态标记为'未开始'或'进行中'", "鞠星冉", "≥10条任务已录入且有负责人"],
            ["4/17-4/18", "TAPD「文档」", "上传编码规范：Kotlin=Google Style、Java=阿里巴巴开发手册、Python=PEP8+Black", "鞠星冉", "文档「编码规范」1份"],
            ["4/17-4/18", "TAPD「文档」", "上传Git分支策略说明：main/dev/feature/bugfix/hotfix 五种分支用途+合并规则+Commit Message格式", "鞠星冉", "文档「分支策略」1份"],
            ["4/18", "TAPD「文档」", "上传原型设计截图（25个高保真界面），标注各界面对应的用户故事编号", "陶星佑", "文档「原型设计」1份(25屏)"],
            ["4/19", "TAPD「迭代」→Sprint1", "Sprint 1 Backlog评审：将US-A01~A07、B01/B02/B04/B05、C01~C05、D01、E01、F01/F04共20条需求拖入Sprint 1，指定负责人和预估完成日", "全体", "Sprint1含20条需求/45点/有负责人"],
            ["4/19", "TAPD「故事墙」", "Sprint 1故事墙初始化：20条需求卡片全部处于「待开始」列；配置故事墙列为：待开始→开发中→代码审查→测试中→已完成", "鞠星冉", "故事墙5列，20张卡片在左侧"],
            ["4/19", "TAPD「甘特图」", "查看TAPD甘特图视图：确认Sprint 0全部任务时间线无冲突、无遗漏、依赖关系合理；导出甘特图截图存档到TAPD文档模块", "鞠星冉", "甘特图截图1张已存档"],
        ]
        make_table(["时间", "TAPD 模块", "具体操作内容", "负责人", "完成标准"],
                   tapd_rows, col_widths=[1.8, 2.2, 7, 2.2, 3],
                   align_cols=[0, 1, 3], font_size=9)
    elif key == "Sprint 1":
        tapd_rows = [
            ["4/20", "TAPD「任务」", "为Sprint 1所有用户故事(20条)拆分子任务：每个US拆2-5个开发/测试子任务(如US-A01拆'前端注册页面'+'后端注册API'+'注册单元测试')，共≥50条子任务，每条指定负责人+预估工时", "全体", "子任务≥50条,每条有负责人"],
            ["4/20 起每日", "TAPD「故事墙」", "每日10:00站会后更新：将开始的卡片拖到「开发中」→代码提交后拖到「代码审查」→Review通过拖到「测试中」→功能验证后拖到「已完成」", "全体", "故事墙每日至少1次更新"],
            ["4/20 起每日", "TAPD「迭代」", "记录每日站会纪要（昨天完成/今天计划/阻碍）到迭代「评论」或「文档」中", "鞠星冉", "站会纪要≥8天份"],
            ["4/20 起每日", "TAPD「看板」", "每日站会后同步更新看板卡片状态：任务从「待开始」→「开发中」→「代码审查」→「测试中」→「已完成」按实际进度拖拽；关注WIP是否超限", "全体", "看板每日至少更新1次"],
            ["4/20-4/22", "TAPD「缺陷」", "修复Android启动崩溃/Room迁移/JWT过期/AI端口Bug，每条缺陷：状态「新建」→「处理中」→「已解决」→组长验证→「关闭」；PR关联缺陷号", "修复人→鞠星冉验证", "P0缺陷8条全部关闭"],
            ["4/20-4/22", "Tgit", "Bug修复分支命名 bugfix/TAPD-xxx，每次PR标题含[TAPD-缺陷号]，Description关联需求US编号", "各修复人", "≥8个PR有TAPD关联"],
            ["4/20-4/28", "TAPD「腾讯工蜂」", "所有代码通过Tgit管理：feature分支命名feature/US-xxx-简述；PR描述关联TAPD需求/缺陷号([TAPD-xxx])；合并需≥1人Review通过；代码提交自动同步到TAPD需求动态", "全体", "≥20个PR有TAPD关联"],
            ["4/23", "TAPD「里程碑」", "检查M1(Sprint0完成)里程碑状态→标记为「已达成」；更新M2(Sprint1完成,4/28)里程碑进度为「进行中」", "鞠星冉", "M1=已达成,M2=进行中"],
            ["4/23-4/24", "TAPD「需求」", "三端联调完成后：US-A01注册→已验证、US-A02登录→已验证、US-A03建档→已验证、US-B01识别→已验证", "全体", "核心4条需求状态=已验证"],
            ["4/23-4/25", "TAPD「需求」", "饮品库60条(US-04一期)→陶星佑标进度50%；食物库120条→吴汉东标进度50%", "陶星佑/吴汉东", "数据扩充需求进度≥50%"],
            ["4/23-4/25", "TAPD「文档」", "上传Postman集合JSON（覆盖Auth/Meal/Drink/AI/Health 5类≥35条API），含请求示例和断言脚本", "鞠星冉", "Postman集合1份(≥35 API)"],
            ["4/24-4/25", "TAPD「文档」", "上传部署相关文档：docker-compose.yml、Dockerfile(后端+AI)、Nginx配置、部署SOP步骤文档", "王文博", "部署文档4份"],
            ["4/25-4/26", "TAPD「文档」", "上传数据库设计文档：ER图PNG、表结构图、12张表DDL、种子数据说明", "吴汉东/王文博", "数据库设计文档≥3份"],
            ["4/26-4/27", "TAPD「测试计划」", "创建测试计划「Sprint 1单元测试」，编写用例：UserDao(5条)、MealDao(5条)、AuthService(5条)、DrinkDao(3条)、MealService(3条)、AIServiceProxy(2条)，共≥23条", "吴汉东/王文博", "测试用例≥23条/通过率≥90%"],
            ["4/26-4/27", "TAPD「测试用例」", "在TAPD「测试用例」模块创建用例集：按模块分文件夹(用户管理5条/食物识别5条/饮食日记5条/AI助手3条/健康记录3条/数据层2条)，每条含前置条件/操作步骤/预期结果字段", "吴汉东/王文博", "测试用例≥23条"],
            ["4/26-4/27", "TAPD「文档」", "上传SonarQube首次扫描报告：代码行数、覆盖率、阻断/严重/一般告警数、技术债务时间", "鞠星冉", "SonarQube报告1份"],
            ["4/26-4/27", "Tgit", "合并 dev → main（Sprint 1 RC版本），打Tag v0.1.0-alpha", "王文博/鞠星冉", "Tag v0.1.0-alpha"],
            ["4/28 上午", "TAPD「报表」", "导出Sprint 1燃尽图截图（含理想线与实际线对比）；导出迭代概况报表", "鞠星冉", "报表截图2张"],
            ["4/28 下午", "TAPD「文档」", "Sprint 1评审会议纪要：已完成需求清单(含故事点)、未完成项及原因、改进Action Item(≥3条)、下迭代聚焦点", "鞠星冉", "会议纪要1份(含Action)"],
            ["4/28", "TAPD「迭代」", "Sprint 1迭代状态改为「已完成」；未完成的需求移入Sprint 2", "鞠星冉", "Sprint 1关闭"],
            ["4/28", "TAPD「甘特图」", "导出Sprint 1甘特图截图：对比计划vs实际进度偏差，标注提前/延迟的任务；存档到TAPD文档", "鞠星冉", "甘特图截图1张(含偏差标注)"],
        ]
        make_table(["时间", "TAPD 模块", "具体操作内容", "负责人", "完成标准"],
                   tapd_rows, col_widths=[1.8, 2.2, 7, 2.2, 3],
                   align_cols=[0, 1, 3], font_size=9)
    elif key == "Sprint 2":
        tapd_rows = [
            ["4/29", "TAPD「任务」", "为Sprint 2所有需求拆分子任务：数据库扩充(US-04/05)每项拆≥4条(数据采集/清洗/入库/验证)；核心优化(US-06~10)每项拆≥3条(前端/后端/测试)，共≥60条子任务", "全体", "子任务≥60条,含工时估算"],
            ["4/29 起每日", "TAPD「故事墙」", "每日站会后更新故事墙卡片状态；如有变更需求（范围/人员/时间），在需求「评论」中记录变更原因", "全体", "故事墙每日更新"],
            ["4/29 起每日", "TAPD「迭代」", "站会纪要记录到Sprint 2迭代评论中（格式：日期+各成员昨日/今日/阻碍）", "鞠星冉", "站会纪要≥9天份"],
            ["4/29 起每日", "TAPD「看板」", "看板日常使用：每日站会后更新；用颜色标签区分优先级(红=P0/橙=P1/蓝=P2)；Sprint中期(5/4)和末期(5/8)各截图存档", "全体", "看板截图2张(中期+末期)"],
            ["4/29-5/1", "TAPD「需求」", "US-04饮品库扩充：陶星佑负责，进度标记：4/29→30%、4/30→60%、5/1→100%（≥100条含品牌/类别/营养/图片）", "陶星佑", "饮品库≥100条，需求→已实现"],
            ["4/29-5/1", "TAPD「需求」", "US-04食物库扩充：吴汉东负责，进度标记：4/29→25%、4/30→50%、5/1→100%（≥200条含GI/份量/图片）", "吴汉东", "食物库≥200条，需求→已实现"],
            ["4/29-5/1", "TAPD「需求」", "US-05健康RAG知识库：鞠星冉负责，收集控糖科普/运动建议/糖尿病饮食指南文档→切分→FAISS入库≥500片段", "鞠星冉", "RAG库≥500片段，文档上传"],
            ["4/29-5/8", "TAPD「腾讯工蜂」", "代码提交关联：所有PR标题含[TAPD-xxx]；核心文件(认证/数据库/AI接口)PR Review≥2人；Sprint末合并dev→main打Tag v0.2.0-beta", "全体", "Tag v0.2.0-beta + PR≥15个"],
            ["5/2-5/4", "TAPD「需求」", "US-06拍照识别优化：王梓名→ML Kit置信度+二次确认UI；鞠星冉→ViT性能调优。完成后需求→已实现", "王梓名/鞠星冉", "识别需求→已实现"],
            ["5/2-5/4", "TAPD「需求」", "US-07日记日历视图：陶星佑→7/30日切换+日历选择器。US-08数据分析：吴汉东→折线图+统计卡片+环比。完成后→已实现", "陶星佑/吴汉东", "日记+分析需求→已实现"],
            ["5/4", "TAPD「里程碑」", "更新M2(Sprint1完成)→「已达成」(如Sprint1末未标记)；M3(Sprint2完成,5/8)进度标记为「进行中」", "鞠星冉", "里程碑状态已更新"],
            ["5/5-5/6", "TAPD「需求」", "US-09 AI问答：王梓名/鞠星冉→多轮对话+RAG上下文。US-10推荐：吴汉东/鞠星冉→混合推荐算法+AI理由。完成后→已实现", "负责人", "问答+推荐需求→已实现"],
            ["5/5-5/6", "TAPD「需求」", "US-11通知系统+成就系统+设置页面等P2需求：实现后逐条改为「已实现」，P2完成率目标≥80%", "各负责人", "P2需求关闭率≥80%"],
            ["5/5-5/6", "TAPD「文档」", "上传RAG知识库构建文档（来源列表/切分策略/向量维度/检索效果评估截图）", "鞠星冉", "RAG文档1份"],
            ["5/5-5/6", "TAPD「文档」", "上传UI设计定稿截图（5大核心页面：首页/日记/识别/分析/AI，标注Material3规范遵循点）", "陶星佑", "UI定稿文档1份(5页截图)"],
            ["5/6-5/7", "TAPD「缺陷」", "运行SonarQube二次扫描，将阻断级(Blocker)/严重级(Critical)告警逐条录入缺陷池，全部修复并关闭", "全体", "Blocker=0, Critical=0"],
            ["5/7-5/8", "TAPD「测试计划」", "创建「Sprint 2集成测试」计划，编写用例：注册登录流程(5条)、拍照识别流程(5条)、日记增删改(5条)、分析图表(5条)、AI问答(5条)、推荐(3条)、通知(2条)，共≥30条", "王梓名/吴汉东", "集成测试用例≥30条"],
            ["5/7-5/8", "TAPD「测试用例」", "在「测试用例」模块补充集成测试用例集：按5大核心功能分文件夹(识别/日记/分析/AI/推荐)，每个用例含详细步骤+预期截图+关联需求号", "王梓名/吴汉东", "测试用例累计≥53条"],
            ["5/7-5/8", "TAPD「测试计划」", "执行集成测试，每条标注通过✓/失败✗/阻塞⊘，失败条目关联新建缺陷号，通过率目标≥95%", "王梓名/吴汉东", "通过率≥95%(截图)"],
            ["5/7-5/8", "TAPD「测试协同」", "使用TAPD测试协同功能闭环：测试人员执行用例→标记通过✓/失败✗→失败条目一键关联创建缺陷→指派开发修复→测试回归验证→缺陷关闭", "全体", "测试协同流程完整走通≥5轮"],
            ["5/8 上午", "TAPD「报表」", "导出Sprint 2燃尽图+需求完成率饼图+缺陷趋势图，与Sprint 1数据对比记录速率提升情况", "鞠星冉", "报表3张+对比说明"],
            ["5/8", "TAPD「项目仪表盘」", "查看项目仪表盘：核心指标概览(需求完成率/缺陷趋势/迭代燃尽/代码提交频次/测试通过率)；截图存档与Sprint 1数据对比", "鞠星冉", "仪表盘截图1张"],
            ["5/8", "TAPD「统计」", "导出Sprint 2统计数据：需求完成率(含与Sprint 1对比)、缺陷密度、平均缺陷修复时长、迭代速率(故事点/天)、代码提交频次趋势", "鞠星冉", "统计数据截图/导出1份"],
            ["5/8", "TAPD「甘特图」", "导出Sprint 2甘特图截图：标注实际vs计划偏差，分析延迟原因和提前完成的任务", "鞠星冉", "甘特图截图1张(含偏差)"],
            ["5/8 下午", "TAPD「文档」", "Sprint 2评审会议纪要：已完成需求清单(含故事点与Sprint 1对比)、Demo截图/录屏、改进Action Item≥3条", "鞠星冉", "会议纪要1份"],
            ["5/8 下午", "TAPD「迭代」→Sprint3", "Sprint 2关闭；Sprint 3 Backlog评审：US-12~US-15创新需求拖入Sprint 3，按「必做2项+可选2项」优先级排列", "全体", "Sprint3含≥6条需求"],
        ]
        make_table(["时间", "TAPD 模块", "具体操作内容", "负责人", "完成标准"],
                   tapd_rows, col_widths=[1.8, 2.2, 7, 2.2, 3],
                   align_cols=[0, 1, 3], font_size=9)
    else:  # Sprint 3
        tapd_rows = [
            ["5/9", "TAPD「任务」", "Sprint 3所有需求(含创新功能)拆分子任务：创新功能(US-12~15)每项拆≥5条(设计/前端/后端/测试/文档)；测试/部署/答辩准备各拆子任务，共≥70条", "全体", "子任务≥70条"],
            ["5/9 起每日", "TAPD「故事墙」", "每日站会后更新故事墙；创新功能按「必做」「可选」分组管理；关键路径任务标红色标签", "全体", "故事墙每日更新"],
            ["5/9 起每日", "TAPD「迭代」", "站会纪要持续记录；遇到范围裁剪决策时在迭代评论中记录变更理由", "鞠星冉", "站会纪要≥9天份"],
            ["5/9 起每日", "TAPD「看板」", "看板冲刺管理：创新功能用「必做」(绿标签)和「可选」(灰标签)分组管理；关键路径任务标红色标签；5/15代码冻结后看板只剩文档/答辩类任务", "全体", "看板每日更新+标签分组"],
            ["5/9-5/11", "TAPD「需求」", "US-12 AI营养教练：鞠星冉→周/月报生成器后端；王梓名/陶星佑→语音播报+UI。进度：5/9→30%、5/10→70%、5/11→100%", "鞠/王梓/陶", "AI营养教练→已实现"],
            ["5/9-5/11", "TAPD「需求」", "US-13 控糖打卡社区：王文博→后端打卡API+徽章系统；吴汉东/陶星佑→前端排行榜UI。进度标记到100%后→已实现", "王文/吴/陶", "打卡社区→已实现"],
            ["5/9-5/18", "TAPD「腾讯工蜂」", "代码全程Tgit管理：feature分支→PR(含[TAPD-xxx])→Review(≥1人)→合并dev；5/15代码冻结合并dev→main打Tag v1.0.0；Release Note撰写", "全体", "Tag v1.0.0+Release Note"],
            ["5/11-5/13", "TAPD「需求」", "US-14 家庭共管(可选)：评估工作量，如可行则王文博/王梓名开发；否则在需求评论中记录「因时间裁剪到v1.1」", "王文/王梓", "完成或标记裁剪原因"],
            ["5/12-5/13", "TAPD「需求」", "US-15 智能预警+周报PDF(可选)：王梓名→PDF导出与分享。至少2项创新需求标记「已实现」", "王梓名", "≥2项创新功能已实现"],
            ["5/12-5/14", "TAPD「文档」", "上传Jenkins配置文档：Jenkinsfile内容、Pipeline截图（Build→Test→SonarQube→Deploy 4阶段）、Webhook配置步骤", "王文博/鞠星冉", "CI/CD文档1份(含截图)"],
            ["5/12-5/14", "TAPD「文档」", "上传Tgit与TAPD集成配置截图：代码提交自动关联需求/缺陷、构建结果回写到TAPD", "王文博", "集成配置文档1份"],
            ["5/12-5/14", "TAPD「流水线」", "配置TAPD流水线(对接Jenkins)：Tgit代码推送→触发Jenkins构建→单元测试→SonarQube扫描→Docker镜像构建→云端部署→构建结果自动回写TAPD需求/缺陷；Pipeline截图存档", "王文博/鞠星冉", "流水线配置完成+4阶段截图"],
            ["5/13-5/14", "TAPD「文档」", "上传JMeter压测报告：测试场景(登录/识别/日记/AI 4个)、200并发配置、P95/P99延迟数据、TPS曲线图", "鞠星冉", "压测报告1份(含4场景)"],
            ["5/14-5/15", "TAPD「测试计划」", "创建「全量验收测试」计划，编写用例覆盖8个模块：用户管理(8条)、识别(6条)、日记(8条)、分析(6条)、AI(6条)、健康记录(5条)、成就(3条)、通知(4条)、创新功能(6条)，共≥52条", "全体分工", "验收测试用例≥52条"],
            ["5/14-5/15", "TAPD「测试计划」", "执行全量验收测试，所有用例标通过✓或失败✗（失败条目当日修复重测），最终通过率100%", "全体", "通过率=100%"],
            ["5/14-5/15", "TAPD「测试协同」", "验收测试协同闭环：测试人员按52条用例逐条执行→标记通过✓/失败✗→失败条目一键创建缺陷并指派→开发当日修复→测试回归→缺陷关闭。最终通过率=100%", "全体", "测试协同全流程顺畅"],
            ["5/14-5/15", "TAPD「缺陷」", "回归测试新增缺陷全部录入，当日指定修复人→修复→验证→关闭。Sprint 3结束时活跃缺陷=0", "全体", "活跃缺陷=0"],
            ["5/15", "Tgit", "代码冻结：合并 dev→main，打Tag v1.0.0，撰写Release Note（新功能清单/已修复Bug/已知问题/环境要求）", "王文博", "Tag v1.0.0 + Release Note"],
            ["5/15", "TAPD「报表」", "导出5份最终报表：Sprint 3燃尽图、项目整体需求完成率(饼图)、缺陷趋势图(折线)、迭代速率对比(柱状)、测试通过率", "鞠星冉", "5份报表截图"],
            ["5/15", "TAPD「发布计划」", "更新发布计划v1.0.0状态为「已发布」：填写实际发布日期、版本包含功能清单(核心功能+创新功能)、已修复Bug清单、已知遗留问题、运行环境要求", "王文博", "发布计划v1.0.0=已发布"],
            ["5/15", "TAPD「发布评审」", "创建发布评审记录：评审人=全体5人；评审内容=功能完整性+代码质量+测试覆盖+部署稳定性；评审结论(通过/有条件通过)；遗留问题清单及后续计划", "鞠星冉", "发布评审记录1份"],
            ["5/15", "TAPD「里程碑」", "更新里程碑状态：M3(Sprint2完成)→「已达成」、M4(v1.0.0发布)→「已达成」；确认6个里程碑中已有4个达成", "鞠星冉", "M3/M4=已达成"],
            ["5/15-5/17", "TAPD「文档」", "上传答辩PPT（15-20页，覆盖：项目背景/团队/架构/功能Demo/测试数据/总结）", "鞠星冉/陶星佑", "PPT 1份(15-20页)"],
            ["5/15-5/17", "TAPD「文档」", "上传5分钟演示视频（注册→识别→日记→分析→AI→推荐→成就 完整链路）", "王梓名/陶星佑", "演示视频1份(≤5分钟)"],
            ["5/16-5/17", "TAPD「文档」", "上传实习报告终稿（含：敏捷过程规划、项目主要软件过程及成果、总结与评价 三大部分）", "鞠星冉", "实习报告1份"],
            ["5/17", "TAPD「文档」", "上传项目计划书终稿V2.0（补全全部燃尽图截图、冲刺总结会结论、过程改进记录）", "鞠星冉", "计划书V2.0终稿"],
            ["5/18", "TAPD「迭代」", "Sprint 3状态→已完成；逐一验证：P0需求12条全部=已验证关闭、P1需求16条全部=已验证关闭、P2关闭率≥80%", "鞠星冉", "P0/P1=100%关闭"],
            ["5/18", "TAPD「项目」", "项目总结：TAPD中所有需求/缺陷/测试/文档数量核对，确保与实习报告数据一致", "鞠星冉", "TAPD数据=报告数据"],
            ["5/18", "TAPD「里程碑」", "更新M5(答辩材料冻结)→「已达成」；M6(正式答辩)→「进行中」→答辩后标「已达成」。确认全部6个里程碑最终状态", "鞠星冉", "M5=已达成,M6更新中"],
            ["5/18", "TAPD「项目仪表盘」", "导出项目仪表盘最终截图(≥3张)：需求完成率饼图/缺陷趋势折线图/3次迭代燃尽对比/代码提交统计柱状图/测试通过率趋势，作为答辩PPT素材", "鞠星冉", "仪表盘截图≥3张"],
            ["5/18", "TAPD「统计」", "导出最终统计报告：需求48条完成率/缺陷总数及关闭率/测试用例总数及通过率/代码总行数/3次迭代速率对比(故事点/天)/人均贡献度", "鞠星冉", "统计报告1份(含对比分析)"],
            ["5/18", "TAPD「Wiki」", "Wiki最终更新：补充完整API文档(35+接口)、云端部署指南、常见问题FAQ、项目总结与回顾；确保Wiki内容与实习报告、TAPD数据三方一致", "鞠星冉", "Wiki页面≥10页"],
            ["5/18", "TAPD「甘特图」", "导出Sprint 3及项目整体甘特图截图：标注4个Sprint的实际vs计划偏差，写入项目总结文档", "鞠星冉", "甘特图截图2张(Sprint3+全局)"],
        ]
        make_table(["时间", "TAPD 模块", "具体操作内容", "负责人", "完成标准"],
                   tapd_rows, col_widths=[1.8, 2.2, 7, 2.2, 3],
                   align_cols=[0, 1, 3], font_size=9)

    add_heading("甘特图", level=2)
    add_image(png_map[key], width_cm=16.5)
    add_caption(f"图 {key} 甘特图 —— {it['name']}")

    add_heading("DoD（Definition of Done）", level=2)
    if key == "Sprint 0":
        dod = [
            "TAPD 项目、迭代、需求、缺陷池初始化完成，所有成员加入。",
            "Tgit 仓库建立，主分支 main + 迭代分支 dev + 个人分支策略写入 README。",
            "云服务器 SSH/Docker 环境 OK，生成 Access Key 妥善保管。",
            "现有项目 Android、后端、AI 三端分别能在本地独立启动。",
            "Bug 清单 ≥ 20 条（按 P0/P1/P2 分级，写入 TAPD 缺陷池）。",
            "Sprint 1 Backlog 评审通过，所有任务有明确负责人与估时。",
        ]
    elif key == "Sprint 1":
        dod = [
            "P0 级（阻断）Bug 清零，P1 级 Bug 关闭率 ≥ 80%。",
            "Android → 后端 → AI 三端联调通过，提供可运行 Release APK。",
            "后端 + AI + MySQL 已通过 docker-compose 部署至云服务器，公网可访问。",
            "饮品库 ≥ 60 条、食物库 ≥ 120 条，含完整营养成分与图片。",
            "核心模块单元测试覆盖率 ≥ 40%（UserDao/MealDao/AuthService）。",
            "Postman 集合 + SonarQube 首次扫描报告归档到 TAPD。",
            "Sprint 1 评审会记录 + 燃尽图更新至 TAPD 报表。",
        ]
    elif key == "Sprint 2":
        dod = [
            "饮品库 ≥ 100 条、食物库 ≥ 200 条，RAG 健康知识库 ≥ 500 文档切片。",
            "拍照识别 Top-3 准确率 ≥ 85%；AI 问答首字响应 ≤ 3s。",
            "饮食日记、数据分析、AI 问答、智能推荐 4 个核心页面 UI 定稿、无障碍 AA。",
            "SonarQube 阻断级、严重级告警归零。",
            "端到端集成测试用例 ≥ 30 条，通过率 ≥ 95%。",
            "Sprint 2 评审会记录 + 燃尽图更新至 TAPD 报表。",
        ]
    else:
        dod = [
            "2-3 项创新功能（AI 营养教练 / 打卡社区 / 家庭共管 / 智能预警）中至少 2 项可演示。",
            "Jenkins 流水线打通：Tgit 推送 → 构建 → 测试 → SonarQube → 部署 → TAPD 回写。",
            "JMeter 压测：核心接口 200 并发 P95 ≤ 800ms。",
            "TAPD 验收测试用例 ≥ 50 条，通过率 100%。",
            "云端发布 v1.0.0，打 Tag + Release Note。",
            "答辩 PPT（15-20 页）+ 5 分钟演示视频 + 实习报告终稿齐备。",
        ]
    for d in dod:
        add_bullet(d)

    add_heading("风险与应对（本迭代）", level=2)
    if key == "Sprint 0":
        risks = [
            ("组员对 TAPD 不熟", "提前安排 1 小时 TAPD + Tgit 培训；组长先行建模板"),
            ("云服务器审批慢", "首日多渠道申请（腾讯云学生机 + 阿里云试用）并行推进"),
            ("Bug 清单漏项", "按『启动 → 登录 → 主流程 → 边缘场景』四段式走查，结对交叉检查"),
        ]
    elif key == "Sprint 1":
        risks = [
            ("跨端联调阻塞", "设定每日 17:00 联调窗口，建立『阻断问题』TAPD 加急通道"),
            ("Docker 在云端部署踩坑", "预留 1 天 buffer；使用 Nginx 反代 + 健康检查脚本"),
            ("ViT 模型显存不足", "准备 CPU 降级方案（onnx 量化 + batch=1）"),
        ]
    elif key == "Sprint 2":
        risks = [
            ("数据补全工作量大", "分类流水线：爬取 → 清洗 → 人工复核三步，吴汉东、陶星佑并行"),
            ("RAG 召回率低", "准备规则召回 + 向量召回双通道，后期融合 rerank"),
            ("UI 返工", "Sprint 中期安排一次 Design Review，及早冻结关键页面"),
        ]
    else:
        risks = [
            ("创新功能贪多嚼不烂", "Sprint 一开始就按『必做 2 项 + 可选 2 项』分层，优先闭环再丰富"),
            ("答辩准备与开发冲突", "5/15 起非核心成员全面转向答辩材料；组长统筹进度"),
            ("第一次答辩未通过", "预留 5/19 后一周作为二次答辩准备缓冲（按指导书 4.1）"),
        ]
    make_table(["风险项", "应对策略"], risks,
               col_widths=[6, 10], align_cols=[], font_size=10)

    add_heading("燃尽图", level=2)
    add_para("（本迭代结束后由 Scrum Master 在 TAPD 报表中导出并补充到文档。）")

    add_heading("冲刺总结会结论", level=2)
    add_para("（本迭代结束后由全体成员在总结会上讨论补充，内容含：达成/未达成目标、改进项、下迭代聚焦点。）")

    page_break()

# ===== 十二、配置与变更管理 =====
add_heading("十二、配置与变更管理", level=1)
add_para(
    "依据指导书 3.5（7）与 4.2 目标 7，通过 TAPD『迭代』功能 + Tgit 分支策略实现配置与变更管理。"
)
add_heading("12.1 代码分支策略", level=2)
branch_rows = [
    ["main", "永远可部署分支，仅合并通过 Code Review 的 PR", "组长 + 王文博"],
    ["dev", "集成分支，每个迭代开始 fork 一份，迭代结束合入 main", "全体"],
    ["feature/<us-xx>-xxx", "按用户故事开分支，合并到 dev 需 1 人以上 Review", "任务负责人"],
    ["bugfix/<tapd-id>", "按 TAPD 缺陷号开分支，合并到 dev 需 1 人 Review", "任务负责人"],
    ["hotfix/<version>", "生产紧急修复，直接合 main 并回合 dev", "组长"],
]
make_table(["分支", "用途与合并规则", "负责人"], branch_rows,
           col_widths=[3.5, 10, 2.5], align_cols=[0, 2])

add_heading("12.2 Commit / PR / Tag 规范", level=2)
add_bullet("Commit Message：<type>(<scope>): <subject>，type ∈ {feat, fix, docs, style, refactor, test, chore}。")
add_bullet("PR 标题：[TAPD-<id>] <简述>；PR 描述必须关联 TAPD 需求/缺陷号。")
add_bullet("Tag：语义化版本 v<major>.<minor>.<patch>，每个迭代结束打 Tag，Release 写 Changelog。")

add_heading("12.3 变更管理流程", level=2)
add_para(
    "任何范围、优先级、责任人、时间的变更，均须按『提出 → 评估 → 决策 → 记录 → 通知』五步流程走：\n"
    "① 由提出人在 TAPD『需求/缺陷』发起评论，@ 组长；"
    "② 组长 48h 内组织评估（影响范围、工作量、风险）；"
    "③ 组会决策（可在日常站会即时议决）；"
    "④ 决策结果写入 TAPD『变更记录』文档；"
    "⑤ 通过 TAPD 站内信或微信群通知受影响成员。"
)

# ===== 十三、测试计划 =====
add_heading("十三、软件测试计划", level=1)
test_rows = [
    ["单元测试", "JUnit 5 + Mockito + MockK + Room InMemory DB", "≥ 60%（核心模块）", "吴汉东 / 王文博", "每次提交 CI 自动执行"],
    ["API 测试", "Postman 集合 + Newman（CLI 自动化）", "覆盖全部后端 + AI 接口", "鞠星冉 / 王文博", "Sprint 1 起每日夜间跑"],
    ["UI / 集成测试", "Espresso + Compose UI Test", "≥ 20 条核心场景", "王梓名 / 陶星佑", "Sprint 2、3 每次评审前全量跑"],
    ["性能测试", "JMeter 脚本（登录 / 识别 / 分析）", "200 并发 P95 ≤ 800ms", "鞠星冉", "Sprint 3 专项"],
    ["验收测试", "TAPD 测试用例集", "≥ 50 条，通过率 100%", "全体", "Sprint 3 最后 3 天"],
    ["兼容性测试", "Android 8-14 多品牌机型（红米/小米/华为）", "主流机型 P0 全部通过", "王梓名 / 陶星佑", "Sprint 3"],
]
make_table(["测试类型", "工具与方法", "指标", "负责人", "节奏"],
           test_rows, col_widths=[2.5, 5, 3, 2.5, 3], align_cols=[0])
add_para(
    "测试用例与脚本均归档到 TAPD『测试计划 → 测试用例』与 Tgit `tests/` 目录下，"
    "CI 执行日志通过 Jenkins + TAPD DevOps 自动回写到关联的需求/缺陷。"
)

# ===== 十四、部署计划 =====
add_heading("十四、软件部署计划（Jenkins + TAPD DevOps + Tgit）", level=1)
add_para(
    "应用系统按指导书 3.5（6）要求部署至云端，采用 Jenkins + TAPD DevOps + Tgit 的持续集成与部署方案。"
)
deploy_rows = [
    ["① 代码托管", "Tgit（腾讯工蜂）", "Sprint 0 建立仓库，main/dev 保护分支启用"],
    ["② CI 触发", "Tgit WebHook → Jenkins", "feature 分支：构建 + 单元测试；dev 分支：构建 + 全量测试 + SonarQube"],
    ["③ 构建产物", "Gradle（Android）、Maven（Spring Boot）、Dockerfile（AI）", "产物推送到云端制品库"],
    ["④ 自动部署", "dev 分支 → 预发环境；main 打 Tag → 生产环境", "docker-compose up -d + Nginx 灰度"],
    ["⑤ 回写 TAPD", "Jenkins Post-build → TAPD DevOps API", "构建结果关联 TAPD 需求/缺陷，实现可追溯"],
    ["⑥ 灰度 / 回滚", "docker tag :latest / :stable；Nginx 切换 upstream", "回滚不超过 5 分钟"],
    ["⑦ 监控告警", "Prometheus + Grafana（可选）+ Nginx access log", "Sprint 3 补齐"],
]
make_table(["阶段", "工具", "做法"], deploy_rows,
           col_widths=[3, 5, 8], align_cols=[0])

add_heading("14.1 服务器与域名规划", level=2)
server_rows = [
    ["Web 服务器", "腾讯云 / 阿里云 4C8G CentOS 7（或学生机）", "对外 80/443"],
    ["MySQL", "同服务器（生产建议独立实例）", "内网 3306"],
    ["AI 服务", "同服务器 Docker 容器", "内网 8000"],
    ["后端 API", "Docker 容器", "内网 8080 → Nginx 反代 /api/"],
    ["对象存储", "COS / OSS（图片）", "按需"],
    ["域名（可选）", "xxx.sugarguard.top", "配置 HTTPS（Let's Encrypt）"],
]
make_table(["组件", "选型", "备注"], server_rows,
           col_widths=[3, 8, 5], align_cols=[0])

# ===== 十五、代码质量 =====
add_heading("十五、代码质量与审查管理", level=1)
add_para(
    "依据指导书 3.5（4）要求，选择 SonarQube 作为代码审查与质量管理工具，辅以团队规范与同行评审。"
)
q_rows = [
    ["编码规范", "Android：Kotlin 官方 + Google Style；后端：阿里巴巴 Java 开发手册；Python：PEP 8 + Black", "IDE 插件（ktlint / CheckStyle / black）一次性强制"],
    ["静态扫描", "SonarQube Community（Jenkins 集成）", "阻断级 / 严重级告警数 = 0，覆盖率 ≥ 50%"],
    ["Code Review", "Tgit Pull Request + 1 人以上 Review", "核心文件（认证/数据库/AI 接口）Review 必须 ≥ 2 人"],
    ["日志规范", "SLF4J + Logback（后端）、loguru（AI）、Timber（Android）", "禁止裸 System.out.println / print"],
    ["敏感信息", ".env / Secret Manager 统一管理；禁止入库", "Git pre-commit hook（git-secrets）"],
]
make_table(["维度", "工具 / 规范", "底线要求"], q_rows,
           col_widths=[3, 8, 5], align_cols=[0])

# ===== 十六、风险 =====
add_heading("十六、项目风险识别与应对", level=1)
risk_rows = [
    ["R1", "技术", "ViT 模型准确率低", "高", "中", "准备 Top-3 返回 + 人工二次确认 + 本地 ML Kit 兜底", "鞠星冉"],
    ["R2", "技术", "云服务器资源不足（显存/带宽）", "中", "中", "CPU/ONNX 量化方案；分批推理；对象存储托管图片", "王文博 / 鞠星冉"],
    ["R3", "进度", "创新功能做不完", "高", "高", "按 P0/P1 必做 + P2/P3 选做分层；每迭代末裁剪", "组长"],
    ["R4", "人员", "考试/其他课程挤占时间", "中", "高", "关键路径任务提前；每周五盘点可用工时", "组长"],
    ["R5", "数据", "饮品/食物数据版权问题", "低", "中", "使用 Unsplash、品牌官方公开资料；注明来源", "陶星佑"],
    ["R6", "外部", "DeepSeek API 额度/网络抖动", "中", "中", "缓存历史问答；Key 轮换；离线 FAQ 兜底", "鞠星冉"],
    ["R7", "过程", "TAPD / Jenkins 学习成本", "低", "中", "Sprint 0 培训 + 组长代做模板", "组长"],
    ["R8", "质量", "答辩演示关键路径失败", "低", "极高", "演示前 2 天冻结代码 + 录制备份视频 + 离线版本", "全体"],
]
make_table(["编号", "类型", "风险描述", "概率", "影响", "应对策略", "责任人"],
           risk_rows, col_widths=[1, 1.5, 4.5, 1.2, 1.2, 5, 1.6],
           align_cols=[0, 1, 3, 4, 6], font_size=9)

# ===== 十七、里程碑 =====
add_heading("十七、里程碑与交付物清单", level=1)
milestone_rows = [
    ["M1", "2026-04-19", "Sprint 0 完成", "TAPD 初始化、Tgit 建仓、云服务器就绪、Bug 清单"],
    ["M2", "2026-04-28", "Sprint 1 完成", "阻断 Bug 清零、三端联调、云端首次部署、APK Alpha 版"],
    ["M3", "2026-05-08", "Sprint 2 完成", "数据库扩充到位、5 大核心功能优化完成、APK Beta 版"],
    ["M4", "2026-05-15", "v1.0.0 正式发布", "创新功能 2 项完成、Jenkins CI/CD 打通、云端 Release"],
    ["M5", "2026-05-18", "答辩材料冻结", "PPT、演示视频、实习报告、所有文档归档"],
    ["M6", "2026-05-19", "正式答辩", "15 分钟汇报 + 演示 + Q&A"],
]
make_table(["编号", "日期", "里程碑", "主要交付物"], milestone_rows,
           col_widths=[1.5, 3, 4, 7.5], align_cols=[0, 1])

add_heading("17.1 全部文档交付物清单（软件生命周期）", level=2)
doc_rows = [
    ["需求分析", "产品 Backlog（本书第五章）+ 原型 + 用户故事卡（TAPD）", "Sprint 0 末"],
    ["业务建模", "高层业务流程图（本书第四章）+ 用户旅程图", "Sprint 0 末"],
    ["软件设计", "体系结构文档 + ER 图 + 关键时序图", "Sprint 1 末"],
    ["编码", "Tgit 仓库 + Commit 历史 + Release Tag", "每 Sprint"],
    ["代码审查", "SonarQube 报告 + PR Review 记录", "每 Sprint"],
    ["测试", "单元测试报告 + Postman 集合 + Espresso 脚本 + JMeter 报告 + 验收用例", "逐迭代"],
    ["部署", "docker-compose.yml + Jenkinsfile + 部署 SOP", "Sprint 1/3"],
    ["配置变更管理", "TAPD 迭代 + 变更记录 + 分支策略 README", "全程"],
    ["项目管理", "本项目计划书 + 冲刺总结 + 燃尽图 + 会议纪要", "全程"],
    ["答辩", "PPT + 演示视频 + 实习报告", "Sprint 3 末"],
]
make_table(["阶段", "交付物", "交付时间"], doc_rows,
           col_widths=[3, 10, 3], align_cols=[0, 2])

# ===== 十八、答辩 =====
add_heading("十八、答辩准备计划", level=1)
add_para("依据指导书 4.1 答辩要求（15 分钟：10 分钟汇报 + 5 分钟问答），提前规划如下。")
ppt_rows = [
    ["00:00-01:00", "项目背景与选题意义", "鞠星冉"],
    ["01:00-02:30", "团队与分工 + 敏捷过程", "鞠星冉"],
    ["02:30-04:30", "技术栈与体系结构", "王文博"],
    ["04:30-07:30", "核心功能 + 创新功能 演示", "王梓名 / 陶星佑（操作）"],
    ["07:30-09:00", "测试、部署、质量数据", "吴汉东"],
    ["09:00-10:00", "总结与收获、后续展望", "鞠星冉"],
    ["10:00-15:00", "评委问答 Q&A", "全体"],
]
make_table(["时间", "内容", "主讲人"], ppt_rows,
           col_widths=[3, 9, 4], align_cols=[0, 2])

add_para(
    "答辩前 3 天（5/16-5/18）每日进行一次完整预演，由组长扮演评委提问，重点准备 TAPD 过程、"
    "技术选型、测试数据、关键 Bug 修复故事、创新点贡献等 Q&A 问题池（≥ 20 条）。"
)

add_heading("结语", level=1)
add_para(
    "本计划书为《软件工程综合实践》课程的总体项目开发计划，与《软件过程与项目管理》课程的"
    "『敏捷开发过程文档』互为补充，前者侧重『从需求到答辩』的软件生命周期全流程，后者侧重迭代执行与总结。"
    "本计划为动态文档，计划调整将以 V1.1 / V1.2 … 版本发布，并在 TAPD『文档』板块留档。"
    "愿全体组员以 TAPD 为纽带、Tgit 为基建、SonarQube 为底线、敏捷为方法，高质量地完成糖知 AI 项目，"
    "在 2026-05-19 的答辩上交出满意答卷。"
)

tail = doc.add_paragraph()
tail.alignment = WD_ALIGN_PARAGRAPH.RIGHT
r = tail.add_run("组长：鞠星冉  签名：__________    日期：2026-04-17")
set_run_font(r, size=11, bold=True)

# 保存
out_docx = os.path.join(OUT_DIR, "鞠星冉-软件工程综合实践项目计划书.docx")
tmp_docx = os.path.join(OUT_DIR, "鞠星冉-软件工程综合实践项目计划书_tmp.docx")
doc.save(tmp_docx)
try:
    os.replace(tmp_docx, out_docx)
    print("OK docx ->", out_docx)
except PermissionError:
    print("OK docx (tmp) ->", tmp_docx)
    print("WARN: 目标文件被占用，已保存为 _tmp.docx，请手动重命名。")
for k, p in png_map.items():
    print(f"OK png({k}) ->", p)
for k in ["Sprint 1", "Sprint 2", "Sprint 3"]:
    p = os.path.join(OUT_DIR, f"{k.replace(' ', '')}-甘特图.drawio")
    print(f"OK drawio({k}) ->", p)
