# -*- coding: utf-8 -*-
"""
SugarGuard AI —— 编码规范文档生成脚本
输出：CODING_STANDARDS.docx
规范：Kotlin=Google Style / Java=阿里巴巴开发手册 / Python=PEP8+Black
"""
from __future__ import annotations

import os

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml, OxmlElement

OUT_DIR = r"E:\code\Anroid\MyApplication"
os.makedirs(OUT_DIR, exist_ok=True)

doc = Document()

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


def add_code_block(text, size=9):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_run_font(run, name="Consolas", size=size)
    p.paragraph_format.left_indent = Cm(1.0)
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
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
    if col_widths:
        for i, w in enumerate(col_widths):
            for row in table.rows:
                row.cells[i].width = Cm(w)
    doc.add_paragraph()
    return table


def page_break():
    doc.add_page_break()


# ==================== 封面 ====================
cover = doc.add_paragraph()
cover.alignment = WD_ALIGN_PARAGRAPH.CENTER
cover.paragraph_format.space_before = Pt(100)
r = cover.add_run("SugarGuard AI · 糖知")
set_run_font(r, "黑体", 24, bold=True, color=(0x1F, 0x3A, 0x5F))

cover2 = doc.add_paragraph()
cover2.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = cover2.add_run("编码规范说明")
set_run_font(r, "黑体", 28, bold=True, color=(0x26, 0xA6, 0x9A))

for _ in range(3):
    doc.add_paragraph()

info_rows = [
    ("项目名称", "SugarGuard AI（糖知）"),
    ("适用范围", "Android 前端（Kotlin）/ 后端（Java）/ AI 服务（Python）"),
    ("Kotlin 规范", "Google Kotlin Style Guide"),
    ("Java 规范", "阿里巴巴 Java 开发手册（黄山版）"),
    ("Python 规范", "PEP 8 + Black 格式化"),
    ("编制人", "鞠星冉"),
    ("编制日期", "2026-04-18"),
    ("文档版本", "V1.0"),
]
make_table(["项目信息", "内容"], info_rows,
           col_widths=[4, 12], head_color="1F3A5F",
           zebra=True, align_cols=[0])

page_break()

# ==================== 目录 ====================
add_heading("目 录", level=0, center=True)
toc = [
    "一、总则",
    "二、Kotlin 编码规范（Android 前端）",
    "三、Java 编码规范（Spring Boot 后端）",
    "四、Python 编码规范（AI 服务）",
    "五、通用规范（跨语言）",
    "六、代码审查与工具链",
    "七、附录：IDE 配置指南",
]
for t in toc:
    p = doc.add_paragraph()
    run = p.add_run(t)
    set_run_font(run, size=12)
    p.paragraph_format.left_indent = Cm(0.5)

page_break()

# ==================== 一、总则 ====================
add_heading("一、总则", level=1)

add_heading("1.1 目的", level=2)
add_para(
    "本文档为 SugarGuard AI 项目的编码规范，旨在统一团队代码风格，降低沟通成本，"
    "提高代码可读性、可维护性和可测试性。所有项目成员在编写代码时必须严格遵守。"
)

add_heading("1.2 规范来源", level=2)
source_rows = [
    ["Kotlin", "app/（Android 前端）", "Google Kotlin Style Guide", "https://developer.android.com/kotlin/style-guide"],
    ["Java", "backend-api/（Spring Boot 后端）", "阿里巴巴 Java 开发手册（黄山版）", "https://github.com/alibaba/p3c"],
    ["Python", "ai-service/（AI 服务）", "PEP 8 + Black 格式化工具", "https://peps.python.org/pep-0008/"],
]
make_table(["语言", "适用模块", "规范标准", "参考链接"],
           source_rows, col_widths=[2, 4, 5, 5], align_cols=[0])

add_heading("1.3 强制等级", level=2)
level_rows = [
    ["【强制】", "必须遵守，违反将在 Code Review 中被打回", "红色"],
    ["【推荐】", "建议遵守，有合理理由可例外并在 PR 中注释说明", "黄色"],
    ["【参考】", "参考执行，团队可根据实际情况灵活处理", "绿色"],
]
make_table(["等级", "说明", "标注"],
           level_rows, col_widths=[3, 10, 3], align_cols=[0, 2])

page_break()

# ==================== 二、Kotlin ====================
add_heading("二、Kotlin 编码规范（Android 前端）", level=1)
add_para("参照 Google Kotlin Style Guide，结合 Android 官方最佳实践。")

add_heading("2.1 文件与包", level=2)
add_bullet("【强制】源文件编码使用 UTF-8。")
add_bullet("【强制】包名全小写，不使用下划线。如 com.example.myapplication.ui.compose。")
add_bullet("【强制】每个源文件包含单一顶级类，文件名与类名一致（PascalCase）。")
add_bullet("【推荐】相关的扩展函数可放在同一个文件中，文件名使用描述性名称。")

add_heading("2.2 命名规范", level=2)
naming_kt = [
    ["类/接口/对象", "PascalCase", "HomeScreen, DrinkViewModel, UserDao"],
    ["函数/属性", "camelCase", "fetchDrinks(), userName, isLoggedIn"],
    ["常量 (const val / companion)", "UPPER_SNAKE_CASE", "MAX_RETRY_COUNT, API_BASE_URL"],
    ["局部变量", "camelCase", "totalSugar, currentPage"],
    ["Composable 函数", "PascalCase", "HomeScreen(), LoginScreen()"],
    ["包名", "全小写", "com.example.myapplication.viewmodel"],
    ["泛型类型参数", "单个大写字母或 PascalCase", "T, E, RequestType"],
]
make_table(["元素", "风格", "示例"],
           naming_kt, col_widths=[4, 4, 8], align_cols=[0, 1])

add_heading("2.3 格式化规则", level=2)
add_bullet("【强制】缩进使用 4 个空格，禁止使用 Tab。")
add_bullet("【强制】行宽上限 120 字符。")
add_bullet("【强制】花括号遵循 K&R 风格（左括号不换行）。")
add_bullet("【强制】每个语句独占一行，禁止将多个语句写在同一行。")
add_bullet("【强制】when / if-else 多分支时，每个分支独占一行。")
add_bullet("【推荐】函数参数过长时每个参数独占一行，右括号与函数体的 { 同一行。")

add_para("示例：", bold=True)
add_code_block(
    "// ✅ 正确\n"
    "fun createMealRecord(\n"
    "    userId: Long,\n"
    "    foodName: String,\n"
    "    sugarContent: Double,\n"
    "    mealType: MealType = MealType.LUNCH,\n"
    "): MealRecord {\n"
    "    // ...\n"
    "}\n"
    "\n"
    "// ❌ 错误\n"
    "fun createMealRecord(userId: Long, foodName: String, sugarContent: Double, mealType: MealType = MealType.LUNCH): MealRecord {"
)

add_heading("2.4 Kotlin 特性使用", level=2)
add_bullet("【强制】优先使用 val 而非 var，除非变量确实需要修改。")
add_bullet("【强制】使用数据类（data class）表示纯数据模型。")
add_bullet("【强制】空安全：禁止使用 !!（非空断言），使用 ?. / ?: / let 替代。")
add_bullet("【强制】使用 sealed class 表示有限状态集合（如 UI 状态）。")
add_bullet("【推荐】优先使用 Kotlin 标准库函数（map, filter, let, apply, also）。")
add_bullet("【推荐】使用 String 模板 \"Hello, $name\" 替代字符串拼接。")
add_bullet("【推荐】使用 object 声明单例，替代 Java 静态类模式。")
add_bullet("【参考】使用 inline 函数优化高阶函数的性能开销。")

add_heading("2.5 Jetpack Compose 规范", level=2)
add_bullet("【强制】Composable 函数名使用 PascalCase（因其本质上是 UI 组件）。")
add_bullet("【强制】状态提升（State Hoisting）：Composable 不持有状态，状态由上层传入。")
add_bullet("【强制】使用 remember 和 rememberSaveable 管理本地状态。")
add_bullet("【强制】避免在 Composable 中执行副作用（网络/数据库），使用 LaunchedEffect。")
add_bullet("【推荐】将大型 Composable 拆分为小型、可复用的组件函数。")
add_bullet("【推荐】使用 Modifier 参数作为 Composable 的第一个可选参数。")

add_heading("2.6 Android 架构规范", level=2)
add_bullet("【强制】遵循 MVVM 架构：UI(Compose) → ViewModel → Repository → DataSource。")
add_bullet("【强制】ViewModel 通过 StateFlow/LiveData 向 UI 暴露数据。")
add_bullet("【强制】Repository 封装数据源（Room + Retrofit），ViewModel 不直接调用 API。")
add_bullet("【强制】使用 Room DAO 接口访问本地数据库，禁止直接写 SQL。")
add_bullet("【推荐】使用 Hilt/Koin 进行依赖注入。")

page_break()

# ==================== 三、Java ====================
add_heading("三、Java 编码规范（Spring Boot 后端）", level=1)
add_para("参照《阿里巴巴 Java 开发手册（黄山版）》，结合 Spring Boot 最佳实践。")

add_heading("3.1 命名规范", level=2)
naming_java = [
    ["类/接口", "PascalCase", "UserService, AuthController, DrinkRepository"],
    ["方法", "camelCase", "findByUsername(), createMealRecord()"],
    ["常量", "UPPER_SNAKE_CASE", "MAX_PAGE_SIZE, JWT_SECRET_KEY"],
    ["变量/参数", "camelCase", "userId, totalCalories, pageNumber"],
    ["包名", "全小写", "com.example.usermanagement.controller"],
    ["枚举值", "UPPER_SNAKE_CASE", "BREAKFAST, LUNCH, DINNER"],
    ["抽象类", "Abstract 开头", "AbstractBaseService"],
    ["异常类", "Exception 结尾", "UserNotFoundException"],
    ["测试类", "Test 结尾", "UserServiceTest, AuthControllerTest"],
    ["DTO", "Dto 结尾", "LoginRequest, UserDto, MealRequestDto"],
    ["Entity", "无后缀", "User, Drink, MealRecord"],
]
make_table(["元素", "风格", "示例"],
           naming_java, col_widths=[3, 4, 9], align_cols=[0, 1], font_size=9)

add_heading("3.2 格式化规则", level=2)
add_bullet("【强制】缩进使用 4 个空格，禁止使用 Tab。")
add_bullet("【强制】行宽上限 120 字符。")
add_bullet("【强制】左花括号不换行，右花括号独占一行。")
add_bullet("【强制】if / else / for / while / do 后必须加花括号，即使只有一行。")
add_bullet("【强制】方法之间空一行，类内部逻辑分组之间空一行。")
add_bullet("【强制】import 不使用通配符 *，逐个导入。")

add_heading("3.3 OOP 规约", level=2)
add_bullet("【强制】类成员顺序：静态常量 → 静态变量 → 实例变量 → 构造方法 → 公有方法 → 私有方法。")
add_bullet("【强制】equals 与 hashCode 必须同时重写。")
add_bullet("【强制】不允许使用过时（@Deprecated）的类或方法，除非有注释说明原因。")
add_bullet("【强制】POJO 类属性使用包装类型（Integer 而非 int），RPC 返回值和参数使用包装类型。")
add_bullet("【推荐】类设计遵循单一职责原则（SRP），一个类只做一件事。")
add_bullet("【推荐】使用 Lombok @Data / @Builder 简化 POJO，但 Entity 类谨慎使用 @Data。")

add_heading("3.4 异常与日志", level=2)
add_bullet("【强制】不要捕获 Exception 基类，应捕获具体异常。")
add_bullet("【强制】catch 块中禁止使用 e.printStackTrace()，使用 log.error(\"...\", e)。")
add_bullet("【强制】使用 SLF4J（@Slf4j）统一日志门面，禁止直接使用 System.out.println。")
add_bullet("【强制】日志格式使用占位符：log.info(\"User {} logged in\", userId)，不用字符串拼接。")
add_bullet("【推荐】自定义业务异常继承 RuntimeException，全局异常处理器统一返回 ApiResponse。")

add_heading("3.5 Spring Boot 规范", level=2)
add_bullet("【强制】Controller 层不写业务逻辑，只负责参数校验和调用 Service。")
add_bullet("【强制】Service 层处理业务逻辑，通过 Repository/DAO 访问数据。")
add_bullet("【强制】事务注解 @Transactional 加在 Service 方法上，不加在 Controller 上。")
add_bullet("【强制】RESTful 风格：GET 查询 / POST 创建 / PUT 更新 / DELETE 删除。")
add_bullet("【强制】统一返回 ApiResponse<T> 格式：{ code, message, data }。")
add_bullet("【强制】敏感配置（数据库密码、JWT Secret、API Key）使用环境变量或 application-local.yml，不入库。")
add_bullet("【推荐】使用 @Valid + JSR 380 注解（@NotBlank, @Email, @Size）做参数校验。")
add_bullet("【推荐】分页查询使用 Spring Data 的 Pageable，不手写 LIMIT/OFFSET。")

add_heading("3.6 数据库规范（JPA/MySQL）", level=2)
add_bullet("【强制】表名、字段名使用 snake_case，如 meal_records、user_id。")
add_bullet("【强制】主键使用自增 BIGINT（@GeneratedValue(strategy = IDENTITY)）。")
add_bullet("【强制】每张表必须有 created_at 和 updated_at 字段。")
add_bullet("【强制】禁止使用外键约束（FK），在应用层维护关联关系。")
add_bullet("【强制】索引命名：idx_<表名>_<字段名>，唯一索引：uk_<表名>_<字段名>。")
add_bullet("【推荐】大文本使用 TEXT 类型，JSON 数据使用 JSON 类型。")
add_bullet("【推荐】查询超过 3 张表的 JOIN 尽量避免，改用应用层组装。")

page_break()

# ==================== 四、Python ====================
add_heading("四、Python 编码规范（AI 服务）", level=1)
add_para("参照 PEP 8 标准，使用 Black 格式化工具统一代码风格。")

add_heading("4.1 格式化工具配置", level=2)
add_para("本项目使用 Black 作为强制格式化工具，所有 Python 代码提交前必须经过 Black 格式化。")
add_bullet("【强制】行宽上限 88 字符（Black 默认值）。")
add_bullet("【强制】缩进使用 4 个空格。")
add_bullet("【强制】字符串使用双引号 \"（Black 默认）。")
add_bullet("【强制】提交前执行 black ai-service/，确保所有文件格式统一。")

add_para("pyproject.toml 配置示例：", bold=True)
add_code_block(
    "[tool.black]\n"
    "line-length = 88\n"
    "target-version = [\"py311\"]\n"
    "\n"
    "[tool.isort]\n"
    "profile = \"black\"\n"
    "line_length = 88"
)

add_heading("4.2 命名规范", level=2)
naming_py = [
    ["模块/包", "snake_case", "database_query.py, health_assessment.py"],
    ["类", "PascalCase", "DeepSeekAgent, RAGKnowledge, DrinkScraper"],
    ["函数/方法", "snake_case", "get_health_report(), parse_food_item()"],
    ["变量", "snake_case", "user_id, total_sugar, page_size"],
    ["常量", "UPPER_SNAKE_CASE", "API_BASE_URL, MAX_RETRIES, MODEL_NAME"],
    ["私有方法/变量", "_前缀", "_validate_input(), _cached_results"],
    ["类型别名", "PascalCase", "FoodList = list[FoodItem]"],
]
make_table(["元素", "风格", "示例"],
           naming_py, col_widths=[4, 4, 8], align_cols=[0, 1])

add_heading("4.3 Import 规范", level=2)
add_bullet("【强制】import 分三段，段间空一行：① 标准库 ② 第三方库 ③ 本项目模块。")
add_bullet("【强制】使用 isort 工具自动排序（profile = black）。")
add_bullet("【强制】禁止 from module import *，逐个导入需要的名称。")

add_para("示例：", bold=True)
add_code_block(
    "import os\n"
    "from datetime import datetime\n"
    "\n"
    "from fastapi import APIRouter, HTTPException\n"
    "from sqlalchemy.orm import Session\n"
    "\n"
    "from database.models import User, MealRecord\n"
    "from config.settings import settings"
)

add_heading("4.4 类型注解", level=2)
add_bullet("【强制】所有公开函数的参数和返回值必须有类型注解。")
add_bullet("【强制】FastAPI 路由函数必须使用 Pydantic BaseModel 定义请求/响应模型。")
add_bullet("【推荐】使用 from __future__ import annotations 启用延迟类型求值。")

add_para("示例：", bold=True)
add_code_block(
    "from __future__ import annotations\n"
    "\n"
    "def calculate_daily_sugar(\n"
    "    meals: list[MealRecord],\n"
    "    target_limit: float = 50.0,\n"
    ") -> dict[str, float]:\n"
    "    total = sum(m.sugar_content for m in meals)\n"
    "    return {\"total\": total, \"remaining\": target_limit - total}"
)

add_heading("4.5 FastAPI 规范", level=2)
add_bullet("【强制】路由函数使用 async def（异步），除非调用的是阻塞式同步库。")
add_bullet("【强制】请求/响应模型使用 Pydantic BaseModel，字段有 Field 描述。")
add_bullet("【强制】错误返回使用 HTTPException，附带明确的 status_code 和 detail。")
add_bullet("【强制】数据库 Session 通过 Depends 注入，不手动管理生命周期。")
add_bullet("【推荐】API 路径使用 kebab-case：/api/health-report，不用 camelCase。")
add_bullet("【推荐】复杂逻辑抽到 Service 层，Router 只做参数解析和调用。")

add_heading("4.6 AI/ML 代码规范", level=2)
add_bullet("【强制】模型加载使用单例模式或全局缓存，避免每次请求重新加载。")
add_bullet("【强制】推理超时设置上限（如 30s），防止单次请求阻塞整个服务。")
add_bullet("【强制】外部 API 调用（如 DeepSeek）设置重试机制和 fallback。")
add_bullet("【推荐】使用 loguru 替代 print 和 logging，统一日志输出。")
add_bullet("【推荐】向量索引（FAISS）的构建与查询分离，构建在初始化时完成。")

page_break()

# ==================== 五、通用规范 ====================
add_heading("五、通用规范（跨语言）", level=1)

add_heading("5.1 注释规范", level=2)
add_bullet("【强制】注释使用中文或英文，团队内统一（本项目推荐中文注释）。")
add_bullet("【强制】类和公共方法必须有文档注释（Kotlin: KDoc / Java: Javadoc / Python: Docstring）。")
add_bullet("【强制】不写无意义注释，如 // 设置名字、// 返回结果。")
add_bullet("【强制】TODO 注释格式：// TODO(负责人): 具体内容 —— 2026-04-18，必须附日期和负责人。")
add_bullet("【推荐】复杂业务逻辑前写一段说明注释，解释「为什么」而非「做了什么」。")
add_bullet("【推荐】废弃代码直接删除，不要注释掉留着（Git 有历史）。")

add_heading("5.2 安全规范", level=2)
add_bullet("【强制】密码、Token、API Key 等敏感信息禁止硬编码在代码中。")
add_bullet("【强制】使用环境变量或配置文件（.env / application-local.yml）管理敏感配置。")
add_bullet("【强制】.env 和含敏感信息的配置文件已在 .gitignore 中排除，禁止入库。")
add_bullet("【强制】用户密码使用 BCrypt 加密存储，禁止明文或 MD5。")
add_bullet("【强制】SQL 查询使用参数化（JPA/SQLAlchemy），禁止字符串拼接防止 SQL 注入。")

add_heading("5.3 Git 提交规范", level=2)
add_para("详见项目根目录 GIT_BRANCH_STRATEGY.docx 和 GIT_QUICK_GUIDE.txt。")
add_bullet("【强制】Commit Message 格式：<type>(<scope>): <subject>")
add_bullet("【强制】每次提交聚焦单一改动，禁止一个 commit 包含不相关的修改。")
add_bullet("【强制】提交前自检：编译通过 + 单元测试通过 + 格式化工具运行。")

add_heading("5.4 错误码与返回格式", level=2)
code_rows = [
    ["200", "成功", "请求处理成功"],
    ["400", "参数错误", "请求参数不合法"],
    ["401", "未认证", "JWT 无效或过期"],
    ["403", "权限不足", "无权访问该资源"],
    ["404", "未找到", "请求的资源不存在"],
    ["500", "服务器内部错误", "未预期的异常"],
]
make_table(["HTTP 状态码", "含义", "说明"],
           code_rows, col_widths=[3, 4, 9], align_cols=[0, 1])

add_para("统一响应格式：", bold=True)
add_code_block(
    "{\n"
    "  \"code\": 200,\n"
    "  \"message\": \"success\",\n"
    "  \"data\": { ... }\n"
    "}"
)

page_break()

# ==================== 六、代码审查 ====================
add_heading("六、代码审查与工具链", level=1)

add_heading("6.1 静态分析工具", level=2)
tool_rows = [
    ["Kotlin", "ktlint", "Google Kotlin Style 自动检查", "IDE 插件 + CI 集成"],
    ["Kotlin", "detekt", "代码质量与复杂度分析", "Gradle 插件"],
    ["Java", "CheckStyle", "阿里巴巴规约插件", "IDEA 插件 + CI"],
    ["Java", "SonarQube", "全面代码质量管理", "Jenkins 集成"],
    ["Python", "Black", "代码格式化（强制）", "pre-commit hook"],
    ["Python", "isort", "import 排序", "与 Black 配合使用"],
    ["Python", "flake8", "PEP 8 风格检查", "CI 集成"],
    ["Python", "mypy", "静态类型检查", "可选，推荐开启"],
]
make_table(["语言", "工具", "用途", "使用方式"],
           tool_rows, col_widths=[2, 3, 5, 6], align_cols=[0])

add_heading("6.2 Code Review 检查清单", level=2)
cr_rows = [
    ["命名", "变量/函数/类命名是否符合规范？是否有意义？"],
    ["格式", "缩进、行宽、括号风格是否正确？是否经过格式化工具？"],
    ["逻辑", "业务逻辑是否正确？边界条件是否处理？"],
    ["安全", "是否有 SQL 注入、硬编码密码、未鉴权接口？"],
    ["性能", "是否有 N+1 查询、不必要的循环、阻塞操作？"],
    ["异常", "异常是否合理捕获？日志是否充分？"],
    ["测试", "核心逻辑是否有单元测试？测试是否覆盖边界？"],
    ["注释", "复杂逻辑是否有注释？注释是否准确？"],
]
make_table(["检查项", "检查内容"],
           cr_rows, col_widths=[3, 13], align_cols=[0])

page_break()

# ==================== 七、IDE 配置 ====================
add_heading("七、附录：IDE 配置指南", level=1)

add_heading("7.1 Android Studio（Kotlin / Java）", level=2)
add_bullet("安装插件：Kotlin、ktlint、Alibaba Java Coding Guidelines。")
add_bullet("设置 → Editor → Code Style → Kotlin/Java：导入 Google/阿里规约配置。")
add_bullet("设置 → Editor → General → Auto Import：启用自动导入、优化导入。")
add_bullet("设置 → Tools → Actions on Save：启用 Reformat code + Optimize imports。")
add_bullet("Gradle 添加 ktlint 插件：org.jlleitschuh.gradle.ktlint。")

add_heading("7.2 VS Code / PyCharm（Python）", level=2)
add_bullet("安装 Black 格式化器：pip install black isort flake8。")
add_bullet("VS Code settings.json 配置：")
add_code_block(
    "{\n"
    "  \"python.formatting.provider\": \"black\",\n"
    "  \"python.formatting.blackArgs\": [\"--line-length\", \"88\"],\n"
    "  \"editor.formatOnSave\": true,\n"
    "  \"python.sortImports.args\": [\"--profile\", \"black\"],\n"
    "  \"[python]\": {\n"
    "    \"editor.defaultFormatter\": \"ms-python.black-formatter\",\n"
    "    \"editor.codeActionsOnSave\": {\n"
    "      \"source.organizeImports\": true\n"
    "    }\n"
    "  }\n"
    "}"
)
add_bullet("PyCharm：Settings → Tools → Black → Enable Black formatter。")

add_heading("7.3 Git Pre-commit Hook（可选）", level=2)
add_para("推荐配置 pre-commit 在每次提交时自动检查：", bold=True)
add_code_block(
    "# .pre-commit-config.yaml\n"
    "repos:\n"
    "  - repo: https://github.com/psf/black\n"
    "    rev: 24.3.0\n"
    "    hooks:\n"
    "      - id: black\n"
    "        language_version: python3.11\n"
    "  - repo: https://github.com/pycqa/isort\n"
    "    rev: 5.13.2\n"
    "    hooks:\n"
    "      - id: isort\n"
    "  - repo: https://github.com/pycqa/flake8\n"
    "    rev: 7.0.0\n"
    "    hooks:\n"
    "      - id: flake8\n"
    "        args: [--max-line-length=88]"
)

# ===== 结尾 =====
doc.add_paragraph()
add_para(
    "本文档为 SugarGuard AI 项目的编码规范说明，所有成员须在开发过程中严格遵守。"
    "如有疑问或需要调整，请在团队会议上讨论，由组长更新文档版本。",
    bold=True
)

tail = doc.add_paragraph()
tail.alignment = WD_ALIGN_PARAGRAPH.RIGHT
r = tail.add_run("编制人：鞠星冉    日期：2026-04-18")
set_run_font(r, size=11, bold=True)

out_docx = os.path.join(OUT_DIR, "CODING_STANDARDS.docx")
doc.save(out_docx)
print("OK ->", out_docx)
