# 后端bug记录以及修复bug记录

## 一、代码 Bug 记录

### 1. 重复业务实现类导致后端编译失败

问题文件：

backend/src/main/java/com/xtu/labequipment/service/BorrowBusinessServiceImpl.java

backend/src/main/java/com/xtu/labequipment/service/impl/BorrowBusinessServiceImpl.java

问题现象：

后端编译时报：

类重复: com.xtu.labequipment.service.impl.BorrowBusinessServiceImpl

原因：

同一个业务实现类 BorrowBusinessServiceImpl 同时存在于两个位置，其中一个放错了包路径。

修改方式：

删除错误位置的文件：

backend/src/main/java/com/xtu/labequipment/service/BorrowBusinessServiceImpl.java

保留正确位置：

backend/src/main/java/com/xtu/labequipment/service/impl/BorrowBusinessServiceImpl.java

修改结果：

删除重复类后，mvn clean compile 编译成功，说明这个代码问题已经解决。

---

### 2. 数据库字段和初始化数据不一致

问题文件：

database/lab_equipment_create.sql

database/lab_equipment_data.sql

问题现象：

导入数据时报：

Unknown column 'is_top' in 'field list'

原因：

lab_equipment_data.sql 里的 notice 表插入语句使用了字段：

is_top

但是建表脚本 lab_equipment_create.sql 里的 notice 表没有这个字段。

修改方式：

应该在 notice 表建表语句中补充字段：

is_top TINYINT DEFAULT 0 COMMENT '是否置顶'

代码层面的结论：

这是一个 SQL 脚本不一致问题，属于数据库初始化脚本 bug，不是 Java 业务代码 bug。

---

### 3. 根路径 / 没有接口

问题表现：

访问：

http://localhost:8080

返回：

{"code":500,"message":"服务器异常：No static resource .","data":null}

原因：

后端没有写：

@GetMapping("/")

也没有静态首页文件。

是否必须改：

不是必须改。因为后端真正的业务接口都在：

/api/...

如果想优化，可以新增一个健康检查接口：

@RestController

public class HomeController {

    @GetMapping("/")

    public Result<String> home() {

        return Result.ok("lab-equipment backend started");

    }

}

当前处理：

这个问题目前只是确认原因，不影响后端运行。

---

### 4. 登录接口返回 user not found

问题表现：

请求：

POST /api/auth/login

返回：

{"code":500,"message":"user not found","data":null}

原因可能有两类：

第一类是数据库里没有 admin001 这个用户。

第二类是新后端登录逻辑和原数据库初始化账号不一致，比如新代码查询的用户名字段、状态字段、角色逻辑和旧数据不完全匹配。

代码层面的检查点：

需要检查：

AuthController.java

AuthService.java

User.java

UserMapper.java

lab_equipment_data.sql

重点看：

username

password

status

roleId / roleCode

是否和数据库字段、初始化数据一致。

当前处理：

这个还没有作为代码 bug 修改，只是接口测试时发现账号不存在，需要继续对照数据库数据和登录逻辑。

---

### 5. application.yml 写死本地数据库密码的问题

问题文件：

backend/src/main/resources/application.yml

问题写法：

username: root

password: lyc512000

问题原因：

这属于本地个人配置。如果提交给别人，别人 MySQL 密码不同，后端就启动不了。

推荐修改：

改成环境变量写法：

spring:

  datasource:

    username: ${MYSQL_USERNAME:root}

    password: ${MYSQL_PASSWORD:123456}

这样代码默认可以用 root / 123456，你本地也可以通过 bat 输入实际密码。

修改意义：

把“个人本地配置”从代码里剥离出来，提升可移植性。

---

# 二、代码修改记录

## 1. 删除重复的 BorrowBusinessServiceImpl

修改类型：

代码结构修复。

修改前：

存在两个 BorrowBusinessServiceImpl，导致类重复。

修改后：

只保留：

backend/src/main/java/com/xtu/labequipment/service/impl/BorrowBusinessServiceImpl.java

删除：

backend/src/main/java/com/xtu/labequipment/service/BorrowBusinessServiceImpl.java

修改效果：

后端从编译失败变成编译成功。

---

## 2. 清理 Java 源码中的多余注释

修改范围：

backend/src/main/java/**/*.java

清理内容：

去掉了：

// 单行注释

/* 多行注释 */

/**

 * 文档注释

 */

保留内容：

保留 Java 代码逻辑、类结构、方法结构、字符串内容。

修改效果：

代码更简洁，适合课程项目提交或答辩展示。

注意：

这个修改主要是代码风格层面的修改，不应该改变业务逻辑。

---

## 3. 整体替换 backend 后端代码

修改类型：

模块整体替换。

涉及范围：

backend/pom.xml

backend/src/main/java/com/xtu/labequipment/**

backend/src/main/resources/application.yml

backend/src/test/**

从 git status 可以看出，这次替换后 backend 中大量 Java 文件发生了修改、删除和新增，主要变化集中在 common、config、controller、dto、entity、mapper、service 等包中。

---

## 4. 认证相关代码发生调整

从文件变化看，旧版本中删除了：

backend/src/main/java/com/xtu/labequipment/common/LoginUser.java

backend/src/main/java/com/xtu/labequipment/util/TokenUtil.java

backend/src/main/java/com/xtu/labequipment/interceptor/AuthInterceptor.java

新增了：

backend/src/main/java/com/xtu/labequipment/common/JwtUtil.java

backend/src/main/java/com/xtu/labequipment/common/PasswordUtil.java

backend/src/main/java/com/xtu/labequipment/common/TokenUser.java

backend/src/main/java/com/xtu/labequipment/config/AuthInterceptor.java

backend/src/main/java/com/xtu/labequipment/config/RoleInterceptor.java

说明：

这说明后端认证体系从原来的：

LoginUser + TokenUtil + interceptor/AuthInterceptor

调整为：

TokenUser + JwtUtil + PasswordUtil + config/AuthInterceptor + RoleInterceptor

设计变化：

旧代码更像自定义 token。

新代码更偏向：

JWT 工具类

密码工具类

登录用户信息 TokenUser

认证拦截器

角色拦截器

结构比之前更清晰。

---

## 5. 权限拦截逻辑位置调整

旧位置：

backend/src/main/java/com/xtu/labequipment/interceptor/AuthInterceptor.java

新位置：

backend/src/main/java/com/xtu/labequipment/config/AuthInterceptor.java

backend/src/main/java/com/xtu/labequipment/config/RoleInterceptor.java

修改意义：

把拦截器相关代码放入 config 包中，和 WebConfig 放在同一配置层，结构上更集中。

设计理解：

AuthInterceptor：负责判断是否登录、token 是否有效

RoleInterceptor：负责判断角色权限

比单个拦截器同时处理所有逻辑更清楚。

---

## 6. 统计控制器名称变化

删除：

backend/src/main/java/com/xtu/labequipment/controller/StatController.java

新增：

backend/src/main/java/com/xtu/labequipment/controller/StatsController.java

修改含义：

统计模块从 StatController 调整为 StatsController。

注意点：

如果前端原来访问的是：

/api/stats/...

需要确认新 StatsController 的路径是否仍然保持一致。否则前端统计页面可能请求不到数据。

---

## 7. 设备分类接口被删除

删除文件：

backend/src/main/java/com/xtu/labequipment/controller/DeviceCategoryController.java

backend/src/main/java/com/xtu/labequipment/service/DeviceCategoryService.java

backend/src/main/java/com/xtu/labequipment/service/impl/DeviceCategoryServiceImpl.java

影响：

如果前端仍然需要设备分类接口，比如查询分类列表、按分类筛选设备，那么要确认新后端是否把分类功能合并到 DeviceController 或其他接口里。

风险点：

这可能影响前端设备管理页面中的分类下拉框。

---

## 8. Service 层结构大幅精简

删除了很多原来的基础 Service 接口和实现类，例如：

BorrowApplyService

BorrowRecordService

DeviceService

NoticeService

RepairRecordService

RoleService

UserService

以及对应的：

service/impl/*ServiceImpl.java

说明：

新代码可能不再使用 MyBatis-Plus 的 IService + ServiceImpl 风格，而是直接在 Controller 或业务 Service 中调用 Mapper。

设计变化：

旧结构：

Controller

  ↓

Service

  ↓

ServiceImpl

  ↓

Mapper

新结构可能变成：

Controller

  ↓

业务 Service / Mapper

  ↓

Mapper

优点：

代码层级更少。

缺点：

如果 Controller 里直接写太多业务逻辑，后期维护性会下降。

---

## 9. 新增 MyBatis-Plus 配置类

新增文件：

backend/src/main/java/com/xtu/labequipment/config/MybatisPlusConfig.java

作用：

通常用于配置 MyBatis-Plus 分页插件等内容。

可能包含：

MybatisPlusInterceptor

PaginationInnerInterceptor

修改意义：

如果后端有分页查询，例如设备列表、用户列表、借用记录列表，这个配置可以让分页功能更规范。

---

## 10. 新增密码工具类 PasswordUtil

新增文件：

backend/src/main/java/com/xtu/labequipment/common/PasswordUtil.java

作用推测：

用于统一处理密码校验或密码加密。

设计意义：

把密码处理从 AuthService 中抽出来，避免登录业务里直接明文比较密码。

需要继续确认：

如果数据库里还是明文 123456，那么 PasswordUtil 要么支持明文校验，要么数据库里的密码也要配套加密。否则会出现登录失败。

---

## 11. 新增 JWT 工具类 JwtUtil

新增文件：

backend/src/main/java/com/xtu/labequipment/common/JwtUtil.java

替代旧文件：

backend/src/main/java/com/xtu/labequipment/util/TokenUtil.java

修改意义：

认证 token 逻辑从自定义 TokenUtil 改为 JwtUtil，命名更规范，也更接近常见后端设计。

---

## 12. 新增 TokenUser

新增文件：

backend/src/main/java/com/xtu/labequipment/common/TokenUser.java

替代旧文件：

backend/src/main/java/com/xtu/labequipment/common/LoginUser.java

修改意义：

登录后的用户上下文对象从 LoginUser 改成 TokenUser。

影响范围：

对应需要修改：

AuthContext.java

AuthInterceptor.java

RoleInterceptor.java

AuthService.java

否则会出现类型不一致。

---

## 13. 修改 AuthContext

修改文件：

backend/src/main/java/com/xtu/labequipment/common/AuthContext.java

修改原因：

原来可能保存的是：

LoginUser

现在应该保存：

TokenUser

修改意义：

配合新的 JWT 登录用户模型，让业务代码可以通过上下文拿到当前登录用户。

---

## 14. 修改 AuthService

修改文件：

backend/src/main/java/com/xtu/labequipment/service/AuthService.java

主要变化：

应该配合新认证体系修改了：

登录校验

密码校验

token 生成

注册逻辑

返回 LoginResponse

需要重点检查：

因为现在登录 admin001 / 123456 返回 user not found，所以 AuthService 里查询用户的逻辑需要重点检查。

重点看它是不是查询：

username

还是查询：

account

phone

studentNo

如果数据库字段不对应，就会登录失败。

---

## 15. 修改 Controller 层

这次大量 Controller 被修改，包括：

AuthController

BorrowController

DeviceAvailabilityController

DeviceController

MyRecordController

NoticeController

OverdueController

ProfileController

RepairController

UserController

并删除/替换了：

DeviceCategoryController

StatController

新增：

StatsController

整体说明：

Controller 层接口应该被重新整理过，可能统一了路径、权限、返回格式或调用逻辑。

需要关注：

前端页面里请求的接口路径是否和新 Controller 保持一致。

---

## 16. 修改 DTO 层

修改和删除的 DTO 包括：

ApproveBorrowRequest

LoginRequest

LoginResponse

RegisterRequest

RepairHandleRequest

ReturnDeviceRequest

删除：

ChangePasswordRequest

DeviceAvailabilityVO

OverdueRecordVO

说明：

请求参数和返回对象被调整过。

风险点：

如果前端仍然按旧字段提交请求，比如：

{

  "username": "...",

  "password": "..."

}

而新 DTO 改了字段名，就会导致接口参数接收失败。

---

## 17. 修改 Entity 层

修改的实体类包括：

BorrowApply

BorrowRecord

Device

DeviceCategory

Notice

RepairRecord

Role

User

说明：

这些类和数据库表字段对应关系被调整过。

重点风险：

实体字段必须和数据库表保持一致。

比如：

User.java 的 username/password/status/roleId

Notice.java 的 isTop

Device.java 的 status/categoryId

如果字段不一致，就会出现查询为空、插入失败、字段映射失败。

---

## 18. 修改 Mapper 层

修改的 Mapper 包括：

BorrowApplyMapper

BorrowRecordMapper

DeviceCategoryMapper

DeviceMapper

NoticeMapper

RepairRecordMapper

RoleMapper

UserMapper

说明：

Mapper 层可能增加了自定义 SQL 查询方法，或者调整了基础继承关系。

需要关注：

如果 Mapper 方法名改了，对应 Controller 或 Service 里的调用也必须同步修改。

---

## 19. 删除测试类

删除了：

backend/src/test/java/com/xtu/labequipment/BackendApiSmokeTests.java

backend/src/test/java/com/xtu/labequipment/LabEquipmentApplicationTests.java

影响：

不会影响后端正常运行，但会减少自动测试覆盖。

---

## 20. 新增后端启动脚本 run_backend.bat

新增文件：

backend/run_backend.bat

功能：

1. 检查 Java

2. 检查 Maven

3. 检查 MySQL80 服务

4. 输入数据库账号密码

5. 设置环境变量

6. 执行 mvn spring-boot:run

关键修复：

bat 中 Maven 命令必须用：

call mvn -version

call mvn spring-boot:run

不能直接写：

mvn -version

mvn spring-boot:run

否则 bat 可能执行到 Maven 后提前退出。

---

# 三、当前代码层面最需要继续检查的点

## 1. 登录账号问题

现在后端能启动，但：

user not found

说明登录逻辑和数据库账号需要继续核对。

重点检查：

AuthService.java

User.java

UserMapper.java

lab_equipment_data.sql

## 2. 前端接口路径和新 Controller 是否一致

因为新后端删除了部分 Controller，新增了 StatsController，所以要检查前端请求路径是否变化。

重点检查：

frontend/*.html

里面所有：

fetch(...)

axios(...)

请求是否和后端接口一致。

## 3. 数据库字段和 Entity 是否一致

重点检查：

User.java ↔ user 表

Notice.java ↔ notice 表

Device.java ↔ device 表

BorrowApply.java ↔ borrow_apply 表

BorrowRecord.java ↔ borrow_record 表

RepairRecord.java ↔ repair_record 表

尤其是：

is_top

role_id

status

username

password

## 4. 删除 Service 层后业务逻辑是否仍然清晰

新代码删除了很多 Service + ServiceImpl，如果业务逻辑直接写到 Controller 或 Mapper 调用中，需要注意后期维护性。

---

# 四、精简版代码修改总结

本次后端代码主要完成了以下修改：首先，删除了错误路径下重复的 BorrowBusinessServiceImpl 实现类，解决了后端编译时类重复的问题。其次，对后端 Java 源码进行了注释清理，去除了多余的单行注释、多行注释和文档注释，使代码结构更加简洁。随后，对 backend 模块进行了整体替换和结构调整，认证模块由原来的 LoginUser、TokenUtil、AuthInterceptor 结构调整为 TokenUser、JwtUtil、PasswordUtil、AuthInterceptor、RoleInterceptor 的组合，使登录认证和角色权限控制更加清晰。统计模块由 StatController 调整为 StatsController，同时新增 MybatisPlusConfig 配置类，完善 MyBatis-Plus 相关配置。最后，新增 run_backend.bat 启动脚本，用于自动检查 Java、Maven、MySQL 服务并启动 Spring Boot 后端，提高项目运行便利性。
