# 家谱管理系统

全方位家谱编排、管理与可视化系统，基于 Spring Boot 开发。

## 功能特性

- ✅ **人物信息管理** - 支持姓名、字、号、生卒年月、父母配偶子女关系、支系、迁徙路线、传记、功绩等完整信息
- ✅ **家谱管理** - 支持多个家谱，每个家谱一个家族
- ✅ **家庭关系管理** - 支持一夫多妻/一妻多夫等复杂关系
- ✅ **GEDCOM导出** - 导出标准 GEDCOM 5.5 格式，可以导入任何家谱软件
- ✅ **可视化** - 集成层级可视化、交互式家谱仪表板
- ✅ **RESTful API** - 完整的前后端分离API，方便二次开发
- ✅ **SQLite数据库** - 无需额外安装数据库，开箱即用

## 技术栈

- **后端**: Spring Boot 3.2.0 + Spring Data JPA + SQLite
- **前端**: 原生 HTML/CSS/JavaScript (可扩展Vue/React)
- **构建**: Maven
- **Java 版本**: 17+

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/actionzjh/genealogy-manager.git
cd genealogy-manager
```

### 2. 运行项目

```bash
run-app.cmd
```

这个脚本会：

- 使用本机 JDK 17 启动项目
- 如果 `target/genealogy-manager-1.0.0.jar` 不存在，先自动构建
- 直接运行打包后的 Spring Boot 应用

如果你希望手动构建后再运行，也可以使用：

```bash
.\.tools\apache-maven-3.9.5\bin\mvn.cmd -s .m2\settings.xml package -DskipTests
java -jar target/genealogy-manager-1.0.0.jar
```

如果你希望先验证构建是否正常，可以使用：

```bash
.\.tools\apache-maven-3.9.5\bin\mvn.cmd -s .m2\settings.xml test
```

### 3. 本地运行目录说明

- `run-app.cmd`：一键启动脚本，推荐本地开发时直接使用
- `.m2/settings.xml`：项目内 Maven 配置，固定本地仓库位置
- `.m2/repository/`：本机依赖缓存目录，体积较大，默认不提交
- `.tools/`：本地 Maven 工具目录，仅用于当前机器构建和启动，默认不提交

### 4. 访问

打开浏览器访问: http://localhost:8081

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/genealogy/list` | GET | 家谱列表（分页） |
| `/api/genealogy/all` | GET | 获取所有家谱 |
| `/api/genealogy` | POST | 新增家谱 |
| `/api/genealogy/{id}` | PUT/DELETE/GET | 更新/删除/获取 |
| `/api/person/list` | GET | 人物列表（分页） |
| `/api/person/all` | GET | 获取所有人物 |
| `/api/person/search` | GET | 搜索人物 `?keyword=xxx` |
| `/api/person` | POST | 新增人物 |
| `/api/person/{id}` | PUT/DELETE/GET | 更新/删除/获取 |
| `/api/person/children/father/{fatherId}` | GET | 根据父亲找子女 |
| `/api/family/list` | GET | 家庭关系列表 |
| `/api/family` | POST | 新增家庭关系 |
| `/api/export/gedcom?genealogyId=xxx` | GET | 导出GEDCOM |
| `/api/*/stats` | GET | 获取统计信息 |

## 项目结构

```
src/main/java/com/genealogy/
├── GenealogyApplication.java      # 启动类
├── controller/                    # 控制器层
│   ├── GenealogyController.java  # 家谱接口
│   ├── PersonController.java     # 人物接口
│   ├── FamilyController.java      # 家庭关系接口
│   └── ExportController.java      # 导出接口
├── service/                        # 服务层
│   ├── GenealogyService.java
│   ├── PersonService.java
│   └── FamilyService.java
├── entity/                         # 实体类
│   ├── Genealogy.java
│   ├── Person.java
│   └── Family.java
├── repository/                     # Repository 数据访问层
│   ├── GenealogyRepository.java
│   ├── PersonRepository.java
│   └── FamilyRepository.java
├── util/                           # 工具类
│   └── GedcomExporter.java        # GEDCOM导出
└── config/                         # 配置类
    └── CorsConfig.java            # CORS跨域配置

src/main/resources/
├── application.properties          # 配置文件
└── static/                         # 静态资源（前端）
    ├── index.html                 # 主页
    ├── hierarchy-visualizer.html  # 层级可视化
    ├── zhangshi-jiadao.html       # 交互式家谱仪表板
    ├── d3.min.js                  # D3.js
    └── zhangshi-hierarchy.json    # 张氏层级数据
```

## 数据库说明

使用 SQLite 数据库，数据库文件 `genealogy.db` 会自动创建在项目根目录，无需手动配置。

## 开发协作

项目由 OpenClaw (小龙虾) 与开发者协同开发完成。

## 许可证

MIT
