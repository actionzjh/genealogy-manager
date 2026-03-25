# 家谱管理系统编码规范与质量指南

> 本文档基于实际编译错误总结，供协助开发人员遵循，以提升代码质量和减少返工。

---

## 一、编译错误类型汇总

### 1. 拼写 / 语法笔误（低级错误）

| 文件 | 错误代码 | 正确代码 | 说明 |
|------|---------|---------|------|
| `SearchController.java:83` | `@RequestLong personId2` | `@RequestParam Long personId2` | Spring 注解拼写错误，`@RequestLong` 不存在 |
| `GedcomImporter.java:19` | `import java.regexp.Pattern` | `import java.util.regex.Pattern` | `java.regexp` 包不存在，应为 `java.util.regex` |

**根因：** 复制粘贴代码后未检查包名和注解名的有效性。

**建议：**
- 使用 IDE（IntelliJ IDEA）编写 Java 代码，自动补全可避免拼写错误。
- 代码生成后，运行 `mvn compile` 快速验证语法正确性，再提交。
- 引入 Checkstyle 插件，强制检查常见拼写和格式问题。

---

### 2. 接口与实现方法签名不匹配

| 位置 | 问题 |
|------|------|
| `PagodaLayoutStrategy` vs `PdfLayoutStrategy` | 实现类使用 `getLayoutName()`/`generatePdf()`，接口定义的是 `getName()`/`layout()` |

**根因：** 实现类和接口分开编写，未同步更新；或接口定义后，实现类未严格对照。

**建议：**
- **先写接口，再写实现类**。接口确定后，实现类照着接口方法签名抄一遍再填充逻辑。
- 或在 IDE 中使用 "Implement Interface" 自动生成骨架代码，再填充。
- 修改接口时，同步更新所有实现类（IDE 会提示编译错误，应立即处理）。

---

### 3. 实体字段命名不一致（API层与服务层断连）

| 场景 | 调用方写法 | 实体实际字段 | 说明 |
|------|---------|-----------|------|
| `GedcomExporter.writeFamily()` | `family.getHusbandId()` | `fatherId` | GEDCOM 中"丈夫"对应 `fatherId`，不是 `husbandId` |
| `GedcomExporter.writeFamily()` | `family.getWifeId()` | `motherId` | 同理 |
| `FamilyService.findByHusbandId()` | 方法名用 `HusbandId` | Repository 用 `FatherId` | 服务层方法名与 Repository 方法名对不上 |
| `PersonController.getByGenealogy()` | `personService.findByGenealogyIdAndUserId()` | Service 中无此方法 | Controller 调用的方法 Service 层未实现 |

**根因：**
- 多人或多个模块并行开发时，字段命名没有统一标准。
- API 层（Controller）直接调用 Service，Service 层实现不完整。
- Entity 字段命名（`fatherId`/`motherId`）与业务命名（`husbandId`/`wifeId`）混淆。

**建议：**
- **Entity 字段名是唯一真相来源**。所有 Service、Repository、Controller 在写代码前先读一遍对应 Entity，确认字段名。
- 在动手写新方法前，先在 Service/Repository 中 grep 搜索是否已存在类似方法，避免重复造轮子或命名冲突。
- 同一个业务概念（如"丈夫"）在整个项目中应使用同一个字段名，建立词汇表（Glossary）：

  ```text
  Family 中的丈夫 → fatherId
  Family 中的妻子 → motherId
  GEDCOM 中 Husband/Wife → 对应到 fatherId/motherId 导出
  ```

---

### 4. iText PDF 库 API 使用错误

| 错误 | 原因 | 正确写法 |
|------|------|---------|
| `document.newPage()` | iText7 `Document` 类无此方法 | `document.getPdfDocument().addNewPage()` |
| `PdfFontFactory.createFont("...","...", false)` | iText7 不接受 `boolean` 作为第三个参数 | `PdfFontFactory.createFont("...", "...", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)` |

**根因：** iText5 和 iText7 API 差异较大，代码可能是从 iText5 迁移而来，或参考了过时文档。

**建议：**
- 明确项目使用的 iText 版本（本项目为 iText7 7.2.5），**只参考对应版本的官方文档**。
- iText7 官方文档：https://itextpdf.com/docs/itext7
- PDF 操作类（`Document`、`PdfWriter`、`PdfFontFactory`）的方法签名在 iText5 和 iText7 中差异巨大，混用会导致编译失败。
- 使用第三方库前，先在 `pom.xml` 确认版本，并在官方文档中确认基础 API 不变。

---

### 5. Repository 接口扩展不完整

| 问题 | 修复 |
|------|------|
| `PersonRepository` 需要使用 JPA Specification 查询，但未扩展 `JpaSpecificationExecutor<Person>` | `extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person>` |
| `PersonRepository.findMaxGeneration()` 需要 genealogyId 参数，但查询方法声明需要参数 | 修改 JPQL 查询支持 `NULL` 参数：`WHERE :genealogyId IS NULL OR p.genealogyId = :genealogyId` |

**根因：** 使用高级 JPA 查询方式（Specification）前未确认 Repository 是否已扩展对应接口。

**建议：**
- 规划 Repository 时，提前列出需要的所有查询能力，再决定扩展哪些接口。
- `JpaSpecificationExecutor` 是高级查询的基础，一旦需要动态条件查询（如搜索功能），立即扩展此接口。

---

### 6. Service 层方法缺失（Controller → Service 断链）

| 场景 | 问题 |
|------|------|
| `PersonController.getByGenealogy()` 调用 `personService.findByGenealogyIdAndUserId()` | Service 层无此方法，编译报错 |
| `PersonService.getMaxGeneration()` 调用 Repository 方法但签名不匹配 | Repository 方法需参数，Service 未传参 |

**根因：** Controller 和 Service 独立编写，未联调。

**建议：**
- 每次新增 Controller 方法时，**同步检查 Service 层是否有对应的 public 方法**。
- 或者先写单元测试，用测试来验证 Controller ↔ Service 的接口契约。

---

### 7. Exception 声明不匹配（throws 传播链断裂）

| 位置 | 问题 |
|------|------|
| `PdfLayoutStrategy.layout()` | 接口声明 `throws IOException`，但实现类内部 `throws Exception` |
| `PagodaLayoutStrategy.layout()` | 内部方法抛出 `Exception`，但覆写签名只声明 `IOException` |
| `PdfExportService.exportGenealogyToPdf()` | 方法内部调用的 `strategy.layout()` 抛出 `Exception`，但方法声明只 `throws IOException` |

**根因：** Java 覆写方法时，throws 子句必须兼容（抛出相同或更少的异常，或不抛）。`IOException` 是受检异常，链路中任何地方抛出了更宽泛的 `Exception` 就会导致编译失败。

**建议：**
- 方法签名中 `throws` 尽量使用宽泛类型（如 `throws Exception`）或 `throws IOException`，避免混用。
- 如果不确定会抛什么异常，宁可写 `throws Exception`，也不要让调用方编译不过。
- 或在方法内部用 `try-catch` 吞掉异常，转换为运行时异常重新抛出：

  ```java
  @Override
  public void layout(...) {
      try {
          // 业务逻辑
      } catch (Exception e) {
          throw new RuntimeException("PDF排版失败", e);
      }
  }
  ```

---

## 二、其他代码质量建议

### 1. 提交前必做检查清单

每次代码完成，准备提交前，逐项核对：

```
□ 运行 mvn compile，确认无编译错误
□ Entity 字段名确认：所有新增的 getter/setter 对应字段是否存在
□ Controller → Service → Repository 链路是否完整（每个方法都有实现）
□ 接口/抽象类的所有方法是否都被实现（implements 或 extends）
□ 第三方库（iText、EasyExcel）版本确认，API 与版本匹配
□ 方法 throws 子句是否覆盖了所有内部可能抛出的受检异常
□ 新增字段是否在 @PrePersist/@PreUpdate 中正确处理
□ Lombok @Data 生成的 getter/setter 与手写代码命名是否一致
□ 数据库字段（SQLite）与 Java Entity 字段类型是否匹配（注意 NULL 处理）
□ Security 相关：新增的 Controller 方法是否需要登录认证
□ 新增接口是否已在 SecurityConfig 中配置放行或需要认证
```

### 2. 命名规范

- **Entity 字段**：使用业务语义命名（如 `fatherId`、`motherId`），避免口语化命名（如 `husbandId`、`wifeId` 在 Family 实体中）。
- **方法名**：遵循 Java 惯例，动宾结构（如 `findByGenealogyId`、`savePerson`）。
- **包名**：同一业务模块的类放同一包下，避免跨模块直接调用（通过 Service 层解耦）。

### 3. 多人协作时的 API 契约

- **Controller 层**：是外部 API 入口，所有 public 方法应记录 `@GetMapping`/`@PostMapping` 等路由。
- **Service 层**：业务逻辑核心，不允许直接暴露 Repository 给 Controller。
- **Repository 层**：只做数据存取，不要包含业务逻辑。

```
Controller  ←  Service  ←  Repository  ←  Database
(路由+参数)   (业务逻辑)   (数据存取)
```

### 4. 数据库兼容性注意

- 本项目使用 SQLite + Hibernate Community Dialect，部分 SQL 语法与 MySQL/PostgreSQL 不同。
- JPQL 查询中 `NULL` 参数处理需特殊处理（`WHERE :param IS NULL OR t.field = :param`）。
- SQLite 不支持某些窗口函数和高级 SQL 特性，复杂查询优先在 Java 层处理。

### 5. Spring Security 接入后

- 新增 Controller 方法默认需要认证，除非在 `SecurityConfig` 中显式配置 `.permitAll()`。
- 涉及用户数据的接口，从 `Authentication` 对象中获取 `userId`，不要依赖前端传来的 `userId` 参数（安全风险）。
- JWT Token 相关的工具类 `JwtUtil`，确保密钥配置在环境变量中，不要硬编码。

### 6. 前端页面规范

- 所有 HTML 页面放在 `src/main/resources/static/` 目录下。
- API 调用地址使用相对路径（如 `/api/xxx`），避免硬编码 `localhost:8081`。
- 涉及文件上传/下载的页面，确保后端对应接口的 `Content-Type` 和文件流处理正确。

---

## 三、快速修复脚本（供参考）

如果后续代码仍然出现类似问题，可先用以下命令快速定位：

```bash
# 1. 编译检查（最快发现语法错误）
cd /d/project/genealogy-manager && ./mvnw compile

# 2. 查找所有 implements PdfLayoutStrategy 的类
grep -r "implements PdfLayoutStrategy" src/

# 3. 查找所有 throws IOException 但可能抛出 Exception 的方法
grep -r "throws IOException" src/

# 4. 查找 Repository 中所有自定义查询方法是否在 Service 中有对应包装
grep -r "findBy" src/main/java/com/genealogy/repository/
```

---

## 四、总结

本次修复的 15 个编译错误，归类为：

| 类型 | 数量 | 严重程度 |
|------|------|---------|
| 拼写/语法笔误 | 2 | 低（IDE 可自动发现） |
| 接口方法签名不匹配 | 1 | 高（影响整个模块） |
| Entity 字段命名不一致 | 3 | 高（运行时才会暴露） |
| 第三方库 API 错误 | 2 | 高（直接编译失败） |
| Repository 接口扩展缺失 | 2 | 中（影响高级查询） |
| Service 层方法缺失 | 2 | 高（运行时 500 错误） |
| Exception 声明传播断裂 | 3 | 高（编译失败） |

**核心原则：每次写完代码，运行 `mvn compile` 再提交，能避免 80% 的问题。**
